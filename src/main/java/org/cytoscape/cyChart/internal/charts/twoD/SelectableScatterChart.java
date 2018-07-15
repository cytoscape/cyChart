package org.cytoscape.cyChart.internal.charts.twoD;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;

import org.cytoscape.cyChart.internal.charts.Borders;
import org.cytoscape.cyChart.internal.charts.Cursors;
import org.cytoscape.cyChart.internal.charts.MixedDataRow;
import org.cytoscape.cyChart.internal.charts.Range;
import org.cytoscape.cyChart.internal.charts.RectangleUtil;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


/*
 *   A SelectableScatterChart is a scatter chart that can show a two dimensional data set.
 *   Identifying a region in the XY space will take the subset of events in that region
 *  
 */
public class SelectableScatterChart extends VBox
{
	private ScatterChart<Number, Number> scatter;
	private final ScatterChartController controller;
	public ScatterChart<Number, Number> 	getScatterChart()	{ return scatter;	}
	
	public SelectableScatterChart(ScatterChartController ctlr, List<MixedDataRow> observableList, Label statusFeedback)
	{
		this(ctlr, statusFeedback);
//		addData(observableList);
	}

	//------------------------------------------------------------------------
	public SelectableScatterChart(ScatterChartController ctlr, Label statusFeedback)
	{
		controller = ctlr;
		infoLabel = statusFeedback;
		addLayer("Height", "Weight", 0);
		VBox pile = new VBox();
		pile.getChildren().addAll(scatter);
		getChildren().addAll(pile);
		selectionLayerBuilder();
		
	}

	//------------------------------------------------------------------------
	private void addLayer(String xName, String yName, int transitionType)
	{
		System.out.println("scatter");
		final NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel(xName);
//		xAxis.setOnMouseClicked(ev -> {
//			if (ev.isShiftDown()) prevXParm();
//			else				nextXParm();
//		});
		
		final NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel(yName);
//		yAxis.setOnMouseClicked(ev -> {
//			if (ev.isShiftDown()) prevYParm();
//			else				nextYParm();
//		});
		
		scatter = new ScatterChart<Number, Number>(xAxis, yAxis);
		// scatter.setTitle("title goes here");
		Node chartPlotArea = getPlotAreaNode();
		if (chartPlotArea != null)
		{
//			Region rgn = (Region) chartPlotArea;
//			rgn.setBorder(Borders.blueBorder1);
		    ChangeListener<Number> paneSizeListener = (obs, oldV, newV) -> resized();
		    scatter.widthProperty().addListener(paneSizeListener);
		    scatter.heightProperty().addListener(paneSizeListener);  	
		}
		scatter.setStyle(scatter.getStyle() + "-fx-legend-visible: false;  ");
//		Image curImage = (transitionType == 0) ? null : chartSnapshot();
//		if (transitionType != 0 && curImage != null && prevImage != null)
//			new Transitions(prevImage, curImage).play(Transitions.Transition.CUBE);
	
	}
	//------------------------------------------------------------------------
	public void setDataSeries(Series<Number, Number> series1) 
	{ 	
		StackPane stack = (StackPane) series1.getNode();
		if (stack != null) stack.setMaxSize(2, 2);
		scatter.getData().add(series1); 	
	}

	//------------------------------------------------------------------------
	public void setAxes(String x, String y) 
	{ 	
		scatter.getXAxis().setLabel(x); 
		scatter.getYAxis().setLabel(y); 	
	}


/*--------------------------------------------------------------------------------------------
 * This adds a layer on top of a the XY chart named "scatter". 
 *
 */

	private static final String INFO_LABEL_ID = "zoomInfoLabel";
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	private Rectangle selectionRectangleScaleDef;
	private Rectangle selectionRectangle;
	private Label infoLabel;

	private Point2D selRectStart = null;
	private Point2D selRectEnd = null;
	private boolean isRectangleSizeTooSmall() {
		if (selRectStart == null || selRectEnd == null ) return true;
		double width = Math.abs(selRectEnd.getX() - selRectStart.getX());
		double height = Math.abs(selRectEnd.getY() - selRectStart.getY());
		return width < 10 || height < 10;
	}

	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";


