package de.hpi.bpt.scylla.simulation.utils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.hpi.bpt.scylla.logger.ProcessNodeInfo;
import de.hpi.bpt.scylla.logger.ProcessNodeTransitionType;
import de.hpi.bpt.scylla.logger.ResourceInfo;
import de.hpi.bpt.scylla.logger.ResourceStatus;
import de.hpi.bpt.scylla.model.global.resource.TimetableItem;
import de.hpi.bpt.scylla.model.process.ProcessModel;
import de.hpi.bpt.scylla.simulation.ProcessInstance;
import de.hpi.bpt.scylla.simulation.ResourceObject;
import de.hpi.bpt.scylla.simulation.ResourceObjectTuple;
import de.hpi.bpt.scylla.simulation.SimulationModel;
import de.hpi.bpt.scylla.simulation.event.ScyllaEvent;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Contains static methods related to date and time.
 * 
 * @author Tsun Yin Wong
 *
 */
public class DateTimeUtils {

    private static Comparator<TimetableItem> comparatorByWeekdayFromAndBeginTimeAsc;

    static {
        comparatorByWeekdayFromAndBeginTimeAsc = new Comparator<TimetableItem>() {

            @Override
            public int compare(TimetableItem o1, TimetableItem o2) {
                DayOfWeek weekdayFrom1 = o1.getWeekdayFrom();
                LocalTime beginTime1 = o1.getBeginTime();
                DayOfWeek weekdayFrom2 = o2.getWeekdayFrom();
                LocalTime beginTime2 = o2.getBeginTime();
                int weekdayDiff = weekdayFrom1.compareTo(weekdayFrom2);
                if (weekdayDiff != 0) {
                    return weekdayDiff;
                }
                return beginTime1.compareTo(beginTime2);
            }
        };
    }

    // "yyyy-MM-dd'T'HH:mm:ss.SSS"
    private static DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;
    private static ZonedDateTime startDateTime;
    private static TimeUnit timeUnit;
    private static ChronoUnit chronoUnit;

    public static ZoneId getZoneId() {
        return dtf.getZone();
    }

    public static void setZoneId(ZoneId zone) {
        dtf = dtf.withZone(zone);
    }

    public static void setStartDateTime(ZonedDateTime date) {
        DateTimeUtils.startDateTime = date;
    }

    public static TimeUnit getReferenceTimeUnit() {
        return DateTimeUtils.timeUnit;
    }

    public static ChronoUnit getReferenceChronoUnit() {
        return DateTimeUtils.chronoUnit;
    }

    public static void setReferenceTimeUnit(TimeUnit unit) {
        DateTimeUtils.timeUnit = unit;
        DateTimeUtils.chronoUnit = convert(unit);
    }

    public static ZonedDateTime getDateTime(TimeInstant timeInstant) {
        // Java 8 uses ChronoUnit (java.time), but desmoj does not have java 8, so conversion is necessary
        long timeRelativeToStart = timeInstant.getTimeRounded(timeUnit);
        return startDateTime.plus(timeRelativeToStart, chronoUnit);
    }

    public static TimeInstant getTimeInstant(ZonedDateTime dateTime) {
        long timeRelativeToStart = chronoUnit.between(startDateTime, dateTime);
        TimeInstant timeInstant = new TimeInstant(timeRelativeToStart, timeUnit);
        return timeInstant;
    }

    /**
     * Returns the next date with the given weekday. Return the date which has been provided if it is already on the
     * weekday.
     * 
     * @param date
     *            the date
     * @param dayOfWeek
     *            the weekday
     * @return the next date with the given weekday or the current date if it has the given weekday
     */
    public static LocalDate getNextOrSameWeekday(LocalDate date, DayOfWeek dayOfWeek) {
        return date.with(TemporalAdjusters.nextOrSame(dayOfWeek));
    }

    /**
     * Returns the duration between two datetimes.
     * 
     * @param start
     *            the start datetime
     * @param end
     *            the end datetime
     * @return the duration between the datetimes in the reference time unit
     */
    public static long getDuration(ZonedDateTime start, ZonedDateTime end) {
        return Duration.between(start, end).get(chronoUnit);
    }

    public static ZonedDateTime parse(String dateString) {
        if (dateString == null) {
            return null;
        }
        return ZonedDateTime.parse(dateString, dtf);
    }

    public static String format(ZonedDateTime date) {
        if (date == null) {
            return null;
        }
        return date.format(dtf);
    }

