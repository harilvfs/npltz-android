// ad <-> bs conversion and calendar helpers.
package com.harilvfs.npltz;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class CalendarLogic {
    public static final int BS_EPOCH_YEAR = CalendarData.BS_EPOCH_YEAR;
    public static final int BS_MAX_YEAR = CalendarData.BS_MAX_YEAR;
    public static final LocalDate AD_EPOCH = LocalDate.of(1918, 4, 13);

    public static final int NEW_YEAR_MONTH = 1;
    public static final int NEW_YEAR_DAY = 1;

    public static final int BIRTHDAY_MONTH = 5;
    public static final int BIRTHDAY_DAY = 30;

    private static final String[] MONTH_NAMES = {
        "Baisakh", "Jestha", "Ashad", "Shrawan", "Bhadra", "Ashwin",
        "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra",
    };

    private static final String[] WEEKDAY_SHORT = {
        "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT",
    };

    private static final String[] DAY_NAMES = {
        "Aaitabar", "Sombar", "Mangalbar", "Budhabar", "Bihibar", "Shukrabar", "Shanibar",
    };

    private static final String[] MONTH_NAMES_NE = {
        "\u092c\u0948\u0936\u093e\u0916",
        "\u091c\u0947\u0920",
        "\u0905\u0938\u093e\u0930",
        "\u0936\u094d\u0930\u093e\u0935\u0923",
        "\u092d\u0926\u094c",
        "\u0905\u0938\u094b\u091c",
        "\u0915\u093e\u0930\u094d\u0924\u093f\u0915",
        "\u092e\u0902\u0938\u093f\u0930",
        "\u092a\u0941\u0937",
        "\u092e\u093e\u0918",
        "\u092b\u093e\u0932\u094d\u0917\u0941\u0928",
        "\u091a\u0948\u0924",
    };

    private static final String[] DAY_NAMES_NE = {
        "\u0906\u0907\u0924\u092c\u093e\u0930",
        "\u0938\u094b\u092e\u092c\u093e\u0930",
        "\u092e\u0902\u0917\u0932\u092c\u093e\u0930",
        "\u092c\u0941\u0927\u092c\u093e\u0930",
        "\u092c\u093f\u0939\u0940\u092c\u093e\u0930",
        "\u0936\u0941\u0915\u094d\u0930\u092c\u093e\u0930",
        "\u0936\u0928\u093f\u092c\u093e\u0930",
    };

    private static final String[] WEEKDAY_SHORT_NE = {
        "\u0906\u0907\u0924",
        "\u0938\u094b\u092e",
        "\u092e\u0902\u0917\u0932",
        "\u092c\u0941\u0927",
        "\u092c\u093f\u0939\u0940",
        "\u0936\u0941\u0915\u094d\u0930",
        "\u0936\u0928\u093f",
    };

    private static final String[] AD_MONTH_NAMES_NE = {
        "\u091c\u0928\u0935\u0930\u0940",
        "\u092b\u0947\u092c\u094d\u0930\u0941\u0905\u0930\u0940",
        "\u092e\u093e\u0930\u094d\u091a",
        "\u0905\u092a\u094d\u0930\u093f\u0932",
        "\u092e\u0947",
        "\u091c\u0941\u0928",
        "\u091c\u0941\u0932\u093e\u0908",
        "\u0905\u0917\u0938\u094d\u091f",
        "\u0938\u0947\u092a\u094d\u091f\u0947\u092e\u094d\u092c\u0930",
        "\u0905\u0915\u094d\u091f\u094b\u092c\u0930",
        "\u0928\u094b\u092d\u0947\u092e\u094d\u092c\u0930",
        "\u0921\u093f\u0938\u0947\u092e\u094d\u092c\u0930",
    };

    private static final char[] DEVANAGARI_DIGITS = {
        '\u0966', '\u0967', '\u0968', '\u0969', '\u096a',
        '\u096b', '\u096c', '\u096d', '\u096e', '\u096f',
    };

    private static final String[] AD_DAY_NAMES = {
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
    };

    private static final DateTimeFormatter RANGE_START =
        DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter RANGE_END =
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter AD_SHORT =
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private CalendarLogic() {
    }

    public static String monthName(int month) {
        return monthName(month, false);
    }

    public static boolean isNewYear(int month, int day) {
        return month == NEW_YEAR_MONTH && day == NEW_YEAR_DAY;
    }

    public static boolean isBirthday(int month, int day) {
        return month == BIRTHDAY_MONTH && day == BIRTHDAY_DAY;
    }

    public static String monthName(int month, boolean nepali) {
        if (month < 1 || month > 12) {
            return "--";
        }
        return nepali ? MONTH_NAMES_NE[month - 1] : MONTH_NAMES[month - 1];
    }

    public static String dayName(int weekday) {
        return dayName(weekday, false);
    }

    public static String dayName(int weekday, boolean nepali) {
        if (weekday < 0 || weekday > 6) {
            return "--";
        }
        return nepali ? DAY_NAMES_NE[weekday] : DAY_NAMES[weekday];
    }

    public static String weekdayShort(int weekday, boolean nepali) {
        if (weekday < 0 || weekday > 6) {
            return "--";
        }
        return nepali ? WEEKDAY_SHORT_NE[weekday] : WEEKDAY_SHORT[weekday];
    }

    public static String adMonthName(int month, boolean nepali) {
        if (month < 1 || month > 12) {
            return "--";
        }
        if (nepali) {
            return AD_MONTH_NAMES_NE[month - 1];
        }
        String[] names = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        };
        return names[month - 1];
    }

    public static String toDevanagariDigits(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9') {
                out.append(DEVANAGARI_DIGITS[c - '0']);
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String number(int value, boolean nepali) {
        return nepali ? toDevanagariDigits(String.valueOf(value)) : String.valueOf(value);
    }

    public static String number(long value, boolean nepali) {
        return nepali ? toDevanagariDigits(String.valueOf(value)) : String.valueOf(value);
    }

    public static String adDayName(LocalDate date) {
        return AD_DAY_NAMES[weekday(date)];
    }

    public static int weekday(LocalDate date) {
        return date.getDayOfWeek().getValue() % 7;
    }

    public static int weekdayOfBs(int year, int month, int day) {
        LocalDate ad = bsToAd(year, month, day);
        return ad == null ? -1 : weekday(ad);
    }

    // walk the bs month table from the fixed ad epoch.
    public static NepaliDate adToBs(LocalDate ad) {
        long remaining = ad.toEpochDay() - AD_EPOCH.toEpochDay();
        if (remaining < 0) {
            return null;
        }

        int year = BS_EPOCH_YEAR;
        while (year <= BS_MAX_YEAR) {
            int total = CalendarData.totalDays(year);
            int[] months = CalendarData.months(year);
            if (total <= 0 || months == null) {
                return null;
            }
            if (remaining < total) {
                for (int i = 0; i < 12; i++) {
                    if (remaining < months[i]) {
                        return new NepaliDate(year, i + 1, (int) remaining + 1, weekday(ad));
                    }
                    remaining -= months[i];
                }
                return null;
            }
            remaining -= total;
            year++;
        }
        return null;
    }

    public static NepaliDate adToBs(int year, int month, int day) {
        try {
            return adToBs(LocalDate.of(year, month, day));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    // cumulative days before each bs year, built once. keeps bsToAd O(month)
    // instead of walking every year from the epoch on every call (it runs per
    // drawn calendar cell, per widget cell and per event).
    private static final long[] YEAR_OFFSETS = buildYearOffsets();

    private static long[] buildYearOffsets() {
        long[] offsets = new long[BS_MAX_YEAR - BS_EPOCH_YEAR + 1];
        long total = 0;
        for (int year = BS_EPOCH_YEAR; year <= BS_MAX_YEAR; year++) {
            offsets[year - BS_EPOCH_YEAR] = total;
            total += CalendarData.totalDays(year);
        }
        return offsets;
    }

    // add whole bs years first, then months and days.
    public static LocalDate bsToAd(int year, int month, int day) {
        if (!isValidBsDate(year, month, day)) {
            return null;
        }
        long total = YEAR_OFFSETS[year - BS_EPOCH_YEAR];
        int[] months = CalendarData.months(year);
        for (int i = 0; i < month - 1; i++) {
            total += months[i];
        }
        total += day - 1L;
        return AD_EPOCH.plusDays(total);
    }

    public static NepaliDate plusDays(NepaliDate date, int days) {
        if (date == null) {
            return null;
        }
        LocalDate ad = bsToAd(date.year, date.month, date.day);
        if (ad == null) {
            return null;
        }
        return adToBs(ad.plusDays(days));
    }

    public static NepaliDate todayBs() {
        return adToBs(LocalDate.now());
    }

    public static int daysInMonth(int year, int month) {
        if (month < 1 || month > 12) {
            return 0;
        }
        int[] months = CalendarData.months(year);
        return months == null ? 0 : months[month - 1];
    }

    public static boolean isValidBsDate(int year, int month, int day) {
        if (year < BS_EPOCH_YEAR || year > BS_MAX_YEAR) {
            return false;
        }
        if (month < 1 || month > 12) {
            return false;
        }
        int days = daysInMonth(year, month);
        return days > 0 && day >= 1 && day <= days;
    }

    public static int monthStartWeekday(int year, int month) {
        LocalDate start = bsToAd(year, month, 1);
        return start == null ? -1 : weekday(start);
    }

    public static int dayOfYear(int year, int month, int day) {
        if (!isValidBsDate(year, month, day)) {
            return 0;
        }
        int total = 0;
        int[] months = CalendarData.months(year);
        for (int i = 0; i < month - 1; i++) {
            total += months[i];
        }
        return total + day;
    }

    public static int yearTotalDays(int year) {
        return CalendarData.totalDays(year);
    }

    // bs weeks are seven-day blocks from baisakh 1, not iso weeks.
    public static int weekOfYear(int year, int month, int day) {
        int dayOfYear = dayOfYear(year, month, day);
        return dayOfYear <= 0 ? 0 : (dayOfYear - 1) / 7 + 1;
    }

    public static int weeksInYear(int year) {
        int total = yearTotalDays(year);
        return total <= 0 ? 0 : (total + 6) / 7;
    }

    public static String monthAdRangeLabel(int year, int month) {
        return monthAdRangeLabel(year, month, false);
    }

    public static String monthAdRangeLabel(int year, int month, boolean nepali) {
        int days = daysInMonth(year, month);
        LocalDate start = bsToAd(year, month, 1);
        LocalDate end = bsToAd(year, month, days);
        if (start == null || end == null) {
            return "--";
        }
        if (nepali) {
            return adMonthName(start.getMonthValue(), true) + " "
                + number(start.getDayOfMonth(), true) + " - "
                + adMonthName(end.getMonthValue(), true) + " "
                + number(end.getDayOfMonth(), true) + ", "
                + number(end.getYear(), true);
        }
        return RANGE_START.format(start) + " - " + RANGE_END.format(end);
    }

    public static String yearAdRangeLabel(int year) {
        return yearAdRangeLabel(year, false);
    }

    public static String yearAdRangeLabel(int year, boolean nepali) {
        LocalDate start = bsToAd(year, 1, 1);
        LocalDate end = bsToAd(year, 12, daysInMonth(year, 12));
        if (start == null || end == null) {
            return "--";
        }
        if (nepali) {
            return adMonthName(start.getMonthValue(), true) + " "
                + number(start.getDayOfMonth(), true) + ", "
                + number(start.getYear(), true) + " - "
                + adMonthName(end.getMonthValue(), true) + " "
                + number(end.getDayOfMonth(), true) + ", "
                + number(end.getYear(), true);
        }
        return RANGE_START.format(start) + ", " + start.getYear() + " - "
            + RANGE_END.format(end);
    }

    public static String adShort(LocalDate date) {
        return adShort(date, false);
    }

    public static String adShort(LocalDate date, boolean nepali) {
        if (nepali) {
            return adMonthName(date.getMonthValue(), true) + " "
                + number(date.getDayOfMonth(), true) + ", "
                + number(date.getYear(), true);
        }
        return AD_SHORT.format(date);
    }

}
