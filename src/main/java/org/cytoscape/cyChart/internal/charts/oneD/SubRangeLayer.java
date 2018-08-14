package org.cytoscape.cyChart.internal.charts.oneD;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.cytoscape.cyChart.internal.charts.Range;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;


public class SubRangeLayer			// a 1D GateLayer
{
/**
 * This class adds a layer on top of a histogram chart. 
 *
 */
	final private StackPane stackPane;
	public StackPane getPane()	{ 	return stackPane;	}
	public void clear() 		{	stackPane.getChildren().clear();			}
	
	private LineChart<Number, Number> chart;
	private ValueAxis<Number> xAxis;
	private ValueAxis<Number> yAxis;
	double chartOffsetX, chartOffsetY;
	private HistogramChartController controller;
	private Group selectionH;
//	private Label infoLabel;
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
	 * @param stackPane
	 *            the pane on which the selection rectangle will be drawn.
	 */
	public SubRangeLayer(LineChart<Number, Number> inChart, StackPane inPane, HistogramChartController ctrol) 
	{
		stackPane = inPane;
		chart = inChart;
		controller = ctrol;
		
		xAxis = (ValueAxis<Number>) chart.getXAxis();
		yAxis = (ValueAxis<Number>) chart.getYAxis();
		
		selectionH = getSubRangeGroup(); 
		selectionH.setVisible(true);
		selectionH.setManaged(false);
		update(0);
		selectionH.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);
		selectionH.setStyle("-fx-fillcolor: CYAN; -fx-strokewidth: 4;");
		stackPane.getChildren().add(selectionH);

