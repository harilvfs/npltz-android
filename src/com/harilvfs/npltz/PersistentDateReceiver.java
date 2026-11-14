// refreshes the permanent date notification at midnight.
package com.harilvfs.npltz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class PersistentDateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !NotificationScheduler.ACTION_REFRESH.equals(intent.getAction())) {
            return;
        }
        Prefs prefs = new Prefs(context);
        if (!prefs.isPersistentNotifyEnabled()) {
            return;
        }
        NotificationScheduler.showPersistent(context);
        NotificationScheduler.schedulePersistentRefresh(context);
    }
}
