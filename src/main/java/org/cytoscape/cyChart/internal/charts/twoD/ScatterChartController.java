package org.cytoscape.cyChart.internal.charts.twoD;

import java.util.List;

import org.cytoscape.cyChart.internal.charts.AbstractChartController;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.cyChart.internal.model.LinearRegression;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;


public class ScatterChartController extends AbstractChartController
{

//	public ScatterChartController(StackPane parent, CyServiceRegistrar reg, CyColumn xCol) {
//		this(parent, reg, xCol, null);
//	}
	public ScatterChartController(StackPane parent, CyServiceRegistrar reg, CyChartManager mgr) {
		super(parent, reg, true, mgr);
		CyColumn xcol = mgr.getXColumn();
		CyColumn ycol = mgr.getYColumn();
		if (xcol != null)
			xAxisChoices.getSelectionModel().select(xcol.getName());
		else
			xAxisChoices.getSelectionModel().select(0);
		
		if (ycol != null) 
			yAxisChoices.getSelectionModel().select(ycol.getName());
		else
			yAxisChoices.getSelectionModel().select(1);
	}
	// ------------------------------------------------------

	static int DOT_SIZE = 4; 
	private SelectableScatterChart scatterChartHome;

	@Override protected void fillInHeader()
	{
		super.fillInHeader();
		curveFit = new CheckBox("Regression");
			ChangeListener<? super Boolean> regressCheckChange = new ChangeListener<Boolean>() {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> ov,
			        Boolean old_val, Boolean new_val) {
			        linearRegression(new_val);	
			    }};
		curveFit.selectedProperty().addListener(regressCheckChange);
		curveFit.setTranslateY(4);
		header1.getChildren().add(curveFit);
	}

	public void setParameters()
	{
		if (chartBox != null)
		{
			curveFit.setSelected(false);
			chartBox.getChildren().clear();
			String x = xAxisChoices.getSelectionModel().getSelectedItem();
			String y = yAxisChoices.getSelectionModel().getSelectedItem();
//		    System.out.println(x + (isXLog ? " (Log)" : " (Lin)") + " v.  " + y + (isYLog ? " (Log)" : " (Lin)"));
			XYChart.Series<Number, Number> series1 = getDataSeries(x, y);
			scatterChartHome = new SelectableScatterChart(this);
			AnchorPane.setLeftAnchor(scatterChartHome, 0.);
			AnchorPane.setRightAnchor(scatterChartHome, 0.);
			if (series1 != null)
			{
				scatterChartHome.setDataSeries(series1);
		        for (XYChart.Data<Number, Number> dataVal : series1.getData()) {
		        	StackPane stackPane =  (StackPane) dataVal.getNode();
		        	if (stackPane != null)
		        		stackPane.setPrefSize(DOT_SIZE, DOT_SIZE);
		        }
			}
			scatterChartHome.setAxes(x, y);
			if (xAxis == null) xAxis = (ValueAxis<Number>) scatterChartHome.getScatterChart().getXAxis(); 
			if (yAxis == null) yAxis = (ValueAxis<Number>) scatterChartHome.getScatterChart().getYAxis(); 
			logXTransform.setDisable(xAxis == null || xAxis.getLowerBound() <= 0);
			logYTransform.setDisable(yAxis == null || yAxis.getLowerBound() <= 0);

			setChart(scatterChartHome.getScatterChart());
			chartBox.getChildren().add(scatterChartHome);

     		Node legend = scatterChartHome.lookup(".chart-legend");
		    if (legend != null && legend.isVisible()) 
		    	legend.setVisible(false);
		}
	}
	
