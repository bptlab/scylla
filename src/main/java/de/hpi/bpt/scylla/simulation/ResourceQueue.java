package de.hpi.bpt.scylla.simulation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import de.hpi.bpt.scylla.logger.ResourceInfo;
import de.hpi.bpt.scylla.logger.ResourceStatus;
import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import desmoj.core.simulator.TimeInstant;

/**
 * Queue for resource instances.
 * 
 * @author Tsun Yin Wong
 */
class ResourceQueue extends PriorityQueue<ResourceObject> {

    private static final long serialVersionUID = 886409943987052123L;

    /**
     * Constructor.
     * 
     * @param capacity
     *            size of resource queue
     */
    public ResourceQueue(int capacity) {
        // sort by priority, ascending
        super(capacity, new Comparator<ResourceObject>() {
            @Override
            public int compare(ResourceObject o1, ResourceObject o2) {
                int prioComp = o2.getPriority() - o1.getPriority();
                if (prioComp != 0) {
                    return prioComp;
                }
                double diff = o1.getTimeOfLastAccess() - o2.getTimeOfLastAccess();
                if (diff > 0) {
                    return 1;
                }
                else if (diff < 0) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });
    }

    public boolean offer(TimeInstant returnTime, ResourceObject e, ProcessInstance processInstance, int nodeId) {
        double timeOfLastAccess = returnTime.getTimeAsDouble(DateTimeUtils.getReferenceTimeUnit());
        e.setTimeOfLastAccess(timeOfLastAccess);

        SimulationModel model = (SimulationModel) processInstance.getModel();
        if (model.isOutputLoggingOn()) {
            String resourceType = e.getResourceType();
            String resourceId = e.getId();
            ResourceStatus status = ResourceStatus.FREE;
            ResourceInfo info = new ResourceInfo(Math.round(timeOfLastAccess), status, processInstance, nodeId);
            model.addResourceInfo(resourceType, resourceId, info);
        }

        return super.offer(e);

    }

    /**
     * Poll all objects available at a specific point in time
     * Unavailable objects are put back into the queue
     * @param retrievalTime : The point in time the resources should be available
     * @return 
     */
    public List<ResourceObject> pollAvailable(TimeInstant retrievalTime) {
        Set<ResourceObject> unavailableResourceObjects = new HashSet<ResourceObject>();
        List<ResourceObject> availableResourceObjects = new ArrayList<ResourceObject>();

        ResourceObject e = super.poll();

        // looking for an available resource
        while (e != null) {
            ZonedDateTime retrievalDateTime = DateTimeUtils.getDateTime(retrievalTime);
            List<TimetableItem> timetable = e.getTimetable();
            if (timetable == null) { // null == available at any time
                availableResourceObjects.add(e);
            }
            else {
                boolean isWithinAnyTimetable = e.isAvailable(retrievalDateTime);

                if (isWithinAnyTimetable) { // available at certain times
                    availableResourceObjects.add(e);
                }
                else { // not available
                    unavailableResourceObjects.add(e);
                }
            }
            e = super.poll();
        }

        super.addAll(unavailableResourceObjects);

        return availableResourceObjects;
    }

    /**
     * use offer(TimeInstant returnTime, ResourceObject e)
     */
    @Deprecated
    public boolean offer(ResourceObject e) {
        return super.offer(e);
    }

    /**
     * consider using pollAvailable(TimeInstant retrievalTime)
     */
    @Override
    public ResourceObject poll() {
        return super.poll();
    }

}
