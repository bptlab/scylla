package de.hpi.bpt.scylla.model.process.node;

/**
 * Types of BPMN tasks.
 * 
 * @author Tsun Yin Wong
 *
 */
public enum TaskType {
    DEFAULT("task"), SERVICE("serviceTask"), SEND("sendTask"), RECEIVE("receiveTask"), USER("userTask"), MANUAL(
            "manualTask"), BUSINESS_RULE("businessRuleTask"), SCRIPT("scriptTask");

    String xmlElementName;

    TaskType(String name) {
        this.xmlElementName = name;
    }

    public static TaskType getEnum(String value) {
        for (TaskType v : values())
            if (v.xmlElementName.equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return xmlElementName;
    }
}