	public void selectionLayerBuilder() 
	{
		xAxis = (NumberAxis) scatter.getXAxis();
		yAxis = (NumberAxis) scatter.getYAxis();
		makeSelectionRectangle();
		addDragSelectionMechanism(getPlotAreaNode());
		addInfoLabel();

		StackPane.setAlignment(selectionRectangle, Pos.TOP_LEFT);
	}

	private void makeSelectionRectangle()
	{
		selectionRectangle = new Rectangle();  // SelectionRectangle();
		selectionRectangleScaleDef = new Rectangle();
		selectionRectangle.setManaged(false);
//		selectionRectangle.setFill(null);
//		selectionRectangle.setOpacity(0.2);		
		selectionRectangle.setOpacity(0.3);
		selectionRectangle.setFill(Color.CYAN);

		selectionRectangle.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);

		selectionRectangle.setStroke(Color.SADDLEBROWN);
		selectionRectangle.setStrokeWidth(2f);
		RectangleUtil.setupCursors(selectionRectangle);
		selectionRectangle.setOnMousePressed(event -> {
//			if (event.isSecondaryButtonDown()) 	return;
			Pos pos = RectangleUtil.getPos(event, selectionRectangle);
			resizing = RectangleUtil.inCorner(pos);
			if (resizing)
				selRectStart = RectangleUtil.oppositeCorner(event,selectionRectangle);
			else
			{
				selRectStart = new Point2D(event.getX(), event.getY());
				offsetX = event.getX() - selectionRectangle.getX();
				offsetY = event.getY() - selectionRectangle.getY();
			}
			event.consume();

		});

		selectionRectangle.setOnMouseDragged(event -> {
			if (event.isSecondaryButtonDown()) 	return;

			if (resizing)
			{
				// store current cursor position
				selRectEnd = computeRectanglePoint(event.getX(), event.getY());
				if (selRectStart == null)
					selRectStart = RectangleUtil.oppositeCorner(event,selectionRectangle);
//selRectStart = new Point2D(event.getX(), event.getY());			// ERROR -- will reset instead of resize
				double x = Math.min(selRectStart.getX(), selRectEnd.getX());
				double y = Math.min(selRectStart.getY(), selRectEnd.getY());
				double width = Math.abs(selRectStart.getX() - selRectEnd.getX());
				double height = Math.abs(selRectStart.getY() - selRectEnd.getY());
				drawSelectionRectangle(x, y, width, height);
//				System.out.println("x:" + x + " y:" + y);
			} else
			{
				double oldX = selRectStart.getX();
				double oldY = selRectStart.getY();
				double dx = event.getX() - oldX;
				double dy = event.getY() - oldY;
//				System.out.println("x:" + oldX + " y:" + oldY);
				offsetRectangle(selectionRectangle, dx, dy);
				drawSelectionRectangleAt(event.getX() - offsetX, event.getY() - offsetY);
				selRectStart = new Point2D(event.getX(), event.getY());
			}
			event.consume();
		});
		
		
		selectionRectangle.setOnMouseReleased(event -> {
//			if (selRectStart == null || selRectEnd == null) 		return;
//			if (isRectangleSizeTooSmall()) 							return;
			selectionRectangleScaleDef  = rectDef(selectionRectangle, getPlotFrame());
			setAxisBounds();
			selRectStart = selRectEnd = null;
			requestFocus();		// needed for the key event handler to receive events
			event.consume();
		
		});
	}
	
	private void offsetRectangle(Rectangle r, double dx, double dy)
	{
//		NumberFormat fmt = new DecimalFormat("0.00");
//		System.out.println("dx:" + fmt.format(dx) + "dx:" + fmt.format(dy));
		r.setX(r.getX() + dx - offsetX);
		r.setY(r.getY() + dy - offsetY);
	}

