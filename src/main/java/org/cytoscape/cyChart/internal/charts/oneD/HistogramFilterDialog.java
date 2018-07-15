package org.cytoscape.cyChart.internal.charts.oneD;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;

import javafx.scene.control.Button;

public class HistogramFilterDialog extends JDialog implements CyChart, ChangeListener {

	private static final long serialVersionUID = 1L;
	private final CyChartManager manager;
	private SwingPanel currentPanel;
	private String initialTitle = null;


	public HistogramFilterDialog(CyChartManager mgr, String id, String title ) {
		super();
		System.out.println("<HistogramFilterDialog>");
		manager = mgr;
		if (title != null) {
			setTitle(title);
			initialTitle = title;
		} else  setTitle("CyChart");

		System.out.println("ChartDialog start");
		currentPanel = new SwingPanel(manager, id, this);
		getContentPane().add(currentPanel);

		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent evt) { manager.removeChart(id); }
		});

		setPreferredSize(new Dimension(1024, 600));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		System.out.println("</HistogramFilterDialog>");

	}

	public String getTitle(String id) { 
		if (id == null)
			return initialTitle;
		return null;
	}

	public SwingPanel getPanel(String id) { 
		if (id == null) 				return currentPanel; 
		return null;
	}

	@Override public void stateChanged(ChangeEvent e) {
		String ttl = currentPanel.getTitle();
		setTitle((ttl != null) ? ttl : "CyChart");
	}

}
