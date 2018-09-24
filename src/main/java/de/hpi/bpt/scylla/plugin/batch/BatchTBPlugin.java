package de.hpi.bpt.scylla.plugin.batch;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskBeginEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.TimeSpan;

public class BatchTBPlugin extends TaskBeginEventPluggable {


    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskBeginEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {

        //System.out.println(event + " with display name " + event.getDisplayName() + " || " + event.getNextEventMap() + " and source " + event.getSource());


        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logTaskEventForNonResponsiblePI(event, processInstance);

        ProcessSimulationComponents simulationComponents = event.getSimulationComponents();
        // SimulationModel model = (SimulationModel) desmojEvent.getModel();
        int nodeId = event.getNodeId();
        ProcessModel processModel = processInstance.getProcessModel();




        ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance != null) {
            int parentNodeId = processModel.getNodeIdInParent();
            BatchCluster cluster = pluginInstance.getRunningCluster(parentProcessInstance, parentNodeId);
            // If we are the representative (first executed) process instance we add the setUp time for this task
            if (cluster != null && parentProcessInstance == cluster.getResponsibleProcessInstance()) {

                // therefore we fist take a sample of the setUp distribution
                double setUpTimeToAdd = simulationComponents.getSetUpDistributionSample(nodeId);
                TimeUnit unit = simulationComponents.getSetUpDistributionTimeUnit(nodeId);
                TimeSpan setUpTimeToAddAsTimeSpan = new TimeSpan(setUpTimeToAdd, unit);
                // get the old value (this will always be the entry 0 in our map, because it's always the next)
                double standardTime = event.getTimeSpanToNextEventMap().get(0).getTimeAsDouble(TimeUnit.SECONDS);

                // and overwrite the time to the next task in the timeSpanToNextEventMap (=set the calculated time as the new time)
                TimeSpan timeForTaskWithSetUp = new TimeSpan(standardTime + setUpTimeToAddAsTimeSpan.getTimeAsDouble(TimeUnit.SECONDS), TimeUnit.SECONDS);
                event.getTimeSpanToNextEventMap().put(0, timeForTaskWithSetUp);
            }

            if (cluster != null && cluster.hasExecutionType(BatchClusterExecutionType.SEQUENTIAL_TASKBASED)) {
            	ResourceObjectTuple assignedResources = processInstance.getAssignedResources().get(event.getSource());
            	if(assignedResources != null && !assignedResources.getResourceObjects().isEmpty()) {
            		cluster.scheduleStashEvent(event, assignedResources);
            	}
                
            }

        }
        //SimulationConfiguration simulationConfiguration = desmojObjects.getSimulationConfiguration();
        /*Map<Integer, BatchActivity> batchActivities = (Map<Integer, BatchActivity>) simulationConfiguration
                .getExtensionValue(getName(), "batchActivities");*/

        Map<Integer, BatchActivity> batchActivities = processModel.getBatchActivities();
        if (batchActivities.containsKey(nodeId) && processModel.getSubProcesses().containsKey(nodeId)) {

            // subprocess plugin wants to schedule BPMNStartEvents for subprocess
            // we prevent it

            Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
            Map<Integer, TimeSpan> timeSpanToNextEventMap = event.getTimeSpanToNextEventMap();

            for (Integer indexOfSubprocessBPMNStartEvent : nextEventMap.keySet()) {
                ScyllaEvent eventToSchedule = nextEventMap.get(indexOfSubprocessBPMNStartEvent);
                if (eventToSchedule instanceof BPMNStartEvent || eventToSchedule instanceof TaskTerminateEvent) {

                    nextEventMap.remove(indexOfSubprocessBPMNStartEvent);
                    timeSpanToNextEventMap.remove(indexOfSubprocessBPMNStartEvent);
                    break;
                }

            }
        }
    }
}
