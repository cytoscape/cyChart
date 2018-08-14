package org.cytoscape.cyChart.internal.charts;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.cyChart.internal.FilterBuilder;
import org.cytoscape.cyChart.internal.NumberField;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/*
 * This is the parent class of the histogram and scatter controllers.
 * It builds the panel, with header / chart / footer
 */
abstract public class AbstractChartController implements Initializable {
	
	protected CyApplicationManager applicationManager = null;
	protected CyServiceRegistrar registrar;

	protected CyTable nodeTable;
	protected NumberField xMin, xMax;
	protected NumberField yMin, yMax;
	protected Label statusLabel = new Label();
	
	protected AnchorPane chartBox;
	protected StackPane chartContainer;
	protected ChoiceBox<String> xAxisChoices;
	protected CheckBox logXTransform;
	protected ChoiceBox<String> yAxisChoices;
	protected CheckBox logYTransform;
	protected ValueAxis<Number> xAxis;
	protected ValueAxis<Number> yAxis;
	XYChart<Number,Number> theChart;
	protected void setChart(XYChart<Number,Number> c) { theChart = c; }
	protected XYChart<Number,Number> getChart() 	{ return theChart; }
	public Node getPlotAreaNode() 		{		return theChart.lookup(".chart-plot-background");	}	
	public Bounds getPlotBounds()		{		return getPlotAreaNode().getBoundsInParent();		}
	HBox header1, footer1, footer2, footer3;
	
	static Font numberFont = new Font("SansSerif", 10);
	abstract public void setParameters(); 

	//-------------------------------------------------------------
	// use this if you don't use FXML to define the chart
	public AbstractChartController(StackPane parent, CyServiceRegistrar reg, boolean is2D) {
		chartContainer = parent;
//		chartContainer.setBorder(Borders.etchedBorder);
		if (reg == null)
		{
			applicationManager = null;
			nodeTable = null;
		}
		else
		{	registrar = reg;
			applicationManager = registrar.getService(CyApplicationManager.class);
			nodeTable = getCurrentNodeTable();
		}
		
		HBox top = makeHeader();
		chartBox = new AnchorPane();			//  the placeholder to contain the chart
		anchor(chartBox);		
		VBox bottom = makeFooter(is2D);

		BorderPane page = new BorderPane();
		page.setTop(top);
		page.setCenter(chartBox);
		page.setBottom(bottom);
		bottom.setPadding(new Insets(2,10,2,10));

		parent.getChildren().add(page);
		anchor(parent); 
		initialize(null, null);
		
	}
	//-------------------------------------------------------------
	// the FXML callback to define the chart  (FXML is not in use, but this is called thru the constructor)
	
