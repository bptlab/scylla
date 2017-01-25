package de.hpi.bpt.scylla.plugin.dmn;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.simulation.QueueManager;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;

public class DmnLogger extends OutputLoggerPluggable {

    @Override
    public String getName() {
        return "Dmn_KPI";
    }

    public void writeToLog(SimulationModel model, String outputPathWithoutExtension) throws IOException {

        TimeUnit timeUnit = DateTimeUtils.getReferenceTimeUnit();
        double totalEndTime = model.presentTime().getTimeAsDouble(timeUnit);
        Map<String, Map<Integer, List<ProcessNodeInfo>>> processNodeInfos = model.getProcessNodeInfos();
        Map<String, Map<Integer, List<DmnNodeInfo>>> dmnNodeInfos = DmnPluginUtils.getInstance().getDmnNodeInfos();
        

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


        Map<String, List<DmnStatsInfo>> decisionsPerProcess = new HashMap<String, List<DmnStatsInfo>>();

        for (String processId : processNodeInfos.keySet()) {

            Map<Integer, List<ProcessNodeInfo>> nodeInfoOfProcessInstances = processNodeInfos.get(processId);
            Map<Integer, List<DmnNodeInfo>> dmnInfoOfProcessInstances = dmnNodeInfos.get(processId);
            
            if( dmnInfoOfProcessInstances == null ) continue;
            
            Map<String, DmnStatsInfo> dmnStatsInfos = new HashMap<String, DmnStatsInfo>();
            
            for (int processInstanceId : dmnInfoOfProcessInstances.keySet()) {

                List<ProcessNodeInfo> nodeInfoList = nodeInfoOfProcessInstances.get(processInstanceId);
                List<DmnNodeInfo> dmnInfoList = dmnInfoOfProcessInstances.get(processInstanceId);

                long durationTotal = 0;
                double costs = 0;
                
                long timeProcessStart = 0;

                Map<TaskInstanceIdentifier, Long> taskDurations = new HashMap<TaskInstanceIdentifier, Long>();
                Map<TaskInstanceIdentifier, Long> begunOrResumedTasks = new HashMap<TaskInstanceIdentifier, Long>();

                Map<TaskInstanceIdentifier, Long> enabledTasks = new HashMap<TaskInstanceIdentifier, Long>();
                Map<TaskInstanceIdentifier, Long> pausedTasks = new HashMap<TaskInstanceIdentifier, Long>();
                boolean hasDecisionFailure = false;
                for (int i = 0; i < nodeInfoList.size(); i++) {
                	
                    ProcessNodeInfo ni = nodeInfoList.get(i);
                    long timestamp = ni.getTimestamp();
                    String processScopeNodeId = ni.getProcessScopeNodeId();
                    String source = ni.getSource();
                    TaskInstanceIdentifier taskInstanceIdentifier = new TaskInstanceIdentifier(processScopeNodeId,
                            source);
                    Set<String> resources = ni.getResources();

                    if (i == 0) {
                        timeProcessStart = timestamp;
                    }
                    else if (i == nodeInfoList.size() - 1) {
                        durationTotal = timestamp - timeProcessStart;
                    }

                    ProcessNodeTransitionType transition = ni.getTransition();

                    if (transition == ProcessNodeTransitionType.ENABLE) {
                        enabledTasks.put(taskInstanceIdentifier, timestamp);
                    }
                    else if (transition == ProcessNodeTransitionType.BEGIN) {
                        Long enableTimestamp = enabledTasks.get(taskInstanceIdentifier);
                        if (enableTimestamp != null) {
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
                        }
                        taskDurations.put(taskInstanceIdentifier, taskDurations.get(taskInstanceIdentifier) + duration);
                        begunOrResumedTasks.remove(taskInstanceIdentifier);
                    }
                    else if (transition == ProcessNodeTransitionType.RESUME) {
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
                        }
                        taskDurations.remove(taskInstanceIdentifier);
                        begunOrResumedTasks.remove(taskInstanceIdentifier);
                    }
                    else if (transition == ProcessNodeTransitionType.EVENT_BEGIN
                            || transition == ProcessNodeTransitionType.EVENT_TERMINATE) {
                        // not supported
                    } else {
                    	hasDecisionFailure = true;
                    }
                }
                
                String outputKey = "";
                List<Decision> decisions = new LinkedList<Decision>();
                for(DmnNodeInfo dmnNodeInfo : dmnInfoList) {
                	
            		String input = dmnNodeInfo.getInput().toString();
            		String output = dmnNodeInfo.getOutput().toString();
            		String taskName = dmnNodeInfo.getName();
            		
            		decisions.add(new Decision(taskName, input, output));
            		outputKey += output;
                }
                
                if(!dmnStatsInfos.containsKey(outputKey)){
                	dmnStatsInfos.put(outputKey, new DmnStatsInfo());
                }

                DmnStatsInfo dmnStatsInfo = dmnStatsInfos.get(outputKey);
                dmnStatsInfo.setOutputs(outputKey);
                dmnStatsInfo.addDecisions(decisions);
                dmnStatsInfo.updateCosts( costs );
                dmnStatsInfo.updateDuration( durationTotal );
                if(hasDecisionFailure) {
                	dmnStatsInfo.setDecisionFailure();
                }
            }
            
            
            List<DmnStatsInfo> orderedDecisions = new LinkedList<DmnStatsInfo>(); //TreeMultiset.create((lhs, rhs) -> rhs.getFrequency() - lhs.getFrequency());
            for(String outputKey : dmnStatsInfos.keySet()){
            	DmnStatsInfo dmnStatsInfo = dmnStatsInfos.get(outputKey);
            	orderedDecisions.add(dmnStatsInfo);
            }
            Collections.sort(orderedDecisions, (lhs, rhs) -> rhs.getFrequency() - lhs.getFrequency());
            decisionsPerProcess.put(processId, orderedDecisions);
        }

        
        
