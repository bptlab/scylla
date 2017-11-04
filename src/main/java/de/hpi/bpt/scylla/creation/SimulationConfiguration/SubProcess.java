package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;

public class SubProcess extends Task{

	public SubProcess(String id, String name) {
		super(id, name);
		el.setName("subProcess");
	}
	
	protected SubProcess(Element toLink, GlobalConfigurationCreator gcc){
		super(toLink,gcc);
	}

}
