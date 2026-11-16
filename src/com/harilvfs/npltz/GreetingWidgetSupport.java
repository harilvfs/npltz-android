// builds and refreshes the greeting widget.
package com.harilvfs.npltz;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.res.ResourcesCompat;


import java.util.Calendar;
import java.util.TimeZone;

final class GreetingWidgetSupport {
    static final String ACTION_TICK = "com.harilvfs.npltz.GREETING_WIDGET_TICK";

    private static final TimeZone NEPAL = TimeZone.getTimeZone("Asia/Kathmandu");
    private static final int TICK_REQUEST = 7440;
    private static final int OPEN_REQUEST = 7441;

    private GreetingWidgetSupport() {
    }

    static void update(Context context, AppWidgetManager manager, int appWidgetId) {
        update(context, manager, appWidgetId, null);
    }

    static void update(
        Context context,
        AppWidgetManager manager,
        int appWidgetId,
        Bundle newOptions
    ) {
        Prefs prefs = new Prefs(context);
        boolean nepali = prefs.isNepali();
        Theme theme = Theme.byKey(prefs.getTheme());

        RemoteViews views = new RemoteViews(
            context.getPackageName(),
            R.layout.widget_npltz_greeting
        );
        views.setInt(
            R.id.widget_greeting_background,
            "setColorFilter",
            theme.surfaceContainer
        );
        views.setOnClickPendingIntent(
            R.id.widget_greeting_root,
            openIntent(context)
        );

        int[] size = widgetSize(context, manager, appWidgetId, newOptions);
        int width = size[0];
        int height = size[1];
        boolean narrow = width < 210;
        boolean compact = height < 88;
        boolean showWish = height >= 80;

        float baseTitle = compact
            ? (narrow ? 13f : 14f)
            : (narrow ? 16f : 18f);
        if (height >= 145 && !narrow) {
            baseTitle += 1f;
        }
        float wishSize = narrow ? 9f : 10f;

        int padH = compact ? 10 : 14;
        int padV = compact ? 8 : 12;
        views.setViewPadding(
            R.id.widget_greeting_content,
            dp(context, padH),
            dp(context, padV),
            dp(context, padH),
            dp(context, padV)
        );
        views.setTextViewTextSize(
            R.id.widget_greeting_wish,
            TypedValue.COMPLEX_UNIT_SP,
            wishSize
        );
        views.setViewVisibility(
            R.id.widget_greeting_wish,
            showWish ? View.VISIBLE : View.GONE
        );

        if (prefs.isGreetingEnabled()) {
            Greeting greeting = greeting(context, prefs, nepali, theme);
            String titleText = greeting.prefix + greeting.name;
            int textWidthDp = Math.max(
                72,
                (int) (width * 0.8f) - padH * 2 - 38
            );
            views.setTextViewTextSize(
                R.id.widget_greeting_title,
                TypedValue.COMPLEX_UNIT_SP,
                fitTitleSize(context, titleText, baseTitle, textWidthDp)
            );
            SpannableStringBuilder title = new SpannableStringBuilder(titleText);
            title.setSpan(
                new ForegroundColorSpan(greeting.accent),
                greeting.prefix.length(),
                title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            views.setTextViewText(R.id.widget_greeting_title, title);
            views.setTextColor(R.id.widget_greeting_title, theme.onSurface);
            views.setTextViewText(R.id.widget_greeting_wish, greeting.wish);
            views.setTextColor(R.id.widget_greeting_wish, theme.onSurfaceVariant);
            views.setImageViewResource(
                R.id.widget_greeting_icon,
                greeting.iconRes
            );
            views.setInt(
                R.id.widget_greeting_icon,
                "setColorFilter",
                greeting.accent
            );
            views.setViewVisibility(R.id.widget_greeting_icon, View.VISIBLE);
        } else {
            views.setTextViewText(R.id.widget_greeting_title, "Npltz");
            views.setTextColor(R.id.widget_greeting_title, theme.primary);
            views.setViewVisibility(R.id.widget_greeting_wish, View.GONE);
            views.setViewVisibility(R.id.widget_greeting_icon, View.GONE);
        }

        manager.updateAppWidget(appWidgetId, views);
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (manager == null) {
            return;
        }
        ComponentName provider = new ComponentName(
            context,
            GreetingWidgetProvider.class
        );
        int[] ids = manager.getAppWidgetIds(provider);
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            update(context, manager, id);
        }
    }

    static void refreshAll(Context context) {
        updateAll(context);
        scheduleTick(context);
    }