//	public void imagePeek(Image img)
//	{
////		ImageView view = new ImageView(img);
//		Alert dialog = new Alert(AlertType.CONFIRMATION);
//		dialog.setTitle("This shows you the image");
//		dialog.setContentText("");
//		dialog.setGraphic(new ImageView(img));
//		dialog.showAndWait();
//
//	}
//	
	Node getPlotAreaNode() 
	{
		return scatter.lookup(".chart-plot-background");
	}	

	
	Rectangle getAxisScale()
	{
		double xmin = xAxis.getLowerBound();
		double ymin = yAxis.getLowerBound();
		double xmax = xAxis.getUpperBound();
		double ymax = yAxis.getUpperBound();
		return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);	
	}
	
	Rectangle getPlotFrame()
	{
		Node chartPlotArea = getPlotAreaNode() ;
		if (chartPlotArea == null) return new Rectangle(0,0,0,0);
		double w = chartPlotArea.getLayoutBounds().getWidth();
		double h = chartPlotArea.getLayoutBounds().getHeight();
		return new Rectangle(chartPlotArea.getLayoutX(),chartPlotArea.getLayoutY(),w, h);	
		
	}
	/**
	 * The info label shows a short info text
	 */
	private void addInfoLabel() {
		if (infoLabel == null)
			infoLabel = new Label("");
		infoLabel.setId(INFO_LABEL_ID);
		getChildren().add(infoLabel);
		StackPane.setAlignment(infoLabel, Pos.TOP_RIGHT);
		infoLabel.setVisible(false);
	}

	
	/**----------------------------------------------------------------------------------
	 * Adds a mechanism to select an area in the chart 
	 */
	private void addDragSelectionMechanism(Node chartRegion) 
	{
		chartRegion.setOnMousePressed( ev -> {
			if (ev.isSecondaryButtonDown()) 	return;	
			if (!getChildren().contains(selectionRectangle))
				getChildren().add(selectionRectangle);
			selectionRectangle.toFront();
			double offsetX = chartRegion.getLayoutX();
			double offsetY = chartRegion.getLayoutY();
			selRectStart = computeRectanglePoint(ev.getX()+offsetX, ev.getY()+offsetY);		// store position of initial click
			ev.consume();
		});
		chartRegion.setOnMouseDragged(ev -> {
			if (ev.isSecondaryButtonDown()) 	return;	
			double offsetX = chartRegion.getLayoutX();
			double offsetY = chartRegion.getLayoutY();
			selRectEnd = computeRectanglePoint(ev.getX()+offsetX, ev.getY()+offsetY);		// store current cursor position
			Rectangle2D r = union(selRectStart, selRectEnd);
			drawSelectionRectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
			ev.consume();

		});
		chartRegion.setOnMouseReleased(ev -> {
			if (selRectStart == null || selRectEnd == null) 		return;
			if (isRectangleSizeTooSmall()) 							return;
			setAxisBounds();
			selRectStart = selRectEnd = null;
			setSelectionRectangleScale(rectDef(selectionRectangle, getPlotFrame()));
			makeGate(selectionRectangle);
//		if (tool == gater)
//	if (ev.isShiftDown())
//		addGate(selectionRectangle);
//			getChildren().remove(selectionRectangle);

			requestFocus();		// needed for the key event handler to receive events
			ev.consume();

		});

//		setOnKeyReleased(ev -> {
//			if (KeyCode.ESCAPE.equals(ev.getCode())) {
//				xAxis.setAutoRanging(true);
//				yAxis.setAutoRanging(true);
//				infoLabel.setVisible(false);
//			}
//		});
	}

	private void setSelectionRectangleScale(Rectangle r) {		selectionRectangleScaleDef = r;		}
	private Rectangle getSelectionRectangleScale() {		return selectionRectangleScaleDef;		}

	private Point2D computeRectanglePoint(double eventX, double eventY) {
		double lowerBoundX = computeOffsetInChart(xAxis, false);
		double upperBoundX = lowerBoundX + xAxis.getWidth();
		double lowerBoundY = computeOffsetInChart(yAxis, true);
		double upperBoundY = lowerBoundY + yAxis.getHeight();
		// make sure the rectangle's end point is in the interval defined by the lower and upper bounds for each
		// dimension
		double x = Math.max(lowerBoundX, Math.min(eventX, upperBoundX));
		double y = Math.max(lowerBoundY, Math.min(eventY, upperBoundY));
		return new Point2D(x, y);
	}

	/**
	 * Computes the pixel offset of the given node inside the chart node.
	 * 
	 * @param node
	 *            the node for which to compute the pixel offset
	 * @param vertical
	 *            flag that indicates whether the horizontal or the vertical dimension should be taken into account
	 * @return the offset inside the chart node
	 */
	private double computeOffsetInChart(Node node, boolean vertical) {
		double offset = 0;
		do {
			offset += (vertical) ? node.getLayoutY() : node.getLayoutX();
			node = node.getParent();
		} while (node != null && node != scatter);
		return offset;
	}
	/**-------------------------------------------------------------------------------
	 */
	private void drawSelectionRectangle(final double x, final double y, final double width, final double height) {
		selectionRectangle.setVisible(true);
		selectionRectangle.setX(x);
		selectionRectangle.setY(y);
		selectionRectangle.setWidth(width);
		selectionRectangle.setHeight(height);
//		selectionRectangle.toFront();
	}
	private void drawSelectionRectangleAt(final double x, final double y) {
		drawSelectionRectangle(x,y,selectionRectangle.getWidth(), selectionRectangle.getHeight());
	}
	private void disableAutoRanging() {
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
	}

	private void showInfo(double freq, double xMin, double xMax, double yMin, double yMax ) 
	{			
		NumberFormat fmt = new DecimalFormat("0.00");
		String s = fmt.format(freq * 100) +  "% for X( " + fmt.format(xMin) + " - " + fmt.format(xMax) + ") Y( " + fmt.format(yMin) + " - " + fmt.format(yMax) + ")";
		infoLabel.setText(s);
//		System.out.println(s);
		infoLabel.setVisible(true);	
		xRange = new Range(xMin, xMax);
		yRange = new Range(yMin, yMax);
	}
	
	Range xRange = null, yRange = null; 
	
	boolean verbose = false;
	private void resized()
	{
		Rectangle r = getScaleRect(getSelectionRectangleScale(), getPlotFrame());
		drawSelectionRectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
		if (verbose) System.out.println("resized");
	}

	private void setAxisBounds() {
		disableAutoRanging();
		if (selRectStart == null || selRectEnd == null)
		{
			selRectStart = new Point2D(selectionRectangle.getX(),
					selectionRectangle.getY() + selectionRectangle.getHeight());
			selRectEnd = new Point2D(selectionRectangle.getX() + selectionRectangle.getWidth(),
					selectionRectangle.getY());
		}
		double selectionMinX = Math.min(selRectStart.getX(), selRectEnd.getX());
		double selectionMaxX = Math.max(selRectStart.getX(), selRectEnd.getX());
		double selectionMinY = Math.min(selRectStart.getY(), selRectEnd.getY());
		double selectionMaxY = Math.max(selRectStart.getY(), selRectEnd.getY());

		double xMin = frameToScaleX(selectionMinX);
		double xMax = frameToScaleX(selectionMaxX);
		double yMin = frameToScaleY(selectionMaxY);
		double yMax = frameToScaleY(selectionMinY);
		
		double freq = countInRect(scatter, xMin, xMax, yMin, yMax);
		String xName = xAxis.getLabel(), yName = yAxis.getLabel();
		controller.selectRange(xName, xMin, xMax, yName, yMin, yMax );
		showInfo(freq, xMin, xMax, yMin, yMax);

	}
	
	private double countInRect(XYChart<Number, Number> chart, double xMin, double xMax, double yMin, double yMax)
	{
		Objects.requireNonNull(chart);
		if (chart.getData().size() == 0) return 0;
		XYChart.Series<Number, Number> data = chart.getData().get(0);
		Objects.requireNonNull(data);
		int ct = 0;
		for (Data<Number, Number> n : data.getData())
		{
			double x = n.getXValue().doubleValue();
			double y = n.getYValue().doubleValue();
			if ( (x >= xMin && x < xMax) && (y >= yMin && y < yMax))
				ct++;
		}
		return ct / (double) data.getData().size();
		
	}
	
	/**-------------------------------------------------------------------------------
	 *   Mouse handlers for clicks inside the selection rectangle
	 */
	boolean resizing = false;
