package de.hpi.bpt.scylla.plugin.batch;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin.dataobject.DataDistributionWrapper;
import de.hpi.bpt.scylla.plugin.dataobject.DataObjectField;
import de.hpi.bpt.scylla.plugin.dataobject.DataObjectPluginUtils;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.*;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import org.javatuples.Pair;

public class BatchPluginUtils {

    static final String PLUGIN_NAME = "batch";
    private static BatchPluginUtils singleton;

    //Map of running instances
    public Map<Integer,TaskEnableEvent> runningInstances = new HashMap<Integer, TaskEnableEvent>();
    // processID:[nodeId:batchClusters]
    private Map<String, Map<Integer, List<BatchCluster>>> batchClusters = new HashMap<String, Map<Integer, List<BatchCluster>>>();

    /**
     * workaround: in plugins for Task{Cancel,Terminate}Event, we cannot access the resource anymore because the plugins
     * are executed at the end event routine during which the resources are removed, so we have to store them here
     * temporarily and use them for logging
     */
    // eventsourcename:resources
    private Map<String, Set<String>> tasksAndResources = new HashMap<String, Set<String>>();

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

    // Map<String, TaskBeginEvent> getSubprocessStartEventsOnHold() {
    // return subprocessStartEventsOnHold;
    // }

    public Map<String, Map<Integer, List<BatchCluster>>> getBatchClusters() {
        return batchClusters;
    }

