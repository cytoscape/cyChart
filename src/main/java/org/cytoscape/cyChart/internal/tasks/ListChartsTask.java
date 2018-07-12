package org.cytoscape.cyChart.internal.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.cyChart.internal.model.CyChart;
import org.cytoscape.cyChart.internal.model.CyChartManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

public class ListChartsTask extends AbstractTask implements ObservableTask {

	final CyChartManager manager;

	public ListChartsTask(CyChartManager manager) {
		this.manager = manager;
	}

	public void run(TaskMonitor monitor) {
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class, List.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> type) {
		Map<String, CyChart> map = manager.getChartMap();
		if (type.equals(String.class)) {
			String res = "";
			if (map != null) {
				for (String id: map.keySet()) {
					CyChart b = map.get(id);
					res += id+": ";
					if (b.getTitle(id) != null) res += b.getTitle(id)+" ";
//					if (b.getURL(id) != null) res += "("+b.getURL(id)+") ";
					res += "\n";
				}
			}
			return (R)res;
		} else if (type.equals(JSONResult.class)) {
			JSONResult res = () -> { 
				if (map == null) 
					return "{}"; 

				String jsonRes = "[";
				for (String id: map.keySet()) {
					CyChart b = map.get(id);
					jsonRes += "{\"id\": "+id;
					if (b.getTitle(id) != null) jsonRes += ", \"title\":\""+b.getTitle(id)+"\"";
//					if (b.getURL(id) != null) jsonRes += ", \"url\":\""+b.getURL(id)+"\"";
					jsonRes += "}\n";
				}
				jsonRes += "]";
				return jsonRes;
			};
			return (R)res;
		} else if (type.equals(List.class)) {
			return (R)new ArrayList<String>(map.keySet());
		}
		return null;
	}

}
