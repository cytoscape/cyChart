package org.cytoscape.cyChart.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.cyChart.internal.charts.oneD.AppHistograms;
import org.cytoscape.cyChart.internal.charts.oneD.HistogramFilterDialog;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class SwingPanel extends JPanel {
 
	protected JFXPanel jfxPanel;
 
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

	public SwingPanel(CyChartManager manager, HistogramFilterDialog parentDialog, CyColumn column) {
		super(new BorderLayout());
		registrar = manager.getRegistrar();
//		System.out.println("SwingPanel");
		setPreferredSize(new Dimension(600, 500));
		initComponents(column);
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

	private void initComponents(CyColumn column) {
//		System.out.println("initComponents");
		jfxPanel = new JFXPanel();
//		System.out.println("jfxPanel created");
		StackPane appPane = AppHistograms.getStackPane(registrar, column);
//		appPane.setBorder(Borders.magentaBorder);
		if (appPane != null) 
		{
//			System.out.println("appPane created");
			Scene scene = new Scene(appPane);
			jfxPanel.setScene(scene);
//			System.out.println("scene created");
		}
		else System.out.println("appPane came back null");

//		JPanel topBar = new JPanel(new BorderLayout(5, 0));
//		topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

//		JPanel statusBar = new JPanel(new BorderLayout(5, 0));
//		statusBar.setBorder(BorderFactory.createLineBorder(Color.RED));
//		statusBar.add(lblStatus, BorderLayout.CENTER);
//		lblStatus.setMinimumSize(new Dimension(20, 10));
//		lblStatus.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
//		lblStatus.setText("The statRs bar reports the range and percentage selected.");
//		add(topBar, BorderLayout.NORTH);
//		add(statusBar, BorderLayout.SOUTH);
//		JPanel container = new JPanel();
//		container.setBorder(BorderFactory.createLineBorder(Color.GREEN));
//		jfxPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
//		container.add(jfxPanel);
		add(jfxPanel, BorderLayout.CENTER);
	}

}