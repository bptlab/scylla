package de.hpi.bpt.scylla.simulation;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ResourceInfo;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.global.GlobalConfiguration;
import de.hpi.bpt.scylla.model.global.resource.DynamicResource;
import de.hpi.bpt.scylla.model.global.resource.DynamicResourceInstance;
import de.hpi.bpt.scylla.model.global.resource.Resource;
import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.model.process.CommonProcessElements;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.parser.EventOrderType;
import de.hpi.bpt.scylla.simulation.event.ProcessInstanceGenerationEvent;
import de.hpi.bpt.scylla.simulation.event.ProcessSimulationStopEvent;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
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

    private Map<String, ResourceQueue> resourceObjects = new HashMap<String, ResourceQueue>();

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

            if (globalConfiguration != null) {
                convertToResourceObjects(globalConfiguration.getResources());
            }

            // prepare queue and sorting order
            // TODO each resource has its own assignment order
            List<EventOrderType> resourceAssignmentOrder = globalConfiguration.getResourceAssignmentOrder();
            for (String resourceId : resourceObjects.keySet()) {
                ScyllaEventQueue eventQueue = new ScyllaEventQueue(resourceId, resourceAssignmentOrder);
                eventQueues.put(resourceId, eventQueue);
            }
        }
        catch (InstantiationException e) {
            DebugLogger.error(e.getMessage());
            DebugLogger.error("Instantiation of simulation model failed.");
        }
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
                    ResourceObject resObject = new ResourceObject(resourceType, resourceInstanceName, cost, timeUnit,
                            timetable);
                    resQueue.add(resObject);

                    boolean availableAtStart = resObject.isAvailable(startDateTime);
                    SimulationUtils.scheduleNextResourceAvailableEvent(this, resObject, startDateTime,
                            availableAtStart);
                }
            }
            else {
                throw new InstantiationException("Type of resource " + resourceType + " not supported.");
            }
            resourceObjects.put(resourceType, resQueue);
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

    public Map<String, ResourceQueue> getResourceObjects() {
        return resourceObjects;
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
}
