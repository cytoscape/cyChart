package org.cytoscape.cyChart.internal.charts.twoD;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

import org.cytoscape.cyChart.internal.charts.Borders;
import org.cytoscape.cyChart.internal.charts.Cursors;
import org.cytoscape.cyChart.internal.charts.LogarithmicAxis;
import org.cytoscape.cyChart.internal.charts.Range;
import org.cytoscape.cyChart.internal.charts.RectangleUtil;
import org.cytoscape.cyChart.internal.charts.oneD.FrameScaleConverter;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
/*
 *   A SelectableScatterChart is a scatter chart that can show a two dimensional data set.
 *   Identifying a region in the XY space will take the subset of events in that region
 *  
 */
public class SelectableScatterChart extends AnchorPane
{
	FrameScaleConverter converter = new FrameScaleConverter();
	private ScatterChart<Number, Number> scatter;
	private final ScatterChartController controller;
	public ScatterChart<Number, Number> 	getScatterChart()	{ return scatter;	}

	//------------------------------------------------------------------------
	public SelectableScatterChart(ScatterChartController ctlr)
	{
		controller = ctlr;
//		setBorder(Borders.yellowBorder);
		AnchorPane.setTopAnchor(this, 5.0);
		AnchorPane.setBottomAnchor(this, 5.0);
		AnchorPane.setLeftAnchor(this, 5.0);
		AnchorPane.setRightAnchor(this, 5.0);
		addLayer("X", "Y", 0);
		getChildren().addAll(scatter);
		selectionLayerBuilder();
	}

	//------------------------------------------------------------------------
	private void addLayer(String xName, String yName, int transitionType)
	{
//		System.out.println("scatter");
		boolean xLog = controller.isXLog();
		boolean yLog = controller.isYLog();
		ValueAxis<Number> xAxis = xLog ? new LogarithmicAxis() : new NumberAxis();
		xAxis.setLabel(xName);
		
		ValueAxis<Number> yAxis = yLog ? new LogarithmicAxis() : new NumberAxis();
		yAxis.setLabel(yName);
		
		scatter = new ScatterChart<Number, Number>(xAxis, yAxis);
//		scatter.setBorder(Borders.blueBorder1);
		AnchorPane.setTopAnchor(scatter, 10.0);
		AnchorPane.setBottomAnchor(scatter, 10.0);
		AnchorPane.setLeftAnchor(scatter, 10.0);
		AnchorPane.setRightAnchor(scatter, 10.0);

		Node chartPlotArea = getPlotAreaNode();
		if (chartPlotArea != null)
		{
			Region rgn = (Region) chartPlotArea;
			rgn.setStyle("-fx-background-color: #CCCCCC;");
			rgn.setBorder(Borders.thinEtchedBorder);
		    ChangeListener<Number> paneSizeListener = (obs, oldV, newV) -> controller.resized();
		    scatter.widthProperty().addListener(paneSizeListener);
		    scatter.heightProperty().addListener(paneSizeListener);  	
		}
		scatter.setStyle(scatter.getStyle() + "-fx-legend-visible: false; ");
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
	private ValueAxis<Number> xAxis;
	private ValueAxis<Number> yAxis;
	private Rectangle selectionRectangleScaleDef;
	private Rectangle selectionRectangle;
//	private Label infoLabel;

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
		xAxis = (ValueAxis<Number>) scatter.getXAxis();
		yAxis = (ValueAxis<Number>) scatter.getYAxis();
		makeSelectionRectangle();
		addDragSelectionMechanism(getPlotAreaNode());
//		addInfoLabel();

		StackPane.setAlignment(selectionRectangle, Pos.TOP_LEFT);
	}

