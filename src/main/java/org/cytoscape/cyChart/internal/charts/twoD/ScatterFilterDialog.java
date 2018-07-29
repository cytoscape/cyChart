package org.cytoscape.cyChart.internal.charts.twoD;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.cyChart.internal.charts.oneD.SwingPanel;
import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;

public class ScatterFilterDialog extends JDialog implements CyChart, ChangeListener {

	private static final long serialVersionUID = 1L;
	private final CyChartManager manager;
	private ScatterFilterPanel currentPanel;
	private String initialTitle = null;


	public ScatterFilterDialog(CyChartManager mgr, String title ) {
		super(mgr.getOwner());
		System.out.println("<ScatterFilterDialog>");
		manager = mgr;
		if (title != null) {
			setTitle(title);
			initialTitle = title;
		} else  setTitle("CyChart");

		currentPanel = new ScatterFilterPanel(manager, this);
		getContentPane().add(currentPanel);


		setPreferredSize(new Dimension(600, 600));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		System.out.println("</ ScatterFilterDialog>");

	}

	public String getTitle(String id) { 
		if (id == null)
			return initialTitle;
		return null;
	}

	public ScatterFilterPanel getPanel(String id) { 
		if (id == null) 				return currentPanel; 
		return null;
	}

	@Override public void stateChanged(ChangeEvent e) {
		String ttl = currentPanel.getTitle();
		setTitle((ttl != null) ? ttl : "CyChart");
	}

}
