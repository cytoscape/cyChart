package org.cytoscape.cyChart.internal.charts.oneD;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.cyChart.internal.FilterBuilder;
import org.cytoscape.cyChart.internal.charts.AbstractChartController;
import org.cytoscape.cyChart.internal.charts.Borders;
import org.cytoscape.cyChart.internal.charts.LogarithmicAxis;
import org.cytoscape.cyChart.internal.charts.Range;
import org.cytoscape.cyChart.internal.charts.StringUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class HistogramChartController extends AbstractChartController
{
	private static final int INTERACTIVE = 500;
  	private  LineChart<Number, Number> histogramChart;	
	private SubRangeLayer1D subrangeLayer;
	// ------------------------------------------------------
	public HistogramChartController(StackPane parent, CyServiceRegistrar reg, CyColumn column) {
		super(parent, reg, false);
	}
 	
	// ------------------------------------------------------
	@Override protected void makeFilter() {
		if (registrar != null) {		
			String x = xAxisChoices.getSelectionModel().getSelectedItem();
		    FilterBuilder builder = new FilterBuilder(x, new Range(startX, endX));
		    builder.makeSingleFilter(registrar);
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
		if (chartPlotArea != null)
		{
			Region rgn = (Region) chartPlotArea;
			rgn.setStyle("-fx-background-color: #CCCCCC;");
			rgn.setBorder(Borders.thinEtchedBorder);
		}

		Group groupH = subrangeLayer.getSubRangeGroup();
		Bounds bounds = getPlotAreaNode().getBoundsInParent();
		groupH.setTranslateX(bounds.getMinX());
		groupH.setTranslateY(36);
		if (StringUtil.isEmpty(name)) return;
		
		if (h1 != null)
		{
			Range histoRange = h1.getRange();
			histogramChart.getData().clear();
			histogramChart.getData().add(h1.getDataSeries(name) );
			xAxis.setLowerBound(histoRange.min());
			xAxis.setUpperBound(histoRange.max());
			boolean disable = histoRange.contains(0.);
			logXTransform.setDisable(disable);
			yAxis.setLowerBound(0);
			double top = .5 * h1.getMode() / h1.getSize();
			yAxis.setUpperBound(top);
			int size = getDataSize();
			interactive.setSelected(size > 0 && size < INTERACTIVE);
		}
	}
	
	
	private void noHistogram() {
		logXTransform.setDisable(true);			// number fields should also disable
		
	}
	// ------------------------------------------------------
	private Histogram1D getHistogram(String item, Boolean isLog) {
	nodeTable = getCurrentNodeTable(); 
		List<Double> values = null;
		CyColumn column = nodeTable.getColumn(item);
		if (column.getType() == Double.class)
		{
			values = nodeTable.getColumn(item).getValues(Double.class);
			if (values == null || values.isEmpty()) return null;
			if (isLog)
				for (int i=0; i<values.size(); i++)
				{
					Double dub = values.get(i);
					values.set(i, safelog(dub));
				}
		}
		else if (column.getType() == Integer.class)
		{
			values = new ArrayList<Double>();
			for (Integer i : nodeTable.getColumn(item).getValues(Integer.class))
			{
				double d = isLog ? safelog((double) i) : new Double(i);
				values.add(d);
			}
		}
		return new Histogram1D(item, values);
	}
	// ------------------------------------------------------
	public void resized()
	{
		subrangeLayer.chartBoundsChanged();
		setStatus("" + subrangeLayer.getYValue());
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
		if (row == null) {		System.out.println("row is null");		return false;	}
		if (col == null) {		System.out.println("col is null");		return false;	}
		
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