    @SuppressWarnings("unchecked")
    void assignToBatchCluster(ProcessInstance processInstance, int nodeId, TaskBeginEvent parentalBeginEvent) {

        ProcessModel processModel = processInstance.getProcessModel();
        ProcessSimulationComponents pSimComponents = parentalBeginEvent.getDesmojObjects();
        /*Map<Integer, BatchActivity> batchActivities = (Map<Integer, BatchActivity>) pSimComponents.getSimulationConfiguration()
                .getExtensionValue(PLUGIN_NAME, "batchActivities");*/
        Map<Integer, BatchActivity> batchActivities = processModel.getBatchActivities();
        BatchActivity batchActivity = batchActivities.get(nodeId);

        BatchCluster cluster = null;

        // (1) select the right batch cluster
        // (1a) check if there is already a batch with the data view
        String processId = processModel.getId();
        Map<Integer, List<BatchCluster>> batchClustersOfProcess = batchClusters.get(processId);
        if (batchClustersOfProcess != null) {
            List<BatchCluster> clusters = batchClustersOfProcess.get(nodeId);
            if (clusters != null) {
                for (BatchCluster bc : clusters) {




                    if (batchClusterHasNotStarted(bc.getState())){
                        if( isProcessInstanceMatchingToDataView(parentalBeginEvent, processInstance, bc)) {
                            cluster = bc;
                            // clusters.remove(bc);
                            break;

                        }else{
                            //for MinMax-Rule --> check whether similar instance exists

                            //set new timeout for the cluster for comparison
//                    	Duration timeout = bc.getBatchRegion().getActivationRule()
//                    			.getTimeOut(parentalBeginEvent, bc.getProcessInstances().get(0));
//
//                		//reschedule the cluster beginEvent
//            	        long timeoutInSeconds = timeout.get(ChronoUnit.SECONDS);
//            	        TimeSpan timeSpan = new TimeSpan(timeoutInSeconds, TimeUnit.SECONDS);
//
//                        BatchClusterStartEvent clusterStartEvent = (BatchClusterStartEvent) bc.getScheduledEvents().get(0);
//                        bc.cancel();
//                        clusterStartEvent.schedule(bc, timeSpan);
                        }

                    }
                }
            }
        }

        // (1b) if not, create a new one
        if (cluster == null) {
            Model model = processInstance.getModel();
            boolean showInTrace = processInstance.traceIsOn();
            String dataView = this.getDataViewOfInstance(processInstance.getId(), batchActivity);
            TimeInstant currentSimulationTime = processInstance.presentTime();
            cluster = new BatchCluster(model, currentSimulationTime, pSimComponents, batchActivity, nodeId, dataView,
                    showInTrace);

            // schedule BatchClusterStart at current time plus maximum timeout
            BatchClusterStartEvent clusterStartEvent = new BatchClusterStartEvent(model, cluster.getName(),
                    showInTrace);

            Duration timeout = batchActivity.getActivationRule().getTimeOut(parentalBeginEvent, processInstance);
            cluster.setCurrentTimeOut(timeout);
            long timeoutInSeconds = timeout.get(ChronoUnit.SECONDS);
            TimeSpan timeSpan = new TimeSpan(timeoutInSeconds, TimeUnit.SECONDS);

            clusterStartEvent.schedule(cluster, timeSpan);

            // (4) add cluster to not started clusters

            if (batchClustersOfProcess == null) {
                batchClustersOfProcess = new HashMap<Integer, List<BatchCluster>>();
                batchClusters.put(processId, batchClustersOfProcess);
                List<BatchCluster> clusters = batchClustersOfProcess.get(nodeId);
                if (clusters == null) {
                    clusters = new ArrayList<BatchCluster>();
                    batchClustersOfProcess.put(nodeId, clusters);
                }
            }
            batchClustersOfProcess.get(nodeId).add(cluster);

        }

        // (2) add process instance to cluster

        cluster.addProcessInstance(processInstance, parentalBeginEvent);

        // (3) check if bc can be started

        //TODO check whether timeout should be updated
        if (cluster.getState() == BatchClusterState.INIT && cluster.getProcessInstances().size()>1) {

            // if the dueDate of the current instance is earlier as of the instances added before, the cluster begin event is rescheduled
            Duration timeoutForCurrentInstance = batchActivity.getActivationRule().getTimeOut(parentalBeginEvent, processInstance);
            //testing
            TimeUnit epsilon = TimeUnit.SECONDS;
            Duration durationBtwClusterStartAndInstanceTaskBegin = Duration.ofSeconds((long) (parentalBeginEvent.getSimulationTimeOfSource().getTimeAsDouble(epsilon)-cluster.getCreationTime().getTimeAsDouble(epsilon)));
            //System.out.println("InstanceEnable: "+parentalBeginEvent.getSimulationTimeOfSource().getTimeAsDouble(epsilon)+" ClusterCreation: "+cluster.getCreationTime().getTimeAsDouble(epsilon)+" Duration "+durationBtwClusterStartAndInstanceTaskBegin);
            if (timeoutForCurrentInstance.plus(durationBtwClusterStartAndInstanceTaskBegin).compareTo(cluster.getCurrentTimeOut()) < 0){

                //set new timeout for the cluster for comparison
                cluster.setCurrentTimeOut(timeoutForCurrentInstance);

                //reschedule the cluster beginEvent
                long timeoutInSeconds = timeoutForCurrentInstance.get(ChronoUnit.SECONDS);
                TimeSpan timeSpan = new TimeSpan(timeoutInSeconds, TimeUnit.SECONDS);

                BatchClusterStartEvent clusterStartEvent = (BatchClusterStartEvent) cluster.getScheduledEvents().get(0);
                cluster.cancel();
                clusterStartEvent.schedule(cluster, timeSpan);
            }

        }


        if (cluster.getState() == BatchClusterState.MAXLOADED) {
            // (2a) if bc is maxloaded, reschedule BatchClusterStart
            // there is only one event already scheduled for the cluster which is the BatchClusterStart
            BatchClusterStartEvent clusterStartEvent = (BatchClusterStartEvent) cluster.getScheduledEvents().get(0);
            cluster.cancel();
            clusterStartEvent.schedule(cluster); // schedule for immediate execution
        }

    }

    private boolean batchClusterHasNotStarted(BatchClusterState state) {
        return state == BatchClusterState.INIT || state == BatchClusterState.READY;
    }

