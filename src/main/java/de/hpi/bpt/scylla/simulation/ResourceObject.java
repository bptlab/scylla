package de.hpi.bpt.scylla.simulation;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.simulation.utils.DateTimeUtils;

/**
 * Describes a resource instance.
 * 
 * @author Tsun Yin Wong
 *
 */
public class ResourceObject {

    private String resourceType;
    private String id;
    private double timeOfLastAccess = 0;
    // private long timeInUse = 0;
    // private long timeInUseOutOfTimetable = 0;
    private double cost = 0;
    private TimeUnit timeUnit = TimeUnit.DAYS;
    private List<TimetableItem> timetable;

    private int priority = 1;
    // private int availability = 100; // 100 = yes, 0 = no
    // TODO use availability instead of adding/removing resources from queues

    /**
     * Constructor.
     * 
     * @param resourceType
     *            name of resource type
     * @param id
     *            identifier of resource instance, should be unique be resource type
     */
    public ResourceObject(String resourceType, String id) {
        this.resourceType = resourceType;
        this.id = id;
    }

    public ResourceObject(String resourceType, String id, double cost, TimeUnit timeUnit,
            List<TimetableItem> timetable) {
        this(resourceType, id);
        this.cost = cost;
        this.timeUnit = timeUnit;
        this.timetable = timetable;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getId() {
        return id;
    }

    public double getCost() {
        return cost;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public List<TimetableItem> getTimetable() {
        return timetable;
    }

    public int getPriority() {
        return priority;
    }
    //
    // public int getAvailability() {
    // return availability;
    // }
    //
    // public void setAvailability(int availability) {
    // this.availability = availability;
    // }
    //
    // public boolean isAvailable() {
    // return availability > 0;
    // }

    public double getTimeOfLastAccess() {
        return timeOfLastAccess;
    }

    public void setTimeOfLastAccess(double timeOfLastAccess) {
        this.timeOfLastAccess = timeOfLastAccess;
    }
    //
    // public long getTimeInUse() {
    // return timeInUse;
    // }
    //
    // public void setTimeInUse(long timeInUse) {
    // this.timeInUse = timeInUse;
    // }

    /**
     * Checks whether resource instance is available according to timetable.
     * 
     * @param currentDateTime
     *            the current date time
     * @return true if resource instance is available
     */
    public boolean isAvailable(ZonedDateTime currentDateTime) {
        if (timetable == null) {
            return true;
        }
        for (TimetableItem item : timetable) {
            if (DateTimeUtils.isWithin(currentDateTime, item)) {
                return true;
            }
        }
        return false;
    }
}
