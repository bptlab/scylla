package de.hpi.bpt.scylla.model.global.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Class describing dynamic resources (e.g. humans).
 * 
 * @author Tsun
 *
 */
public class DynamicResource extends Resource {

    private double cost;
    private TimeUnit timeUnit;
    private Map<String, DynamicResourceInstance> resourceInstances = new HashMap<String, DynamicResourceInstance>();

    /**
     * Constructor.
     * 
     * @param id
     *            resource identifier
     * @param name
     *            resource name to be displayed
     * @param quantity
     *            number of resource instances to be provided
     * @param cost
     * @param timeUnit
     */
    public DynamicResource(String id, String name, int quantity, double cost, TimeUnit timeUnit) {
        super(id, name, quantity);
        this.cost = cost;
        this.timeUnit = timeUnit;
    }

    public double getCost() {
        return cost;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Map<String, DynamicResourceInstance> getResourceInstances() {
        return resourceInstances;
    }
}