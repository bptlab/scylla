package de.hpi.bpt.scylla.plugin.batch;

import java.util.Map;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskBeginEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.BPMNStartEvent;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import desmoj.core.simulator.TimeSpan;

public class BatchTBPlugin extends TaskBeginEventPluggable {

    @Override
    public String getName() {
        return BatchPluginUtils.PLUGIN_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void eventRoutine(TaskBeginEvent event, ProcessInstance processInstance) throws ScyllaRuntimeException {
        BatchPluginUtils pluginInstance = BatchPluginUtils.getInstance();
        pluginInstance.logTaskEventForNonResponsiblePI(event, processInstance);

        ProcessSimulationComponents desmojObjects = event.getDesmojObjects();
        // SimulationModel model = (SimulationModel) desmojEvent.getModel();
        int nodeId = event.getNodeId();
        ProcessModel processModel = processInstance.getProcessModel();
        Map<Integer, BatchRegion> batchRegions = (Map<Integer, BatchRegion>) processModel
                .getExtensionValue(getName(), "batchRegions");
        if (batchRegions != null && batchRegions.containsKey(nodeId) && processModel.getSubProcesses().containsKey(nodeId)) {

            // subprocess plugin wants to schedule BPMNStartEvents for subprocess
            // we prevent it

            Map<Integer, ScyllaEvent> nextEventMap = event.getNextEventMap();
            Map<Integer, TimeSpan> timeSpanToNextEventMap = event.getTimeSpanToNextEventMap();

            for (Integer eventIndex : nextEventMap.keySet()) {
                ScyllaEvent eventToSchedule = nextEventMap.get(eventIndex);
                if (eventToSchedule instanceof BPMNStartEvent) {
                    Integer indexOfSubprocessBPMNStartEvent = eventIndex;

                    nextEventMap.remove(indexOfSubprocessBPMNStartEvent);
                    timeSpanToNextEventMap.remove(indexOfSubprocessBPMNStartEvent);
                    break;
                }

            }
        }
    }
}
