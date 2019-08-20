package org.cytoscape.cyChart.internal.charts.twoD;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.CountDownLatch;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class ScatterFilterPanel extends JPanel {
 
	protected JFXPanel jfxPanel;
 
	public static final String EVENT_TYPE_CLICK = "click";
	public static final String EVENT_TYPE_CONTEXT_MENU = "contextmenu";

	private String title = null;
	private CyServiceRegistrar registrar;

	final Logger logger = Logger.getLogger(CyUserLog.NAME);
 
	public ScatterFilterPanel(CyChartManager manager, ScatterFilterDialog parentDialog, CyColumn defaulX, CyColumn defaultY) {
		super(new BorderLayout());
		registrar = manager.getRegistrar();
//		System.out.println("ScatterFilterPanel");
		setPreferredSize(new Dimension(520, 500));
		initComponents(defaulX, defaultY);
//		Platform.setImplicitExit(false);
	}

	public String getTitle() 	{		return title;	}

	public String execute(final String script) {
		final String[] returnVal = new String[1]; 
		final CountDownLatch doneLatch = new CountDownLatch(1);
		try {
			doneLatch.await();
		} catch (InterruptedException e) {
		}
		return returnVal[0];
	}

	private void initComponents(CyColumn defaultX, CyColumn defaultY) {
		jfxPanel = new JFXPanel();
		StackPane appPane = AppScatters.getStackPane(registrar, defaultX, defaultY);
		if (appPane != null) 
		{
			Scene scene = new Scene(appPane);
			jfxPanel.setScene(scene);
//			System.out.println("scene created");
		}
		else System.out.println("appPane came back null");
		add(jfxPanel, BorderLayout.CENTER);
	}

}