package de.hpi.bpt.scylla.plugin.batch;

import java.util.List;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.MultipleStartNodesException;
import de.hpi.bpt.scylla.model.process.graph.exception.NoStartNodeException;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class BatchClusterStartEvent extends Event<BatchCluster> {

    public BatchClusterStartEvent(Model owner, String name, boolean showInTrace) {
        super(owner, "BCStart_" + name, showInTrace);
    }

    @Override
    public void eventRoutine(BatchCluster cluster) throws SuspendExecution {

        BatchActivity activity = cluster.getBatchActivity();
        int nodeId = activity.getNodeId();

        List<TaskBeginEvent> parentalStartEvents = cluster.getParentalStartEvents();


        // Schedule all task begin events of the process instance
        for (TaskBeginEvent pse : parentalStartEvents) {
            ProcessInstance pi = pse.getProcessInstance();
            pse.schedule(pi);
        }

        // schedule subprocess start events for all process instances in parent
        // processInstances and parentalStartEvents are ordered the same way

        // Set the responsible process instance in the batch cluster, first one by default
        cluster.setResponsibleProcessInstance(parentalStartEvents.get(0).getProcessInstance());

        // Go through all process instances. If it's the first one, schedule it. If not, save it to be scheduled later on
        for (int j = 0; j < parentalStartEvents.size(); j++) {
            TaskBeginEvent startEvent = parentalStartEvents.get(j);
            ProcessInstance responsibleProcessInstance = startEvent.getProcessInstance();


            int processInstanceId = responsibleProcessInstance.getId();
            boolean showInTrace = responsibleProcessInstance.traceIsOn();
            SimulationModel model = (SimulationModel) responsibleProcessInstance.getModel();
            String source = startEvent.getSource();
            TimeInstant currentSimulationTime = cluster.presentTime();

            ProcessSimulationComponents pSimComponentsOfSubprocess = cluster.getProcessSimulationComponents().getChildren()
                    .get(nodeId);
            ProcessModel subprocess = pSimComponentsOfSubprocess.getProcessModel();

            try {
                Integer startNodeId = subprocess.getStartNode();
                ProcessInstance subprocessInstance = new ProcessInstance(model, subprocess, processInstanceId, showInTrace);
                subprocessInstance.setParent(responsibleProcessInstance);

                ScyllaEvent subprocessEvent = new BPMNStartEvent(model, source, currentSimulationTime,
                        pSimComponentsOfSubprocess, subprocessInstance, startNodeId);
                //System.out.println("Created BPMNStartEvent for PI " + subprocessInstance.getId() + " / " + responsibleProcessInstance.getId() + " in Batch Cluster");

                if (j == 0) { // If it is the first process instance, schedule it...
                    subprocessEvent.schedule(subprocessInstance);
                    cluster.setStartNodeId(startNodeId);
                } else { // ...if not, save them for later
                    cluster.addPIEvent(startNodeId, subprocessEvent, subprocessInstance);
                }

            } catch (NodeNotFoundException | MultipleStartNodesException | NoStartNodeException e) {
                DebugLogger.log("Start node of process model " + subprocess.getId() + " not found.");
                System.err.println(e.getMessage());
                e.printStackTrace();
                SimulationUtils.abort(model, responsibleProcessInstance, nodeId, traceIsOn());
                return;
            }
        }
        // move batch cluster from list of not started ones to running ones
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.setClusterToRunning(cluster);

        // next node and timespan to next event determined by responsible process instance
        // tasks resources only assigned to responsible subprocess instance

        // only responsible subprocess instance is simulated
        // other subprocess instances of batch are covered in process logs
    }

}
