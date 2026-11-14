// plays reminder sounds and vibration.
package com.harilvfs.npltz;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

final class AlertPlayer {
    private AlertPlayer() {
    }

    static void play(Context context) {
        try {
            AudioManager audio =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audio == null || audio.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                return;
            }
            NotificationManager notifications =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifications != null
                && notifications.getCurrentInterruptionFilter()
                    != NotificationManager.INTERRUPTION_FILTER_ALL) {
                return;
            }

            if (audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                Uri sound = Settings.System.DEFAULT_NOTIFICATION_URI;
                if (sound == null) {
                    sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
                Ringtone tone = RingtoneManager.getRingtone(
                    context.getApplicationContext(),
                    sound
                );
                if (tone != null) {
                    tone.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
                    tone.play();
                }
            }

            Vibrator vibrator =
                (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createWaveform(
                    new long[] {0L, 350L, 250L, 350L},
                    -1
                ));
            }
        } catch (Exception ignored) {
        }
    }
}
