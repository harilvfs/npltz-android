// shows the daily date notification.
package com.harilvfs.npltz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class DailyReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Prefs prefs = new Prefs(context);
        if (!prefs.isNotifyEnabled()) {
            return;
        }
        if (NotificationScheduler.showToday(context)) {
            AlertPlayer.play(context);
        }
        NotificationScheduler.scheduleDaily(
            context,
            prefs.getNotifyHour(),
            prefs.getNotifyMinute()
        );
    }
}
