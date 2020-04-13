package de.hpi.bpt.scylla.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.ResourceInfo;
import de.hpi.bpt.scylla.logger.ResourceStatus;
import de.hpi.bpt.scylla.model.configuration.ResourceReference;
import de.hpi.bpt.scylla.model.global.resource.DynamicResource;
import de.hpi.bpt.scylla.model.global.resource.DynamicResourceInstance;
import de.hpi.bpt.scylla.model.global.resource.Resource;
import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceAssignmentPluggable;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.TimeInstant;

/**
 * Responsible for event queues and resource queues.
 * 
 * @author Tsun Yin Wong
 *
 */
public class QueueManager {
	
	private SimulationModel model;
    private Map<String, ResourceQueue> resourceObjects = new HashMap<String, ResourceQueue>();
    

    private static Comparator<ResourceObjectTuple> resourceObjectTupleComparator = new Comparator<ResourceObjectTuple>() {

        @Override
        public int compare(ResourceObjectTuple o1, ResourceObjectTuple o2) {
            double diff = o1.getAvgOfLastAccesses() - o2.getAvgOfLastAccesses();
            if (diff < 0) {
                return -1;
            }
            else if (diff > 0) {
                return 1;
            }
            else {
                return 0;
            }
        }
    };
    
    public QueueManager(SimulationModel simulationModel) throws InstantiationException {
		this.model = simulationModel;
        convertToResourceObjects(model.getGlobalConfiguration().getResources());
	}
    
    private void convertToResourceObjects(Map<String, Resource> resources) throws InstantiationException {

        resourceObjects = new HashMap<String, ResourceQueue>();
        for (String resourceType : resources.keySet()) {
            Resource resource = resources.get(resourceType);
            int quantity = resource.getQuantity();
            ResourceQueue resQueue = new ResourceQueue(quantity);
            if (resource instanceof DynamicResource) {
                DynamicResource dynResource = (DynamicResource) resource;
                Map<String, DynamicResourceInstance> resourceInstances = dynResource.getResourceInstances();
                for (String resourceInstanceName : resourceInstances.keySet()) {
                    DynamicResourceInstance instance = resourceInstances.get(resourceInstanceName);
                    double cost = instance.getCost();
                    TimeUnit timeUnit = instance.getTimeUnit();
                    List<TimetableItem> timetable = instance.getTimetable();
                    ResourceObject resObject = new ResourceObject(resourceType, resourceInstanceName, cost, timeUnit, timetable);
                    resQueue.add(resObject);

                    boolean availableAtStart = resObject.isAvailable(model.getStartDateTime());
                    //TODO model should not be first parameter here
                    SimulationUtils.scheduleNextResourceAvailableEvent(model, resObject, model.getStartDateTime(), availableAtStart);
                }
            } else {
                throw new InstantiationException("Type of resource " + resourceType + " not supported.");
            }
            resourceObjects.put(resourceType, resQueue);
        }
    }
    
    private Map<String, ResourceQueue> getResourceObjects() {
        return resourceObjects;
    }
    
    public Collection<ResourceObject> availableResources(String typeId) {
    	return resourceObjects.get(typeId);
    }
    
    public Collection<String> getResourceTypes() {
    	return resourceObjects.keySet();
    }    

    /**
     * Checks whether resource instances are available for the given event.
     * 
     * @param event
     *            the DesmoJ event in question
     * @return true if resource instances are available for the given event
     */
    public boolean hasResourcesForEvent(ScyllaEvent event) {
        ResourceObjectTuple resourceObjectTuple = getResourcesForEvent(event);
        if (resourceObjectTuple == null) {
            return false;
        }
        else {
            Set<ResourceObject> resourceObjects = resourceObjectTuple.getResourceObjects();
            for (ResourceObject obj : resourceObjects) {
                String resourceId = obj.getResourceType();
                // put not-chosen objects back into resource queues
                getResourceObjects().get(resourceId).add(obj);
            }
            return true;
        }

    }