//	double SLOP = 4;
	double offsetX = 0, offsetY = 0;



	
	private void makeGate(Rectangle marquee)
	{
		marquee.getStyleClass().add("selection");
		marquee.setOpacity(0.3);
		marquee.setFill(Color.CYAN);
//		scatter.addRectangleOverlay(displayGate);
		// scatter.add
//		System.out.println("Added Gate: " + displayGate.toString());
		marquee.setOnMouseDragged(event -> {
			if (resizing && isRectangleSizeTooSmall())
				return;

			// store current cursor position
			selRectEnd = computeRectanglePoint(event.getX(), event.getY());
			Rectangle2D union = union(selRectStart, selRectEnd);
			drawSelectionRectangle(union);
			event.consume();
		});
		marquee.setOnMouseReleased(event -> {
			if (resizing && isRectangleSizeTooSmall())
				return;
			setAxisBounds();		// send new bounds to the controller
			selRectStart = selRectEnd = null;
			resizing = false;
			requestFocus(); // needed for the key event handler to receive events
			event.consume();
		});

		marquee.setOnMouseClicked(ev -> { 	if (ev.getClickCount() > 1)	controller.selectionDoubleClick(); });	

		marquee.setOnMouseEntered(event -> {
			marquee.setCursor(Cursors.getResizeCursor(RectangleUtil.getPos(event, marquee)));
		});
		
		marquee.setOnMouseMoved(event -> {
			marquee.setCursor(Cursors.getResizeCursor(RectangleUtil.getPos(event, marquee)));
		});
		
		marquee.setOnMouseExited(event -> {		
			marquee.setCursor(Cursor.DEFAULT);
		});
		
		marquee.setOnMousePressed(event -> {
//			if (event.isSecondaryButtonDown()) 	return;
			Pos pos = RectangleUtil.getPos(event, marquee);
			resizing = RectangleUtil.inCorner(pos);
			if (resizing)
				selRectStart = RectangleUtil.oppositeCorner(event,marquee);
			else
			{
				selRectStart = new Point2D(event.getX(), event.getY());
				offsetX = event.getX() - marquee.getX();
				offsetY = event.getY() - marquee.getY();
			}
			event.consume();

		});

		marquee.setOnMouseDragged(event -> {
			if (event.isSecondaryButtonDown()) 	return;

			if (resizing)
			{
				// store current cursor position
				selRectEnd = computeRectanglePoint(event.getX(), event.getY());
				if (selRectStart == null)
					selRectStart = RectangleUtil.oppositeCorner(event, marquee);
				if (selRectStart == null) return;
				if (selRectEnd == null)
					selRectEnd = new Point2D(event.getX(), event.getY());
//selRectStart = new Point2D(event.getX(), event.getY());			// ERROR -- will reset instead of resize
				marquee.setX(Math.min(selRectStart.getX(), selRectEnd.getX()));
				marquee.setY(Math.min(selRectStart.getY(), selRectEnd.getY()));
				marquee.setWidth(Math.abs(selRectStart.getX() - selRectEnd.getX()));
				marquee.setHeight(Math.abs(selRectStart.getY() - selRectEnd.getY()));
//				System.out.println("x:" + x + " y:" + y);
			} else
			{
				double oldX = selRectStart.getX();
				double oldY = selRectStart.getY();
				double dx = event.getX() - oldX - offsetX;
				double dy = event.getY() - oldY - offsetY;
//				System.out.println("x:" + oldX + " y:" + oldY);
				marquee.setX(oldX + dx);
				marquee.setY(oldY + dy);
//				offsetRectangle(displayGate, dx, dy);
//				drawSelectionRectangleAt(event.getX() - offsetX, event.getY() - offsetY);
				selRectStart = new Point2D(event.getX(), event.getY());
			}
			event.consume();
		});
		
		
		marquee.setOnMouseReleased(event -> {
//			if (selRectStart == null || selRectEnd == null) 		return;
//			if (isRectangleSizeTooSmall()) 							return;
//			gateDef  = rectDef(displayGate, getPlotFrame());
			setAxisBounds();
			selRectStart = selRectEnd = null;
			requestFocus();		// needed for the key event handler to receive events
			event.consume();
		
		});
		
	}

		/**
		 * Draws a selection box in the view.
		 */
	private void drawSelectionRectangle(Rectangle2D r)
	{
		selectionRectangle.setVisible(true);
		selectionRectangle.setX(r.getMinX());
		selectionRectangle.setY(r.getMinY());
		selectionRectangle.setWidth(r.getWidth());
		selectionRectangle.setHeight(r.getHeight());
	}

	Rectangle2D union(Point2D a, Point2D b)
	{
		if (a == null || b == null) return Rectangle2D.EMPTY;
		double x = Math.min(a.getX(), b.getX());
		double y = Math.min(a.getY(), b.getY());
		double width = Math.abs(a.getX() - b.getX());
		double height = Math.abs(a.getY() - b.getY());
		return new Rectangle2D(x,y,width,height);
	}
	
	private double frameToScaleX(double value)
	{
		Rectangle frame = getPlotFrame();
		double chartZeroX = frame.getX();
		double chartWidth = frame.getWidth();
		return computeBound(value, chartZeroX, chartWidth, xAxis.getLowerBound(), xAxis.getUpperBound(), false);
	}

	private double frameToScaleY(double value)
	{
		Rectangle frame = getPlotFrame();
		double chartZeroY = frame.getY();
		double chartHeight = frame.getHeight();
		return computeBound(value, chartZeroY, chartHeight, yAxis.getLowerBound(), yAxis.getUpperBound(),true);
	}
	
	
	private double computeBound(double pixelPosition, double pixelOffset, double pixelLength, double lowerBound,
			double upperBound, boolean axisInverted) {
		double pixelPositionWithoutOffset = pixelPosition - pixelOffset;
		double relativePosition = pixelPositionWithoutOffset / pixelLength;
		double axisLength = upperBound - lowerBound;

		// The screen's y axis grows from top to bottom, whereas the chart's y axis goes from bottom to top.
		// That's why we need to have this distinction here.
		double offset = 0;
		int sign = 0;
		if (axisInverted) {		offset = upperBound;	sign = -1;		} 
		else 			  {		offset = lowerBound;	sign = 1;		}

		double newBound = offset + sign * relativePosition * axisLength;
		return newBound;

}


	Rectangle getScaleRect(Rectangle def, Rectangle frame)
	{
		double frameWidth = frame.getWidth();
		double frameHeight = frame.getHeight();
		Rectangle r = new Rectangle(frame.getX() + def.getX() * frameWidth,
				frame.getY() + def.getY() * frameHeight, 
				def.getWidth() * frameWidth, def.getHeight() * frameHeight);
		return r;
	}
//
//	Rectangle rescaleRect(Rectangle def, Rectangle frame)
//	{
//		Rectangle display = new Rectangle();
//		double frameWidth = frame.getWidth();
//		double frameHeight = frame.getHeight();
//		display.setX(frame.getX() + def.getX() * frameWidth);
//		display.setY(frame.getY() + def.getY() * frameHeight);
//		display.setWidth(def.getWidth() * frameWidth);
//		display.setHeight(def.getHeight() * frameHeight);
//		return display;
//	}

	Rectangle rectDef(Rectangle child, Rectangle frame)
	{
		double frameWidth = frame.getWidth();
		double frameHeight = frame.getHeight();
		Rectangle r = new Rectangle((child.getX() - frame.getX()) / frameWidth,
				(child.getY() - frame.getY()) / frameHeight, 
				child.getWidth() / frameWidth, child.getHeight() / frameHeight);
		return r;
	}



}
