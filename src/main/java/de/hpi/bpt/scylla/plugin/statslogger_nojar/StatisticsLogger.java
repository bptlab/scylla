package de.hpi.bpt.scylla.plugin.statslogger_nojar;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.logger.ResourceInfo;
import de.hpi.bpt.scylla.logger.ResourceStatus;
import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import desmoj.core.simulator.TimeInstant;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

//import org.json.JSONObject;
//import org.json.XML;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
                	
                	// neu
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
                    //cost, waiting, duration
                    else if (i == nodeInfoList.size() - 1) {
                        durationTotal = timestamp - timeProcessStart;
                    }

                    ProcessNodeTransitionType transition = ni.getTransition();

                    if (begunOrResumedTasks.isEmpty()) {
                        durationInactive += timestamp - previousTimestamp;
                    }
                    // while transitiontype as below: waiting
                    if (transition == ProcessNodeTransitionType.ENABLE) {
                        enabledTasks.put(taskInstanceIdentifier, timestamp);
                    }
                    // when is a task waiting?
                    // duration
                    else if (transition == ProcessNodeTransitionType.BEGIN) {
                        Long enableTimestamp = enabledTasks.get(taskInstanceIdentifier);
                        if (enableTimestamp != null) {
                            long duration = timestamp - enableTimestamp;
                            durationWaiting += duration;
                            taskDurationWaiting += duration; // new
                            enabledTasks.remove(taskInstanceIdentifier);

                            //stio.setDurationWaiting(duration);
                        }

                        taskDurations.put(taskInstanceIdentifier, 0L);
                        begunOrResumedTasks.put(taskInstanceIdentifier, timestamp);
                    }
                    // resourcesIdle
                    else if (transition == ProcessNodeTransitionType.PAUSE) {
                        pausedTasks.put(taskInstanceIdentifier, timestamp);

                        Long beginOrResumeTimestamp = begunOrResumedTasks.get(taskInstanceIdentifier);
                        long duration = timestamp - beginOrResumeTimestamp;
                        for (String resourceName : resources) {
                            Double costPerUnit = costPerResourceInstance.get(resourceName);
                            costs += duration * costPerUnit;
                            taskCosts += duration * costPerUnit; // new
                        }
                        //stio.setCost(taskCosts);
                        taskDurations.put(taskInstanceIdentifier, taskDurations.get(taskInstanceIdentifier) + duration);
                        begunOrResumedTasks.remove(taskInstanceIdentifier);
                    }
                    // duration
                    else if (transition == ProcessNodeTransitionType.RESUME) {
                        Long pauseTimestamp = pausedTasks.get(taskInstanceIdentifier);
                        long duration = timestamp - pauseTimestamp;
                        durationResourcesIdle += duration;
                        taskDurationResourcesIdle += duration; // new
                        pausedTasks.remove(taskInstanceIdentifier);

                        begunOrResumedTasks.put(taskInstanceIdentifier, timestamp);
                    }
                    // nothing anymore
                    else if (transition == ProcessNodeTransitionType.TERMINATE
                            || transition == ProcessNodeTransitionType.CANCEL) {
                        Long beginOrResumeTimestamp = begunOrResumedTasks.get(taskInstanceIdentifier);
                        long duration = timestamp - beginOrResumeTimestamp;
                        for (String resourceName : resources) {
                            Double costPerUnit = costPerResourceInstance.get(resourceName);
                            costs += duration * costPerUnit;
                            taskCosts += duration * costPerUnit; // new
                        }

                        // taskDurationEffetive without off timetable hours where resources are idle
                        // total duration time of task
                        taskDurationEffective = taskDurations.get(taskInstanceIdentifier) + duration;
                        //stio.setDurationEffective(taskDurationEffective);
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
                        // hier leere Map, an die geadded wird, wenn ich eine neue TaskInstanz dazufüge
                        statsPerTaskOfProcess.put(processScopeNodeId, new HashMap<String, StatisticsTaskInstanceObject>());
                        //statsPerTaskOfProcess.put(processScopeNodeId, new StatisticsTaskInstanceObject(taskName));
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

        // print

        try {
	        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("resourceUtilization");
			doc.appendChild(rootElement);
			
			Element configuration = doc.createElement("configuration");
			rootElement.appendChild(configuration);

			Element referenceUnit = doc.createElement("time_unit");
			referenceUnit.appendChild(doc.createTextNode(String.valueOf(DateTimeUtils.getReferenceTimeUnit())));
			configuration.appendChild(referenceUnit);
			
	        for (String processId : statsPerProcess.keySet()) {
	            Map<Integer, StatisticsProcessInstanceObject> statsPerProcessInstance = statsPerProcess.get(processId);
	            
	            Element processes = doc.createElement("process");
	    		rootElement.appendChild(processes);

        		Element processIdElement = doc.createElement("id");
        		processIdElement.appendChild(doc.createTextNode(processId));
        		processes.appendChild(processIdElement);
	    		
        		// set up
        		
	            Element processCostStats = doc.createElement("cost");
	            processes.appendChild(processCostStats);
	            
	            Element processCostMin = doc.createElement("min");
	            processCostStats.appendChild(processCostMin);
	    		Element processCostMax = doc.createElement("max");
	            processCostStats.appendChild(processCostMax);
	    		Element processCostMedian = doc.createElement("median");
	            processCostStats.appendChild(processCostMedian);
	    		Element processCostQ1 = doc.createElement("Q1");
	            processCostStats.appendChild(processCostQ1);
	    		Element processCostQ3 = doc.createElement("Q3");
	            processCostStats.appendChild(processCostQ3);
	    		Element processCostAvg = doc.createElement("avg");
	            processCostStats.appendChild(processCostAvg);
	            Element processCostTotal = doc.createElement("total");
	            processCostStats.appendChild(processCostTotal);
	            
		        Element processTimeStats = doc.createElement("time");
		        processes.appendChild(processTimeStats);
		        
		        
	    		Element processFlowTime = doc.createElement("flow_time");
		        processTimeStats.appendChild(processFlowTime);
		        Element processEffectiveStats = doc.createElement("effective");
		        processTimeStats.appendChild(processEffectiveStats);
		        Element processWaitingStats = doc.createElement("waiting");
		        processTimeStats.appendChild(processWaitingStats);
		        Element processOffTimeStats = doc.createElement("off_timetable");
		        processTimeStats.appendChild(processOffTimeStats);

	            Element processFlowTimeMin = doc.createElement("min");
	            processFlowTime.appendChild(processFlowTimeMin);
	            Element processFlowTimeMax = doc.createElement("max");
	            processFlowTime.appendChild(processFlowTimeMax);
	            Element processFlowTimeMedian = doc.createElement("median");
	            processFlowTime.appendChild(processFlowTimeMedian);
	            Element processFlowTimeQ1 = doc.createElement("Q1");
	            processFlowTime.appendChild(processFlowTimeQ1);
	            Element processFlowTimeQ3 = doc.createElement("Q3");
	            processFlowTime.appendChild(processFlowTimeQ3);
	            Element processFlowTimeAvg = doc.createElement("avg");
	            processFlowTime.appendChild(processFlowTimeAvg);
	            Element processFlowTimeTotal = doc.createElement("total");
	            processFlowTime.appendChild(processFlowTimeTotal);
	            
	            Element processEffectiveMin = doc.createElement("min");
	            processEffectiveStats.appendChild(processEffectiveMin);
	            Element processEffectiveMax = doc.createElement("max");
	            processEffectiveStats.appendChild(processEffectiveMax);
	            Element processEffectiveMedian = doc.createElement("median");
	            processEffectiveStats.appendChild(processEffectiveMedian);
	            Element processEffectiveQ1 = doc.createElement("Q1");
	            processEffectiveStats.appendChild(processEffectiveQ1);
	            Element processEffectiveQ3 = doc.createElement("Q3");
	            processEffectiveStats.appendChild(processEffectiveQ3);
	            Element processEffectiveAvg = doc.createElement("avg");
	            processEffectiveStats.appendChild(processEffectiveAvg);
	            Element processEffectiveTotal = doc.createElement("total");
	            processEffectiveStats.appendChild(processEffectiveTotal);

	            Element processWaitingMin = doc.createElement("min");
	            processWaitingStats.appendChild(processWaitingMin);
	            Element processWaitingMax = doc.createElement("max");
	            processWaitingStats.appendChild(processWaitingMax);
	            Element processWaitingMedian = doc.createElement("median");
	            processWaitingStats.appendChild(processWaitingMedian);
	            Element processWaitingQ1 = doc.createElement("Q1");
	            processWaitingStats.appendChild(processWaitingQ1);
	            Element processWaitingQ3 = doc.createElement("Q3");
	            processWaitingStats.appendChild(processWaitingQ3);
	            Element processWaitingAvg = doc.createElement("avg");
	            processWaitingStats.appendChild(processWaitingAvg);
	            Element processWaitingTotal = doc.createElement("total");
	            processWaitingStats.appendChild(processWaitingTotal);
	            
	            Element processOffTimeMin = doc.createElement("min");
	            processOffTimeStats.appendChild(processOffTimeMin);
	            Element processOffTimeMax = doc.createElement("max");
	            processOffTimeStats.appendChild(processOffTimeMax);
	            Element processOffTimeMedian = doc.createElement("median");
	            processOffTimeStats.appendChild(processOffTimeMedian);
	            Element processOffTimeQ1 = doc.createElement("Q1");
	            processOffTimeStats.appendChild(processOffTimeQ1);
	            Element processOffTimeQ3 = doc.createElement("Q3");
	            processOffTimeStats.appendChild(processOffTimeQ3);
	            Element processOffTimeAvg = doc.createElement("avg");
	            processOffTimeStats.appendChild(processOffTimeAvg);
	            Element processOffTimeTotal = doc.createElement("total");
	            processOffTimeStats.appendChild(processOffTimeTotal);

	            // should be renamed
	            StatisticsCalculationObject costStats = new StatisticsCalculationObject();
	            StatisticsCalculationObject flowTimeStats = new StatisticsCalculationObject();
	            StatisticsCalculationObject effectiveStats = new StatisticsCalculationObject();
	            StatisticsCalculationObject offTimetableStats = new StatisticsCalculationObject();
	            StatisticsCalculationObject waitingStats = new StatisticsCalculationObject();
	            for (Integer processInstanceId : statsPerProcessInstance.keySet()) {
	                StatisticsProcessInstanceObject stats = statsPerProcessInstance.get(processInstanceId);

	                Element instance = doc.createElement("instance");
	                processes.appendChild(instance);
	        		
	                long durationTotal = stats.getDurationTotal();
	                long durationEffective = durationTotal - stats.getDurationInactive();
	                double percentageEffective = durationEffective / (double) durationTotal;
	
	                long durationResourcesIdle = stats.getDurationResourcesIdle();
	                long durationWaiting = stats.getDurationWaiting();
	
	                double cost = stats.getCosts();

	                Element instanceCosts = doc.createElement("costs");
	                instanceCosts.appendChild(doc.createTextNode(String.valueOf(cost)));
	                instance.appendChild(instanceCosts);

	                Element instanceTime = doc.createElement("time");
	                instance.appendChild(instanceTime);
	                	                
	                Element instanceDuration = doc.createElement("duration");
	                instanceDuration.appendChild(doc.createTextNode(String.valueOf(durationTotal)));
	                instanceTime.appendChild(instanceDuration);
	                
	                Element instanceEffective = doc.createElement("effective");
	                instanceEffective.appendChild(doc.createTextNode(String.valueOf(durationEffective)));
	                instanceTime.appendChild(instanceEffective);
	                
	                Element instanceWaiting = doc.createElement("waiting");
	                instanceWaiting.appendChild(doc.createTextNode(String.valueOf(durationWaiting)));
	                instanceTime.appendChild(instanceWaiting);

	                Element instanceOffTime = doc.createElement("offTime");
	                instanceOffTime.appendChild(doc.createTextNode(String.valueOf(durationResourcesIdle)));
	                instanceTime.appendChild(instanceOffTime);
	                
	                // not used: percentageEffective
	                
	                costStats.addValue(cost);
	                flowTimeStats.addValue(durationTotal);
	                effectiveStats.addValue(durationEffective);
	                offTimetableStats.addValue(durationResourcesIdle);
	                waitingStats.addValue(durationWaiting);
	            }
		        costStats.calculateStatistics();
	            flowTimeStats.calculateStatistics();
	            effectiveStats.calculateStatistics();
	            waitingStats.calculateStatistics();
	            offTimetableStats.calculateStatistics();
	            
	            // Befüllen der oben angelegten Elemente
                // costs
	            processCostMin.appendChild(doc.createTextNode(String.valueOf(costStats.getMin())));
                processCostMax.appendChild(doc.createTextNode(String.valueOf(costStats.getMax())));
                processCostMedian.appendChild(doc.createTextNode(String.valueOf(costStats.getMedian())));
                processCostQ1.appendChild(doc.createTextNode(String.valueOf(costStats.getQ1())));
                processCostQ3.appendChild(doc.createTextNode(String.valueOf(costStats.getQ3())));
                processCostAvg.appendChild(doc.createTextNode(String.valueOf(costStats.getAverage())));
		        processCostTotal.appendChild(doc.createTextNode(String.valueOf(costStats.getTotal())));
                // flowTime
                processFlowTimeMin.appendChild(doc.createTextNode(String.valueOf(flowTimeStats.getMin())));
                processFlowTimeMax.appendChild(doc.createTextNode(String.valueOf(flowTimeStats.getMax())));
                processFlowTimeMedian.appendChild(doc.createTextNode(String.valueOf(flowTimeStats.getMedian())));
                processFlowTimeQ1.appendChild(doc.createTextNode(String.valueOf(flowTimeStats.getQ1())));
                processFlowTimeQ3.appendChild(doc.createTextNode(String.valueOf(flowTimeStats.getQ3())));
                processFlowTimeAvg.appendChild(doc.createTextNode(String.valueOf(flowTimeStats.getAverage())));
		        processFlowTimeTotal.appendChild(doc.createTextNode(String.valueOf(flowTimeStats.getTotal())));
                // effective
                processEffectiveMin.appendChild(doc.createTextNode(String.valueOf(effectiveStats.getMin())));
                processEffectiveMax.appendChild(doc.createTextNode(String.valueOf(effectiveStats.getMax())));
                processEffectiveMedian.appendChild(doc.createTextNode(String.valueOf(effectiveStats.getMedian())));
                processEffectiveQ1.appendChild(doc.createTextNode(String.valueOf(effectiveStats.getQ1())));
                processEffectiveQ3.appendChild(doc.createTextNode(String.valueOf(effectiveStats.getQ3())));
                processEffectiveAvg.appendChild(doc.createTextNode(String.valueOf(effectiveStats.getAverage())));
		        processEffectiveTotal.appendChild(doc.createTextNode(String.valueOf(effectiveStats.getTotal())));
                // waiting
                processWaitingMin.appendChild(doc.createTextNode(String.valueOf(waitingStats.getMin())));
                processWaitingMax.appendChild(doc.createTextNode(String.valueOf(waitingStats.getMax())));
                processWaitingMedian.appendChild(doc.createTextNode(String.valueOf(waitingStats.getMedian())));
                processWaitingQ1.appendChild(doc.createTextNode(String.valueOf(waitingStats.getQ1())));
                processWaitingQ3.appendChild(doc.createTextNode(String.valueOf(waitingStats.getQ3())));
                processWaitingAvg.appendChild(doc.createTextNode(String.valueOf(waitingStats.getAverage())));
		        processWaitingTotal.appendChild(doc.createTextNode(String.valueOf(waitingStats.getTotal())));
                // offTime
                processOffTimeMin.appendChild(doc.createTextNode(String.valueOf(offTimetableStats.getMin())));
                processOffTimeMax.appendChild(doc.createTextNode(String.valueOf(offTimetableStats.getMax())));
                processOffTimeMedian.appendChild(doc.createTextNode(String.valueOf(offTimetableStats.getMedian())));
                processOffTimeQ1.appendChild(doc.createTextNode(String.valueOf(offTimetableStats.getQ1())));
                processOffTimeQ3.appendChild(doc.createTextNode(String.valueOf(offTimetableStats.getQ3())));
                processOffTimeAvg.appendChild(doc.createTextNode(String.valueOf(offTimetableStats.getAverage())));
		        processOffTimeTotal.appendChild(doc.createTextNode(String.valueOf(offTimetableStats.getTotal())));
                
	            Map<String, Map<String, StatisticsTaskInstanceObject>> statsPerTaskOfProcess = statsPerTask.get(processId);
	            for (String processScopeNodeId : statsPerTaskOfProcess.keySet()) {
	            	
	            	long taskDuration = 0;
	            	for (StatisticsTaskInstanceObject instance : statsPerTaskOfProcess.get(processScopeNodeId).values()) {
	            		taskDuration += instance.getDurationEffective();
	            	}
	            	// skip tasks with zero duration (which are most likely events)
	            	if (taskDuration == 0) continue;
	            	
	            	StatisticsCalculationObject taskCostStats = new StatisticsCalculationObject();
		            StatisticsCalculationObject taskEffectiveStats = new StatisticsCalculationObject();
		            StatisticsCalculationObject taskWaitingStats = new StatisticsCalculationObject();
		            StatisticsCalculationObject taskResourcesIdleStats = new StatisticsCalculationObject();
		            Map<String, StatisticsTaskInstanceObject> statsPerTaskInstance = statsPerTaskOfProcess.get(processScopeNodeId);
		            String taskName = "";
		            
		            // prepare print
		            
		            Element activities = doc.createElement("activity");
	        		processes.appendChild(activities);
	        		
	        		Element activitiesId = doc.createElement("id");
	        		activitiesId.appendChild(doc.createTextNode(processScopeNodeId));
	        		activities.appendChild(activitiesId);
	        		Element activitiesName = doc.createElement("name");
	        		activities.appendChild(activitiesName);
	        		Element activitiesCostStats = doc.createElement("cost");
	        		activities.appendChild(activitiesCostStats);
	        		Element activitiesTimeStats = doc.createElement("time");
	        		activities.appendChild(activitiesTimeStats);
	        		Element activitiesInstances = doc.createElement("instances");
	        		activities.appendChild(activitiesInstances);
	        		
	        		Element activitiesEffectiveStats = doc.createElement("duration");
	        		activitiesTimeStats.appendChild(activitiesEffectiveStats);
	        		Element activitiesWaitingStats = doc.createElement("waiting");
	        		activitiesTimeStats.appendChild(activitiesWaitingStats);
	        		Element activitiesResourcesIdleStats = doc.createElement("resources_idle");
	        		activitiesTimeStats.appendChild(activitiesResourcesIdleStats);
	        		
	        		
			        Element activitiesCostMin = doc.createElement("min");
		            activitiesCostStats.appendChild(activitiesCostMin);
		            Element activitiesCostMax = doc.createElement("max");
		            activitiesCostStats.appendChild(activitiesCostMax);
		            Element activitiesCostMedian = doc.createElement("median");
		            activitiesCostStats.appendChild(activitiesCostMedian);
		            Element activitiesCostQ1 = doc.createElement("Q1");
		            activitiesCostStats.appendChild(activitiesCostQ1);
		            Element activitiesCostQ3 = doc.createElement("Q3");
		            activitiesCostStats.appendChild(activitiesCostQ3);
		            Element activitiesCostAvg = doc.createElement("avg");
		            activitiesCostStats.appendChild(activitiesCostAvg);
		            Element activitiesCostTotal = doc.createElement("total");
		            activitiesCostStats.appendChild(activitiesCostTotal);
	        		
		            Element activitiesEffectiveMin = doc.createElement("min");
		            activitiesEffectiveStats.appendChild(activitiesEffectiveMin);
		            Element activitiesEffectiveMax = doc.createElement("max");
		            activitiesEffectiveStats.appendChild(activitiesEffectiveMax);
		            Element activitiesEffectiveMedian = doc.createElement("median");
		            activitiesEffectiveStats.appendChild(activitiesEffectiveMedian);
		            Element activitiesEffectiveQ1 = doc.createElement("Q1");
		            activitiesEffectiveStats.appendChild(activitiesEffectiveQ1);
		            Element activitiesEffectiveQ3 = doc.createElement("Q3");
		            activitiesEffectiveStats.appendChild(activitiesEffectiveQ3);
		            Element activitiesEffectiveAvg = doc.createElement("avg");
		            activitiesEffectiveStats.appendChild(activitiesEffectiveAvg);
		            Element activitiesEffectiveTotal = doc.createElement("total");
		            activitiesEffectiveStats.appendChild(activitiesEffectiveTotal);
	        		
		            Element activitiesWaitingMin = doc.createElement("min");
		            activitiesWaitingStats.appendChild(activitiesWaitingMin);
		            Element activitiesWaitingMax = doc.createElement("max");
		            activitiesWaitingStats.appendChild(activitiesWaitingMax);
		            Element activitiesWaitingMedian = doc.createElement("median");
		            activitiesWaitingStats.appendChild(activitiesWaitingMedian);
		            Element activitiesWaitingQ1 = doc.createElement("Q1");
		            activitiesWaitingStats.appendChild(activitiesWaitingQ1);
		            Element activitiesWaitingQ3 = doc.createElement("Q3");
		            activitiesWaitingStats.appendChild(activitiesWaitingQ3);
		            Element activitiesWaitingAvg = doc.createElement("avg");
		            activitiesWaitingStats.appendChild(activitiesWaitingAvg);
		            Element activitiesWaitingTotal = doc.createElement("total");
		            activitiesWaitingStats.appendChild(activitiesWaitingTotal);
	        		
		            Element activitiesResourcesIdleMin = doc.createElement("min");
		            activitiesResourcesIdleStats.appendChild(activitiesResourcesIdleMin);
		            Element activitiesResourcesIdleMax = doc.createElement("max");
		            activitiesResourcesIdleStats.appendChild(activitiesResourcesIdleMax);
		            Element activitiesResourcesIdleMedian = doc.createElement("median");
		            activitiesResourcesIdleStats.appendChild(activitiesResourcesIdleMedian);
		            Element activitiesResourcesIdleQ1 = doc.createElement("Q1");
		            activitiesResourcesIdleStats.appendChild(activitiesResourcesIdleQ1);
		            Element activitiesResourcesIdleQ3 = doc.createElement("Q3");
		            activitiesResourcesIdleStats.appendChild(activitiesResourcesIdleQ3);
		            Element activitiesResourcesIdleAvg = doc.createElement("avg");
		            activitiesResourcesIdleStats.appendChild(activitiesResourcesIdleAvg);
		            Element activitiesResourcesIdleTotal = doc.createElement("total");
		            activitiesResourcesIdleStats.appendChild(activitiesResourcesIdleTotal);
	        		
	            	for (String taskInstanceId : statsPerTaskInstance.keySet()) {
	            		StatisticsTaskInstanceObject stats = statsPerTaskInstance.get(taskInstanceId);
		                
	            		if (taskName.isEmpty()) {
	            			taskName = stats.getTaskName();
	            		}
	            		long durationEffective = stats.getDurationEffective();
	            	    long durationResourcesIdle = stats.getDurationResourcesIdle();
	            	    long durationWaiting = stats.getDurationWaiting();
	            	    double cost = stats.getCost();
	            	    
	            	    Element instanceCosts = doc.createElement("cost");
		                instanceCosts.appendChild(doc.createTextNode(String.valueOf(cost)));
		                activitiesInstances.appendChild(instanceCosts);
		                Element instanceTime = doc.createElement("time");
		                activitiesInstances.appendChild(instanceTime);
		                Element instanceEffective = doc.createElement("effective");
		                instanceEffective.appendChild(doc.createTextNode(String.valueOf(durationEffective)));
		                instanceTime.appendChild(instanceEffective);
		                Element instanceWaiting = doc.createElement("waiting");
		                instanceWaiting.appendChild(doc.createTextNode(String.valueOf(durationWaiting)));
		                instanceTime.appendChild(instanceWaiting);
		                Element instanceResourcesIdle= doc.createElement("resources_idle");
		                instanceResourcesIdle.appendChild(doc.createTextNode(String.valueOf(durationResourcesIdle)));
		                instanceTime.appendChild(instanceResourcesIdle);
	            	    
	            	    taskCostStats.addValue(cost);
	            	    taskEffectiveStats.addValue(durationEffective);
	            	    taskResourcesIdleStats.addValue(durationResourcesIdle);
	            	    taskWaitingStats.addValue(durationWaiting);
	            	}

	        		activitiesName.appendChild(doc.createTextNode(taskName));
	            	
	            	taskCostStats.calculateStatistics();
	            	taskEffectiveStats.calculateStatistics();
	            	taskWaitingStats.calculateStatistics();
		            taskResourcesIdleStats.calculateStatistics();
		            
		            activitiesCostMin.appendChild(doc.createTextNode(String.valueOf(taskCostStats.getMin())));
		            activitiesCostMax.appendChild(doc.createTextNode(String.valueOf(taskCostStats.getMax())));
		            activitiesCostMedian.appendChild(doc.createTextNode(String.valueOf(taskCostStats.getMedian())));
		            activitiesCostQ1.appendChild(doc.createTextNode(String.valueOf(taskCostStats.getQ1())));
		            activitiesCostQ3.appendChild(doc.createTextNode(String.valueOf(taskCostStats.getQ3())));
		            activitiesCostAvg.appendChild(doc.createTextNode(String.valueOf(taskCostStats.getAverage())));
		            activitiesCostTotal.appendChild(doc.createTextNode(String.valueOf(taskCostStats.getTotal())));
	                
		            activitiesEffectiveMin.appendChild(doc.createTextNode(String.valueOf(taskEffectiveStats.getMin())));
		            activitiesEffectiveMax.appendChild(doc.createTextNode(String.valueOf(taskEffectiveStats.getMax())));
		            activitiesEffectiveMedian.appendChild(doc.createTextNode(String.valueOf(taskEffectiveStats.getMedian())));
		            activitiesEffectiveQ1.appendChild(doc.createTextNode(String.valueOf(taskEffectiveStats.getQ1())));
		            activitiesEffectiveQ3.appendChild(doc.createTextNode(String.valueOf(taskEffectiveStats.getQ3())));
		            activitiesEffectiveAvg.appendChild(doc.createTextNode(String.valueOf(taskEffectiveStats.getAverage())));
		            activitiesEffectiveTotal.appendChild(doc.createTextNode(String.valueOf(taskEffectiveStats.getTotal())));
	                
		            activitiesWaitingMin.appendChild(doc.createTextNode(String.valueOf(taskWaitingStats.getMin())));
		            activitiesWaitingMax.appendChild(doc.createTextNode(String.valueOf(taskWaitingStats.getMax())));
		            activitiesWaitingMedian.appendChild(doc.createTextNode(String.valueOf(taskWaitingStats.getMedian())));
		            activitiesWaitingQ1.appendChild(doc.createTextNode(String.valueOf(taskWaitingStats.getQ1())));
		            activitiesWaitingQ3.appendChild(doc.createTextNode(String.valueOf(taskWaitingStats.getQ3())));
		            activitiesWaitingAvg.appendChild(doc.createTextNode(String.valueOf(taskWaitingStats.getAverage())));
		            activitiesWaitingTotal.appendChild(doc.createTextNode(String.valueOf(taskWaitingStats.getTotal())));
		            
		            activitiesResourcesIdleMin.appendChild(doc.createTextNode(String.valueOf(taskResourcesIdleStats.getMin())));
		            activitiesResourcesIdleMax.appendChild(doc.createTextNode(String.valueOf(taskResourcesIdleStats.getMax())));
		            activitiesResourcesIdleMedian.appendChild(doc.createTextNode(String.valueOf(taskResourcesIdleStats.getMedian())));
		            activitiesResourcesIdleQ1.appendChild(doc.createTextNode(String.valueOf(taskResourcesIdleStats.getQ1())));
		            activitiesResourcesIdleQ3.appendChild(doc.createTextNode(String.valueOf(taskResourcesIdleStats.getQ3())));
		            activitiesResourcesIdleAvg.appendChild(doc.createTextNode(String.valueOf(taskResourcesIdleStats.getAverage())));
		            activitiesResourcesIdleTotal.appendChild(doc.createTextNode(String.valueOf(taskResourcesIdleStats.getTotal())));
	            }
	        }

	        for (String resourceType : statsPerResource.keySet()) {
	            Map<String, StatisticsResourceObject> statsPerResourceInstance = statsPerResource.get(resourceType);
	            double percentageSum = 0;
	            
	            Element resource = doc.createElement("resource");
        		rootElement.appendChild(resource);
        		
        		Element resourceTypeElement = doc.createElement("type");
        		resourceTypeElement.appendChild(doc.createTextNode(resourceType));
        		resource.appendChild(resourceTypeElement);
        		
        		// set up
        		
        		Element resourceCostStats = doc.createElement("cost");
        		resource.appendChild(resourceCostStats);
        		Element resourceTimeStats = doc.createElement("time");
        		resource.appendChild(resourceTimeStats);
        		
        		Element resourceInUse = doc.createElement("in_use");
                resourceTimeStats.appendChild(resourceInUse);
                Element resourceAvailable = doc.createElement("available");
                resourceTimeStats.appendChild(resourceAvailable);
                
                Element resourceCostMin = doc.createElement("min");
                resourceCostStats.appendChild(resourceCostMin);
        		Element resourceCostMax = doc.createElement("max");
                resourceCostStats.appendChild(resourceCostMax);
        		Element resourceCostMedian = doc.createElement("median");
                resourceCostStats.appendChild(resourceCostMedian);
        		Element resourceCostQ1 = doc.createElement("Q1");
                resourceCostStats.appendChild(resourceCostQ1);
        		Element resourceCostQ3 = doc.createElement("Q3");
                resourceCostStats.appendChild(resourceCostQ3);
        		Element resourceCostAvg = doc.createElement("avg");
                resourceCostStats.appendChild(resourceCostAvg);
                Element resourceCostTotal = doc.createElement("total");
                resourceCostStats.appendChild(resourceCostTotal);
                
                Element resourceInUseMin = doc.createElement("min");
                resourceInUse.appendChild(resourceInUseMin);
                Element resourceInUseMax = doc.createElement("max");
                resourceInUse.appendChild(resourceInUseMax);
        		Element resourceInUseMedian = doc.createElement("median");
                resourceInUse.appendChild(resourceInUseMedian);
        		Element resourceInUseQ1 = doc.createElement("Q1");
                resourceInUse.appendChild(resourceInUseQ1);
        		Element resourceInUseQ3 = doc.createElement("Q3");
                resourceInUse.appendChild(resourceInUseQ3);
        		Element resourceInUseAvg = doc.createElement("avg");
                resourceInUse.appendChild(resourceInUseAvg);
                Element resourceInUseTotal = doc.createElement("total");
                resourceInUse.appendChild(resourceInUseTotal);
                
                Element resourceAvailableMin = doc.createElement("min");
                resourceAvailable.appendChild(resourceAvailableMin);
                Element resourceAvailableMax = doc.createElement("max");
                resourceAvailable.appendChild(resourceAvailableMax);
                Element resourceAvailableMedian = doc.createElement("median");
                resourceAvailable.appendChild(resourceAvailableMedian);
                Element resourceAvailableQ1 = doc.createElement("Q1");
                resourceAvailable.appendChild(resourceAvailableQ1);
                Element resourceAvailableQ3 = doc.createElement("Q3");
                resourceAvailable.appendChild(resourceAvailableQ3);
                Element resourceAvailableAvg = doc.createElement("avg");
                resourceAvailable.appendChild(resourceAvailableAvg);
                Element resourceAvailableTotal = doc.createElement("total");
                resourceAvailable.appendChild(resourceAvailableTotal);
                
                StatisticsCalculationObject costStats = new StatisticsCalculationObject();
                StatisticsCalculationObject inUseStats = new StatisticsCalculationObject();
                StatisticsCalculationObject availableStats = new StatisticsCalculationObject();
	            for (String resourceId : statsPerResourceInstance.keySet()) {
	                StatisticsResourceObject stats = statsPerResourceInstance.get(resourceId);
	                long durationInUse = stats.getDurationInUse();
	                long durationAvailable = stats.getDurationAvailable();
	                double percentageInUse = durationInUse / (double) durationAvailable;
	                double cost = stats.getCosts();
	                
	                Element resourceInstance = doc.createElement("resourceInstance");
	        		resource.appendChild(resourceInstance);
	        		
	        		Element resourceInstanceId = doc.createElement("id");
	        		resourceInstanceId.appendChild(doc.createTextNode(resourceId));
	        		resourceInstance.appendChild(resourceInstanceId);

	        		Element resourceInstanceCost = doc.createElement("cost");
	        		resourceInstanceCost.appendChild(doc.createTextNode(String.valueOf(cost)));
	        		resourceInstance.appendChild(resourceInstanceCost);
	        		Element resourceInstanceTime = doc.createElement("time");
	        		resourceInstance.appendChild(resourceInstanceTime);
	        		
	        		Element resourceInstanceInUse = doc.createElement("in_use");
	        		resourceInstanceInUse.appendChild(doc.createTextNode(String.valueOf(durationInUse)));
	        		resourceInstanceTime.appendChild(resourceInstanceInUse);
	        		Element resourceInstanceAvailable = doc.createElement("available");
	        		resourceInstanceAvailable.appendChild(doc.createTextNode(String.valueOf(durationAvailable)));
	        		resourceInstanceTime.appendChild(resourceInstanceAvailable);
	        		
	                percentageSum += percentageInUse;
	                
	                costStats.addValue(cost);
	                inUseStats.addValue(durationInUse);
	                availableStats.addValue(durationAvailable);
	            }
	            costStats.calculateStatistics();
	            inUseStats.calculateStatistics();
	            availableStats.calculateStatistics();
	            
	            resourceCostMin.appendChild(doc.createTextNode(String.valueOf(costStats.getMin())));
	            resourceCostMax.appendChild(doc.createTextNode(String.valueOf(costStats.getMax())));
	            resourceCostMedian.appendChild(doc.createTextNode(String.valueOf(costStats.getMedian())));
	            resourceCostQ1.appendChild(doc.createTextNode(String.valueOf(costStats.getQ1())));
	            resourceCostQ3.appendChild(doc.createTextNode(String.valueOf(costStats.getQ3())));
	            resourceCostAvg.appendChild(doc.createTextNode(String.valueOf(costStats.getAverage())));
	            resourceCostTotal.appendChild(doc.createTextNode(String.valueOf(costStats.getTotal())));
        		
	            resourceInUseMin.appendChild(doc.createTextNode(String.valueOf(inUseStats.getMin())));
	            resourceInUseMax.appendChild(doc.createTextNode(String.valueOf(inUseStats.getMax())));
	            resourceInUseMedian.appendChild(doc.createTextNode(String.valueOf(inUseStats.getMedian())));
	            resourceInUseQ1.appendChild(doc.createTextNode(String.valueOf(inUseStats.getQ1())));
	            resourceInUseQ3.appendChild(doc.createTextNode(String.valueOf(inUseStats.getQ3())));
	            resourceInUseAvg.appendChild(doc.createTextNode(String.valueOf(inUseStats.getAverage())));
	            resourceInUseTotal.appendChild(doc.createTextNode(String.valueOf(inUseStats.getTotal())));
        		
	            resourceAvailableMin.appendChild(doc.createTextNode(String.valueOf(availableStats.getMin())));
	            resourceAvailableMax.appendChild(doc.createTextNode(String.valueOf(availableStats.getMax())));
	            resourceAvailableMedian.appendChild(doc.createTextNode(String.valueOf(availableStats.getMedian())));
	            resourceAvailableQ1.appendChild(doc.createTextNode(String.valueOf(availableStats.getQ1())));
	            resourceAvailableQ3.appendChild(doc.createTextNode(String.valueOf(availableStats.getQ3())));
	            resourceAvailableAvg.appendChild(doc.createTextNode(String.valueOf(availableStats.getAverage())));
	            resourceAvailableTotal.appendChild(doc.createTextNode(String.valueOf(availableStats.getTotal())));
	        }

	        String resourceUtilizationFileName = outputPathWithoutExtension + model.getGlobalConfiguration().getFileNameWithoutExtension()+"_resourceutilization.xml";
	            
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    	Transformer transformer = transformerFactory.newTransformer();
	    	DOMSource source = new DOMSource(doc);
	    	StreamResult result = new StreamResult(resourceUtilizationFileName);
	    	//StreamResult result = new StreamResult(System.out);
	    	transformer.transform(source, result);
	
	        System.out.println("Wrote resource utilization statistics to " + resourceUtilizationFileName);
        	} catch (ParserConfigurationException pce) {
        		pce.printStackTrace();
            } catch (TransformerException tfe) {
        		tfe.printStackTrace();
      	  	}
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
