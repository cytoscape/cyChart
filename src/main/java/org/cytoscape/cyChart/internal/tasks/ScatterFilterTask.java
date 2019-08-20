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
			
			ScatterFilterDialog	chart = new ScatterFilterDialog(manager, title);
			chart.setVisible(true);
//		}
				
	}

}
