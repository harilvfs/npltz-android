// schedules and shows event reminders.
package com.harilvfs.npltz;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class EventNotifier {
    public static final String CHANNEL_ID = "calendar_events_v3";
    public static final String ACTION_EVENT = "com.harilvfs.npltz.CALENDAR_EVENT";
    public static final String EXTRA_EVENT_ID = "calendar_event_id";

    private static final int NOTIFY_BASE = 42000;

    private EventNotifier() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        // old channel ids should not collect duplicate entries.
        manager.deleteNotificationChannel("calendar_events");
        manager.deleteNotificationChannel("calendar_events_v2");

        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "Calendar events",
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Reminders for events and notes saved on the calendar.");

        // miui can drop background channel sound; alertplayer plays it instead.
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setShowBadge(true);
        manager.createNotificationChannel(channel);
    }

    public static long triggerMillis(EventStore.Event event) {
        if (event == null) {
            return -1L;
        }
        LocalDate ad = CalendarLogic.bsToAd(event.year, event.month, event.day);
        if (ad == null) {
            return -1L;
        }
        ZonedDateTime trigger = ad
            .atTime(Math.max(0, Math.min(23, event.hour)),
                Math.max(0, Math.min(59, event.minute)))
            .atZone(ZoneId.systemDefault());
        return trigger.toInstant().toEpochMilli();
    }

    public static boolean schedule(Context context, EventStore.Event event) {
        if (event == null) {
            return false;
        }
        ensureChannel(context);
        long trigger = triggerMillis(event);
        if (trigger <= System.currentTimeMillis()) {
            return false;
        }
        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return false;
        }
        PendingIntent pending = alarmIntent(context, event.id);
        alarmManager.cancel(pending);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
                setAllowWhileIdle(alarmManager, trigger, pending);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, trigger, pending);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, trigger, pending);
            }
            return true;
        } catch (SecurityException ignored) {
            // exact alarms may be blocked; use an inexact alarm instead.
            try {
                setAllowWhileIdle(alarmManager, trigger, pending);
                return true;
            } catch (Exception fallbackFailure) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void setAllowWhileIdle(AlarmManager alarmManager, long trigger,
        PendingIntent pending) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, trigger, pending);
        }
    }

    public static void cancel(Context context, EventStore.Event event) {
        if (event == null || event.id <= 0) {
            return;
        }
        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent(context, event.id));
        }
    }

    public static void rescheduleAll(Context context) {
        ensureChannel(context);
        long now = System.currentTimeMillis();
        for (EventStore.Event event : EventStore.all(context)) {
            if (triggerMillis(event) > now) {
                schedule(context, event);
            }
        }
    }

    public static void show(Context context, long eventId) {
        EventStore.Event event = EventStore.find(context, eventId);
        if (event == null) {
            return;
        }
        ensureChannel(context);
        if (!canNotify(context)) {
            return;
        }

        Prefs prefs = new Prefs(context);
        Theme theme = Theme.byKey(prefs.getTheme());
        boolean nepali = prefs.isNepali();
        String time = NotificationScheduler.timeLabel(context, event.hour, event.minute);
        String date = CalendarLogic.monthName(event.month, nepali) + " "
            + CalendarLogic.number(event.day, nepali) + ", "
            + CalendarLogic.number(event.year, nepali);
        String meta = date + " \u00b7 " + time;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_today)
            .setContentTitle(event.title)
            .setContentText(meta + (event.note.isEmpty() ? "" : " \u00b7 " + event.note))
            .setStyle(new NotificationCompat.BigTextStyle().bigText(
                event.note.isEmpty() ? meta : meta + "\n\n" + event.note
            ))
            .setContentIntent(openIntent(context, eventId))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(theme.primary)
            .setWhen(triggerMillis(event))
            .setShowWhen(true);
        if (!event.note.isEmpty()) {
            builder.setSubText(date + " \u00b7 " + time);
        }

        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        manager.notify(notificationId(eventId), builder.build());
        EventStore.markNotified(context, eventId);
        AlertPlayer.play(context);
    }

    public static boolean canNotify(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null
            || !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false;
        }
        NotificationChannel channel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? manager.getNotificationChannel(CHANNEL_ID) : null;
        if (channel != null
            && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
            return false;
        }
        return true;
    }

    private static PendingIntent alarmIntent(Context context, long eventId) {
        Intent intent = new Intent(context, EventReceiver.class)
            .setAction(ACTION_EVENT)
            .setData(Uri.parse("npltz://event/" + eventId))
            .putExtra(EXTRA_EVENT_ID, eventId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(
            context,
            requestCode(eventId),
            intent,
            flags
        );
    }

    private static PendingIntent openIntent(Context context, long eventId) {
        Intent intent = new Intent(context, MainActivity.class)
            .setData(Uri.parse("npltz://event/open/" + eventId))
            .putExtra(EXTRA_EVENT_ID, eventId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(
            context,
            requestCode(eventId) + 1,
            intent,
            flags
        );
    }

    private static int requestCode(long eventId) {
        return (int) (eventId ^ (eventId >>> 32));
    }

    private static int notificationId(long eventId) {
        return NOTIFY_BASE + (int) (eventId % 100000L);
    }
}
