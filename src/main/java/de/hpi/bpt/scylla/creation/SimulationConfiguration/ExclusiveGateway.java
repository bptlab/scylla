package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

public class ExclusiveGateway extends Gateway{
	
	private Map<String,Element> branches;


	public ExclusiveGateway(Element toLink) {
		super(toLink);
		for(Element branch : el.getChildren("outgoingSequenceFlow",stdNsp)){
			branches.put(branch.getAttributeValue("id"), branch);
		}
	}
	
	public ExclusiveGateway(String id, List<String> branches){
		super(id,"exclusiveGateway",branches);
	}
	
	@Override
	protected Element addBranch(String id){
		Element branch = super.addBranch(id);
		branch.addContent(new Element("branchingProbability", stdNsp));
		branches.put(id,branch);
		return branch;
	}
	
	@Override
	protected void init(){
		branches = new HashMap<String,Element>();
	}
	
	public void setBranchingProbability(String branchId, double value){
		if(branches.containsKey(branchId))branches.get(branchId).getChild("branchingProbability", stdNsp).setText(value+"");
	}
	
	public String getBranchingProbability(String branchId){
		if(branches.containsKey(branchId))return branches.get(branchId).getChild("branchingProbability", stdNsp).getValue();
		else return null;
	}
	
	public Set<String> getBranches(){
		return branches.keySet();
	}
	

}
