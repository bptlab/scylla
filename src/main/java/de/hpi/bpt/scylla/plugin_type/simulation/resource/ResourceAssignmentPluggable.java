package de.hpi.bpt.scylla.plugin_type.simulation.resource;

import java.util.Iterator;
import java.util.Optional;
import de.hpi.bpt.scylla.plugin_loader.PluginLoader;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

/**
 * Plugins for this entry point can return a resource assignment for a given event.
 * If specified, this assignment is used instead of the planned assignment.
 * @author Leon Bein
 *
 */
public abstract class ResourceAssignmentPluggable implements IPluggable {
	
	/**
	 * Return first plugin that is interested to handle this assignment
	 * @param model
	 * @param event
	 * @return
	 */
    public static Optional<ResourceAssignmentPluggable> getInterestedPlugin(SimulationModel model, ScyllaEvent event) {
        Class<ResourceAssignmentPluggable> clazz = ResourceAssignmentPluggable.class;
        Iterator<? extends ResourceAssignmentPluggable> plugins = PluginLoader.dGetPlugins(clazz);
        while (plugins.hasNext()) {
        	ResourceAssignmentPluggable plugin = plugins.next();
        	if(plugin.wantsToHandleAssignment(model, event))return Optional.of(plugin);
        }
        return Optional.empty();
    }

    public abstract boolean wantsToHandleAssignment(SimulationModel model, ScyllaEvent event);
    public abstract Optional<ResourceObjectTuple> getResourcesForEvent(SimulationModel model, ScyllaEvent event);

}
