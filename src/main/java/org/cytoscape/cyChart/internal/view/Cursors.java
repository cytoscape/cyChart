package org.cytoscape.cyChart.internal.view;

import java.util.HashMap;

import org.cytoscape.cyChart.internal.model.NodeUtil;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class Cursors
{
	public static Cursor getResizeCursor(Pos p)
	{
		if (p == Pos.CENTER) return Cursor.HAND;
		
		if (p == Pos.TOP_LEFT) return Cursor.NW_RESIZE;
		if (p == Pos.TOP_RIGHT) return Cursor.NE_RESIZE;
		if (p == Pos.TOP_CENTER) return Cursor.N_RESIZE;

		if (p == Pos.BOTTOM_LEFT) return Cursor.SW_RESIZE;
		if (p == Pos.BOTTOM_RIGHT) return Cursor.SE_RESIZE;
		if (p == Pos.BOTTOM_CENTER) return Cursor.S_RESIZE;

		if (p == Pos.CENTER_LEFT) return Cursor.W_RESIZE;
		if (p == Pos.CENTER_RIGHT) return Cursor.E_RESIZE;

		return Cursor.HAND;
	}

	
    public static Cursor getTextCursor(String txt)
    {
    	return  getTextCursor(txt, Color.GREEN); 
    }
    
    static HashMap<String, Cursor> cursorCache = new HashMap<String, Cursor>();
    static String STYLE = "-fx-background-color: whitesmoke; -fx-font-size: 36; ";
    static int W = 50;		// the width (and height) of the cursor

	public static Cursor getTextCursor(String txt, Color col)
    {
    	Cursor curs = cursorCache.get(txt);
    	if (curs != null) return curs;
    	
    	Label label = new Label(txt);
		label.setStyle(STYLE);
	    label.setWrapText(true);
	    label.setTextFill(col);
	    StackPane pane = new StackPane(label);
	    NodeUtil.forceSize(pane, W, W);
	    pane.setBorder(Borders.blueBorder1);
	    Scene scene = new Scene(pane);
	    WritableImage img = new WritableImage(W, W) ;
	    scene.snapshot(img);
	    curs = new ImageCursor(img, W/2, W/2); 
	    cursorCache.put(txt, curs);
//	    System.out.println("cursorCache: " + txt);
	    return curs; 
    }
}

