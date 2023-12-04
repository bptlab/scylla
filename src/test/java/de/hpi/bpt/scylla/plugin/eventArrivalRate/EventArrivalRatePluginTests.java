package de.hpi.bpt.scylla.plugin.eventArrivalRate;

import de.hpi.bpt.scylla.SimulationTest;
import org.junit.jupiter.api.Test;

public class EventArrivalRatePluginTests extends SimulationTest {
    @Override
    protected String getFolderName() {
        return "EventArrivalRatePlugin";
    }

    @Test()
    public void testCanExecuteCatchingEvents() {
        runSimpleSimulation(
                "MinimalGlobalConfiguration.xml",
                "ArrivingEventsModel.bpmn",
                "ArrivingEventsConfiguration.xml");
    }
}
