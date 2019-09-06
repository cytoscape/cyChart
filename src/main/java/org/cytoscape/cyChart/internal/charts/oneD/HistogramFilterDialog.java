package org.cytoscape.cyChart.internal.charts.oneD;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.cyChart.internal.view.SwingPanel;
import org.cytoscape.model.CyColumn;

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
	public String getTitle(String id) { 	return (id == null) ? getTitle() : null;	}
	public SwingPanel getPanel(String id) { return (id == null) ? currentPanel : null;	}

	@Override public void stateChanged(ChangeEvent e) {
		String ttl = currentPanel.getTitle();
		setTitle((ttl != null) ? ttl : "CyChart");
	}

}
