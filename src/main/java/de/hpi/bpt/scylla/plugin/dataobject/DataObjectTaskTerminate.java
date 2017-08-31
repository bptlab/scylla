package de.hpi.bpt.scylla.plugin.dataobject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskTerminateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;

public class DataObjectTaskTerminate extends TaskTerminateEventPluggable  {

	@Override
	public String getName() {
		 return DataObjectPluginUtils.PLUGIN_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void eventRoutine(TaskTerminateEvent desmojEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        ProcessModel processModel = processInstance.getProcessModel();
        // int processInstanceId = processInstance.getId();
        try {
			try {
				Set<Integer> refferingObjects = processModel.getDataObjectsGraph().getTargetObjects(desmojEvent.getNodeId());
					Collection<Object> allFields = desmojEvent.getDesmojObjects().getExtensionDistributions().get("dataobject").values();
					for (Object fields : allFields) {
						Integer i = 0;
						while (((Map<String, Map<Integer, DataObjectField>>) fields).values().toArray().length - i != 0) {
							DataObjectField field = (DataObjectField) ((Map<String, Map<Integer, DataObjectField>>) fields).values().toArray()[i];
							if (refferingObjects.contains(field.getNodeId())){
								System.out.println(processInstance.getId() + " " + desmojEvent.getDisplayName() + " " + processModel.getDisplayNames().get(field.getNodeId()) + " " + field.getDataDistributionWrapper().getSample());
							}
							i++;
						}
					}
			} catch (NodeNotFoundException e) {
				e.printStackTrace();
			}
		} catch (ScyllaRuntimeException | ScyllaValidationException e1) {
			e1.printStackTrace();
		}
	}	
}
