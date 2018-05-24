package de.hpi.bpt.scylla.plugin.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import desmoj.core.simulator.TimeInstant;

public class BatchOnlyBatchCSVLogger extends OutputLoggerPluggable{
	
	private static DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	@Override
	public String getName() {
        return BatchPluginUtils.PLUGIN_NAME+"_csv_only_batch";
	}

	@Override
	public void writeToLog(SimulationModel model, String outputPathWithoutExtension) throws IOException {
        ZonedDateTime baseDateTime = model.getStartDateTime();
		
		BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        Map<String, Map<Integer, List<BatchCluster>>> clusters = pluginInstance.getBatchClusters();
        
        Map<String, Map<Integer, List<ProcessNodeInfo>>> processNodeInfos = model.getProcessNodeInfos();

        for (String processId : clusters.keySet()) {
        	
        	List<Object[]> table = new ArrayList<Object[]>();
            Map<Integer, List<ProcessNodeInfo>> nodeInfoOfProcessInstances = processNodeInfos.get(processId);
        	Map<Integer, String> displayNames = model.getDesmojObjectsMap().get(processId).getProcessModel().getDisplayNames();
            Map<Integer, List<BatchCluster>> clustersOfProcess = clusters.get(processId);
            
            for (Integer clusterNodeId : clustersOfProcess.keySet()) {
            	
            	String activityName = displayNames.get(clusterNodeId);
                List<BatchCluster> clustersOfNode = clustersOfProcess.get(clusterNodeId);
                
                for(BatchCluster cluster : clustersOfNode) {
                	
                	int instance = cluster.getResponsibleProcessInstance().getId();
                    String creation = getTimeString(baseDateTime, cluster.getCreationTime());
                	String start = getTimeString(baseDateTime, cluster.getStartTime());
                	String batchNumber = cluster.getName().split("#")[1];
                	Set<String> resources = nodeInfoOfProcessInstances.get(instance).get(clusterNodeId).getResources();
                	
                	String processInstanceIds = cluster.getProcessInstances().stream()
                    		.map(((Function<ProcessInstance, Integer>)ProcessInstance::getId).andThen(Object::toString))
                    		.collect(Collectors.joining(","));
                	
                	for(ProcessInstance pi : cluster.getProcessInstances()) {
                		ProcessNodeInfo pni = nodeInfoOfProcessInstances.get(pi.getId()).get(clusterNodeId);
                		pni.getTimestamp();
                		table.add(new Object[]{pi.getId(), processInstanceIds, activityName, creation, start, String.join(",",resources), batchNumber});
                        
                	}
                	
                    
                	

                	//table.add(new Object[]{processInstanceIds, activityName, creation, start, String.join(",",resources), batchNumber});
                }
            }
            
            
            /*for (int processInstanceId : nodeInfoOfProcessInstances.keySet()) {
                List<ProcessNodeInfo> nodeInfoList = nodeInfoOfProcessInstances.get(processInstanceId);
                for(ProcessNodeInfo ni : nodeInfoList) {
                	ni.getTaskName();
                }
            }*/
            
            
    		
    		String s = makeString(table);
    		String fileName = outputPathWithoutExtension + processId + "_processOnlyBatchActivityStats.csv";
            writeToFile(fileName, s);
        }
	}
	
	private static String getTimeString(ZonedDateTime baseTime, TimeInstant ti) {
    	Calendar c = ti.getTimeAsCalender();
        Date timestamp = new Date(baseTime.toInstant().toEpochMilli() + c.getTimeInMillis());
        String dates = timeFormat.format(timestamp);
    	return dates;
	}

	
	private static String makeString(List<Object[]> table) {
		StringBuilder sb = new StringBuilder();
		sb.append("\"sep=;\"\n");
		for(int i = 0; i < table.get(0).length; i++) {
			sb.append("Col "+i+"; ");
		}
		sb.append("\n");
		for(Object[] i : table) {
			for(Object o : i) {
				sb.append(o.toString());
				sb.append(";");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private static void writeToFile(String path, String s) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.write(s);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
