package org.cytoscape.cyChart.internal.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.CountDownLatch;

import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.cyChart.internal.charts.oneD.AppHistograms;
import org.cytoscape.cyChart.internal.charts.oneD.HistogramChartController;
import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class SwingPanel extends JPanel {
 
	protected JFXPanel jfxPanel;
	private CyChartManager manager;
 
	public static final String EVENT_TYPE_CLICK = "click";
	public static final String EVENT_TYPE_CONTEXT_MENU = "contextmenu";

	private String title = null;
	private CyServiceRegistrar registrar;

	final Logger logger = Logger.getLogger(CyUserLog.NAME);
 
//	public SwingPanel(CyChartManager manager, HistogramFilterDialog parentDialog) {
//		super(new BorderLayout());
//		registrar = manager.getRegistrar();
////		System.out.println("SwingPanel");
//		setPreferredSize(new Dimension(800, 500));
//		initComponents(null);
//		Platform.setImplicitExit(false);
//	}
	public SwingPanel(CyChartManager mgr, HistogramFilterDialog parentDialog) {
		super(new BorderLayout());
		manager = mgr;
		registrar = manager.getRegistrar();
//		System.out.println("SwingPanel");
		setPreferredSize(new Dimension(600, 500));
		initComponents();
		Platform.setImplicitExit(false);
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

	private void initComponents() {
//		System.out.println("initComponents");
		jfxPanel = new JFXPanel();
//		System.out.println("jfxPanel created");
//		StackPane appPane = AppHistograms.getStackPane(registrar, manager);
	    StackPane pane = new StackPane();
	    pane.setPrefWidth(520);
	    pane.setPrefHeight(500);
	    try
	    {
	    	System.out.println("preconstruction");
	    	HistogramChartController ctrl = new HistogramChartController(pane, registrar, manager);
//		appPane.setBorder(Borders.magentaBorder);
			Scene scene = new Scene(pane);
			Platform.runLater(() -> {	jfxPanel.setScene(scene);	});
		add(jfxPanel, BorderLayout.CENTER);
	}
	    catch (Exception e) 
	    {
	    	e.printStackTrace();
	    }
	}
	
	

}