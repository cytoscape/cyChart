package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class HideChartTaskFactory extends AbstractTaskFactory {

	final CyChartManager manager;
	public HideChartTaskFactory(CyChartManager m) { manager = m;  }
	public boolean isReady() { return true; }

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new HideChartTask(manager));
	}
}

