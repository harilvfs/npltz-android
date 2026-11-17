// schedules daily, permanent and special day notifications.
package com.harilvfs.npltz;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class NotificationScheduler {
    public static final String CHANNEL_ID = "daily_patro_v2";
    public static final String CHANNEL_ID_PERSISTENT = "persistent_date";
    public static final String CHANNEL_ID_SPECIAL = "special_days";
    public static final String ACTION_DAILY = "com.harilvfs.npltz.DAILY_REMINDER";
    public static final String ACTION_REFRESH = "com.harilvfs.npltz.REFRESH_DATE";
    public static final String ACTION_NEW_YEAR = "com.harilvfs.npltz.NEW_YEAR";
    public static final String ACTION_AD_NEW_YEAR = "com.harilvfs.npltz.AD_NEW_YEAR";
    public static final String ACTION_BIRTHDAY = "com.harilvfs.npltz.BIRTHDAY";

    private static final int ALARM_REQUEST = 7421;
    private static final int NOTIFY_ID = 20810;
    private static final int OPEN_REQUEST = 7422;
    private static final int PERSISTENT_ALARM_REQUEST = 7423;
    private static final int NEW_YEAR_REQUEST = 7431;
    private static final int BIRTHDAY_REQUEST = 7432;
    private static final int AD_NEW_YEAR_REQUEST = 7434;
    private static final int NEW_YEAR_NOTIFY_ID = 20812;
    private static final int BIRTHDAY_NOTIFY_ID = 20813;
    private static final int AD_NEW_YEAR_NOTIFY_ID = 20814;
    private static final int SPECIAL_HOUR = 9;
    static final int PERSISTENT_NOTIFY_ID = 20811;

    private static final DateTimeFormatter TRIGGER_FORMAT_12 =
        DateTimeFormatter.ofPattern("EEE, MMM d \u00b7 h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TRIGGER_FORMAT_24 =
        DateTimeFormatter.ofPattern("EEE, MMM d \u00b7 HH:mm", Locale.US);

    private NotificationScheduler() {
    }

    public static void createChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        // remove the old channel so its sound cannot overlap receiver playback.
        manager.deleteNotificationChannel("daily_patro");

        NotificationChannel daily = new NotificationChannel(
            CHANNEL_ID,
            "Npltz daily date",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        daily.setDescription("Today's Bikram Sambat and AD date, once a day.");

        // the receiver plays sound itself; keep the channel quiet.
        daily.setSound(null, null);
        daily.enableVibration(false);
        daily.setShowBadge(true);
        manager.createNotificationChannel(daily);

        NotificationChannel persistent = new NotificationChannel(
            CHANNEL_ID_PERSISTENT,
            "Npltz date",
            NotificationManager.IMPORTANCE_LOW
        );
        persistent.setDescription(
            "An ongoing notification with today's Bikram Sambat and AD date."
        );
        persistent.enableVibration(false);
        persistent.setShowBadge(false);
        persistent.setSound(null, null);
        manager.createNotificationChannel(persistent);

        NotificationChannel special = new NotificationChannel(
            CHANNEL_ID_SPECIAL,
            "Npltz celebrations",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        special.setDescription(
            "Nepali New Year, Gregorian New Year and developer birthday greetings."
        );
        special.setShowBadge(true);
        manager.createNotificationChannel(special);
    }

    private static PendingIntent alarmIntent(Context context) {
        Intent intent = new Intent(context, DailyReminderReceiver.class).setAction(ACTION_DAILY);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, ALARM_REQUEST, intent, flags);
    }

    private static PendingIntent refreshIntent(Context context) {
        Intent intent = new Intent(context, PersistentDateReceiver.class)
            .setAction(ACTION_REFRESH);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, PERSISTENT_ALARM_REQUEST, intent, flags);
    }

    private static PendingIntent openIntent(Context context) {
        Intent open = new Intent(context, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getActivity(context, OPEN_REQUEST, open, flags);
    }

    public static void scheduleDaily(Context context, int hour, int minute) {
        createChannel(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pending = alarmIntent(context);
        alarmManager.cancel(pending);
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTriggerMillis(hour, minute),
            pending
        );
    }

    public static void schedulePersistentRefresh(Context context) {
        createChannel(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pending = refreshIntent(context);
        alarmManager.cancel(pending);
        long trigger = nextMidnightMillis();
        // exact so roms like miui cannot defer the midnight refresh for a
        // backgrounded app; falls back when the user has not granted the
        // schedule-exact special access on android 14+.
        boolean exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            || alarmManager.canScheduleExactAlarms();
        if (exact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending);
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending);
        }
    }

    public static void scheduleSpecialDays(Context context) {
        createChannel(context);
        scheduleSpecialDay(
            context,
            CalendarLogic.NEW_YEAR_MONTH,
            CalendarLogic.NEW_YEAR_DAY,
            ACTION_NEW_YEAR,
            NEW_YEAR_REQUEST
        );
        scheduleSpecialDay(
            context,
            CalendarLogic.BIRTHDAY_MONTH,
            CalendarLogic.BIRTHDAY_DAY,
            ACTION_BIRTHDAY,
            BIRTHDAY_REQUEST
        );
        scheduleAdNewYear(context);
    }

    private static void scheduleAdNewYear(Context context) {
        LocalDate now = LocalDate.now();
        LocalDate next = LocalDate.of(now.getYear(), 1, 1);
        if (!next.isAfter(now)) {
            next = LocalDate.of(now.getYear() + 1, 1, 1);
        }
        ZonedDateTime trigger = next.atTime(SPECIAL_HOUR, 0).atZone(ZoneId.systemDefault());

        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pending = specialIntent(context, ACTION_AD_NEW_YEAR, AD_NEW_YEAR_REQUEST);
        alarmManager.cancel(pending);
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger.toInstant().toEpochMilli(),
            pending
        );
    }

    private static void scheduleSpecialDay(Context context, int month, int day,
                                           String action, int requestCode) {
        NepaliDate today = CalendarLogic.todayBs();
        if (today == null) {
            return;
        }
        int year = today.year;
        LocalDate ad = CalendarLogic.bsToAd(year, month, day);
        if (ad == null) {
            return;
        }
        ZonedDateTime trigger = ad.atTime(SPECIAL_HOUR, 0).atZone(ZoneId.systemDefault());
        if (!trigger.isAfter(ZonedDateTime.now())) {
            year++;
            if (year > CalendarLogic.BS_MAX_YEAR) {
                return;
            }
            ad = CalendarLogic.bsToAd(year, month, day);
            if (ad == null) {
                return;
            }
            trigger = ad.atTime(SPECIAL_HOUR, 0).atZone(ZoneId.systemDefault());
        }

        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pending = specialIntent(context, action, requestCode);
        alarmManager.cancel(pending);
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger.toInstant().toEpochMilli(),
            pending
        );
    }

    private static PendingIntent specialIntent(Context context, String action, int requestCode) {
        Intent intent = new Intent(context, SpecialDayReceiver.class).setAction(action);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent(context));
        }
    }

    public static void cancelPersistent(Context context) {
        DateNotificationService.stop(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(refreshIntent(context));
        }
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(PERSISTENT_NOTIFY_ID);
        }
    }

    public static long nextTriggerMillis(int hour, int minute) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = now
            .withHour(Math.max(0, Math.min(23, hour)))
            .withMinute(Math.max(0, Math.min(59, minute)))
            .withSecond(0)
            .withNano(0);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return next.toInstant().toEpochMilli();
    }

    private static long nextMidnightMillis() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = now.toLocalDate()
            .atStartOfDay(now.getZone())
            .plusMinutes(1);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return next.toInstant().toEpochMilli();
    }

    public static String nextTriggerLabel(Context context, int hour, int minute) {
        ZonedDateTime next = ZonedDateTime.now();
        ZonedDateTime candidate = next
            .withHour(Math.max(0, Math.min(23, hour)))
            .withMinute(Math.max(0, Math.min(59, minute)))
            .withSecond(0)
            .withNano(0);
        if (!candidate.isAfter(next)) {
            candidate = candidate.plusDays(1);
        }
        DateTimeFormatter formatter = new Prefs(context).isClock24Hour()
            ? TRIGGER_FORMAT_24
            : TRIGGER_FORMAT_12;
        return formatter.format(candidate);
    }

    public static String timeLabel(Context context, int hour, int minute) {
        LocalTime time = LocalTime.of(
            Math.max(0, Math.min(23, hour)),
            Math.max(0, Math.min(59, minute))
        );
        if (new Prefs(context).isClock24Hour()) {
            return time.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US));
        }
        return time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
    }

    public static boolean canNotify(Context context) {
        return canNotify(context, CHANNEL_ID);
    }

    private static boolean canNotify(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || !manager.areNotificationsEnabled()) {
            return false;
        }
        NotificationChannel channel = manager.getNotificationChannel(channelId);
        if (channel != null && channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
            return false;
        }
        return true;
    }

    private static final class DateText {
        String bsTitle;
        String weekday;
        String adLong;
        String adShort;
    }

    private static DateText dateText(Context context) {
        LocalDate now = LocalDate.now();
        NepaliDate bs = CalendarLogic.adToBs(now);
        if (bs == null) {
            return null;
        }
        boolean nepali = new Prefs(context).isNepali();
        DateText text = new DateText();
        text.bsTitle = CalendarLogic.monthName(bs.month, nepali) + " "
            + CalendarLogic.number(bs.year, nepali);
        text.weekday = CalendarLogic.dayName(bs.weekday, nepali);
        String adMonth = nepali
            ? CalendarLogic.adMonthName(now.getMonthValue(), true)
            : now.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH));
        text.adLong = adMonth + " "
            + CalendarLogic.number(now.getDayOfMonth(), nepali) + ", "
            + CalendarLogic.number(now.getYear(), nepali);
        text.adShort = CalendarLogic.adShort(now, nepali);
        return text;
    }

    private static void applyTextColors(Context context, RemoteViews views, boolean small) {
        int mode = context.getResources().getConfiguration().uiMode
            & Configuration.UI_MODE_NIGHT_MASK;
        boolean dark = mode == Configuration.UI_MODE_NIGHT_YES;
        int title = dark ? 0xFFF4F4F4 : 0xFF1B1B1F;
        int secondary = dark ? 0xFFB9B9BE : 0xFF5A5A60;
        views.setTextColor(R.id.notif_title, title);
        if (small) {
            views.setTextColor(R.id.notif_subtitle, secondary);
        } else {
            views.setTextColor(R.id.notif_weekday, secondary);
            views.setTextColor(R.id.notif_ad, secondary);
        }
    }

    private static Bitmap notificationTile(Context context) {
        LocalDate now = LocalDate.now();
        NepaliDate bs = CalendarLogic.adToBs(now);
        if (bs == null) {
            return null;
        }
        Prefs prefs = new Prefs(context);
        boolean nepali = prefs.isNepali();
        Theme theme = Theme.byKey(prefs.getTheme());

        int size = 192;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(theme.primary);
        canvas.drawRoundRect(new RectF(0, 0, size, size), 48, 48, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Theme.soft(theme.onPrimary, 0x28));
        canvas.drawRoundRect(new RectF(1, 1, size - 1, size - 1), 47, 47, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setColor(Theme.soft(theme.onPrimary, 0xC2));
        paint.setTextSize(24f);
        canvas.drawText(nepali ? "\u0935\u093f.\u0938\u0902." : "BS", size / 2f, 38f, paint);

        paint.setColor(theme.onPrimary);
        paint.setTextSize(104f);
        String day = CalendarLogic.number(bs.day, nepali);
        float width = paint.measureText(day);
        if (width > size - 44f) {
            paint.setTextSize(104f * (size - 44f) / width);
        }
        canvas.drawText(day, size / 2f, 150f, paint);
        return bitmap;
    }

    private static Notification build(Context context, boolean ongoing) {
        createChannel(context);
        DateText text = dateText(context);
        if (text == null) {
            return null;
        }
        Theme theme = Theme.byKey(new Prefs(context).getTheme());
        String pkg = context.getPackageName();

        RemoteViews small = new RemoteViews(pkg, R.layout.notification_date_small);
        small.setTextViewText(R.id.notif_title, text.bsTitle);
        small.setTextViewText(
            R.id.notif_subtitle,
            text.weekday + " \u00b7 " + text.adShort
        );
        applyTextColors(context, small, true);

        RemoteViews big = new RemoteViews(pkg, R.layout.notification_date_big);
        big.setTextViewText(R.id.notif_title, text.bsTitle);
        big.setTextViewText(R.id.notif_weekday, text.weekday);
        big.setTextViewText(R.id.notif_ad, text.adLong);
        applyTextColors(context, big, false);

        Bitmap tile = notificationTile(context);
        if (tile != null) {
            small.setImageViewBitmap(R.id.notif_tile, tile);
            big.setImageViewBitmap(R.id.notif_tile, tile);
        }

        Notification.Builder builder = new Notification.Builder(
            context,
            ongoing ? CHANNEL_ID_PERSISTENT : CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_stat_calendar)
            .setContentTitle("Npltz")
            .setContentText(text.bsTitle + " \u00b7 " + text.weekday)
            .setContentIntent(openIntent(context))
            .setOnlyAlertOnce(true)
            .setColor(theme.primary)
            .setCustomContentView(small)
            .setCustomBigContentView(big);

        if (ongoing) {
            builder.setOngoing(true)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setCategory(Notification.CATEGORY_STATUS);
        } else {
            builder.setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER);
        }

        return builder.build();
    }

    public static boolean showToday(Context context) {
        if (!canNotify(context)) {
            return false;
        }
        Notification notification = build(context, false);
        if (notification == null) {
            return false;
        }
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return false;
        }
        manager.notify(NOTIFY_ID, notification);
        return true;
    }

    public static boolean showSpecialDay(Context context, String action) {
        createChannel(context);
        if (!canNotify(context, CHANNEL_ID_SPECIAL)) {
            return false;
        }
        boolean newYear = ACTION_NEW_YEAR.equals(action);
        boolean adNewYear = ACTION_AD_NEW_YEAR.equals(action);
        boolean birthday = ACTION_BIRTHDAY.equals(action);
        if (!newYear && !adNewYear && !birthday) {
            return false;
        }

        Prefs prefs = new Prefs(context);
        boolean ne = prefs.isNepali();
        Theme theme = Theme.byKey(prefs.getTheme());
        LocalDate date = LocalDate.now();
        String adDate = CalendarLogic.adShort(date, ne);

        String title;
        String text;
        int iconRes;
        int accent;
        if (adNewYear) {
            iconRes = R.drawable.ic_sparkles;
            accent = theme.secondary;
            title = ne ? "नयाँ वर्षको शुभकामना!" : "Happy New Year!";
            text = ne
                ? adDate + "\u0964 \u0928\u092f\u093e\u0901 \u0935\u0930\u094d\u0937 "
                    + CalendarLogic.number(date.getYear(), true)
                    + " \u0915\u094b \u0939\u093e\u0930\u094d\u0926\u093f\u0915 "
                    + "\u0936\u0941\u092d\u0915\u093e\u092e\u0928\u093e\u0964"
                : adDate + ". A brand new year begins, happy "
                    + date.getYear() + "!";
        } else if (newYear) {
            iconRes = R.drawable.ic_star;
            accent = theme.primary;
            title = ne ? "\u0928\u092f\u093e\u0901 \u0935\u0930\u094d\u0937" : "Nepali New Year";
            String bsDate = CalendarLogic.monthName(CalendarLogic.NEW_YEAR_MONTH, ne)
                + " " + CalendarLogic.number(CalendarLogic.NEW_YEAR_DAY, ne);
            text = ne
                ? bsDate + " \u00b7 " + adDate + "\u0964 \u0936\u0941\u092d \u0928\u092f\u093e\u0901 \u0935\u0930\u094d\u0937!"
                : bsDate + " \u00b7 " + adDate + ". Happy Nepali New Year!";
        } else {
            iconRes = R.drawable.ic_cake;
            accent = theme.primary;
            title = ne
                ? "\u0939\u0930\u093f, \u091c\u0928\u094d\u092e\u0926\u093f\u0928\u0915\u094b \u0936\u0941\u092d\u0915\u093e\u092e\u0928\u093e!"
                : "Happy birthday, Hari!";
            String bsDate = CalendarLogic.monthName(CalendarLogic.BIRTHDAY_MONTH, ne)
                + " " + CalendarLogic.number(CalendarLogic.BIRTHDAY_DAY, ne);
            text = ne
                ? bsDate + " \u00b7 " + adDate
                    + "\u0964 \u0939\u0930\u093f \u091a\u0932\u093f\u0938\u0947\u0932\u093e\u0908 "
                    + "\u091c\u0928\u094d\u092e\u0926\u093f\u0928\u0915\u094b \u0936\u0941\u092d\u0915\u093e\u092e\u0928\u093e\u0964"
                : bsDate + " \u00b7 " + adDate
                    + ". Happy birthday to Hari Chalise, the developer of Npltz.";
        }

        Notification notification = new Notification.Builder(context, CHANNEL_ID_SPECIAL)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new Notification.BigTextStyle().bigText(text))
            .setContentIntent(openIntent(context))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setColor(accent)
            .setCategory(Notification.CATEGORY_REMINDER)
            .build();

        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return false;
        }
        manager.notify(
            newYear ? NEW_YEAR_NOTIFY_ID
                : adNewYear ? AD_NEW_YEAR_NOTIFY_ID : BIRTHDAY_NOTIFY_ID,
            notification
        );
        return true;
    }

    public static void refresh(Context context) {
        Prefs prefs = new Prefs(context);

        if (prefs.isPersistentNotifyEnabled()) {
            // force a rebuild so theme and language changes repaint the
            // notification even though the date has not changed.
            showPersistent(context, true);
        }
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || !prefs.isNotifyEnabled() || !canNotify(context, CHANNEL_ID)) {
            return;
        }
        boolean dailyVisible = false;
        for (StatusBarNotification notification : manager.getActiveNotifications()) {
            if (notification.getId() == NOTIFY_ID) {
                dailyVisible = true;
                break;
            }
        }
        if (dailyVisible) {
            showToday(context);
        }
    }

    // a foreground service keeps the date in the shade after the task is swiped away.
    public static boolean showPersistent(Context context) {
        return showPersistent(context, false);
    }

    private static boolean showPersistent(Context context, boolean force) {
        createChannel(context);
        if (!canNotify(context, CHANNEL_ID_PERSISTENT)) {
            return false;
        }
        return force
            ? DateNotificationService.restart(context)
            : DateNotificationService.start(context);
    }

    static Notification buildPersistentNotification(Context context) {
        createChannel(context);
        if (!canNotify(context, CHANNEL_ID_PERSISTENT)) {
            return null;
        }
        return build(context, true);
    }

    static boolean postPersistentNotification(Context context) {
        Notification notification = buildPersistentNotification(context);
        if (notification == null) {
            return false;
        }
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return false;
        }
        manager.notify(PERSISTENT_NOTIFY_ID, notification);
        return true;
    }
}
