// receives month widget updates.
package com.harilvfs.npltz;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;

public final class NpltzMonthWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(
        Context context,
        AppWidgetManager appWidgetManager,
        int[] appWidgetIds
    ) {
        if (appWidgetIds == null) {
            return;
        }
        for (int appWidgetId : appWidgetIds) {
            WidgetSupport.updateMonth(context, appWidgetManager, appWidgetId);
        }
        WidgetSupport.scheduleMidnight(context);
    }

    @Override
    public void onEnabled(Context context) {
        WidgetSupport.refreshMonth(context);
    }

    @Override
    public void onDisabled(Context context) {
        WidgetSupport.scheduleMidnight(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(
        Context context,
        AppWidgetManager appWidgetManager,
        int appWidgetId,
        Bundle newOptions
    ) {
        WidgetSupport.updateMonth(context, appWidgetManager, appWidgetId);
    }
}
