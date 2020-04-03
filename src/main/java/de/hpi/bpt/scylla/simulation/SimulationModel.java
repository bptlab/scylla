package de.hpi.bpt.scylla.simulation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ResourceInfo;
import de.hpi.bpt.scylla.model.configuration.ResourceReference;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.global.GlobalConfiguration;
import de.hpi.bpt.scylla.model.process.CommonProcessElements;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.parser.EventOrderType;
import de.hpi.bpt.scylla.plugin_type.simulation.resource.ResourceQueueUpdatedPluggable;
import de.hpi.bpt.scylla.simulation.event.ProcessInstanceGenerationEvent;
import de.hpi.bpt.scylla.simulation.event.ProcessSimulationStopEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * The business process simulation-specific implementation of a DesmoJ model.
 * 
 * @author Tsun Yin Wong
 */
public class SimulationModel extends Model {

    private GlobalConfiguration globalConfiguration;
    
    private QueueManager resourceManager;

    //Events that are waiting for resources
    private Map<String, ScyllaEventQueue> eventQueues = new HashMap<String, ScyllaEventQueue>();

    private ZonedDateTime startDateTime;
    private ZonedDateTime endDateTime;
    private TimeUnit smallestTimeUnit = TimeUnit.DAYS; // default;

    // process specific

    private Map<String, ProcessSimulationComponents> pSimMap = new HashMap<String, ProcessSimulationComponents>();

    private Map<String, Map<Integer, List<ProcessNodeInfo>>> processNodeInfos = new TreeMap<String, Map<Integer, List<ProcessNodeInfo>>>();
    private Map<String, Map<String, List<ResourceInfo>>> resourceInfos = new TreeMap<String, Map<String, List<ResourceInfo>>>();

