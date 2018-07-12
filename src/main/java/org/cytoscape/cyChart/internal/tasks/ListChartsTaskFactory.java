package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ListChartsTaskFactory extends AbstractTaskFactory {

	final CyChartManager manager;

	public ListChartsTaskFactory(CyChartManager manager) {
		this.manager = manager;
	}

	public boolean isReady() {
		return true;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ListChartsTask(manager));
	}
}

