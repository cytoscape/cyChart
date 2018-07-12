package org.cytoscape.cyChart.internal.charts;

import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class RectangleUtil
{
	//---------------------------------------------------------------------------
	static public void setRect(Rectangle r, Point2D a, Point2D b)
	{
		double x = Math.min(a.getX(), b.getX());
		double y = Math.min(a.getY(), b.getY());
		double width = Math.abs(a.getX() - b.getX());
		double height = Math.abs(a.getY() - b.getY());
		setRect(r, x,y,width, height);
	}

	static public void setRect(Rectangle target, Rectangle src)
	{
		setRect( target, src.getX(), src.getY(),src.getWidth(),src.getHeight());
	}
//	
	static public void setRect(Rectangle target, Rectangle src, Rectangle constraint)
	{
		double x = src.getX();
		double y = src.getY();
		if (constraint != null)
		{
			if (x < 0) x = 0;
			if (x > (constraint.getX() - target.getWidth()))
				x = constraint.getX() - target.getWidth();
			if (y < 0) y = 0;
			if (y > (constraint.getY() - target.getHeight()))
				y = constraint.getY() - target.getHeight();
		}
	}
	static public void moveRect(Rectangle r, Point2D diff)
	{
		double x = r.getX() + diff.getX();
		double y = r.getY()+ diff.getY();
//		System.out.println("Moving " + r.getId() + " to " + x + ", " + y + " diff: " + diff);
		setRect( r, x, y);
	}
	
	static public void moveRect(Rectangle r, Point2D diff, Point2D constraint)
	{
		double x = r.getX() + diff.getX();
		double y = r.getY() + diff.getY();
		
		if (constraint != null)
		{
			if (x < 0) x = 0;
			if (x > (constraint.getX() - r.getWidth()))
				x = constraint.getX() - r.getWidth();
			if (y < 0) y = 0;
			if (y > (constraint.getY() - r.getHeight()))
				y = constraint.getY() - r.getHeight();
		}
//		System.out.println("Moving " + r.getId() + " to " + x + ", " + y + " diff: " + diff);
		setRect( r, x, y);
	}
	
	static public void moveRect(Rectangle r, double dx, double dy)
	{
		double x = r.getX();
		double y = r.getY();
		setRect( r, dx + x, dy + y);
	}
	
	static public void setRect(Rectangle r, double x, double y, double w, double h)
	{
		r.setX(x);
		r.setY(y);
		r.setWidth(w);
		r.setHeight(h);
	}
	static public void setRect(Region r, double x, double y, double w, double h)
	{
		r.setLayoutX(x);
		r.setLayoutY(y);
		r.prefWidth(w);
		r.prefHeight(h);
	}
	
	static public void setRect(StackPane r, Point2D a, Point2D b)
	{
		double x = Math.min(a.getX(), b.getX());
		double y = Math.min(a.getY(), b.getY());
		double width = Math.abs(a.getX() - b.getX());
		double height = Math.abs(a.getY() - b.getY());
		setRect(r, x,y,width, height);
	}

	
	static public void setRect(StackPane r, double x, double y, double w, double h)
	{
//		r.localToParent(new Bounds(x,y,w,h));
		r.setLayoutX(x);
		r.setLayoutY(y);
		r.prefWidth(w);
//		r.setMaxWidth(w);
//		r.setMinWidth(w);
		r.prefHeight(h);
//		r.setMaxHeight(h);
//		r.setMinHeight(h);
	}
	static public void setRect(ImageView r, double x, double y, double w, double h)
	{
		r.setX(x);
		r.setY(y);
		r.setFitWidth(w);
		r.setFitHeight(h);
	}
//	static public void setRect(WebView r, double x, double y, double w, double h)
//	{
//		r.setLayoutX(x);
//		r.setLayoutY(y);
//		r.prefWidth(w);
//		r.prefHeight(h);
//	}
//	static public void setRect(TextArea r, double x, double y, double w, double h)
//	{
//		r.setLayoutX(x);
//		r.setLayoutY(y);
//		r.setPrefWidth(w);
//		r.setPrefHeight(h);
//	}
	static public void setRect(Rectangle r, double x, double y)
	{
		r.setX(x);
		r.setY(y);
	}

	public static Point2D diff(Point2D a, Point2D b)		{ return new Point2D(a.getX() - b.getX(), a.getY() - b.getY());	}
	public static double distance(Point2D a, Point2D b)		{ return Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY()));	}
	
	public static boolean isRectangleSizeTooSmall(Point2D start, Point2D end) {
		double width = Math.abs(end.getX() - start.getX());
		double height = Math.abs(end.getY() - start.getY());
		return width < 10 || height < 10;
	}

	static public void offsetRectangle(Rectangle r, double dx, double dy)
	{
		r.setX(r.getX() + dx);
		r.setY(r.getY() + dy );
	}

	static public Rectangle union(Point2D a, Point2D b)
	{
		double x = Math.min(a.getX(), b.getX());
		double y = Math.min(a.getY(), b.getY());
		double width = Math.abs(a.getX() - b.getX());
		double height = Math.abs(a.getY() - b.getY());
		return new Rectangle(x,y,width,height);
	}
	static public Rectangle constrainedUnion(Point2D a, Point2D b, double aspectRatio)
	{
		double x = Math.min(a.getX(), b.getX());
		double y = Math.min(a.getY(), b.getY());
		double width = Math.abs(a.getX() - b.getX());
		double height = width / aspectRatio;  
		return new Rectangle(x,y,width,height);
	}
	static public Rectangle intersect(Rectangle a, Rectangle b)
	{
		double x = Math.max(a.getX(), b.getX());
		double y = Math.max(a.getY(), b.getY());
		double width = Math.min(a.getWidth(), b.getWidth());
		double height = Math.min(a.getHeight(), b.getHeight());
		return new Rectangle(x,y,width,height);
	}
	static public Point2D oppositeCorner(MouseEvent ev, Rectangle r)
	{
		return oppositeCorner(ev.getX(), ev.getY(), r);
	}	
	static public Point2D oppositeCorner(Point2D pt, Rectangle r)
	{
		return oppositeCorner(pt.getX(), pt.getY(), r);
	}	
	static public Point2D oppositeCorner(Point2D pt, StackPane r)
	{
		return oppositeCorner(pt.getX(), pt.getY(), r.getBoundsInLocal());
	}	
	static public Point2D oppositeCorner(Point2D pt, Bounds r)
	{
		return oppositeCorner(pt.getX(), pt.getY(), r);
	}	
	
	static public Point2D oppositeCorner(double inX, double inY, Rectangle r)
	{
		double outX = -1, outY = -1;
		double x = inX;
		double y = inY;
		double left = r.getX();
		double right = left + r.getWidth();
		double top = r.getY();
		double bottom = top + r.getHeight();
		if (Math.abs(x-left) < SLOP)  		outX = right;
		else if (Math.abs(x-right) < SLOP) 	outX = left;
		else return null;

		if (Math.abs(y-top) < SLOP)  		outY = bottom;
		else if (Math.abs(y-bottom) < SLOP) outY = top;
		else return null;
		
		return new Point2D(outX, outY);
	}
	
	static public Point2D oppositeCorner(double inX, double inY, Bounds r)
	{
		double outX = -1, outY = -1;
		double x = inX;
		double y = inY;
		double left = r.getMinX();
		double right = left + r.getWidth();
		double top = r.getMinY();
		double bottom = top + r.getHeight();
		if (Math.abs(x-left) < SLOP)  		outX = right;
		else if (Math.abs(x-right) < SLOP) 	outX = left;
		else return null;

		if (Math.abs(y-top) < SLOP)  		outY = bottom;
		else if (Math.abs(y-bottom) < SLOP) outY = top;
		else return null;
		
		return new Point2D(outX, outY);
	}
	
	
	
	public static Pos getPos(MouseEvent ev, Rectangle r)
	{
		double x = ev.getX();
		double y = ev.getY();
		double left = r.getX();
		double right = left + r.getWidth();
		double xCenter = (left + right) / 2;
		double top = r.getY();
		double bottom = top + r.getHeight();
		double yCenter = (top + bottom) / 2;
		if (Math.abs(x-left) < SLOP)
		{
			if ((y-top) < SLOP)			return Pos.TOP_LEFT;
			if ((y-yCenter) < SLOP)		return Pos.CENTER_LEFT;
			if ((y-bottom) < SLOP)		return Pos.BOTTOM_LEFT;
		}
		if (Math.abs(x-xCenter) < SLOP)
		{
			if ((y-top) < SLOP)			return Pos.TOP_CENTER;
			if ((y-yCenter) < SLOP)		return Pos.CENTER;
			if ((y-bottom) < SLOP)		return Pos.BOTTOM_CENTER;
		}
			
		if (Math.abs(x-right) < SLOP)
		{
			if ((y-top) < SLOP)			return Pos.TOP_RIGHT;
			if ((y-yCenter) < SLOP)		return Pos.CENTER_RIGHT;
			if ((y-bottom) < SLOP)		return Pos.BOTTOM_RIGHT;
		}
		return Pos.CENTER;
	}

	
	
	static double SLOP = 8;
	
	static public boolean inCorner(MouseEvent ev)
	{
		if (ev == null) return false;
		if (ev.getTarget() instanceof Rectangle)
			return inCorner(ev.getX(), ev.getY(), (Rectangle)ev.getTarget());
		if (ev.getTarget() instanceof StackPane)
			return inCorner(ev.getX(), ev.getY(), (StackPane)ev.getTarget());
		if (ev.getTarget() instanceof VBox)
			return inCorner(ev.getX(), ev.getY(), (VBox)ev.getTarget());
		
		return false;	
	}	
	
	static public boolean inCorner(Pos p)
	{
		if (p == Pos.TOP_LEFT  || p == Pos.BOTTOM_LEFT) 	return true;
		if (p == Pos.TOP_RIGHT || p == Pos.BOTTOM_RIGHT) 	return true;
		return false;
	}
	

	static public Point2D oppositeCorner(MouseEvent ev)
	{
		if (ev == null) return null;
		EventTarget target = ev.getTarget();
		if (target instanceof Rectangle)
			return RectangleUtil.oppositeCorner(ev.getX(), ev.getY(), (Rectangle)target);
		if (ev.getTarget() instanceof StackPane)
			return RectangleUtil.oppositeCorner(ev.getX(), ev.getY(), ((StackPane)target).getBoundsInLocal());
		if (ev.getTarget() instanceof VBox)
			return RectangleUtil.oppositeCorner(ev.getX(), ev.getY(), ((VBox)target).getBoundsInLocal());
		
		return null;	
	}	
	
	
	static public boolean inCorner(MouseEvent ev, Rectangle r)
	{
		return inCorner(ev.getX(), ev.getY(), r);
	}	
	static public boolean inCorner(Point2D pt, Rectangle r)
	{
		if (pt == null || r == null) return false;
		return inCorner(pt.getX(), pt.getY(), r);
	}	
	static public boolean inCorner(Point2D pt, StackPane r)
	{
		return inCorner(pt.getX(), pt.getY(), r.getBoundsInLocal());
	}	
	static public boolean inCorner(double inX, double inY, StackPane r)
	{
		return inCorner(inX, inY, r.getBoundsInLocal());
	}	
	
	static public boolean inCorner(double inX, double inY, VBox r)
	{
		return inCorner(inX, inY, r.getBoundsInLocal());
	}	
	static public boolean inCorner(Point2D pt, Region r)
	{
		if (pt == null || r == null) return false;
		return inCorner(pt.getX(), pt.getY(), r.getBoundsInLocal());
	}	

	static public boolean inCorner(double inX, double inY, Rectangle r)
	{
		double x =inX;
		double y = inY;
		double left = r.getX();
		double right = left + r.getWidth();
		double top = r.getY();
		double bottom = top + r.getHeight();
		if (Math.abs(x-left) < SLOP || Math.abs(x-right) < SLOP)
			if (Math.abs(y-top) < SLOP || Math.abs(y-bottom) < SLOP)
				return true;
		return false;
	}
	public static boolean inCorner(Point2D curPoint, Bounds bounds)
	{
		return inCorner(curPoint.getX(), curPoint.getY(), bounds);
	}
	
	public static boolean inCorner(double inX, double inY, Bounds bounds)
	{
		double left = bounds.getMinX();
		double right = left + bounds.getWidth();
		double top = bounds.getMinY();
		double bottom = top + bounds.getHeight();
		if (Math.abs(inX-left) < SLOP || Math.abs(inX-right) < SLOP)
			if (Math.abs(inY-top) < SLOP || Math.abs(inY-bottom) < SLOP)
				return true;
		return false;
	}

	public static void setupCursors(Rectangle r)
	{
		r.setOnMouseEntered(event -> 	{	r.setCursor(Cursors.getResizeCursor(getPos(event, r)));});
		r.setOnMouseMoved(event -> 		{	r.setCursor(Cursors.getResizeCursor(getPos(event, r)));	});
		r.setOnMouseExited(event -> 	{	r.setCursor(Cursor.DEFAULT);	});
	}
