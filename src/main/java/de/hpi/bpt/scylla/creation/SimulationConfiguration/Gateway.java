package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.List;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

public abstract class Gateway extends ElementLink{
	
	

	protected Gateway(Element toLink) {
		super(toLink);
		init();
	}
	
	public Gateway(String id, String type, List<String> branches){
		super(new Element(type,stdNsp));
		init();
		setAttribute("id", id);
		for(String s : branches){
			addBranch(s);
		}
	}

	public String getId(){return el.getAttributeValue("id");}
	
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
