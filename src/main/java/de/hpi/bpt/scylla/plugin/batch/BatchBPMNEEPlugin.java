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

        // schedule parental end events

        ProcessInstance parentProcessInstance = processInstance.getParent();
        if (parentProcessInstance != null) {

            ProcessModel processModel = processInstance.getProcessModel();
            int parentNodeId = processModel.getNodeIdInParent();

            BatchCluster cluster = pluginInstance.getRunningCluster(parentProcessInstance, parentNodeId);

            if (cluster != null) {

                cluster.setProcessInstanceToFinished();
                if (cluster.areAllProcessInstancesFinished() || cluster.getBatchActivity().getExecutionType().equals("parallel")) {

                    if (pluginInstance.isProcessInstanceCompleted(processInstance)) {
                        List<TaskTerminateEvent> parentalEndEvents = cluster.getParentalEndEvents();
                        for (TaskTerminateEvent pee : parentalEndEvents) {
                            ProcessInstance pi = pee.getProcessInstance();
                            pee.schedule(pi);
                        }

                        parentalEndEvents.clear();

                        pluginInstance.setClusterToTerminated(parentProcessInstance, parentNodeId);
                    }

                    // prevent parental task terminate event from scheduling, if there is any (from subprocess plugin)

                    Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
                    if (!nextEventMap.isEmpty()) {
                        Map<Integer, TimeSpan> timeSpanToNextEventMap = event.getTimeSpanToNextEventMap();
                        int indexOfParentalTaskTerminateEvent = 0;

                        nextEventMap.remove(indexOfParentalTaskTerminateEvent);
                        timeSpanToNextEventMap.remove(indexOfParentalTaskTerminateEvent);
                    }
                } else if (cluster.getBatchActivity().getExecutionType().equals("sequential-casebased")) {
                    /*ScyllaEvent nextProcessInstanceStartEvent = cluster.getParentalStartEvents().remove(1);
                    if (nextProcessInstanceStartEvent != null){
                        nextProcessInstanceStartEvent.schedule(nextProcessInstanceStartEvent.getProcessInstance());

                    } else {
                        throw new ScyllaRuntimeException("This should not have happend!");
                    }*/
                    pluginInstance.scheduleNextCaseInBatchProcess(event, processInstance);
                }
            }
        }

    }

}
