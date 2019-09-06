package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class HistogramFilterTask extends AbstractEmptyObservableTask {
	private String title = "Histogram Plot";

	@Tunable(description = "X Axis Parameter", context= Tunable.NOGUI_CONTEXT)
	
	public String xColumn = "Degree";

	
	@ProvidesTitle
	public String getTitle() {		return title;	}
	final private CyChartManager manager;
	private CyColumn column;

	//----------------------------------------------------
	public HistogramFilterTask(CyChartManager mgr) 	{ 	this(mgr, null);	}	
	
	public HistogramFilterTask(CyChartManager mgr, CyColumn col) {
		manager = mgr;
		column = col;
		if (column == null)
		{
			CyNetwork net = manager.getCurrentNetwork();
			if (net == null) return;
			CyTable table = net.getDefaultNodeTable();
			column = table.getColumn(xColumn);
			if (column == null)
				column = table.getColumn("EdgeCount");
			if (column == null)
				column = table.getColumn("OutDegree");

		}
	}

	public void run(TaskMonitor monitor) 
	{	
		if (manager.getCurrentNetwork() == null) return;
		manager.setXColumn(column);
		monitor.setStatusMessage("Building histogram");
		CyNetwork net = manager.getCurrentNetwork();
		title = net.getDefaultNetworkTable().getTitle();
		HistogramFilterDialog	dlog = new HistogramFilterDialog(manager, title);
		dlog.setVisible(true);
		
	}
}