	private void makeSelectionRectangle()
	{
		selectionRectangle = new Rectangle(); 
		selectionRectangleScaleDef = new Rectangle();
		selectionRectangle.setManaged(false);
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
			onDragged(event); 
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

	Node getPlotAreaNode() 	{		return scatter.lookup(".chart-plot-background");	}	
	Bounds getPlotBounds() 	{		return getPlotAreaNode().getBoundsInParent();	}	

	private void onDragged(MouseEvent event) {
		if (event.isSecondaryButtonDown()) 	return;
		boolean option = event.isAltDown();
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
			drawSelectionRectangle(x, y, width, height, option);
//			System.out.println("x:" + x + " y:" + y);
		} else
		{
			double oldX = selRectStart.getX();
			double oldY = selRectStart.getY();
			double dx = event.getX() - oldX;
			double dy = event.getY() - oldY;
			Bounds chartPlotArea = getPlotBounds();
			double minAllowedX = chartPlotArea.getMinX();
			double maxAllowedX = minAllowedX + chartPlotArea.getWidth();
			double minAllowedY = chartPlotArea.getMinY();
			double maxAllowedY = minAllowedY + chartPlotArea.getHeight();

			double newLeft = selectionRectangle.getX() + dx;
			double newRight = newLeft + selectionRectangle.getWidth();
			double newTop = selectionRectangle.getY() + dy;
			double newBottom = newTop + selectionRectangle.getHeight();
			if (newLeft < minAllowedX || newRight > maxAllowedX) return;
			if (newTop < minAllowedY || newBottom > maxAllowedY) return;
//			System.out.println("x:" + oldX + " y:" + oldY);
			offsetRectangle(selectionRectangle, dx, dy);
			drawSelectionRectangleAt(event.getX() - offsetX, event.getY() - offsetY, option);
			selRectStart = new Point2D(event.getX(), event.getY());
		}
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
			boolean optionDrag = ev.isAltDown();
			double offsetX = chartRegion.getLayoutX();
			double offsetY = chartRegion.getLayoutY();
			selRectEnd = computeRectanglePoint(ev.getX()+offsetX, ev.getY()+offsetY);		// store current cursor position
			Rectangle2D r = union(selRectStart, selRectEnd);
			drawSelectionRectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), optionDrag);
			ev.consume();

		});
		
		chartRegion.setOnMouseReleased(ev -> {
			if (selRectStart == null || selRectEnd == null) 		return;
			if (isRectangleSizeTooSmall()) 							return;
			setAxisBounds();
			selRectStart = selRectEnd = null;
			setSelectionRectangleScale(rectDef(selectionRectangle, getPlotFrame()));
			makeSelectionRect(selectionRectangle);
			requestFocus();		// needed for the key event handler to receive events
			ev.consume();
		});


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
//		System.out.println("( " + x + ", " + y + " )");
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
	//-------------------------------------------------------------------------------

	private void drawSelectionRectangle(final double x, final double y, final double width, final double height, boolean optionDrag) {
		selectionRectangle.setVisible(true);
		selectionRectangle.setX(x);
		selectionRectangle.setY(y);
		selectionRectangle.setWidth(width);
		selectionRectangle.setHeight(height);
//		selectionRectangle.toFront();
	}
	private void drawSelectionRectangleAt(final double x, final double y, boolean optionDrag) {
		drawSelectionRectangle(x,y,selectionRectangle.getWidth(), selectionRectangle.getHeight(), optionDrag);
	}
	private void disableAutoRanging() {
		xAxis.setAutoRanging(false);
		yAxis.setAutoRanging(false);
	}

	//-------------------------------------------------------------------------------
	private void showInfo(int found, int total, double xMin, double xMax, double yMin, double yMax ) 
	{			
		NumberFormat fmt = new DecimalFormat("0.00");
		String s = found + " / " + total; //  + " in range X: [ " + fmt.format(xMin) + " - " + fmt.format(xMax) + "] Y: [ " + fmt.format(yMin) + " - " + fmt.format(yMax) + "]";
//		infoLabel.setText(s);
//		System.out.println(s);
		xRange = new Range(xMin, xMax);
		yRange = new Range(yMin, yMax);
		controller.setStatus(s, xRange, yRange);	
	}
	
	Range xRange = null, yRange = null; 
	
	//-------------------------------------------------------------------------------
	boolean verbose = false;
	public void resized()
	{
		Rectangle r = getScaleRect(getSelectionRectangleScale(), getPlotFrame());
		drawSelectionRectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight(), false);
		if (verbose) System.out.println("resized");
	}

	//-------------------------------------------------------------------------------
	private void setAxisBounds() {
		disableAutoRanging();
		if (selRectStart == null || selRectEnd == null)
		{
			selRectStart = new Point2D(selectionRectangle.getX(),
					selectionRectangle.getY() + selectionRectangle.getHeight());
			selRectEnd = new Point2D(selectionRectangle.getX() + selectionRectangle.getWidth(),
					selectionRectangle.getY());
		}
		Rectangle bounds = getPlotFrame();
		double xBase = bounds.getX();
		double yBase = bounds.getY();
		double selectionMinX = Math.min(selRectStart.getX(), selRectEnd.getX()) - xBase;
		double selectionMaxX = Math.max(selRectStart.getX(), selRectEnd.getX()) - xBase;
		double selectionMinY = Math.min(selRectStart.getY(), selRectEnd.getY()) - yBase;
		double selectionMaxY = Math.max(selRectStart.getY(), selRectEnd.getY()) - yBase;

		double xMin = converter.frameToScale(selectionMinX, scatter, false);
		double xMax = converter.frameToScale(selectionMaxX, scatter, false);
		double yMin = converter.frameToScale(selectionMaxY, scatter, true);
		double yMax = converter.frameToScale(selectionMinY, scatter, true);
		
		int found = countInRect(scatter, xMin, xMax, yMin, yMax);
		int total = getDataSize(scatter);
		String xName = xAxis.getLabel(), yName = yAxis.getLabel();
		controller.selectRange(xName, xMin, xMax, yName, yMin, yMax );
		showInfo(found, total, xMin, xMax, yMin, yMax);
	}
	
	//-------------------------------------------------------------------------------
	public Range getXRange()
	{
		if (selRectStart == null || selRectEnd == null) 		return new Range(0,4);
		double selectionMinX = Math.min(selRectStart.getX(), selRectEnd.getX());
		double selectionMaxX = Math.max(selRectStart.getX(), selRectEnd.getX());
		double xMin = converter.frameToScale(selectionMinX, scatter, false);
		double xMax = converter.frameToScale(selectionMaxX, scatter, false);
		return new Range(xMin, xMax);
	}
	public Range getYRange()
	{
		if (selRectStart == null || selRectEnd == null) 		return  new Range(0,1);
		double selectionMinY = Math.min(selRectStart.getY(), selRectEnd.getY());
		double selectionMaxY = Math.max(selRectStart.getY(), selRectEnd.getY());
		double yMin = converter.frameToScale(selectionMaxY, scatter, true);
		double yMax = converter.frameToScale(selectionMinY, scatter, true);
		return new Range(yMin, yMax);
	}
	
	//-------------------------------------------------------------------------------
	private int countInRect(XYChart<Number, Number> chart, double xMin, double xMax, double yMin, double yMax)
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
		return ct;
		
	}
	
	private int getDataSize(XYChart<Number, Number> chart)
	{
		if (chart.getData().size() == 0) return 0;
		XYChart.Series<Number, Number> data = chart.getData().get(0);
		return data.getData().size();
	}
	
	/**-------------------------------------------------------------------------------
	 *   Mouse handlers for clicks inside the selection rectangle
	 */
	boolean resizing = false;
