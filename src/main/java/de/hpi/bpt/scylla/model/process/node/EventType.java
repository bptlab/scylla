package de.hpi.bpt.scylla.model.process.node;

/**
 * Types of BPMN events.
 * 
 * @author Tsun Yin Wong
 *
 */
public enum EventType {
    START("startEvent"), INTERMEDIATE_CATCH("intermediateCatchEvent"), INTERMEDIATE_THROW(
            "intermediateThrowEvent"), IMPLICIT_THROW("implicitThrowEvent"), BOUNDARY("boundaryEvent"), END("endEvent");

    String xmlElementName;

    EventType(String name) {
        this.xmlElementName = name;
    }

    public static EventType getEnum(String value) {
        for (EventType v : values())
            if (v.xmlElementName.equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return xmlElementName;
    }
}
