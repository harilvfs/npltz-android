// receives greeting widget updates and ticks.
package com.harilvfs.npltz;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public final class GreetingWidgetProvider extends AppWidgetProvider {
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
            GreetingWidgetSupport.update(context, appWidgetManager, appWidgetId);
        }
        GreetingWidgetSupport.scheduleTick(context);
    }

    @Override
    public void onEnabled(Context context) {
        GreetingWidgetSupport.refreshAll(context);
    }

    @Override
    public void onDisabled(Context context) {
        GreetingWidgetSupport.scheduleTick(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(
        Context context,
        AppWidgetManager appWidgetManager,
        int appWidgetId,
        Bundle newOptions
    ) {
        GreetingWidgetSupport.update(
            context,
            appWidgetManager,
            appWidgetId,
            newOptions
        );
        GreetingWidgetSupport.scheduleTick(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (GreetingWidgetSupport.ACTION_TICK.equals(action)
            || WidgetSupport.ACTION_REFRESH.equals(action)) {
            GreetingWidgetSupport.refreshAll(context);
        }
    }
}
