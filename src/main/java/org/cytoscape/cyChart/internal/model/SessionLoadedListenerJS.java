package org.cytoscape.cyChart.internal.model;

import javafx.scene.web.WebEngine;

import netscape.javascript.JSObject;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import org.apache.log4j.Logger;


public class SessionLoadedListenerJS extends JSListener implements SessionLoadedListener {

	SessionLoadedListenerJS(WebEngine engine, String callback) {
		super(engine, callback);
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		doCallback(callback, null);
	}
}
