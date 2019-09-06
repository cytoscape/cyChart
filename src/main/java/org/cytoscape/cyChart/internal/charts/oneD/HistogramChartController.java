package org.cytoscape.cyChart.internal.charts.oneD;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.cytoscape.cyChart.internal.FilterBuilder;
import org.cytoscape.cyChart.internal.charts.AbstractChartController;
import org.cytoscape.cyChart.internal.charts.StringUtil;
import org.cytoscape.cyChart.internal.model.CyChartManager;
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

public class HistogramChartController extends AbstractChartController
{
	private static final int INTERACTIVE = 500;
  	private  LineChart<Number, Number> histogramChart;	
	private SubRangeLayer1D subrangeLayer;
	// ------------------------------------------------------
	public HistogramChartController(StackPane parent, CyServiceRegistrar reg, CyChartManager mgr) {
		super(parent, reg, false, mgr);
	}
 	

	@Override
	public void initialize(URL url, ResourceBundle bundle) {
		super.initialize(url, bundle);
		
	}
	// ------------------------------------------------------
	@Override protected void makeFilter() {
		if (registrar != null) {		
			String x = xAxisChoices.getSelectionModel().getSelectedItem();
		    FilterBuilder builder = new FilterBuilder(x, new Range(startX, endX));
		    builder.makeSingleFilter(registrar);
		    selectFilterPanel();
		}
	 }
	// ------------------------------------------------------
	public void setParameters()
	{
		setXParameter(xAxisChoices.getSelectionModel().getSelectedItem());
	}
	// ------------------------------------------------------
	// this recreates the entire chart, axes, data series, etc.
	public void setXParameter(String name)
	{
	System.out.println(name);
//	if (xColumn == null) return;
//	if (xColumn.getType().equals(Integer.class))
//		System.out.println("INTEGER COLUMN");
//		int nBins = column.getRange().
		xColumn = findColumn(name);
		if (xColumn == null)  { noHistogram();	return;  }
		Histogram1D h1 = getHistogram(name, isXLog);
		if (h1 == null)  { noHistogram();	return;  }
		chartBox.getChildren().clear();
		if (subrangeLayer != null) subrangeLayer.hideSelection();
		if (isXLog)
		{
			logXTransform.setSelected(false);
			return;
		}
		xAxis = isXLog ? new LogarithmicAxis() : new NumberAxis();
		xAxis.setLabel(name);
		yAxis = new NumberAxis();
		histogramChart = new LineChart<Number, Number>(xAxis, yAxis);
		histogramChart.setCreateSymbols(false);
		anchor(histogramChart);
		setChart(histogramChart);
		chartBox.getChildren().add(histogramChart);
		subrangeLayer = new SubRangeLayer1D(histogramChart, chartContainer, this);
		Node chartPlotArea = getPlotAreaNode();
		String rootStr = ".root {\n    -fx-font-size: 24pt;\n -fx-font-family: \"Courier New\";\n" + 
				" -fx-base: rgb(132, 145, 47);\n   -fx-background: rgb(240, 240, 240);\n -fx-legend-visible: false; }";

		if (chartPlotArea != null)
		{
			Region rgn = (Region) chartPlotArea;
			rgn.setStyle(".root { -fx-background-color: #F8F0F8;  -fx-legend-visible: false; }");
//			rgn.setBorder(Borders.thinEtchedBorder);
		}
		histogramChart.setStyle(rootStr);
		Group groupH = subrangeLayer.getSubRangeGroup();
		Bounds bounds = getPlotAreaNode().getBoundsInParent();
		groupH.setTranslateX(bounds.getMinX());
		groupH.setTranslateY(36);
		if (StringUtil.isEmpty(name)) return;
		
		if (h1 != null)
		{
			Range histoRange = h1.getRange();
			h1.dump();
			histogramChart.getData().clear();
			
			boolean isInt = xColumn != null && xColumn.getType().equals(Integer.class);
			double area = isInt ? 1 : h1.getArea();
			XYChart.Series<Number, Number> data = h1.getDataSeries(name,0.,area);
			histogramChart.getData().add(data);
			xAxis.setLowerBound(histoRange.min());
			xAxis.setUpperBound(histoRange.max());
			boolean disable = histoRange.contains(0.);
			logXTransform.setDisable(disable);
			yAxis.setLowerBound(0);
			double top =  h1.getMode();
			if (!isInt) top = .5 * h1.getMode() / h1.getSize();
			yAxis.setUpperBound(top);
			int size = getDataSize();
			interactive.setSelected(size > 0 && size < INTERACTIVE);
		}
	}
	
	
	private void noHistogram() {
		logXTransform.setDisable(true);			
		xMin.setDisable(true);			// number fields should also disable
		xMax.setDisable(true);
		
	}
	// ------------------------------------------------------
	private Histogram1D getHistogram(String item, Boolean isLog) 
	{
		try 
		{
			nodeTable = getCurrentNodeTable(); 
	
			List<Double> values = null;
			CyColumn column = nodeTable.getColumn(item);
			if (column == null)
			{
				System.err.println("column is null for " + item);
				return null;
			}
			if (column.getType() == Double.class)
			{
				values = nodeTable.getColumn(item).getValues(Double.class);
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
	public void resized()
	{
		if (subrangeLayer != null)
		{
			subrangeLayer.chartBoundsChanged();
			setStatus("" + subrangeLayer.getYValue());
		}
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
		for (CyRow row : nodeTable.getAllRows())
		{	
			boolean selected = rowMatch(row, col, xMin, xMax);
			row.set(CyNetwork.SELECTED, selected);
//			if (selected) System.out.println("selected ");
		}
	}
	
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
}