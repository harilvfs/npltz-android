// saves and loads events and notes.
package com.harilvfs.npltz;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EventStore {
    private static final String FILE = "npltz";
    private static final String KEY_EVENTS = "calendar_events_v1";
    private static final int MAX_EVENTS = 600;

    public static final int MAX_TITLE = 80;
    public static final int MAX_NOTE = 500;

    private static final Comparator<Event> ORDER = new Comparator<Event>() {
        @Override
        public int compare(Event a, Event b) {
            int value = Integer.compare(a.year, b.year);
            if (value != 0) {
                return value;
            }
            value = Integer.compare(a.month, b.month);
            if (value != 0) {
                return value;
            }
            value = Integer.compare(a.day, b.day);
            if (value != 0) {
                return value;
            }
            value = Integer.compare(a.hour, b.hour);
            if (value != 0) {
                return value;
            }
            value = Integer.compare(a.minute, b.minute);
            if (value != 0) {
                return value;
            }
            return a.title.compareToIgnoreCase(b.title);
        }
    };

    public static final class Event {
        public long id;
        public int year;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public String title;
        public String note;
        public boolean notified;

        public Event(long id, int year, int month, int day, int hour, int minute,
                     String title, String note) {
            this.id = id;
            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.title = title == null ? "" : title;
            this.note = note == null ? "" : note;
        }

        public String dateKey() {
            return EventStore.dateKey(year, month, day);
        }
    }

    private EventStore() {
    }

    public static List<Event> all(Context context) {
        List<Event> events = read(context);
        Collections.sort(events, ORDER);
        return events;
    }

    public static List<Event> forDate(Context context, int year, int month, int day) {
        List<Event> events = new ArrayList<>();
        for (Event event : read(context)) {
            if (event.year == year && event.month == month && event.day == day) {
                events.add(event);
            }
        }
        Collections.sort(events, ORDER);
        return events;
    }

    public static Event find(Context context, long id) {
        if (id <= 0) {
            return null;
        }
        for (Event event : read(context)) {
            if (event.id == id) {
                return event;
            }
        }
        return null;
    }

    public static Set<String> dayKeys(Context context) {
        Set<String> keys = new HashSet<>();
        for (Event event : read(context)) {
            keys.add(event.dateKey());
        }
        return keys;
    }

    public static Event save(Context context, Event event) {
        if (event == null) {
            return null;
        }
        event.title = clean(event.title, MAX_TITLE);
        event.note = clean(event.note, MAX_NOTE);
        if (event.title.isEmpty()) {
            return null;
        }
        if (!validDate(event.year, event.month, event.day)) {
            return null;
        }
        event.hour = Math.max(0, Math.min(23, event.hour));
        event.minute = Math.max(0, Math.min(59, event.minute));

        List<Event> events = read(context);
        if (event.id > 0) {
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).id == event.id) {
                    events.set(i, event);
                    write(context, events);
                    return event;
                }
            }
        }

        long id = System.currentTimeMillis();
        boolean collision;
        do {
            collision = false;
            for (Event existing : events) {
                if (existing.id == id) {
                    id++;
                    collision = true;
                    break;
                }
            }
        } while (collision);
        event.id = id;
        events.add(event);
        while (events.size() > MAX_EVENTS) {
            events.remove(0);
        }
        write(context, events);
        return event;
    }

    public static void markNotified(Context context, long id) {
        List<Event> events = read(context);
        for (Event event : events) {
            if (event.id == id) {
                event.notified = true;
                write(context, events);
                return;
            }
        }
    }

    public static boolean delete(Context context, long id) {
        if (id <= 0) {
            return false;
        }
        List<Event> events = read(context);
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).id == id) {
                events.remove(i);
                write(context, events);
                return true;
            }
        }
        return false;
    }

    public static String dateKey(int year, int month, int day) {
        return String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    private static boolean validDate(int year, int month, int day) {
        return year >= CalendarLogic.BS_EPOCH_YEAR
            && year <= CalendarLogic.BS_MAX_YEAR
            && month >= 1
            && month <= 12
            && day >= 1
            && day <= CalendarLogic.daysInMonth(year, month);
    }

    private static List<Event> read(Context context) {
        List<Event> events = new ArrayList<>();
        SharedPreferences prefs =
            context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_EVENTS, "");
        if (raw == null || raw.isEmpty()) {
            return events;
        }
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) {
                    continue;
                }
                Event event = new Event(
                    object.optLong("id", 0L),
                    object.optInt("year", 0),
                    object.optInt("month", 0),
                    object.optInt("day", 0),
                    object.optInt("hour", 0),
                    object.optInt("minute", 0),
                    object.optString("title", ""),
                    object.optString("note", "")
                );
                event.notified = object.optBoolean("notified", false);
                if (event.id > 0 && validDate(event.year, event.month, event.day)
                    && !event.title.trim().isEmpty()) {
                    events.add(event);
                }
            }
        } catch (Exception ignored) {
        }
        return events;
    }

    private static void write(Context context, List<Event> events) {
        JSONArray array = new JSONArray();
        for (Event event : events) {
            try {
                JSONObject object = new JSONObject();
                object.put("id", event.id);
                object.put("year", event.year);
                object.put("month", event.month);
                object.put("day", event.day);
                object.put("hour", event.hour);
                object.put("minute", event.minute);
                object.put("title", clean(event.title, MAX_TITLE));
                object.put("note", clean(event.note, MAX_NOTE));
                object.put("notified", event.notified);
                array.put(object);
            } catch (Exception ignored) {
            }
        }
        SharedPreferences prefs =
            context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_EVENTS, array.toString()).apply();
    }

    private static String clean(String value, int limit) {
        String clean = value == null ? "" : value.trim();
        if (clean.length() > limit) {
            clean = clean.substring(0, limit).trim();
        }
        return clean;
    }
}
