// restores reminder alarms after boot or a time change.
package com.harilvfs.npltz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
            && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
            && !Intent.ACTION_TIME_CHANGED.equals(action)
            && !Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            return;
        }
        Prefs prefs = new Prefs(context);
        NotificationScheduler.createChannel(context);
        NotificationScheduler.scheduleSpecialDays(context);
        EventNotifier.ensureChannel(context);
        EventNotifier.rescheduleAll(context);
        if (prefs.isNotifyEnabled()) {
            NotificationScheduler.scheduleDaily(
                context,
                prefs.getNotifyHour(),
                prefs.getNotifyMinute()
            );
        }
        if (prefs.isPersistentNotifyEnabled()) {
            NotificationScheduler.showPersistent(context);
            NotificationScheduler.schedulePersistentRefresh(context);
        }
        WidgetSupport.refreshAll(context);
    }
}
