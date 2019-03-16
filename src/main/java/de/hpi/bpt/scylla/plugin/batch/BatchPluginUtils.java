package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

public class BatchPluginUtils {

    static final String PLUGIN_NAME = "batch";
	static final String ACTIVITIES_KEY = "batchActivities";
    private static BatchPluginUtils singleton;

     // processID:[nodeId:batchClusters]
    private Map<String, Map<Integer, List<BatchCluster>>> batchClusters = new HashMap<String, Map<Integer, List<BatchCluster>>>();
    
    
    //TODO remove
    private Set<BatchStashResourceEvent> stashEvents = new HashSet<>();

    // TODO data view blocks next batch cluster
    // TODO one process instance is responsible for the others
    // TODO how to interact with subprocess plugin? (should stop regular subprocess execution)
    // TODO BatchTaskEnableEvent extends TaskEnableEvent ?

    private BatchPluginUtils() {
    }

    static BatchPluginUtils getInstance() {
        if (singleton == null) {
            singleton = new BatchPluginUtils();
        }
        return singleton;
    }

    static public void clear()
    {
        singleton = null;
    }
    
    public static boolean isInitialized() {
    	return singleton != null;
    }

    // Map<String, TaskBeginEvent> getSubprocessStartEventsOnHold() {
    // return subprocessStartEventsOnHold;
    // }

    public Map<String, Map<Integer, List<BatchCluster>>> getBatchClusters() {
        return batchClusters;
    }

