package de.hpi.bpt.scylla.plugin.boundaryevent;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNIntermediateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;
import de.hpi.bpt.scylla.simulation.utils.SimulationUtils;
import desmoj.core.simulator.TimeSpan;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class BoundaryEventBPMNIntermediateEvent extends BPMNIntermediateEventPluggable {

        @Override
        public String getName() {
            return BoundaryEventPluginUtils.PLUGIN_NAME;
        }

        @Override
        public void eventRoutine(BPMNIntermediateEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
            SimulationModel model = (SimulationModel) desmojEvent.getModel();
            ProcessModel processModel = processInstance.getProcessModel();
            if ((processModel.getEventTypes().get(desmojEvent.getNodeId()).toString()).equals("boundaryEvent") && processModel.getCancelActivities().get(desmojEvent.getNodeId())){
                //desmojEvent.setTimeSpanToNextEvent(desmojEvent.getNodeId(), new TimeSpan(0, TimeUnit.SECONDS));
                /*Collection<Map<Integer, List<ProcessNodeInfo>>> allProcesses = model.getProcessNodeInfos().values();
                for (Map<Integer, java.util	.List<ProcessNodeInfo>> process : allProcesses) {
                    Lis
                    boolean eventIsInterrupting = cancelActivities.get(nodeIdOfElementToSchedule);t<ProcessNodeInfo> currentProcess = process.get(processInstance.getId());
                    for (ProcessNodeInfo task : currentProcess) {
                        if (task.getSource().equals(desmojEvent.getSource())){
                            return true;
                        }
                    }*/
                /*long timestamp = Math.round(model.presentTime().getTimeRounded(DateTimeUtils.getReferenceTimeUnit()));

                String taskName = desmojEvent.getDisplayName();
                Set<String> resources = new HashSet<String>();

                String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, desmojEvent.getNodeId());
                ProcessNodeInfo info = new ProcessNodeInfo(2, "2", desmojEvent.getSource(), timestamp, "Task 1", resources,
                        ProcessNodeTransitionType.CANCEL);
                model.addNodeInfo(processModel, processInstance, info);*/
            }

        }


}
