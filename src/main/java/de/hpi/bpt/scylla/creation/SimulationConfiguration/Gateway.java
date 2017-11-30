package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.List;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

/**
 * Basic abstract class for gateways, to be extended; 
 * allows for new gateways to determine their id, type and branch list (defined in the process model)
 * @author Leon Bein
 *
 */
public abstract class Gateway extends ElementLink{
	
	

	/**
	 * Link constructor; Links with given xml element and calls overridden {@link #init()} method
	 * @param toLink
	 */
	protected Gateway(Element toLink) {
		super(toLink);
		init();
	}
	
	/**
	 * Constructor, id, type and initial branches and calls overridden {@link #init()} method before
	 * @param id : Gateway id
	 * @param type : Gateway type
	 * @param branches : Set of branches, added by {@link #addBranch(String)}
	 */
	public Gateway(String id, String type, List<String> branches){
		super(new Element(type,stdNsp));
		init();
		setAttribute("id", id);
		for(String s : branches){
			addBranch(s);
		}
	}
	
	/**
	 * Overridable method to add "outgoingSequenceFlow" element branches with given id
	 * @param id : Id for the branch
	 * @return The element that has been created
	 */
	protected Element addBranch(String id){
		Element branch = new Element("outgoingSequenceFlow",stdNsp);
		branch.setAttribute("id", id);
		el.addContent(branch);
		return branch;
	}
	
	/**
	 * Method that can be overridden in order to initialize fields that might be used in the superconstructor
	 * e.g. in an overridden addBranch method.
	 */
	protected void init(){}

}
