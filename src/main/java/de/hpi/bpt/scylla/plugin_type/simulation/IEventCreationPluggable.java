package de.hpi.bpt.scylla.plugin_type.simulation;

import java.util.List;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

interface IEventCreationPluggable {

    List<ScyllaEvent> createEventForNextNode(ScyllaEvent currentEvent, ProcessSimulationComponents desmojObjects,
            ProcessInstance processInstance, int nextNodeId) throws ScyllaRuntimeException;
}
