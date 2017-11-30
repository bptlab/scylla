package de.hpi.bpt.scylla.model.process.node;

/**
 * Definition types of BPMN events.
 * 
 * @author Tsun Yin Wong
 *
 */
public enum EventDefinitionType {

    CANCEL("cancelEventDefinition"), COMPENSATION("compensateEventDefinition"), CONDITIONAL(
            "conditionalEventDefinition"), ERROR("errorEventDefinition"), ESCALATION("escalationEventDefinition"), LINK(
                    "linkEventDefinition"), MESSAGE("messageEventDefinition"), SIGNAL(
                            "signalEventDefinition"), TERMINATE("terminateEventDefinition"), TIMER(
                                    "timerEventDefinition");

    String xmlElementName;

    EventDefinitionType(String name) {
        this.xmlElementName = name;
    }

    public static EventDefinitionType getEnum(String value) {
        for (EventDefinitionType v : values())
            if (v.xmlElementName.equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return xmlElementName;
    }
}
