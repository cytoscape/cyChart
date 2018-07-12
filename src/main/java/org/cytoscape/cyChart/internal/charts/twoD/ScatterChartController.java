package org.cytoscape.cyChart.internal.charts.twoD;

import java.net.URL;
import java.util.ResourceBundle;

import org.cytoscape.cyChart.internal.charts.Borders;
import org.cytoscape.cyChart.internal.charts.CSVTableData;
import org.cytoscape.cyChart.internal.charts.MixedDataRow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class ScatterChartController implements Initializable
{
//  @FXML private  LineChart<Number, Number> histogramChart;
 
  @FXML  private Pane chartBox;
  @FXML  private StackPane chartContainer;
  @FXML  private ChoiceBox<String> columnChoices;
  @FXML  private ChoiceBox<String> yAxisChoices;
  @FXML  private TableView<MixedDataRow> tableview;
  @FXML  private TableColumn<MixedDataRow, Integer> ID;
  @FXML  private TableColumn<MixedDataRow, String> colA;
  @FXML  private TableColumn<MixedDataRow, Integer> colB;
  @FXML  private TableColumn<MixedDataRow, Double> colC;
  private CSVTableData dataTable;
//  private NumberAxis xAxis;
//  private NumberAxis yAxis;

	private static String getPathName() {		return "/Users/adam/galFilteredOut.csv";	}
  
  
	private SelectableScatterChart scatterChartHome;

  static boolean bypass = false;
	@Override public void initialize(URL url, ResourceBundle rb)
	{
	    System.out.println("ScatterChartController.initialize");
	   bypass = true;
//		assert (scatterChart != null);
	    assert( chartContainer != null);
		buildData();
		ChangeListener<Number> xListener = new ChangeListener<Number>() 		{	
			@Override public void changed(ObservableValue<? extends Number> obs, Number oldV, Number newV) 
			{   setXParameters(newV);   }	
		};
		ChangeListener<Number> yListener = new ChangeListener<Number>() 		{	
			@Override public void changed(ObservableValue<? extends Number> obs, Number oldV, Number newV) 
			{   setYParameters(newV);   }	
		};
		
		columnChoices.getSelectionModel().selectedIndexProperty().addListener(xListener);
		columnChoices.getSelectionModel().select("Eccentricity");
		yAxisChoices.getSelectionModel().selectedIndexProperty().addListener(yListener);
		yAxisChoices.getSelectionModel().select("ClosenessCentrality");
		bypass = false;		
		setParameters();
	}
	// ------------------------------------------------------
	// reads the table file and fills the cells into a tableview's model
	
	public void buildData()
	{
	    System.out.println("buildData");
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
//	SelectableScatterChart ssc;
	private void setupChart()
	{
		if (!dataTable.getColumnNames().isEmpty())
		{
			for (String colName : dataTable.getColumnNames())
			{
				columnChoices.getItems().add(colName);
				yAxisChoices.getItems().add(colName);
			}
		}
		chartBox.getChildren().clear();
		SelectableScatterChart ssc = new SelectableScatterChart(this, dataTable.getData());
		chartBox.getChildren().add(ssc);
//		chartBox.setBorder(Borders.greenBorder);
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
		if (bypass) return;
		if (chartBox != null)
		{
			chartBox.getChildren().clear();
			String x = columnChoices.getSelectionModel().getSelectedItem();
			String y = yAxisChoices.getSelectionModel().getSelectedItem();
		    System.out.println(x + " v. " + y);
			XYChart.Series<Number, Number> series1 = dataTable.generateData(x, y);
			scatterChartHome = new SelectableScatterChart(this);
			scatterChartHome.setDataSeries(series1);
	        for (XYChart.Data<Number, Number> dataVal : series1.getData()) {
	        	StackPane stackPane =  (StackPane) dataVal.getNode();
	        	if (stackPane != null)
	        		stackPane.setPrefSize(DOT_SIZE, DOT_SIZE);
	        }
			scatterChartHome.setAxes(x, y);
			chartBox.getChildren().add(scatterChartHome);
//			chartPlotArea = (Region) scatterChartHome.getPlotAreaNode();
		    Node legend = scatterChartHome.lookup(".chart-legend");
		    if (legend != null && legend.isVisible()) 
		    	legend.setVisible(false);
		}
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
		int xcol = findColumn(xname);
		int ycol = findColumn(yname);
		if (xcol < 0 || ycol < 0) return;
		tableview.getSelectionModel().clearSelection();
		selectRange(xcol, xMin, xMax, ycol, yMin, yMax);
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
	
	public void selectRange(int xcolIndex, double xMin, double xMax, int ycolIndex, double yMin, double yMax) 
	{
		for (MixedDataRow row : tableview.getItems())
			if (rowMatch(row, xcolIndex, xMin, xMax, ycolIndex, yMin, yMax))
				tableview.getSelectionModel().select(row);
	}
	
	boolean verbose = false;
	private boolean rowMatch(MixedDataRow row, int xcolIndex, double xMin, double xMax, int ycolIndex, double yMin, double yMax) {
		double x = row.get(xcolIndex).doubleValue();
		double y = row.get(ycolIndex).doubleValue();
		
		boolean match = (xMin <= x && xMax >= x && yMin <= y && yMax >= y);
		if (verbose) System.out.println(String.format("%s X is col %d (%.2f %.2f) %.2f Y is col %d (%.2f %.2f) %.2f",( match ? "HIT " : "MISS" ), xcolIndex, xMin, xMax, x, ycolIndex, yMin, yMax, y));
		return match;
	}

	}