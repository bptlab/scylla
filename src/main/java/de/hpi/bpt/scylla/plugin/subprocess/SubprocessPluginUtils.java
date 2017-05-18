package de.hpi.bpt.scylla.plugin.subprocess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hpi.bpt.scylla.simulation.event.TaskTerminateEvent;

class SubprocessPluginUtils {

    static final String PLUGIN_NAME = "subprocess";
    private static SubprocessPluginUtils singleton;

    // processInstanceName:[nodeId:event]
    private Map<String, Map<Integer, TaskTerminateEvent>> eventsOnHold;
    private Set<String> nameOfEventsThatWereOnHold;

    private SubprocessPluginUtils() {
        eventsOnHold = new HashMap<String, Map<Integer, TaskTerminateEvent>>();
        nameOfEventsThatWereOnHold = new HashSet<String>();
    }

    static SubprocessPluginUtils getInstance() {
        if (singleton == null) {
            singleton = new SubprocessPluginUtils();
        }
        return singleton;
    }

    Map<String, Map<Integer, TaskTerminateEvent>> getEventsOnHold() {
        return eventsOnHold;
    }

    Set<String> getNameOfEventsThatWereOnHold() {
        return nameOfEventsThatWereOnHold;
    }
}
