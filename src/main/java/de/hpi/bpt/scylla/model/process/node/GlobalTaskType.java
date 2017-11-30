package de.hpi.bpt.scylla.model.process.node;

/**
 * Types of BPMN global tasks.
 * 
 * @author Tsun Yin Wong
 *
 */
public enum GlobalTaskType {
    DEFAULT("globalTask"), USER("globalUserTask"), MANUAL("globalManualTask"), BUSINESS_RULE(
            "globalBusinessRuleTask"), SCRIPT("globalScriptTask");

    String xmlElementName;

    GlobalTaskType(String name) {
        this.xmlElementName = name;
    }

    public static GlobalTaskType getEnum(String value) {
        for (GlobalTaskType v : values())
            if (v.xmlElementName.equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return xmlElementName;
    }
}
