package de.hpi.bpt.scylla.plugin.statslogger_nojar;

import java.util.ArrayList;

public class StatisticsCalculationObject {
	
	 ArrayList<Double> values = new ArrayList<Double>();
	private double min; // defaultwerte?
	private double max;
	private double median;
	private double q1;
	private double q3;
	private double total = 0.0;
	private double average;

	public void addValue(double value) {
		this.values.add(value);
	}
	
	public void calculateStatistics() {
		if (!values.isEmpty()) {			
			values.sort(null);
			this.min = values.get(0);
			this.max = values.get(values.size() - 1);
			this.median = values.get((int) Math.ceil(values.size() / 2.0) - 1);
			this.q1 = values.get((int) Math.ceil(values.size() / 4.0) - 1);
			this.q3 = values.get((int) Math.ceil(3 * values.size() / 4.0) - 1);
			for(double value : values) this.total += value;
			this.average = total / values.size();
		}
	}
	
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }

    public double getMedian() {
        return median;
    }

    public double getQ1() {
        return q1;
    }

    public double getQ3() {
        return q3;
    }
    
    public double getTotal() {
        return total;
    }

    public double getAverage() {
        return average;
    }
}