    /**
     * Returns resource instances which are available for the given event.
     * @param event
     *            the DesmoJ event in question
     * @return the resource instances which are available for the given event
     */
    public ResourceObjectTuple getResourcesForEvent(ScyllaEvent event) {
    	
    	Optional<ResourceObjectTuple> assignment = ResourceAssignmentPluggable.runPlugins(model, event);
    	if(assignment.isPresent())return assignment.get();
    	
        TimeInstant currentSimulationTime = model.presentTime();

        Map<String, List<ResourceObject>> availableResourceObjects = new TreeMap<String, List<ResourceObject>>();
        ProcessSimulationComponents simulationComponents = event.getSimulationComponents();
        int nodeId = event.getNodeId();
        Set<ResourceReference> resourceReferences = simulationComponents.getSimulationConfiguration()
                .getResourceReferenceSet(nodeId);

        if (resourceReferences.isEmpty()) {
            return new ResourceObjectTuple();
        }

        //Id and amount of needed resources
        Map<String, Integer> resourcesRequired = new HashMap<String, Integer>();
        for (ResourceReference ref : resourceReferences) {
            String resourceId = ref.getResourceId();
            int amount = ref.getAmount();
            resourcesRequired.put(resourceId, amount);
        }

        // keySet() on HashMap returns values on random order
        // but we need some fixed order of processing the different resource types
        String[] resourceIds = resourcesRequired.keySet().toArray(new String[0]);

        // retrieve all available resources
        boolean enoughPotentialResourceInstancesAvailable = true;
        for (String resourceId : resourceIds) {
            int amount = resourcesRequired.get(resourceId);
            ResourceQueue queue = getResourceObjects().get(resourceId);
            List<ResourceObject> resourceObjects = queue.pollAvailable(currentSimulationTime);
            availableResourceObjects.put(resourceId, resourceObjects);
            if (resourceObjects.size() < amount) { // less available than required
                enoughPotentialResourceInstancesAvailable = false;
                break;
            }
        }

        //Put back all already polled resources, they are not needed as there are not enough of them
        if (!enoughPotentialResourceInstancesAvailable) {
            for (String resourceId : availableResourceObjects.keySet()) {
                List<ResourceObject> resourceObjects = availableResourceObjects.get(resourceId);
                getResourceObjects().get(resourceId).addAll(resourceObjects);
            }
            return null;
        }

        // find timetable-matching instances per resource -> one match = one tuple
        Map<String, List<ResourceObjectTuple>> tuplesPerResource = new HashMap<String, List<ResourceObjectTuple>>();
        for (String resourceId : resourceIds) {
            int amount = resourcesRequired.get(resourceId);
            List<ResourceObject> resourceObjects = availableResourceObjects.get(resourceId);
            List<ResourceObjectTuple> tuples = new ArrayList<ResourceObjectTuple>();
            for (int index = 0; index < resourceObjects.size(); index++) {
                tuples.addAll(findMatchingResourceObjects(resourceObjects, index, amount));
            }
            tuplesPerResource.put(resourceId, tuples);
        }

        // match tuples of different resource types
        List<ResourceObjectTuple> matchingTuples = findMatchingResourceObjectTuples(tuplesPerResource, resourceIds);

        if (matchingTuples.isEmpty()) { // no resources available
            return null;
        }

        for (ResourceObjectTuple tuple : matchingTuples) {
            List<Double> lastAccesses = new ArrayList<Double>();
            for (ResourceObject resourceObject : tuple.getResourceObjects()) {
                double timeOfLastAccess = resourceObject.getTimeOfLastAccess();
                lastAccesses.add(timeOfLastAccess);
            }
            double avgOfLastAccesses = DateTimeUtils.mean(lastAccesses);
            tuple.setAvgOfLastAccesses(avgOfLastAccesses);
        }

        // sort by average of last access times, ascending (see comparator)
        Collections.sort(matchingTuples, resourceObjectTupleComparator);

        ResourceObjectTuple chosenTuple = matchingTuples.get(0);
        Set<ResourceObject> chosenObjects = chosenTuple.getResourceObjects();

        for (String resourceId : availableResourceObjects.keySet()) {
            // remove chosen objects from available resource objects
            availableResourceObjects.get(resourceId).removeAll(chosenObjects);
            // put not-chosen objects back into resource queues
            getResourceObjects().get(resourceId).addAll(availableResourceObjects.get(resourceId));
        }

        return chosenTuple;
    }

