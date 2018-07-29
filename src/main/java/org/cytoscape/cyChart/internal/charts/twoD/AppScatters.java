package org.cytoscape.cyChart.internal.charts.twoD;

import javax.swing.JLabel;

import org.cytoscape.service.util.CyServiceRegistrar;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class AppScatters extends Application 
{
    public static void main(final String[] args) {    Application.launch(args);    }

    @Override public  void start(Stage stage) throws Exception 
    {
    	me = this;
//	    FXMLLoader fxmlLoader = new FXMLLoader();
//	    URL url = getClass().getResource("ScatterChart.fxml");
//	    fxmlLoader.setLocation(url);
//	    AnchorPane appPane = fxmlLoader.load();
    	StackPane pane = getStackPane(null, null);
    	Scene scene = new Scene(pane, 520, 550);
	    stage.setScene(scene);
	    stage.show();
   }

    public static StackPane getStackPane(CyServiceRegistrar registrar, JLabel status)
    {
	    StackPane pane = new StackPane();
	    pane.setPrefWidth(520);
	    pane.setPrefHeight(550);
	    ScatterChartController cntl = new ScatterChartController(pane, registrar, status);
//	    System.out.println("ScatterChartController status = "  + cntl.ping()); 
	    return pane;
    }  
    
    
 static public AppScatters getInstance()	{ return me;	}
 static private AppScatters me;
 static private Stage theStage;
 static public Stage getStage() { return theStage;  }
     
}