// shows new year and birthday notifications.
package com.harilvfs.npltz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class SpecialDayReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (!NotificationScheduler.ACTION_NEW_YEAR.equals(action)
            && !NotificationScheduler.ACTION_AD_NEW_YEAR.equals(action)
            && !NotificationScheduler.ACTION_BIRTHDAY.equals(action)) {
            return;
        }
        NotificationScheduler.showSpecialDay(context, action);
        NotificationScheduler.scheduleSpecialDays(context);
    }
}
