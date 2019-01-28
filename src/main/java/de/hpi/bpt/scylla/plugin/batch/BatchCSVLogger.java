package de.hpi.bpt.scylla.plugin.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.logger.OutputLoggerPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;

/**
 * This is a logger for batch analysis.
 * The output contains timestamp and resource data for all tasks 
 * and is also enriched by additional batch region data where possible.
 * This allows for instance to analyze and predict which tasks lie in a batch region
 * and then verify the results by examining the additional  batch data.
 * @author Leon Bein
 *
 */
public class BatchCSVLogger extends OutputLoggerPluggable{
	
	
	
	/**Standard time format for table*/
	public static final DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**The header for the table*/
	private static Object[] header = new Object[] {"Process Instance", "Activity Name", "Arrival", "Start", "Complete", "Resources", "Batch Number", "Batch Type", "Natural Arrival Time"};

	public static class BatchCSVEntry {
		private Integer instanceId;
		private String activityName;
		private String arrival;
		private String start;
		private String complete;
		private String resources;
		private String batchNumber;
		private BatchClusterExecutionType batchType;
		private String naturalArrival;
		
		public Integer getInstanceId() {return instanceId;}
		public String getActivityName() {return activityName;}
		public String getArrival() {return arrival;}
		public String getStart() {return start;}
		public String getComplete() {return complete;}
		public String getResources() {return resources;}
		public String getBatchNumber() {return batchNumber;}
		public BatchClusterExecutionType getBatchType() {return batchType;}
		public String getNaturalArrival() {return naturalArrival;}

		Object[] toArray() {
			Object[] result = new Object[] {instanceId, activityName, arrival, start, complete, resources, batchNumber, batchType, naturalArrival};
			assert result.length == header.length;
			return result;
		}
		
		void fillFromArray(String[] array) {
			instanceId = Integer.parseInt(array[0]);
			activityName = array[1];
			arrival = array[2];
			start = array[3];
			complete = array[4];
			resources = array[5];
			batchNumber = array[6];
			if(!array[7].isEmpty())batchType = BatchClusterExecutionType.valueOf(array[7]);
			naturalArrival = array[8];
		}
		
		static BatchCSVEntry fromArray(String[] array) {
			BatchCSVEntry entry = new BatchCSVEntry();
			entry.fillFromArray(array);
			return entry;
		}
	}
	
	@Override
	public String getName() {
        return BatchPluginUtils.PLUGIN_NAME+"_csv";
	}

