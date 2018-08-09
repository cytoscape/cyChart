package org.cytoscape.cyChart.internal;

import org.cytoscape.cyChart.internal.charts.Range;

public class FilterBuilder {
	String xColumnName;
	Range xRange;
	String yColumnName;
	Range yRange;

	public FilterBuilder(String columnName, Range r)
	{
		xColumnName = columnName;
		xRange = r;
	}
	public FilterBuilder(String xcolumn, Range xRange, String yCol, Range inYRange)
	{
		this(xcolumn, xRange);
		yColumnName = yCol;
		yRange = inYRange;
	}
	
	public String makeString(boolean isX)
	{
		String criterion, columnName;
		if (isX)
		{
			criterion = String.format("[ %.4f, %.4f ]", xRange.min(), xRange.max());
			columnName = xColumnName;
		}
		else
		{
			criterion = String.format("[ %.4f, %.4f ]", yRange.min(), yRange.max());
			columnName = yColumnName;
		}
		StringBuilder buildr = new StringBuilder();
		buildr.append("{\n");
		buildr.append("\"id\" : \"ColumnFilter\", \n" );
		buildr.append("\"parameters\" : {\n");
		addLine(buildr,"predicate", "\"BETWEEN\"", true );
		addLine(buildr,"criterion", criterion,true );
		addLine(buildr,"caseSensitive", "false",true );
		addLine(buildr,"type", "\"nodes\"",true );
		addLine(buildr,"anyMatch", "true",true );
		addLine(buildr,"columnName", inQuotes(columnName), false );
		buildr.append("\n }\n}");
		return buildr.toString();
	 }
			
	private String inQuotes(String a) {		return '"' + a + '"';	}

		
	void addLine(StringBuilder b, String attr, String value, boolean addComma)
	{
		b.append(inQuotes(attr) + " : " + value  );
		if (addComma)  b.append(",\n");
	}
	
	public String makeComposite() {
		
		String filter1 = makeString(true);
		String filter2 = makeString(false);
		StringBuilder compos = new StringBuilder();
		compos.append("{\n\"id\" : \"CompositeFilter\",\n");
		compos.append("\"parameters\" : {\n \"type\" : \"ALL\"\n},\n");
		compos.append("\"transformers\" : [ \n");
		compos.append(filter1 + ", \n" + filter2);
		compos.append("] \n}\n");		
		return compos.toString();
	}
		
}
