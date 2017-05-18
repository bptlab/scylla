package de.hpi.bpt.scylla.plugin_type.parser;

import org.jdom2.Element;

import de.hpi.bpt.scylla.SimulationManager;
import de.hpi.bpt.scylla.exception.ScyllaValidationException;
import de.hpi.bpt.scylla.model.process.CommonProcessElements;

public abstract class CommonProcessElementsParserPluggable extends ParserPluggable<CommonProcessElements> {

    public static void runPlugins(SimulationManager simEnvironment, CommonProcessElements commonProcessElements,
            Element rootElement) throws ScyllaValidationException {
        run(simEnvironment, CommonProcessElementsParserPluggable.class, commonProcessElements, rootElement);
    }

}
