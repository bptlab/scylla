package de.hpi.bpt.scylla.plugin.batch;

import java.util.List;
import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNEndEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNEndEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;
import desmoj.core.simulator.TimeSpan;

public class BatchBPMNEEPlugin extends BPMNEndEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(BPMNEndEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {

        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logBPMNEventForNonResponsiblePI(event, processInstance);

        // Schedule parental end events
        BatchCluster cluster = pluginInstance.getCluster(processInstance);

        if (cluster != null) {

            cluster.setProcessInstanceToFinished();
            // Schedule them only if either all process instances has passed the last event of the batch activity or the execution type is parallel
            if (cluster.areAllProcessInstancesFinished() || cluster.hasExecutionType(BatchClusterExecutionType.PARALLEL)) {

                if (pluginInstance.isProcessInstanceCompleted(processInstance)) {
                    List<TaskTerminateEvent> parentalEndEvents = cluster.getParentalEndEvents();
                    for (TaskTerminateEvent pee : parentalEndEvents) {
                        pee.schedule();
                    }

                    parentalEndEvents.clear();

                	ProcessInstance parentProcessInstance = processInstance.getParent();
                    ProcessModel processModel = processInstance.getProcessModel();
                    int parentNodeId = processModel.getNodeIdInParent();
                    pluginInstance.setClusterToTerminated(parentProcessInstance, parentNodeId);
                }

                // Prevent parental task terminate event from scheduling, if there is any (from subprocess plugin)

                Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
                if (!nextEventMap.isEmpty()) {
                    Map<Integer, TimeSpan> timeSpanToNextEventMap = event.getTimeSpanToNextEventMap();
                    int indexOfParentalTaskTerminateEvent = 0;

                    nextEventMap.remove(indexOfParentalTaskTerminateEvent);
                    timeSpanToNextEventMap.remove(indexOfParentalTaskTerminateEvent);
                }
            } else if (cluster.hasExecutionType(BatchClusterExecutionType.SEQUENTIAL_CASEBASED)) {
                // Schedule the next start event
                cluster.scheduleNextCaseInBatchProcess();
            } else if (cluster.hasExecutionType(BatchClusterExecutionType.SEQUENTIAL_TASKBASED)) {
            	//Schedule other end events
            	ScyllaEvent eventToSchedule = cluster.pollNextQueuedEvent(event.getNodeId());
            	if(eventToSchedule != null)eventToSchedule.schedule();
            }
        }

    }

}