    public void assignResourcesToEvent(ScyllaEvent event, ResourceObjectTuple resourceObjectTuple) {
        ProcessInstance processInstance = event.getProcessInstance();
        String source = event.getSource();
        int nodeId = event.getNodeId();
        double timeOfLastAccess = model.presentTime().getTimeAsDouble(DateTimeUtils.getReferenceTimeUnit());

        Set<ResourceObject> resourceObjects = resourceObjectTuple.getResourceObjects();
        for (ResourceObject resourceObject : resourceObjects) {
            String resourceType = resourceObject.getResourceType();
            String resourceId = resourceObject.getId();

            resourceObject.setTimeOfLastAccess(timeOfLastAccess);
            //
            // // logging
            // if (StatisticsLogger.isActive()) {
            // String resourceType = e.getResourceType();
            // String id = e.getId();
            // ResourceTransitionType transition = ResourceTransitionType.START_USE;
            // ResourceInfo info = new ResourceInfo(Math.round(timeOfLastAccess), transition);
            // StatisticsLogger.addResourceInfo(resourceType, id, info);
            // }
            // }

            model.sendTraceNote("Assign resource " + resourceType + " (" + resourceId + ") to process instance "
                    + processInstance.getName() + ", source: " + source);

            if (model.isOutputLoggingOn()) {
                ResourceStatus status = ResourceStatus.IN_USE;
                ResourceInfo info = new ResourceInfo(Math.round(timeOfLastAccess), status, processInstance, nodeId);
                model.addResourceInfo(resourceType, resourceId, info);
            }
        }
        resourceObjectTuple.setAvgOfLastAccesses(timeOfLastAccess);
        processInstance.getAssignedResources().put(source, resourceObjectTuple);

    }

    /**
     * Releases the resource instances assigned to the given event, selects event(s) from the event queues which are due
     * next and schedules them immediately.
     * 
     * @param releasingEvent
     *            the DesmoJ event whose resource instances may be released
     * @throws ScyllaRuntimeException
     */
    public void releaseResourcesAndScheduleQueuedEvents(ScyllaEvent releasingEvent) throws ScyllaRuntimeException {
    	ProcessInstance processInstance = releasingEvent.getProcessInstance();
        int nodeId = releasingEvent.getNodeId();
        String nameOfResponsibleEvent = releasingEvent.getSource();

        // release resources
        Set<String> resourceQueuesUpdated = new HashSet<String>();
        Set<ResourceObject> assignedResources = new HashSet<ResourceObject>();

        if (nameOfResponsibleEvent == null) { // release all resources of process instance
            for (ResourceObjectTuple tuple : processInstance.getAssignedResources().values()) {
                assignedResources.addAll(tuple.getResourceObjects());
            }
        }
        else {
            assignedResources
                    .addAll(processInstance.getAssignedResources().get(nameOfResponsibleEvent).getResourceObjects());
        }
        Map<String, ResourceQueue> resourceObjects = getResourceObjects();
        TimeInstant presentTime = model.presentTime();

        for (ResourceObject resourceObject : assignedResources) {
            String resourceId = resourceObject.getResourceType();
            resourceObjects.get(resourceId).offer(presentTime, resourceObject, processInstance, nodeId);
            String traceNote = "Dissociate resource " + resourceId + " (" + resourceObject.getId()
                    + ") from process instance " + processInstance.getName();
            if (nameOfResponsibleEvent != null) {
                traceNote += ", source: " + nameOfResponsibleEvent;
            }
            model.sendTraceNote(traceNote);
            resourceQueuesUpdated.add(resourceId);
        }
        if (nameOfResponsibleEvent == null) {
            processInstance.getAssignedResources().clear();
        }
        else {
            processInstance.getAssignedResources().remove(nameOfResponsibleEvent);
        }

        // ... and schedule next task if there are any in the queue
        /**
         * there may be no event ready for schedule, especially if they rely on resources that are together not
         * available at the moment, so we have to check "later" again ? (whatever later means)
         * 
         * --> solved by introduction of ResourceAvailableEvent
         */
        model.scheduleAllEventsFromQueueReadyForSchedule(resourceQueuesUpdated);
    }

    /**
     * Returns all resource instances.
     * 
     * @param sm
     *            the simulation model
     * @return resource instances of all resource types
     */
    public Set<ResourceObject> getAllResourceObjects() {
        Set<ResourceObject> objects = new HashSet<ResourceObject>();
        Map<String, ResourceQueue> resources = getResourceObjects();
        for (String resourceType : resources.keySet()) {
            ResourceQueue resourceQueue = resources.get(resourceType);
            ResourceObject obj = resourceQueue.poll();
            while (obj != null) {
                objects.add(obj);
                obj = resourceQueue.poll();
            }
            resourceQueue.addAll(objects);
        }
        return objects;

    }
    

