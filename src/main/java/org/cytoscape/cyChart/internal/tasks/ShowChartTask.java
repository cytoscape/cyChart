package org.cytoscape.cyChart.internal.tasks;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ShowChartTask extends AbstractEmptyObservableTask {

	@Tunable (description="URL", 
	          longDescription="The URL the browser should load",
	          exampleStringValue="http://www.cytoscape.org",
	          gravity=1.0)
	public String url;

	@Tunable (description="HTML Text", 
	          longDescription="HTML text to initially load into the browser",
	          exampleStringValue="<HTML><HEAD><TITLE>Hello</TITLE></HEAD><BODY>Hello, world!</BODY></HTML>",
	          context="nogui")
	public String text;

	@Tunable (description="Window Title", 
	          longDescription="Text to be shown in the title bar of the browser window",
	          exampleStringValue="Cytoscape Home Page",
	          context="nogui")
	public String title = null;

	@Tunable (description="Window ID", 
	          longDescription="The ID for this browser window.  Use this with ``cybrowser hide`` to hide the browser",
	          exampleStringValue="Window 1",
	          context="nogui")
	public String id = null;

	final CyChartManager manager;
  final CytoPanel cytoPanel = null;

	public ShowChartTask(CyChartManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CyChart br = manager.getChart(id);
//				ResultsPanelBrowser browser;
//				if (br != null && br instanceof ResultsPanelBrowser)
//					browser = (ResultsPanelBrowser) br;
//				else 
//					if (br != null && br instanceof SwingBrowser) {
//					SwingPanel panel = br.getPanel(id);
//					((SwingBrowser)br).setVisible(false);
//					browser = new ResultsPanelBrowser(manager, id, panel, title);
//				} else {
//					browser = new ResultsPanelBrowser(manager, id, title);
//				}
//
//				if (url != null && url.length() > 3) {
//					browser.loadURL(url);
//				} else if (text != null) {
//					browser.loadText(text);
////				}
//				manager.registerCytoPanel(browser);
//				br = (CyChart) browser;

				manager.addChart(br, id);
			}
		});

	}

	@ProvidesTitle
	public String getTitle() {
		return "Showing Results Panel Browser";
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		return getIDResults(type, id);
	}
}
