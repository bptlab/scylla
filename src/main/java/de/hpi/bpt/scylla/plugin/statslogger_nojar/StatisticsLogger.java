package de.hpi.bpt.scylla.plugin.statslogger_nojar;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
        Map<String, Map<String, StatisticsTaskObject>> statsPerTask = new HashMap<String, Map<String, StatisticsTaskObject>>();

        for (String processId : processNodeInfos.keySet()) {

            Map<Integer, StatisticsProcessInstanceObject> statsPerProcessInstance = new HashMap<Integer, StatisticsProcessInstanceObject>();
            Map<String, StatisticsTaskObject> statsPerTaskOfProcess = new HashMap<String, StatisticsTaskObject>();

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

                    ProcessNodeInfo ni = nodeInfoList.get(i);
                    long timestamp = ni.getTimestamp();
                    String processScopeNodeId = ni.getProcessScopeNodeId();
                    String source = ni.getSource();
                    TaskInstanceIdentifier taskInstanceIdentifier = new TaskInstanceIdentifier(processScopeNodeId,
                            source);
                    Set<String> resources = ni.getResources();

                    if (!statsPerTaskOfProcess.containsKey(processScopeNodeId)) {
                        String taskName = ni.getTaskName();
                        statsPerTaskOfProcess.put(processScopeNodeId, new StatisticsTaskObject(taskName));
                    }
                    StatisticsTaskObject sto = statsPerTaskOfProcess.get(processScopeNodeId);

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
                            enabledTasks.remove(taskInstanceIdentifier);

                            sto.updateAvgWaitingTime(duration);
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
                        Long pauseTimestamp = pausedTasks.get(taskInstanceIdentifier);
                        long duration = timestamp - pauseTimestamp;
                        durationResourcesIdle += duration;
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

                        // taskDurationEffetive without off timetable hours where resources are idle
                        long taskDurationEffective = taskDurations.get(taskInstanceIdentifier) + duration;
                        sto.updateAvgDuration(taskDurationEffective);
                        taskDurations.remove(taskInstanceIdentifier);
                        begunOrResumedTasks.remove(taskInstanceIdentifier);
                    }
                    else if (transition == ProcessNodeTransitionType.EVENT_BEGIN
                            || transition == ProcessNodeTransitionType.EVENT_TERMINATE) {
                        // not supported
                    }

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

        StringBuffer sb = new StringBuffer();
        sb.append("Total simulation time: " + totalEndTime + System.lineSeparator());
        sb.append("Reference unit: " + DateTimeUtils.getReferenceTimeUnit() + System.lineSeparator());
        sb.append(System.lineSeparator());

        DecimalFormat percentFormat = new DecimalFormat("#.#%");
        // print resource utilization
        sb.append("=== RESOURCE UTILIZATION ===" + System.lineSeparator());
        for (String resourceType : statsPerResource.keySet()) {
            sb.append(System.lineSeparator());
            sb.append("== Resource type: " + resourceType + System.lineSeparator());
            Map<String, StatisticsResourceObject> statsPerResourceInstance = statsPerResource.get(resourceType);
            double percentageSum = 0;
            for (String resourceId : statsPerResourceInstance.keySet()) {
                StatisticsResourceObject stats = statsPerResourceInstance.get(resourceId);
                long durationInUse = stats.getDurationInUse();
                long durationAvailable = stats.getDurationAvailable();
                double percentageInUse = durationInUse / (double) durationAvailable;
                double costs = stats.getCosts();

                sb.append(System.lineSeparator());
                sb.append("-- " + resourceId + System.lineSeparator());
                sb.append("Time in use / available: " + durationInUse + " / " + durationAvailable + " ("
                        + percentFormat.format(percentageInUse) + ")" + System.lineSeparator());
                sb.append("Costs: " + costs + System.lineSeparator());
                percentageSum += percentageInUse;
            }
            double totalPercentage = percentageSum / statsPerResourceInstance.size();
            sb.append(System.lineSeparator());
            sb.append("## Overall: " + percentFormat.format(totalPercentage) + System.lineSeparator());
        }

        sb.append(System.lineSeparator());
        sb.append("=== STATISTICS PER PROCESS ===" + System.lineSeparator());
        for (String processId : statsPerProcess.keySet()) {
            sb.append(System.lineSeparator());
            sb.append("== Process identifier: " + processId + System.lineSeparator());
            Map<Integer, StatisticsProcessInstanceObject> statsPerProcessInstance = statsPerProcess.get(processId);

            int instancesProcessed = 0;
            double avgDurationTotal = 0;
            double avgDurationEffective = 0;
            double avgPercentageEffective = 0;
            double avgDurationResourcesIdle = 0;
            double avgDurationWaiting = 0;
            double avgCost = 0;
            for (Integer processInstanceId : statsPerProcessInstance.keySet()) {
                StatisticsProcessInstanceObject stats = statsPerProcessInstance.get(processInstanceId);
                ++instancesProcessed;

                long durationTotal = stats.getDurationTotal();
                long durationEffective = durationTotal - stats.getDurationInactive();
                double percentageEffective = durationEffective / (double) durationTotal;

                long durationResourcesIdle = stats.getDurationResourcesIdle();
                long durationWaiting = stats.getDurationWaiting();

                double cost = stats.getCosts();

                sb.append(System.lineSeparator());
                sb.append("-- " + processInstanceId + System.lineSeparator());
                sb.append("Time effective / total: " + durationEffective + " / " + durationTotal + " ("
                        + percentFormat.format(percentageEffective) + ")" + System.lineSeparator());
                sb.append("Off-timetable time (cumulated): " + durationResourcesIdle + System.lineSeparator());
                sb.append("Waiting time (cumulated): " + durationWaiting + System.lineSeparator());
                sb.append("Cost: " + cost + System.lineSeparator());

                avgDurationTotal += (durationTotal - avgDurationTotal) / instancesProcessed;
                avgDurationEffective += (durationEffective - avgDurationEffective) / instancesProcessed;
                avgPercentageEffective += (percentageEffective - avgPercentageEffective) / instancesProcessed;
                avgDurationResourcesIdle += (durationResourcesIdle - avgDurationResourcesIdle) / instancesProcessed;
                avgDurationWaiting += (durationWaiting - avgDurationWaiting) / instancesProcessed;
                avgCost += (cost - avgCost) / instancesProcessed;
            }
            sb.append(System.lineSeparator());
            sb.append("## Overall average: " + System.lineSeparator());
            sb.append("Time effective / total: " + avgDurationEffective + " / " + avgDurationTotal + " ("
                    + percentFormat.format(avgPercentageEffective) + ")" + System.lineSeparator());
            sb.append("Off-timetable time (cumulated): " + avgDurationResourcesIdle + System.lineSeparator());
            sb.append("Waiting time (cumulated): " + avgDurationWaiting + System.lineSeparator());
            sb.append("Cost: " + avgCost + System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append("Total cost: " + avgCost * instancesProcessed + System.lineSeparator());

        }

        sb.append(System.lineSeparator());
        sb.append("=== STATISTICS PER TASK ===" + System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Note: Tasks with zero duration and zero waiting time are not logged." + System.lineSeparator());
        for (String processId : statsPerTask.keySet()) {
            sb.append(System.lineSeparator());
            sb.append("== Process identifier: " + processId + System.lineSeparator());
            Map<String, StatisticsTaskObject> statsPerTaskOfProcess = statsPerTask.get(processId);
            for (String processScopeNodeId : statsPerTaskOfProcess.keySet()) {
                StatisticsTaskObject stats = statsPerTaskOfProcess.get(processScopeNodeId);
                String taskName = stats.getTaskName();
                double avgDuration = stats.getAvgDuration();
                double avgWaitingTime = stats.getAvgWaitingTime();

                if (avgDuration == 0d && avgWaitingTime == 0d) {
                    // skip tasks with zero duration (which are most likely events)
                    continue;
                }

                sb.append(System.lineSeparator());
                sb.append("-- " + taskName + " (" + processScopeNodeId + ")" + System.lineSeparator());
                sb.append("Average duration: " + avgDuration + System.lineSeparator());
                sb.append("Average waiting time: " + avgWaitingTime + System.lineSeparator());
            }
        }

        if (outputPathWithoutExtension == null) {
            System.out.println(sb.toString());
        }
        else {
            String resourceUtilizationFileName = outputPathWithoutExtension + model.getGlobalConfiguration().getFileNameWithoutExtension()+"_resourceutilization.txt";

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
