package de.hpi.bpt.scylla.plugin_type.parser;

import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.logger.DebugLogger;
import de.hpi.bpt.scylla.model.configuration.SimulationConfiguration;
import de.hpi.bpt.scylla.model.process.ProcessModel;

public abstract class SimulationConfigurationParserPluggable extends ParserPluggable<SimulationConfiguration> {

    public static void runPlugins(SimulationManager simEnvironment, SimulationConfiguration simulationConfiguration,
            Document document) throws ScyllaValidationException {

        Namespace simNamespace = document.getRootElement().getNamespace();
        List<Element> simElements = document.getRootElement().getChildren("simulationConfiguration", simNamespace);

        Element sim = simElements.get(0);

        String processRef = sim.getAttributeValue("processRef");
        ProcessModel processModel = simEnvironment.getProcessModels().get(processRef);

        runPluginsPerSC(simEnvironment, simulationConfiguration, processModel, sim);
    }

    private static void runPluginsPerSC(SimulationManager simEnvironment,
            SimulationConfiguration simulationConfiguration, ProcessModel processModel, Element sim)
                    throws ScyllaValidationException {

        run(simEnvironment, SimulationConfigurationParserPluggable.class, simulationConfiguration, sim);

        Map<Integer, SimulationConfiguration> configurationsOfSubProcesses = simulationConfiguration
                .getConfigurationsOfSubProcesses();

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

                SimulationConfiguration childSimulationConfiguration = configurationsOfSubProcesses.get(nodeId);
                ProcessModel subProcess = processModel.getSubProcesses().get(nodeId);
                runPluginsPerSC(simEnvironment, childSimulationConfiguration, subProcess, el);
            }
        }
    }

}
