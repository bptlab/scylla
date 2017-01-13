package de.hpi.bpt.scylla.plugin.boundaryevent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.event.TaskBeginEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.TaskBeginEvent;
import desmoj.core.simulator.TimeSpan;

public class BoundaryTBPlugin extends TaskBeginEventPluggable {

    @Override
    public String getName() {
        return BoundaryEventPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(TaskBeginEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {

        SimulationModel model = (SimulationModel) desmojEvent.getModel();

        // create attached events

        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();

        List<Integer> referenceToBoundaryEvents = processModel.getReferencesToBoundaryEvents().get(nodeId);

        BoundaryEventPluginUtils pluginInstance = BoundaryEventPluginUtils.getInstance();

        if (referenceToBoundaryEvents != null) {
            double startTimeOfTask = model.presentTime().getTimeAsDouble(TimeUnit.SECONDS);
            pluginInstance.initializeBoundaryObject(startTimeOfTask, desmojEvent, referenceToBoundaryEvents);
        }

        // we usually do that in the event scheduling part, but in BoundaryEventSchedulingPlugin, it might not be called
        // if the TaskBeginEvent is put on a queue
        pluginInstance.createAndScheduleBoundaryEvents(desmojEvent, new TimeSpan(0));

    }

}
