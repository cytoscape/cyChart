package org.cytoscape.cyChart.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.cyChart.internal.charts.oneD.ColumnFilterTaskFactory;
import org.cytoscape.cyChart.internal.charts.twoD.Column2DFilterTaskFactory;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.cyChart.internal.tasks.HistogramFilterTaskFactory;
 import org.cytoscape.cyChart.internal.tasks.ScatterFilterTaskFactory;
import org.cytoscape.cyChart.internal.tasks.VersionTaskFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// See if we have a graphics console or not
		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());
		if (ref == null)  	return;

		// Get a handle on the CyServiceRegistrar
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		CyChartManager manager = new CyChartManager(registrar);
		String version = bc.getBundle().getVersion().toString();
		manager.setVersion(version);

		HistogramFilterTaskFactory histoChart = new HistogramFilterTaskFactory(manager);
		Properties props = new Properties();
		props.setProperty(MENU_GRAVITY, "0.1");
		props.setProperty(PREFERRED_MENU, "Tools");
		props.setProperty(TITLE, "Plot Histogram...");
		props.setProperty(IN_MENU_BAR, "true");
		props.setProperty(COMMAND_NAMESPACE, "cychart");
		props.setProperty(COMMAND, "histogram");
		props.setProperty(COMMAND_DESCRIPTION, "Launch a histogram chart in a separate window");
		props.setProperty(COMMAND_LONG_DESCRIPTION,  "Launch a Cytoscap CyChart in a separate window.  " );
		props.setProperty(COMMAND_SUPPORTS_JSON, "true");
		props.setProperty(COMMAND_EXAMPLE_JSON, "{\"id\":\"my window\"}");
		registerService(bc, histoChart, TaskFactory.class, props);

		ColumnFilterTaskFactory histoChart2 = new ColumnFilterTaskFactory(manager);
		props = new Properties();
		props.setProperty(TITLE, "Plot Histogram...");
		props.setProperty(IN_MENU_BAR, "true");
		props.setProperty(COMMAND_NAMESPACE, "cychart");
		props.setProperty(COMMAND, "histogram");
		props.setProperty(COMMAND_DESCRIPTION, "Launch a Histogram chart in a separate window");
		props.setProperty(COMMAND_LONG_DESCRIPTION,  "Launch a Cytoscap CyChart in a separate window.  "       );
		props.setProperty(COMMAND_SUPPORTS_JSON, "true");
		props.setProperty(COMMAND_EXAMPLE_JSON, "{\"id\":\"my window\"}");
		registerService(bc, histoChart2, TableColumnTaskFactory.class, props);

		ScatterFilterTaskFactory scatChart = new ScatterFilterTaskFactory(manager);
		props = new Properties();
		props.setProperty(PREFERRED_MENU, "Tools");
		props.setProperty(TITLE, "Plot Scatter...");
		props.setProperty(MENU_GRAVITY, "0.3");
		props.setProperty(IN_MENU_BAR, "true");
		props.setProperty(COMMAND_NAMESPACE, "cychart");
		props.setProperty(COMMAND, "scatter");
		props.setProperty(COMMAND_DESCRIPTION, "Launch a Scatter chart in a separate window");
		props.setProperty(COMMAND_LONG_DESCRIPTION, 
		                  "Launch a Cytoscap CyChart in a separate window.  ");
		props.setProperty(COMMAND_SUPPORTS_JSON, "true");
		props.setProperty(COMMAND_EXAMPLE_JSON, "{\"id\":\"my window\"}");
		registerService(bc, scatChart, TaskFactory.class, props);
	
		Column2DFilterTaskFactory scatChart2 = new Column2DFilterTaskFactory(manager);
		props = new Properties();
		props.setProperty(TITLE, "Plot Scatter...");
		props.setProperty(COMMAND_NAMESPACE, "cychart");
		props.setProperty(COMMAND, "column");
		props.setProperty(COMMAND_DESCRIPTION, "Launch a Scatter chart in a separate window");
		props.setProperty(COMMAND_LONG_DESCRIPTION,  "Launch a Cytoscap CyChart in a separate window.  "       );
		props.setProperty(COMMAND_SUPPORTS_JSON, "false");
		registerService(bc, scatChart2, TableColumnTaskFactory.class, props);


		VersionTaskFactory versionTask = new VersionTaskFactory(version);
		props = new Properties();
		props.setProperty(COMMAND_NAMESPACE, "cychart");
		props.setProperty(COMMAND, "version");
		props.setProperty(COMMAND_DESCRIPTION, "Display the CyChart version");
		props.setProperty(COMMAND_LONG_DESCRIPTION, "Display the version of the CyChart app.");
		props.setProperty(COMMAND_SUPPORTS_JSON, "true");
		props.setProperty(COMMAND_EXAMPLE_JSON, "{\"version\":\"1.0\"}");
		registerService(bc, versionTask, TaskFactory.class, props);
        registerService(bc, histoChart, HistogramFilterTaskFactory.class, props);
//        registerService(bc, startChart, TableColumnTaskFactory.class, props);
	}
}
