package de.hpi.bpt.scylla.plugin.boundaryevent;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.plugin_type.simulation.EventSchedulingPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import desmoj.core.simulator.TimeSpan;

    // This class is not used because it is not needed anymore. Everything is already done in the BoundaryTBPlugin class as you can read in the comment there.
public class BoundaryEventSchedulingPlugin extends EventSchedulingPluggable {

    @Override
    public String getName() {
        return BoundaryEventPluginUtils.PLUGIN_NAME;
    }

    @Override
    public boolean scheduleEvent(ScyllaEvent event, TimeSpan timeSpan) throws ScyllaRuntimeException {

//    	BoundaryEventPluginUtils pluginInstance = BoundaryEventPluginUtils.getInstance();
//      pluginInstance.createAndScheduleBoundaryEvents(event, timeSpan);
        return true;
    }

}
