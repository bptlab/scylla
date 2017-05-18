package de.hpi.bpt.scylla.simulation.event;

import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.MultipleStartNodesException;
import de.hpi.bpt.scylla.model.process.graph.exception.NoStartNodeException;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.ProcessInstanceGenerationEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event responsible for generating new process instances and respective BPMN start events.
 * 
 * @author Tsun Yin Wong
 *
 */
public class ProcessInstanceGenerationEvent extends Event<ProcessInstance> {

    private String processId;
    private Long endTimeRelativeToGlobalStart;
    private ProcessInstance processInstance;
    private TimeSpan timeSpanToNextProcessInstance;
    private TimeSpan timeSpanToStartEvent;

    public ProcessInstanceGenerationEvent(Model owner, String processId, Long endTimeRelativeToGlobalStart,
            boolean showInTrace) {
        super(owner, processId + "_ProcessInstanceGeneration", showInTrace);
        this.processId = processId;
        this.endTimeRelativeToGlobalStart = endTimeRelativeToGlobalStart;
    }

    // TODO XSD validation
    // TODO fixed cost per task
    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        this.processInstance = processInstance;
        SimulationModel model = (SimulationModel) getModel();

        TimeInstant currentSimulationTime = model.presentTime();
        TimeUnit timeUnit = DateTimeUtils.getReferenceTimeUnit();
        long currentTime = currentSimulationTime.getTimeRounded(timeUnit);

        if (currentTime >= endTimeRelativeToGlobalStart) {
            // do not schedule event(s) for new process instance
            // if the end time is reached
            return;
        }

        boolean showInTrace = traceIsOn();
        String name = getName();
        ProcessSimulationComponents desmojObjects = model.getDesmojObjectsMap().get(processId);
        ProcessModel processModel = desmojObjects.getProcessModel();
        try {
            Integer startNodeId = processModel.getStartNode();

            timeSpanToStartEvent = new TimeSpan(0);

            ProcessInstanceGenerationEventPluggable.runPlugins(this, processInstance);

            BPMNStartEvent event = new BPMNStartEvent(model, name, currentSimulationTime, desmojObjects,
                    processInstance, startNodeId);

            int processInstanceId = desmojObjects.incrementProcessInstancesStarted();

            // schedule next process instance start event
            if (processInstanceId <= desmojObjects.getSimulationConfiguration().getNumberOfProcessInstances()) {
                double duration = desmojObjects.getDistributionSample(startNodeId);
                TimeUnit unit = desmojObjects.getDistributionTimeUnit(startNodeId);

                ProcessInstance nextProcessInstance = new ProcessInstance(model, processModel, processInstanceId,
                        showInTrace);
                timeSpanToNextProcessInstance = new TimeSpan(duration, unit);

                ProcessInstanceGenerationEvent nextEvent = new ProcessInstanceGenerationEvent(model, processId,
                        endTimeRelativeToGlobalStart, showInTrace);

                nextEvent.schedule(nextProcessInstance, timeSpanToNextProcessInstance);
            }

            // schedule for start of simulation
            event.schedule(processInstance, timeSpanToStartEvent);
        }
        catch (NodeNotFoundException | MultipleStartNodesException | NoStartNodeException | ScyllaRuntimeException e) {
            DebugLogger.error(e.getMessage());
            e.printStackTrace();
            DebugLogger.log("Error during instantiation of process model " + processModel.getId() + ".");
            int nodeId = 0; // no node initialized, use zero
            SimulationUtils.abort(model, processInstance, nodeId, showInTrace);
        }
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Long getEndTimeRelativeToGlobalStart() {
        return endTimeRelativeToGlobalStart;
    }

    public void setEndTimeRelativeToGlobalStart(Long endTimeRelativeToGlobalStart) {
        this.endTimeRelativeToGlobalStart = endTimeRelativeToGlobalStart;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public TimeSpan getTimeSpanToNextProcessInstance() {
        return timeSpanToNextProcessInstance;
    }

    public void setTimeSpanToNextProcessInstance(TimeSpan timeSpanToNextProcessInstance) {
        this.timeSpanToNextProcessInstance = timeSpanToNextProcessInstance;
    }

    public TimeSpan getTimeSpanToStartEvent() {
        return timeSpanToStartEvent;
    }

    public void setTimeSpanToStartEvent(TimeSpan timeSpanToStartEvent) {
        this.timeSpanToStartEvent = timeSpanToStartEvent;
    }

}
