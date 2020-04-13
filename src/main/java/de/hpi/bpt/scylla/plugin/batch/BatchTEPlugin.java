package de.hpi.bpt.scylla.plugin.batch;

import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskEnableEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import desmoj.core.simulator.TimeSpan;

public class BatchTEPlugin extends TaskEnableEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskEnableEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {

    	if(BatchPluginUtils.isBatchActivityEvent(event))return;
    	BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();

        //ProcessSimulationComponents desmojObjects = event.getDesmojObjects();
        // SimulationModel model = (SimulationModel) desmojEvent.getModel();
        int nodeId = event.getNodeId();
        ProcessModel processModel = processInstance.getProcessModel();
        //SimulationConfiguration simulationConfiguration = desmojObjects.getSimulationConfiguration();
        /*Map<Integer, BatchActivity> batchActivities = (Map<Integer, BatchActivity>) simulationConfiguration
                .getExtensionValue(getName(), "batchActivities");*/
        Map<Integer, BatchActivity> batchActivities = BatchPluginUtils.getBatchActivities(processModel);

        /**If the task is a batch activity task or subprocess, cancel its normal execution 
         * and execute as batch activity instead*/
        if (batchActivities.containsKey(nodeId) && (processModel.getSubProcesses().containsKey(nodeId) || processModel.getTasks().containsKey(nodeId))) {
        	
        	// in any case: put taskbeginevent of subprocess container on hold
            // String source = desmojEvent.getSource();
            int indexOfSubprocessBeginEvent = 0;

            Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
            Map<Integer, TimeSpan> timeSpanToNextEventMap = event.getTimeSpanToNextEventMap();
            // Map<String, TaskBeginEvent> subprocessStartEventsOnHold =
            // pluginInstance.getSubprocessStartEventsOnHold();

            /**Might also be a normal begin event when inside a batch task*/
            TaskBeginEvent subprocessBeginEvent = event.getBeginEvent();
            pluginInstance.assignToBatchCluster(processInstance, nodeId, subprocessBeginEvent);

            nextEventMap.remove(indexOfSubprocessBeginEvent);
            timeSpanToNextEventMap.remove(indexOfSubprocessBeginEvent);
            SimulationModel model = (SimulationModel) subprocessBeginEvent.getModel();
            //If it is a normal task begin event, it might be on resource waiting queues
            model.getResourceManager().removeFromEventQueues(subprocessBeginEvent);
            if(processInstance.getAssignedResources().get(subprocessBeginEvent.getSource()) != null)
            	model.getResourceManager().releaseResourcesAndScheduleQueuedEvents(subprocessBeginEvent);            
            //model.getResourceManager().assignResourcesToEvent((SimulationModel) subprocessBeginEvent.getModel(), subprocessBeginEvent, new ResourceObjectTuple());
        }

        BatchCluster cluster = pluginInstance.getCluster(processInstance);
        if(cluster == null)cluster = pluginInstance.getCluster(event);
        if(cluster != null)cluster.taskEnableEvent(event);
    }

}
