// keeps the permanent date notification running.
package com.harilvfs.npltz;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;

import java.time.LocalDate;

public final class DateNotificationService extends Service {
    private static boolean running;
    private static String shownDate;

    static boolean start(Context context) {
        Intent intent = new Intent(context, DateNotificationService.class);
        try {
            context.startForegroundService(intent);
            return true;
        } catch (RuntimeException refused) {
            // android 12+ may reject a background start; post a normal notification.
            return NotificationScheduler.postPersistentNotification(context);
        }
    }

    // rebuilds the notification even when the date has not changed, e.g.
    // after a theme or language change from the settings screen.
    static boolean restart(Context context) {
        shownDate = null;
        return start(context);
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, DateNotificationService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // onResume calls start() on every return to the app; skip the rebuild
        // while the ongoing notification actually shows today's date. roms like
        // miui can stop the service without onDestroy, so never trust the
        // static flags alone, verify the notification is still posted.
        String today = LocalDate.now().toString();
        if (running && today.equals(shownDate) && persistentVisible()) {
            return START_STICKY;
        }
        Notification notification =
            NotificationScheduler.buildPersistentNotification(this);
        if (notification == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(NotificationScheduler.PERSISTENT_NOTIFY_ID, notification);
        running = true;
        shownDate = today;
        return START_STICKY;
    }

    private boolean persistentVisible() {
        NotificationManager manager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return false;
        }
        for (StatusBarNotification active : manager.getActiveNotifications()) {
            if (active.getId() == NotificationScheduler.PERSISTENT_NOTIFY_ID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        running = false;
        shownDate = null;
        stopForeground(STOP_FOREGROUND_REMOVE);
        NotificationManager manager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NotificationScheduler.PERSISTENT_NOTIFY_ID);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
