package de.hpi.bpt.scylla.model.process.node;

/**
 * Types of BPMN gateways.
 * 
 * @author Tsun Yin Wong
 *
 */
public enum GatewayType {
    DEFAULT("gateway"), EXCLUSIVE("exclusiveGateway"), INCLUSIVE("inclusiveGateway"), PARALLEL(
            "parallelGateway"), COMPLEX("complexGateway"), EVENT_BASED("eventBasedGateway");

    String xmlElementName;

    GatewayType(String name) {
        this.xmlElementName = name;
    }

    public static GatewayType getEnum(String value) {
        for (GatewayType v : values())
            if (v.xmlElementName.equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return xmlElementName;
    }
}