    private static ChronoUnit convert(TimeUnit tu) {
        if (tu == null) {
            return null;
        }
        switch (tu) {
            case DAYS :
                return ChronoUnit.DAYS;
            case HOURS :
                return ChronoUnit.HOURS;
            case MINUTES :
                return ChronoUnit.MINUTES;
            case SECONDS :
                return ChronoUnit.SECONDS;
            case MICROSECONDS :
                return ChronoUnit.MICROS;
            case MILLISECONDS :
                return ChronoUnit.MILLIS;
            case NANOSECONDS :
                return ChronoUnit.NANOS;
            default :
                assert false : "There are no other TimeUnit ordinal values.";
                return null;
        }
    }

    /**
     * Checks whether provided datetime is within interval of given timetable item. Interval of timetable item is
     * left-closed, right-open [start,end).
     * 
     * @param retrievalDateTime
     *            the datetime in question
     * @param timetableItem
     *            the timetable item in question
     * @return true if datetime is within the interval
     */
    public static boolean isWithin(ZonedDateTime retrievalDateTime, TimetableItem timetableItem) {
        // info from retrievalDateTime
        DayOfWeek dayOfWeek = retrievalDateTime.getDayOfWeek();
        int hour = retrievalDateTime.getHour();
        int minute = retrievalDateTime.getMinute();
        int second = retrievalDateTime.getSecond();
        LocalTime localTime = LocalTime.of(hour, minute, second);

        // info from timetableItem
        DayOfWeek weekdayFrom = timetableItem.getWeekdayFrom();
        DayOfWeek weekdayTo = timetableItem.getWeekdayTo();
        LocalTime beginTime = timetableItem.getBeginTime();
        LocalTime endTime = timetableItem.getEndTime();

        if (weekdayFrom.getValue() < weekdayTo.getValue()) {
            if (weekdayFrom.getValue() <= dayOfWeek.getValue() && dayOfWeek.getValue() <= weekdayTo.getValue()) {
                if (weekdayFrom == dayOfWeek) {
                    if (beginTime.compareTo(localTime) <= 0) {
                        return true;
                    };
                }
                else if (weekdayTo == dayOfWeek) {
                    if (localTime.compareTo(endTime) < 0) {
                        return true;
                    }
                }
                else {
                    // dayOfWeek is between weekdayFrom and weekdayTo
                    return true;
                }
            }
        }
        else if (weekdayFrom.getValue() == weekdayTo.getValue()) {
            if (weekdayFrom == dayOfWeek) { // == weekdayTo
                if (beginTime.compareTo(endTime) < 0) {
                    if (beginTime.compareTo(localTime) <= 0 && localTime.compareTo(endTime) < 0) {
                        return true;
                    };
                }
                else {
                    if (beginTime.compareTo(localTime) <= 0 || localTime.compareTo(endTime) < 0) {
                        return true;
                    }
                }
            }
        }
        // e.g. if weekdayFrom is SUNDAY and weekdayTo is TUESDAY
        else if (weekdayFrom.getValue() > weekdayTo.getValue()) {
            if (weekdayFrom.getValue() <= dayOfWeek.getValue() || dayOfWeek.getValue() <= weekdayTo.getValue()) {
                if (weekdayFrom == dayOfWeek) {
                    if (beginTime.compareTo(localTime) <= 0) {
                        return true;
                    };
                }
                else if (weekdayTo == dayOfWeek) {
                    if (localTime.compareTo(endTime) < 0) {
                        return true;
                    }
                }
                else {
                    // dayOfWeek is between weekdayTo and weekdayFrom
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the duration between the provided datetime and the next/same datetime with the given weekday and time.
     * 
     * @param startDateTime
     *            the start datetime
     * @param untilWeekday
     *            weekday of end datetime
     * @param untilTime
     *            time of end datetime
     * @return the duration between the start datetime and the built end datetime
     */
    public static double getDurationInReferenceUnit(ZonedDateTime startDateTime, DayOfWeek untilWeekday,
            LocalTime untilTime) {
        ZonedDateTime endDateTime = getNextOrSameZonedDateTime(startDateTime, untilWeekday, untilTime);
        return chronoUnit.between(startDateTime, endDateTime);
    }

    /**
     * Returns the next datetime with the given weekday and time from the provided datetime. Returns the provided
     * datetime if it already has the given weekday and the given time.
     * 
     * @param dateTime
     *            the datetime
     * @param weekday
     *            the desired weekday
     * @param time
     *            the desired time
     * @return the next datetime with the given weekday and the given time or the current datetime if it has the given
     *         weekday and given time
     */
    public static ZonedDateTime getNextOrSameZonedDateTime(ZonedDateTime dateTime, DayOfWeek weekday, LocalTime time) {
        ZonedDateTime dt = dateTime.with(TemporalAdjusters.nextOrSame(weekday));
        return dt.with(time);
    }

    /**
     * Returns the next datetime with the given weekday and time from the provided datetime.
     * 
     * @param dateTime
     *            the datetime
     * @param weekday
     *            the desired weekday
     * @param time
     *            the desired time
     * @return the next datetime with the given weekday and the given time
     */
    public static ZonedDateTime getNextZonedDateTime(ZonedDateTime dateTime, DayOfWeek weekday, LocalTime time) {
        ZonedDateTime dt = dateTime;
        if (!(dateTime.isBefore(dateTime.with(time)) && dateTime.getDayOfWeek() == weekday)) {
            dt = dateTime.with(TemporalAdjusters.next(weekday));
        }
        return dt.with(time);
    }

    public static Comparator<TimetableItem> getComparatorByWeekdayFromAndBeginTimeAsc() {
        return comparatorByWeekdayFromAndBeginTimeAsc;
    }

    /**
     * Calculates the relative end time of a task. Consideres timetables of resources instances. If any resource
     * instance is idle, the duration is extended by the idle time.
     * 
     * @param timeSpan
     *            the original duration of the task without any interruptions
     * @param presentTime
     *            current simulation time
     * @param tuple
     *            resource instances assigned to the task
     * @param event
     *            source event (for logging purposes)
     * @return the end time of the task
     */
    public static TimeInstant getTaskTerminationTime(TimeSpan timeSpan, TimeInstant presentTime,
            ResourceObjectTuple tuple, ScyllaEvent event) {

        SimulationModel model = (SimulationModel) event.getModel();
        ProcessInstance processInstance = event.getProcessInstance();
        ProcessModel processModel = processInstance.getProcessModel();
        String source = event.getSource();
        String taskName = event.getDisplayName();
        int nodeId = event.getNodeId();
        String processScopeNodeId = SimulationUtils.getProcessScopeNodeId(processModel, nodeId);

        Set<String> resources = new HashSet<String>();
        Set<ResourceObject> resourceObjects = tuple.getResourceObjects();

        for (ResourceObject res : resourceObjects) {
            String resourceName = res.getResourceType() + "_" + res.getId();
            resources.add(resourceName);
        }

        // start

        long duration = timeSpan.getTimeRounded(timeUnit);
        if (duration == 0) {
            return presentTime;
        }

        ZonedDateTime dateTime = DateTimeUtils.getDateTime(presentTime);
        List<TimetableItem> timetable = tuple.getSharedTimetable();

        if (timetable == null) {
            return new TimeInstant(presentTime.getTimeRounded(timeUnit) + duration);
        }

        Integer index = null;
        for (int i = 0; i < timetable.size(); i++) {
            TimetableItem item = timetable.get(i);
            if (isWithin(dateTime, item)) {
                index = i;
                break;
            }
        }
        long timePassed = 0;
        while (timePassed < duration) {
            TimetableItem item = timetable.get(index);
            DayOfWeek untilWeekday = item.getWeekdayTo();
            LocalTime untilTime = item.getEndTime();

            ZonedDateTime dateTimeUntilEnd = getNextOrSameZonedDateTime(dateTime, untilWeekday, untilTime);
            long durationUntilEnd = chronoUnit.between(dateTime, dateTimeUntilEnd);
            long amountToAdd;

            if (timePassed + durationUntilEnd >= duration) { // task completes in current timetable item
                amountToAdd = duration - timePassed;
            }
            else { // until end of timetable item
                amountToAdd = durationUntilEnd;
            }
            timePassed += amountToAdd;
            dateTime = dateTime.plus(amountToAdd, chronoUnit);

            if (timePassed + durationUntilEnd < duration) {
                // task is not completed in current timetable item, so jump to the start of the next timetable item
                if (model.isOutputLoggingOn()) {
                    // log idle during use
                    ResourceStatus status = ResourceStatus.IN_USE_IDLE;
                    long timeRelativeToStart = getTimeInstant(dateTime).getTimeRounded(timeUnit);
                    ResourceInfo info = new ResourceInfo(timeRelativeToStart, status, processInstance, nodeId);
                    for (ResourceObject obj : tuple.getResourceObjects()) {
                        String resourceType = obj.getResourceType();
                        String resourceId = obj.getId();
                        model.addResourceInfo(resourceType, resourceId, info);
                    }

                    ProcessNodeTransitionType transition = ProcessNodeTransitionType.PAUSE;
                    ProcessNodeInfo nodeInfo = new ProcessNodeInfo(processScopeNodeId, source, timeRelativeToStart,
                            taskName, resources, transition);
                    model.addNodeInfo(processModel, processInstance, nodeInfo);
                }

                index++;
                if (index == timetable.size()) {
                    index = 0;
                }
                TimetableItem nextItem = timetable.get(index);
                untilWeekday = nextItem.getWeekdayFrom();
                untilTime = nextItem.getBeginTime();
                dateTime = getNextZonedDateTime(dateTime, untilWeekday, untilTime);

                if (model.isOutputLoggingOn()) {
                    // log back to work
                    ResourceStatus status = ResourceStatus.IN_USE;
                    long timeRelativeToStart = getTimeInstant(dateTime).getTimeRounded(timeUnit);
                    ResourceInfo info = new ResourceInfo(timeRelativeToStart, status, processInstance, nodeId);
                    for (ResourceObject obj : tuple.getResourceObjects()) {
                        String resourceType = obj.getResourceType();
                        String resourceId = obj.getId();
                        model.addResourceInfo(resourceType, resourceId, info);
                    }

                    ProcessNodeTransitionType transition = ProcessNodeTransitionType.RESUME;
                    ProcessNodeInfo nodeInfo = new ProcessNodeInfo(processScopeNodeId, source, timeRelativeToStart,
                            taskName, resources, transition);
                    model.addNodeInfo(processModel, processInstance, nodeInfo);
                }
            }
        }

        TimeInstant timeInstant = getTimeInstant(dateTime);
        return timeInstant;
    }

    /**
     * Calculates the intersection between two timetables.
     * 
     * @param timetable1
     *            first timetable
     * @param timetable2
     *            second timetable
     * @return the intersection between the two timetables
     */
    public static List<TimetableItem> intersectTimetables(List<TimetableItem> timetable1,
            List<TimetableItem> timetable2) {
        if (timetable1 == null) { // null means "anytime", so choose timetable2
            return timetable2;
        }
        else if (timetable2 == null) { // null means "anytime", so choose timetable1
            return timetable1;
        }

        Collections.sort(timetable1, comparatorByWeekdayFromAndBeginTimeAsc);
        Collections.sort(timetable2, comparatorByWeekdayFromAndBeginTimeAsc);

        List<TimetableItem> intersection = new ArrayList<TimetableItem>();

        int index1 = 0;
        int index2 = 0;
        while (index1 < timetable1.size() && index2 < timetable2.size()) {
            TimetableItem item1 = timetable1.get(index1);
            TimetableItem item2 = timetable2.get(index2);

            TimetableItem result = intersectTimetableItems(item1, item2);
            if (result != null) {
                intersection.add(result);
            }

            int diff = compareWeekdayTime(item1.getWeekdayTo(), item1.getEndTime(), item2.getWeekdayTo(),
                    item2.getEndTime());
            if (diff <= 0) { // move to next interval from timetable1 if it ends earlier
                index1++;
            }
            else {
                index2++;
            }

        }

        return intersection;
    }

    /**
     * Calculates the intersection between two timetable items.
     * 
     * @param item1
     *            first timetable item
     * @param item2
     *            second timetable item
     * @return the intersection between the two timetable items
     */
    public static TimetableItem intersectTimetableItems(TimetableItem item1, TimetableItem item2) {
        TimetableItem intersectedItem = null;

        int diff;
        diff = compareWeekdayTime(item1.getWeekdayFrom(), item1.getBeginTime(), item2.getWeekdayFrom(),
                item2.getBeginTime());

        DayOfWeek startDay;
        LocalTime startTime;
        if (diff > 0) {// item2 is earlier, so take item1
            startDay = item1.getWeekdayFrom();
            startTime = item1.getBeginTime();
            diff = compareWeekdayTime(startDay, startTime, item2.getWeekdayTo(), item2.getEndTime());
            if (diff >= 0) { // start of item1 is after end of item2, so there is no overlap
                return null;
            }
        }
        else {
            startDay = item2.getWeekdayFrom();
            startTime = item2.getBeginTime();
            diff = compareWeekdayTime(startDay, startTime, item1.getWeekdayTo(), item1.getEndTime());
            if (diff >= 0) { // start of item2 is after end of item1, so there is no overlap
                return null;
            }
        }

        DayOfWeek endDay;
        LocalTime endTime;

        diff = compareWeekdayTime(item1.getWeekdayTo(), item1.getEndTime(), item2.getWeekdayTo(), item2.getEndTime());

        if (diff <= 0) { // item1 is earlier, so take it
            endDay = item1.getWeekdayTo();
            endTime = item1.getEndTime();
        }
        else {
            endDay = item2.getWeekdayTo();
            endTime = item2.getEndTime();
        }

        intersectedItem = new TimetableItem(startDay, endDay, startTime, endTime);

        return intersectedItem;
    }

    public static int compareWeekdayTime(DayOfWeek weekday1, LocalTime time1, DayOfWeek weekday2, LocalTime time2) {
        int diff = weekday1.compareTo(weekday2);
        if (diff == 0) {
            diff = time1.compareTo(time2);
        }
        return diff;
    }

    public static double mean(List<Double> lastAccessesOfFirst) {
        double average = 0;
        int t = 1;
        for (double x : lastAccessesOfFirst) {
            average += (x - average) / t;
            ++t;
        }
        return average;
    }

    /**
     * Calculates the duration of availability from the start datetime of simulation to the given end datetime (which is
     * calculated from the provided time instant which describes the time relative to simulation start). The calculation
     * considers the timetable.
     * 
     * @param timetable
     *            the timetable
     * @param timeInstant
     *            the time instant from which the end datetime is calculated
     * @return the duration of availability
     */
    public static long getAvailabilityTime(List<TimetableItem> timetable, TimeInstant timeInstant) {
        long availabilityTime = 0;
        ZonedDateTime endDateTime = getDateTime(timeInstant);

        if (timetable == null) {
            return timeInstant.getTimeRounded(timeUnit);
        }
        else if (timetable.isEmpty()) {
            return availabilityTime;
        }

        int index = getTimeTableIndexWithinOrNext(startDateTime, timetable);
        ZonedDateTime dateTime = startDateTime;

        while (dateTime.compareTo(endDateTime) < 0) {
            TimetableItem item = timetable.get(index);

            DayOfWeek untilWeekday = item.getWeekdayTo();
            LocalTime untilTime = item.getEndTime();
            ZonedDateTime dateTimeUntilEnd = getNextOrSameZonedDateTime(dateTime, untilWeekday, untilTime);

            DayOfWeek startWeekday = item.getWeekdayFrom();
            LocalTime startTime = item.getBeginTime();
            ZonedDateTime dateTimeOfStart = getNextOrSameZonedDateTime(dateTime, startWeekday, startTime);
            if (dateTime.compareTo(dateTimeOfStart) < 0) { // i.e. dateTime is before dateTimeOfStart
                dateTime = dateTimeOfStart;
            }

            if (dateTimeUntilEnd.compareTo(endDateTime) > 0) { // end of timetable item is after end of simulation
                dateTimeUntilEnd = endDateTime;
            }
            long durationUntilEnd = chronoUnit.between(dateTime, dateTimeUntilEnd);
            availabilityTime += durationUntilEnd;

            dateTime = dateTimeUntilEnd;

            if (!dateTime.equals(endDateTime)) {
                // prepare next timetable item

                index++;
                if (index == timetable.size()) {
                    index = 0;
                }
                TimetableItem nextItem = timetable.get(index);
                untilWeekday = nextItem.getWeekdayFrom();
                untilTime = nextItem.getBeginTime();
                dateTime = getNextZonedDateTime(dateTime, untilWeekday, untilTime);
            }
        }

        return availabilityTime;
    }

    /**
     * Determines the index of the timetable item in which the given datetime is located.
     * 
     * @param dateTime
     *            the datetime in question
     * @param timetable
     *            the timetable in question
     * @return the index of the timetable item in which the given datetime is located
     */
    public static int getTimeTableIndexWithinOrNext(ZonedDateTime dateTime, List<TimetableItem> timetable) {
        int index = -1;
        double minDurationToItemStart = Double.MAX_VALUE;
        for (int i = 0; i < timetable.size(); i++) {
            TimetableItem item = timetable.get(i);
            if (isWithin(dateTime, item)) {
                return i;
            }
            double durationUntilItemStart = getDurationInReferenceUnit(dateTime, item.getWeekdayFrom(),
                    item.getBeginTime());
            if (durationUntilItemStart < minDurationToItemStart) {
                minDurationToItemStart = durationUntilItemStart;
                index = i;
            }
        }
        return index;
    }

    public static double convertCost(TimeUnit source, TimeUnit target, double costPerSourceUnit) {
        long oneDayInNanoseconds = 86400000000000L;
        long oneDayInSourceUnit = timeUnit.convert(oneDayInNanoseconds, source);
        long oneDayInTargetUnit = timeUnit.convert(oneDayInNanoseconds, target);

        double factor = oneDayInTargetUnit / (double) oneDayInSourceUnit;

        return factor * costPerSourceUnit;
    }

    // public static void main(String[] args) {
    // ZonedDateTime retrievalDateTime;
    // TimetableItem timetableItem;
    // // retrievalDateTime = ZonedDateTime.now(ZoneId.of("GMT+02:00"));
    // retrievalDateTime = ZonedDateTime.of(2016, 10, 31, 7, 0, 0, 0, ZoneId.of("GMT+01:00"));
    // timetableItem = new TimetableItem(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, LocalTime.of(7, 0, 0),
    // LocalTime.of(23, 0, 0));
    // System.out.println(isWithin(retrievalDateTime, timetableItem));
    // timetableItem = new TimetableItem(DayOfWeek.TUESDAY, DayOfWeek.TUESDAY, LocalTime.of(7, 0, 0),
    // LocalTime.of(23, 0, 0));
    // System.out.println(isWithin(retrievalDateTime, timetableItem));
    // timetableItem = new TimetableItem(DayOfWeek.TUESDAY, DayOfWeek.TUESDAY, LocalTime.of(23, 0, 0),
    // LocalTime.of(7, 0, 0));
    // System.out.println(isWithin(retrievalDateTime, timetableItem));
    // timetableItem = new TimetableItem(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, LocalTime.of(23, 0, 0),
    // LocalTime.of(19, 0, 0));
    // System.out.println(isWithin(retrievalDateTime, timetableItem));
    // timetableItem = new TimetableItem(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, LocalTime.of(13, 0, 0),
    // LocalTime.of(15, 0, 0));
    // System.out.println(isWithin(retrievalDateTime, timetableItem));
    // timetableItem = new TimetableItem(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, LocalTime.of(13, 0, 0),
    // LocalTime.of(19, 0, 0));
    // System.out.println(isWithin(retrievalDateTime, timetableItem));
    // timetableItem = new TimetableItem(DayOfWeek.TUESDAY, DayOfWeek.MONDAY, LocalTime.of(13, 0, 0),
    // LocalTime.of(19, 0, 0));
    // System.out.println(isWithin(retrievalDateTime, timetableItem));

    // List<TimetableItem> timetable1 = new ArrayList<TimetableItem>();
    // timetable1.add(
    // new TimetableItem(DayOfWeek.MONDAY, DayOfWeek.MONDAY, LocalTime.of(8, 0, 0), LocalTime.of(23, 0, 0)));
    // timetable1.add(
    // new TimetableItem(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, LocalTime.of(23, 30, 0), LocalTime.of(1, 0, 0)));
    // timetable1.add(
    // new TimetableItem(DayOfWeek.TUESDAY, DayOfWeek.TUESDAY, LocalTime.of(5, 0, 0), LocalTime.of(9, 0, 0)));
    // timetable1.add(new TimetableItem(DayOfWeek.TUESDAY, DayOfWeek.TUESDAY, LocalTime.of(16, 0, 0),
    // LocalTime.of(18, 0, 0)));
    //
    // List<TimetableItem> timetable2 = new ArrayList<TimetableItem>();
    // timetable2.add(
    // new TimetableItem(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, LocalTime.of(16, 0, 0), LocalTime.of(1, 0, 0)));
    // timetable2.add(new TimetableItem(DayOfWeek.TUESDAY, DayOfWeek.TUESDAY, LocalTime.of(13, 0, 0),
    // LocalTime.of(17, 0, 0)));
    //
    // List<TimetableItem> intersection = intersectTimetables(timetable1, timetable2);
    // for (TimetableItem item : intersection) {
    // System.out.println(item.getWeekdayFrom() + " " + item.getBeginTime() + ", " + item.getWeekdayTo() + " "
    // + item.getEndTime());
    // }
    // }
}
