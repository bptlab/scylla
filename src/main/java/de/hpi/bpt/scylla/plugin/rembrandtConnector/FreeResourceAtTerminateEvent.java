package de.hpi.bpt.scylla.plugin.rembrandtConnector;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskTerminateEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;

public class FreeResourceAtTerminateEvent extends TaskTerminateEventPluggable {

    @Override
    public String getName() {
        return rembrandtConnectorUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskTerminateEvent terminateEvent, ProcessInstance processInstance) throws ScyllaRuntimeException {
        // TODO: free Resource in Rembrandt
        System.out.println("free resource: " + rembrandtConnectorUtils.resourceTaskMap.get(Integer.toString(terminateEvent.getNodeId()) + "." + Integer.toString(terminateEvent.getProcessInstance().getId())));
    }

}
