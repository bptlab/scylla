package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

public class ExclusiveGateway extends Gateway{
	
	private Map<String,Element> branches = new HashMap<String,Element>();

	public ExclusiveGateway(Element toLink) {
		super(toLink);
		// TODO create linking constructor
	}
	
	public ExclusiveGateway(String id, List<String> branches){
		super(id,"exclusiveGateway",branches);
	}
	
	@Override
	public Element addBranch(String id){
		Element branch = super.addBranch(id);
		branches.put(id,branch);
		return branch;
	}
	
	public void setBranchingProbability(String branchId, double value){
		branches.get(branchId).setAttribute("branchingProbability", value+"");
	}
	

}
