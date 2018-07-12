package org.cytoscape.cyChart.internal.model;

import javafx.scene.web.WebEngine;

import netscape.javascript.JSObject;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.apache.log4j.Logger;


public class NetworkAddedListenerJS extends JSListener implements NetworkAddedListener {

	NetworkAddedListenerJS(WebEngine engine, String callback) {
		super(engine, callback);
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		CyNetwork network = e.getNetwork();
		doCallback(callback, "{\"suid\":"+network.getSUID()+"}");
	}
}
