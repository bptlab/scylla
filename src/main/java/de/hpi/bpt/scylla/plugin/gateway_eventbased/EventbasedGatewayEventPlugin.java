package de.hpi.bpt.scylla.plugin.gateway_eventbased;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.TimeInstant;

public class EventbasedGatewayEventPlugin extends GatewayEventPluggable{

	@Override
	public String getName() {
		return EventbasedGatewayPluginUtils.PLUGIN_NAME;
	}

	@Override
	public void eventRoutine(GatewayEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {

		SimulationModel model = (SimulationModel) desmojEvent.getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();
        GatewayType type = processModel.getGateways().get(nodeId);
        try {
			Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);
			if(type == GatewayType.EVENT_BASED && idsOfNextNodes.size() > 1) {

				//Schedule all following events
				List<ScyllaEvent> nextEvents = new ArrayList<ScyllaEvent>(desmojEvent.getNextEventMap().values());
				desmojEvent.scheduleNextEvents();
				//and look which is scheduled first.
				ScyllaEvent first = nextEvents.get(0);
				for(ScyllaEvent e : nextEvents) {
//					System.out.println(e.getName()+" "+e.getDisplayName()+" "+e.scheduledNext());
					if(TimeInstant.isBefore(e.scheduledNext(), first.scheduledNext())) {
						first = e;
					}
				}
				//Cancel all other events except the one that is scheduled first.
				nextEvents.remove(first);
				for(ScyllaEvent e : nextEvents) {
					e.cancel();
				}
			}
		} catch (NodeNotFoundException | ScyllaValidationException | SuspendExecution e) {
			e.printStackTrace();
			//Critical error (following nodes not found or validation error), abort the instance.
            SimulationUtils.abort(model, processInstance, nodeId, desmojEvent.traceIsOn());
		}
	}

}
