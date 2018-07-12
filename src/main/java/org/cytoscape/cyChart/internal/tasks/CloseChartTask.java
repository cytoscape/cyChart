package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CloseChartTask extends AbstractEmptyObservableTask {

	@Tunable (description="Window ID", 
	          longDescription="The ID for the chart window to close",
	          exampleStringValue="Window 1",
	          context="nogui")
	public String id = null;

	final CyChartManager manager;

	public CloseChartTask(CyChartManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		manager.closeChart(id);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Hiding Cytoscape Chart";
	}
}