        // print

        StringBuffer sb = new StringBuffer();
        sb.append("Total simulation time: " + totalEndTime + System.lineSeparator());
        sb.append("Reference unit: " + DateTimeUtils.getReferenceTimeUnit() + System.lineSeparator());
        sb.append(System.lineSeparator());

        DecimalFormat percentFormat = new DecimalFormat("#.#%");
        
        
        sb.append(System.lineSeparator());
        sb.append("=== DECISIONS ===" + System.lineSeparator());
        for (String processId : decisionsPerProcess.keySet()) {
            sb.append(System.lineSeparator());
            sb.append("== Process identifier: " + processId + System.lineSeparator());
            List<DmnStatsInfo> orderedDecisions = decisionsPerProcess.get(processId);
           
            int totalDecisionCount = orderedDecisions.stream().mapToInt(f -> f.getFrequency()).sum();
            double maxCost = orderedDecisions.stream()
            		.max((rhs, lhs) -> (int)(rhs.getAverageCosts() - lhs.getAverageCosts())).get().getAverageCosts();
            double maxTime = orderedDecisions.stream()
            		.max((rhs, lhs) -> (int)(rhs.getAverageDuration() - lhs.getAverageDuration())).get().getAverageDuration();
           
            double costDistance = (41 / maxCost);
            double timeDistance = (22 / maxTime);
            
            // array of x = 23 (time) and y = 42 (cost)
            String [][] array = new String[23][42];

            int counter = 0;
            for (DmnStatsInfo dmnStatsInfo : orderedDecisions) {
                
                double percantage = (double)dmnStatsInfo.getFrequency() / (double)totalDecisionCount;
                
                double averageDuration = dmnStatsInfo.getAverageDuration();
                double averageCost = dmnStatsInfo.getAverageCosts();
                String outputs = dmnStatsInfo.getOutputs();
                String inputs = dmnStatsInfo.getDecisions().stream()
                		.map(f -> "\t\t" + f.getName() + ": " + f.getInput())
                		.collect(Collectors.joining(System.lineSeparator()));
                
                sb.append(System.lineSeparator());
                
                // headline: how often did this flow appear
                sb.append("(" + ++counter + ") " + percentFormat.format(percantage) + System.lineSeparator());
                if(dmnStatsInfo.hasDecisionFailure()){
                	sb.append("!!! Decision led to a non-existing flow. !!!" + System.lineSeparator());
                	sb.append("!!! Please check the simulation log for errors. !!!" + System.lineSeparator());
                }
                sb.append("\tOutputs: " + outputs + System.lineSeparator());
                sb.append("\tAverage cost: " + averageCost + System.lineSeparator());
                sb.append("\tAverage time: " + averageDuration + System.lineSeparator());
                sb.append("\tInputs:\n" + inputs + System.lineSeparator());
                
                int y = (int) Math.floor(averageCost * costDistance);
                int x = (int) Math.floor(averageDuration * timeDistance);
                
                // if there is already an element at this position,
                // move the element up
                while(y < 42 && x < 23 && array[x][y] != null){
                	y++;
                	x++;
                }
                
                array[x][y] = "(" + (counter > 9 ? counter : " " + counter ) + ")";
            }
            
            // arrow on top
            sb.append(System.lineSeparator());
            sb.append("       cost" + System.lineSeparator());
            sb.append("        \u2191");
            
            for(int j = 41; j >= 0; j-- ){
            	sb.append(System.lineSeparator());
            	if(j % 5 == 0){
            		double label = Math.round( j * maxCost / 41.0 * 100.0) / 100.0;
            		String costLabel = "";
            		if(label >= 100){
            			costLabel = Double.toString(label);
            		} else if (label >= 10){
            			costLabel = " " + Double.toString(label);
            		} else {
            			costLabel = "  " + Double.toString(label);
            		}
            		sb.append(" " + costLabel + (costLabel.indexOf('.') != costLabel.length() - 3 ? "0 +" : " +"));
            	} else {
            		// create y axis
            		sb.append("        |");
            	}
            	
            	for(int i = 0; i < 23; i++){
            		int position = (int) String.valueOf(i).charAt(String.valueOf(i).length()-1) - '0';
            		
            		if(array[i][j] != null){
            			// data element available
            			sb.append(array[i][j]);
            		} else if(j % 5 == 0 && (position == 2 || position == 7)){
            			sb.append((j == 0) ? "-+--" :" +  ");
            		} else if(j % 5 == 0 && (position == 4 || position == 9)){
            			sb.append((j == 0) ? "---+" :"   +"); 
            		} else {
            			sb.append((j == 0) ? "----" :"    ");
            		}
            	}
            }
            
            // write end of x axis
            sb.append("--> time" + System.lineSeparator() + "     ");

            // write x labels
            for(int i = 0; i < 46; i++){
            	if(i % 5 == 0){
            		double label = Math.round(i * maxTime / 46 * 100.0) / 100.0;
            		String timeLabel = "";
            		if(label >= 100){
            			timeLabel = Double.toString(label);
            		} else if (label >= 10){
            			timeLabel = " " + Double.toString(label);
            		} else {
            			timeLabel = "  " + Double.toString(label);
            		}
            		sb.append(timeLabel + (timeLabel.indexOf('.') != timeLabel.length() - 3 ? "0" : ""));
            	} else {
            		sb.append(" ");
            	}
            }
        }
        
        
        
        if (outputPathWithoutExtension == null) {
            System.out.println(sb.toString());
        }
        else {
            String resourceUtilizationFileName = outputPathWithoutExtension + "_dmn.txt";

            PrintWriter writer = new PrintWriter(resourceUtilizationFileName, "UTF-8");
            writer.println(sb.toString());
            writer.close();

            System.out.println("Wrote resource utilization statistics to " + resourceUtilizationFileName);
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
