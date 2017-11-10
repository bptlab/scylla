package de.hpi.bpt.scylla.creation.SimulationConfiguration;

import org.jdom2.Element;

import de.hpi.bpt.scylla.creation.GlobalConfiguration.GlobalConfigurationCreator;

/**
 * Wrapper class for subprocesses, to distinguish them from normal tasks
 * @author Leon Bein
 *
 */
public class SubProcess extends Task{

	/**@see {@link Task#Task(String, String)}*/
	public SubProcess(String id, String name) {
		super(id, name);
		el.setName("subProcess");
	}
	
	/**@see {@link Task#Task(Element, GlobalConfigurationCreator)}*/
	protected SubProcess(Element toLink, GlobalConfigurationCreator gcc){
		super(toLink,gcc);
	}

}
