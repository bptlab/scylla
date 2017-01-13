package de.hpi.bpt.scylla.model.process.node;

/**
 * BPMN data object types.
 * 
 * @author Tsun Yin Wong
 *
 */
public enum DataObjectType {
    DEFAULT("dataObject"); // , INPUT("dataInput"), OUTPUT("dataOutput");

    String xmlElementName;

    DataObjectType(String name) {
        this.xmlElementName = name;
    }

    public static DataObjectType getEnum(String value) {
        for (DataObjectType v : values())
            if (v.xmlElementName.equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return xmlElementName;
    }
}
