// checks calendar conversion and month boundaries on the jvm.
import com.harilvfs.npltz.CalendarLogic;
import com.harilvfs.npltz.NepaliDate;

import java.time.LocalDate;

public final class CoreTest {
    private static int checks;
    private static int failures;

    public static void main(String[] args) {
        NepaliDate reference = CalendarLogic.adToBs(LocalDate.of(2024, 4, 13));
        eq("epoch date", "2081-01-01", reference == null ? null : reference.iso());
        eq("epoch weekday", 6, reference == null ? -1 : reference.weekday);

        NepaliDate previous = CalendarLogic.adToBs(LocalDate.of(2024, 4, 12));
        eq("day before epoch", "2080-12-30", previous == null ? null : previous.iso());

        NepaliDate known = CalendarLogic.adToBs(LocalDate.of(2026, 6, 20));
        eq("known date", "2083-03-06", known == null ? null : known.iso());
        eq("known weekday", 6, known == null ? -1 : known.weekday);

        eq("month start weekday", 6, CalendarLogic.monthStartWeekday(2081, 1));
        eq("days in 2081-01", 31, CalendarLogic.daysInMonth(2081, 1));
        eq("bs to ad", LocalDate.of(2024, 4, 13), CalendarLogic.bsToAd(2081, 1, 1));
        eq("invalid month", 0, CalendarLogic.daysInMonth(2081, 13));
        eq("invalid day", false, CalendarLogic.isValidBsDate(2081, 1, 32));

        LocalDate ad = CalendarLogic.bsToAd(2083, 3, 6);
        eq("roundtrip ad", LocalDate.of(2026, 6, 20), ad);
        eq("day of year", 68, CalendarLogic.dayOfYear(2083, 3, 6));
        eq("year days", 365, CalendarLogic.yearTotalDays(2083));
        eq("week of year", 10, CalendarLogic.weekOfYear(2083, 3, 6));
        eq("first week", 1, CalendarLogic.weekOfYear(2083, 1, 7));
        eq("second week", 2, CalendarLogic.weekOfYear(2083, 1, 8));
        eq("last week", 53, CalendarLogic.weekOfYear(2083, 12, 30));
        eq("weeks in year", 53, CalendarLogic.weeksInYear(2083));
        eq("ad range label", "Apr 13 - May 13, 2024",
            CalendarLogic.monthAdRangeLabel(2081, 1));

        for (int year = CalendarLogic.BS_EPOCH_YEAR; year <= CalendarLogic.BS_MAX_YEAR; year++) {
            for (int month = 1; month <= 12; month++) {
                int last = CalendarLogic.daysInMonth(year, month);
                checkRoundtrip(year, month, 1);
                checkRoundtrip(year, month, last);
            }
        }

        if (failures > 0) {
            System.err.println(failures + " of " + checks + " checks failed");
            System.exit(1);
        }
        System.out.println("calendar core ok (" + checks + " checks)");
    }

    private static void checkRoundtrip(int year, int month, int day) {
        LocalDate ad = CalendarLogic.bsToAd(year, month, day);
        if (ad == null) {
            fail("no AD date for " + year + "-" + month + "-" + day);
            return;
        }
        NepaliDate back = CalendarLogic.adToBs(ad);
        if (back == null) {
            fail("no BS roundtrip for " + ad);
            return;
        }
        eq("roundtrip " + year + "-" + month + "-" + day,
            String.format("%04d-%02d-%02d", year, month, day), back.iso());
    }

    private static void eq(String label, Object expected, Object actual) {
        checks++;
        if (expected == null ? actual != null : !expected.equals(actual)) {
            fail(label + ": expected " + expected + ", got " + actual);
        }
    }

    private static void fail(String message) {
        failures++;
        System.err.println("FAIL " + message);
    }
}
