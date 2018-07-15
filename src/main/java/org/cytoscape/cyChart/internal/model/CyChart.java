package org.cytoscape.cyChart.internal.model;
import javax.swing.JPanel;

public interface CyChart {
	public String getTitle(String id);
	public JPanel getPanel(String id);
}
