package org.cytoscape.cyChart.internal.charts.twoD;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JLabel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ScatterChartController implements Initializable
{
  private Pane chartBox;
  private StackPane chartContainer;
  private ChoiceBox<String> columnChoices;
  private ChoiceBox<String> yAxisChoices;
  private NumberAxis xAxis;
  private NumberAxis yAxis;
	private final CyApplicationManager applicationManager;
	private CyTable nodeTable;
	CyServiceRegistrar registrar;
	private SelectableScatterChart scatterChartHome;
	JLabel statusLabel;
	
	// use this if you don't use FXML to define the chart
	public ScatterChartController(StackPane parent, CyServiceRegistrar reg, JLabel status) {
		chartContainer = parent;
		statusLabel = status;
		if (reg == null)
		{
			applicationManager = null;
			nodeTable = null;
			
		}else
		{	registrar = reg;
			applicationManager = registrar.getService(CyApplicationManager.class);
			nodeTable = getCurrentNodeTable();
		}
		xAxis = new NumberAxis();
		yAxis = new NumberAxis();
		chartBox = new Pane();
		columnChoices = new ChoiceBox<String>();
		yAxisChoices = new ChoiceBox<String>();
		HBox line = new HBox(columnChoices);
		HBox line2 = new HBox(yAxisChoices);
		VBox page = new VBox(chartBox, line, line2);
		page.setSpacing(4);
		parent.getChildren().add(page);
		initialize(null, null);
	}

	CyTable getCurrentNodeTable() 
	{	
		if (applicationManager == null) return null;
		return applicationManager.getCurrentNetwork().getDefaultNodeTable();	
	}
	
public void setStatus(String s)
{
	if ( statusLabel != null)  statusLabel.setText(s);
}
  

//  static boolean bypass = false;
	@Override public void initialize(URL url, ResourceBundle rb)
	{
//	    System.out.println("ScatterChartController.initialize");
//	   bypass = true;
//		assert (scatterChart != null);
	    assert( chartContainer != null);
		ChangeListener<Number> xListener = new ChangeListener<Number>() 		{	
			@Override public void changed(ObservableValue<? extends Number> obs, Number oldV, Number newV) 
			{   setXParameters(newV);   }	
		};
		ChangeListener<Number> yListener = new ChangeListener<Number>() 		{	
			@Override public void changed(ObservableValue<? extends Number> obs, Number oldV, Number newV) 
			{   setYParameters(newV);   }	
		};
		populateColumnChoices();
		columnChoices.getSelectionModel().selectedIndexProperty().addListener(xListener);
		columnChoices.getSelectionModel().select(0);
		yAxisChoices.getSelectionModel().selectedIndexProperty().addListener(yListener);
		yAxisChoices.getSelectionModel().select(1);
//		bypass = false;		
		setParameters();
	}
	// ------------------------------------------------------
	public void setXParameters(Number val)
	{
		columnChoices.getSelectionModel().select(val.intValue());
		setParameters();
	}
	
	public void setYParameters(Number val)
	{
		yAxisChoices.getSelectionModel().select(val.intValue());
		setParameters();
	}
static int DOT_SIZE = 4; 
	public void setParameters()
	{
//		if (bypass) return;
		if (chartBox != null)
		{
			chartBox.getChildren().clear();
			String x = columnChoices.getSelectionModel().getSelectedItem();
			String y = yAxisChoices.getSelectionModel().getSelectedItem();
		    System.out.println(x + " v.  " + y);
			XYChart.Series<Number, Number> series1 = getDataSeries(x, y);
			scatterChartHome = new SelectableScatterChart(this, null);
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
			chartBox.getChildren().add(scatterChartHome);
//			chartPlotArea = (Region) scatterChartHome.getPlotAreaNode();
		    Node legend = scatterChartHome.lookup(".chart-legend");
		    if (legend != null && legend.isVisible()) 
		    	legend.setVisible(false);
		}
	}
	private void populateColumnChoices() {
		if (nodeTable != null && !nodeTable.getColumns().isEmpty())
		{
			for (CyColumn col : nodeTable.getColumns())
			{
				if (!isNumericColumn(col)) continue;
				columnChoices.getItems().add(col.getName());
				yAxisChoices.getItems().add(col.getName());
			}
			columnChoices.getSelectionModel().select(0);
		}
	}
	private boolean isNumericColumn(CyColumn col) {
		return col.getType() == Double.class || col.getType() == Integer.class;
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
				data.add(new XYChart.Data<Number, Number>(x,y));
			}
		}
		catch (Exception e)		{			e.printStackTrace();		}
		return series;
	}
	
	private List<Double> getColumnValues(CyColumn col)
	{
		if (col.getType() == Double.class)
			return col.getValues(Double.class);
		if (col.getType() == Integer.class)
		{
			List<Integer> intvalues = col.getValues(Integer.class);
			List<Double> dubvalues = new ArrayList<Double>();
			for (Integer i : intvalues)
				dubvalues.add(new Double(i));
			return dubvalues;
		}
		return null;
	}

	//	// ------------------------------------------------------
	private double startX, endX, startY, endY;			// these are values in the charts data space
	
	public double getSelectionStart()	{ 	return startX;	}
	public double getSelectionEnd()		{	return endX;	}
	public double getSelectionTop()		{ 	return startY;	}
	public double getSelectionBottom()	{ 	return endY;	}
	

	public void setSelectionValues(double selStart, double selEnd, double yStart, double yEnd) 
	{
		if (Double.isNaN(selStart) || Double.isNaN(selEnd)) return;
		startX = Math.min(selStart, selEnd);
		endX = Math.max(selStart, selEnd);
		startY = Math.min(yStart, yEnd);
		endY = Math.max(yStart, yEnd);
	}

	public void selectionDoubleClick() 
	{
		String info = String.format("%.2f - %.2f,  %.2f - %.2f ", startX, endX, startY, endY);
		System.out.println("Make a filter: " + info);
		
	}

	//------------------------------------------------------------------
	public void selectRange(String xname, double xMin, double xMax, String yname, double yMin, double yMax) {
		
		startX = xMin;	endX = xMax;
		startY = yMin; 	endY = yMax;
		CyColumn xcol = findColumn(xname);
		CyColumn ycol = findColumn(yname);
		if (xcol == null || ycol == null) return;
		selectRange(xcol, xMin, xMax, ycol, yMin, yMax);
	}
	
	public CyColumn findColumn(String name)
	{
		if (nodeTable == null) return null;
		for (CyColumn column : nodeTable.getColumns())
			if (name.equals(column.getName()))
				return column;
		return null;
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
	private boolean rowMatch(CyRow row, CyColumn col, double xMin, double xMax) {
		if (row == null) {		System.out.println("row is null");		return false;	}
		if (col == null) {		System.out.println("col is null");		return false;	}
		
//		System.out.println(String.format("col %s (%.2f - %.2f)", col.getName(), xMin, xMax));
		Object val = row.get(col.getName(), col.getType());
		if (val == null) return false;
		System.out.println("" + val);
		if (val instanceof Double)
		{ 
			Double v = (Double) val;
			boolean hit = (xMin <= v && xMax >= v);
			return hit;
		}
		if (val instanceof Integer)
		{ 
			double v = 1.0 * (Integer) val;
			boolean hit = (xMin <= v && xMax >= v);
			return hit;
		}
		return false;
		
	}

	public String ping() {		return "JERE";		}
	}