package de.hpi.bpt.scylla.plugin.batch;

import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskEnableEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import de.hpi.bpt.scylla.simulation.event.TaskEnableEvent;
import desmoj.core.simulator.TimeSpan;

public class BatchTEPlugin extends TaskEnableEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void eventRoutine(TaskEnableEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {


        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logTaskEventForNonResponsiblePI(event, processInstance);

        //ProcessSimulationComponents desmojObjects = event.getDesmojObjects();
        // SimulationModel model = (SimulationModel) desmojEvent.getModel();
        int nodeId = event.getNodeId();
        ProcessModel processModel = processInstance.getProcessModel();
        //SimulationConfiguration simulationConfiguration = desmojObjects.getSimulationConfiguration();
        /*Map<Integer, BatchActivity> batchActivities = (Map<Integer, BatchActivity>) simulationConfiguration
                .getExtensionValue(getName(), "batchActivities");*/
        Map<Integer, BatchActivity> batchActivities = processModel.getBatchActivities();

        if (batchActivities.containsKey(nodeId) && processModel.getSubProcesses().containsKey(nodeId)) {

            // in any case: put taskbeginevent of subprocess container on hold
            // String source = desmojEvent.getSource();
            int indexOfSubprocessBeginEvent = 0;

            Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
            Map<Integer, TimeSpan> timeSpanToNextEventMap = event.getTimeSpanToNextEventMap();
            // Map<String, TaskBeginEvent> subprocessStartEventsOnHold =
            // pluginInstance.getSubprocessStartEventsOnHold();

            TaskBeginEvent subprocessBeginEvent = (TaskBeginEvent) nextEventMap.get(indexOfSubprocessBeginEvent);
            pluginInstance.assignToBatchCluster(processInstance, nodeId, subprocessBeginEvent);

            nextEventMap.remove(indexOfSubprocessBeginEvent);
            timeSpanToNextEventMap.remove(indexOfSubprocessBeginEvent);

        }
    }

}
