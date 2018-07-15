package org.cytoscape.cyChart.internal.charts.oneD;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;


public class SubRangeLayer			// a 1D GateLayer
{
/**
 * This class adds a layer on top of a histogram chart. 
 *
 */
	private static final String INFO_LABEL_ID = "zoomInfoLabel";
	private static final String POSITION_LABEL_ID = "positionLabel";
	private Pane pane;
	public Pane getPane()	{ return pane;	}
	
	private LineChart<Number, Number> chart;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	double chartOffsetX, chartOffsetY;
	private HistogramChartController controller;
	private Group selectionH;
	private Label infoLabel;
	private Label positionLabel;
	FrameScaleConverter converter = new FrameScaleConverter();

	private double selectionAnchor = -1;			// these are pixel (frame) coordinates, as seen by mouse events
	private double selectionMovingEnd = -1;
	public boolean isSelectionSizeTooSmall() {	return 10 > Math.abs(selectionMovingEnd - selectionAnchor);	}

	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";

	/**
	 * Create a new instance of this class with the given chart and pane instances. The {@link Pane} instance is needed
	 * as a parent for the group (H) that represents the user selection.
	 * 
	 * @param chart
	 *            the xy chart to which the zoom support should be added
	 * @param pane
	 *            the pane on which the selection rectangle will be drawn.
	 */
	public SubRangeLayer(LineChart<Number, Number> inChart, Pane inPane, HistogramChartController ctrol) 
	{
		pane = inPane;
		chart = inChart;
		controller = ctrol;
		
		xAxis = (NumberAxis) chart.getXAxis();
		yAxis = (NumberAxis) chart.getYAxis();
		
		selectionH = buildSubRangeGroup(chart, this);  // SelectionRectangle();
		selectionH.setManaged(false);
		update(0);
		selectionH.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);
		selectionH.setStyle("-fx-fillcolor: CYAN; -fx-strokewidth: 4;");
		pane.getChildren().add(selectionH);

