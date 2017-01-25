package de.hpi.bpt.scylla.plugin.dmn;

import java.util.LinkedList;
import java.util.List;

public class DmnStatsInfo {
	private List<Decision> decisions = new LinkedList<Decision>();
	private String outputs;
	private boolean hasDecisionFailure = false;
	
	private double totalDuration = 0;
    private double totalCost = 0;
    
    public void addDecisions(List<Decision> decisions) {
		this.decisions.addAll(decisions);
	}
	
	public List<Decision> getDecisions(){
		return decisions;
	}

	public double getAverageDuration() {
		return totalDuration / decisions.size();
	}

	public void updateDuration(long currentDuration) {
		totalDuration += currentDuration;
	}

	public double getAverageCosts() {
		return totalCost / decisions.size();
	}

	public void updateCosts(double currentCosts) {
		totalCost += currentCosts;
	}

	public String getOutputs() {
		return outputs;
	}

	public void setOutputs(String outputs) {
		this.outputs = outputs;
	}

	public int getFrequency() {
		return decisions.size();
	}
	
	public void setDecisionFailure() {
		hasDecisionFailure = true;
	}
	
	public boolean hasDecisionFailure() {
		return hasDecisionFailure;
	}
	
}
