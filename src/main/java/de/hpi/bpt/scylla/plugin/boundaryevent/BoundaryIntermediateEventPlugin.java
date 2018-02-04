package de.hpi.bpt.scylla.plugin.boundaryevent;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.BPMNIntermediateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.BPMNIntermediateEvent;

public class BoundaryIntermediateEventPlugin extends BPMNIntermediateEventPluggable{

	@Override
	public String getName() {
		return BoundaryEventPluginUtils.PLUGIN_NAME;
	}

	@Override
	public void eventRoutine(BPMNIntermediateEvent event, ProcessInstance processInstance)
			throws ScyllaRuntimeException {
    	Integer nodeId = event.getNodeId();
        ProcessModel processModel = processInstance.getProcessModel();
        Boolean isCancelActivity = processModel.getCancelActivities().get(nodeId);
        if (isCancelActivity != null && isCancelActivity) {
            processInstance.cancel();
        }
	}

}
