// receives date widget updates.
package com.harilvfs.npltz;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public final class NpltzWidgetProvider extends AppWidgetProvider {
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
            WidgetSupport.updateSimple(context, appWidgetManager, appWidgetId);
        }
        WidgetSupport.scheduleMidnight(context);
    }

    @Override
    public void onEnabled(Context context) {
        WidgetSupport.refreshSimple(context);
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
        WidgetSupport.updateSimple(
            context,
            appWidgetManager,
            appWidgetId,
            newOptions
        );
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent != null && WidgetSupport.ACTION_REFRESH.equals(intent.getAction())) {
            WidgetSupport.refreshAll(context);
        }
    }
}
