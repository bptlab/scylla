package de.hpi.bpt.scylla.plugin_type.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.plugin_type.IPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

public abstract class EventCreationPluggable implements IPluggable, IEventCreationPluggable {

    public static List<ScyllaEvent> runPlugins(ScyllaEvent currentEvent, ProcessSimulationComponents desmojObjects,
            ProcessInstance processInstance, int nextNodeId) throws ScyllaRuntimeException {
        Class<EventCreationPluggable> clazz = EventCreationPluggable.class;
        ServiceLoader<? extends EventCreationPluggable> serviceLoader = (ServiceLoader<? extends EventCreationPluggable>) ServiceLoader
                .load(clazz);
        Iterator<? extends EventCreationPluggable> plugins = serviceLoader.iterator();
        String eName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
        Set<String> namesOfExtensions = new HashSet<String>();
        List<ScyllaEvent> events = new ArrayList<ScyllaEvent>();
        while (plugins.hasNext()) {
            EventCreationPluggable plugin = plugins.next();
            String name = plugin.getName();
            if (namesOfExtensions.contains(name)) {
                try {
                    throw new ScyllaValidationException("Duplicate event creation extension name for parser type "
                            + eName + ": " + name + ". Event creation extension name must be unique per model type.");
                }
                catch (ScyllaValidationException e) {
                    e.printStackTrace();
                    // TODO REMOVE THIS
                }
            }
            namesOfExtensions.add(name);

            // run the routine
            events.addAll(plugin.createEventForNextNode(currentEvent, desmojObjects, processInstance, nextNodeId));
        }
        return events;
    }

}
