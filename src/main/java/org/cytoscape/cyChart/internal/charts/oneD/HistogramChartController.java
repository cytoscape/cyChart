package org.cytoscape.cyChart.internal.charts.oneD;

import java.net.URL;
import java.util.ResourceBundle;

import org.cytoscape.cyChart.internal.charts.CSVTableData;
import org.cytoscape.cyChart.internal.charts.MixedDataRow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HistogramChartController implements Initializable
{
  @FXML private  LineChart<Number, Number> histogramChart;
  @FXML  private StackPane chartContainer;
  @FXML  private ChoiceBox<String> columnChoices;
  @FXML  private TableView<MixedDataRow> tableview;
  @FXML  private TableColumn<MixedDataRow, Integer> ID;
  @FXML  private TableColumn<MixedDataRow, String> colA;
  @FXML  private TableColumn<MixedDataRow, Integer> colB;
  @FXML  private TableColumn<MixedDataRow, Double> colC;
  private CSVTableData dataTable;
  private NumberAxis xAxis;
  private NumberAxis yAxis;

    // use this if you don't use FXML to define the chart
  public HistogramChartController(StackPane parent)
  {
	  chartContainer = parent;
	  xAxis = new NumberAxis();
	  yAxis = new NumberAxis();
	  histogramChart = new LineChart<Number, Number>(xAxis, yAxis);
	  columnChoices = new ChoiceBox<String>();
	  tableview = new TableView<MixedDataRow>();
	  colA = new TableColumn<MixedDataRow, String>();
	  colB = new TableColumn<MixedDataRow, Integer>();
	  colC = new TableColumn<MixedDataRow, Double>();
//	  SplitPane split = new SplitPane(histogramChart, tableview);
//	  split.setOrientation(Orientation.HORIZONTAL);
//	  parent.getChildren().add(split);
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
		columnChoices.getSelectionModel().selectedIndexProperty().addListener(
			new ChangeListener<Number>() {	@Override public void changed(ObservableValue<? extends Number> obs, Number oldV, Number newV) 
					{   setXParameter(newV);   }	});
		columnChoices.getSelectionModel().select("Eccentricity");
		
		subrangeLayer = new SubRangeLayer(histogramChart, chartContainer, this);
	}
	SubRangeLayer subrangeLayer;
	// ------------------------------------------------------
	// reads the table file and fills the cells into a tableview's model
	
	public void buildData()
	{
	    System.out.println("buildData");
		histogramChart.setTitle("DATA LOAD ERROR!");
		xAxis = (NumberAxis) histogramChart.getXAxis();
		yAxis = (NumberAxis) histogramChart.getYAxis();
		try
		{
			String pathname = getPathName();
			dataTable = CSVTableData.readCSVfile(pathname);
			if (dataTable == null) return;
			dataTable.populateCSVTable(tableview);
			setupChart();
		} 
		catch (Exception e) { e.printStackTrace(); }
	}
	// ------------------------------------------------------
	private void setupChart()
	{
		Histogram1D h1 = dataTable.getHistogram("Eccentricity");
//		Histogram1D h2 = dataTable.getHistogram("Degree");
		if (!dataTable.getColumnNames().isEmpty())
		{
			for (String colName : dataTable.getColumnNames())
				columnChoices.getItems().add(colName);
			columnChoices.getSelectionModel().select(0);
		}
		histogramChart.setTitle("Histogram Chart");
		histogramChart.setCreateSymbols(false);
		if (h1 == null) 
			return;
		XYChart.Series<Number, Number> series1 = h1.getDataSeries("Eccentricity");
		if (series1 != null)
			histogramChart.getData().add(series1 );

	}

	// ------------------------------------------------------
	public void setXParameter(Number index)
	{
		String item = columnChoices.getItems().get( index.intValue());
//	    System.out.println(item);
		if (subrangeLayer != null) subrangeLayer.hideSelection();
		Histogram1D h1 = dataTable.getHistogram(item);
		if (h1 != null)
		{
			histogramChart.getData().clear();
			histogramChart.getData().add(h1.getDataSeries(item) );
			xAxis.setLowerBound(h1.getRange().min());
			xAxis.setUpperBound(h1.getRange().max());
			yAxis.setLowerBound(0);
			yAxis.setUpperBound(0.1);
			System.out.println(dataTable.toString());
		}
	}
//	// ------------------------------------------------------
//	public void setXYParameter(Number xIndex, Number yIndex)
//	{
//		String xItem = columnChoices.getItems().get( xIndex.intValue());
//		String yItem = yAxisChoices.getItems().get( yIndex.intValue());
//	    System.out.println(xItem + " v. " + yItem);
//
//	    
//	    Histogram1D h1 = dataTable.getHistogram(xItem);
//	    Histogram1D h2 = dataTable.getHistogram(yItem);
//		if (h1 != null)
//		{
//			histogramChart.getData().clear();
//			histogramChart.getData().add(h1.getDataSeries(item) );
//			xAxis.setLowerBound(h1.getRange().min);
//			xAxis.setUpperBound(h1.getRange().max);
//			yAxis.setLowerBound(h2.getRange().min);
//			yAxis.setUpperBound(h2.getRange().max);
//			System.out.println(dataTable.toString());
//		}
//	}
	
	private static String getPathName() {		return "/Users/adam/galFilteredOut.csv";	}
	// ------------------------------------------------------
	private double startX, endX, yVaule;			// these are values in the charts data space
	
	public double getSelectionStart()	{ 	return startX;	}
	public double getSelectionEnd()		{	return endX;	}
	public double getSelectionHeight()	{ 	return yVaule;	}
	
	public void setGateValues(double selStart, double selEnd, double y) {
		setGateValues(selStart, selEnd);
		yVaule = y;
	}
	
	public void setGateValues(double selStart, double selEnd) {
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
		
		int col = findColumn(name+"");
		if (col < 0) return;
		tableview.getSelectionModel().clearSelection();
		selectRange(col, xMin, xMax);

	}
	
	public int findColumn(String name)
	{
		int nCols = tableview.getColumns().size();
		for (int col = 0; col < nCols; col++)
		{
			TableColumn<?,?> column = tableview.getColumns().get(col);
			if (name.equals(column.getText()))
				return col;
		}
		return -1;
	}
	public int ping()	{  return 1;	}
	public void selectRange(int colIndex, double xMin, double xMax) 
	{
		for (MixedDataRow row : tableview.getItems())
			if (rowMatch(row, colIndex, xMin, xMax))
				tableview.getSelectionModel().select(row);
	}
	
	private boolean rowMatch(MixedDataRow row, int colIndex, double xMin, double xMax) {
		double val = row.get(colIndex).doubleValue();
		
//		System.out.println(String.format("col %d (%.2f %.2f) %.2f", colIndex, xMin, xMax, val));
		return (xMin <= val && xMax >= val);
	}
}