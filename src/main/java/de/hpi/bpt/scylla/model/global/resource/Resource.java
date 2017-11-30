package de.hpi.bpt.scylla.model.global.resource;

/**
 * Abstract class for resources.
 * 
 * @author Tsun Yin Wong
 *
 */
public abstract class Resource {

    protected String id;
    protected String name;
    protected int quantity;

    /**
     * Constructor.
     * 
     * @param id
     *            resource identifier
     * @param name
     *            resource name to be displayed
     * @param quantity
     *            number of resource instances to be provided
     */
    public Resource(String id, String name, int quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }
}