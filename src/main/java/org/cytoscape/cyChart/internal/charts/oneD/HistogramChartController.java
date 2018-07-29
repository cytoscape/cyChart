package org.cytoscape.cyChart.internal.charts.oneD;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.cyChart.internal.charts.Borders;
import org.cytoscape.cyChart.internal.charts.LogarithmicAxis;
import org.cytoscape.cyChart.internal.charts.StringUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

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
  private ValueAxis<Number> xAxis;
  private ValueAxis<Number> yAxis;
  Pane chartPane;
  CyServiceRegistrar registrar; 
	private final CyApplicationManager applicationManager;
	private CyTable nodeTable;
	JLabel statusLabel;
	CheckBox logTransform;
	
	// use this if you don't use FXML to define the chart
	public HistogramChartController(StackPane parent, CyServiceRegistrar reg, JLabel status, CyColumn column) {
		chartContainer = parent;
		registrar = reg;
		statusLabel = status;
		if (registrar == null)
		{
			applicationManager = null;
			nodeTable = null;
		}
		else
		{	
			applicationManager = registrar.getService(CyApplicationManager.class);
			nodeTable = getNodeTable(getCurrentNetwork());
		}
		columnChoices = new ChoiceBox<String>();
		logTransform = new CheckBox("Log");
		ChangeListener<Boolean> logChange = new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
		        setLogDistribution(new_val);
		    }
		};
		logTransform.selectedProperty().addListener(logChange);
		logTransform.setAlignment(Pos.CENTER);
		makeFilter = new Button("Create Filter");
		makeFilter.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {	makeFilter();	}
		});
		copyImage = new Button("Copy Image");
		copyImage.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {	copyImage();	}
		});
		HBox lineA = new HBox(8, makeFilter, copyImage);
		lineA.setMinHeight(28);
		lineA.setMaxHeight(28);
		// tableview = new TableView<MixedDataRow>();
		// colA = new TableColumn<MixedDataRow, String>();
		// colB = new TableColumn<MixedDataRow, Integer>();
		// colC = new TableColumn<MixedDataRow, Double>();
		// SplitPane split = new SplitPane(histogramChart, tableview);
		// split.setOrientation(Orientation.HORIZONTAL);
		// parent.getChildren().add(split);
		HBox line = new HBox(8, columnChoices, logTransform);
		chartPane = new Pane();
		VBox page = new VBox(lineA, chartPane, line);
		parent.getChildren().add(page);
		initialize(null, null);
		if (column != null)
		{
			int idx = findColumnIndex(column.getName());
			columnChoices.getSelectionModel().select(column.getName());
			System.out.println("index set to " + idx);
			setXParameter(column.getName());
		}
		else setLogDistribution(false);
	}
  
	Button makeFilter;
	Button copyImage;
	 protected void makeFilter() {
			System.out.println( "Make a NumericFilter");
			String x = columnChoices.getSelectionModel().getSelectedItem();
		    System.out.println(x + (logTransform.isSelected() ? " (Log)" : " (Lin)"));
	}
	 
	private void copyImage() {
		copyImage.setVisible(false);
		makeFilter.setVisible(false);
	    FileChooser fileChooser = new FileChooser();	
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));
	
	    //Prompt user to select a file
	    File file = fileChooser.showSaveDialog(null);
	    if(file != null){
	        try {
	            //Pad the capture area
	            WritableImage writableImage = new WritableImage((int)chartContainer.getWidth() + 20,
	                    (int)chartContainer.getHeight() + 20);
	            chartContainer.snapshot(null, writableImage);
	            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
	            //Write the snapshot to the chosen file
	            ImageIO.write(renderedImage, "png", file);
	        } catch (IOException ex) { ex.printStackTrace(); }
	    }
		copyImage.setVisible(true);
		makeFilter.setVisible(true);
	}
	 public void setStatusText(String s) {
		 if (statusLabel != null)
			 statusLabel.setText(s);
			 
	 }
	 boolean isLog = false;
	
	 private void setLogDistribution(Boolean new_val) 
	{
		isLog = new_val;
		setXParameter(columnChoices.getSelectionModel().getSelectedItem());
			
	}
	@Override public void initialize(URL url, ResourceBundle rb)
	{
//	    System.out.println("HistogramChartController.initialize");
//		assert (histogramChart != null);
	    assert( chartContainer != null);
		populateColumnChoices();
		columnChoices.getSelectionModel().selectedIndexProperty().addListener(
			new ChangeListener<Number>() {	@Override public void changed(ObservableValue<? extends Number> obs, Number oldV, Number newV) 
					{   setXParameter(newV);   }	});
		columnChoices.getSelectionModel().select(0);
		setXParameter(0);
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
	Node getPlotAreaNode() 
	{
		return histogramChart.lookup(".chart-plot-background");
	}	

	
	// ------------------------------------------------------
	public void setXParameter(String name)
	{
		chartPane.getChildren().clear();
		if (subrangeLayer != null)
			subrangeLayer.clear();
		xAxis = isLog ? new LogarithmicAxis() : new NumberAxis();
		yAxis = new NumberAxis();
		histogramChart = new LineChart<Number, Number>(xAxis, yAxis);
		histogramChart.setTitle("Histogram Chart");
		histogramChart.setCreateSymbols(false);
		Node chartPlotArea = getPlotAreaNode();
		if (chartPlotArea != null)
		{
			Region rgn = (Region) chartPlotArea;
			rgn.setStyle("-fx-background-color: #CCCCCC;");
			rgn.setBorder(Borders.thinEtchedBorder);
		}
		subrangeLayer = new SubRangeLayer(histogramChart, chartContainer, this);
		chartPane.getChildren().add(histogramChart);
		System.out.println("setXParameter: " + name);
		if (StringUtil.isEmpty(name)) return;
		if (subrangeLayer != null) 
		{
			subrangeLayer.hideSelection();
			Group g = subrangeLayer.getSubRangeGroup();
			Bounds bounds = getPlotAreaNode().getBoundsInParent();
			g.setTranslateX(bounds.getMinX());
			g.setTranslateY(bounds.getMinY());
		}
		Histogram1D h1 = getHistogram(name, isLog);
		if (h1 != null)
		{
			histogramChart.getData().clear();
			histogramChart.getData().add(h1.getDataSeries(name) );
			xAxis.setLowerBound(h1.getRange().min());
			xAxis.setUpperBound(h1.getRange().max());
			yAxis.setLowerBound(0);
			double top = .5 * h1.getMode() / h1.getSize();
			yAxis.setUpperBound(top);
			h1.calcDistributionStats();
			System.out.println("Histo: " + h1.getStatString());
		}
	}
	public void setXParameter(Number index)
	{
		int i =  index.intValue();
		if (i >= 0 && i < columnChoices.getItems().size())
			setXParameter(columnChoices.getItems().get(i));
	}

	private Histogram1D getHistogram(String item, Boolean isLog) {
		nodeTable = getNodeTable(getCurrentNetwork()); 
		List<Double> values = null;
		CyColumn column = nodeTable.getColumn(item);
		if (column.getType() == Double.class)
		{
			values = nodeTable.getColumn(item).getValues(Double.class);
			if (values == null) return null;
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
	
	private Double safelog(Double i) {
		return (i == null || i <= 0) ? 0 : Math.log(i);
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
	
	public int findColumnIndex(String name)
	{
		int idx = 0;
		for (CyColumn column : nodeTable.getColumns())
		{
			if (name.equals(column.getName()))
				return idx;
			idx++;
		}
		return idx;
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
			if (isLog) v = safelog(v);
			boolean hit = (xMin <= v && xMax >= v);
			return hit;
		}
		if (val instanceof Integer)
		{ 
			Integer i = (Integer) val;
			double d = i;
			if (isLog) 
				d = safelog(d);
			boolean hit = (xMin <= d && xMax >= d);
			return hit;
		}
		return false;
		
	}
}