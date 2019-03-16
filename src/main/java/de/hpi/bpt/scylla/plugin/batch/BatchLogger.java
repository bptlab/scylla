package de.hpi.bpt.scylla.plugin.batch;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import desmoj.core.simulator.TimeInstant;

public class BatchLogger extends OutputLoggerPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    public void writeToLog(SimulationModel model, String outputPathWithoutExtension) throws IOException {

        StringBuffer sb = new StringBuffer();

        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
            Map<String, Map<Integer, List<BatchCluster>>> clusters = pluginInstance.getBatchClusters();

        TimeUnit referenceTimeUnit = DateTimeUtils.getReferenceTimeUnit();

        sb.append("BATCH REPORT" + System.lineSeparator());
        sb.append("Reference unit: " + referenceTimeUnit + System.lineSeparator());
        sb.append(
                "Please note: Duration and cost of batch activity execution can be found in output file of statslogger plug-in."
                        + System.lineSeparator());
        sb.append(System.lineSeparator());

        for (String processId : clusters.keySet()) {
            sb.append("=== PROCESS " + processId + " ===" + System.lineSeparator());
            Map<Integer, List<BatchCluster>> clustersOfProcess = clusters.get(processId);
            for (Integer nodeId : clustersOfProcess.keySet()) {
                sb.append("== Node " + nodeId + System.lineSeparator());

                double waitingTimeMaximumAllActivities = -1;
                List<Double> waitingTimeAveragesAllActivities = new ArrayList<Double>();

                List<BatchCluster> bcs = clustersOfProcess.get(nodeId);
                for (BatchCluster bc : bcs) {
                    sb.append(System.lineSeparator());
                    sb.append("-- " + bc.getName() + System.lineSeparator());

                    double startTimeInRefUnit = bc.getStartTime().getTimeAsDouble(referenceTimeUnit);
                    double maximumWaitingTime = startTimeInRefUnit
                            - bc.getCreationTime().getTimeAsDouble(referenceTimeUnit);
                    if (maximumWaitingTime > waitingTimeMaximumAllActivities) {
                        waitingTimeMaximumAllActivities = maximumWaitingTime;
                    }
                    sb.append("Maximum waiting time: " + maximumWaitingTime + System.lineSeparator());

                    List<TimeInstant> entranceTimes = bc.getProcessInstanceEntranceTimes();
                    List<Double> waitingTimes = new ArrayList<Double>();
                    for (TimeInstant et : entranceTimes) {
                        waitingTimes.add(startTimeInRefUnit - et.getTimeAsDouble(referenceTimeUnit));
                    }
                    double avgWaitingTime = DateTimeUtils.mean(waitingTimes);
                    waitingTimeAveragesAllActivities.add(avgWaitingTime);
                    sb.append("Average waiting time: " + avgWaitingTime + System.lineSeparator());

                    List<ProcessInstance> processInstances = bc.getProcessInstances();
                    List<Integer> processInstanceIds = new ArrayList<Integer>();
                    for (ProcessInstance pi : processInstances) {
                        processInstanceIds.add(pi.getId());
                    }
                    sb.append("Process instances: " + processInstanceIds + " (" + processInstanceIds.size()
                            + " process instance(s))" + System.lineSeparator());
                }
                sb.append(System.lineSeparator());
                sb.append("Maximum waiting time of all activities: " + waitingTimeMaximumAllActivities
                        + System.lineSeparator());
                sb.append("Average waiting time of all activities: " + DateTimeUtils.mean(waitingTimeAveragesAllActivities)
                        + System.lineSeparator());
            }
            sb.append(System.lineSeparator());
        }

        // print


        String resourceUtilizationFileName = outputPathWithoutExtension + model.getGlobalConfiguration().getFileNameWithoutExtension() + "_batchactivitystats.txt";
        PrintWriter writer = new PrintWriter(resourceUtilizationFileName, "UTF-8");
        writer.println(sb.toString());
        writer.close();

        System.out.println("Wrote batch activity statistics to " + resourceUtilizationFileName);
    }

}
