package de.hpi.bpt.scylla.simulation.event;

import java.util.List;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.plugin_type.simulation.event.ProcessSimulationStopEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * DesmoJ event responsible for stopping the business process simulation.
 * 
 * @author Tsun Yin Wong
 *
 */
public class ProcessSimulationStopEvent extends ExternalEvent {

    String processId;

    public ProcessSimulationStopEvent(Model owner, String processId, boolean showInTrace) {
        super(owner, processId + "_StopProcessSimulation", showInTrace);
        this.processId = processId;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        SimulationModel model = (SimulationModel) getModel();

        Set<Integer> idsOfProcessInstancesToAbort = QueueManager.clearEventQueuesByProcessId(model, processId);

        try {
            ProcessSimulationStopEventPluggable.runPlugins(this);

            boolean includeSubmodels = false; // we do not have any submodels
            List<Entity> entities = model.getEntities(includeSubmodels);

            for (Entity entity : entities) {
                if (entity instanceof ProcessInstance) {
                    ProcessInstance processInstance = (ProcessInstance) entity;
                    if (processInstance.isScheduled() && processId.equals(processInstance.getProcessModel().getId())) {
                        processInstance.cancel();

                        idsOfProcessInstancesToAbort.add(processInstance.getId());
                    }
                }
                else {
                    DebugLogger.log("Found unsupported DesmoJ entity: " + entity.getName());
                }
            }

            for (Integer id : idsOfProcessInstancesToAbort) {
                DebugLogger.log("Abort process instance " + id + " of process " + processId + ".");
            }
            if (idsOfProcessInstancesToAbort.size() > 0) {
                DebugLogger.log("End time of process " + processId + " reached.");
            }

            if (model.getEndDateTime() != null) {
                long currentTime = model.presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit());
                long simulationEndTime = DateTimeUtils.getDuration(model.getStartDateTime(), model.getEndDateTime());
                if (simulationEndTime == currentTime) {
                    model.getExperiment().stop(new TimeInstant(currentTime + 1));
                }
            }
        }
        catch (ScyllaRuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
