package org.cytoscape.cyChart.internal.charts.twoD;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Objects;

import org.cytoscape.cyChart.internal.charts.oneD.FrameScaleConverter;
import org.cytoscape.cyChart.internal.model.LinearRegression;
import org.cytoscape.cyChart.internal.model.LogarithmicAxis;
import org.cytoscape.cyChart.internal.model.Range;
import org.cytoscape.cyChart.internal.model.RectangleUtil;
import org.cytoscape.cyChart.internal.view.Cursors;

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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
/*
 *   A SelectableScatterChart is a scatter chart that can show a two dimensional data set.
 *   Identifying a region in the XY space will take the subset of events in that region
 *   It consists of a regular ScatterChart<> class in an AnchorPane.  
 *   selectionRectangle: an overlaid rectangle in the anchor pane, listening to mouse events
 *   regressionLine: an (optionally visible) overlaid line with slope calculated based on the displayed dataset
 *  
 *  frame coordinates refer to pixels on the screen, scale coordinates are the model space (as shown on axes)
 */
public class SelectableScatterChart extends AnchorPane
{
	FrameScaleConverter converter = new FrameScaleConverter();
	public FrameScaleConverter getConverter() 					{		return converter;	}
	private ScatterChart<Number, Number> scatter;
	private final ScatterChartController controller;
	public ScatterChart<Number, Number> 	getScatterChart()	{ 		return scatter;	}

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
		clearRegression();
	}

	//------------------------------------------------------------------------
	private void addLayer(String xName, String yName, int transitionType)
	{
//		System.out.println("scatter");
		boolean xLog = controller.isXLog();
		boolean yLog = controller.isYLog();
		ValueAxis<Number> xAxis = xLog ? new LogarithmicAxis() : new NumberAxis();
		xAxis.setLabel(xName);
		xAxis.setLowerBound(-100);
		
		ValueAxis<Number> yAxis = yLog ? new LogarithmicAxis() : new NumberAxis();
		yAxis.setLabel(yName);
		
		scatter = new ScatterChart<Number, Number>(xAxis, yAxis);
//		
//		URL sheet = getClass().getClassLoader().getResource("/org/cytoscape/cyChart/internal/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getClassLoader().getResource("/org/cytoscape/cyChart/internal/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getClassLoader().getResource("../resources/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getClassLoader().getResource("resources/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getClassLoader().getResource("/../org/cytoscape/cyChart/internal/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getClassLoader().getResource("/../../org/cytoscape/cyChart/internal/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getClassLoader().getResource("/../../../org/cytoscape/cyChart/internal/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getClassLoader().getResource("/../../../../org/cytoscape/cyChart/internal/chart.css");
//		if (sheet == null) 
//			sheet = SelectableScatterChart.class.getClassLoader().getResource("../resources/chart.css");
//		if (sheet == null) 
//			sheet = CyActivator.class.getResource("../resources/chart.css");
//		if (sheet == null)
//			sheet = SelectableScatterChart.class.getResource("resources/chart.css");
//		if (sheet == null) 
//			sheet = SelectableScatterChart.class.getResource("./chart.css");
//		if (sheet == null) 
//			sheet = SelectableScatterChart.class.getResource("chart.css");
//		if (sheet != null)
//			scatter.getStylesheets().add(sheet.toExternalForm());
//		scatter.getStylesheets().add("chart.css");
		controller.anchor(scatter);
		controller.setChart(scatter);

		String rootStr = ".root {\n    -fx-font-size: 24pt;\n -fx-font-family: \"Courier New\";\n" + 
				" -fx-base: rgb(132, 145, 47);\n   -fx-background: rgb(240, 240, 240);\n -fx-legend-visible: false; }";

		Node chartPlotArea = controller.getPlotAreaNode();
		if (chartPlotArea == null) return;

		Region rgn = (Region) chartPlotArea;
		rgn.setStyle("-fx-background-color: #FCFCFC;");
//			rgn.setBorder(Borders.thinEtchedBorder);
	    ChangeListener<Number> paneSizeListener = (obs, oldV, newV) -> controller.resized();
	    scatter.widthProperty().addListener(paneSizeListener);
	    scatter.heightProperty().addListener(paneSizeListener);  	

		scatter.setStyle(rootStr);
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
	private Rectangle selectionRectangleScaleDef = new Rectangle(0,0,0,0);	// a representation of the selection relative to the bounds
	private Rectangle selectionRectangle = new Rectangle(0,0,1,1);
	private Rectangle mirrorRectangle = new Rectangle(0,0,1,1);			// a second selection rectangle used in volcano plots

	
	
	private Point2D selRectStart = null;
	private Point2D selRectEnd = null;
	private boolean isRectangleSizeTooSmall() {
		if (selectionRectangle == null) 			return true;
		if (selectionRectangle.getWidth() < 10)		return true;
		if (selectionRectangle.getHeight() < 10) 	return true;
		return false;
	}

	private static final String STYLE_CLASS_SELECTION_BOX = "chart-selection-rectangle";


	public void selectionLayerBuilder() 
	{
		xAxis = (ValueAxis<Number>) scatter.getXAxis();
		yAxis = (ValueAxis<Number>) scatter.getYAxis();
		makeSelectionRectangle();
		addDragSelectionMechanism(controller.getPlotAreaNode());
//		addInfoLabel();

		StackPane.setAlignment(selectionRectangle, Pos.TOP_LEFT);
		StackPane.setAlignment(mirrorRectangle, Pos.TOP_LEFT);
	}

	private void makeSelectionRectangle()
	{

		selectionRectangle = new Rectangle(); 
		selectionRectangle.setOnMouseEntered(	event -> { setCursor(selectionRectangle, event); } );
		selectionRectangle.setOnMouseMoved(		event -> { setCursor(selectionRectangle, event); });
		selectionRectangle.setOnMouseExited(	event -> { setCursor(Cursor.DEFAULT);		});

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
		// this is used for volcano plots to mirror the selection around 0
		mirrorRectangle.setManaged(false);
		mirrorRectangle.setOpacity(0.3);
		mirrorRectangle.setFill(Color.CYAN);
		mirrorRectangle.getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);
		mirrorRectangle.setStroke(Color.WHITE);
		mirrorRectangle.setStrokeWidth(2f);

	}
	// a drag in the chart area, but not in the selection.  creates a selection

	private void onDragged(MouseEvent event) {
		if (event.isSecondaryButtonDown()) 	return;
		boolean option = event.isAltDown();
		if (resizing)
		{
			// store current cursor position
			selRectEnd = computeRectanglePoint(event.getX(), event.getY());
			if (selRectStart == null)
				selRectStart = RectangleUtil.oppositeCorner(event,selectionRectangle);
			if (selRectStart == null) return;
//selRectStart = new Point2D(event.getX(), event.getY());			// ERROR -- will reset instead of resize
			double x = Math.min(selRectStart.getX(), selRectEnd.getX());
			double y = Math.min(selRectStart.getY(), selRectEnd.getY());
			double width = Math.abs(selRectStart.getX() - selRectEnd.getX());
			double height = Math.abs(selRectStart.getY() - selRectEnd.getY());
			drawSelectionRectangle(x, y, width, height, option);
//			System.out.println("x:" + x + " y:" + y + " width:" + width + " height:" + height);
		} else		// dragging
		{
			double oldX = selRectStart.getX();
			double oldY = selRectStart.getY();
			double dx = event.getX() - oldX;
			double dy = event.getY() - oldY;
			Bounds chartPlotArea = controller.getPlotBounds();
			double minAllowedX = chartPlotArea.getMinX() + 12;
			double maxAllowedX = minAllowedX + chartPlotArea.getWidth() + 12;
			double minAllowedY = chartPlotArea.getMinY() + 12;
			double maxAllowedY = minAllowedY + chartPlotArea.getHeight() + 12;

			double newLeft = selectionRectangle.getX() + dx;
			double newRight = newLeft + selectionRectangle.getWidth();
			double newTop = selectionRectangle.getY() + dy;
			double newBottom = newTop + selectionRectangle.getHeight();
			if (dx != 0 && (newLeft < minAllowedX || newRight > maxAllowedX)) return;
			if (newTop < minAllowedY || newBottom > maxAllowedY) return;
//			System.out.println("x:" + oldX + " y:" + oldY);
//			offsetRectangle(selectionRectangle, dx, dy);
			selectionRectangle.setX(selectionRectangle.getX() + dx);	
			selectionRectangle.setY(selectionRectangle.getY() + dy);
			drawSelectionRectangleAt(event.getX() - offsetX, event.getY() - offsetY, option);
			selRectStart = new Point2D(event.getX(), event.getY());
		}
		if (controller.isInteractive()) 
			setAxisBounds();
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
		Node chartPlotArea = controller.getPlotAreaNode() ;
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
			{	
				getChildren().add(selectionRectangle);
				getChildren().add(mirrorRectangle);		// a mirror of the selection
			}
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
			selectionRectangleScaleDef = rectDef(selectionRectangle, getPlotFrame());
			setAxisBounds();
			ev.consume();
		});
		
		chartRegion.setOnMouseReleased(ev -> {
			if (selRectStart == null || selRectEnd == null) 		return;
			if (isRectangleSizeTooSmall()) 							return;
			setAxisBounds();
			selRectStart = selRectEnd = null;
			selectionRectangleScaleDef = rectDef(selectionRectangle, getPlotFrame());
			requestFocus();		// needed for the key event handler to receive events
			ev.consume();
		});
	}

	private Point2D computeRectanglePoint(double eventX, double eventY) {
		double lowerBoundX = computeOffsetInChart(xAxis, false);
		double upperBoundX = lowerBoundX + xAxis.getWidth();
		double lowerBoundY = computeOffsetInChart(yAxis, true);
		double upperBoundY = lowerBoundY + yAxis.getHeight();
		double offsetX = 0;
		double offsetY = 0;
		// make sure the rectangle's end point is in the interval defined by the bounds for each dimension
		double x = Math.max(lowerBoundX, Math.min(eventX +offsetX, upperBoundX));
		double y = Math.max(lowerBoundY, Math.min(eventY +offsetY, upperBoundY));
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
	public void drawSelectionRectangle(Point2D a, Point2D b, boolean optionDrag) 
	{
		double wid = Math.abs(b.getX() - a.getX());
		double hght = Math.abs(b.getY() - a.getY());
		double x = Math.min(a.getX(),b.getX());
		double y = Math.min(a.getY(),b.getY());
		drawSelectionRectangle(x,y,wid,hght, optionDrag);
	}	
	
	public void drawSelectionRectangle(final double x, final double y, final double width, final double height, boolean optionDrag) {
		selectionRectangle.setVisible(true);
		selectionRectangle.setX(x);
		selectionRectangle.setY(y);
		selectionRectangle.setWidth(width);
		selectionRectangle.setHeight(height);
//		selectionRectangle.toFront();
		if (optionDrag)
		{
			Range xRange = controller.getXRange();
			if (xRange.min() < 0 && xRange.max() > 0)
			{
				double xScale = converter.frameToScale(x, scatter, false);
				mirrorRectangle.setVisible(true);
				double ghostX = converter.scaleToFrame(-1 * xScale, scatter, false);
				if (xScale > 0)
					ghostX -= width;

				mirrorRectangle.setX( ghostX);
				mirrorRectangle.setY(y);
				mirrorRectangle.setWidth(width);
				mirrorRectangle.setHeight(height);
	//			ghostRectangle.toFront();
			}
		}
		drawRegressionLine();
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
		String s = found + " / " + total + " in range X: [ " + fmt.format(xMin) + " - " + fmt.format(xMax) + "] Y: [ " + fmt.format(yMin) + " - " + fmt.format(yMax) + "]";
//		infoLabel.setText(s);
//		System.out.println(s);
		xRange = new Range(xMin, xMax);
		yRange = new Range(yMin, yMax);
		controller.setStatus(s, xRange, yRange);	
	}
	
	Range xRange = null, yRange = null; 
	
	//-------------------------------------------------------------------------------
//	boolean verbose = false;
	// user has typed numbers into the min or max fields, so we have to reverse the normal direction of info (view -> model)
	public void resizedRangeFields()
	{
//		Range xRange = controller.getXRange();
//		Range yRange = controller.getYRange();
////		System.out.println("yRange: " + yRange.toString());
//		selectionRectangleScaleDef.setX(xRange.min());
//		selectionRectangleScaleDef.setY(yRange.min());
//		Rectangle frame = getPlotFrame();
//		selectionRectangleScaleDef.setWidth(xRange.width() / frame.getWidth());
//		selectionRectangleScaleDef.setHeight(yRange.width() / frame.getHeight());
//		Rectangle scaler = getScaleRect(selectionRectangleScaleDef, frame);
//		Rectangle r = rectDef(scaler, getPlotFrame());
//		
//		System.out.println(r.getLayoutX() + ", " + r.getLayoutY() + ", " + r.getWidth() + ", " + r.getHeight() );
//		System.out.println(scaler.getLayoutX() + ", " + scaler.getLayoutY() + ", " + scaler.getWidth() + ", " + scaler.getHeight() );
//		
//		drawSelectionRectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight(), false);
	}

	//-------------------------------------------------------------------------------
	public void setAxisBounds() {			// selection rectangle has changed
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
//	public Range getXRange()
//	{
//		if (selRectStart == null || selRectEnd == null) 		return new Range(0,4);
//		double selectionMinX = Math.min(selRectStart.getX(), selRectEnd.getX());
//		double selectionMaxX = Math.max(selRectStart.getX(), selRectEnd.getX());
//		double xMin = converter.frameToScale(selectionMinX, scatter, false);
//		double xMax = converter.frameToScale(selectionMaxX, scatter, false);
//		return new Range(xMin, xMax);
//	}
	
//	public Range getYRange()
//	{
//		if (selRectStart == null || selRectEnd == null) 		return  new Range(0,1);
//		double selectionMinY = Math.min(selRectStart.getY(), selRectEnd.getY());
//		double selectionMaxY = Math.max(selRectStart.getY(), selRectEnd.getY());
//		double yMin = converter.frameToScale(selectionMaxY, scatter, true);
//		double yMax = converter.frameToScale(selectionMinY, scatter, true);
//		return new Range(yMin, yMax);
//	}
	
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
	
	boolean resizing = false;
//	double SLOP = 4;
	double offsetX = 0, offsetY = 0;
	
	//-------------------------------------------------------------------------------
	void setCursor(Rectangle r, MouseEvent event)
	{
		 Pos pos = RectangleUtil.getPos(event, r);
		 r.setCursor(Cursors.getResizeCursor(pos));
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
		Rectangle scaler = new Rectangle(frame.getX() + def.getX() * frameWidth,
				frame.getY() + def.getY() * frameHeight, 
				def.getWidth() * frameWidth, def.getHeight() * frameHeight);
		
		return scaler;
	}

	//-------------------------------------------------------------------------------
	Rectangle rectDef(Rectangle child, Rectangle frame)
	{
		double frameWidth = frame.getWidth();
		double frameHeight = frame.getHeight();
		Rectangle r = new Rectangle((child.getX() - frame.getX()) / frameWidth,
				(child.getY() - frame.getY()) / frameHeight, 
				child.getWidth() / frameWidth, child.getHeight() / frameHeight);
//		if (r.getWidth() > 4)	
//			System.out.println("gotit");
		return r;
	}

	//----------------------------------------------------------------------------
	//----------------------------------------------------------------------------
	//----------------------------------------------------------------------------
	//----------------------------------------------------------------------------

	private Line regressionLine =null;
	private Label regressionLabel=null;
	private double regressionSlope = Double.NaN;
	private double regressionIntercept = Double.NaN;
	private double regressionCorrelation = Double.NaN;
	void setRegression(LinearRegression r )
	{
		regressionSlope = r.slope(); 
		regressionIntercept = r.intercept();
		regressionCorrelation = r.R2();
	}
	void clearRegression()
	{
		if (regressionLine != null) getChildren().remove(regressionLine);
		if (regressionLabel != null) getChildren().remove(regressionLabel);
		regressionSlope =  regressionIntercept = regressionCorrelation = Double.NaN;
		regressionLine = null;
		regressionLabel=null;
	}
	double getSlope() 		{ return regressionSlope; }
	double getIntercept() 	{ return regressionIntercept; }
	double getCorrelation() { return regressionCorrelation; }
	
	
	private void drawRegressionLine() {
		if (!Double.isNaN(regressionSlope))
		{
			double xMin =xAxis.getLowerBound();
			double xMax = xAxis.getUpperBound();
			double yMin =yAxis.getLowerBound();
			double yMax = yAxis.getUpperBound();
			double y1 = xMin * regressionSlope + regressionIntercept;
			double y2 = xMax * regressionSlope + regressionIntercept;
			double x1 = 0;
			double x2 = 0;
			
			if (y1 < yMin)
			{
				x1 = (yMin - regressionIntercept) / regressionSlope;
				y1 = yMin;
			}
			else if (y1 > yMax)
			{
				x1 = (yMax - regressionIntercept) / regressionSlope;
				y1 = yMax;
			}
			else x1 = xMin;
			
			if (y2 < yMin)
			{
				x2 = (yMin - regressionIntercept) / regressionSlope;
				y2 = yMin;
			}
			else if (y2 > yMax)
			{
				x2 = (yMax - regressionIntercept) / regressionSlope;
				y2 = yMax;
			}
			else x2 = xMax;
			
			double startX = converter.scaleToFrame(x1, scatter, false);
			double startY= converter.scaleToFrame(y1, scatter, true);
			double endX = converter.scaleToFrame(x2, scatter, false);
			double  endY= converter.scaleToFrame(y2, scatter, true);
			
			if (regressionLine == null) 
			{
				regressionLine = new Line();
				regressionLabel = new Label();
			}
			else 
			{
				getChildren().remove(regressionLine);
				getChildren().remove(regressionLabel);
			}

			Bounds b = controller.getPlotBounds();
			double left = b.getMinX();		
			double top = b.getMinY();
			
			regressionLine.setStartX(startX + left);
			regressionLine.setStartY(startY + top);
			regressionLine.setEndX(endX + left);
			regressionLine.setEndY(endY + top);
			regressionLine.setOpacity(0.8);
			regressionLine.setStroke(Color.PURPLE);
			regressionLine.setStrokeWidth(4);
			getChildren().add(regressionLine);
			
			String text = String.format("m=%.2f, b=%.2f \n R=%.4f", 
					regressionSlope, regressionIntercept, regressionCorrelation);
			double textWidth = 100;
			double offset = 50;
			regressionLabel.setText(text);
			regressionLabel.setTranslateX(endX + left - textWidth);
			regressionLabel.setTranslateY(endY + top + (regressionSlope > 0 ? offset : -offset));
			regressionLabel.setVisible(true);
			getChildren().add(regressionLabel);
		}
	}



}