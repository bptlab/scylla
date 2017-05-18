package de.hpi.bpt.scylla.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.bpt.scylla.model.global.resource.TimetableItem;

/**
 * Container class for set of resource instances assigned to a task / process instance.
 * 
 * @author Tsun Yin Wong
 */
public class ResourceObjectTuple implements Cloneable {

    private Set<ResourceObject> resourceObjects = new HashSet<ResourceObject>();
    private List<TimetableItem> sharedTimetable = null;
    private Double avgOfLastAccesses = null;
    // private int accumulatedIndex = 0;

    public Set<ResourceObject> getResourceObjects() {
        return resourceObjects;
    }

    public void setResourceObjects(Set<ResourceObject> resourceObjects) {
        this.resourceObjects = resourceObjects;
    }

    public int size() {
        return resourceObjects.size();
    }

    public List<TimetableItem> getSharedTimetable() {
        return sharedTimetable;
    }

    public void setSharedTimetable(List<TimetableItem> sharedTimetable) {
        this.sharedTimetable = sharedTimetable;
    }

    public Double getAvgOfLastAccesses() {
        return avgOfLastAccesses;
    }

    public void setAvgOfLastAccesses(Double avgOfLastAccesses) {
        this.avgOfLastAccesses = avgOfLastAccesses;
    }

    // public int getAccumulatedIndex() {
    // return accumulatedIndex;
    // }
    //
    // public void setAccumulatedIndex(int accumulatedIndex) {
    // this.accumulatedIndex = accumulatedIndex;
    // }

    @Override
    public ResourceObjectTuple clone() {
        ResourceObjectTuple clone = new ResourceObjectTuple();

        if (resourceObjects != null) {
            HashSet<ResourceObject> resourceObjectsClone = new HashSet<ResourceObject>();
            resourceObjectsClone.addAll(resourceObjects);
            clone.setResourceObjects(resourceObjectsClone);
        }

        if (sharedTimetable != null) {
            List<TimetableItem> sharedTimetableClone = new ArrayList<TimetableItem>();
            sharedTimetableClone.addAll(sharedTimetable);
            clone.setSharedTimetable(sharedTimetableClone);
        }

        if (avgOfLastAccesses != null) {
            Double avgOfLastAccessesClone = new Double(avgOfLastAccesses);
            clone.setAvgOfLastAccesses(avgOfLastAccessesClone);
        }

        return clone;
    }
}