    /**
     * Called when a batch activity is enabled
     * Assigns the process instance to a cluster (creates a new one if needed)
     * Takes care of scheduling of the cluster start events
     * @param processInstance : Instance of process where the batch activity was started
     * @param nodeId : NodeId of the subprocess
     * @param parentalBeginEvent : Begin event of the subprocess task
     */
    void assignToBatchCluster(ProcessInstance processInstance, int nodeId, TaskBeginEvent parentalBeginEvent) {

        ProcessModel processModel = processInstance.getProcessModel();
        ProcessSimulationComponents simulationComponents = parentalBeginEvent.getSimulationComponents();
        /*Map<Integer, BatchActivity> batchActivities = (Map<Integer, BatchActivity>) pSimComponents.getSimulationConfiguration()
                .getExtensionValue(PLUGIN_NAME, "batchActivities");*/
        Map<Integer, BatchActivity> batchActivities = getBatchActivities(processModel);
        BatchActivity batchActivity = batchActivities.get(nodeId);

        BatchCluster cluster = null;

        // (1) select the right batch cluster
        // (1a) check if there is already a batch with the data view (= a cluster the instance can be added to)
        String processId = processModel.getId();
        Map<Integer, List<BatchCluster>> batchClustersOfProcess = batchClusters.computeIfAbsent(processId, (s) -> new HashMap<Integer, List<BatchCluster>>());
        List<BatchCluster> clusters = batchClustersOfProcess.get(nodeId);
        if (clusters != null) {
        	cluster = clusters.stream()
        		.filter(BatchCluster::hasNotStarted)
        		.filter(eachCluster -> eachCluster.isProcessInstanceMatchingGroupingCharacteristic(processInstance))
        		.findFirst().orElse(null);
        }

        // (1b) if not, create a new one
        if (cluster == null) {
            Model model = processInstance.getModel();
            boolean showInTrace = processInstance.traceIsOn();
            TimeInstant currentSimulationTime = processInstance.presentTime();
            cluster = BatchCluster.create(model, currentSimulationTime, simulationComponents, batchActivity, nodeId, showInTrace);

            // schedule BatchClusterStart at current time plus maximum timeout
            BatchClusterEnableEvent clusterStartEvent = new BatchClusterEnableEvent(processInstance, cluster);
            cluster.setEnableEvent(clusterStartEvent);

            Duration timeout = batchActivity.getActivationRule().getTimeOut(parentalBeginEvent, processInstance);
            cluster.setCurrentTimeOut(timeout);
            long timeoutInSeconds = timeout.get(ChronoUnit.SECONDS);
            TimeSpan timeSpan = new TimeSpan(timeoutInSeconds, TimeUnit.SECONDS);

            clusterStartEvent.schedule(timeSpan);

            // (4) add cluster to not started clusters
            batchClustersOfProcess
            	.computeIfAbsent(nodeId,(i) -> new ArrayList<BatchCluster>())
            	.add(cluster);

        }

        // (2) add process instance to cluster

        cluster.addProcessInstance(processInstance, parentalBeginEvent);


        // (3) check whether cluster timeout should be updated (lowered)
        // Cluster can be "started" when it is maxloaded => don't update
        // Instance can be first in cluster => nothing to update
        if (cluster.hasNotStarted() && cluster.getProcessInstances().size() > 1) {

            // if the dueDate of the current instance is earlier as of the instances added before, the cluster begin event is rescheduled
            Duration timeoutForCurrentInstance = batchActivity.getActivationRule().getTimeOut(parentalBeginEvent, processInstance);
            //testing
            TimeUnit timeUnit = TimeUnit.SECONDS;
            /**Time since cluster was created*/ //TODO: when updating to java9, please use TimeUnit.toChronoUnit() and then Duration#of(long,Chronounit)
            Duration durationBtwClusterCreationAndInstanceTaskBegin = Duration.ofSeconds(
            		(long) (parentalBeginEvent.getSimulationTimeOfSource().getTimeAsDouble(timeUnit)
            		-cluster.getCreationTime().getTimeAsDouble(timeUnit)));
            //If the new timeout from the current point in time occurs before the current timeour
            if (timeoutForCurrentInstance.plus(durationBtwClusterCreationAndInstanceTaskBegin).compareTo(cluster.getCurrentTimeOut()) < 0){
            	//set new timeout for the cluster for comparison
                cluster.setCurrentTimeOut(timeoutForCurrentInstance);

                //reschedule the cluster beginEvent
                long timeoutInSeconds = timeoutForCurrentInstance.get(ChronoUnit.SECONDS);
                TimeSpan timeSpan = new TimeSpan(timeoutInSeconds, TimeUnit.SECONDS);

                //Scheduled with timeout from current point in time
                BatchClusterEnableEvent clusterStartEvent = cluster.getEnableEvent();
                clusterStartEvent.cancel();
                clusterStartEvent.schedule(timeSpan);
            }

        }
        
        // (4) check if bc can be started
        if (cluster.getState() == BatchClusterState.MAXLOADED || cluster.getState() == BatchClusterState.READY) {
            // (2a) if bc is maxloaded, reschedule BatchClusterStart
            // there is only one event already scheduled for the cluster which is the BatchClusterStart
        	BatchClusterEnableEvent clusterStartEvent = cluster.getEnableEvent();
            if(clusterStartEvent.isScheduled())clusterStartEvent.cancel();
            clusterStartEvent.schedule(); // schedule for immediate execution
        }

    }



    BatchCluster getRunningCluster(ProcessInstance processInstance, int nodeId) {
        ProcessModel processModel = processInstance.getProcessModel();
        String processId = processModel.getId();

        if (batchClusters.containsKey(processId)) {
            Map<Integer, List<BatchCluster>> batchClustersOfProcess = batchClusters.get(processId);
            if (batchClustersOfProcess.containsKey(nodeId)) {
                List<BatchCluster> clusters = batchClustersOfProcess.get(nodeId);
                for (BatchCluster bc : clusters) {
                    List<ProcessInstance> clusterProcessInstances = bc.getProcessInstances();
                    if (bc.getState() == BatchClusterState.RUNNING
                            && clusterProcessInstances.contains(processInstance)) {
                        return bc;
                    }
                }
            }
        }
        return null;
    }

