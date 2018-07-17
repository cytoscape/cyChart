package org.cytoscape.cyChart.internal.charts.oneD;

import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.cyChart.internal.tasks.HistogramFilterTask;
import org.cytoscape.model.CyColumn;
import org.cytoscape.task.AbstractTableColumnTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ColumnFilterTaskFactory extends AbstractTableColumnTaskFactory {

	final CyChartManager manager;

	public ColumnFilterTaskFactory(CyChartManager mgr) {	manager = mgr;	}

	public boolean isReady() {		return true;	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column) { return new TaskIterator(new HistogramFilterTask(manager, column)); 	}


}

