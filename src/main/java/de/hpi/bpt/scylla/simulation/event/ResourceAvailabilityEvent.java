package de.hpi.bpt.scylla.simulation.event;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.ResourceAvailabilityEventPluggable;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * DesmoJ event responsible for scheduling queued events.
 * 
 * @author Tsun Yin Wong
 *
 */
public class ResourceAvailabilityEvent extends ExternalEvent {

    private ResourceObject resourceObject;

    public ResourceAvailabilityEvent(Model owner, ResourceObject resourceObject, boolean showInTrace) {
        super(owner, resourceObject.getResourceType() + "_" + resourceObject.getId() + "_ResourceAvailable",
                showInTrace);
        this.resourceObject = resourceObject;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        SimulationModel model = (SimulationModel) getModel();

        TimeInstant currentSimulationTime = model.presentTime();

        Set<String> resourceQueuesUpdated = new HashSet<String>();
        String resourceType = resourceObject.getResourceType();
        resourceQueuesUpdated.add(resourceType);
        try {
            ScyllaEvent eventFromQueue = QueueManager.getEventFromQueueReadyForSchedule(model, resourceQueuesUpdated);
            while (eventFromQueue != null) {
                SimulationUtils.scheduleEvent(eventFromQueue, new TimeSpan(0));
                eventFromQueue = QueueManager.getEventFromQueueReadyForSchedule(model, resourceQueuesUpdated);
            }

            ResourceAvailabilityEventPluggable.runPlugins(this);

            // schedule next ResourceAvailableEvent
            ZonedDateTime currentDateTime = DateTimeUtils.getDateTime(currentSimulationTime);
            boolean currentlyInTimetableItem = true;
            SimulationUtils.scheduleNextResourceAvailableEvent(model, resourceObject, currentDateTime,
                    currentlyInTimetableItem);
        }
        catch (ScyllaRuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
