// stores user settings.
package com.harilvfs.npltz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserManager;

public final class Prefs {
    private static final String FILE = "npltz";
    private static final String KEY_THEME = "theme";
    private static final String KEY_FONT = "font";
    private static final String KEY_SCRIPT = "script";
    private static final String KEY_TEXT_SCALE = "text_scale";
    private static final float DEFAULT_TEXT_SCALE = 1f;
    private static final float MIN_TEXT_SCALE = 0.85f;
    private static final float MAX_TEXT_SCALE = 1.25f;
    private static final String KEY_CLOCK_24H = "clock_24h";
    private static final String KEY_NOTIFY_ENABLED = "notify_enabled";
    private static final String KEY_NOTIFY_HOUR = "notify_hour";
    private static final String KEY_NOTIFY_MINUTE = "notify_minute";
    private static final String KEY_PERSISTENT_NOTIFY = "persistent_notify";
    private static final String KEY_GREETING_NAME = "greeting_name";
    private static final String KEY_GREETING_ENABLED = "greeting_enabled";

    private final SharedPreferences prefs;

    public Prefs(Context context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, Theme.CATPPUCCIN.key);
    }

    public void setTheme(String key) {
        prefs.edit().putString(KEY_THEME, key).apply();
    }

    public String getFont() {
        return prefs.getString(KEY_FONT, Ui.FONT_INTER);
    }

    public void setFont(String key) {
        prefs.edit().putString(KEY_FONT, key).apply();
    }

    public String getScript() {
        return prefs.getString(KEY_SCRIPT, "en");
    }

    public boolean isNepali() {
        return "ne".equals(getScript());
    }

    public void setScript(String script) {
        prefs.edit().putString(KEY_SCRIPT, "ne".equals(script) ? "ne" : "en").apply();
    }

    public float getTextScale() {
        float value = prefs.getFloat(KEY_TEXT_SCALE, DEFAULT_TEXT_SCALE);
        return Math.max(MIN_TEXT_SCALE, Math.min(MAX_TEXT_SCALE, value));
    }

    public void setTextScale(float scale) {
        float clamped = Math.max(MIN_TEXT_SCALE, Math.min(MAX_TEXT_SCALE, scale));
        prefs.edit().putFloat(KEY_TEXT_SCALE, clamped).apply();
    }

    public boolean isClock24Hour() {
        return prefs.getBoolean(KEY_CLOCK_24H, false);
    }

    public void setClock24Hour(boolean enabled) {
        prefs.edit().putBoolean(KEY_CLOCK_24H, enabled).apply();
    }

    public boolean isNotifyEnabled() {
        return prefs.getBoolean(KEY_NOTIFY_ENABLED, false);
    }

    public void setNotifyEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFY_ENABLED, enabled).apply();
    }

    public int getNotifyHour() {
        return prefs.getInt(KEY_NOTIFY_HOUR, 7);
    }

    public int getNotifyMinute() {
        return prefs.getInt(KEY_NOTIFY_MINUTE, 0);
    }

    public void setNotifyTime(int hour, int minute) {
        prefs.edit()
            .putInt(KEY_NOTIFY_HOUR, Math.max(0, Math.min(23, hour)))
            .putInt(KEY_NOTIFY_MINUTE, Math.max(0, Math.min(59, minute)))
            .apply();
    }

    public boolean isPersistentNotifyEnabled() {
        return prefs.getBoolean(KEY_PERSISTENT_NOTIFY, false);
    }

    public void setPersistentNotifyEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PERSISTENT_NOTIFY, enabled).apply();
    }

    public String getSavedGreetingName() {
        return prefs.getString(KEY_GREETING_NAME, "").trim();
    }

    public void setGreetingName(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.length() > 40) {
            clean = clean.substring(0, 40).trim();
        }
        if (clean.isEmpty()) {
            prefs.edit().remove(KEY_GREETING_NAME).apply();
        } else {
            prefs.edit().putString(KEY_GREETING_NAME, clean).apply();
        }
    }

    public String getGreetingName(Context context) {
        String saved = getSavedGreetingName();
        if (!saved.isEmpty()) {
            return saved;
        }
        return getSystemGreetingName(context);
    }

    @SuppressLint("MissingPermission")
    public String getSystemGreetingName(Context context) {
        try {
            UserManager users =
                (UserManager) context.getSystemService(Context.USER_SERVICE);
            String name = users == null ? null : users.getUserName();
            if (isUsableName(name)) {
                return name.trim();
            }
        } catch (Exception ignored) {
        }
        return "user";
    }

    private static boolean isUsableName(String name) {
        if (name == null) {
            return false;
        }
        String clean = name.trim();
        if (clean.isEmpty() || clean.length() > 40) {
            return false;
        }
        String lower = clean.toLowerCase(java.util.Locale.US);
        return !lower.equals("new user")
            && !lower.equals("owner")
            && !lower.equals("user")
            && !lower.equals("unknown")
            && !lower.equals("guest")
            && !lower.equals("admin");
    }

    public boolean isGreetingEnabled() {
        return prefs.getBoolean(KEY_GREETING_ENABLED, true);
    }

    public void setGreetingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_GREETING_ENABLED, enabled).apply();
    }
}