    private boolean outputLoggingIsOn = false;
    protected SimulationModel(Model owner, String name, boolean showInReport, boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);
    }

    /**
     * Constructor.
     * 
     * @param owner
     *            parent DesmoJ model
     * @param globalConfiguration
     *            the global configuration
     * @param commonProcessElements
     *            map of identifier of process model to common process elements
     * @param processModels
     *            map of identifier of process model to process model
     * @param simulationConfigurations
     *            map of identifier of process model to simulation configuration
     * @param enableBpsLogging
     *            true if business process-specific logging is enabled
     * @param enableDesLogging
     *            true if DesmoJ logging is enabled
     */
    public SimulationModel(Model owner, GlobalConfiguration globalConfiguration,
            Map<String, CommonProcessElements> commonProcessElements, Map<String, ProcessModel> processModels,
            Map<String, SimulationConfiguration> simulationConfigurations, boolean enableBpsLogging,
            boolean enableDesLogging) {
        this(null, globalConfiguration.getId(), enableDesLogging, enableDesLogging);

        this.globalConfiguration = globalConfiguration; // top simulation model only

        ProcessSimulationComponents parent = null;
        for (String processId : commonProcessElements.keySet()) {
            CommonProcessElements cpe = commonProcessElements.get(processId);
            ProcessModel processModel = processModels.get(processId);
            SimulationConfiguration simulationConfiguration = simulationConfigurations.get(processId);
            ProcessSimulationComponents desmojObj = new ProcessSimulationComponents(this, parent, processModel,
                    simulationConfiguration);
            desmojObj.setCommonProcessElements(cpe);

            pSimMap.put(processId, desmojObj);

            if (enableBpsLogging) {
                outputLoggingIsOn = true;
                processNodeInfos.put(processId, new TreeMap<Integer, List<ProcessNodeInfo>>());
            }
        }
    }

    @Override
    public String description() {
        return "Default simulation model description.";
    }

    @Override
    public void init() {
        try {

            for (String processId : pSimMap.keySet()) {
                ProcessSimulationComponents desmojObjects = pSimMap.get(processId);
                desmojObjects.init();

                ZonedDateTime startDateOfSimulationConf = desmojObjects.getSimulationConfiguration().getStartDateTime();
                if (startDateTime == null || startDateOfSimulationConf.isBefore(startDateTime)) {
                    startDateTime = startDateOfSimulationConf;
                }

                boolean oneSimulationConfHasNoEndDate = false; // if one of the simulation configurations has no end
                                                               // date,
                                                               // then we cannot tell the overall end of simulation
                ZonedDateTime endDateOfSimulationConf = desmojObjects.getSimulationConfiguration().getEndDateTime();
                if (endDateOfSimulationConf == null) {
                    oneSimulationConfHasNoEndDate = true;
                    endDateTime = null;
                }
                if (!oneSimulationConfHasNoEndDate && endDateOfSimulationConf != null
                        && (endDateTime == null || endDateTime.isBefore(endDateOfSimulationConf))) {
                    endDateTime = endDateOfSimulationConf;
                }
            }

            DateTimeUtils.setStartDateTime(startDateTime);

            resourceManager = new QueueManager(this);
            
            // prepare queue and sorting order
            // TODO each resource has its own assignment order
            List<EventOrderType> resourceAssignmentOrder = globalConfiguration.getResourceAssignmentOrder();
            for (String resourceId : resourceManager.getResourceTypes()) {
                ScyllaEventQueue eventQueue = new ScyllaEventQueue(resourceId, resourceAssignmentOrder);
                eventQueues.put(resourceId, eventQueue);
            }
        }
        catch (InstantiationException e) {
            DebugLogger.error(e.getMessage());
            DebugLogger.error("Instantiation of simulation model failed.");
        }
    }

    @Override
    public void doInitialSchedules() {
        TimeUnit timeUnit = DateTimeUtils.getReferenceTimeUnit();
        for (String processId : pSimMap.keySet()) {
            ProcessSimulationComponents desmojObj = pSimMap.get(processId);

            // schedule for end of simulation (if defined), aligned to global start time
            ZonedDateTime endDateOfSimulationConf = desmojObj.getSimulationConfiguration().getEndDateTime();
            Long endTimeRelativeToGlobalStart = Long.MAX_VALUE;
            if (endDateOfSimulationConf != null) {
                endTimeRelativeToGlobalStart = DateTimeUtils.getDuration(startDateTime, endDateOfSimulationConf);
                ProcessSimulationStopEvent endEvent = new ProcessSimulationStopEvent(this, processId, traceIsOn());
                // java.util.Date getTime() returns milliseconds
                endEvent.schedule(new TimeSpan(endTimeRelativeToGlobalStart, timeUnit));
            }

            // schedule for start of simulation, aligned to global start time
            ZonedDateTime startDateOfSimulationConf = desmojObj.getSimulationConfiguration().getStartDateTime();
            long startTimeRelativeToGlobalStart = DateTimeUtils.getDuration(startDateTime, startDateOfSimulationConf);

            ProcessModel processModel = desmojObj.getProcessModel();
            int processInstanceId = desmojObj.incrementProcessInstancesStarted();
            ProcessInstance firstProcessInstance = new ProcessInstance(this, processModel, processInstanceId,
                    traceIsOn());

            ProcessInstanceGenerationEvent startEvent = new ProcessInstanceGenerationEvent(this, processId,
                    endTimeRelativeToGlobalStart, traceIsOn());
            startEvent.schedule(firstProcessInstance, new TimeSpan(startTimeRelativeToGlobalStart, timeUnit));
        }
        if (endDateTime != null) {
            /// java.util.Date getTime() returns milliseconds
            long simulationDuration = DateTimeUtils.getDuration(startDateTime, endDateTime);
            TimeInstant timeInstant = new TimeInstant(simulationDuration, DateTimeUtils.getReferenceTimeUnit());
            for (String processId : pSimMap.keySet()) {
                ProcessSimulationStopEvent abortEvent = new ProcessSimulationStopEvent(this, processId, traceIsOn());
                abortEvent.schedule(timeInstant);
            }
        }
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    public List<EventOrderType> getResourceAssignmentOrder() {
        return globalConfiguration.getResourceAssignmentOrder();
    }

	public QueueManager getResourceManager() {
		return resourceManager;
	}

    public void addToResourceObjects(String resourceType, ResourceQueue resourceQueue) {
        resourceObjects.put(resourceType, resourceQueue);
        return;
    }

    public Map<String, ScyllaEventQueue> getEventQueues() {
        return eventQueues;
    }

    public Map<String, ProcessSimulationComponents> getDesmojObjectsMap() {
        return pSimMap;
    }

    public Map<String, Map<Integer, List<ProcessNodeInfo>>> getProcessNodeInfos() {
        return processNodeInfos;
    }

    public Map<String, Map<String, List<ResourceInfo>>> getResourceInfos() {
        return resourceInfos;
    }

    public boolean addResourceInfo(String resourceType, String id, ResourceInfo info) {
        if (!resourceInfos.containsKey(resourceType)) {
            resourceInfos.put(resourceType, new TreeMap<String, List<ResourceInfo>>());
        }
        if (!resourceInfos.get(resourceType).containsKey(id)) {
            resourceInfos.get(resourceType).put(id, new ArrayList<ResourceInfo>());
        }
        return resourceInfos.get(resourceType).get(id).add(info);
    }

    public boolean addNodeInfo(ProcessModel processModel, ProcessInstance processInstance, ProcessNodeInfo nodeInfo) {
        String processId = processModel.getId();
        ProcessModel parent = processModel.getParent();
        while (parent != null) {
            processId = parent.getId();
            parent = parent.getParent();
        }
        int processInstanceId = processInstance.getId();

        if (!processNodeInfos.containsKey(processId)) {
            processNodeInfos.put(processId, new TreeMap<Integer, List<ProcessNodeInfo>>());
        }
        Map<Integer, List<ProcessNodeInfo>> nodeInfosOfProcess = processNodeInfos.get(processId);
        if (!nodeInfosOfProcess.containsKey(processInstanceId)) {
            nodeInfosOfProcess.put(processInstanceId, new ArrayList<ProcessNodeInfo>());
        }
        return nodeInfosOfProcess.get(processInstanceId).add(nodeInfo);
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    public TimeUnit getSmallestTimeUnit() {
        return smallestTimeUnit;
    }

    public void setSmallestTimeUnit(TimeUnit smallestTimeUnit) {
        this.smallestTimeUnit = smallestTimeUnit;
    }

    public boolean isOutputLoggingOn() {
        return outputLoggingIsOn;
    }
    
    /**
     * Adds event to event queues.
     * 
     * @param event
     *            the event to be added to event queues
     */
    public void addToEventQueues(ScyllaEvent event) {
        ProcessSimulationComponents simulationComponents = event.getSimulationComponents();
        int nodeId = event.getNodeId();
        Set<ResourceReference> resourceReferences = simulationComponents.getSimulationConfiguration()
                .getResourceReferenceSet(nodeId);

        Map<String, ScyllaEventQueue> eventQueues = getEventQueues();
        for (ResourceReference resourceRef : resourceReferences) {
            String resourceId = resourceRef.getResourceId();

            ScyllaEventQueue eventQueue = eventQueues.get(resourceId);
            eventQueue.offer(event);
        }
    }
    
    /**
     * Returns event which is ready to be scheduled for immediate execution from event queues.
     * 
     * @param resourceQueuesUpdated
     *            resource queues which have been updated recently -> only the events which require resources from these
     *            queues are considered
     * @return the DesmoJ event which is ready to be scheduled for immediate execution
     */
    public ScyllaEvent getEventFromQueueReadyForSchedule(Set<String> resourceQueuesUpdated) {
    	
    	ScyllaEvent eventFromPlugin = ResourceQueueUpdatedPluggable.runPlugins(this, resourceQueuesUpdated);
    	if(eventFromPlugin != null)return eventFromPlugin;

        List<ScyllaEvent> eventCandidates = new ArrayList<ScyllaEvent>();
        int accumulatedIndex = Integer.MAX_VALUE;

        for (String resourceId : resourceQueuesUpdated) {
            ScyllaEventQueue eventQueue = getEventQueues().get(resourceId);
            for (int i = 0; i < eventQueue.size(); i++) {
                ScyllaEvent eventFromQueue = eventQueue.peek(i);
                if (eventCandidates.contains(eventFromQueue)) {
                    continue;
                }

                int index = 0;
                boolean eventIsEligible = resourceManager.hasResourcesForEvent(eventFromQueue);

                if (eventIsEligible) {
                    ProcessSimulationComponents simulationComponents = eventFromQueue.getSimulationComponents();
                    int nodeId = eventFromQueue.getNodeId();
                    Set<ResourceReference> resourceReferences = simulationComponents.getSimulationConfiguration()
                            .getResourceReferenceSet(nodeId);
                    for (ResourceReference ref : resourceReferences) {
                        // add position in queue and add to index
                        String resId = ref.getResourceId();
                        ScyllaEventQueue eventQ = getEventQueues().get(resId);
                        index += eventQ.getIndex(eventFromQueue);

                    }
                    if (accumulatedIndex < index) {
                        break;
                    }
                    else if (accumulatedIndex == index) {
                        eventCandidates.add(eventFromQueue);
                    }
                    else if (accumulatedIndex > index) {
                        accumulatedIndex = index;
                        eventCandidates.clear();
                        eventCandidates.add(eventFromQueue);
                    }
                }
            }
        }

        if (eventCandidates.isEmpty()) {
            return null;
        }
        else {
            Collections.sort(eventCandidates, new Comparator<ScyllaEvent>() {
                @Override
                public int compare(ScyllaEvent e1, ScyllaEvent e2) {
                    return e1.getSimulationTimeOfSource().compareTo(e2.getSimulationTimeOfSource());
                }
            });

            ScyllaEvent event = eventCandidates.get(0);

            // get and assign resources

            ResourceObjectTuple resourcesObjectTuple = resourceManager.getResourcesForEvent(event);
            resourceManager.assignResourcesToEvent(event, resourcesObjectTuple);

            removeFromEventQueues(event);
            return event;
        }
    }
    
    /**
     * Checks whether any event is scheduled or queued (if not, simulation may terminate).
     * 
     * @return true if a event is either scheduled or queued
     */
    public boolean isAnyEventScheduledOrQueued() {
        List<Entity> entities = getEntities(false);
        for (Entity entity : entities) {
            TimeInstant timeInstant = entity.scheduledNext();
            if (timeInstant != null) {
                return true;
            }
        }
        Collection<ScyllaEventQueue> eventQueues = getEventQueues().values();
        for (ScyllaEventQueue queue : eventQueues) {
            if (!queue.isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes all events which are related to the given process model from the event queues.
     * 
     * @param processId
     *            the identifier of the process model
     * @return identifiers of the process instances which are affected by removing
     */
    public Set<Integer> clearEventQueuesByProcessId( String processId) {
        Set<Integer> idsOfProcessInstancesToAbort = new HashSet<Integer>();

        // remove events of process from queues
        Map<String, ScyllaEventQueue> eventQueues = getEventQueues();
        for (String resourceId : eventQueues.keySet()) {
            ScyllaEventQueue queue = eventQueues.get(resourceId);
            Iterator<ScyllaEvent> iterator = queue.iterator();
            Set<ScyllaEvent> eventsToRemove = new HashSet<ScyllaEvent>();
            while (iterator.hasNext()) {
                ScyllaEvent queuedEvent = iterator.next();
                String eventNameOfQueuedEvent = queuedEvent.getName();
                if (eventNameOfQueuedEvent.startsWith(processId)) {
                    eventsToRemove.add(queuedEvent);

                    ProcessInstance processInstance = queuedEvent.getProcessInstance();
                    while (processInstance.getParent() != null) {
                        // we want the id of the process instance, not of any sub process instance
                        processInstance = processInstance.getParent();
                    }
                    int idOfTopProcessInstance = processInstance.getId();
                    idsOfProcessInstancesToAbort.add(idOfTopProcessInstance);
                }
            }
            queue.removeAll(eventsToRemove);
        }
        return idsOfProcessInstancesToAbort;
    }
    
    public void removeFromEventQueues(ScyllaEvent event) {
        ProcessSimulationComponents simulationComponents = event.getSimulationComponents();
        int nodeId = event.getNodeId();
        Set<ResourceReference> resourceReferences = simulationComponents.getSimulationConfiguration()
                .getResourceReferenceSet(nodeId);
        for (ResourceReference ref : resourceReferences) {
            // remove from event queues
            String resourceId = ref.getResourceId();
            ScyllaEventQueue eventQueue = getEventQueues().get(resourceId);
            eventQueue.remove(event);
        }
    }
    
	/**
     * Immediately schedules all possible events that become ready through updates at the given resources
     * (As multiple events might wait for one resource, most likely not all waiting events for that resource will be scheduled)
     * @param resourceQueuesUpdated : Set of ids of resources that have been updated (usually have become available again)
     * @throws ScyllaRuntimeException
     */
    public void scheduleAllEventsFromQueueReadyForSchedule(Set<String> resourceQueuesUpdated) throws ScyllaRuntimeException {
    	ScyllaEvent eventFromQueue = getEventFromQueueReadyForSchedule(resourceQueuesUpdated);
        while (eventFromQueue != null) {
        	SimulationUtils.scheduleEvent(eventFromQueue, new TimeSpan(0));
            eventFromQueue = getEventFromQueueReadyForSchedule(resourceQueuesUpdated);
        }
    }
}
