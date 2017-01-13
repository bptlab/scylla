package de.hpi.bpt.scylla.simulation.event;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Abstract class for DesmoJ events representing BPMN tasks.
 * 
 * @author Tsun Yin Wong
 *
 */
public abstract class TaskEvent extends ScyllaEvent {

    public TaskEvent(Model owner, String source, TimeInstant simulationTimeOfSource,
            ProcessSimulationComponents desmojObjects, ProcessInstance processInstance, int nodeId) {
        super(owner, source, simulationTimeOfSource, desmojObjects, processInstance, nodeId);
    }

    protected abstract void addToLog(ProcessInstance processInstance);

    @Override
    public void eventRoutine(ProcessInstance processInstance) throws SuspendExecution {
        SimulationModel model = (SimulationModel) getModel();
        boolean outputLoggingIsOn = model.isOutputLoggingOn();
        if (outputLoggingIsOn) {
            addToLog(processInstance);
        }
    }

}