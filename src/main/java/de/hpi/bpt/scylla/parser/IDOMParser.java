package de.hpi.bpt.scylla.parser;

import org.jdom2.Element;

import de.hpi.bpt.scylla.exception.ScyllaValidationException;

interface IDOMParser<T> {

    T parse(Element rootElement) throws ScyllaValidationException;

}
