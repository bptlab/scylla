package de.hpi.bpt.scylla.plugin.bpmntimer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.exception.ScyllaRuntimeException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.model.process.graph.exception.MultipleStartNodesException;
import de.hpi.bpt.scylla.model.process.graph.exception.NoStartNodeException;
import de.hpi.bpt.scylla.model.process.graph.exception.NodeNotFoundException;
import de.hpi.bpt.scylla.model.process.node.EventDefinitionType;
import de.hpi.bpt.scylla.plugin_type.simulation.event.ProcessInstanceGenerationEventPluggable;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ProcessSimulationComponents;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ProcessInstanceGenerationEvent;
import desmoj.core.simulator.TimeSpan;

public class BPMNTimerPIGEPlugin extends ProcessInstanceGenerationEventPluggable {

    @Override
    public String getName() {
        return BPMNTimerPluginUtils.PLUGIN_NAME;
    }

    @Override
    public void eventRoutine(ProcessInstanceGenerationEvent desmojEvent, ProcessInstance processInstance)
            throws ScyllaRuntimeException {
        SimulationModel model = (SimulationModel) desmojEvent.getModel();
        String processId = desmojEvent.getProcessId();
        ProcessSimulationComponents desmojObjects = model.getDesmojObjectsMap().get(processId);
        ProcessModel processModel = desmojObjects.getProcessModel();
        Integer startNodeId;
        try {
            startNodeId = processModel.getStartNode();
            if (desmojObjects.getDistributions().get(startNodeId) == null) {
                // no arrival rate defined, check if start event is timer event and use value from there
                Map<EventDefinitionType, Map<String, String>> eventDefinitions = processModel.getEventDefinitions()
                        .get(startNodeId);
                Map<String, String> definitionAttributes = eventDefinitions.get(EventDefinitionType.TIMER);
                if (definitionAttributes != null) { // if start event is timer event
                    String timeDuration = definitionAttributes.get("timeCycle"); // ISO 8601 duration
                    if (timeDuration == null) {
                        String identifier = processModel.getIdentifiers().get(startNodeId);
                        DebugLogger.log("Timer event " + identifier + " has no timer definition, skip.");
                    }
                    // TODO support timeDate and timeDuration attributes?
                    else {
                        Duration javaDuration = Duration.parse(timeDuration);
                        long duration = javaDuration.get(ChronoUnit.SECONDS);
                        TimeUnit unit = TimeUnit.SECONDS;
                        TimeSpan timeSpan = new TimeSpan(duration, unit);

                        desmojEvent.setTimeSpanToNextProcessInstance(timeSpan);
                    }
                }
            }

        }
        catch (NodeNotFoundException | MultipleStartNodesException | NoStartNodeException e) {
            DebugLogger.error(e.getMessage());
            DebugLogger.log("Error during instantiation of process model " + processModel.getId() + ".");
        }
    }

}
