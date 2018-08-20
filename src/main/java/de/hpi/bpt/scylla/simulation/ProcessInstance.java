package de.hpi.bpt.scylla.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEvent;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * Describes the state of a process instance during discrete event simulation.<br>
 * <br>
 * Scope: sub-process or process, i.e. each sub-process or process has its own ProcessInstance entity.<br>
 * This is relevant when sub-process has terminate end event, then only DesmoJ events of sub-process are unscheduled.
 * 
 */
public class ProcessInstance extends Entity {

    private ProcessInstance parent;
    private Map<String, ProcessInstance> children = new HashMap<String, ProcessInstance>();

    private ProcessModel processModel;
    private int id;

    /**
     * map of identifier of DesmoJ event (responsible for assigning the resource instances) to resource instances
     */
    private Map<String, ResourceObjectTuple> assignedResources = new HashMap<String, ResourceObjectTuple>();

    /**
     * map of graph node identifier of BPMN event to graph node identifiers of nodes which have triggered the BPMN event
     */
    private Map<Integer, Set<Integer>> referenceToEventsOnHold = new HashMap<Integer, Set<Integer>>();

    private double startTime = 0;

    /**
     * Constructor.
     * 
     * @param owner
     *            simulation model to which the process instance belongs
     * @param processModel
     *            process model of the process instance
     * @param processInstanceId
     *            unique identifier of the process instance
     * @param showInTrace
     *            true if DesmoJ logging is enabled
     */
    public ProcessInstance(Model owner, ProcessModel processModel, int processInstanceId, boolean showInTrace) {
        super(owner, buildProcessInstanceName(processModel, processInstanceId), showInTrace);
        this.processModel = processModel;
        this.id = processInstanceId;
    }

    private static String buildProcessInstanceName(ProcessModel processModel, int processInstanceId) {
        return prependProcessModelIds(processModel) + "PI" + processInstanceId;
    }

    private static String prependProcessModelIds(ProcessModel processModel) {
        if (processModel.getParent() == null) {
            return processModel.getId() + "_";
        }
        return prependProcessModelIds(processModel.getParent()) + processModel.getId() + "_";
    }

    public ProcessInstance getParent() {
        return parent;
    }

    public void setParent(ProcessInstance parent) {
        parent.addChild(this);
        this.parent = parent;
    }

    public void addChild(ProcessInstance processInstance) {
        String name = processInstance.getName();
        this.children.put(name, processInstance);
    }

    public void removeChild(ProcessInstance processInstance) {
        String name = processInstance.getName();
        this.children.remove(name);
    }

    public ProcessModel getProcessModel() {
        return processModel;
    }

    public int getId() {
        return id;
    }

    public Map<String, ResourceObjectTuple> getAssignedResources() {
        return assignedResources;
    }

    public Map<Integer, Set<Integer>> getNodesAndTriggers() {
        return referenceToEventsOnHold;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    // public double getTimeRelativeToSubProcessStart() {
    // return timeRelativeToSubProcessStart;
    // }
    //
    // public void setTimeRelativeToSubProcessStart(double timeRelativeToSubProcessStart) {
    // this.timeRelativeToSubProcessStart = timeRelativeToSubProcessStart;
    // }
    //
    // public Double getEndTimeRelativeToSubProcessStart() {
    // return endTimeRelativeToSubProcessStart;
    // }
    //
    // public void setEndTimeRelativeToSubProcessStart(Double endTimeRelativeToSubProcessStart) {
    // this.endTimeRelativeToSubProcessStart = endTimeRelativeToSubProcessStart;
    // }

    @Override
    public void cancel() {
        try {
            Iterator<String> iterator = children.keySet().iterator();
            while (iterator.hasNext()) {
                String name = iterator.next();
                ProcessInstance child = children.get(name);
                child.cancel();
            }

            if (isScheduled()) {
                List<EventAbstract> scheduledEvents = getScheduledEvents();
                List<ScyllaEvent> rescheduledEvents = new ArrayList<ScyllaEvent>();
                for (EventAbstract e : scheduledEvents) {
                    if (e instanceof TaskEvent) {
                        TaskEvent event = (TaskEvent) e;

                        TaskCancelEvent cancelEvent = new TaskCancelEvent(event.getModel(), event.getSource(),
                                event.getSimulationTimeOfSource(), event.getSimulationComponents(), this, event.getNodeId());
                        rescheduledEvents.add(cancelEvent);
                    }
                }
                super.cancel();
                for (ScyllaEvent event : rescheduledEvents) {
                    TimeSpan timeSpan = new TimeSpan(0);
                    SimulationUtils.scheduleEvent(event, timeSpan);
                }
            }

            if (parent != null) {
                parent.removeChild(this);
            }
        }
        catch (ScyllaRuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
