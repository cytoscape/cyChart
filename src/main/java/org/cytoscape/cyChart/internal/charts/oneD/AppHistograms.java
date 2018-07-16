package org.cytoscape.cyChart.internal.charts.oneD;

import java.io.IOException;
import java.net.URL;

import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class AppHistograms extends Application 
{
    public static void main(final String[] args) {    Application.launch(args);    }

    @Override public  void start(Stage stage) throws Exception 
    {
    	me = this;
    	StackPane appPane = getStackPane(null);
    	Scene scene = new Scene(appPane, 600, 400);
	    stage.setScene(scene);
	    stage.show();
   }

    public static StackPane getStackPane(CyServiceRegistrar registrar)
    {
	    StackPane pane = new StackPane();
	    pane.setPrefWidth(600);
	    pane.setPrefHeight(400);
	    HistogramChartController cntl = new HistogramChartController(pane, registrar);
	    System.out.println("HistogramChartControllers status = "  + cntl.ping()); 
	    return pane;
    }  
//
//    public static AnchorPane getAnchorPane()
//    {
//	    FXMLLoader fxmlLoader = new FXMLLoader();
//	    URL url = AppHistograms.class.getResource("HistogramChart.fxml");
//	    HistogramChartController cntl = new org.cytoscape.cyChart.internal.charts.oneD.HistogramChartController();
//	   System.out.println("HistogramChartControllesr status = "  + cntl.ping()); 
//	    fxmlLoader.setLocation(url);
//	    AnchorPane appPane;
//		try {
//			appPane = fxmlLoader.load();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			appPane = null;
//		}
//	    return appPane;
//    }
    
    
 static public AppHistograms getInstance()	{ return me;	}
 static private AppHistograms me;
// static private Stage theStage;
// static public Stage getStage() { return theStage;  }
     
}