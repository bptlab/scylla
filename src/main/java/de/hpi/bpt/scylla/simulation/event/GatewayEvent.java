package de.hpi.bpt.scylla.simulation.event;

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
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event representing BPMN gateways.
 * 
 * @author Tsun Yin Wong
 *
 */
public class GatewayEvent extends ScyllaEvent {

    // private Set<Integer> nodesTriggeredFrom = new HashSet<Integer>();

    public GatewayEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();

        try {
            Set<Integer> idsOfPreviousNodes = processModel.getIdsOfPreviousNodes(nodeId);
            Set<Integer> idsOfNextNodes = processModel.getIdsOfNextNodes(nodeId);

            String message = null;
            GatewayType type = processModel.getGateways().get(nodeId);

            String convergeDivergeName = "Join and Split";
            if (idsOfPreviousNodes.size() == 1) {
                convergeDivergeName = "Split";
            }
            else if (idsOfNextNodes.size() == 1) {
                convergeDivergeName = "Join";
            }

            if (type == GatewayType.DEFAULT) {
                message = "Default " + convergeDivergeName + " Gateway: " + displayName;
            }
            if (type == GatewayType.EXCLUSIVE) {
                message = "Exclusive " + convergeDivergeName + " Gateway: " + displayName;
            }
            else if (type == GatewayType.EVENT_BASED) {
                message = "Eventbased " + convergeDivergeName + " Gateway: " + displayName;
            }
            else if (type == GatewayType.INCLUSIVE) {
                message = "Inclusive " + convergeDivergeName + " Gateway: " + displayName;
            }
            else if (type == GatewayType.PARALLEL) {
                message = "Parallel " + convergeDivergeName + " Gateway: " + displayName;
            }
            else if (type == GatewayType.COMPLEX) {
                message = "Complex " + convergeDivergeName + " Gateway: " + displayName;
            }
            else {
                // TODO write to log because element not supported
                SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
                return;
            }

            sendTraceNote(message);

            // default behavior: prepare DesmoJ events for nodes that follow after all outgoing flows
            // i.e. for splits: default behavior is the one of a parallel gateway
            for (Integer nextNodeId : idsOfNextNodes) {
                List<ScyllaEvent> events = SimulationUtils.createEventsForNextNode(this, pSimComponents,
                        processInstance, nextNodeId);

                // next DesmoJ event occurs immediately after start event
                TimeSpan timeSpan = new TimeSpan(0);

                for (ScyllaEvent event : events) {
                    int index = getNewEventIndex();
                    nextEventMap.put(index, event);
                    timeSpanToNextEventMap.put(index, timeSpan);
                }
            }

            GatewayEventPluggable.runPlugins(this, processInstance);

            scheduleNextEvents();
        }
        catch (NodeNotFoundException | ScyllaValidationException | ScyllaRuntimeException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, processInstance, nodeId, traceIsOn());
            return;
        }
    }

    // public Set<Integer> getNodesTriggeredFrom() {
    // return nodesTriggeredFrom;
    // }
}