//	// **-------------------------------------------------------------------------------
//	// derive a rectangle that expresses how the kid lies within the parent
//	
//	public static Rectangle ratioRect(Rectangle parent, Rectangle kid)
//	{
//		assert(parent.getWidth() > 0 && kid.getHeight() > 0);
//		double relX = kid.getWidth() / parent.getWidth();
//		double relY = kid.getHeight() / parent.getHeight();
//		double ratioX = (kid.getX() - parent.getX()) * relX;
//		double ratioY = (kid.getY() - parent.getX()) * relX;
//		double ratioW = kid.getWidth() * relX;
//		double ratioH = kid.getHeight() * relY;
//		return new Rectangle(ratioX, ratioY, ratioW, ratioH);
//	}
//	
//	// **-------------------------------------------------------------------------------
//	// apply a ratio rectangle to derive the kid from the parent
//
//	public static Rectangle kidRect(Rectangle parent, Rectangle ratio)
//	{
//		double kidW = parent.getWidth() * ratio.getWidth();
//		double kidH = parent.getHeight() * ratio.getHeight();
//		double kidX = (parent.getX() + ratio.getX() * kidW);
//		double kidY = (parent.getY() + ratio.getY() * kidH);
//		return new Rectangle(kidX, kidY, kidW, kidH);
//	}


	}
