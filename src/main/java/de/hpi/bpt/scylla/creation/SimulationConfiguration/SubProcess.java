package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import org.jdom2.Element;

public class SubProcess extends Task{

	public SubProcess(String id, String name) {
		super(id, name);
	}
	
	protected SubProcess(Element toLink){
		super(toLink);
		// TODO create linking constructor
	}

}
