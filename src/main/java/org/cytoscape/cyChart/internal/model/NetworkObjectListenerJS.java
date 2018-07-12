package org.cytoscape.cyChart.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import netscape.javascript.JSObject;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AboutToRemoveNodesEvent;
import org.cytoscape.model.events.AboutToRemoveNodesListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.apache.log4j.Logger;


public class NetworkObjectListenerJS extends JSListener 
                                     implements RowsSetListener,
                                                AboutToRemoveEdgesListener, 
                                                AboutToRemoveNodesListener {
	CyTable table = null;
	CyNetwork network;
	Class<? extends CyIdentifiable> type;

	NetworkObjectListenerJS(WebEngine engine, CyNetwork network, String callback, Class<? extends CyIdentifiable> type) {
		super(engine, callback);
		if (network != null)
			table = network.getTable(type, CyNetwork.LOCAL_ATTRS);
		this.network = network;
		this.type = type;
	}

	@Override
	public void handleEvent(RowsSetEvent e) {
		if (!e.containsColumn(CyNetwork.SELECTED)) return;
		if (table != null && e.getSource() != table) return;
		List<? extends CyIdentifiable> selection;
		if (type.equals(CyNode.class))
	 		selection = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		else if (type.equals(CyEdge.class))
	 		selection = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		else
			return;

		String s = getJSON(selection);
		doCallback(callback, s);
	}

	@Override
	public void handleEvent(AboutToRemoveNodesEvent e) {
		Collection<CyNode> nodes = e.getNodes();
		String s = getJSON(new ArrayList<CyNode>(nodes));
		doCallback(callback, s);
	}

	@Override
	public void handleEvent(AboutToRemoveEdgesEvent e) {
		Collection<CyEdge> edges = e.getEdges();
		String s = getJSON(new ArrayList<CyEdge>(edges));
		doCallback(callback, s);
	}

	private String getJSON(List<? extends CyIdentifiable> ids) {
		String s = "[";
		for (CyIdentifiable id: ids) {
			s += toJSON(id)+",";
		}
		if (s.length() > 1)
			s = s.substring(0, s.length()-1);
		s += "]";
		return s;
	}

	private String toJSON(CyIdentifiable id) {
		return "{\"suid\":"+id.getSUID()+",\"name\":\""+network.getRow(id).get(CyNetwork.NAME, String.class)+"\"}";
	}
}
