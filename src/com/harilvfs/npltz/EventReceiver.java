// handles an event reminder alarm.
package com.harilvfs.npltz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class EventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !EventNotifier.ACTION_EVENT.equals(intent.getAction())) {
            return;
        }
        long id = intent.getLongExtra(EventNotifier.EXTRA_EVENT_ID, -1L);
        if (id > 0) {
            EventNotifier.show(context, id);
        }
    }
}
