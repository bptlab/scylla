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
    public void eventRoutine(BatchCluster bc) throws SuspendExecution {

        BatchRegion region = bc.getBatchRegion();
        int nodeId = region.getNodeId();

        List<TaskBeginEvent> parentalStartEvents = bc.getParentalStartEvents();
        TaskBeginEvent parentalStartEvent = parentalStartEvents.get(0); // first one by default
        ProcessInstance responsibleProcessInstance = parentalStartEvent.getProcessInstance(); // first one by default

        // schedule subprocess start events for all process instances in parent
        // processInstances and parentalStartEvents are ordered the same way

        for (int i = 0; i < parentalStartEvents.size(); i++) {
            TaskBeginEvent pse = parentalStartEvents.get(i);
            ProcessInstance pi = pse.getProcessInstance();
            pse.schedule(pi);
        }

        // schedule first event of responsible process instance

        int processInstanceId = responsibleProcessInstance.getId();
        boolean showInTrace = responsibleProcessInstance.traceIsOn();
        SimulationModel model = (SimulationModel) responsibleProcessInstance.getModel();
        String source = parentalStartEvent.getSource();
        TimeInstant currentSimulationTime = bc.presentTime();

        ProcessSimulationComponents pSimComponentsOfSubprocess = bc.getProcessSimulationComponents().getChildren()
                .get(nodeId);
        ProcessModel subprocess = pSimComponentsOfSubprocess.getProcessModel();

        try {
            Integer startNodeId = subprocess.getStartNode();
            ProcessInstance subprocessInstance = new ProcessInstance(model, subprocess, processInstanceId, showInTrace);
            subprocessInstance.setParent(responsibleProcessInstance);
            ScyllaEvent subprocessEvent = new BPMNStartEvent(model, source, currentSimulationTime,
                    pSimComponentsOfSubprocess, subprocessInstance, startNodeId);
            subprocessEvent.schedule(subprocessInstance);
        }
        catch (NodeNotFoundException | MultipleStartNodesException | NoStartNodeException e) {
            DebugLogger.log("Start node of process model " + subprocess.getId() + " not found.");
            System.err.println(e.getMessage());
            e.printStackTrace();
            SimulationUtils.abort(model, responsibleProcessInstance, nodeId, traceIsOn());
            return;
        }

        // move batch cluster from list of not started ones to running ones
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.setClusterToRunning(bc);

        // next node and timespan to next event determined by responsible process instance
        // tasks resources only assigned to responsible subprocess instance

        // only responsible subprocess instance is simulated
        // other subprocess instances of batch are covered in process logs
    }

}
