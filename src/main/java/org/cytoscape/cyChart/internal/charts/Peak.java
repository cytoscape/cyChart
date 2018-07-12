package org.cytoscape.cyChart.internal.charts;

import org.cytoscape.cyChart.internal.charts.oneD.Histogram1D;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class Peak implements Comparable<Peak>
{
	private double mean;
	private double stdev;
	private double amplitude;
	private double area;
	private double min;
	private double max;
	private Histogram1D histogram;
	private XYChart<Number, Number> chart;
	NumberAxis xAxis;
	NumberAxis yAxis;
	private Path path;
	public Peak()
	{
		this(1, 1, 1);
	}

	public Peak(double m, double s, double a)
	{
		mean = m;
		stdev = s;
		amplitude = a;
		area = 0;
		min = 0;
		max = 1;
		path = null;
	}

	public Peak(Histogram1D h, XYChart<Number, Number> c)
	{
		this();
		histogram = h;
		setChart(c);
		path = new Path();
	}
	//----------------------------------------------------------------------------------

	public void setChart(XYChart<Number, Number> c)
	{
		chart = c;
		if (c != null) 
		{
			xAxis = (NumberAxis)chart.getXAxis();
			yAxis = (NumberAxis)chart.getYAxis();
		}
	}

	public String toString()
	{
		return String.format("[ %.0f - %.0f ] %.2f @ %.2f, area: %.2f", min, max, amplitude, mean, area);
	}

//@formatter:off
	@Override public int compareTo(Peak x)	{		return x.mean > mean ? -1 : 1;	}
 
	public double getMean()  		{ 	return mean;	}
	public void setMean(double d)  	{  	mean = d;	}
	public double getStdev()  		{ 	return stdev;	}
	public void setStdev(double d)  {  	stdev = d;	}
	public double getAmplitude()  	{ 	return amplitude;	}
	public double getUnitAmplitude()  	{ 	return (amplitude) / histogram.getArea();	}
	public void setAmplitude(double d)  {  amplitude = d;	}

	public Histogram1D getHistogram()  {  return histogram;	}
	public void setHistogram(Histogram1D h)  {  histogram = h;	}
	
	public void setBounds(double a, double b)  {  min = a; max = b;	}
	public double getWidth()	{ return max - min;	}
	public double getMin()  		{ 	return  min;	}
	public double getMax()  		{ 	return  max;	}
	public double getArea()			{  	return area; }
	public void setArea(double a)	{   area = a; }
	public void addArea(double a)	{   area += a; }
	public double get(double x)		{	return gauss(x, mean, stdev, getUnitAmplitude());	}
	public double getCV()			{	return stdev / mean;	}
//@formatter:on
	//----------------------------------------------------------------------------------
	public void calcStdev()
	{
		double width = 1 + max - min;
		amplitude = area / width;
		double var = 0;
		for (int i = (int) min; i < max; i++)
		{
			double d = amplitude - histogram.get(i);
			var += (d * d);
		}
		stdev = Math.sqrt(var / width);
	}

	public static double gauss(double x, double mn, double sd, double amp)
	{
		double exp = -((x - mn) * (x - mn)) / (2 * sd * sd);
		double denom = sd * Math.sqrt(2 * Math.PI);
		double val = amp * Math.exp(exp) / denom ;
		// System.out.println(String.format("%.2f", val));
		return val;
	}

	//----------------------------------------------------------------------------------
	public Path getPath()
	{
		if (chart != null)		//path == null &&   always rebuild!
		{
			path.getElements().clear();
			double startBin = getMin();
			double endBin = getMax();

			double lowery = ((NumberAxis) yAxis).getLowerBound();
			double uppery = ((NumberAxis) yAxis).getUpperBound();

			Histogram1D histo = getHistogram();
			double y0 = yAxis.getDisplayPosition(yAxis.toRealValue(lowery));
			Point2D left = new Point2D(xAxis.getDisplayPosition(histo.binToVal((int) startBin)), y0);
			Point2D right = new Point2D(xAxis.getDisplayPosition(histo.binToVal((int) endBin)), y0);

			int meanBin = (int) getMean();
			double x = histo.binToVal(meanBin);
			double h = xAxis.getDisplayPosition(x);
			double y = histo.getValue(meanBin) / histo.getArea();	//2.5 * curve.getUnitAmplitude();			// HACK
			double v = yAxis.getDisplayPosition(y);
			
			double altV = get(meanBin);
			double altV2 = yAxis.getDisplayPosition(altV);
			
			Point2D center = new Point2D(h, v);
			// fill 7 points around the center, in the order: 4, 2, 1, 3, 6, 5, 7
			
//			int x2Bin = (int) ((startBin + meanBin) / 2);
//			Point2D x2Pt = getPoint(x2Bin, xAxis, yAxis);
//			
//			int x1Bin = (int) ((startBin + x2Bin )/ 2);
//			Point2D x1Pt = getPoint(x1Bin, xAxis, yAxis);
//
//			int x3Bin = (int) ((meanBin + x2Bin)/ 2);
//			Point2D x3Pt = getPoint(x3Bin, xAxis, yAxis);
//			
//			int x6Bin= (int) ((endBin + meanBin) / 2);
//			Point2D x6Pt = getPoint(x6Bin, xAxis, yAxis);
//	
//			int x5Bin = (int) ((meanBin + x6Bin) / 2);
//			Point2D x5Pt = getPoint(x5Bin, xAxis, yAxis);
//
//			int x7Bin = (int) ((endBin + x6Bin) / 2);
//			Point2D x7Pt = getPoint(x7Bin, xAxis, yAxis);

			boolean showRightEdge = true;
			boolean showLefEdge = true;

//			Point2D[] dataPoints = new Point2D[] { left, x1Pt, x2Pt, x3Pt, center, x5Pt, x6Pt, x7Pt, right };
			Point2D[] dataPoints = new Point2D[] { left,   center,   right };
			path.setStroke(Color.BLUE);
			if (showLefEdge) 
				path.getElements().addAll(new MoveTo(left.getX(), y0), new LineTo(left.getX(), uppery)); 

			path.getElements().add(new MoveTo(left.getX(), left.getY()));
			for (Point2D point : dataPoints)
			{
				path.getElements().add(new LineTo(point.getX(), point.getY()));
			}
		    if (showRightEdge)
		    	path.getElements().addAll(new MoveTo(right.getX(), y0), new LineTo(right.getX(), uppery));
		}
		return path;
	}

	private Point2D getPoint(int xBin, NumberAxis xAxis, NumberAxis yAxis)
	{
		double x = histogram.binToVal(xBin);
		double xPosition = xAxis.getDisplayPosition(x);
		double yVal = get(xBin);   // histo.getValue(xNegBin) / histo.getArea();				// usually near 0
		double yPosition= yAxis.getDisplayPosition(yVal);
		return new Point2D(xPosition, yPosition);
	}

	public void setNode(Node n)	{		if (n instanceof Path) path = (Path) n;	}
}
