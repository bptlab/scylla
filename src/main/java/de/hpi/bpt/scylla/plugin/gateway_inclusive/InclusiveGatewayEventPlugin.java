package de.hpi.bpt.scylla.plugin.gateway_inclusive;

import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.GatewayType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.GatewayEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.GatewayEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;

public class InclusiveGatewayEventPlugin extends GatewayEventPluggable {

    @Override
    public String getName() {
        return InclusiveGatewayPluginUtils.PLUGIN_NAME;
    }

    // @SuppressWarnings("unchecked")
    @Override
    public void eventRoutine(GatewayEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        SimulationModel model = (SimulationModel) desmojEvent.getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();

        boolean showInTrace = desmojEvent.traceIsOn();

        GatewayType type = processModel.getGateways().get(nodeId);
        // ProcessSimulationComponents desmojObjects = desmojEvent.getDesmojObjects();

        try {
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);

            if (idsOfNextNodes.size() > 1) { // split
                if (type == GatewayType.INCLUSIVE) {
                    // TODO this is incomplete, must find corresponding join gateway and define behavior for it
                    // Map<Integer, Object> branchingDistributions = desmojObjects.getDistributionsExtensional()
                    // .get(getName());
                    // Map<Integer, BoolDistBernoulli> distributions = (Map<Integer, BoolDistBernoulli>)
                    // branchingDistributions
                    // .get(nodeId);
                    // Set<Integer> idsOfNodesToBeActivated = new HashSet<Integer>();
                    // for (Integer nextFlowId : distributions.keySet()) {
                    // BoolDistBernoulli distribution = distributions.get(nextFlowId);
                    // if (!processModel.getIdentifiers().keySet().contains(nextFlowId)) {
                    // throw new ScyllaValidationException("Flow with id " + nextFlowId + " does not exist.");
                    // }
                    // boolean activateOutgoingFlow = distribution.sample();
                    // if (activateOutgoingFlow) {
                    // Set<Integer> nodeIds = processModel.getTargetObjectIds(nextFlowId);
                    // if (nodeIds.size() != 1) {
                    // throw new ScyllaValidationException("Flow " + nextFlowId
                    // + " does not connect to 1 node, but" + nodeIds.size() + " .");
                    // }
                    // int nextNodeId = nodeIds.iterator().next();
                    //
                    // idsOfNodesToBeActivated.add(nextNodeId);
                    // }
                    // // TODO default flow
                    // }
                    //
                    // Map<Integer, ScyllaEvent> nextEventMap = desmojEvent.getNextEventMap();
                    // List<Integer> indicesOfEventsToKeep = new ArrayList<Integer>();
                    // for (int index : nextEventMap.keySet()) {
                    // ScyllaEvent eventCandidate = nextEventMap.get(index);
                    // int nodeIdOfCandidate = eventCandidate.getNodeId();
                    // if (idsOfNodesToBeActivated.contains(nodeIdOfCandidate)) {
                    // indicesOfEventsToKeep.add(index);
                    // }
                    // }
                    // Map<Integer, TimeSpan> timeSpanToNextEventMap = desmojEvent.getTimeSpanToNextEventMap();
                    // nextEventMap.keySet().retainAll(indicesOfEventsToKeep);
                    // timeSpanToNextEventMap.keySet().retainAll(indicesOfEventsToKeep);
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