    private boolean isProcessInstanceMatchingToDataView(TaskBeginEvent desmojEvent, ProcessInstance processInstance,
                                                        BatchCluster batchCluster) {

        String bcDataView = batchCluster.getDataView();
        String instanceDataView = getDataViewOfInstance(processInstance.getId(), batchCluster.getBatchActivity());

        if ((bcDataView == null && instanceDataView == null) || instanceDataView.equals(bcDataView)){
            return true;
        }else{

//			// only for the manual use case

//		     long numberOfDays = (long) DataObjectField.getDataObjectValue(processInstance.getId(),"RoomRequest.Date");
//		     Duration timeoutForCurrentInstance = Duration.ofDays(numberOfDays);
//		     TimeUnit epsilon = TimeUnit.SECONDS;
//		     Duration threeDaysInSeconds = Duration.ofSeconds(3*24*60*60);
//	    	 Duration durationBtwClusterStartAndInstanceTaskBegin = Duration.ofSeconds((long) (desmojEvent.getSimulationTimeOfSource().getTimeAsDouble(epsilon)-batchCluster.getCreationTime().getTimeAsDouble(epsilon)));
//	    	 //System.out.println("InstanceEnable: "+parentalBeginEvent.getSimulationTimeOfSource().getTimeAsDouble(epsilon)+" ClusterCreation: "+cluster.getCreationTime().getTimeAsDouble(epsilon)+" Duration "+durationBtwClusterStartAndInstanceTaskBegin);
//
//	    	 Duration differenceBtwClusterInst = batchCluster.getCurrentTimeOut().minus(timeoutForCurrentInstance.plus(durationBtwClusterStartAndInstanceTaskBegin));
//
//	    	 //if differenceBtwClusterInst is less than three days
//	    	 if (threeDaysInSeconds.compareTo(differenceBtwClusterInst.abs())>0){
//	        	  return true;
//	         }



            return false;
        }

    }


