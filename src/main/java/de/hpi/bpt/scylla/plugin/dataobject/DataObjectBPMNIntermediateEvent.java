package de.hpi.bpt.scylla.plugin.dataobject;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNIntermediateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;

import java.util.*;

public class DataObjectBPMNIntermediateEvent extends BPMNIntermediateEventPluggable {

    @Override
    public String getName() {
        return DataObjectPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(BPMNIntermediateEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        ProcessModel processModel = processInstance.getProcessModel();
        // int processInstanceId = processInstance.getId();
        try {
            if (processModel.getDataObjectsGraph().getNodes().containsKey(desmojEvent.getNodeId())) {
                Set<Integer> refferingObjects = processModel.getDataObjectsGraph().getTargetObjects(desmojEvent.getNodeId());
                Collection<Object> allFields = desmojEvent.getDesmojObjects().getExtensionDistributions().get("dataobject").values();
                for (Object fields : allFields) {
                    Integer i = 0;
                    while (((Map<String, Map<Integer, DataObjectField>>) fields).values().toArray().length - i != 0) {
                        DataObjectField field = (DataObjectField) ((Map<String, Map<Integer, DataObjectField>>) fields).values().toArray()[i];
                        if (refferingObjects.contains(field.getNodeId())){
                            //System.out.println(processInstance.getId() + " " + desmojEvent.getDisplayName() + " " + processModel.getDisplayNames().get(field.getNodeId()) + " " + field.getDataDistributionWrapper().getSample());
                            SimulationModel model = (SimulationModel) desmojEvent.getModel();
                            Collection<Map<Integer, java.util.List<ProcessNodeInfo>>> allProcesses = model.getProcessNodeInfos().values();
                            for (Map<Integer, java.util	.List<ProcessNodeInfo>> process : allProcesses) {
                                List<ProcessNodeInfo> currentProcess = process.get(processInstance.getId());
                                for (ProcessNodeInfo task : currentProcess) {
                                    //System.out.println(task);
                                    //System.out.println(processModel.getDisplayNames().get(processModel.getDataObjectsGraph().getSourceObjects(field.getNodeId()).toArray()[0]) + " " + task.getTaskName());
                                    for (Integer j = 0; j <processModel.getDataObjectsGraph().getSourceObjects(field.getNodeId()).toArray().length; j++) {
                                        if (task.getId().equals(processModel.getDataObjectsGraph().getSourceObjects(field.getNodeId()).toArray()[j]) && task.getTransition() == ProcessNodeTransitionType.EVENT_TERMINATE) {
                                            //check all tasks and find the ones that may be looged; already logged ones will get ignored next line
                                            if (!task.getDataObjectField().containsKey(processModel.getDisplayNames().get(field.getNodeId()) + "." + field.getFieldName())) { //don't log if task already has this field logged
                                                Map<String, Object> fieldSample = new HashMap<String, Object>();
                                                Object currentSample = field.getDataDistributionWrapper().getSample();
                                                fieldSample.put(processModel.getDisplayNames().get(field.getNodeId()) + "." + field.getFieldName(), currentSample); //log Value at TaskTerminate
                                                task.SetDataObjectField(fieldSample);
                                                DataObjectField.addDataObjectValue(processInstance.getId(), fieldSample.keySet().toArray()[0].toString(), currentSample); //set current DataObjectFieldValue
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        i++;
                    }
                }
            } else {
                //do nothing and continue with the next task because Node has no dataobejcts
            }
        } catch (ScyllaRuntimeException | ScyllaValidationException | NodeNotFoundException e) {
            e.printStackTrace();
        }
    }
}
