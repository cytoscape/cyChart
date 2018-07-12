package org.cytoscape.cyChart.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.cyChart.internal.charts.twoD.ChartDialog;
import org.cytoscape.service.util.CyServiceRegistrar;

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
			@Override public void run() {	if (chart instanceof ChartDialog)	((ChartDialog)chart).dispose();	}	
		});
			
		removeChart(id);
	}

	public Map<String, CyChart> getChartMap() {		return idMap;	}
	public void setVersion(String v) { this.version = v; }
	public String getVersion() { return version; }
}
