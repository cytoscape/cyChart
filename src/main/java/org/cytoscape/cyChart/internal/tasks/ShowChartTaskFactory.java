package org.cytoscape.cyChart.internal.tasks;

import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ShowChartTaskFactory extends AbstractTaskFactory {

	final private CyChartManager manager;
	public ShowChartTaskFactory(CyChartManager m) {	manager = m;}
	public boolean isReady() 			{	return true;	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ShowChartTask(manager));
	}
}

