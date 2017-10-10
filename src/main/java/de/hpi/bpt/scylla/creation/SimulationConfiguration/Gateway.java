package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import java.util.List;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.ElementLink;

public abstract class Gateway extends ElementLink{
	
	

	public Gateway(Element toLink) {
		super(toLink);
		// TODO create linking constructor
	}
	
	public Gateway(String id, String type, List<String> branches){
		super(new Element(type,stdNsp));
		for(String s : branches){
			addBranch(s);
		}
	}

	public String getId(){return el.getAttributeValue("id");}
	public Element addBranch(String id){
		Element branch = new Element("outgoingSequenceFlow",stdNsp);
		branch.setAttribute("id", id);
		el.addContent(branch);
		return branch;
	}

}
