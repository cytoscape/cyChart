package org.cytoscape.cyChart.internal.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.cyChart.internal.charts.StringUtil;
import org.cytoscape.cyChart.internal.charts.oneD.Histogram1D;
import org.cytoscape.cyChart.internal.charts.oneD.OverlaidLineChart;

import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CSVTableData
{
	private String name;
	private List<StringUtil.TYPES> types;
	private List< String> columnNames;
	private List<MixedDataRow> rows;
	private List<Range> ranges;
	private Map<String, Histogram1D> histograms;
	private Map<String, Map<String, Histogram1D>> gatedHistogramMap;
//	private List<Histogram2D> histogram2Ds;
//	private List<OverlaidScatterChart<Number, Number>> scatters;
//	private Map<String, Image> images;
//	private Map<String, Integer> gateNames = new HashMap<String, Integer>();
//	ScatterChart<Number, Number> scatter;
	//--------------------------------------------------------------------------------
	
	
	public CSVTableData(String id)
	{
		name = id;
		types = new ArrayList<StringUtil.TYPES>();
		columnNames = FXCollections.observableArrayList();
		rows = new ArrayList<MixedDataRow>();
		ranges = new ArrayList<Range>();
		histograms = new HashMap<String,Histogram1D>();
//		histogram2Ds = new ArrayList<Histogram2D>();
//		scatters = new ArrayList<OverlaidScatterChart<Number, Number>>();
//		images = new HashMap<String, Image>();
//		gatedHistogramMap = new HashMap<String, Map<String, Histogram1D>>();
	}
	//--------------------------------------------------------------------------------
	static final String TAB = "\t";
	static final String COMMA = ",";
	static public  CSVTableData readCSVfile(String path)
	{
		CSVTableData tableData = new CSVTableData(path);
		
		int lineCt = 0;
		try
		{
			FileInputStream fis = new FileInputStream(new File(path));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			
			line = br.readLine();		// first line is text labels, but not in columns
			String[] columns = line.split(COMMA); 
			String[] strs = line.split(COMMA);
			for (int i=0; i<strs.length; i++)
				strs[i] = StringUtil.stripQuotes(strs[i]);
			tableData.setColumnNames(Arrays.asList(strs));
	
			int len = columns.length;
			line = br.readLine();

			while (line != null) {
				String[] row = line.split(COMMA);  
				if (row.length != len)	throw new IllegalArgumentException();		// there must be the same number of fields in every row
				MixedDataRow dataRow = new MixedDataRow(row.length); 
				for (int i = 0; i< row.length; i++)
				{
					String txt = StringUtil.stripQuotes(row[i]);
					dataRow.setString(i, txt);
					if (StringUtil.isNumber(txt))
						dataRow.set(i, StringUtil.toDouble(txt));
				}
				tableData.getData().add(dataRow);
				line = br.readLine();
				lineCt++;
			}
		 
			br.close();
		}
		catch (NumberFormatException e)		{ e.printStackTrace();	return null; 	}
		catch (IllegalArgumentException e)	{ e.printStackTrace();	return null; 	}
		catch (FileNotFoundException e)		{ e.printStackTrace();	return null; 	}
		catch (IOException e)				{ e.printStackTrace();	return null; 	}
//		System.out.println( lineCt + " lines");
		
		tableData.calculateRanges();
		tableData.generateHistograms();			////	just building a unit file here.  Segment.java has the full code 
//		tableData.calculateStats();
//		System.out.println(tableData.getName() + " has row count: " + tableData.getCount());
		return tableData;
	}

	//--------------------------------------------------------------------------------
	public void clear()  {
		types.clear();
		columnNames.clear();
		rows.clear();
		ranges.clear();
		histograms.clear();		
//		scatters.clear();		
//		images.clear();		
	}
	//--------------------------------------------------------------------------------
	
	public int nRows()			{ return rows.size(); }
	public int nColumns()		{ return columnNames.size(); }

	private int getIndexByStart(String name)			
	{
		for (int i= 0; i< columnNames.size(); i++)
			if (columnNames.get(i).startsWith(name))
				return i;
		return -1;
	}
	private String getIndex(int i)			{ return columnNames.get(i); }
//	private int gateIndex(String name)			{ return (name == null) ? -1 : gateNames.get(name); }
	public  String getName() 					{ return name; }
	public  List<StringUtil.TYPES> getTypes() 	{ return types; }
	public  List<Range> getRanges() 				{ return ranges; }
//	public  Map<String,Image> getImages() 		{ return images; }
	public  Range getRange(int i) 				{ return ranges.get(i); }
	public  Map<String,Histogram1D> getHistograms() 	{ return histograms; }
	public  Histogram1D getHistogram(String name) 
	{ 
		if (histograms.isEmpty()) 
			generateHistograms(); 
		return histograms.get(name); 
	}
	
	public  Map<String,Histogram1D> getGatedHistograms(String popname) 	
	{ 	 return ("All".equals(popname) || "^".equals(popname)) ? histograms : gatedHistogramMap.get(popname);
	}

	public  List<String> getColumnNames() 	{ 	return columnNames; }
	public  List<String> getNumericColumnNames() 	
	{
		List<String> cols = new ArrayList<String>();
		for (int i=0; i<types.size(); i++)
			if (StringUtil.isNumber(types.get(i)))
				cols.add(columnNames.get(i));
		return cols; 
	}
	public 	int getCount()					{ 	return rows.size();	}
	public 	int getWidth()					{	return (rows.size() == 0) ? 0 : rows.get(0).getWidth();	}
	public  List<MixedDataRow> getData() 	{ 	return rows; }
	public  MixedDataRow getDataRow(int i) { 	return rows.get(i); }

	public  void  setTypes(List<StringUtil.TYPES> t) { types = t; }
	public  void  setColumnNames(List<String> c) { for (String s : c) columnNames.add(s); }
	public  void  addColumnName(String n) 		{ columnNames.add(n); }
	public  void  setData(List<MixedDataRow> d) {  rows = d; }
	

//	public  List<OverlaidScatterChart> getScatters() 
//	{ 
//		if (scatters.isEmpty()) generateScatters(); 
//		return scatters; 
//	}
//	public void clearScatters()				{	scatters.clear();		}		// force regeneration
	
	//--------------------------------------------------------------------------------
	public void calculateRanges()
	{
		if (!ranges.isEmpty())	return;
		int nRows = rows.size() - 1;	
		if (nRows <= 0) return ;
		int nCols = getWidth();
	
		double[] mins = new double[nCols];
		double[] maxs = new double[nCols];
		for (int i=0;i<nCols;i++)
		{
			mins[i] = Double.MAX_VALUE;
			maxs[i] = Double.MIN_VALUE;
		}
		for (int row=0; row < nRows; row++)		// scan for ranges of all columns
		{
			MixedDataRow aRow = rows.get(row);
			for (int i=0;i<nCols;i++)
			{
				Double s = aRow.get(i).get();
//				if (s <= 0) continue;	// ONLY POSITIVE NUMBERS ALLOWED
//				{
//					System.out.println("STOP");
//					break;
//				}
				mins[i] = Math.min(mins[i],  s);
				maxs[i] = Math.max(maxs[i],  s);
			}
		}
		
		for (int i=0;i<nCols;i++)
		{
			Range r = mins[i] <  maxs[i] ? new Range(mins[i], maxs[i]) : null;
			ranges.add(r);
//			System.out.println("Range for " + columnNames.get(i) + " is " + r.toString());
		}
	}
	//--------------------------------------------------------------------------------
	public void generateHistograms()
	{
		if (!histograms.isEmpty())	return;
		
		int nRows = rows.size() - 1;	
		if (nRows <= 0) return ;
//		IntegerDataRow row0 = rows.get(0);
		int nCols = columnNames.size();
		Histogram1D hist = null;
		for (int i=0;i<nCols; i++)
		{
			Range r = ranges.get(i);
			if (r != null)
			{	
				hist = new Histogram1D(columnNames.get(i) , ranges.get(i));
				for (int row=0; row<nRows; row++)	
				{
					MixedDataRow aRow = rows.get(row);
					Double s = aRow.get(i).get();
					hist.count(s);
				}
			}
			if (hist != null)
			{	
				histograms.put(hist.getName(), hist);
				System.out.println(hist.getName() +  " has area: " + hist.getArea() + " / " + rows.size()); 
			}
		}
	}
	//--------------------------------------------------------------------------------
//	public void generateGatedHistograms(String popName)
//	{
//		Map<String, Histogram1D> gatedHistograms = new HashMap<String, Histogram1D>();
//		int index = gateIndex(popName);
//		if (index >= 0)
//		{
////			IntegerDataRow row0 = rows.get(0);
//			for (int i=3;i<columnNames.size(); i++)
//			{
//				Histogram1D hist = new Histogram1D(columnNames.get(i) , ranges.get(i));
//				for (MixedDataRow aRow : rows)		
//				{
//					Double gate = aRow.get(index).get();
//					if (gate == 1)
//					{
//						Double val = aRow.get(i).get();
//						hist.count(val);	
//					}
//				}
//				gatedHistograms.put(hist.getName(), hist);
//				System.out.println(hist.getName() +  " has area: " + hist.getArea() + " / " + rows.size()); 
//			}
//			gatedHistogramMap.put(popName, gatedHistograms);
//		}
//	}
	//--------------------------------------------------------------------------------
	public Histogram1D getGatedHistogram(GraphRequest req)
	{
		return getGatedHistogram(req.getX(), req.getPopulation());
	}
	public Histogram1D getGatedHistogram(String dimName, String popName)
	{
		Map<String, Histogram1D> gatedHistograms = gatedHistogramMap.get(popName);
		if (gatedHistograms == null) 
			gatedHistograms = histograms;

		if (gatedHistograms == null) return null;
		return gatedHistograms.get(dimName);
	}

	//--------------------------------------------------------------------------------
	public List<Point2D> getPointList(String popName, String xDim, String yDim)
	{
		List<Point2D> pointList = new ArrayList<Point2D>();
		int index = 0;  //gateIndex(popName);
		if (index >= 0)
		{
			int xIdx = indexOf(xDim);
			int yIdx = indexOf(yDim);
			for (MixedDataRow aRow : rows)		
			{
				Double gate = aRow.get(index).get();
				if (gate == 1)
				{
					Point2D pt = new Point2D(aRow.get(xIdx).doubleValue(), aRow.get(yIdx).doubleValue());
					pointList.add(pt);
				}
			}
		}
//		System.out.println( "There are " + pointList.size() + " points in " + popName); 
		return pointList;
	}

	int xIndex = 0;
	int yIndex = 1; 
	int nDimensions = 8;
	
	int indexOf(String s)
	{
		if (s == null) return -1;
		for (int i=0; i< columnNames.size(); i++)
			if (s.equals(columnNames.get(i)))	return i;
		return -1;
	}

	//--------------------------------------------------------------------------------
	public void calculateStats()
	{
		int nCols = getWidth();
		for (int i=0;i<nCols;i++)
		{
			String status = columnNames.get(i) + " has range: " + ranges.get(i);
			Histogram1D h =  histograms.get(i);
			if (h == null) continue;
			double area = h.getArea(); 
			double gutter = h.getGutterCount(); 
			status += " Gutter: " + (int) gutter + " / " + (int) area;
//			System.out.println(status);
		}
	}


	//--------------------------------------------------------------------------------
	public void populateCSVTable(TableView<MixedDataRow> tableview)
	{
		tableview.getColumns().clear();
		tableview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		int idx = 0;
		for (String name : getColumnNames())
		{
            TableColumn<MixedDataRow, Double> newColumn = new TableColumn<MixedDataRow, Double>(name);     
            final int j = idx++;
            newColumn.setCellValueFactory(cellData -> cellData.getValue().get(j).asObject());
			tableview.getColumns().add(newColumn);
		}
		tableview.getItems().clear();
		int nCols = tableview.getColumns().size();

		int nRows = getData().size();  
		
//		System.out.println("table is " + nRows + " rows of " + nCols);
		for (int row=0; row<nRows; row++)
		{
			MixedDataRow newRow = new MixedDataRow(nCols);
			newRow.setRowNum(row);
			for (int i=1;i<nCols;i++)
			{
				Double k = getDataRow(row).get(i-1).get();
				newRow.set(i-1, k);
			}
			tableview.getItems().add(newRow);
//			tableview.getItems().add(getDataRow(row));
		}	
	}
	//--------------------------------------------------------------------------------
	public void makeGatedHistogramOverlay(Histogram1D histo, 
					OverlaidLineChart peakFitChart, double offsetIncrement, String ... pops)
	{
		if (peakFitChart != null && histo != null) 
		{
			double yOffset = 0;
			String histoName = histo.getName();
			double area = histo.getArea();
			for (String popName : pops)
			{
				yOffset += offsetIncrement;
				Histogram1D gatedHistogram = getGatedHistogram(histoName, popName);
				if (gatedHistogram != null)
					peakFitChart.getData().add( gatedHistogram.getDataSeries(popName, yOffset, area));	
			}
		}
	}


//	//--------------------------------------------------------------------------------
//	public OverlaidLineChart showGatedHistogram(GraphRequest req)
//	{
//		return showGatedHistogram(req.getPopulation(), req.firstChild(), req.getX());
//	}
//	
//	public OverlaidLineChart showGatedHistogram(String parent, String child, String dim)
//	{
//		if (StringUtil.isEmpty(parent))	parent = "^";
//		Histogram1D parentHisto = getGatedHistogram(dim, parent);
//		Histogram1D childHisto = getGatedHistogram(dim, child );
//		if (parentHisto == null || childHisto == null)		return null;
//		System.out.println(parentHisto.getName() + " area: " + parentHisto.getArea());
//		System.out.println(childHisto.getName() + " area: " + childHisto.getArea());
//		if (parentHisto.getArea() == 0) System.out.println("no parent events");
//		else System.out.println("Freq of Parent: " + (int)(100 * childHisto.getArea() / (double) parentHisto.getArea()) + "%");
//		
//		NumberAxis  xAxis = new NumberAxis();	
//		xAxis.setLabel(dim);
//		NumberAxis  yAxis = new NumberAxis();
//		OverlaidLineChart  chart = new OverlaidLineChart(xAxis, yAxis);
//		chart.setTitle(child + " Definition");
//		chart.setCreateSymbols(false);
//		chart.getData().add( parentHisto.getDataSeries(parent, 0, parentHisto.getArea()));	
//		chart.getData().add( childHisto.getDataSeries(child, 0, parentHisto.getArea()));		// scale child to parent area -- not working!!
//		chart.setLegendVisible(false);
//		chart.getXAxis().setTickLabelsVisible(true);		// use CSS
//		chart.getYAxis().setTickLabelsVisible(true);
////		chart.setPrefHeight(100);
//		VBox.setVgrow(chart, Priority.ALWAYS);
//		chart.setId(parent + "/" + child);
//		return chart;
//	}
	//--------------------------------------------------------------------------------
//	public void generateRawHistogramCharts(VBox graphVBox)
//	{
//		Map<String, Histogram1D> histos = getHistograms(); 
//		if (histos == null) return;
//		for (String key : histos.keySet())
//		{
//			Histogram1D histo = histos.get(key);
//			if (histo == null) continue;		// first 5 are null
//			Range r = histo.getRange();
//			if (r.width() < 50) continue;
//			LineChart<Number, Number> chart = histo.makeChart();
//			graphVBox.getChildren().add(chart);
////			chart.getXAxis().setTickLabelsVisible(false);		// use CSS
//			chart.getYAxis().setTickLabelsVisible(false);
////			chart.getXAxis().setVisible(false);
//			VBox.setVgrow(chart, Priority.ALWAYS);
//		}
//	}
	//--------------------------------------------------------------------------------
//	int DOTSIZE = 1;
	public XYChart.Series<Number, Number> generateData(String xParm, String yParm)
	{
        List<Double> xVals = getColumnValues(xParm);
        List<Double> yVals = getColumnValues(yParm);
        if (xVals == null || yVals == null) return null;
        XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
        series1.setName(xParm + " / " + yParm);
        List<Data<Number, Number>> dataList = series1.getData();
        int nRows = xVals.size();
        for (int i=0; i<nRows; i++)
        {
        	XYChart.Data<Number, Number> dataVal = new XYChart.Data<Number, Number>(xVals.get(i), yVals.get(i));
            dataList.add(dataVal);
        }
//        chart.getData().add(series1);
//        for (XYChart.Data<Number, Number> dataVal : dataList) {		must be done to data series after its added to the chart
//        	StackPane stackPane =  (StackPane) dataVal.getNode();
//        	if (stackPane != null)
//        	{
//        		stackPane.setPrefWidth(DOTSIZE);
//        		stackPane.setPrefHeight(DOTSIZE);
//        	}
//        }
        return series1;
	}

	int findColumn(String col)
	{
		if (col != null)
			for (int i=0; i<columnNames.size(); i++)
				if (col.equals(columnNames.get(i)))
					return i;
		return -1;
	}
	private List<Double> getColumnValues(String parm) {
		int index = findColumn(parm);
		if (index < 0) return null;
		List<Double > values = new ArrayList<Double>();
		List<MixedDataRow> rows = getData();
		for (MixedDataRow row : rows)
			values.add(row.get(index).doubleValue());
		return values;
	}
	

}