//alt method:	https://math.stackexchange.com/questions/3625/easy-to-implement-method-to-fit-a-power-function-regression
	protected void linearRegression(boolean visible)
	{
//		System.out.println("linearRegression " + (visible ? "on" : "off"));
	
		if (!visible)
		{
			scatterChartHome.clearRegression();
			return;

		}
		String x = xAxisChoices.getSelectionModel().getSelectedItem();
		String y = yAxisChoices.getSelectionModel().getSelectedItem();
//	    System.out.println(x + (isXLog ? " (Log)" : " (Lin)") + " v.  " + y + (isYLog ? " (Log)" : " (Lin)"));
		XYChart.Series<Number, Number> series1 = getDataSeries(x, y);
		int seriesSize = series1.getData().size();
		double[] X = new double[seriesSize];
		double[] Y = new double[seriesSize];
		for (int i=0; i<seriesSize; i++)
		{
			Data<Number, Number> d = series1.getData().get(i);
			X[i] = (double) d.getXValue();
			Y[i] = (double) d.getYValue();
		}
		
		scatterChartHome.setRegression(new LinearRegression(X, Y));
		scatterChartHome.resized();
	}

	protected void logRegression(boolean visible)
	{
//		System.out.println("linearRegression " + (visible ? "on" : "off"));
	
		if (!visible)
		{
			scatterChartHome.clearRegression();
			return;
		}
		String x = xAxisChoices.getSelectionModel().getSelectedItem();
		String y = yAxisChoices.getSelectionModel().getSelectedItem();
//	    System.out.println(x + (isXLog ? " (Log)" : " (Lin)") + " v.  " + y + (isYLog ? " (Log)" : " (Lin)"));
		XYChart.Series<Number, Number> series1 = getDataSeries(x, y);
		int seriesSize = series1.getData().size();
		double[] X = new double[seriesSize];
		double[] Y = new double[seriesSize];
		for (int i=0; i<seriesSize; i++)
		{
			Data<Number, Number> d = series1.getData().get(i);
			X[i] = Math.log((double) d.getXValue());
			Y[i] = Math.log((double) d.getYValue());
		}
		
		scatterChartHome.setRegression(new LinearRegression(X, Y));
		scatterChartHome.resized();
	}

	
	private XYChart.Series<Number, Number>  getDataSeries(String xName, String yName) {
		nodeTable = getCurrentNodeTable(); 
		if (nodeTable == null) return null;
		CyColumn xcol = nodeTable.getColumn(xName);
		if (xcol == null) return null; 
		CyColumn ycol = nodeTable.getColumn(yName);
		if (ycol == null) return null;
		List<Double> xvalues = getColumnValues(xcol);
		List<Double> yvalues = getColumnValues(ycol);
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
		series.nameProperty().set("");
		try
		{
			ObservableList<Data<Number, Number>>  data = series.getData();
			int size = xvalues.size();
			for (int i = 0; i < size; i++)
			{
				Double x = xvalues.get(i);
				if (x == null) continue;
				Double y = yvalues.get(i);	
				if (y == null) continue;
				
				if (isYLog)
					y = -1 * safelog(y);
				
				data.add(new XYChart.Data<Number, Number>(x,y));
			}
		}
		catch (Exception e)		{			e.printStackTrace();		}
		return series;
	}
	//------------------------------------------------------------------
	public void resized()
	{
		if (scatterChartHome != null) 
		{
			scatterChartHome.resized();
			setStatus("" + scatterChartHome.offsetX);
		}
	}
	
//	//------------------------------------------------------------------
//	public void setSelectionValues(double selStart, double selEnd, double yStart, double yEnd) 
//	{
//		if (Double.isNaN(selStart) || Double.isNaN(selEnd)) return;
//		startX = Math.min(selStart, selEnd);
//		endX = Math.max(selStart, selEnd);
//		startY = Math.min(yStart, yEnd);
//		endY = Math.max(yStart, yEnd);
//	}
//
	//------------------------------------------------------------------
	public void selectRange(String xname, double xMin, double xMax, String yname, double yMin, double yMax) {
		
		startX = xMin;	endX = xMax;
		startY = yMin; 	endY = yMax;
		CyColumn xcol = findColumn(xname);
		CyColumn ycol = findColumn(yname);
		if (xcol == null || ycol == null) return;
		selectRange(xcol, xMin, xMax, ycol, yMin, yMax);
	}
	

	public void selectRange(CyColumn col, double xMin, double xMax, CyColumn ycol, double yMin, double yMax) 
	{
		if (nodeTable == null) return;
		for (CyRow row : nodeTable.getAllRows())
		{	
			boolean selectedX =  (rowMatch(row, col, xMin, xMax));
			boolean selectedY =  (rowMatch(row, ycol, yMin, yMax));
			boolean selected = selectedX && selectedY;
			row.set(CyNetwork.SELECTED, selected);
//			System.out.println((selected ? "selecting " : "deselecting ") + row.get("SUID", Long.class));
		}
	}
	//------------------------------------------------------------------
	private boolean rowMatch(CyRow row, CyColumn col, double xMin, double xMax) {
		if (row == null) {		System.err.println("row is null");		return false;	}
		if (col == null) {		System.err.println("col is null");		return false;	}
		
//		System.out.println(String.format("col %s (%.2f - %.2f)", col.getName(), xMin, xMax));
		Object val = row.get(col.getName(), col.getType());
		if (val == null) return false;
//		System.out.println("" + val);
		if (val instanceof Double)
		{ 
			Double v = (Double) val;
			if (isXLog) v = safelog(v);
			boolean hit = (xMin <= v && xMax >= v);
			return hit;
		}
		if (val instanceof Integer)
		{ 
			double v = 1.0 * (Integer) val;
			if (isXLog) 
				v = safelog(v);
			boolean hit = (xMin <= v && xMax >= v);
			return hit;
		}
		return false;
		
	}
}