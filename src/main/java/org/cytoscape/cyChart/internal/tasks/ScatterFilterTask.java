package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.charts.twoD.ScatterFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

public class ScatterFilterTask extends AbstractEmptyObservableTask {
	public String title = null;

	@ProvidesTitle
	public String getTitle() {		return "Starting Cytoscape Scatter Plot";	}

	@Override	public <R> R getResults(Class<? extends R> type) {		return getIDResults(type, null);	}

	final CyChartManager manager;
	CyColumn column = null;

	//----------------------------------------------------
	public ScatterFilterTask(CyChartManager mgr) {
		manager = mgr;
//		ScatterFilterDialog	chart = new ScatterFilterDialog(manager, title);
//		chart.setVisible(true);
	}

	public ScatterFilterTask(CyChartManager mgr, CyColumn col) {
		manager = mgr;
		column = col;
	}

	public void run(TaskMonitor monitor) {
//		SwingUtilities.invokeLater(new Runnable() {	public void run() {	}	});	
		ScatterFilterDialog	chart = new ScatterFilterDialog(manager, title, column);
		chart.setVisible(true);
		}

}
