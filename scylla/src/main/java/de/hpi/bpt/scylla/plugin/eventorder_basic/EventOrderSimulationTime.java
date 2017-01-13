package de.hpi.bpt.scylla.plugin.eventorder_basic;

import de.hpi.bpt.scylla.plugin_type.parser.EventOrderType;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;

public class EventOrderSimulationTime extends EventOrderType {

    private EventOrderSimulationTime instance;

    public EventOrderSimulationTime() {
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    public String getName() {
        return "simulationTime";
    }

    @Override
    public int compare(String resourceId, ScyllaEvent e1, ScyllaEvent e2) {
        return e1.getSimulationTimeOfSource().compareTo(e2.getSimulationTimeOfSource());
    }

}
