package de.hpi.bpt.scylla.plugin.gateway_exclusive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.dist.DiscreteDistEmpirical;
import desmoj.core.simulator.TimeSpan;

public class ExclusiveGatewayEventPlugin extends GatewayEventPluggable {

    @Override
    public String getName() {
        return ExclusiveGatewayPluginUtils.PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void eventRoutine(GatewayEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        SimulationModel model = (SimulationModel) desmojEvent.getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();

        boolean showInTrace = desmojEvent.traceIsOn();

        GatewayType type = processModel.getGateways().get(nodeId);
        ProcessSimulationComponents desmojObjects = desmojEvent.getDesmojObjects();

        try {
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);

            if (idsOfNextNodes.size() > 1) { // split
                if (type == GatewayType.DEFAULT || type == GatewayType.EXCLUSIVE || type == GatewayType.EVENT_BASED) {
                    Map<Integer, Object> branchingDistributions = desmojObjects.getExtensionDistributions()
                            .get(getName());
                    DiscreteDistEmpirical<Integer> distribution = (DiscreteDistEmpirical<Integer>) branchingDistributions
                            .get(nodeId);
                    // decide on next node
                    Integer nextFlowId = distribution.sample().intValue();
                    if (!processModel.getIdentifiers().keySet().contains(nextFlowId)) {
                        throw new ScyllaValidationException("Flow with id " + nextFlowId + " does not exist.");
                    }
                    Set<Integer> nodeIds = processModel.getTargetObjectIds(nextFlowId);
                    if (nodeIds.size() != 1) {
                        throw new ScyllaValidationException(
                                "Flow " + nextFlowId + " does not connect to 1 node, but" + nodeIds.size() + " .");
                    }
                    int nextNodeId = nodeIds.iterator().next();

                    Map<Integer, ScyllaEvent> nextEventMap = desmojEvent.getNextEventMap();
                    List<Integer> indicesOfEventsToKeep = new ArrayList<Integer>();
                    for (int index : nextEventMap.keySet()) {
                        ScyllaEvent eventCandidate = nextEventMap.get(index);
                        int nodeIdOfCandidate = eventCandidate.getNodeId();
                        if (nodeIdOfCandidate == nextNodeId) {
                            indicesOfEventsToKeep.add(index);
                            break;
                        }
                    }
                    Map<Integer, TimeSpan> timeSpanToNextEventMap = desmojEvent.getTimeSpanToNextEventMap();
                    nextEventMap.keySet().retainAll(indicesOfEventsToKeep);
                    timeSpanToNextEventMap.keySet().retainAll(indicesOfEventsToKeep);

                    // TODO default flow on data dependencies
                }
            }
        }
        catch (NodeNotFoundException | ScyllaValidationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
            return;
        }
    }

}
