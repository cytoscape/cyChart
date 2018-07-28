package org.cytoscape.cyChart.internal.charts.oneD;

import javafx.geometry.Bounds;
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
	
	public double frameToScale(double framePosition, XYChart<Number, Number> theChart, boolean isYAxis) {
		Bounds bounds =  getChartPlotBounds(theChart);
		double frameLength = isYAxis ? bounds.getHeight() : bounds.getWidth();
		//https://stackoverflow.com/questions/16268207/why-cant-i-modify-the-axes-from-a-javafx-linechart
		ValueAxis<Number> axis = (ValueAxis<Number>) (isYAxis ? theChart.getYAxis() : theChart.getXAxis()); 
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


	/**-------------------------------------------------------------------------------
	 * assumes a 0 based left coordinate for the transformation, which comes from 
	 * theChart.lookup(".chart-plot-background")
	 */

	public double scaleToFrame(double scaleValue, XYChart<Number, Number> theChart, boolean isYAxis) {
		ValueAxis<Number> axis = (ValueAxis<Number>) (isYAxis ? theChart.getYAxis() : theChart.getXAxis()); 
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
	
	
	public Bounds getChartPlotBounds(XYChart<Number, Number> theChart) {
		Node chartPlotArea = theChart.lookup(".chart-plot-background");
		return (chartPlotArea == null) ? null : chartPlotArea.getLayoutBounds();
	}

}
