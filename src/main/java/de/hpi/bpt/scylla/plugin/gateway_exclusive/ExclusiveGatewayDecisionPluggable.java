package de.hpi.bpt.scylla.plugin.gateway_exclusive;

import java.util.Iterator;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_loader.Requires;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;

@Requires(ExclusiveGatewayEventPlugin.class)
public abstract class ExclusiveGatewayDecisionPluggable implements IPluggable{
	
	
	public static Integer runPlugins(GatewayEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
    	Iterator<? extends ExclusiveGatewayDecisionPluggable> plugins = PluginLoader.dGetPlugins(ExclusiveGatewayDecisionPluggable.class);
    	Integer currentlyChosen = null;
        while (plugins.hasNext()) {
        	Integer decision = plugins.next().decideGateway(desmojEvent, processInstance, currentlyChosen);
        	if(decision != null) currentlyChosen = decision;
        }
        return currentlyChosen;
	}
	
	/**
	 * Decide which path/flow to choose at an exclusive gateway.
	 * @param desmojEvent : The gateway event
	 * @param processInstance : Instance the event is occuring
	 * @param currentlyChosen : The current decision if another plugin has already made one. It is recommended to not override this decision if not necessary
	 * @return Id of next flow node decision or null if no decision is made
	 * @throws ScyllaRuntimeException
	 */
	public abstract Integer decideGateway(GatewayEvent desmojEvent, ProcessInstance processInstance, Integer currentlyChosen) throws ScyllaRuntimeException;

}
