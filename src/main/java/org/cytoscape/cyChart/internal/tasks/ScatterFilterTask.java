package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.charts.twoD.ScatterFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ScatterFilterTask extends AbstractEmptyObservableTask {
	public String title = null;

	@Tunable(description = "X Axis Parameter", context= Tunable.NOGUI_CONTEXT)
	public String x = "Degree";
	@Tunable(description = "Y Axis Parameter", context= Tunable.NOGUI_CONTEXT)
	public String y = "Betweeenness";

	@ProvidesTitle
	public String getTitle() {		return "Starting Cytoscape Scatter Plot";	}

	@Override	public <R> R getResults(Class<? extends R> type) {		return getIDResults(type, null);	}

	final CyChartManager manager;
	CyColumn column = null;
	CyColumn yColumn = null;

	//----------------------------------------------------
	public ScatterFilterTask(CyChartManager mgr) {
		manager = mgr;
		column = mgr.getXColumn();
		yColumn = mgr.getYColumn();
	}

	public ScatterFilterTask(CyChartManager mgr, CyColumn col, CyColumn ycol) {
		manager = mgr;
		mgr.setXColumn(col);
		mgr.setYColumn(ycol);
	}

	public void run(TaskMonitor monitor) 
	{
//		if (monitor instanceof TFExecutor)
//		{
//			TFExecutor exec = (TFExecutor) monitor;
//			exec.interceptor.
			
			manager.setXColumnName(x);
			manager.setYColumnName(y);
			ScatterFilterDialog	chart = new ScatterFilterDialog(manager, title);
			chart.setVisible(true);
//		}
				
	}

}
