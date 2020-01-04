package org.cytoscape.cyChart.internal.charts.oneD;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.cyChart.internal.view.SwingPanel;

public class HistogramFilterDialog extends JDialog implements CyChart, ChangeListener {

	private static final long serialVersionUID = 1L;
	private final CyChartManager manager;
	private SwingPanel currentPanel;


	public HistogramFilterDialog(CyChartManager mgr,String title ) {
		super(mgr.getOwner());
		manager = mgr;
		setTitle((title != null) ? title : "CyChart");
		currentPanel = new SwingPanel(manager, this);
		getContentPane().add(currentPanel);
		setPreferredSize(new Dimension(520, 500));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
	}
//
	public String getTitle(String id) { 	return getTitle();	}
	public SwingPanel getPanel(String id) { return currentPanel;	}

	@Override public void stateChanged(ChangeEvent e) {
		String ttl = currentPanel.getTitle();
		setTitle((ttl != null) ? ttl : "CyChart");
	}

}