//	double SLOP = 4;
	double offsetX = 0, offsetY = 0;
	
	private void makeSelectionRect(Rectangle marquee)
	{
		marquee.getStyleClass().add("selection");
		marquee.setOpacity(0.3);
		marquee.setFill(Color.CYAN);

		marquee.setOnMouseReleased(event -> {
			if (resizing && isRectangleSizeTooSmall())
				return;
			setAxisBounds();		// send new bounds to the controller
			selRectStart = selRectEnd = null;
			resizing = false;
			requestFocus(); // needed for the key event handler to receive events
			event.consume();
		});

//		marquee.setOnMouseClicked(ev -> { 	if (ev.getClickCount() > 1)	controller.selectionDoubleClick(); });	

		marquee.setOnMouseEntered(event -> { setCursor(marquee, event); } );
		marquee.setOnMouseMoved(event -> { setCursor(marquee, event); });
		marquee.setOnMouseExited(event -> {	marquee.setCursor(Cursor.DEFAULT);		});
		
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
			double h = event.getX();
			double v = event.getY();
			if (resizing)
			{
				// store current cursor position
				selRectEnd = computeRectanglePoint(h, v);
				if (selRectStart == null)
					selRectStart = RectangleUtil.oppositeCorner(event, marquee);
				if (selRectStart == null) return;
				if (selRectEnd == null)
					selRectEnd = new Point2D(event.getX(), event.getY());

				marquee.setX(Math.min(selRectStart.getX(), selRectEnd.getX()));
				marquee.setY(Math.min(selRectStart.getY(), selRectEnd.getY()));
				marquee.setWidth(Math.abs(selRectStart.getX() - selRectEnd.getX()));
				marquee.setHeight(Math.abs(selRectStart.getY() - selRectEnd.getY()));
//				System.out.println("x:" + x + " y:" + y);
			} else
			{
				Bounds chartPlotArea = getPlotBounds();
				double minAllowed = chartPlotArea.getMinX();
				double maxAllowed = minAllowed + chartPlotArea.getWidth()-60;
				boolean inRange = h >= minAllowed && h <= maxAllowed;
				if (!inRange) return;

				double minAllowedY = chartPlotArea.getMinY();
				double maxAllowedY = minAllowedY + chartPlotArea.getHeight()-30;		
				inRange = v >= minAllowedY && v <= maxAllowedY;
				if (!inRange) return;
				
 				double oldX = selRectStart.getX();
				double oldY = selRectStart.getY();
				double dx = h - oldX - offsetX;
				double dy = v - oldY - offsetY;

				if (selectionRectangle.getX() < minAllowed && dx < 0) return;
				if ((selectionRectangle.getX() + selectionRectangle.getWidth() > maxAllowed) && dx > 0) return;
				if (selectionRectangle.getY() < minAllowedY && dy < 0) return;
				if ((selectionRectangle.getY() + selectionRectangle.getHeight() > maxAllowedY) && dy > 0) return;

				marquee.setX(oldX + dx);
				marquee.setY(oldY + dy);
				selRectStart = new Point2D(h, v);
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

	//-------------------------------------------------------------------------------
	void setCursor(Rectangle r, MouseEvent event)
	{
		 r.setCursor(Cursors.getResizeCursor(RectangleUtil.getPos(event, r)));
	}

	//-------------------------------------------------------------------------------
	private Rectangle2D union(Point2D a, Point2D b)
	{
		if (a == null || b == null) return Rectangle2D.EMPTY;
		double x = Math.min(a.getX(), b.getX());
		double y = Math.min(a.getY(), b.getY());
		double width = Math.abs(a.getX() - b.getX());
		double height = Math.abs(a.getY() - b.getY());
		return new Rectangle2D(x,y,width,height);
	}

	//-------------------------------------------------------------------------------
	Rectangle getScaleRect(Rectangle def, Rectangle frame)
	{
		double frameWidth = frame.getWidth();
		double frameHeight = frame.getHeight();
		Rectangle r = new Rectangle(frame.getX() + def.getX() * frameWidth,
				frame.getY() + def.getY() * frameHeight, 
				def.getWidth() * frameWidth, def.getHeight() * frameHeight);
		return r;
	}

	//-------------------------------------------------------------------------------
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