    String getDataViewOfInstance(Integer processInstanceID, BatchActivity batchRegion){

        if (batchRegion.getGroupingCharacteristic().isEmpty()){
            return null;
        }else{

            String dataView = "";
            for (String dataViewElement:batchRegion.getGroupingCharacteristic()){
                //TODO is currently only programmed for one Data View Element
                dataView = (String) DataObjectField.getDataObjectValue(processInstanceID,dataViewElement);
            }

            return dataView;
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
                    pse.getSimulationTimeOfSource(), pse.getDesmojObjects(), pse.getProcessInstance(), pse.getNodeId());
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

    // If the execution type is parallel this makes the entry for the not really simulated process instances for events
    void logBPMNEventForNonResponsiblePI(BPMNEvent event, ProcessInstance processInstance) {

        ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance != null) {

            ProcessModel processModel = processInstance.getProcessModel();
            int parentNodeId = processModel.getNodeIdInParent();

            BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
            BatchCluster cluster = pluginInstance.getRunningCluster(parentProcessInstance, parentNodeId);

            if (cluster != null && cluster.hasExecutionType(BatchClusterExecutionType.PARALLEL)) {
                SimulationModel model = (SimulationModel) event.getModel();

                long timestamp = Math.round(model.presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit()));
                Set<String> resources = new HashSet<String>();

                String taskName = event.getDisplayName();
                int nodeId = event.getNodeId();
                String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);
                String source = event.getSource();

                int sourceSuffix = 0;
                List<ProcessInstance> processInstances = cluster.getProcessInstances();
                for (ProcessInstance pi : processInstances) {

                    if (!processInstance.getParent().equals(pi)) {

                        // the source attribute comes from an event, but we did not really simulate the events for the
                        // non-responsible process instances, so we mock a source attribute value
                        String mockSource = source + "##" + ++sourceSuffix;

                        ProcessNodeInfo info;
                        info = new ProcessNodeInfo(nodeId, processScopeNodeId, mockSource, timestamp, taskName, resources,
                                ProcessNodeTransitionType.EVENT_BEGIN);
                        model.addNodeInfo(processModel, pi, info);

                        info = new ProcessNodeInfo(nodeId, processScopeNodeId, mockSource, timestamp, taskName, resources,
                                ProcessNodeTransitionType.EVENT_TERMINATE);
                        model.addNodeInfo(processModel, pi, info);
                    }
                }
            }
        }
    }

    // If the execution type is parallel this makes the entry for the not really simulated process instances for tasks
    void logTaskEventForNonResponsiblePI(TaskEvent event, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance != null) {

            ProcessModel processModel = processInstance.getProcessModel();
            int parentNodeId = processModel.getNodeIdInParent();

            BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
            BatchCluster cluster = pluginInstance.getRunningCluster(parentProcessInstance, parentNodeId);

            if (cluster != null && cluster.getBatchActivity().getExecutionType().equals(BatchClusterExecutionType.PARALLEL)) {
                SimulationModel model = (SimulationModel) event.getModel();

                long timestamp = Math.round(model.presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit()));

                String taskName = event.getDisplayName();
                int nodeId = event.getNodeId();
                String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);
                String source = event.getSource();

                ProcessNodeTransitionType transition;
                Set<String> resources = new HashSet<String>();
                if (event instanceof TaskEnableEvent) {
                    transition = ProcessNodeTransitionType.ENABLE;
                } else if (event instanceof TaskBeginEvent) {
                    transition = ProcessNodeTransitionType.BEGIN;
                    Set<ResourceObject> resourceObjects = processInstance.getAssignedResources().get(source)
                            .getResourceObjects();
                    for (ResourceObject res : resourceObjects) {
                        String resourceName = res.getResourceType() + "_" + res.getId();
                        resources.add(resourceName);
                    }
                    tasksAndResources.put(source, resources);
                } else if (event instanceof TaskCancelEvent) {
                    transition = ProcessNodeTransitionType.CANCEL;
                    resources = tasksAndResources.get(source);
                    tasksAndResources.remove(source);
                } else if (event instanceof TaskTerminateEvent) {
                    transition = ProcessNodeTransitionType.TERMINATE;
                    resources = tasksAndResources.get(source);
                    tasksAndResources.remove(source);
                } else {
                    throw new ScyllaRuntimeException("Task event type not supported.");
                }

                int sourceSuffix = 0;
                List<ProcessInstance> processInstances = cluster.getProcessInstances();


                for (ProcessInstance pi : processInstances) {

                    if (!processInstance.getParent().equals(pi)) {

                        // the source attribute comes from an event, but we did not really simulate the events for the
                        // non-responsible process instances, so we mock a source attribute value
                        String mockSource = source + "##" + ++sourceSuffix;

                        ProcessNodeInfo info;
                        info = new ProcessNodeInfo(nodeId, processScopeNodeId, mockSource, timestamp, taskName, resources,
                                transition);
                        model.addNodeInfo(processModel, pi, info);
                    }
                }
            }
        }
    }

    // if the execution type is sequential-taskbased, this is responsible for scheduling the same event of the next process instance, if it exists
    void scheduleNextEventInBatchProcess(ScyllaEvent event, ProcessInstance processInstance) {

        ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance != null) {

            ProcessModel processModel = processInstance.getProcessModel();
            int parentNodeId = processModel.getNodeIdInParent();

            BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
            BatchCluster cluster = pluginInstance.getRunningCluster(parentProcessInstance, parentNodeId);

            if (cluster != null && cluster.hasExecutionType(BatchClusterExecutionType.SEQUENTIAL_TASKBASED)) {
                // Get the other events or tasks from this batch to be scheduled after the current one...
                Integer nodeId = event.getNodeId();
                Pair<ScyllaEvent, ProcessInstance> eventToSchedule = cluster.getNotPIEvents(nodeId);
                if (eventToSchedule != null) {
                    eventToSchedule.getValue0().schedule(eventToSchedule.getValue1());
                    //System.out.println("Scheduled " + eventToSchedule.getValue0().getDisplayName() + " for process instance " + eventToSchedule.getValue1());
                }

                if (parentProcessInstance != cluster.getResponsibleProcessInstance()) {
                    // ..and save the next events before them getting clered
                    ScyllaEvent nextEvent = event.getNextEventMap().get(0);
                    Integer nodeIdOfNextElement = nextEvent.getNodeId();
                    cluster.addPIEvent(nodeIdOfNextElement, nextEvent, processInstance);
                    //System.out.println("Added " + nextEvent.getDisplayName() + " to cluster queue for process instance " + processInstance);
                }
            }
        }
    }

    // if the execution type is sequential-casebased, this is responsible for scheduling the next start event in the batch activty
    void scheduleNextCaseInBatchProcess(BatchCluster cluster) {
        // Get the start event of the next process instance and schedule it
        Pair<ScyllaEvent, ProcessInstance> eventToSchedule = cluster.getNotPIEvents(cluster.getStartNodeId());
        if (eventToSchedule != null) {
            eventToSchedule.getValue0().schedule(eventToSchedule.getValue1());
            //System.out.println("Scheduled " + eventToSchedule.getValue0().getDisplayName() + " for process instance " + eventToSchedule.getValue1());
        }

    }
}
