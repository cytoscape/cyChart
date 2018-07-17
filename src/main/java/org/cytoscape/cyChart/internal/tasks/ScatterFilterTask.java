package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.charts.twoD.ScatterFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

public class ScatterFilterTask extends AbstractEmptyObservableTask {
	public String title = null;

	@ProvidesTitle
	public String getTitle() {		return "Starting Cytoscape Scatter Filter";	}

	@Override	public <R> R getResults(Class<? extends R> type) {		return getIDResults(type, null);	}

	final CyChartManager manager;

	//----------------------------------------------------
	public ScatterFilterTask(CyChartManager mgr) {
		manager = mgr;
		ScatterFilterDialog	chart = new ScatterFilterDialog(manager, title);
		chart.setVisible(true);
	}

	public void run(TaskMonitor monitor) {
//		SwingUtilities.invokeLater(new Runnable() {	public void run() {	}	});	
		}

}
