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
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskCancelEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.EventAbstract;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

public class BatchPluginUtils {

    static final String PLUGIN_NAME = "batch";
    private static BatchPluginUtils singleton;

    // private Map<String, TaskBeginEvent> subprocessStartEventsOnHold = new HashMap<String, TaskBeginEvent>();
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
        Map<Integer, BatchRegion> batchRegions = (Map<Integer, BatchRegion>) processModel
                .getExtensionValue(PLUGIN_NAME, "batchRegions");
        BatchRegion region = batchRegions.get(nodeId);

        BatchCluster cluster = null;

        // (1) select the right batch cluster
        // (1a) check if there is already a batch with the data view
        String processId = processModel.getId();
        Map<Integer, List<BatchCluster>> batchClustersOfProcess = batchClusters.get(processId);
        if (batchClustersOfProcess != null) {
            List<BatchCluster> clusters = batchClustersOfProcess.get(nodeId);
            if (clusters != null) {
                for (BatchCluster bc : clusters) {
                    Map<String, String> bcDataView = bc.getDataView();
                    if (batchClusterHasNotStarted(bc.getState())
                            && isProcessInstanceMatchingToDataView(processInstance, bcDataView)) {
                        cluster = bc;
                        // clusters.remove(bc);
                        break;
                    }
                }
            }
        }

        // (1b) if not, create a new one
        if (cluster == null) {
            Model model = processInstance.getModel();
            boolean showInTrace = processInstance.traceIsOn();
            Map<String, String> dataView = null; // TODO support data view
            TimeInstant currentSimulationTime = processInstance.presentTime();
            cluster = new BatchCluster(model, currentSimulationTime, pSimComponents, region, nodeId, dataView,
                    showInTrace);

            // schedule BatchClusterStart at current time plus maximum timeout
            BatchClusterStartEvent clusterStartEvent = new BatchClusterStartEvent(model, cluster.getName(),
                    showInTrace);

            MinMaxRule minMaxRule = region.getMinMaxRule();
            // TODO implement ExistingEqualPI()
            Duration timeout = minMaxRule.getMaxTimeout();
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

    private boolean isProcessInstanceMatchingToDataView(ProcessInstance processInstance,
            Map<String, String> bcDataView) {
        /**
         * TODO no data support in process simulator, therefore no data views supported. now: all instances are in the
         * same data view
         */
        return true;
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
        // BatchRegion region = bc.getBatchRegion();
        // String processId = region.getProcessModel().getId();
        // int nodeId = region.getNodeId();
        // List<BatchCluster> clusters= batchClusters.get(processId).get(nodeId);

        // // remove from not started
        // BatchRegion region = bc.getBatchRegion();
        // String processId = region.getProcessModel().getId();
        // int nodeId = region.getNodeId();
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
        parentalStartEvents.clear();

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

    void logBPMNEventForNonResponsiblePI(BPMNEvent event, ProcessInstance processInstance) {

        ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance != null) {

            ProcessModel processModel = processInstance.getProcessModel();
            int parentNodeId = processModel.getNodeIdInParent();

            BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
            BatchCluster cluster = pluginInstance.getRunningCluster(parentProcessInstance, parentNodeId);

            if (cluster != null) {
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
                        info = new ProcessNodeInfo(processScopeNodeId, mockSource, timestamp, taskName, resources,
                                ProcessNodeTransitionType.EVENT_BEGIN);
                        model.addNodeInfo(processModel, pi, info);

                        info = new ProcessNodeInfo(processScopeNodeId, mockSource, timestamp, taskName, resources,
                                ProcessNodeTransitionType.EVENT_TERMINATE);
                        model.addNodeInfo(processModel, pi, info);
                    }
                }
            }
        }
    }

    void logTaskEventForNonResponsiblePI(TaskEvent event, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance != null) {

            ProcessModel processModel = processInstance.getProcessModel();
            int parentNodeId = processModel.getNodeIdInParent();

            BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
            BatchCluster cluster = pluginInstance.getRunningCluster(parentProcessInstance, parentNodeId);

            if (cluster != null) {
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
                }
                else if (event instanceof TaskBeginEvent) {
                    transition = ProcessNodeTransitionType.BEGIN;
                    Set<ResourceObject> resourceObjects = processInstance.getAssignedResources().get(source)
                            .getResourceObjects();
                    for (ResourceObject res : resourceObjects) {
                        String resourceName = res.getResourceType() + "_" + res.getId();
                        resources.add(resourceName);
                    }
                    tasksAndResources.put(source, resources);
                }
                else if (event instanceof TaskCancelEvent) {
                    transition = ProcessNodeTransitionType.CANCEL;
                    resources = tasksAndResources.get(source);
                    tasksAndResources.remove(source);
                }
                else if (event instanceof TaskTerminateEvent) {
                    transition = ProcessNodeTransitionType.TERMINATE;
                    resources = tasksAndResources.get(source);
                    tasksAndResources.remove(source);
                }
                else {
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
                        info = new ProcessNodeInfo(processScopeNodeId, mockSource, timestamp, taskName, resources,
                                transition);
                        model.addNodeInfo(processModel, pi, info);
                    }
                }
            }
        }
    }
}
