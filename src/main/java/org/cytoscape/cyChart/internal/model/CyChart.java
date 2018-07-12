package org.cytoscape.cyChart.internal.model;
import org.cytoscape.cyChart.internal.charts.twoD.SwingPanel;

public interface CyChart {
//	public void loadURL(String url);
//	public void loadText(String text);
	public String getTitle(String id);
//	public String getURL(String id);
	public SwingPanel getPanel(String id);
}
