package de.hpi.bpt.scylla.simulation.event;

import java.util.HashSet;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Abstract class for DesmoJ events representing BPMN events.
 * 
 * @author Tsun Yin Wong
 *
 */
public abstract class BPMNEvent extends ScyllaEvent {

    public BPMNEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    protected void addToLog(ProcessInstance processInstance) {
        long timestamp = Math.round(getModel().presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit()));
        String taskName = displayName;
        Set<String> resources = new HashSet<String>();

        SimulationModel model = (SimulationModel) getModel();
        ProcessModel processModel = processInstance.getProcessModel();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);

        ProcessNodeInfo info;
        info = new ProcessNodeInfo(processScopeNodeId, source, timestamp, taskName, resources,
                ProcessNodeTransitionType.EVENT_BEGIN);
        model.addNodeInfo(processModel, processInstance, info);

        info = new ProcessNodeInfo(processScopeNodeId, source, timestamp, taskName, resources,
                ProcessNodeTransitionType.EVENT_TERMINATE);
        model.addNodeInfo(processModel, processInstance, info);
    };

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        SimulationModel model = (SimulationModel) getModel();
        boolean outputLoggingIsOn = model.isOutputLoggingOn();
        if (outputLoggingIsOn) {
            addToLog(processInstance);
        }
    }

}