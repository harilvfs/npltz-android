// builds and refreshes the date and month widgets.
package com.harilvfs.npltz;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class WidgetSupport {
    static final String ACTION_REFRESH = "com.harilvfs.npltz.WIDGET_REFRESH";

    private static final int ALARM_REQUEST = 7430;
    private static final int OPEN_REQUEST = 7431;
    private static final int MONTH_ROWS = 6;

    private static final int SIMPLE_ACCENT_HEIGHT_DP = 135;

    private static final int[] MONTH_WEEKDAY_IDS = {
        R.id.widget_month_wd_0,
        R.id.widget_month_wd_1,
        R.id.widget_month_wd_2,
        R.id.widget_month_wd_3,
        R.id.widget_month_wd_4,
        R.id.widget_month_wd_5,
        R.id.widget_month_wd_6,
    };

    private static final int[][] MONTH_BS_IDS = {
        {
            R.id.widget_month_bs_0_0, R.id.widget_month_bs_0_1,
            R.id.widget_month_bs_0_2, R.id.widget_month_bs_0_3,
            R.id.widget_month_bs_0_4, R.id.widget_month_bs_0_5,
            R.id.widget_month_bs_0_6,
        },
        {
            R.id.widget_month_bs_1_0, R.id.widget_month_bs_1_1,
            R.id.widget_month_bs_1_2, R.id.widget_month_bs_1_3,
            R.id.widget_month_bs_1_4, R.id.widget_month_bs_1_5,
            R.id.widget_month_bs_1_6,
        },
        {
            R.id.widget_month_bs_2_0, R.id.widget_month_bs_2_1,
            R.id.widget_month_bs_2_2, R.id.widget_month_bs_2_3,
            R.id.widget_month_bs_2_4, R.id.widget_month_bs_2_5,
            R.id.widget_month_bs_2_6,
        },
        {
            R.id.widget_month_bs_3_0, R.id.widget_month_bs_3_1,
            R.id.widget_month_bs_3_2, R.id.widget_month_bs_3_3,
            R.id.widget_month_bs_3_4, R.id.widget_month_bs_3_5,
            R.id.widget_month_bs_3_6,
        },
        {
            R.id.widget_month_bs_4_0, R.id.widget_month_bs_4_1,
            R.id.widget_month_bs_4_2, R.id.widget_month_bs_4_3,
            R.id.widget_month_bs_4_4, R.id.widget_month_bs_4_5,
            R.id.widget_month_bs_4_6,
        },
        {
            R.id.widget_month_bs_5_0, R.id.widget_month_bs_5_1,
            R.id.widget_month_bs_5_2, R.id.widget_month_bs_5_3,
            R.id.widget_month_bs_5_4, R.id.widget_month_bs_5_5,
            R.id.widget_month_bs_5_6,
        },
    };

    private static final int[][] MONTH_AD_IDS = {
        {
            R.id.widget_month_ad_0_0, R.id.widget_month_ad_0_1,
            R.id.widget_month_ad_0_2, R.id.widget_month_ad_0_3,
            R.id.widget_month_ad_0_4, R.id.widget_month_ad_0_5,
            R.id.widget_month_ad_0_6,
        },
        {
            R.id.widget_month_ad_1_0, R.id.widget_month_ad_1_1,
            R.id.widget_month_ad_1_2, R.id.widget_month_ad_1_3,
            R.id.widget_month_ad_1_4, R.id.widget_month_ad_1_5,
            R.id.widget_month_ad_1_6,
        },
        {
            R.id.widget_month_ad_2_0, R.id.widget_month_ad_2_1,
            R.id.widget_month_ad_2_2, R.id.widget_month_ad_2_3,
            R.id.widget_month_ad_2_4, R.id.widget_month_ad_2_5,
            R.id.widget_month_ad_2_6,
        },
        {
            R.id.widget_month_ad_3_0, R.id.widget_month_ad_3_1,
            R.id.widget_month_ad_3_2, R.id.widget_month_ad_3_3,
            R.id.widget_month_ad_3_4, R.id.widget_month_ad_3_5,
            R.id.widget_month_ad_3_6,
        },
        {
            R.id.widget_month_ad_4_0, R.id.widget_month_ad_4_1,
            R.id.widget_month_ad_4_2, R.id.widget_month_ad_4_3,
            R.id.widget_month_ad_4_4, R.id.widget_month_ad_4_5,
            R.id.widget_month_ad_4_6,
        },
        {
            R.id.widget_month_ad_5_0, R.id.widget_month_ad_5_1,
            R.id.widget_month_ad_5_2, R.id.widget_month_ad_5_3,
            R.id.widget_month_ad_5_4, R.id.widget_month_ad_5_5,
            R.id.widget_month_ad_5_6,
        },
    };

    private static final int[][] MONTH_CIRCLE_IDS = {
        {
            R.id.widget_month_c_0_0, R.id.widget_month_c_0_1,
            R.id.widget_month_c_0_2, R.id.widget_month_c_0_3,
            R.id.widget_month_c_0_4, R.id.widget_month_c_0_5,
            R.id.widget_month_c_0_6,
        },
        {
            R.id.widget_month_c_1_0, R.id.widget_month_c_1_1,
            R.id.widget_month_c_1_2, R.id.widget_month_c_1_3,
            R.id.widget_month_c_1_4, R.id.widget_month_c_1_5,
            R.id.widget_month_c_1_6,
        },
        {
            R.id.widget_month_c_2_0, R.id.widget_month_c_2_1,
            R.id.widget_month_c_2_2, R.id.widget_month_c_2_3,
            R.id.widget_month_c_2_4, R.id.widget_month_c_2_5,
            R.id.widget_month_c_2_6,
        },
        {
            R.id.widget_month_c_3_0, R.id.widget_month_c_3_1,
            R.id.widget_month_c_3_2, R.id.widget_month_c_3_3,
            R.id.widget_month_c_3_4, R.id.widget_month_c_3_5,
            R.id.widget_month_c_3_6,
        },
        {
            R.id.widget_month_c_4_0, R.id.widget_month_c_4_1,
            R.id.widget_month_c_4_2, R.id.widget_month_c_4_3,
            R.id.widget_month_c_4_4, R.id.widget_month_c_4_5,
            R.id.widget_month_c_4_6,
        },
        {
            R.id.widget_month_c_5_0, R.id.widget_month_c_5_1,
            R.id.widget_month_c_5_2, R.id.widget_month_c_5_3,
            R.id.widget_month_c_5_4, R.id.widget_month_c_5_5,
            R.id.widget_month_c_5_6,
        },
    };

    private WidgetSupport() {
    }

    static void updateSimple(
        Context context,
        AppWidgetManager manager,
        int appWidgetId
    ) {
        updateSimple(context, manager, appWidgetId, null);
    }

    static void updateSimple(
        Context context,
        AppWidgetManager manager,
        int appWidgetId,
        Bundle options
    ) {
        Prefs prefs = new Prefs(context);
        boolean nepali = prefs.isNepali();
        Theme theme = Theme.byKey(prefs.getTheme());
        LocalDate today = LocalDate.now();
        NepaliDate bs = CalendarLogic.adToBs(today);

        SimpleLayout layout = simpleLayout(
            context,
            manager,
            appWidgetId,
            options
        );
        RemoteViews views = base(context, theme, R.layout.widget_npltz);
        applySimpleMetrics(context, views, layout);
        views.setTextColor(R.id.widget_brand, theme.primary);
        views.setTextColor(R.id.widget_bs_label, theme.onSurfaceVariant);
        views.setTextColor(
            R.id.widget_weekday,
            bs == null
                ? theme.onSurfaceVariant
                : bs.weekday == 0
                    ? theme.yellow
                    : bs.weekday == 6 ? theme.error : theme.onSurfaceVariant
        );
        views.setTextColor(R.id.widget_month, theme.onSurface);
        views.setTextColor(R.id.widget_day, theme.primary);
        views.setTextColor(R.id.widget_ad, theme.onSurfaceVariant);

        views.setTextViewText(R.id.widget_brand, "Npltz");
        views.setTextViewText(R.id.widget_bs_label, nepali ? "वि.सं." : "BS");

        if (bs != null) {
            views.setTextViewText(R.id.widget_day, CalendarLogic.number(bs.day, nepali));
            views.setTextViewText(
                R.id.widget_weekday,
                CalendarLogic.dayName(bs.weekday, nepali)
            );
            String month = CalendarLogic.monthName(bs.month, nepali);
            if (layout.showYear) {
                month += " " + CalendarLogic.number(bs.year, nepali);
            }
            views.setTextViewText(R.id.widget_month, month);
            views.setTextViewText(
                R.id.widget_ad,
                adLine(
                    today,
                    nepali,
                    layout.showAdYear,
                    layout.showAdWeekday
                )
            );
        } else {
            views.setTextViewText(R.id.widget_day, "--");
            views.setTextViewText(R.id.widget_weekday, "");
            views.setTextViewText(R.id.widget_month, "");
            views.setTextViewText(R.id.widget_ad, "");
        }

        manager.updateAppWidget(appWidgetId, views);
    }

    private static SimpleLayout simpleLayout(
        Context context,
        AppWidgetManager manager,
        int appWidgetId,
        Bundle newOptions
    ) {
        int[] size = widgetSizeDp(
            context,
            manager,
            appWidgetId,
            newOptions,
            250,
            120
        );
        int width = size[0];
        int height = size[1];

        // short cells shrink type and drop the accent before hiding whole lines.
        float heightScale = Math.max(0.62f, Math.min(1f, height / 130f));
        float scale = width / 250f * heightScale;
        scale = Math.max(0.55f, Math.min(1.30f, scale));

        SimpleLayout layout = new SimpleLayout();
        layout.scale = scale;

        layout.showYear = width >= 180;
        layout.showMonth = true;
        layout.showWeekday = height >= 62;
        layout.showHeader = height >= 62;
        layout.showLogo = height >= 62 && width >= 170;
        layout.compactLogo = height < 118;
        layout.showAccent = height >= SIMPLE_ACCENT_HEIGHT_DP;

        layout.showAd = height >= 60;
        layout.showAdYear = width >= 170;
        layout.showAdWeekday = width >= 150;

        float verticalDp = Math.max(4f, Math.min(14f, height * 0.07f));
        float density = context.getResources().getDisplayMetrics().density;
        layout.horizontalPadding =
            Math.round(18f * scale * density);
        layout.verticalPadding = Math.round(verticalDp * density);

        float day = 40f * scale;
        if (height < 130f) {
            day = Math.min(day, Math.max(18f, height * 0.32f));
        }
        layout.daySize = day;
        return layout;
    }

    private static int[] widgetSizeDp(
        Context context,
        AppWidgetManager manager,
        int appWidgetId,
        Bundle newOptions,
        int defaultWidth,
        int defaultHeight
    ) {
        int width = defaultWidth;
        int height = defaultHeight;
        Bundle options = newOptions;
        if (options == null) {
            options = manager.getAppWidgetOptions(appWidgetId);
        }
        if (options == null) {
            return new int[] {width, height};
        }

        boolean landscape = context.getResources().getConfiguration()
            .orientation == Configuration.ORIENTATION_LANDSCAPE;
        int optionWidth = options.getInt(
            landscape
                ? AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH
                : AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
            0
        );
        int optionHeight = options.getInt(
            landscape
                ? AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT
                : AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
            0
        );
        if (optionWidth <= 0) {
            optionWidth = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                0
            );
        }
        if (optionHeight <= 0) {
            optionHeight = options.getInt(
                AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                0
            );
        }
        if (optionWidth > 0) {
            width = optionWidth;
        }
        if (optionHeight > 0) {
            height = optionHeight;
        }
        return new int[] {width, height};
    }

    private static void applySimpleMetrics(
        Context context,
        RemoteViews views,
        SimpleLayout layout
    ) {
        views.setTextViewTextSize(
            R.id.widget_brand,
            TypedValue.COMPLEX_UNIT_SP,
            13f * layout.scale
        );
        views.setTextViewTextSize(
            R.id.widget_bs_label,
            TypedValue.COMPLEX_UNIT_SP,
            10f * layout.scale
        );
        views.setTextViewTextSize(
            R.id.widget_day,
            TypedValue.COMPLEX_UNIT_SP,
            layout.daySize
        );
        views.setTextViewTextSize(
            R.id.widget_weekday,
            TypedValue.COMPLEX_UNIT_SP,
            11f * layout.scale
        );
        views.setTextViewTextSize(
            R.id.widget_month,
            TypedValue.COMPLEX_UNIT_SP,
            16f * layout.scale
        );
        views.setTextViewTextSize(
            R.id.widget_ad,
            TypedValue.COMPLEX_UNIT_SP,
            11f * layout.scale
        );

        views.setViewVisibility(
            R.id.widget_header,
            layout.showHeader ? View.VISIBLE : View.GONE
        );
        views.setViewVisibility(
            R.id.widget_accent_line,
            layout.showAccent ? View.VISIBLE : View.GONE
        );
        views.setViewVisibility(
            R.id.widget_weekday,
            layout.showWeekday ? View.VISIBLE : View.GONE
        );
        views.setViewVisibility(
            R.id.widget_month,
            layout.showMonth ? View.VISIBLE : View.GONE
        );
        views.setViewVisibility(
            R.id.widget_ad,
            layout.showAd ? View.VISIBLE : View.GONE
        );
        views.setViewVisibility(
            R.id.widget_logo,
            layout.showLogo && !layout.compactLogo
                ? View.VISIBLE
                : View.GONE
        );
        views.setViewVisibility(
            R.id.widget_logo_compact,
            layout.showLogo && layout.compactLogo
                ? View.VISIBLE
                : View.GONE
        );

        views.setViewPadding(
            R.id.widget_content,
            layout.horizontalPadding,
            layout.verticalPadding,
            layout.horizontalPadding,
            layout.verticalPadding
        );
    }

    private static final class SimpleLayout {
        float scale;
        float daySize;
        int horizontalPadding;
        int verticalPadding;
        boolean showYear;
        boolean showMonth;
        boolean showWeekday;
        boolean showHeader;
        boolean showLogo;
        boolean compactLogo;
        boolean showAccent;
        boolean showAd;
        boolean showAdYear;
        boolean showAdWeekday;
    }

    static void updateMonth(
        Context context,
        AppWidgetManager manager,
        int appWidgetId
    ) {
        Prefs prefs = new Prefs(context);
        boolean nepali = prefs.isNepali();
        Theme theme = Theme.byKey(prefs.getTheme());
        LocalDate today = LocalDate.now();
        NepaliDate bs = CalendarLogic.adToBs(today);

        int year = bs != null ? bs.year : CalendarLogic.BS_EPOCH_YEAR;
        int month = bs != null ? bs.month : 1;
        int todayDay = bs != null ? bs.day : -1;
        int start = CalendarLogic.monthStartWeekday(year, month);
        int days = CalendarLogic.daysInMonth(year, month);

        RemoteViews views = base(context, theme, R.layout.widget_npltz_month);
        views.setTextColor(R.id.widget_month_brand, theme.primary);
        views.setTextColor(R.id.widget_month_title, theme.onSurface);
        views.setTextColor(R.id.widget_month_bs_label, theme.onSurfaceVariant);
        views.setTextColor(R.id.widget_month_ad, theme.onSurfaceVariant);
        views.setTextViewText(R.id.widget_month_brand, "Npltz");
        views.setTextViewText(
            R.id.widget_month_title,
            bs == null
                ? "--"
                : CalendarLogic.monthName(month, nepali) + " "
                    + CalendarLogic.number(year, nepali)
        );
        views.setTextViewText(
            R.id.widget_month_bs_label,
            nepali ? "वि.सं." : "BS"
        );
        views.setTextViewText(R.id.widget_month_ad, monthYear(today, nepali));

        for (int column = 0; column < 7; column++) {
            int id = MONTH_WEEKDAY_IDS[column];
            views.setTextViewText(
                id,
                CalendarLogic.weekdayShort(column, nepali)
            );
            views.setTextColor(
                id,
                column == 6
                    ? theme.error
                    : column == 0 ? theme.yellow : theme.onSurfaceVariant
            );
        }

        for (int row = 0; row < MONTH_ROWS; row++) {
            for (int column = 0; column < 7; column++) {
                int day = row * 7 + column - start + 1;
                int bsId = MONTH_BS_IDS[row][column];
                int adId = MONTH_AD_IDS[row][column];
                int circleId = MONTH_CIRCLE_IDS[row][column];
                if (day < 1 || day > days) {
                    views.setTextViewText(bsId, "");
                    views.setTextViewText(adId, "");
                    views.setViewVisibility(circleId, View.GONE);
                    continue;
                }

                boolean isToday = day == todayDay;
                LocalDate ad = CalendarLogic.bsToAd(year, month, day);
                String bsText = CalendarLogic.number(day, nepali);
                String adText = ad == null
                    ? ""
                    : CalendarLogic.number(ad.getDayOfMonth(), nepali);

                int dayColor;
                int adColor;
                if (isToday) {
                    dayColor = theme.onPrimary;
                    adColor = theme.primary;
                } else if (column == 6) {
                    dayColor = theme.error;
                    adColor = Theme.soft(theme.error, 0xB0);
                } else if (column == 0) {
                    dayColor = theme.yellow;
                    adColor = Theme.soft(theme.yellow, 0xB0);
                } else {
                    dayColor = theme.onSurface;
                    adColor = theme.onSurfaceVariant;
                }

                views.setTextViewText(bsId, bsText);
                views.setTextViewText(adId, adText);
                views.setTextColor(bsId, dayColor);
                views.setTextColor(adId, adColor);
                views.setViewVisibility(
                    circleId,
                    isToday ? View.VISIBLE : View.GONE
                );
                if (isToday) {
                    views.setInt(circleId, "setColorFilter", theme.primary);
                }
            }
        }

        manager.updateAppWidget(appWidgetId, views);
    }

    static void updateAll(Context context) {
        updateAll(context, NpltzWidgetProvider.class, false);
        updateAll(context, NpltzMonthWidgetProvider.class, true);
    }

    private static void updateAll(
        Context context,
        Class<? extends AppWidgetProvider> providerClass,
        boolean month
    ) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (manager == null) {
            return;
        }
        ComponentName provider = new ComponentName(context, providerClass);
        int[] ids = manager.getAppWidgetIds(provider);
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            if (month) {
                updateMonth(context, manager, id);
            } else {
                updateSimple(context, manager, id);
            }
        }
    }

    static void refreshAll(Context context) {
        updateAll(context);
        GreetingWidgetSupport.refreshAll(context);
        scheduleMidnight(context);
    }

    static void refreshSimple(Context context) {
        updateAll(context, NpltzWidgetProvider.class, false);
        scheduleMidnight(context);
    }

    static void refreshMonth(Context context) {
        updateAll(context, NpltzMonthWidgetProvider.class, true);
        scheduleMidnight(context);
    }

    static boolean hasWidgets(
        Context context,
        Class<? extends AppWidgetProvider> providerClass
    ) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (manager == null) {
            return false;
        }
        int[] ids = manager.getAppWidgetIds(
            new ComponentName(context, providerClass)
        );
        return ids != null && ids.length > 0;
    }

    static void scheduleMidnight(Context context) {
        cancelMidnight(context);
        if (!hasWidgets(context, NpltzWidgetProvider.class)
            && !hasWidgets(context, NpltzMonthWidgetProvider.class)) {
            return;
        }
        AlarmManager alarms =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                nextMidnightMillis(), refreshIntent(context));
        } else {
            alarms.setExact(AlarmManager.RTC_WAKEUP,
                nextMidnightMillis(), refreshIntent(context));
        }
    }

    static void cancelMidnight(Context context) {
        AlarmManager alarms =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) {
            alarms.cancel(refreshIntent(context));
        }
    }

    private static RemoteViews base(Context context, Theme theme, int layout) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layout);
        views.setInt(
            R.id.widget_background,
            "setColorFilter",
            theme.surfaceContainer
        );
        views.setInt(R.id.widget_accent_line, "setBackgroundColor", theme.primary);
        views.setOnClickPendingIntent(R.id.widget_root, openIntent(context));
        return views;
    }

    private static String adLine(
        LocalDate date,
        boolean nepali,
        boolean showYear,
        boolean showWeekday
    ) {
        String month = nepali
            ? CalendarLogic.adMonthName(date.getMonthValue(), true)
            : date.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH));
        StringBuilder line = new StringBuilder();
        if (showWeekday) {
            line.append(
                nepali
                    ? CalendarLogic.dayName(CalendarLogic.weekday(date), true)
                    : CalendarLogic.adDayName(date)
            ).append(", ");
        }
        line.append(month).append(" ")
            .append(CalendarLogic.number(date.getDayOfMonth(), nepali));
        if (showYear) {
            line.append(", ")
                .append(CalendarLogic.number(date.getYear(), nepali));
        }
        return line.toString();
    }

    private static String monthYear(LocalDate date, boolean nepali) {
        String month = nepali
            ? CalendarLogic.adMonthName(date.getMonthValue(), true)
            : date.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH));
        return month + " " + CalendarLogic.number(date.getYear(), nepali);
    }

    private static PendingIntent openIntent(Context context) {
        Intent open = new Intent(context, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
            context,
            OPEN_REQUEST,
            open,
            pendingFlags()
        );
    }

    private static PendingIntent refreshIntent(Context context) {
        Intent refresh = new Intent(context, NpltzWidgetProvider.class)
            .setAction(ACTION_REFRESH);
        return PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST,
            refresh,
            pendingFlags()
        );
    }

    private static int pendingFlags() {
        return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
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

}
