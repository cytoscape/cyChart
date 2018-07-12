package org.cytoscape.cyChart.internal.charts;

import javafx.beans.binding.Binding;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class NodeUtil
{
	static public void centerAt(ImageView view, double x, double y)
	{
		view.setX(x - (view.getFitWidth() / 2));
		view.setY(y + (view.getFitHeight() / 2));
	}
	static public void forceWidth(Region n, int w)
	{
		n.setPrefWidth(w);
		n.setMinWidth(w);
		n.setMaxWidth(w);
	}
	static public void forceHeight(Region n, int h)
	{
		n.setPrefHeight(h);
		n.setMinHeight(h);
		n.setMaxHeight(h);
	}
	static public void forceSize(Region n, int w, int h)
	{
		forceWidth(n, w);
		forceHeight(n, h);
	}
	
    static public void invalOnActionOrFocusLost(Node n, Binding b)
    {
    	n.addEventHandler(ActionEvent.ACTION, evt -> b.invalidate());
        n.focusedProperty().addListener((obs, old, isFocused)-> {   if (! isFocused) {	b.invalidate(); }  });
    }

    static boolean verbose = true;

	public static String shortClassname(String class1)
	{
		return class1.substring(1 + class1.lastIndexOf('.'));
	}


	public static void showKids(Parent parent, String indent)
	{

		String id = parent.getId();
		if (verbose)
			System.out.println(indent + shortClassname(parent.getClass().toString()) + ":  "
							+ (id == null ? "-" : id));
		if (parent instanceof SplitPane)
		{
			for (Node n : ((SplitPane) parent).getItems())
				if (n instanceof Parent)
					showKids((Parent) n, indent + "    ");
		} else if (parent instanceof ScrollPane)
		{
			Node content = ((ScrollPane) parent).getContent();
			if (content instanceof Parent)
				showKids((Parent) content, indent + "    ");
			if (verbose)
				System.out.println(indent + shortClassname(content.getClass().toString()) + ":  "
								+ (content.getId() == null ? "-" : content.getId()));
		} else
			for (Node n : parent.getChildrenUnmodifiable())
				if (n instanceof Parent)
					showKids((Parent) n, indent + "    ");

	}
	public static void reset(Node... nodes)
	{
		for (Node n: nodes)
		{
	    	n.setOpacity(1);
	    	n.setTranslateX(0);        n.setTranslateY(0);        n.setTranslateZ(0);
	    	n.setRotate(0);            n.setScaleX(1);            n.setScaleY(1);
		}
	}



}
