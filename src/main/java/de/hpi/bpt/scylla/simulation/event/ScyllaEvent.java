package de.hpi.bpt.scylla.simulation.event;

import java.util.HashMap;
import java.util.Map;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Abstract class for DesmoJ events representing business process simulation.
 * 
 * @author Tsun Yin Wong
 *
 */
public abstract class ScyllaEvent extends Event<ProcessInstance> {

    protected String source;
    protected TimeInstant simulationTimeOfSource;

    protected ProcessSimulationComponents pSimComponents;
    protected ProcessInstance processInstance;
    protected int nodeId;
    protected String displayName;

    protected Map<Integer, ScyllaEvent> nextEventMap = new HashMap<Integer, ScyllaEvent>();
    protected Map<Integer, TimeSpan> timeSpanToNextEventMap = new HashMap<Integer, TimeSpan>();

    private int nextEventIndex = 0;

    public ScyllaEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, buildEventName(processInstance.getProcessModel(), processInstance.getId(), nodeId),
                owner.traceIsOn());
        this.source = source;
        this.simulationTimeOfSource = simulationTimeOfSource;
        this.pSimComponents = desmojObjects;
        this.processInstance = processInstance;
        this.nodeId = nodeId;
        this.displayName = processInstance.getProcessModel().getDisplayNames().get(nodeId);
        if (displayName == null) {
            this.displayName = processInstance.getProcessModel().getIdentifiers().get(nodeId);
        }
    }

    public static String buildEventName(ProcessModel processModel, int processInstanceId, int nodeId) {
        return prependProcessModelIds(processModel) + "PI" + processInstanceId + "_N" + nodeId;
    }

    private static String prependProcessModelIds(ProcessModel processModel) {
        if (processModel.getParent() == null) {
            return processModel.getId() + "_";
        }
        return prependProcessModelIds(processModel.getParent()) + processModel.getId() + "_";
    }

    public void scheduleNextEvents() throws ScyllaRuntimeException, SuspendExecution {
        for (int i : nextEventMap.keySet()) {
            ScyllaEvent nextEvent = nextEventMap.get(i);
            TimeSpan timeSpanToNextEvent = timeSpanToNextEventMap.get(i);
            SimulationUtils.scheduleEvent(nextEvent, timeSpanToNextEvent);
        }
        // to make sure that one will not schedule events twice
        nextEventMap.clear();
        timeSpanToNextEventMap.clear();
    }

    public ProcessSimulationComponents getDesmojObjects() {
        return pSimComponents;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public int getNodeId() {
        return nodeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSource() {
        return source;
    }

    public TimeInstant getSimulationTimeOfSource() {
        return simulationTimeOfSource;
    }

    public Map<Integer, ScyllaEvent> getNextEventMap() {
        return nextEventMap;
    }

    public Map<Integer, TimeSpan> getTimeSpanToNextEventMap() {
        return timeSpanToNextEventMap;
    }

    public int getNewEventIndex() {
        return nextEventIndex++;
    }
}