	@Override
	public void writeToLog(SimulationModel model, String outputPathWithoutExtension) throws IOException {
		/**Base time to convert event timestamps to global time*/
        ZonedDateTime baseDateTime = model.getStartDateTime();
		
        /**Get simulated batch clusters in [processId, [processInstanceId, [clustersOfInstance]]]*/
		BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        Map<String, Map<Integer, List<BatchCluster>>> clusters = pluginInstance.getBatchClusters();
        
        /**Infos for simulated events in [processId, [processInstanceId, [nodesOfInstance]]]*/
        Map<String, Map<Integer, List<ProcessNodeInfo>>> processNodeInfos = model.getProcessNodeInfos();

        //For all processes that have a batch cluster in them
        for (String processId : clusters.keySet()) {
        	
        	/**Node infos for all instances of specific process*/
        	Map<Integer, List<ProcessNodeInfo>> nodeInfoOfProcessInstances = processNodeInfos.get(processId);
        	/**Clusters for all instances of specific process*/
        	Map<Integer, List<BatchCluster>> clustersOfProcess = clusters.get(processId);
        	/**All subProcesses of the specific process, need to determine which tasks lay in which cluster*/
        	Map<Integer, ProcessModel> subProcesses = model.getDesmojObjectsMap().get(processId).getProcessModel().getSubProcesses();
            
        	/**The table to later be printed*/
        	List<BatchCSVEntry> entries = new ArrayList<BatchCSVEntry>();
            
        	/**All data for all tasks of all instances of specific process in [processInstanceId, [taskNodeId, [taskInformation]]]*/
        	Map<Integer, Map<String, BatchCSVEntry>> instanceTasks = new HashMap<Integer, Map<String, BatchCSVEntry>>();
            
            //For all instances of that process
            for(Integer instanceId : nodeInfoOfProcessInstances.keySet()) {
            	
            	/**Node infos for specific process instance*/
            	List<ProcessNodeInfo> infosOfInstance = nodeInfoOfProcessInstances.get(instanceId);
            	/**Map of tasks and their information in [taskNodeId, [taskInformation]]*/
            	Map<String, BatchCSVEntry> tasks = new HashMap<String, BatchCSVEntry>();
            	//For all information element we have for the current process instance
            	for(ProcessNodeInfo nodeInfo : infosOfInstance) {
            		/**Identifier for the node the info belongs to*/
            		String nodeId = nodeInfo.getProcessScopeNodeId();
            		/**Timestamp of node info in wanted format*/
            		String timeStamp = getTimeString(baseDateTime, nodeInfo.getTimestamp());
            		/**Check whether info belongs to a task and put all available data to table*/
            		switch(nodeInfo.getTransition()) {
            		/**On enable: create new data array for task and save enable and general information*/
            		case ENABLE:
            			BatchCSVEntry taskData = new BatchCSVEntry();
            			taskData.instanceId = instanceId;
            			taskData.activityName = nodeInfo.getTaskName();
            			taskData.arrival = timeStamp;
            			tasks.put(nodeId, taskData);
            			entries.add(taskData);
            			break;
            		/**On begin: write time and resource information*/
            		case BEGIN:
            			tasks.get(nodeId).start = timeStamp;
            			tasks.get(nodeId).resources = String.join(",",nodeInfo.getResources());
            			break;
            		/**On terminate or cancel: write time*/
            		case TERMINATE:
            		case CANCEL:
            			tasks.get(nodeId).complete = timeStamp;
            			break;
            		default: continue;
            		}
            	}
            	/**Put information to data structure to be able to quick access later when adding batch information*/
            	instanceTasks.put(instanceId, tasks);
            }
            

            
            /**For each batchcluster add information to table*/
            for (Integer clusterNodeId : clustersOfProcess.keySet()) {
            	
            	/**Get all tasks inside the cluster*/
            	ProcessModel clusterSubProcess = subProcesses.get(clusterNodeId);
            	Set<Integer> tasksOfCluster = Objects.nonNull(clusterSubProcess) ?
            			clusterSubProcess.getTasks().keySet() :
            			Collections.emptySet();
            	
            	for(BatchCluster cluster : clustersOfProcess.get(clusterNodeId)) {
            		
            		/**Cluster identifier to group tasks of different instances but one cluster*/
                	String batchNumber = cluster.getName().split("#")[1];
                	/**Execution type information*/
                	BatchClusterExecutionType batchType = cluster.getBatchActivity().getExecutionType();
                	
                	List<BatchCSVEntry> tasksOfClusterInstance = new ArrayList<>();
                	
                	/**Add information to all tasks of cluster in all instances that participated in that cluster*/
                	for(ProcessInstance instance : cluster.getProcessInstances()) {
                		
                		Map<String, BatchCSVEntry> tasksOfProcessInstance = instanceTasks.get(instance.getId());
                		/**Write information for cluster task*/
                		tasksOfProcessInstance.get(clusterNodeId.toString()).batchNumber = batchNumber;
                		tasksOfProcessInstance.get(clusterNodeId.toString()).batchType = batchType;
                		/**Write information for all tasks inside cluster*/
                		for(Integer taskId : tasksOfCluster) {
                			BatchCSVEntry task = tasksOfProcessInstance.get(SimulationUtils.getProcessScopeNodeId(clusterSubProcess, taskId));
                			if(task == null)continue; //Not all tasks are necessarily visited due to XOR-Gateways and similar
                			task.batchNumber = batchNumber;
                    		task.batchType = batchType;
                    		tasksOfClusterInstance.add(task);
                		}
                	}

        			Map<Object, List<BatchCSVEntry>> activities = tasksOfClusterInstance.stream()
            				.collect(Collectors.groupingBy((each)->{return each.activityName;}));
            		if(batchType == BatchClusterExecutionType.SEQUENTIAL_TASKBASED) {
            			for(List<BatchCSVEntry> activity : activities.values()) {
            				BatchCSVEntry minEnable = activity.stream().min((first, second)->{
            					return (first.arrival).compareTo(second.arrival);
            				}).get();
            				for(BatchCSVEntry activityInstance : activity)activityInstance.naturalArrival = minEnable.arrival;
            			}
            		}
            		
            		if(batchType == BatchClusterExecutionType.SEQUENTIAL_TASKBASED || batchType == BatchClusterExecutionType.SEQUENTIAL_CASEBASED) {
            			BatchCSVEntry firstActivity = tasksOfClusterInstance.stream()
            				.min((first, second)->{return ((String)first.arrival).compareTo((String)second.arrival);}).orElse(null);
            			tasksOfClusterInstance.stream()
            				.filter((each)->{return each.activityName.equals(firstActivity.activityName);})
            					.forEach((each)->{each.naturalArrival = instanceTasks.get(each.instanceId).get(clusterNodeId.toString()).arrival;});
            		}
            	}
            }
            
            List<Object[]> table = entries.stream()
            		.map(BatchCSVEntry::toArray)
            		.collect(Collectors.toList());
            table.add(0, header);
            
    		/**Build string for table*/
    		String s = buildString(table);
    		/**Write to file*/
    		String fileName = outputPathWithoutExtension + processId + "_processBatchActivityStats.csv";
            writeToFile(fileName, s);
        }
	}

	/**
	 * Converts a (relative) timeStamp to global time
	 * @param baseTime : The base time the is relative to
	 * @param timeStamp : A time stamp, relative to the base time, given in the reference chrono unit
	 * @return The resulting date formatted as String of {@link #timeFormat}
	 */
	private static String getTimeString(ZonedDateTime baseTime, long timeStamp) {
        ZonedDateTime zonedDateTime = baseTime.plus(timeStamp,DateTimeUtils.getReferenceChronoUnit());
        Date timestamp = new Date(zonedDateTime.toInstant().toEpochMilli());
        String dates = timeFormat.format(timestamp);
    	return dates;
	}

	/**
	 * Builds a csv string from a given table
	 * @param table : A table given as set/iterable of columns in {@link #header} format
	 * @return A csv string representation of the table, separated by ";"
	 */
	private static String buildString(Iterable<Object[]> table) {
		StringBuilder sb = new StringBuilder();
		//sb.append("\"sep=;\"\n");
		for(Object[] row : table) {
			for(Object o : row) {
				if(o != null)sb.append(o.toString());
				sb.append(";");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	/**
	 * Writes a string to a file
	 * @param path : Path to the file
	 * @param s : A string to be written
	 */
	private static void writeToFile(String path, String s) {
		try(PrintWriter pw = new PrintWriter(new File(path))){
			pw.write(s);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static int headerLength() {
		return header.length;
	}
}
