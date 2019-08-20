package org.cytoscape.cyChart.internal.model;

import java.awt.Frame;
import java.awt.Window;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

public class CyChartManager {

	private CyServiceRegistrar registrar;
//	private Map<String, CyChart> idMap = new HashMap<String, CyChart>();
	private String version = "unknown";
//	private static int chartCount = 1;

	public CyChartManager(CyServiceRegistrar reg) {
		registrar = reg;
//		CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
	}

	public CyServiceRegistrar getRegistrar() {		return registrar;	}

//	public  CyChart getChart(String id) {
//		if (id == null) id = "";
//		// System.out.println("Retrieving browser with id: '"+id+"'");
//		if (idMap.containsKey(id)){
//			// System.out.println("Found browser: "+idMap.get(id)+" with id: '"+id+"'");
//			return idMap.get(id);
//		}
//		return null;
//	}

	public CyNetwork getCurrentNetwork() 		
	{	
		if (registrar == null) return null;
		CyApplicationManager appMgr = registrar.getService(CyApplicationManager.class);
		if (appMgr == null) return null;
		return appMgr.getCurrentNetwork();	
	}
	
	public CyTable getNodeTable(CyNetwork net) 
	{	
		if (net == null) return null;
		return net.getDefaultNodeTable();	
	}
//	public Map<String, CyChart> getChartMap() {		return idMap;	}
	public void setVersion(String v) { this.version = v; }
	public String getVersion() { return version; }

	public Frame getOwner() {
		final CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
		return swingApplication == null ? null : swingApplication.getJFrame();
	}
	CyColumn xColumn;
	CyColumn yColumn;

	public void setXColumn(CyColumn c) 	{	xColumn = c;	}
	public void setYColumn(CyColumn c) 	{	yColumn = c;	}
	public CyColumn getXColumn() 		{	return xColumn;	}
	public CyColumn getYColumn() 		{	return yColumn;
	}
	
	
//	Map<Long, Double> getColumnDoubleMap(CyTable table, String colName)
//	{
//		Map<Long, Double> map = new HashMap<Long, Double> ();
//		CyColumn col = table.getColumn(colName);
//		if (col != null)
//		{
//			List<Double> vals = null;
//			List<Long> ids = null;
//			vals = col.getValues(Double.class);
//			ids = col.getValues(Long.class);
//			int sz = vals.size();
//			for (int i=0; i< sz; i++)
//				map.put(ids.get(i), vals.get(i));
//		}
//		return map;
//	}
//	
//	Map<Long, Point2D> getColumnPointMap(CyTable table, String xColName, String yColName)
//	{
//		Map<Long, Point2D> map = new HashMap<Long, Point2D> ();
//		CyColumn xcol = table.getColumn(xColName);
//		CyColumn ycol = table.getColumn(yColName);
//		CyColumn idcol = table.getColumn("SUID");
//		if (xcol != null && ycol != null)
//		{
//			List<Long> ids = null;
//			List<Double> xvals = xcol.getValues(Double.class);
//			List<Double> yvals = xcol.getValues(Double.class);
//			ids = idcol.getValues(Long.class);
//			int sz = xvals.size();
//			for (int i=0; i< sz; i++)
//				map.put(ids.get(i), new Point2D(xvals.get(i), yvals.get(i)));
//		}
//		return map;
//	}
//	
//		


}
