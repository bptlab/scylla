package de.hpi.bpt.scylla.plugin.batch;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin.gateway_exclusive.ExclusiveGatewayEventPlugin;
import de.hpi.bpt.scylla.plugin_loader.TemporalDependent;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;

@TemporalDependent(ExclusiveGatewayEventPlugin.class)
public class BatchGatewayPlugin extends GatewayEventPluggable{

	@Override
	public String getName() {
		return BatchPluginUtils.PLUGIN_NAME;
	}

	@Override
	public void eventRoutine(GatewayEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {
		BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        
        BatchCluster cluster = pluginInstance.getCluster(processInstance);
        if(cluster != null)cluster.gatewayEvent(event);
	}

}