    private static List<ResourceObjectTuple> findMatchingResourceObjectTuples(Map<String, List<ResourceObjectTuple>> tuplesPerResource, String[] resourceIds) {
        List<ResourceObjectTuple> tuples = new ArrayList<ResourceObjectTuple>();
        List<ResourceObjectTuple> tuplesOfFirstResource = tuplesPerResource.get(resourceIds[0]);

        for (int i = 0; i < tuplesOfFirstResource.size(); i++) {
            ResourceObjectTuple potentiallyMergedTuple = tuplesOfFirstResource.get(i);
            int indexOfResourceToBeCovered = 1; // potentiallyMergedTuple initially contains ResourceObjects from first
                                                // resource type, so cover second resource type as next, whose index is
                                                // 1
            mergeAndAddToTuple(tuplesPerResource, resourceIds, potentiallyMergedTuple, indexOfResourceToBeCovered,
                    tuples);
        }

        return tuples;
    }

    private static void mergeAndAddToTuple(Map<String, List<ResourceObjectTuple>> tuplesPerResource, String[] resourceIds, ResourceObjectTuple potentiallyMergedTuple, int indexOfResourceToBeCovered, List<ResourceObjectTuple> tuples) {
        if (resourceIds.length == indexOfResourceToBeCovered) {
            tuples.add(potentiallyMergedTuple);
            return;
        }
        List<ResourceObjectTuple> tuplesOfResource = tuplesPerResource.get(resourceIds[indexOfResourceToBeCovered]);
        for (int i = 0; i < tuplesOfResource.size(); i++) {
            ResourceObjectTuple tupleOfResourceToBeCovered = tuplesOfResource.get(i);
            List<TimetableItem> sharedTimetable = DateTimeUtils.intersectTimetables(
                    potentiallyMergedTuple.getSharedTimetable(), tupleOfResourceToBeCovered.getSharedTimetable());
            if (sharedTimetable != null && sharedTimetable.isEmpty()) { // intersection timetable is empty
                continue;
            }
            // update tuple TODO avoid clone by recursive implementation
            ResourceObjectTuple tuple = potentiallyMergedTuple.clone();
            tuple.getResourceObjects().addAll(tupleOfResourceToBeCovered.getResourceObjects());
            tuple.setSharedTimetable(sharedTimetable);
            // continue
            mergeAndAddToTuple(tuplesPerResource, resourceIds, tuple, indexOfResourceToBeCovered + 1, tuples);
        }

    }

    private static List<ResourceObjectTuple> findMatchingResourceObjects(List<ResourceObject> resourceObjects,
            int index, int amount) {
        List<ResourceObjectTuple> tuples = new ArrayList<ResourceObjectTuple>();

        ResourceObjectTuple potentialTuple = new ResourceObjectTuple();
        ResourceObject firstObject = resourceObjects.get(index);
        potentialTuple.getResourceObjects().add(firstObject);
        potentialTuple.setSharedTimetable(firstObject.getTimetable());
        addToTuple(resourceObjects, index, potentialTuple, amount, tuples);

        return tuples;
    }

    private static void addToTuple(List<ResourceObject> resourceObjects, int index, ResourceObjectTuple potentialTuple,
            int amount, List<ResourceObjectTuple> tuples) {
        if (potentialTuple.size() == amount) {
            tuples.add(potentialTuple);
            return;
        }
        for (int i = index + 1; i < resourceObjects.size(); i++) {
            ResourceObject obj = resourceObjects.get(i);
            List<TimetableItem> sharedTimetable = DateTimeUtils.intersectTimetables(potentialTuple.getSharedTimetable(),
                    obj.getTimetable());
            if (sharedTimetable != null && sharedTimetable.isEmpty()) { // intersection timetable is empty
                continue;
            }
            // update tuple TODO avoid clone by recursive implementation
            ResourceObjectTuple tuple = potentialTuple.clone();
            tuple.getResourceObjects().add(obj);
            tuple.setSharedTimetable(sharedTimetable);
            // continue
            addToTuple(resourceObjects, i, tuple, amount, tuples);
        }

    }

}
