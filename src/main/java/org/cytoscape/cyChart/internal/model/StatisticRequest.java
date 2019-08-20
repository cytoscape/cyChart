package org.cytoscape.cyChart.internal.model;

public class StatisticRequest
{
		String population;
		Statistic stat;
		double value;
		
	public enum Statistic 	{ MEAN, MEDIAN, CV, ROBUSTCV, STDEV };

	public StatisticRequest(String pop)
	{
		population = pop;
	}
	public String getPopulation()	{ return population;	}
}