		addDragSelectionMechanism();
		addInfoLabel();
	    ChangeListener<Number> paneSizeListener = (obs, oldV, newV) -> setAxisBounds();
	    stackPane.widthProperty().addListener(paneSizeListener);
	    stackPane.heightProperty().addListener(paneSizeListener);  	    
	}


	private void addInfoLabel() {
		positionLabel = new Label("positionLabel");
		stackPane.getChildren().add(positionLabel);
		StackPane.setAlignment(positionLabel, Pos.TOP_RIGHT);
		positionLabel.setVisible(false);
	}

	public void updateController()
	{
		setRangeValues(selectionAnchor, selectionMovingEnd);
		double xMin = controller.getSelectionStart();
		double xMax = controller.getSelectionEnd();
		NumberFormat fmt = new DecimalFormat("0.00");
		String s = getRangeCount(chart, xMin, xMax) + " / " + controller.getDataSize(chart) +   " are between " + fmt.format(xMin) + " and " + fmt.format(xMax) ;
		
		Range xRange = new Range(xMin, xMax);
		controller.setStatus(s, xRange, null);
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
		Node chartPlotArea = controller.getPlotAreaNode();
		chartPlotArea.setOnMouseMoved(event -> 		{ 	onMouseMoved(event);	});
		chartPlotArea.setOnMouseExited(event -> 	{ 	positionLabel.setVisible(false);  });
		chartPlotArea.setOnMousePressed(event -> 	{	onPressed(event);		});
		chartPlotArea.setOnMouseDragged(event -> 	{  	onDragged(event);	 	});
		chartPlotArea.setOnMouseReleased(event -> 	{  	onReleased(event); });
		chartPlotArea.setOnKeyReleased(event -> 	{	});  // TODO
	}
	
	private Point2D getPosition(MouseEvent event)
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
	
	public void reportRange() {

		if (selectionAnchor > 0 &&  selectionMovingEnd > 0)
		{
			double min = Math.min(selectionAnchor, selectionMovingEnd);
			double max = Math.max(selectionAnchor, selectionMovingEnd);
			double x0 = converter.frameToScale(min, chart, false);
			double x1 = converter.frameToScale(max, chart, false);
			String msg = String.format("%.1f - %.1f ", x0, x1);
//			System.out.println(msg);
//			positionLabel.setText();
//			positionLabel.setVisible(true);
		}
		else positionLabel.setVisible(false);
			
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
		Node chartPlotArea = controller.getPlotAreaNode();
	    chartOffsetX = chartPlotArea.getLayoutX();
	    chartOffsetY = chartPlotArea.getLayoutY();
		double x = event.getX() + chartOffsetX;
		double y = event.getY() + chartOffsetY;
//		debugH(x);
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
			previousH = x - chartOffsetX;
			dragging = true;
			if (hitSpot == 1)  			setSelection(right, x);
			else if (hitSpot == 2)  	setSelection(left, right);
			else if (hitSpot == 3)  	setSelection(left, x);
			else if (x < leftBar.getStartX())
				setSelection(right, x);
			else if (x > rightBar.getStartX()-SLOP)
				setSelection(left, x);
		}
		else
		{
			selectionH.setVisible(true);
			dragging = true;
			setSelection(event.getX(), event.getX());
			update(y);
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
		Node chartPlotArea = controller.getPlotAreaNode();
		double minAllowed = -1;   //chartPlotArea.getLayoutX();
		double maxAllowed = minAllowed + chartPlotArea.getLayoutBounds().getWidth() + 2;		//+ 6
		double h = event.getX(); // + chartPlotArea.getLayoutX();
//		System.out.println("onDragged: " + h + ", " + event.getY());
		boolean inRange = h >= minAllowed && h <= maxAllowed;
		if (!inRange) 
			{
//			System.out.println(String.format("%.2f <= %.2f <= %.2f ", minAllowed, h, maxAllowed));
			return;
			}
		
		double v = event.getY();
		double minYAllowed = 0;
		double maxYAllowed = chartPlotArea.getLayoutBounds().getHeight();
		boolean inVRange = v >= minYAllowed && v <= maxYAllowed;
		if (!inVRange) 	return;
		
		double delta = h - previousH;
		if (delta == 0) return;
		
		if (hitSpot == 2)
			offsetSelection(delta);
		else 		
			selectionMovingEnd = h;
		update(v);
		previousH = h;
	}

	private void offsetSelection(double delta)
	{
//		System.out.println("Offsetting: " + delta);
		Node chartPlotArea = controller.getPlotAreaNode();
		double minAllowed = 0; // chartPlotArea.getLayoutX();
		double maxAllowed = chartPlotArea.getLayoutBounds().getWidth();		//minAllowed + 
		double left = Math.min(selectionAnchor, selectionMovingEnd) + delta;
		double right = Math.max(selectionAnchor, selectionMovingEnd) + delta;
//		System.out.println("left " + left);
		if (left <= minAllowed || right > maxAllowed) 
			return;
		selectionAnchor += delta;		
		selectionMovingEnd += delta;	
//		System.out.println("selection: " + selectionAnchor + " - " + selectionMovingEnd);
	}
	
	
	private void onReleased(MouseEvent event) {
		if (!dragging) return;
		boolean ok = selectionAnchor > 0 && selectionMovingEnd > 0;
		if (ok && isSelectionSizeTooSmall()) 
			ok = false;
		double width  = controller.getPlotAreaNode().getBoundsInParent().getWidth();
		double h = Math.max(Math.min(event.getX(), width), 0); // + chartPlotArea.getLayoutX();
		if (hitSpot != 2) selectionMovingEnd = h;
		double v = event.getY();
		if (ok)
		{
//			selectionH.update(selectionStart, selectionEnd, y);
			previousH = -1;
//			setAxisBounds();
			double right = Math.max(selectionAnchor, selectionMovingEnd);
			double left = Math.min(selectionAnchor, selectionMovingEnd);
			setSelection(left,right);
			update(v);
			updateController();
			reportRange();
			setSelection(-1, -1); 
		} 
		stackPane.requestFocus();		// needed for the key event handler to receive events
		dragging = false;
		event.consume();		
	}


	//-----------------------------------------------------------------
	
	public void setRangeValues(double selAnchorH, double selEndH) {
		double x0 = converter.frameToScale(selAnchorH, chart, false);
		double x1 = converter.frameToScale(selEndH, chart, false);
		controller.setRangeValues(x0, x1);		
	}
	public void setRangeValues(double selAnchorH, double selEndH, double v) {
		double x0 = converter.frameToScale(selAnchorH, chart, false);
		double x1 = converter.frameToScale(selEndH, chart,  false);
		double y1 = converter.frameToScale(v, chart, true);
		controller.setRangeValues(x0, x1, y1, y1);		
	}

	//-----------------------------------------------------------------
	private void disableAutoRanging() {
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
	}

	//-----------------------------------------------------------------
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
		double v0 = converter.scaleToFrame(controller.getSelectionTop(), chart, true);
		setSelection(h0, h1);
		update(v0);
	}

	//-----------------------------------------------------------------
	private int getRangeCount(XYChart<Number, Number> chart, double xMin, double xMax)
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
		return ct;
	}
	
	private void selectRange(XYChart<Number, Number> chart, double xMin, double xMax)
	{
		List<XYChart.Series<Number, Number>> dataList = chart.getData();
		if (dataList==null || dataList.isEmpty()) return;
		XYChart.Series<Number, Number> data = chart.getData().get(0);
		controller.selectRange(data.getName(), xMin, xMax);
	}
	
	public void hideSelection() {
		selectionAnchor = selectionMovingEnd = -1;
		selectionH.setVisible(false);
	}
	
	//-----------------------------------------------------------------
	private Group groupH = new Group();
	private Line leftBar, crossBar, rightBar;
	int resizing = 0;
	double dragStart = -1;
	double SLOP = 9;
	double yValue = 0;
	
	public double getYValue()	{ return yValue;	}
	
	//-----------------------------------------------------------------
	public Group getSubRangeGroup()
	{
		leftBar = new Line(20, 10, 20, 399);
		crossBar = new Line(20, 100, 220, 100);
		rightBar = new Line(220, 10, 220, 399);
//		update(0,100, 0);
		leftBar.setStrokeWidth(2);		leftBar.setStroke(Color.PURPLE);
		crossBar.setStrokeWidth(4);		crossBar.setStroke(Color.PURPLE);
		rightBar.setStrokeWidth(2);		rightBar.setStroke(Color.PURPLE);
		groupH.getChildren().clear();
		groupH.getChildren().addAll(leftBar, crossBar, rightBar);
		groupH.setOpacity(0.3);
		groupH.setMouseTransparent(true);
		return groupH;
	}
	//-----------------------------------------------------------------
	public void update(double v)			// these are in frame (mouse) coords
	{
		Bounds bounds = controller.getPlotBounds();
		double offX = bounds.getMinX();
		double offY = bounds.getMinY();
		double top = 14;  
		double bottom = top + bounds.getHeight();    
		double vMargin = 12;	
		v = pinValue(vMargin, bottom-vMargin, v);

		double left = 0; 
		double right = left + bounds.getWidth(); 
		selectionMovingEnd = pinValue(left, right, selectionMovingEnd);
		double a1 = selectionAnchor + offX;
		double a2 = selectionMovingEnd + offX;  
		double x1 = Math.min(a1,  a2);
		double x2 = Math.max(a1,  a2);
		double y = v  + offY;	//

//		System.out.println(String.format("update:   %.2f - %.2f  @ %.2f  ", x1 , x2, y));
		
		setLine(leftBar, x1, top, x1, bottom);
		setLine(rightBar, x2, top, x2, bottom);
		setLine(crossBar, x1, y, x2, y);
		setRangeValues(Math.min(selectionAnchor, selectionMovingEnd), Math.max(selectionAnchor, selectionMovingEnd), v);
	}

	private void setLine(Line bar, double x1, double y1, double x2, double y2) {
		bar.setStartX(x1);		bar.setStartY(y1);		
		bar.setEndX(x2);		bar.setEndY(y2);
	}
	private double pinValue(double top, double bottom, double v) {	return Math.min(Math.max(v, top), bottom);	}

	// 0 is outside, 1=left bar, 2=middle, 3=right bar
	public int testHit(double x)
	{
		if (leftBar == null || rightBar == null) return 0;
		if (Math.abs(x - leftBar.getStartX()) < SLOP) return 1;
		if (Math.abs(x - rightBar.getStartX()) < SLOP) return 3;
		if (x > leftBar.getStartX() && x < rightBar.getStartX()) return 2;
		return 0;
	}

}
