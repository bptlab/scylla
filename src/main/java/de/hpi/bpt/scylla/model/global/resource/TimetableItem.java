package de.hpi.bpt.scylla.model.global.resource;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimetableItem {

    DayOfWeek weekdayFrom;
    DayOfWeek weekdayTo;
    LocalTime beginTime;
    LocalTime endTime;

    public TimetableItem(DayOfWeek weekdayFrom, DayOfWeek weekdayTo, LocalTime beginTime, LocalTime endTime) {
        this.weekdayFrom = weekdayFrom;
        this.weekdayTo = weekdayTo;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public DayOfWeek getWeekdayFrom() {
        return weekdayFrom;
    }
    public DayOfWeek getWeekdayTo() {
        return weekdayTo;
    }

    public LocalTime getBeginTime() {
        return beginTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
