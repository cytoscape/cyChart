package org.cytoscape.cyChart.internal.charts.oneD;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.cyChart.internal.FilterBuilder;
import org.cytoscape.cyChart.internal.charts.AbstractChartController;
import org.cytoscape.cyChart.internal.charts.StringUtil;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.cyChart.internal.model.LinearRegression;
import org.cytoscape.cyChart.internal.model.LogarithmicAxis;
import org.cytoscape.cyChart.internal.model.Range;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/*
 * Controller class for the one dimensional CyChart
 */


public class HistogramChartController extends AbstractChartController
{
	private static final int INTERACTIVE = 500;
  	private  LineChart<Number, Number> histogramChart;	
	private SubRangeLayer1D subrangeLayer;

	private Histogram1D histogram;			// we'll need to access this to calc stats
	public Histogram1D getCurrentHistogram()	{ return histogram;	}
	// ------------------------------------------------------
	public HistogramChartController(StackPane parent, CyServiceRegistrar reg, CyChartManager mgr) {
		super(parent, reg, false, mgr);
//		System.out.println("HistogramChartController");
	}
 
	// WATCH OUT:  the class Initializable may not be in the included JavaFX libraries
//	@Override
//	public void initialize(URL url, ResourceBundle bundle) {
//		super.initialize(url, bundle);
//		
//	}
	// ------------------------------------------------------
/*
 *  Respond to the Create Filter button (top left)
 */
	@Override protected void makeFilter() {	if (registrar != null) {		
			String x = xAxisChoices.getSelectionModel().getSelectedItem();
		    FilterBuilder builder = new FilterBuilder(x, new Range(startX, endX));
		    builder.makeSingleFilter(registrar);
		    selectFilterPanel();
		}
	 }
	// ------------------------------------------------------
/*
 * 	Look at the popup and set the axis accordingly
 */
	public void setParameters()
	{
		setXParameter(xAxisChoices.getSelectionModel().getSelectedItem());
	}
	// ------------------------------------------------------
	// this recreates the entire chart, axes, data series, regression, etc.
	boolean verbose = false;
	
	/*
	 * Assign the parameter to use, and rebuild everything.  
	 * Only called by the method that checks the ChoiceBox
	 * 
	 *  @param name  The column name to go on the domain axis
	 */
	
	private void setXParameter(String name)
	{
		xColumn = findColumn(name);
		if (xColumn == null)  { noHistogram();	return;  }
		histogram = getHistogram(name, isXLog);
		if (histogram == null)  { noHistogram();	return;  }
		chartBox.getChildren().clear();
		if (subrangeLayer != null) subrangeLayer.hideSelection();
		if (isXLog)
		{
			logXTransform.setSelected(false);
			return;
		}
		xAxis = isXLog ? new LogarithmicAxis() : new NumberAxis();
		xAxis.setLabel(name);
		yAxis = new NumberAxis();		//LogarithmicAxis();   
		histogramChart = new LineChart<Number, Number>(xAxis, yAxis);
		histogramChart.setCreateSymbols(false);
		anchor(histogramChart);
		setChart(histogramChart);
		chartBox.getChildren().add(histogramChart);
		subrangeLayer = new SubRangeLayer1D(histogramChart, chartContainer, this);
		Node chartPlotArea = getPlotAreaNode();
		
		// TODO set the styles from a resource file
		
		String rootStr = ".root {\n    -fx-font-size: 24pt;\n -fx-font-family: \"Courier New\";\n" + 
				" -fx-base: rgb(132, 145, 47);\n   -fx-background: rgb(240, 240, 240);\n -fx-legend-visible: false; }";

		if (chartPlotArea != null)
		{
			Region rgn = (Region) chartPlotArea;
			rgn.setStyle(".root { -fx-background-color: #F8F0F8;  -fx-legend-visible: false; }");
//			rgn.setBorder(Borders.thinEtchedBorder);
		}
		histogramChart.setStyle(rootStr);
		
			
//		System.out.println("C");
		Group groupH = subrangeLayer.getSubRangeGroup();
		Bounds bounds = getPlotAreaNode().getBoundsInParent();
		groupH.setTranslateX(bounds.getMinX());
		groupH.setTranslateY(36);
		if (StringUtil.isEmpty(name)) return;
		
		if (histogram == null) return;

		Range histoRange = histogram.getRange();
//			h1.dump();
		histogramChart.getData().clear();
		
		boolean isInt = xColumn != null && xColumn.getType().equals(Integer.class);
		double area = isInt ? 1 : histogram.getArea();
		XYChart.Series<Number, Number> data = histogram.getDataSeries(name,0.,area);
		histogramChart.getData().add(data);
		int minX = (int) histoRange.min();
		int maxX = (int) histoRange.max();
		xAxis.setLowerBound(minX);
		xAxis.setUpperBound(maxX);
		
		boolean disableLog = histoRange.contains(0.);
		logXTransform.setDisable(disableLog);
		yAxis.setLowerBound(0);
		double top =  histogram.getMode();
		if (!isInt) top = .5 * top / histogram.getSize();
		yAxis.setUpperBound(top);
		int size = getDataSize();
		interactive.setSelected(size > 0 && size < INTERACTIVE);
		boolean showRegression = curveFit.isSelected();

		if (showRegression)
		{
			List<Double> xs = new ArrayList<Double>();
			List<Double> ys = new ArrayList<Double>();
			for (int i=minX; i < maxX; i++)
			{
				double count = histogram.get(i);
				if (count <= 0) continue;
//				xs.add(Math.log(Double.valueOf(i)));
				xs.add(Double.valueOf(i));
				ys.add(Math.log(count));
//				raw.getData().add(new XYChart.Data<Number, Number>(Double.valueOf(i),Math.log(count)));
				if (verbose)		
					System.out.println(Double.valueOf(i) + "\t" + count);
			}
			LinearRegression foo = new LinearRegression(xs, ys);
			if (verbose)	
				System.out.println(foo.toString());
	
			XYChart.Series<Number, Number> regression = new XYChart.Series<Number, Number>();
			histogramChart.getData().add(regression);
			regression.nameProperty().set("Log Regression");

			for (int i=minX; i < maxX; i++)
			{
				double xi = i;
				double yi = foo.predict(xi);
				double powx = Math.pow(Math.E, xi);
				double powy = Math.pow(Math.E, yi);
				if (verbose)	System.out.println(" -> " + xi + ", " + powy + " - " + yi);
				regression.getData().add(new XYChart.Data<Number, Number>(xi,powy));
			}
		}
	}
	
