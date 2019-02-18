package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

public class HistogramFilterTask extends AbstractEmptyObservableTask {
	final private String title = "Histogram Plot";

	@ProvidesTitle
	public String getTitle() {		return title;	}
	final private CyChartManager manager;

	//----------------------------------------------------
	public HistogramFilterTask(CyChartManager mgr) 	{ 	this(mgr, null);	}	
	
	public HistogramFilterTask(CyChartManager mgr, CyColumn column) {
		manager = mgr;
		if (mgr.getCurrentNetwork() == null) return;
		HistogramFilterDialog	dlog = new HistogramFilterDialog(manager, title, column);
		dlog.setVisible(true);
	}

	public void run(TaskMonitor monitor) {	}
}
