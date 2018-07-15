package org.cytoscape.cyChart.internal.charts.oneD;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HistogramChartController implements Initializable
{
  @FXML private  LineChart<Number, Number> histogramChart;
  @FXML  private StackPane chartContainer;
  @FXML  private ChoiceBox<String> columnChoices;
//  @FXML  private TableView<MixedDataRow> tableview;
//  @FXML  private TableColumn<MixedDataRow, Integer> ID;
//  @FXML  private TableColumn<MixedDataRow, String> colA;
//  @FXML  private TableColumn<MixedDataRow, Integer> colB;
//  @FXML  private TableColumn<MixedDataRow, Double> colC;
//  private CSVTableData dataTable;
  private NumberAxis xAxis;
  private NumberAxis yAxis;
  CyServiceRegistrar registrar; 
	private final CyApplicationManager applicationManager;
	private CyTable nodeTable;

	// use this if you don't use FXML to define the chart
	public HistogramChartController(StackPane parent, CyServiceRegistrar reg) {
		chartContainer = parent;
		registrar = reg;
		applicationManager = registrar.getService(CyApplicationManager.class);
		nodeTable = getNodeTable(getCurrentNetwork());
		xAxis = new NumberAxis();
		yAxis = new NumberAxis();
		histogramChart = new LineChart<Number, Number>(xAxis, yAxis);
		columnChoices = new ChoiceBox<String>();
		// tableview = new TableView<MixedDataRow>();
		// colA = new TableColumn<MixedDataRow, String>();
		// colB = new TableColumn<MixedDataRow, Integer>();
		// colC = new TableColumn<MixedDataRow, Double>();
		// SplitPane split = new SplitPane(histogramChart, tableview);
		// split.setOrientation(Orientation.HORIZONTAL);
		// parent.getChildren().add(split);
		HBox line = new HBox(columnChoices);
		VBox page = new VBox(histogramChart, line);
		parent.getChildren().add(page);
		initialize(null, null);
	}
  
  
	@Override public void initialize(URL url, ResourceBundle rb)
	{
	    System.out.println("HistogramChartController.initialize");
		assert (histogramChart != null);
	    assert( chartContainer != null);
		buildData();
		populateColumnChoices();
		columnChoices.getSelectionModel().selectedIndexProperty().addListener(
			new ChangeListener<Number>() {	@Override public void changed(ObservableValue<? extends Number> obs, Number oldV, Number newV) 
					{   setXParameter(newV);   }	});
		columnChoices.getSelectionModel().select(0);
		
		subrangeLayer = new SubRangeLayer(histogramChart, chartContainer, this);
	}
	
	private void populateColumnChoices() {
		if (nodeTable != null && !nodeTable.getColumns().isEmpty())
		{
			for (CyColumn col : nodeTable.getColumns())
			{
				if (!isNumericColumn(col)) continue;
				columnChoices.getItems().add(col.getName());
			}
			columnChoices.getSelectionModel().select(0);
		}
	}
	private boolean isNumericColumn(CyColumn col) {
		return col.getType() == Double.class || col.getType() == Integer.class;
	}	
	
	SubRangeLayer subrangeLayer;
	// ------------------------------------------------------
	// reads the table file and fills the cells into a tableview's model
	
	public void buildData()
	{
	    System.out.println("buildData");
		histogramChart.setTitle("DATA LOAD ERROR!");
		histogramChart.setTitle("Histogram Chart");
		histogramChart.setCreateSymbols(false);
		xAxis = (NumberAxis) histogramChart.getXAxis();
		yAxis = (NumberAxis) histogramChart.getYAxis();
		//		try
//		{
//			String pathname = getPathName();
//			dataTable = CSVTableData.readCSVfile(pathname);
//			if (dataTable == null) return;
//			dataTable.populateCSVTable(tableview);
//			setupChart();
//		} 
//		catch (Exception e) { e.printStackTrace(); }
	}
	// ------------------------------------------------------
//	private void setupChart()
//	{
//		Histogram1D h1 = dataTable.getHistogram("Eccentricity");
//		Histogram1D h2 = dataTable.getHistogram("Degree");
//		CyTable nodeTable = getCurrentNetwork(). 
//		if (!dataTable.getColumnNames().isEmpty())
//		{
//			for (String colName : dataTable.getColumnNames())
//				columnChoices.getItems().add(colName);
//			columnChoices.getSelectionModel().select(0);
//		}
//		if (h1 == null) 
//			return;
//		XYChart.Series<Number, Number> series1 = h1.getDataSeries("Eccentricity");
//		if (series1 != null)
//			histogramChart.getData().add(series1 );
//
//	}

	CyNetwork getCurrentNetwork() 		{	return registrar.getService(CyApplicationManager.class).getCurrentNetwork();	}
	CyTable getNodeTable(CyNetwork net) {	return net.getDefaultNodeTable();	}
//	
//	Map<Long, Double> getColumnDoubleMap(CyTable table, String colName)
//	{
//		Map<Long, Double> map = new HashMap<Long, Double> ();
//		CyColumn col = table.getColumn(colName);
//		if (col != null)
//		{
//			List<Double> vals = null;
//			List<Long> ids = null;
//			vals = col.getValues(Double.class);
//			ids = col.getValues(Long.class);
//			int sz = vals.size();
//			for (int i=0; i< sz; i++)
//				map.put(ids.get(i), vals.get(i));
//		}
//		return map;
//	}

	// ------------------------------------------------------
	public void setXParameter(Number index)
	{
		String item = columnChoices.getItems().get( index.intValue());
//	    System.out.println(item);
		if (subrangeLayer != null) subrangeLayer.hideSelection();
		Histogram1D h1 = getHistogram(item);
		if (h1 != null)
		{
			histogramChart.getData().clear();
			histogramChart.getData().add(h1.getDataSeries(item) );
			xAxis.setLowerBound(h1.getRange().min());
			xAxis.setUpperBound(h1.getRange().max());
			yAxis.setLowerBound(0);
			yAxis.setUpperBound(0.1);
//			System.out.println(dataTable.toString());
		}
	}

	private Histogram1D getHistogram(String item) {
		nodeTable = getNodeTable(getCurrentNetwork()); 
		List<Double> values = null;
		CyColumn column = nodeTable.getColumn(item);
		if (column.getType() == Double.class)
			values = nodeTable.getColumn(item).getValues(Double.class);
		else if (column.getType() == Integer.class)
		{
			values = new ArrayList<Double>();
			for (Integer i : nodeTable.getColumn(item).getValues(Integer.class))
				values.add(new Double(i));

		}
		return new Histogram1D(item, values);
	}
// ------------------------------------------------------

	private double startX, endX, yVaule;			// these are values in the charts data space
	
	public double getSelectionStart()	{ 	return startX;	}
	public double getSelectionEnd()		{	return endX;	}
	public double getSelectionHeight()	{ 	return yVaule;	}
	
	public void setRange1DValues(double selStart, double selEnd, double y) {
		setRange1DValues(selStart, selEnd);
		yVaule = y;
	}
	
	public void setRange1DValues(double selStart, double selEnd) {
		if (Double.isNaN(selStart) || Double.isNaN(selEnd)) return;
		startX = Math.min(selStart, selEnd);
		endX = Math.max(selStart, selEnd);
		
		if (xAxis != null && !(inBounds(selStart) || !inBounds(selEnd)))
			System.out.print("BAD VALUES");
//		System.out.println(String.format("SetGateValues:   %.2f - %.2f  @ %.2f  ", startX , endX, yVaule));
	
//		int nRows = tableview.getItems().size();
//		for (int row=0; row < nRows; row++)
//		{
//			if (row % 9 == 0)
//				tableview.getSelectionModel().select(row);
//		}
	}
	
	boolean inBounds(double x)
	{
		double low = xAxis.getLowerBound();
		double up = xAxis.getUpperBound();
		return (x >= low && x <= up);
	}

	
	public void selectRange(String name, double xMin, double xMax) {
		
		CyColumn col = findColumn(name);
		if (col  == null) return;
		selectRange(col, xMin, xMax);

	}
	
	public CyColumn findColumn(String name)
	{
		for (CyColumn column : nodeTable.getColumns())
			if (name.equals(column.getName()))
				return column;
		return null;
	}
	
	public int ping()	{  return 2;	}
	
	public void selectRange(CyColumn col, double xMin, double xMax) 
	{
		for (CyRow row : nodeTable.getAllRows())
		{	
			boolean selected =  (rowMatch(row, col, xMin, xMax));
			row.set(CyNetwork.SELECTED, selected);
//			System.out.println((selected ? "selecting " : "deselecting ") + row.get("SUID", Long.class));
		}
	}
	
	private boolean rowMatch(CyRow row, CyColumn col, double xMin, double xMax) {
		if (row == null) {		System.out.println("row is null");		return false;	}
		if (col == null) {		System.out.println("col is null");		return false;	}
		
		Object val = row.get(col.getName(), col.getType());
		if (val == null) return false;
		if (val instanceof Double)
		{ 
			Double v = (Double) val;
			boolean hit = (xMin <= v && xMax >= v);
			return hit;
		}
		if (val instanceof Integer)
		{ 
			Integer i = (Integer) val;
			boolean hit = (xMin <= i && xMax >= i);
			return hit;
		}
		return false;
		
	}
}