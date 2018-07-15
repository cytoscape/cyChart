package org.cytoscape.cyChart.internal.tasks;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class HistogramFilterTask extends AbstractEmptyObservableTask {
//	@Tunable (description="Window Title", 
//	          longDescription="Text to be shown in the title bar of the chart window",
//	          exampleStringValue="Chart Filter Window",
//	    	          gravity=4.0,  context="gui")
	public String title = null;

//	@Tunable (description="Window ID", 
//	          longDescription="The ID for this chart window.  Use this with ``cychart hide`` to hide the chart",
//	          exampleStringValue="Window 1",	gravity=5.0, context="gui")
	public String id = null;

	@ProvidesTitle
	public String getTitle() {		return "Starting Cytoscape Chart Filter";	}

	@Override	public <R> R getResults(Class<? extends R> type) {		return getIDResults(type, id);	}

	final CyChartManager manager;

	//----------------------------------------------------
	public HistogramFilterTask(CyChartManager mgr) {
		System.out.println("<DialogTask");
		manager = mgr;
		HistogramFilterDialog	chart = new HistogramFilterDialog(manager, id, title);
		manager.addChart(chart, id);
		chart.setVisible(true);
	}

	public void run(TaskMonitor monitor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				ChartDialog	chart = new ChartDialog(manager, id, title);
//				manager.addChart(chart, id);
//				chart.setVisible(true);
			}
		});
	}

}
