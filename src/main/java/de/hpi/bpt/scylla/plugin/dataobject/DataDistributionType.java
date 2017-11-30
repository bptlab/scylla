package de.hpi.bpt.scylla.plugin.dataobject;

public enum DataDistributionType {
	LONG("long"), DOUBLE("double"), STRING("string"), BOOLEAN("boolean");

    String xmlElementName;

    DataDistributionType(String name) {
        this.xmlElementName = name;
    }

    public static DataDistributionType getEnum(String value) {
        for (DataDistributionType v : values())
            if (v.xmlElementName.equalsIgnoreCase(value))
                return v;
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return xmlElementName;
    }

}
