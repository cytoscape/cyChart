package org.cytoscape.cyChart.internal.charts.twoD;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;

public class ScatterFilterDialog extends JDialog implements CyChart, ChangeListener {

	private static final long serialVersionUID = 1L;
	private final CyChartManager manager;
	private ScatterFilterPanel currentPanel;
	private String initialTitle = null;

	public ScatterFilterDialog(CyChartManager mgr, String title) {
		super(mgr.getOwner());
		
		manager = mgr;
		if (title != null) {
			setTitle(title);
			initialTitle = title;
		} else  setTitle("CyChart");

		currentPanel = new ScatterFilterPanel(manager, this, mgr.getXColumn(), mgr.getYColumn());
		getContentPane().add(currentPanel);
		setPreferredSize(new Dimension(520, 600));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();

	}

	public String getTitle(String id) 				{ 	return (id == null)?  initialTitle : null;	}
	public ScatterFilterPanel getPanel(String id) 	{ 	return (id == null)?  currentPanel : null; }

	@Override public void stateChanged(ChangeEvent e) {
		String ttl = currentPanel.getTitle();
		setTitle((ttl != null) ? ttl : "CyChart");
	}

}