    static void scheduleTick(Context context) {
        AlarmManager alarms =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms == null) {
            return;
        }
        alarms.cancel(tickIntent(context));
        if (!hasWidgets(context)) {
            return;
        }
        long now = System.currentTimeMillis();
        // no clock in the widget, so the greeting only needs to repaint on
        // the hour without waking the device.
        long next = now - now % 3600000L + 3600000L + 500L;
        alarms.setAndAllowWhileIdle(AlarmManager.RTC, next, tickIntent(context));
    }

    private static boolean hasWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        if (manager == null) {
            return false;
        }
        int[] ids = manager.getAppWidgetIds(
            new ComponentName(context, GreetingWidgetProvider.class)
        );
        return ids != null && ids.length > 0;
    }

    private static float fitTitleSize(
        Context context,
        String text,
        float preferredSp,
        int availableDp
    ) {
        if (text == null || text.isEmpty() || availableDp <= 0) {
            return preferredSp;
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.inter_medium);
        paint.setTypeface(typeface != null
            ? typeface
            : Typeface.create("sans-serif-medium", Typeface.NORMAL));
        paint.setTextSize(preferredSp * context.getResources()
            .getDisplayMetrics().scaledDensity);
        float measured = paint.measureText(text);
        float available = availableDp * context.getResources()
            .getDisplayMetrics().density;
        if (measured <= available || measured <= 0f) {
            return preferredSp;
        }
        float fitted = preferredSp * available / measured;
        fitted = Math.max(9f, Math.min(preferredSp, fitted));
        return Math.round(fitted * 2f) / 2f;
    }

    private static Greeting greeting(
        Context context,
        Prefs prefs,
        boolean nepali,
        Theme theme
    ) {
        int hour = Calendar.getInstance(NEPAL).get(Calendar.HOUR_OF_DAY);
        String prefix;
        String wish;
        int accent;
        int iconRes;
        if (hour >= 5 && hour < 12) {
            prefix = nepali
                ? "\u0936\u0941\u092d \u092a\u094d\u0930\u092d\u093e\u0924, "
                : "Good morning, ";
            wish = nepali
                ? "\u0924\u092a\u093e\u0908\u0902\u0915\u094b \u0926\u093f\u0928 \u0936\u0941\u092d \u0939\u094b\u0938\u094d\u0964"
                : "Wishing you a happy day.";
            accent = theme.warning;
            iconRes = R.drawable.ic_sun;
        } else if (hour >= 12 && hour < 17) {
            prefix = nepali
                ? "\u0936\u0941\u092d \u0926\u093f\u0909\u0901\u0938\u094b, "
                : "Good afternoon, ";
            wish = nepali
                ? "\u0924\u092a\u093e\u0908\u0902\u0915\u094b \u0926\u093f\u0928 \u0930\u093e\u092e\u094d\u0930\u094b \u0939\u094b\u0938\u094d\u0964"
                : "Hope your day is going well.";
            accent = theme.primary;
            iconRes = R.drawable.ic_sun;
        } else if (hour >= 17 && hour < 21) {
            prefix = nepali
                ? "\u0936\u0941\u092d \u0938\u093e\u0901\u091d, "
                : "Good evening, ";
            wish = nepali
                ? "\u0905\u092c \u0906\u0930\u093e\u092e \u0917\u0930\u094d\u0928\u0941\u0939\u094b\u0938\u094d\u0964"
                : "Time to relax and unwind.";
            accent = theme.secondary;
            iconRes = R.drawable.ic_sunset;
        } else {
            prefix = nepali
                ? "\u0936\u0941\u092d \u0930\u093e\u0924\u094d\u0930\u0940, "
                : "Good night, ";
            wish = nepali
                ? "\u092e\u0940\u0920\u094b \u0938\u092a\u0928\u093e \u0926\u0947\u0916\u094d\u0928\u0941\u0939\u094b\u0938\u094d\u0964"
                : "Have a sweet dream.";
            accent = theme.primary;
            iconRes = R.drawable.widget_greeting_moon;
        }
        return new Greeting(
            prefix,
            displayName(prefs.getGreetingName(context)),
            wish,
            accent,
            iconRes
        );
    }

    private static String displayName(String raw) {
        if (raw == null) {
            return "user";
        }
        String clean = raw.trim();
        if (clean.isEmpty() || clean.equalsIgnoreCase("user")) {
            return "user";
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    private static int[] widgetSize(
        Context context,
        AppWidgetManager manager,
        int appWidgetId,
        Bundle newOptions
    ) {
        int width = 250;
        int height = 110;
        Bundle options = newOptions != null
            ? newOptions
            : manager.getAppWidgetOptions(appWidgetId);
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

    private static PendingIntent tickIntent(Context context) {
        Intent tick = new Intent(context, GreetingWidgetProvider.class)
            .setAction(ACTION_TICK);
        return PendingIntent.getBroadcast(
            context,
            TICK_REQUEST,
            tick,
            pendingFlags()
        );
    }

    private static int pendingFlags() {
        return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    }

    private static int dp(Context context, float value) {
        return Math.round(
            value * context.getResources().getDisplayMetrics().density
        );
    }

    private static final class Greeting {
        final String prefix;
        final String name;
        final String wish;
        final int accent;
        final int iconRes;

        Greeting(
            String prefix,
            String name,
            String wish,
            int accent,
            int iconRes
        ) {
            this.prefix = prefix;
            this.name = name;
            this.wish = wish;
            this.accent = accent;
            this.iconRes = iconRes;
        }
    }
}
