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
        ProcessModel processModel = processInstance.getProcessModel();
        int nodeId = desmojEvent.getNodeId();
        BoundaryEventPluginUtils pluginInstance = BoundaryEventPluginUtils.getInstance();

        // At the begin of each task check for corresponding boundary events. If there are some, create and schedule them.
        List<Integer> referenceToBoundaryEvents = processModel.getReferencesToBoundaryEvents().get(nodeId);

        if (referenceToBoundaryEvents != null) {
            double startTimeOfTask = model.presentTime().getTimeAsDouble(TimeUnit.SECONDS);
            // Create the corresponding boundary object for this task, which contains all necessary information.
            pluginInstance.initializeBoundaryObject(startTimeOfTask, desmojEvent, referenceToBoundaryEvents);

            // We usually do that in the event scheduling part, but in BoundaryEventSchedulingPlugin, it might not be called, if the TaskBeginEvent is put on a queue.
            // That is the reason why the eventroutine in the BoundaryEventSchedulingPlugin class is commented out and so on never used. We should not need it anymore and it could be deleted.

            // Create and schedule all boundary events this boundary object has for the current instance.
            pluginInstance.createAndScheduleBoundaryEvents(desmojEvent, new TimeSpan(0));
        }

    }

}