	@Override public void initialize(URL url, ResourceBundle rb)
	{
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
		xAxisChoices.getSelectionModel().selectedIndexProperty().addListener(xListener);
		xAxisChoices.getSelectionModel().select(0);
		yAxisChoices.getSelectionModel().selectedIndexProperty().addListener(yListener);
		yAxisChoices.getSelectionModel().select(1);
		setParameters();
	}
	// ------------  
	private HBox makeHeader() {
		
		makeFilter = new Button("Create Filter");
		makeFilter.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {	makeFilter();	}
		});
		copyImage = new Button("Export Image...");
		copyImage.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {	copyImage();	}
		});
		header1 = new HBox(8, makeFilter, copyImage);
		header1.setMinHeight(30);
		header1.setMaxHeight(30);
		header1.setBorder(Borders.emptyBorder);
		AnchorPane.setLeftAnchor(header1, 12.0);
		return header1;
	}
	// ------------  
	private VBox makeFooter(boolean is2D) {
		// -make the x axis choice box and log check box
		xAxisChoices = new ChoiceBox<String>();
		ChangeListener<Boolean> logXChange = new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
		        setLogXDistribution(new_val);
		    }
		};

		logXTransform = new CheckBox("Log");
		logXTransform.selectedProperty().addListener(logXChange);
		logXTransform.setAlignment(Pos.CENTER);
		xMin = makeNumberField();
		xMax = makeNumberField();	
		
		// ------------   we always create the Y axis line, but only include it if (is2D)
		yAxisChoices = new ChoiceBox<String>();
		ChangeListener<Boolean> logYChange = new ChangeListener<Boolean>() {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
			        setLogYDistribution(new_val);
			    }
			};

		logYTransform = new CheckBox("Log");
		logYTransform.selectedProperty().addListener(logYChange);
		logYTransform.setAlignment(Pos.CENTER);
		yMin = makeNumberField();
		yMax = makeNumberField();
		
		// -------------- layout		
		footer1 = makeNumberRangeLine(xAxisChoices, logXTransform, xMin, xMax);
		footer2 = makeNumberRangeLine(yAxisChoices, logYTransform, yMin, yMax);
		footer3 = new HBox(statusLabel); 
		VBox bottom = new VBox();
		bottom.getChildren().add(footer1);
		if (is2D)
			bottom.getChildren().add(footer2);
		bottom.getChildren().add(footer3);
		return bottom;
	}
	// ------------ 
	private HBox makeNumberRangeLine(ChoiceBox<?> choices, CheckBox log, NumberField minFld, NumberField maxFld)		// 6 controls
	{
		Label min = new Label("Min:");
		Label max = new Label("Max:");
		min.setFont(numberFont);
		max.setFont(numberFont);
		return new HBox(8, choices, log, min, minFld, max, maxFld);
	}
	// ------------ 
	private NumberField makeNumberField() {
		NumberField fld = new NumberField();
		fld.setAlignment(Pos.CENTER);
		fld.setFont(numberFont);
		fld.setMaxWidth(55);
		ChangeListener<String> txtListener =  (observable, oldValue, newValue) -> textChanged(newValue);		
		fld.textProperty().addListener(txtListener);
		return fld;
	}
	// ------------  respond to user edits of range values.  
	private void textChanged(String newValue) {
		BigDecimal newXmin = xMin.getNumber();
		BigDecimal newXmax = xMax.getNumber();
		BigDecimal newYmin = yMin.getNumber();
		BigDecimal newYmax = yMax.getNumber();
		System.out.println(newXmin + " - " + newXmax + " , " + newYmin + " - " + newYmax);
}
	//-------------------------------------------------------------
	protected Button makeFilter;
	protected Button copyImage;
	
	protected void makeFilter() {
		if (registrar == null) {		System.err.println("No registrar found");  return; 	}
		String x = xAxisChoices.getSelectionModel().getSelectedItem();
		String y = yAxisChoices.getSelectionModel().getSelectedItem();
	    FilterBuilder builder = new FilterBuilder(x, new Range(startX, endX), y, new Range(startY, endY));
	    builder.makeCompositeFilter(registrar);
	 }
	 
	private void copyImage() {
	    FileChooser fileChooser = new FileChooser();	
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));
	
	    //Prompt user to select a file
	    File file = fileChooser.showSaveDialog(null);
	    if(file != null){
	        try {
	    		header1.setVisible(false);
	    		footer1.setVisible(false);
	    		footer2.setVisible(false);
	    		footer3.setVisible(true);
	            //Pad the capture area
	    		int width = (int)chartContainer.getWidth() + 20;
	    		int height = (int)chartContainer.getHeight() + 20;
	            WritableImage writableImage = new WritableImage(width, height);
	            chartContainer.snapshot(null, writableImage);
	            RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
	            ImageIO.write(renderedImage, "png", file);		   //Write the snapshot to the chosen file
	        } catch (IOException ex) { ex.printStackTrace(); }
	    }
		header1.setVisible(true);
		footer1.setVisible(true);
		footer2.setVisible(true);
		footer3.setVisible(true);
	}

	//-------------------------------------------------------------
	 protected boolean isXLog = false;
	 protected boolean isYLog = false;

	 public boolean isXLog()	{ return isXLog;	}
	 public boolean isYLog()	{ return isYLog;	}
	 
	 
	protected void populateColumnChoices() {
		if (nodeTable != null && !nodeTable.getColumns().isEmpty()) {
			for (CyColumn col : nodeTable.getColumns()) {
				if (!isNumericColumn(col))
					continue;
				xAxisChoices.getItems().add(col.getName());
				yAxisChoices.getItems().add(col.getName());
			}
			xAxisChoices.getSelectionModel().select(0);
		}
	}

	protected CyColumn findColumn(String name) {
		if (nodeTable == null)
			return null;
		for (CyColumn column : nodeTable.getColumns())
			if (name.equals(column.getName()))
				return column;
		return null;
	}
	// -------------------------------------------------------------
	public void setXParameters(Number val) {
		xAxisChoices.getSelectionModel().select(val.intValue());
		setParameters();
	}

	protected void setLogXDistribution(Boolean new_val) {
		isXLog = new_val;
		setXParameters(xAxisChoices.getSelectionModel().getSelectedIndex());
	}
	// -------------------------------------------------------------
	public void setYParameters(Number val)
	{
		yAxisChoices.getSelectionModel().select(val.intValue());
		setParameters();
	}

	protected void setLogYDistribution(Boolean new_val) {
		isYLog = new_val;
		setYParameters(yAxisChoices.getSelectionModel().getSelectedIndex());
	}
	// -------------------------------------------------------------
	protected CyTable getCurrentNodeTable() {
		if (applicationManager == null)
			return null;
		return applicationManager.getCurrentNetwork().getDefaultNodeTable();
	}

	public void setStatus(String s)	{
		if ( statusLabel != null)  statusLabel.setText(s);
	}
	  
	public void setStatus(String s, Range xRange, Range yRange)	{
		if ( statusLabel != null)  statusLabel.setText(s);
		setXRange(xRange);
		setYRange(yRange);
	}
	  
	private void setXRange(Range r) {
		if (r == null) return;
		xMin.setNumber(r.min());
		xMax.setNumber(r.max());		
	}
	private void setYRange(Range r) {
		if (r == null) return;
		yMin.setNumber(r.min());
		yMax.setNumber(r.max());		
	}
	
		
	// ------------------------------------------------------
	public int getDataSize(XYChart<Number, Number> chart)
	{
		List<XYChart.Series<Number, Number>> dataList = chart.getData();
		if (dataList == null || dataList.isEmpty()) return 0;
		XYChart.Series<Number, Number> data = dataList.get(0);
		return data.getData().size();
	}
	
	// ------------------------------------------------------
	protected boolean isNumericColumn(CyColumn col) {
		return col.getType() == Double.class || col.getType() == Integer.class;
	}
	protected List<Double> getColumnValues(CyColumn col)
	{
		if (col.getType() == Double.class)
			return col.getValues(Double.class);
		if (col.getType() == Integer.class)
		{
			List<Integer> intvalues = col.getValues(Integer.class);
			List<Double> dubvalues = new ArrayList<Double>();
			for (Integer i : intvalues)
				if (i != null) 
					dubvalues.add(new Double(i));
			return dubvalues;
		}
		return null;
	}

	protected double safelog(double d) {
		if (d <= 0) return 0;
		return Math.log(d);
	}
	// ------------------------------------------------------
	protected double startX, endX, startY, endY;			// these are values in the charts data space
	public double getSelectionStart()	{ 	return startX;	}
	public double getSelectionEnd()		{	return endX;	}
	public double getSelectionTop()		{ 	return startY;	}
	public double getSelectionBottom()	{ 	return endY;	}

	// ------------------------------------------------------
	public void setRangeValues(double selStart, double selEnd) {
		if (Double.isNaN(selStart) || Double.isNaN(selEnd)) return;
		startX = Math.max(xAxis.getLowerBound(), Math.min(selStart, selEnd));
		endX = Math.min(xAxis.getUpperBound(), Math.max(selStart, selEnd));
	}

	public void setRangeValues(double selStart, double selEnd, double selStartY, double selEndY) {
		if (Double.isNaN(selStart) || Double.isNaN(selEnd)) return;
		startX = Math.max(xAxis.getLowerBound(), Math.min(selStart, selEnd));
		endX = Math.min(xAxis.getUpperBound(), Math.max(selStart, selEnd));

		startY = Math.max(yAxis.getLowerBound(), Math.min(selStartY, selEndY));
		endY = Math.min(yAxis.getUpperBound(), Math.max(selStartY, selEndY));
	}

	// ------------------------------------------------------
	protected void anchor(Node n)					{	anchor(n,0);	}
	protected void anchor(Node n, double margin)	{	anchor(n, margin, margin, margin, margin);	}
	protected void anchor(Node n, double top, double left, double bottom, double right )
	{
		AnchorPane.setTopAnchor(n, top);
		AnchorPane.setLeftAnchor(n, left);
		AnchorPane.setBottomAnchor(n, bottom);
		AnchorPane.setRightAnchor(n, right);
		
	}
}