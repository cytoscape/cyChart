package org.cytoscape.cyChart.internal.tasks;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.cyChart.internal.charts.twoD.ChartDialog;
import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class DialogTask extends AbstractEmptyObservableTask {
//
//	@Tunable (description="A SPOT FOR YOUR UNUSED URL", 
//	          longDescription="The URL the browser should load",
//	          exampleStringValue="http://www.cytoscape.org",
//	          gravity=1.0)
//	public String url;
//
//	@Tunable (description="Are you a Histogram?", 
//	          longDescription="If true, open the a 1D graph",
//	          exampleStringValue="true",
//	          gravity=2.0, context="gui")
//	public boolean resultsPanel = false;
//
//	@Tunable (description="X Axis", 
//	          longDescription="The name of the X or horizontal axis",
//	          exampleStringValue="Degree",  gravity=3.0,
//	          context="gui")
//	public String text;
//
	@Tunable (description="Window Title", 
	          longDescription="Text to be shown in the title bar of the chart window",
	          exampleStringValue="Cytoscape Home Page",
	    	          gravity=4.0,  context="gui")
	public String title = null;

	@Tunable (description="Window ID", 
	          longDescription="The ID for this chart window.  Use this with ``cychart hide`` to hide the chart",
	          exampleStringValue="Window 1",	gravity=5.0, context="gui")
	public String id = null;

	final CyChartManager manager;
  final CytoPanel cytoPanel = null;

	public DialogTask(CyChartManager manager) {
		System.out.println("<DialogTask");
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("RUN");
				CyChart chrt = manager.getChart(id);
				ChartDialog chart = null;
//
//				if (id == null) {
//					if (title != null) 
//						id = title;
//					else
//						id = manager.makeId();
//				}

				if (chrt != null && chrt instanceof ChartDialog)
					chart = (ChartDialog) chrt;
				else
					chart = new ChartDialog(manager, id, title);

				chart.setVisible(true);
				chrt = (CyChart) chart;

				System.out.println("addChart");
				manager.addChart(chrt, id);
			}
		});

	}

	@ProvidesTitle
	public String getTitle() {		return "Starting Cytoscape Chart Filter";	}

	@Override	public <R> R getResults(Class<? extends R> type) {		return getIDResults(type, id);	}
}
