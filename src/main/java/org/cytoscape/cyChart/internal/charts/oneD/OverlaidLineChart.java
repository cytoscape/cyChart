package org.cytoscape.cyChart.internal.charts.oneD;

import java.util.Objects;

import org.cytoscape.cyChart.internal.model.Peak;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Pair;

public class OverlaidLineChart extends LineChart<Number,Number>
{
	   public OverlaidLineChart(Axis<Number> xAxis, Axis<Number> yAxis) {
	       super(xAxis, yAxis);
	   }


	   /**
	    * Overridden to layout the value markers.
	    */
	   @Override
	   protected void layoutPlotChildren() {
	       super.layoutPlotChildren();

	     }
   

}
