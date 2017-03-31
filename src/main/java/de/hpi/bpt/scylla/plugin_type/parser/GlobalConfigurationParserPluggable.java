package de.hpi.bpt.scylla.plugin_type.parser;

import org.jdom2.Element;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.global.GlobalConfiguration;

public abstract class GlobalConfigurationParserPluggable extends ParserPluggable<GlobalConfiguration> {

    public static void runPlugins(SimulationManager simEnvironment, GlobalConfiguration globalConfiguration,
            Element rootElement) throws ScyllaValidationException {
        run(simEnvironment, GlobalConfigurationParserPluggable.class, globalConfiguration, rootElement);
    }

}
