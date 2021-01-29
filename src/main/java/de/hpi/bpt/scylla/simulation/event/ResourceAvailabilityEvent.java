package de.hpi.bpt.scylla.simulation.event;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.ResourceAvailabilityEventPluggable;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * DesmoJ event responsible for scheduling queued events.
 * This event occurs at the start of each timetable item for each resource instance
 * 
 * @author Tsun Yin Wong
 */
public class ResourceAvailabilityEvent extends ExternalEvent {

    private ResourceObject resourceObject;

    public ResourceAvailabilityEvent(Model owner, ResourceObject resourceObject, boolean showInTrace) {
        super(owner, resourceObject.getResourceType() + "_" + resourceObject.getId() + "_ResourceAvailable",
                showInTrace);
        this.resourceObject = resourceObject;
    }

    /**
     * This notifies all events waiting for resources with the type of the specific resource instance of this event
     * and reschedules such an event for the beginning of the next timetable item
     */
    @Override
    public void eventRoutine() throws SuspendExecution {
        SimulationModel model = (SimulationModel) getModel();

        TimeInstant currentSimulationTime = model.presentTime();

        String resourceType = resourceObject.getResourceType();
        String[] resourceQueuesUpdated = new String[] {resourceType};
        
        try {
            model.scheduleAllEventsFromQueueReadyForSchedule(resourceQueuesUpdated);

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
