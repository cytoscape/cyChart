package org.cytoscape.cyChart.internal.charts.oneD;

import org.cytoscape.cyChart.internal.model.LogarithmicAxis;
import org.cytoscape.cyChart.internal.model.Range;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;

public class FrameScaleConverter {

	/**-------------------------------------------------------------------------------
	 * Because the event handler is attached to the chart's plot area, 
	 * aot the plot or parent panel, the event coordinates come in already 
	 * relative to the plot area, so it removes an offset arg from the normal conversion
	 * 
	 */
	double HACK = 0;
	
	public Range frameToScaleRange(double anchor, double moving, XYChart<Number, Number> theChart)
	{
		double left = Math.min(anchor, moving);
		double right = Math.max(anchor, moving);
		double x0 = frameToScale(left, theChart, false);
		double x1 = frameToScale(right, theChart, false);
		return new Range(x0, x1);
	}
	
	public double frameToScale(double framePosition, XYChart<Number, Number> theChart, boolean isYAxis) {
		framePosition -= HACK;
		Bounds bounds =  getChartPlotBounds(theChart);
		double frameLength = isYAxis ? bounds.getHeight() : bounds.getWidth();
		//https://stackoverflow.com/questions/16268207/why-cant-i-modify-the-axes-from-a-javafx-linechart
		ValueAxis<Number> axis = (ValueAxis<Number>) (isYAxis ? theChart.getYAxis() : theChart.getXAxis()); 
		if (axis instanceof LogarithmicAxis)
		{
			LogarithmicAxis logAxis = (LogarithmicAxis) axis;
			double outScale =  logAxis.getValueForDisplay(framePosition).doubleValue();
			return outScale;
		}
		
		else
		{
		double scaleMin = axis.getLowerBound();
		double scaleMax = axis.getUpperBound();
		double relativePosition = framePosition / frameLength;
		double scaleLength = scaleMax - scaleMin;

		// The screen's y axis grows from top to bottom, whereas the chart's y axis goes from bottom to top.
		// That's why we need to have this distinction here.
		double offset = scaleMin;
		int sign = 0;
		if (isYAxis) 		{	offset = scaleMax;	sign = -1;	} 
		else 				{	offset = scaleMin;	sign = 1;	}

		double outScale = offset + sign * relativePosition * scaleLength;
		boolean VERBOSE = false;
		if (VERBOSE) 
		{
			String axisName = isYAxis ? "Y" : "X";
			System.out.println(String.format("%s AxisBounds:[%.1f, - %.1f] FrameLength:[%.1f] %.1f -> %.2f", axisName, scaleMin, scaleMax, frameLength, framePosition, outScale));
		}
		return outScale;
		}
	}


	/**-------------------------------------------------------------------------------
	 * assumes a 0 based left coordinate for the transformation, which comes from 
	 * theChart.lookup(".chart-plot-background")
	 */
double FUDGE = 0;
	public double scaleToFrame(double scaleValue, XYChart<Number, Number> theChart, boolean isYAxis) {
		scaleValue += FUDGE;
		ValueAxis<Number> axis = (ValueAxis<Number>) (isYAxis ? theChart.getYAxis() : theChart.getXAxis()); 
		if (axis instanceof LogarithmicAxis)
		{
			LogarithmicAxis logAxis = (LogarithmicAxis) axis;
			double outFrame =  logAxis.getDisplayPosition(scaleValue);
			return outFrame;
		}
		else
		{
			Bounds bounds =  getChartPlotBounds(theChart);
			double widthOrHeight = isYAxis ? bounds.getHeight() : bounds.getWidth();
			double lower = axis.getLowerBound();
			double upper = axis.getUpperBound();
			double relativePosition = 	(scaleValue - lower) / (upper - lower);
			if (isYAxis) relativePosition = 1 - relativePosition;
			double frameVal = relativePosition * widthOrHeight;
			boolean VERBOSE = false;
			if (VERBOSE) 
			{
				String axisName = isYAxis ? "Y" : "X";
				System.out.println(String.format("scaleToFrame: dim: %s AxisBounds:[%.1f, - %.1f] FrameLength:[%.1f] %.1f <- %.2f", axisName, lower,upper, widthOrHeight, frameVal, scaleValue));
			}
			return frameVal;
		}
	}
	
	public Point2D scaleToFrame(Point2D pt,  XYChart<Number, Number> theChart)
	{
		Point2D offset = getChartOffset(theChart);
		double x = scaleToFrame(pt.getX(), theChart, false) + offset.getX() + 5;
		double y = scaleToFrame(pt.getY(), theChart, true) +  + offset.getY() + 5;
		return new Point2D(x,y);
	}
	
	public Bounds getChartPlotBounds(XYChart<Number, Number> theChart) {
		Node chartPlotArea = theChart.lookup(".chart-plot-background");
		return (chartPlotArea == null) ? null : chartPlotArea.getLayoutBounds();
	}

	public Point2D getChartOffset(XYChart<Number, Number> theChart) {
		Node chartPlotArea = theChart.lookup(".chart-plot-background");
		return (chartPlotArea == null) ? null : new Point2D(chartPlotArea.getLayoutX(), chartPlotArea.getLayoutY());
	}

}
