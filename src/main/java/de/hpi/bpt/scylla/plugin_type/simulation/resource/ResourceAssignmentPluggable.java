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
	
    public static Optional<ResourceObjectTuple> runPlugins(SimulationModel model, ScyllaEvent event) {
        Class<ResourceAssignmentPluggable> clazz = ResourceAssignmentPluggable.class;
        Iterator<? extends ResourceAssignmentPluggable> plugins = PluginLoader.dGetPlugins(clazz);
        while (plugins.hasNext()) {
        	ResourceAssignmentPluggable plugin = plugins.next();
        	Optional<ResourceObjectTuple> assignment = plugin.getResourcesForEvent(model, event);
        	if(assignment.isPresent())return Optional.of(assignment.get());
        }
        return Optional.empty();
    }

    public abstract Optional<ResourceObjectTuple> getResourcesForEvent(SimulationModel model, ScyllaEvent event);

}
