package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

public class HistogramFilterTask extends AbstractEmptyObservableTask {
	public String title = null;

	@ProvidesTitle
	public String getTitle() {		return "Starting Cytoscape Chart Filter";	}


	final CyChartManager manager;

	//----------------------------------------------------
	public HistogramFilterTask(CyChartManager mgr) {
		manager = mgr;
		if (mgr.getCurrentNetwork() == null) return;
		HistogramFilterDialog	chart = new HistogramFilterDialog(manager,title, null);
		chart.setVisible(true);
	}	
	
	public HistogramFilterTask(CyChartManager mgr, CyColumn column) {
		manager = mgr;
		HistogramFilterDialog	chart = new HistogramFilterDialog(manager, title, column);
		chart.setVisible(true);
	}

	public void run(TaskMonitor monitor) {
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				ChartDialog	chart = new ChartDialog(manager, id, title);
//				manager.addChart(chart, id);
//				chart.setVisible(true);
			}
//		});
//	}

}
