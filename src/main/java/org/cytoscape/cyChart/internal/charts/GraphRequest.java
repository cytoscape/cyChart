package org.cytoscape.cyChart.internal.charts;

import java.io.File;

public class GraphRequest extends StatisticRequest
{
	public enum Graph 		{ HISTOGRAM, SCATTER, DENSITY, CONTOUR };

	Graph type;
	File output;
	String x;
	String y = ".";
	String z = ".";
	String[] children;
	
	public GraphRequest(Graph typ, String xDim, String yDim, String mom, String ... kids)
	{
		super(mom);
		x = xDim;
		y = yDim;
		children = kids;
		type = typ;
	}
	
	public String firstChild()	{ return children == null || children.length == 0 ? "" : children[0];	}
	public int nLayers()			{ return children.length;	}
	public String[] getLayers()			{ return children;	}
	String getId()
	{
		return type.toString() + x + y + z + population;
	}
	public String getX()	{ return x;	}
	public String getY()	{ return y;	}
	public String getZ()	{ return z;	}
	public boolean isHistogram() { return type == Graph.HISTOGRAM;	}
	public boolean isScatter() { return type == Graph.SCATTER;	}
	public boolean isDensity() { return type == Graph.DENSITY;	}
	public boolean isContour() { return type == Graph.CONTOUR;	}
}

