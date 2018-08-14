package org.cytoscape.cyChart.internal.charts.oneD;

import javax.swing.JLabel;

import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class AppHistograms extends Application 
{
    public static void main(final String[] args) {    Application.launch(args);    }

    @Override public  void start(Stage stage) throws Exception 
    {
    	me = this;
    	StackPane appPane = getStackPane(null, null);
    	Scene scene = new Scene(appPane, 520, 500);
	    stage.setScene(scene);
	    stage.show();
   }

    public static StackPane getStackPane(CyServiceRegistrar registrar, CyColumn col)
    {
	    StackPane pane = new StackPane();
	    pane.setPrefWidth(520);
	    pane.setPrefHeight(500);
	    new HistogramChartController(pane, registrar, col);
	    return pane;
    }  
    
 static public AppHistograms getInstance()	{ return me;	}
 static private AppHistograms me;     
}