    void setClusterToRunning(BatchCluster bc) {
        // BatchActivity activity = bc.getBatchActivity();
        // String processId = activity.getProcessModel().getId();
        // int nodeId = activity.getNodeId();
        // List<BatchCluster> clusters= batchClusters.get(processId).get(nodeId);

        // // remove from not started
        // BatchActivity activity = bc.getBatchActivity();
        // String processId = activity.getProcessModel().getId();
        // int nodeId = activity.getNodeId();
        // batchClustersNotStarted.get(processId).get(nodeId).remove(bc);
        //
        // // move to running
        //
        bc.setState(BatchClusterState.RUNNING);
        bc.setStartTime(bc.presentTime());
        //
        // Map<Integer, List<BatchCluster>> batchClustersOfProcess = batchClustersRunning.get(processId);
        // if (batchClustersOfProcess == null) {
        // batchClustersOfProcess = batchClustersRunning.put(processId, new HashMap<Integer, List<BatchCluster>>());
        // List<BatchCluster> clusters = batchClustersOfProcess.get(nodeId);
        // if (clusters == null) {
        // clusters = batchClustersOfProcess.put(nodeId, new ArrayList<BatchCluster>());
        // }
        // }

        // create parental subprocess end events and put them on hold

        List<TaskBeginEvent> parentalStartEvents = bc.getParentalStartEvents();
        for (TaskBeginEvent pse : parentalStartEvents) {
            TaskTerminateEvent taskTerminateEvent = new TaskTerminateEvent(pse.getModel(), pse.getSource(),
                    pse.getSimulationTimeOfSource(), pse.getSimulationComponents(), pse.getProcessInstance(), pse.getNodeId());
            bc.getParentalEndEvents().add(taskTerminateEvent);

        }
        // parentalStartEvents.clear();

        // batchClustersOfProcess.get(nodeId).add(bc);
    }

    void setClusterToTerminated(ProcessInstance processInstance, int nodeId) {
        ProcessModel processModel = processInstance.getProcessModel();
        String processId = processModel.getId();

        if (batchClusters.containsKey(processId)) {
            Map<Integer, List<BatchCluster>> batchClustersOfProcess = batchClusters.get(processId);
            if (batchClustersOfProcess.containsKey(nodeId)) {
                List<BatchCluster> clusters = batchClustersOfProcess.get(nodeId);
                for (BatchCluster bc : clusters) {
                    List<ProcessInstance> clusterProcessInstances = bc.getProcessInstances();
                    if (bc.getState() == BatchClusterState.RUNNING
                            && clusterProcessInstances.contains(processInstance)) {
                        bc.setState(BatchClusterState.TERMINATED);
                        // return clusters.remove(bc);
                    }
                }
            }
        }
        // return false;
    }

    boolean isProcessInstanceCompleted(ProcessInstance processInstance) {
        for (EventAbstract event : processInstance.getScheduledEvents()) {
            if (!(event instanceof TaskCancelEvent)) {
                return false;
            }
        }
        return true;
    }
   
    BatchCluster getCluster(ProcessInstance processInstance) {
    	ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance == null) return null;
        ProcessModel processModel = processInstance.getProcessModel();
        int parentNodeId = processModel.getNodeIdInParent();
        return getRunningCluster(parentProcessInstance, parentNodeId);
    }
    
    BatchCluster getCluster(TaskEvent event) {
    	BatchCluster potentialCluster = getRunningCluster(event.getProcessInstance(), event.getNodeId());
    	if(Objects.isNull(potentialCluster) || !potentialCluster.isBatchTask())return null;
    	return potentialCluster;
    }
    

	public Set<BatchStashResourceEvent> getStashEvents() {
		return stashEvents;
	}
	
	public void scheduleStashEvent(BatchStashResourceEvent event) {
		stashEvents.add(event);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<Integer, BatchActivity> getBatchActivities(ProcessModel processModel) {
		return (Map<Integer, BatchActivity>) processModel.getExtensionValue(PLUGIN_NAME, ACTIVITIES_KEY);
	}
	
	public static boolean isBatchActivityEvent(Object o) {
		return Stream.of(
			BatchClusterStartEvent.class, 
			BatchClusterEnableEvent.class
		).anyMatch(each -> each.isInstance(o));
	}

}
