package org.cytoscape.cyChart.internal.charts.twoD;

import java.net.URL;

import org.cytoscape.cyChart.internal.charts.oneD.HistogramChartController;
import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class AppScatters extends Application 
{
    public static void main(final String[] args) {    Application.launch(args);    }

    @Override public  void start(Stage stage) throws Exception 
    {
    	me = this;
	    FXMLLoader fxmlLoader = new FXMLLoader();
	    URL url = getClass().getResource("ScatterChart.fxml");
	    fxmlLoader.setLocation(url);
	    AnchorPane appPane = fxmlLoader.load();
	    Scene scene = new Scene(appPane, 1000, 800);
	    stage.setScene(scene);
	    stage.show();
   }

    public static StackPane getStackPane(CyServiceRegistrar registrar)
    {
	    StackPane pane = new StackPane();
	    pane.setPrefWidth(500);
	    pane.setPrefHeight(600);
	    ScatterChartController cntl = new ScatterChartController(pane, registrar);
	    System.out.println("ScatterChartController status = "  + cntl.ping()); 
	    return pane;
    }  
    
    
 static public AppScatters getInstance()	{ return me;	}
 static private AppScatters me;
 static private Stage theStage;
 static public Stage getStage() { return theStage;  }
     
}