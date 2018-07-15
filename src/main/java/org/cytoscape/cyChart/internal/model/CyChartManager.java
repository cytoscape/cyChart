package org.cytoscape.cyChart.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyChart.internal.charts.oneD.Histogram1D;
import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.geometry.Point2D;

public class CyChartManager {

	private CyServiceRegistrar registrar;
	private Map<String, CyChart> idMap = new HashMap<String, CyChart>();
	private String version = "unknown";
	private static int chartCount = 1;

	public CyChartManager(CyServiceRegistrar reg) {
		registrar = reg;
		CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
	}

	public CyServiceRegistrar getRegistrar() {		return registrar;	}

	public  CyChart getChart(String id) {
		if (id == null) id = "";
		// System.out.println("Retrieving browser with id: '"+id+"'");
		if (idMap.containsKey(id)){
			// System.out.println("Found browser: "+idMap.get(id)+" with id: '"+id+"'");
			return idMap.get(id);
		}
		return null;
	}

	CyNetwork getCurrentNetwork() 		{	return registrar.getService(CyApplicationManager.class).getCurrentNetwork();	}
	CyTable getNodeTable(CyNetwork net) {	return net.getDefaultNodeTable();	}
	
	Map<Long, Double> getColumnDoubleMap(CyTable table, String colName)
	{
		Map<Long, Double> map = new HashMap<Long, Double> ();
		CyColumn col = table.getColumn(colName);
		if (col != null)
		{
			List<Double> vals = null;
			List<Long> ids = null;
			vals = col.getValues(Double.class);
			ids = col.getValues(Long.class);
			int sz = vals.size();
			for (int i=0; i< sz; i++)
				map.put(ids.get(i), vals.get(i));
		}
		return map;
	}
	
	Map<Long, Point2D> getColumnPointMap(CyTable table, String xColName, String yColName)
	{
		Map<Long, Point2D> map = new HashMap<Long, Point2D> ();
		CyColumn xcol = table.getColumn(xColName);
		CyColumn ycol = table.getColumn(yColName);
		CyColumn idcol = table.getColumn("SUID");
		if (xcol != null && ycol != null)
		{
			List<Long> ids = null;
			List<Double> xvals = xcol.getValues(Double.class);
			List<Double> yvals = xcol.getValues(Double.class);
			ids = idcol.getValues(Long.class);
			int sz = xvals.size();
			for (int i=0; i< sz; i++)
				map.put(ids.get(i), new Point2D(xvals.get(i), yvals.get(i)));
		}
		return map;
	}
	
		
	
	Histogram1D getHistogram(CyNetwork network, String column)
	{
		return null;
	}
	
	public String makeId() {
		String id = "CyChart "+chartCount;
		chartCount++;
		return id;
	}

	public void addChart(CyChart browser, String id) {
		if (id == null) {
			id = "CyChart "+chartCount;
			chartCount++;
		}
		// System.out.println("Adding browser: "+browser+" with id: '"+id+"'");
		idMap.put(id, browser);
	}

	public void removeChart(String id) {
		if (id == null) id = "";
		// System.out.println("Removing browser: '"+id+"'");
		idMap.remove(id);
	}

	public void removeChart(CyChart chart) {
		List<String> ids = new ArrayList<String>(idMap.keySet());
		for (String id: ids) {
			if (idMap.get(id).equals(chart))
				removeChart(id);
		}
	}

	public void closeChart(String id) {
		CyChart chart = getChart(id);
		if (chart == null) {
			System.out.println("Unable to get chart for id: '"+id+"'");
			return;
		}

		SwingUtilities.invokeLater( new Runnable() {
			@Override public void run() {	if (chart instanceof HistogramFilterDialog)	((HistogramFilterDialog)chart).dispose();	}	
		});
			
		removeChart(id);
	}

	public Map<String, CyChart> getChartMap() {		return idMap;	}
	public void setVersion(String v) { this.version = v; }
	public String getVersion() { return version; }
}
