package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

/**
 * Wrapper class for exclusive gateway elements
 * Note: Although Gateways belong to plugins, their standard behavior is defined here because of their essential nature
 * Note2: X Gateways expand "normal" {@link Gateway}s by defining disjoint probabilities for each of their branches
 * @author Leon Bein
 *
 */
public class ExclusiveGateway extends Gateway{
	
	/**Map of "outgoingSequenceFlow" branch elements and their ids*/
	private Map<String,Element> branches;


	/**
	 * Link constructor, links a new object to an existing element and parses all outgoing branches
	 * @param toLink : XML element to link with
	 */
	public ExclusiveGateway(Element toLink) {
		super(toLink);
		for(Element branch : el.getChildren("outgoingSequenceFlow",stdNsp)){
			branches.put(branch.getAttributeValue("id"), branch);
		}
	}
	
	/**
	 * Constructor to create a new object and element, sets the gateways type to X
	 * @param id : Id of gateway
	 * @param branches : List of branches, as defined in the corresponding process model
	 */
	public ExclusiveGateway(String id, List<String> branches){
		super(id,"exclusiveGateway",branches);
	}
	
	/**
	 * Calls super addBranch but adds a probability element to each branch and logs their ids.
	 */
	@Override
	protected Element addBranch(String id){
		Element branch = super.addBranch(id);
		branch.addContent(new Element("branchingProbability", stdNsp));
		branches.put(id,branch);
		return branch;
	}
	
	/**
	 * Inits the {@link #branches} map
	 */
	@Override
	protected void init(){
		branches = new HashMap<String,Element>();
	}
	
	/**
	 * Sets the branching probability for a branch.
	 * Does not check for validity (negative numbers or sums that are not equal one are allowed!)
	 * @param branchId : Id of the branch
	 * @param value : Probability to be set
	 */
	public void setBranchingProbability(String branchId, double value){
		if(branches.containsKey(branchId))branches.get(branchId).getChild("branchingProbability", stdNsp).setText(value+"");
	}
	
	/**
	 * Returns the branching probability for a branch 
	 * @param branchId : The branches id.
	 * @return
	 */
	public String getBranchingProbability(String branchId){
		if(branches.containsKey(branchId))return branches.get(branchId).getChild("branchingProbability", stdNsp).getValue();
		else return null;
	}
	
	/**
	 * @return All branch ids, but not their corresponding xml elements
	 */
	public Set<String> getBranches(){
		return branches.keySet();
	}
	

}
