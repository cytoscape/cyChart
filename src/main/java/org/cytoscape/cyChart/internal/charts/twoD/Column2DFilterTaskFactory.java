package org.cytoscape.cyChart.internal.charts.twoD;

import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.cyChart.internal.tasks.HistogramFilterTask;
import org.cytoscape.cyChart.internal.tasks.ScatterFilterTask;
import org.cytoscape.model.CyColumn;
import org.cytoscape.task.AbstractTableColumnTaskFactory;
import org.cytoscape.work.TaskIterator;

public class Column2DFilterTaskFactory extends AbstractTableColumnTaskFactory {

	final CyChartManager manager;

	public Column2DFilterTaskFactory(CyChartManager mgr) {	manager = mgr;	}

	public boolean isReady() {		return true;	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column)
	{ 
		return new TaskIterator(new ScatterFilterTask(manager, column, null)); 	
	}


}

