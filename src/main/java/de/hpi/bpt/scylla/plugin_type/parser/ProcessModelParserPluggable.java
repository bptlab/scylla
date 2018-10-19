package de.hpi.bpt.scylla.plugin_type.parser;

import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.process.ProcessModel;

public abstract class ProcessModelParserPluggable extends ParserPluggable<ProcessModel> {

    public static void runPlugins(SimulationManager simEnvironment, ProcessModel processModel, Element rootElement)
            throws ScyllaValidationException {
        // run(simEnvironment, ProcessModelParserPluggable.class, processModel, rootElement);

        List<Element> processElements = rootElement.getChildren("process", rootElement.getNamespace());
        for(Element processElement : processElements) {
            runPluginsPerPM(simEnvironment, processModel, processElement);
        }
    }

    private static void runPluginsPerPM(SimulationManager simEnvironment, ProcessModel processModel, Element sim)
            throws ScyllaValidationException {

        run(simEnvironment, ProcessModelParserPluggable.class, processModel, sim);

        Map<Integer, ProcessModel> subProcesses = processModel.getSubProcesses();

        for (Element el : sim.getChildren()) {
            String elementName = el.getName();

            if (elementName.equals("subProcess")) {

                String identifier = el.getAttributeValue("id");
                if (identifier == null) {
                    DebugLogger.log("Warning: Simulation configuration definition element '" + elementName
                            + "' does not have an identifier, skip.");
                    continue; // no matching element in process, so skip definition
                }
                Integer nodeId = processModel.getIdentifiersToNodeIds().get(identifier);
                if (nodeId == null) {
                    DebugLogger.log("Simulation configuration definition for process element '" + identifier
                            + "', but not available in process, skip.");
                    continue; // no matching element in process, so skip definition
                }

                ProcessModel subProcess = subProcesses.get(nodeId);
                runPluginsPerPM(simEnvironment, subProcess, el);
            }
        }
    }

}