		addDragSelectionMechanism();
		addInfoLabel();
	    ChangeListener<Number> paneSizeListener = (obs, oldV, newV) -> setAxisBounds();
	    pane.widthProperty().addListener(paneSizeListener);
	    pane.heightProperty().addListener(paneSizeListener);  

//	    if (showChartOutlines) {
//	    chart.setBorder(Borders.cyanBorder);
//		Region chartPlotArea = (Region) chart.lookup(".chart-plot-background");
//		chartPlotArea.setBorder(Borders.greenBorder);}
	    
}

	/**
	 * The info label shows a short info text that tells the user how to unreset the zoom level.
	 */
	private void addInfoLabel() {
		infoLabel = new Label("Subrange info goes here");
		infoLabel.setId(INFO_LABEL_ID);
		pane.getChildren().add(infoLabel);
		StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
		infoLabel.setVisible(false);
		positionLabel = new Label("positionLabel");
		positionLabel.setId(POSITION_LABEL_ID);
		pane.getChildren().add(positionLabel);
		StackPane.setAlignment(positionLabel, Pos.TOP_LEFT);
		positionLabel.setVisible(false);
	}

	private void disableAutoRanging() {
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
	}

	private void showInfo() 				{			infoLabel.setVisible(true);		}
	public void updateController()
	{
		setGateValues(selectionAnchor, selectionMovingEnd);
		double xMin = controller.getSelectionStart();
		double xMax = controller.getSelectionEnd();
		double freq = getRangeFreq(chart, xMin, xMax);
		NumberFormat fmt = new DecimalFormat("0.00");
		String s = fmt.format(freq * 100) +  "% for ( " + fmt.format(xMin) + ", " + fmt.format(xMax) +  ")";
		infoLabel.setText(s);
		showInfo();
		selectRange(chart, xMin, xMax);

	}
	boolean BETWEEN(double a, double min, double max)		{		return a >= min && a <= max;		}
	double previousH = -1;
	boolean dragging = false;
	//-------------------------------------------------------------------------------
	/**
	 * Adds a mechanism to select an area in the chart that should be the subrange.
	 */
	private void addDragSelectionMechanism() {
		Region chartPlotArea = (Region) chart.lookup(".chart-plot-background");
		chartPlotArea.setOnMouseMoved(event -> 		{ 	onMouseMoved(event);	});
		chartPlotArea.setOnMouseExited(event -> 	{ 	positionLabel.setVisible(false);  });
		chartPlotArea.setOnMousePressed(event -> 	{	onPressed(event);		});
		chartPlotArea.setOnMouseDragged(event -> 	{  	onDragged(event);	 	});
		chartPlotArea.setOnMouseReleased(event -> 	{  	onReleased(event); });
		chartPlotArea.setOnKeyReleased(event -> 	{	});
	}
	
	Point2D getPosition(MouseEvent event)
	{
		double h = event.getX();
		double v = event.getY();
		double x = converter.frameToScale(h, chart, false);
		double y = converter.frameToScale(v, chart, true);
		return new Point2D(x,y);
	}

	public void reportPosition(MouseEvent event) {
	Point2D pt = getPosition(event);
	positionLabel.setText(String.format("%.1f , %.3f ", pt.getX(), pt.getY()));
	positionLabel.setVisible(true);
	}
	
	public void dumpPosition(MouseEvent event) {
	Point2D pt = getPosition(event);
	System.out.println(String.format("(%.0f , %.0f) ->   %.1f , %.3f ", event.getX(),event.getY(), pt.getX(), pt.getY()));
	
	}

	//-------------------------------------------------------------------------------
	private void onMouseMoved(MouseEvent event) {
		double x = event.getX() + chartOffsetX;
		hitSpot = testHit(x);
//		System.out.println(hitSpot);
		Cursor c = Cursor.DEFAULT;
		if (selectionH.isVisible())
		{
			switch (hitSpot)
			{
				case 1:  c = Cursor.H_RESIZE;  break;
				case 2:  c = Cursor.OPEN_HAND;  break;
				case 3:  c = Cursor.H_RESIZE;  break;
			}
		}
		getPane().getScene().setCursor(c);
		reportPosition(event);
		event.consume();		
	}	
	//-------------------------------------------------------------------------------
	int hitSpot;
	
	private void onPressed(MouseEvent event) 
	{
		Region chartPlotArea = (Region) chart.lookup(".chart-plot-background");
	    chartOffsetX = chartPlotArea.getLayoutX();
	    chartOffsetY = chartPlotArea.getLayoutY();
		double x = event.getX() + chartOffsetX;
		debugH(x);
		if (event.isSecondaryButtonDown()) 		return;		// do nothing for a right-click
		double scaleStart = controller.getSelectionStart();
		selectionAnchor = converter.scaleToFrame(scaleStart, chart, false);
		double scaleEnd = controller.getSelectionEnd();
		selectionMovingEnd = converter.scaleToFrame(scaleEnd, chart, false);
		boolean vis = selectionH.isVisible();
		hitSpot = testHit(x);
		double right = Math.max(selectionAnchor, selectionMovingEnd);
		double left = Math.min(selectionAnchor, selectionMovingEnd);
		if (vis)		//   && hitSpot > 0
		{
			previousH = x;
			dragging = true;
			if (hitSpot == 1)  			setSelection(right, x);
			else if (hitSpot == 3)  	setSelection(left, x);
			else if (x < leftBar.getStartX())
				setSelection(right, x);
			else if (x > rightBar.getStartX()-SLOP)
				setSelection(left, x);

		}
		else
		{
			dragging = true;
			setSelection(x, x);
			selectionH.setVisible(true);
		}
		event.consume();
	}

	private void setSelection(double anchor, double movingEnd)
	{
		selectionAnchor = anchor;
		selectionMovingEnd = movingEnd;
	}
	
	
	private void onDragged(MouseEvent event) {
		reportPosition(event);
		if (event.isSecondaryButtonDown()) 		return;
		event.consume();
		if (!dragging) return;
		Node chartPlotArea = chart.lookup(".chart-plot-background");
		double minAllowed = chartPlotArea.getLayoutX();
		double maxAllowed = minAllowed + chartPlotArea.getLayoutBounds().getWidth();
		double h = event.getX() + chartPlotArea.getLayoutX();
//		System.out.println(h);
		boolean inRange = h >= minAllowed && h <= maxAllowed;
		if (!inRange) return;
		double delta = h - previousH;
		if (delta == 0) return;

		if (selectionAnchor > 0 && selectionMovingEnd > 0)
			inRange = ((selectionAnchor + delta) >= minAllowed) && ((selectionMovingEnd + delta) <= maxAllowed);
		if (inRange) 
		{
//			System.out.println(hitSpot);
			if (hitSpot == 2)
				offsetSelection(delta);
			else 		
				selectionMovingEnd = h;
			update(event.getY());
			previousH = h;
		}
	}

	private void offsetSelection(double delta)
	{
//		System.out.println("Offsetting: " + delta);
		selectionAnchor += delta;		
		selectionMovingEnd += delta;		
	}
	private void onReleased(MouseEvent event) {
		if (!dragging) return;
		boolean ok = selectionAnchor > 0 && selectionMovingEnd > 0;
		if (ok && isSelectionSizeTooSmall()) 
			ok = false;
		if (ok)
		{
//			selectionH.update(selectionStart, selectionEnd, y);
			previousH = -1;
//			setAxisBounds();
			double right = Math.max(selectionAnchor, selectionMovingEnd);
			double left = Math.min(selectionAnchor, selectionMovingEnd);
			setSelection(left,right);
			update(event.getY());
			updateController();
			setSelection(-1, -1); 
		} 
		pane.requestFocus();		// needed for the key event handler to receive events
		positionLabel.setVisible(false);
		dragging = false;
		event.consume();		
	}


	/**-------------------------------------------------------------------------------
	 *
	 */
	
	void debugH(double h)
	{
		System.out.println(String.format("H: %.2f = %.2f", h, converter.frameToScale(h, chart, false)));
	}
	void debugV(double v)
	{
		System.out.println(String.format("V: %.2f = #.2f", v, converter.frameToScale(v, chart, true)));
	}

	/**-------------------------------------------------------------------------------
	 *
	 */
	public double getSelectionMin()			{		return Math.min(selectionAnchor, selectionMovingEnd);		}
	public double getSelectionMax()			{		return Math.max(selectionAnchor, selectionMovingEnd);		}

	public double getSelectionStart()		{		return selectionAnchor;		}
	public double getSelectionEnd()			{		return selectionMovingEnd;		}
	public void setSelectionStart(double i)	{		selectionAnchor = i;		}
	public void setSelectionEnd(double i)	{		selectionMovingEnd = i;	}


	//a drag causes new values to go from the frame to model
	
	public void setGateValues(double selAnchorH, double selEndH) {
		double x0 = converter.frameToScale(selAnchorH, chart, false);
		double x1 = converter.frameToScale(selEndH, chart, false);
		controller.setRange1DValues(x0, x1);		
	}
	public void setRangeValues(double selAnchorH, double selEndH, double v) {
		double x0 = converter.frameToScale(selAnchorH, chart, false);
		double x1 = converter.frameToScale(selEndH, chart,  false);
		double y1 = converter.frameToScale(v, chart, true);
		controller.setRange1DValues(x0, x1, y1);		
	}

	public void setAxisBounds() 		// window has resized or selection set programmatically
	{
		disableAutoRanging();
		if (selectionAnchor < 0 || selectionMovingEnd < 0)
		{
			selectionAnchor = selectionH.getLayoutX();
			selectionMovingEnd = selectionH.getLayoutX() + selectionH.getBoundsInLocal().getWidth();
		}
		
		double xMin = controller.getSelectionStart();
		double xMax = controller.getSelectionEnd();
		if (Double.isNaN(xMin) || Double.isNaN(xMax)) return;
		if (Double.isInfinite(xMin) || Double.isInfinite(xMax)) return;
		double h0 = converter.scaleToFrame(xMin, chart, false);
		double h1 = converter.scaleToFrame(xMax, chart, false);
		double v0 = converter.scaleToFrame(controller.getSelectionHeight(), chart, true);
		setSelection(h0, h1);
		update(v0);
//		System.out.println(String.format("setAxisBounds %.2f - %.2f  @ %.2f  ", xMin, xMax, controller.getSelectionHeight()));
//		System.out.println(String.format("%.2f - %.2f  @ %.2f  \n\n",h0,h1, v0));
//		updateController();
		
		}
	
	private double getRangeFreq(XYChart<Number, Number> chart, double xMin, double xMax)
	{
		int ct = 0;
		List<XYChart.Series<Number, Number>> dataList = chart.getData();
		if (dataList == null || dataList.isEmpty()) return 0;
		XYChart.Series<Number, Number> data = dataList.get(0);
		if (data.getData().isEmpty()) return 0;
		for (Data<Number, Number> n : data.getData())
		{
			double x = n.getXValue().doubleValue();
			if ( BETWEEN(x, xMin, xMax))
				ct++;
		}
		return ct / (double) data.getData().size();
		
	}
	
	private void selectRange(XYChart<Number, Number> chart, double xMin, double xMax)
	{
		XYChart.Series<Number, Number> data = chart.getData().get(0);
		controller.selectRange(data.getName(), xMin, xMax);
	}
	
	public void hideSelection() {
		selectionAnchor = selectionMovingEnd = -1;
		selectionH.setVisible(false);
		
	}
	private Line leftBar, crossBar, rightBar;
	int resizing = 0;
	double dragStart = -1;
	double SLOP = 9;
	double yValue = 0;
	
	public double getYValue()	{ return yValue;	}
	public Group buildSubRangeGroup(XYChart<Number, Number> inChart, SubRangeLayer overlayer)
	{
		leftBar = new Line(20, 10, 20, 399);
		crossBar = new Line(20, 100, 220, 100);
		rightBar = new Line(220, 10, 220, 399);
		Group g = new Group();
//		update(0,100, 0);
		g.setMouseTransparent(true);
		leftBar.setStrokeWidth(2);		leftBar.setStroke(Color.PURPLE);
		crossBar.setStrokeWidth(4);		crossBar.setStroke(Color.PURPLE);
		rightBar.setStrokeWidth(2);		rightBar.setStroke(Color.PURPLE);
		g.getChildren().addAll(leftBar, crossBar, rightBar);
		g.setOpacity(0.3);
		return g;
	}
	
	public void update(double v)			// these are in frame (mouse) coords
	{
//		if (selectionStart > selectionEnd)
//		{
//			double d = selectionStart;
//			selectionStart = selectionEnd;
//			selectionEnd = d;
//		}
		
		Node chartPlotArea = chart.lookup(".chart-plot-background");
		Bounds bounds = chartPlotArea.getLayoutBounds();
		double offX = chartPlotArea.getLayoutX();
		double offY = chartPlotArea.getLayoutY() + 30 ;
		double top = offY + bounds.getMinY();				//TODO  mystery fudge factor 
		double bottom = offY + bounds.getMaxY();  
		double vMargin = 12;	
		v = pinValue(top+vMargin, bottom-vMargin, v);

		double left = bounds.getMinX() + offX;
		double right = bounds.getMaxX() + offX;  
		selectionAnchor = pinValue(left, right, selectionAnchor);
		selectionMovingEnd = pinValue(left, right, selectionMovingEnd);
//		System.out.println(String.format("update:   %.2f - %.2f  @ %.2f  ", selStartH , selEndH, v));
		
		leftBar.setStartX(selectionAnchor);		leftBar.setStartY(top);			leftBar.setEndX(selectionAnchor);	leftBar.setEndY(bottom);
		rightBar.setStartX(selectionMovingEnd);		rightBar.setStartY(top);		rightBar.setEndX(selectionMovingEnd);		rightBar.setEndY(bottom);
		crossBar.setStartX(selectionAnchor);		crossBar.setStartY(v);			crossBar.setEndX(selectionMovingEnd);		crossBar.setEndY(v);
		setRangeValues(selectionAnchor, selectionMovingEnd, v);
		
		double startX = leftBar.getStartX();
		double endX = rightBar.getStartX();
//		System.out.println(String.format(" %.2f - %.2f ", startX, endX));

	}

	private double pinValue(double top, double bottom, double v) {	return Math.min(Math.max(v, top), bottom);	}

	public int testHit(double x)
	{
		if (leftBar == null || rightBar == null) return 0;
		double startX = leftBar.getStartX();
		if (Math.abs(x - leftBar.getStartX()) < SLOP) return 1;
		if (Math.abs(x - rightBar.getStartX()) < SLOP) return 3;
		if (x > leftBar.getStartX() && x < rightBar.getStartX()) return 2;
		return 0;
	}

}