	//----------------------------------------------------------------------------
	//----------------------------------------------------------------------------
/*
 * Set the dialog to an disabled state, because data is unassigned or network is inactive
 */
	private void noHistogram() {
		logXTransform.setDisable(true);			
		xMin.setDisable(true);			// number fields should also disable
		xMax.setDisable(true);
		
	}
	// ------------------------------------------------------
/*	
 * Create the Histogram structure from the name of the desired column
 * 
 * @param item - the name of the column with the data to bin
 * @param isLog - switch to enable log binning
 */
	private Histogram1D getHistogram(String item, Boolean isLog) 
	{
		try 
		{
			table = manager.getCurrentTable(); 
	
			List<Double> values = null;
			CyColumn column = table.getColumn(item);
			if (column == null)
			{
				System.err.println("column is null for " + item);
				return null;
			}
			if (column.getType() == Double.class)
			{
				values = table.getColumn(item).getValues(Double.class);
				if (values == null || values.isEmpty()) return null;
				if (isLog)
					for (int i=0; i<values.size(); i++)
					{
						Double dub = values.get(i);
						if (dub == null) continue;
						values.set(i, safelog(dub));
					}
				return new Histogram1D(item, values);
			}
			
			if (column.getType() == Integer.class)
			{
				int min = Integer.MAX_VALUE;
				int max = Integer.MIN_VALUE;
				values = new ArrayList<Double>();
				for (Integer i : column.getValues(Integer.class))
				{
					if (i == null) continue;
					if (i<min) min = i;
					if (i>max) max = i;
					double d = isLog ? safelog((double) i) : new Double(i);
					values.add(d);
				}
				Range r = new Range(min, max);
				Histogram1D hi = new Histogram1D(item, r, max-min+1);
				for (Integer i : column.getValues(Integer.class))
				{
					hi.count(i);
				}
				return hi;
		}
		}
		catch (Exception ex)
		{
			System.err.println("Exception " + ex.toString());
			ex.printStackTrace();
		}
		return null;
	}
	// ------------------------------------------------------
	/**
	 *  Reset the parameters after the window has change size
	 */
	
	
	public void resized()
	{
		if (subrangeLayer != null)
		{
			subrangeLayer.chartBoundsChanged();
			setStatus("");	// + subrangeLayer.getYValue()
		}
	}
	/**
	 *  In response to the check box, replot with or without regression
	 */
	
	
	protected void regression(boolean vis)
	{
		setParameters();
	}
	// ------------------------------------------------------
	boolean inBounds(double x)
	{
		double low = xAxis.getLowerBound();
		double up = xAxis.getUpperBound();
		return (x >= low && x <= up);
	}

	// ------------------------------------------------------
	public void selectRange(String name, double xMin, double xMax) {
		if (name == null) name = xAxis.getLabel();
		CyColumn col = findColumn(name);
		if (col  != null)
			selectRange(col, xMin, xMax);
	}
	
	public void selectRange(CyColumn col, double xMin, double xMax) 
	{
		for (CyRow row : table.getAllRows())
		{	
			boolean selected = rowMatch(row, col, xMin, xMax);
			row.set(CyNetwork.SELECTED, selected);
//			if (selected) System.out.println("selected ");
		}
	}
	/*
	 * Check if the data at row,col is in bounds
	 * @param row The CyRow structure
	 * @param col The CyColumn structure
	 * @param xMin Lower bounds of the X range
	 * @param xMax Upper bounds of the X range
	 * 
	 */
	
	private boolean rowMatch(CyRow row, CyColumn col, double xMin, double xMax) {
		if (row == null) {		System.err.println("row is null");		return false;	}
		if (col == null) {		System.err.println("col is null");		return false;	}
		
		Object val = row.get(col.getName(), col.getType());
		if (val == null) return false;
		if (val instanceof Double)
		{ 
			double v = (Double) val;
			if (isXLog) v = safelog(v);
			boolean hit = (xMin <= v && xMax >= v);
			return hit;
		}
		if (val instanceof Integer)
		{ 
			Integer i = (Integer) val;
			double d = i;
			if (isXLog) 
				d = safelog(d);
			boolean hit = (xMin <= d && xMax >= d);
			return hit;
		}
		return false;
		
	}
	/*
	 * Kick the update after user manually edited a value
	 */

	@Override
	public void 	fieldEdited(String fldId, BigDecimal newValue)
	{
		super.fieldEdited(fldId, newValue);
		subrangeLayer.chartBoundsChanged();	
		subrangeLayer.reportRange();
	}
	
	/*
	 * Update the UI after user dragged the selection
	 */

	@Override
	public void resizeRangeFields() {
		if (subrangeLayer  != null)
		{
			subrangeLayer.reportRange();
			subrangeLayer.chartBoundsChanged();	
		}
		
		
	}
}