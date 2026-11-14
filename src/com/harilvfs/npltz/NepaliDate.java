// a bikram sambat date value.
package com.harilvfs.npltz;

import java.util.Locale;

public final class NepaliDate {
    public final int year;
    public final int month;
    public final int day;

    public final int weekday;

    public NepaliDate(int year, int month, int day, int weekday) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.weekday = weekday;
    }

    public String monthName() {
        return CalendarLogic.monthName(month);
    }

    public String dayName() {
        return CalendarLogic.dayName(weekday);
    }

    public String iso() {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    public String formatLong() {
        return dayName() + ", " + day + " " + monthName() + " " + year;
    }

    @Override
    public String toString() {
        return formatLong();
    }
}
