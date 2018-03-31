package de.hpi.bpt.scylla.plugin.statslogger;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.logger.ResourceInfo;
import de.hpi.bpt.scylla.logger.ResourceStatus;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import desmoj.core.simulator.TimeInstant;

import org.apache.xml.serialize.XMLSerializer;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class StatisticsLogger extends OutputLoggerPluggable {

    @Override
    public String getName() {
        return "KPI";
    }

    public void writeToLog(SimulationModel model, String outputPathWithoutExtension) throws IOException {

        TimeUnit timeUnit = DateTimeUtils.getReferenceTimeUnit();
        double totalEndTime = model.presentTime().getTimeAsDouble(timeUnit);
        Map<String, Map<Integer, List<ProcessNodeInfo>>> processNodeInfos = model.getProcessNodeInfos();
        Map<String, Map<String, List<ResourceInfo>>> resourceInfos = model.getResourceInfos();

        Set<ResourceObject> resourceObjectsSet = QueueManager.getAllResourceObjects(model);
        Map<String, Double> costPerResourceInstance = new HashMap<String, Double>();
        Map<String, Map<String, ResourceObject>> resourceObjects = new HashMap<String, Map<String, ResourceObject>>();
        for (ResourceObject res : resourceObjectsSet) {
            String resourceName = res.getResourceType() + "_" + res.getId();

            TimeUnit timeUnitOfResource = res.getTimeUnit();
            TimeUnit referenceUnit = DateTimeUtils.getReferenceTimeUnit();
            double cost = res.getCost();
            double costInReferenceUnit = DateTimeUtils.convertCost(timeUnitOfResource, referenceUnit, cost);
            costPerResourceInstance.put(resourceName, costInReferenceUnit);

            String resourceType = res.getResourceType();
            if (!resourceObjects.containsKey(resourceType)) {
                resourceObjects.put(resourceType, new HashMap<String, ResourceObject>());
            }
            String resourceId = res.getId();
            resourceObjects.get(resourceType).put(resourceId, res);
        }

        // resource utilization

        Map<String, Map<String, StatisticsResourceObject>> statsPerResource = new HashMap<String, Map<String, StatisticsResourceObject>>();
        // double totalEndTime = 0;
        for (String resourceType : resourceInfos.keySet()) {
            Map<String, StatisticsResourceObject> statsPerResourceInstance = new HashMap<String, StatisticsResourceObject>();
            Map<String, List<ResourceInfo>> resourceInfosOfType = resourceInfos.get(resourceType);
            for (String resourceId : resourceInfosOfType.keySet()) {

                long durationInUse = 0;
                long durationInUseIdle = 0;
                long currentTime = 0;

                List<ResourceInfo> infosOfResource = resourceInfosOfType.get(resourceId);
                ResourceStatus previousStatus = null;

                for (ResourceInfo info : infosOfResource) {

                    ResourceStatus status = info.getTransition();
                    long timestamp = info.getTimestamp();

                    // FREE <-> IN_USE <-> IN_USE_IDLE
                    if (status == ResourceStatus.IN_USE) {
                        if (previousStatus == ResourceStatus.IN_USE_IDLE) {
                            durationInUseIdle += timestamp - currentTime;
                        }
                    }
                    else if (status == ResourceStatus.FREE || status == ResourceStatus.IN_USE_IDLE) {
                        durationInUse += timestamp - currentTime;
                    }
                    else {
                        DebugLogger.log("Resource transition type not supported: " + status);
                    }
                    currentTime = timestamp;

                    // if (currentTime > totalEndTime) {
                    // totalEndTime = currentTime;
                    // }
                }

                long durationAvailable = DateTimeUtils.getAvailabilityTime(
                        resourceObjects.get(resourceType).get(resourceId).getTimetable(),
                        new TimeInstant(totalEndTime));

                String resourceName = resourceType + "_" + resourceId;
                Double costPerUnit = costPerResourceInstance.get(resourceName);
                double costs = durationAvailable * costPerUnit;

                StatisticsResourceObject sro = new StatisticsResourceObject();
                sro.setDurationAvailable(durationAvailable);
                sro.setDurationInUse(durationInUse);
                sro.setCosts(costs); // for total available time
                sro.setDurationInUseIdle(durationInUseIdle);
                statsPerResourceInstance.put(resourceId, sro);
            }
            statsPerResource.put(resourceType, statsPerResourceInstance);
        }

        // process time with and without off-timetable hours (= idle time)
        // process waiting time
        // process costs
        // task duration
        // task waiting time

        Map<String, Map<Integer, StatisticsProcessInstanceObject>> statsPerProcess = new HashMap<String, Map<Integer, StatisticsProcessInstanceObject>>();
        Map<String, Map<String, Map<String, StatisticsTaskInstanceObject>>> statsPerTask = new HashMap<String, Map<String, Map<String, StatisticsTaskInstanceObject>>>();

        for (String processId : processNodeInfos.keySet()) {

            Map<Integer, StatisticsProcessInstanceObject> statsPerProcessInstance = new HashMap<Integer, StatisticsProcessInstanceObject>();
            Map<String, Map<String, StatisticsTaskInstanceObject>> statsPerTaskOfProcess = new HashMap<String, Map<String, StatisticsTaskInstanceObject>>();

            Map<Integer, List<ProcessNodeInfo>> nodeInfoOfProcessInstances = processNodeInfos.get(processId);
            for (int processInstanceId : nodeInfoOfProcessInstances.keySet()) {

                List<ProcessNodeInfo> nodeInfoList = nodeInfoOfProcessInstances.get(processInstanceId);

                long durationTotal = 0;
                long durationInactive = 0;
                long durationResourcesIdle = 0;
                long durationWaiting = 0;
                double costs = 0;

                Long previousTimestamp = null;
                long timeProcessStart = 0;

                Map<TaskInstanceIdentifier, Long> taskDurations = new HashMap<TaskInstanceIdentifier, Long>();
                Map<TaskInstanceIdentifier, Long> begunOrResumedTasks = new HashMap<TaskInstanceIdentifier, Long>();

                Map<TaskInstanceIdentifier, Long> enabledTasks = new HashMap<TaskInstanceIdentifier, Long>();
                Map<TaskInstanceIdentifier, Long> pausedTasks = new HashMap<TaskInstanceIdentifier, Long>();
                for (int i = 0; i < nodeInfoList.size(); i++) {
                	
                	String taskInstanceId = String.valueOf(i);
                	long taskDurationEffective = 0;
                    long taskDurationResourcesIdle = 0;
                    long taskDurationWaiting = 0;
                    double taskCosts = 0;

                    ProcessNodeInfo ni = nodeInfoList.get(i);
                    long timestamp = ni.getTimestamp();
                    String processScopeNodeId = ni.getProcessScopeNodeId();
                    String source = ni.getSource();
                    TaskInstanceIdentifier taskInstanceIdentifier = new TaskInstanceIdentifier(processScopeNodeId,
                            source);
                    
                    Set<String> resources = ni.getResources();
                    
                    String taskName = ni.getTaskName();
                    StatisticsTaskInstanceObject stio = new StatisticsTaskInstanceObject(taskName);

                    if (i == 0) {
                        previousTimestamp = timestamp;
                        timeProcessStart = timestamp;
                    }
                    else if (i == nodeInfoList.size() - 1) {
                        durationTotal = timestamp - timeProcessStart;
                    }

                    ProcessNodeTransitionType transition = ni.getTransition();

                    if (begunOrResumedTasks.isEmpty()) {
                        durationInactive += timestamp - previousTimestamp;
                    }
                    if (transition == ProcessNodeTransitionType.ENABLE) {
                        enabledTasks.put(taskInstanceIdentifier, timestamp);
                    }
                    else if (transition == ProcessNodeTransitionType.BEGIN) {
                        Long enableTimestamp = enabledTasks.get(taskInstanceIdentifier);
                        if (enableTimestamp != null) {
                            long duration = timestamp - enableTimestamp;
                            durationWaiting += duration;
                            taskDurationWaiting += duration;
                            enabledTasks.remove(taskInstanceIdentifier);
                        }

                        taskDurations.put(taskInstanceIdentifier, 0L);
                        begunOrResumedTasks.put(taskInstanceIdentifier, timestamp);
                    }
                    else if (transition == ProcessNodeTransitionType.PAUSE) {
                        pausedTasks.put(taskInstanceIdentifier, timestamp);

                        Long beginOrResumeTimestamp = begunOrResumedTasks.get(taskInstanceIdentifier);
                        long duration = timestamp - beginOrResumeTimestamp;
                        for (String resourceName : resources) {
                            Double costPerUnit = costPerResourceInstance.get(resourceName);
                            costs += duration * costPerUnit;
                            taskCosts += duration * costPerUnit;
                        }
                        taskDurations.put(taskInstanceIdentifier, taskDurations.get(taskInstanceIdentifier) + duration);
                        begunOrResumedTasks.remove(taskInstanceIdentifier);
                    }
                    else if (transition == ProcessNodeTransitionType.RESUME) {
                        Long pauseTimestamp = pausedTasks.get(taskInstanceIdentifier);
                        long duration = timestamp - pauseTimestamp;
                        durationResourcesIdle += duration;
                        taskDurationResourcesIdle += duration;
                        pausedTasks.remove(taskInstanceIdentifier);

                        begunOrResumedTasks.put(taskInstanceIdentifier, timestamp);
                    }
                    else if (transition == ProcessNodeTransitionType.TERMINATE
                            || transition == ProcessNodeTransitionType.CANCEL) {
                        Long beginOrResumeTimestamp = begunOrResumedTasks.get(taskInstanceIdentifier);
                        long duration = timestamp - beginOrResumeTimestamp;
                        for (String resourceName : resources) {
                            Double costPerUnit = costPerResourceInstance.get(resourceName);
                            costs += duration * costPerUnit;
                            taskCosts += duration * costPerUnit;
                        }

                        taskDurationEffective = taskDurations.get(taskInstanceIdentifier) + duration;
                        taskDurations.remove(taskInstanceIdentifier);
                        begunOrResumedTasks.remove(taskInstanceIdentifier);
                    }
                    else if (transition == ProcessNodeTransitionType.EVENT_BEGIN
                            || transition == ProcessNodeTransitionType.EVENT_TERMINATE) {
                        // not supported
                    }

                    stio.setDurationEffective(taskDurationEffective);
                    stio.setDurationResourcesIdle(taskDurationResourcesIdle);
                    stio.setDurationWaiting(taskDurationWaiting);
                    stio.setCost(taskCosts);
                    
                    if (!statsPerTaskOfProcess.containsKey(processScopeNodeId)) {
                        statsPerTaskOfProcess.put(processScopeNodeId, new HashMap<String, StatisticsTaskInstanceObject>());
                    }
                    
                    statsPerTaskOfProcess.get(processScopeNodeId).put(taskInstanceId, stio);

                    previousTimestamp = ni.getTimestamp();
                }

                StatisticsProcessInstanceObject spio = new StatisticsProcessInstanceObject();
                spio.setDurationTotal(durationTotal);
                spio.setDurationInactive(durationInactive);
                spio.setDurationResourcesIdle(durationResourcesIdle);
                spio.setDurationWaiting(durationWaiting);
                spio.setCosts(costs);
                statsPerProcessInstance.put(processInstanceId, spio);
            }
            statsPerProcess.put(processId, statsPerProcessInstance);
            statsPerTask.put(processId, statsPerTaskOfProcess);
        }

        // build xml document

        Element resourceUtilization = new Element("resourceUtilization");
        
        Document doc = new Document(resourceUtilization);
        
        Element configuration = new Element("configuration");
        Element processes = new Element("processes");
        Element resources = new Element("resources");
        
        doc.getRootElement().addContent(configuration);
        doc.getRootElement().addContent(processes);
        doc.getRootElement().addContent(resources);
        
        configuration.addContent(new Element("time_unit")
        		.setText(String.valueOf(DateTimeUtils.getReferenceTimeUnit())));
        
        // add processes
        for (String processId : statsPerProcess.keySet()) {
        	Map<Integer, StatisticsProcessInstanceObject> statsPerProcessInstance = statsPerProcess.get(processId);
            
        	Element process = new Element("process");
        	processes.addContent(process);
        	
        	process.addContent(new Element("id").setText(processId));
        	Element processCost = new Element("cost");
        	Element processTime = new Element("time");
        	Element processInstances = new Element("instances");
        	Element processActivities = new Element("activities");
        	process.addContent(processCost);
        	process.addContent(processTime);
        	process.addContent(processInstances);
        	process.addContent(processActivities);
        	
        	Element processFlowTime = new Element("flow_time");
        	Element processEffectiveTime = new Element("effective");
        	Element processWaitingTime = new Element("waiting");
        	Element processOffTime = new Element("off_timetable");
        	processTime.addContent(processFlowTime);
        	processTime.addContent(processEffectiveTime);
        	processTime.addContent(processWaitingTime);
        	processTime.addContent(processOffTime);
        	
        	StatisticsCalculationObject costStats = new StatisticsCalculationObject();
            StatisticsCalculationObject flowTimeStats = new StatisticsCalculationObject();
            StatisticsCalculationObject effectiveStats = new StatisticsCalculationObject();
            StatisticsCalculationObject offTimeStats = new StatisticsCalculationObject();
            StatisticsCalculationObject waitingStats = new StatisticsCalculationObject();
            
            // add process instances
            for (Integer processInstanceId : statsPerProcessInstance.keySet()) {
                StatisticsProcessInstanceObject stats = statsPerProcessInstance.get(processInstanceId);
                
                long durationTotal = stats.getDurationTotal();
                long durationEffective = durationTotal - stats.getDurationInactive();
                long durationResourcesIdle = stats.getDurationResourcesIdle();
                long durationWaiting = stats.getDurationWaiting();
                double cost = stats.getCosts();
                
                Element instance = new Element("instance");
                processInstances.addContent(instance);
                
                instance.addContent(new Element("costs").setText(String.valueOf(cost)));
                Element instanceTime = new Element("time");
                instanceTime.addContent(new Element("duration").setText(String.valueOf(durationTotal)));
                instanceTime.addContent(new Element("effective").setText(String.valueOf(durationEffective)));
                instanceTime.addContent(new Element("waiting").setText(String.valueOf(durationWaiting)));
                instanceTime.addContent(new Element("offTime").setText(String.valueOf(durationResourcesIdle)));
                instance.addContent(instanceTime);
                
                costStats.addValue(cost);
                flowTimeStats.addValue(durationTotal);
                effectiveStats.addValue(durationEffective);
                offTimeStats.addValue(durationResourcesIdle);
                waitingStats.addValue(durationWaiting);
            }
            costStats.calculateStatistics();
            flowTimeStats.calculateStatistics();
            effectiveStats.calculateStatistics();
            waitingStats.calculateStatistics();
            offTimeStats.calculateStatistics();
            
            processCost.addContent(costStats.getStatsAsElements());
            processFlowTime.addContent(flowTimeStats.getStatsAsElements());
            processEffectiveTime.addContent(effectiveStats.getStatsAsElements());
            processWaitingTime.addContent(waitingStats.getStatsAsElements());
            processOffTime.addContent(offTimeStats.getStatsAsElements());
        
            
            Map<String, Map<String, StatisticsTaskInstanceObject>> statsPerTaskOfProcess = statsPerTask.get(processId);
            // add activities
            for (String processScopeNodeId : statsPerTaskOfProcess.keySet()) {
            	
            	long taskDuration = 0;
            	for (StatisticsTaskInstanceObject instance : statsPerTaskOfProcess.get(processScopeNodeId).values()) {
            		taskDuration += instance.getDurationEffective();
            	}
            	// skip tasks with zero duration (which are most likely events)
            	if (taskDuration == 0) continue;
            	
            	StatisticsCalculationObject taskCostStats = new StatisticsCalculationObject();
	            StatisticsCalculationObject taskDurationStats = new StatisticsCalculationObject();
	            StatisticsCalculationObject taskWaitingStats = new StatisticsCalculationObject();
	            StatisticsCalculationObject taskResourcesIdleStats = new StatisticsCalculationObject();
	            Map<String, StatisticsTaskInstanceObject> statsPerTaskInstance = statsPerTaskOfProcess.get(processScopeNodeId);
	            String taskName = "";
	            
	            Element activity = new Element("activity");
	            processActivities.addContent(activity);
	            
	            activity.addContent(new Element("id").setText(processScopeNodeId));
	            Element activityName = new Element("name");
	            Element activityCost = new Element("cost");
	            Element activityTime = new Element("time");
	            Element activityInstances = new Element("instances");
	        	activity.addContent(activityName);
	            activity.addContent(activityCost);
	            activity.addContent(activityTime);
	            activity.addContent(activityInstances);
	            
	        	Element activityDurationTime = new Element("duration");
	        	Element activityWaitingTime = new Element("waiting");
	        	Element activityResourcesIdleTime = new Element("resources_idle");
	        	activityTime.addContent(activityDurationTime);
	        	activityTime.addContent(activityWaitingTime);
	        	activityTime.addContent(activityResourcesIdleTime);
	        	
	        	// add activity instances
	        	for (String taskInstanceId : statsPerTaskInstance.keySet()) {
            		StatisticsTaskInstanceObject stats = statsPerTaskInstance.get(taskInstanceId);
	                
            		if (taskName.isEmpty()) {
            			taskName = stats.getTaskName();
            		}
            		long durationEffective = stats.getDurationEffective();
            	    long durationResourcesIdle = stats.getDurationResourcesIdle();
            	    long durationWaiting = stats.getDurationWaiting();
            	    double cost = stats.getCost();
            	    
            	    Element activityInstance = new Element("instance");
            	    activityInstances.addContent(activityInstance);
            	   
            	    activityInstance.addContent(new Element("cost").setText(String.valueOf(cost)));
            	    Element activityInstanceTime = new Element("time");
            	    activityInstanceTime.addContent(new Element("effective").setText(String.valueOf(durationEffective)));
            	    activityInstanceTime.addContent(new Element("waiting").setText(String.valueOf(durationWaiting)));
            	    activityInstanceTime.addContent(new Element("resources_idle").setText(String.valueOf(durationResourcesIdle)));
            	    activityInstance.addContent(activityInstanceTime);
            	                	    
            	    taskCostStats.addValue(cost);
            	    taskDurationStats.addValue(durationEffective);
            	    taskWaitingStats.addValue(durationWaiting);
            	    taskResourcesIdleStats.addValue(durationResourcesIdle);
            	}
	        	
	        	activityName.setText(taskName);
	        	
	        	taskCostStats.calculateStatistics();
	            taskDurationStats.calculateStatistics();
	            taskWaitingStats.calculateStatistics();
	            taskResourcesIdleStats.calculateStatistics();
	            
	            activityCost.addContent(taskCostStats.getStatsAsElements());
	            activityDurationTime.addContent(taskDurationStats.getStatsAsElements());
	            activityWaitingTime.addContent(taskWaitingStats.getStatsAsElements());
	            activityResourcesIdleTime.addContent(taskResourcesIdleStats.getStatsAsElements());
            }

            // add resources
            for (String resourceType : statsPerResource.keySet()) {
	            Map<String, StatisticsResourceObject> statsPerResourceInstance = statsPerResource.get(resourceType);
	            
	            Element resource = new Element("resource");
	            resources.addContent(resource);
	            
	            resource.addContent(new Element("type").setText(resourceType));
	            Element resourceCost = new Element("cost");
	            Element resourceTime = new Element("time");
	            Element resourceInstances = new Element("instances");
	            resource.addContent(resourceCost);
	            resource.addContent(resourceTime);
	            resource.addContent(resourceInstances);
	            
	            Element resourceInUse = new Element("in_use");
	            Element resourceAvailable = new Element("available");
	            Element resourceWorkload = new Element("workload");
	            resourceTime.addContent(resourceInUse);
	            resourceTime.addContent(resourceAvailable);
	            resourceTime.addContent(resourceWorkload);
	            
	            StatisticsCalculationObject resourceCostStats = new StatisticsCalculationObject();
                StatisticsCalculationObject resourceInUseStats = new StatisticsCalculationObject();
                StatisticsCalculationObject resourceAvailableStats = new StatisticsCalculationObject();
                StatisticsCalculationObject resourceWorkloadStats = new StatisticsCalculationObject();
                
                // add resource instances
	            for (String resourceId : statsPerResourceInstance.keySet()) {
	                StatisticsResourceObject stats = statsPerResourceInstance.get(resourceId);
	                long durationInUse = stats.getDurationInUse();
	                long durationAvailable = stats.getDurationAvailable();
	                double percentageInUse = durationInUse / (double) durationAvailable;
	                double cost = stats.getCosts();
	                
	                Element resourceInstance = new Element("instance");
	                resourceInstances.addContent(resourceInstance);
	                
	                resourceInstance.addContent(new Element("id").setText(resourceId));
	                resourceInstance.addContent(new Element("cost").setText(String.valueOf(cost)));
	                Element resourceInstanceTime = new Element("time");
	                resourceInstanceTime.addContent(new Element("in_use").setText(String.valueOf(durationInUse)));
	                resourceInstanceTime.addContent(new Element("available").setText(String.valueOf(durationAvailable)));
	                resourceInstanceTime.addContent(new Element("workload").setText(String.valueOf(percentageInUse)));
	                resourceInstance.addContent(resourceInstanceTime);
	                
	                resourceCostStats.addValue(cost);
	                resourceInUseStats.addValue(durationInUse);
	                resourceAvailableStats.addValue(durationAvailable);
	                resourceWorkloadStats.addValue(percentageInUse);
	            }
                resourceCostStats.calculateStatistics();
                resourceInUseStats.calculateStatistics();
                resourceAvailableStats.calculateStatistics();
                resourceWorkloadStats.calculateStatistics();
                
                resourceCost.addContent(resourceCostStats.getStatsAsElements());
                resourceInUse.addContent(resourceInUseStats.getStatsAsElements());
                resourceAvailable.addContent(resourceAvailableStats.getStatsAsElements());
                resourceWorkload.addContent(resourceWorkloadStats.getStatsAsElements());
            }
        }
        
        // print
        
        String resourceUtilizationFileName = outputPathWithoutExtension + model.getGlobalConfiguration().getFileNameWithoutExtension()+"_resourceutilization.xml";
        FileOutputStream fos = new FileOutputStream(resourceUtilizationFileName);



        XMLOutputter xmlOutput = new XMLOutputter();
	    xmlOutput.setFormat(Format.getPrettyFormat());
        xmlOutput.output(doc, fos);
    }




    class TaskInstanceIdentifier {

        String processScopeNodeId;
        String source;

        TaskInstanceIdentifier(String processScopeNodeId, String source) {
            this.processScopeNodeId = processScopeNodeId;
            this.source = source;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof TaskInstanceIdentifier) {
                TaskInstanceIdentifier tio = (TaskInstanceIdentifier) object;
                if (this.processScopeNodeId.equals(tio.processScopeNodeId) && this.source.equals(tio.source)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return processScopeNodeId.hashCode() + source.hashCode();
        }

    }

}
