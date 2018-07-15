package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class HideChartTask extends AbstractEmptyObservableTask {

	@Tunable (description="Window ID", 
	          longDescription="The ID for the browser window to hide",
	          exampleStringValue="Window 1",
	          context="nogui")
	public String id = null;

	final CyChartManager manager;

	public HideChartTask(CyChartManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		CyChart chart = manager.getChart(id);
		// System.out.println("Hiding window "+id+" browser "+browser);
//		if (browser instanceof ResultsPanelBrowser)
//			manager.unregisterCytoPanel((ResultsPanelBrowser)browser);
//		else 
			if (chart instanceof HistogramFilterDialog)
			((HistogramFilterDialog)chart).dispose();
		manager.removeChart(id);
	}

	@ProvidesTitle
	public String getTitle() {
		return "Hiding Cytoscape Web Browser";
	}
}
