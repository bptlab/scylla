package de.hpi.bpt.scylla.plugin_type.parser;

import java.util.Map;

import org.jdom2.Element;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;

interface IDOMParserPluggable<T> {

    Map<String, Object> parse(T simulationInput, Element sim) throws ScyllaValidationException;
}
