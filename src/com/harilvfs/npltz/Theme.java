// palette metadata. colours live in palettes.xml as material theme overlays.
package com.harilvfs.npltz;

import android.graphics.Color;

public final class Theme {
    public final String key;
    public final String displayName;
    public final boolean light;

    public final int bg;
    public final int fg;
    public final int primary;
    public final int secondary;
    public final int success;
    public final int warning;
    public final int yellow;
    public final int error;

    public final int onPrimary;
    public final int primaryContainer;
    public final int onPrimaryContainer;
    public final int secondaryContainer;
    public final int onSecondaryContainer;
    public final int onSurface;
    public final int onSurfaceVariant;
    public final int outline;
    public final int outlineVariant;
    public final int surfaceContainerLowest;
    public final int surfaceContainerLow;
    public final int surfaceContainer;
    public final int surfaceContainerHigh;
    public final int surfaceContainerHighest;
    public final int scrim;

    public final int styleRes;

    private Theme(
        String key,
        String displayName,
        int bg,
        int fg,
        int primary,
        int secondary,
        int success,
        int warning,
        int yellow,
        int error,
        int styleRes
    ) {
        this.key = key;
        this.displayName = displayName;
        this.bg = bg;
        this.fg = fg;
        this.primary = primary;
        this.secondary = secondary;
        this.success = success;
        this.warning = warning;
        this.yellow = yellow;
        this.error = error;
        this.styleRes = styleRes;

        this.light = Color.luminance(bg) > 0.5f;
        int darkText = light ? fg : bg;
        int lightText = light ? bg : fg;

        this.onPrimary = contrasting(primary, darkText, lightText);
        this.primaryContainer = blend(bg, primary, 0.24f);
        this.onPrimaryContainer = contrasting(primaryContainer, darkText, lightText);
        this.secondaryContainer = blend(bg, secondary, 0.22f);
        this.onSecondaryContainer = contrasting(secondaryContainer, darkText, lightText);

        this.onSurface = fg;
        this.onSurfaceVariant = blend(fg, bg, 0.38f);
        this.outline = blend(bg, fg, 0.30f);
        this.outlineVariant = blend(bg, fg, 0.14f);

        this.surfaceContainerLowest = blend(bg, 0xFF000000, 0.16f);
        this.surfaceContainerLow = blend(bg, fg, 0.03f);
        this.surfaceContainer = blend(bg, fg, 0.06f);
        this.surfaceContainerHigh = blend(bg, fg, 0.09f);
        this.surfaceContainerHighest = blend(bg, fg, 0.13f);

        this.scrim = 0x99000000;
    }

    public static final Theme PLAIN_DARK = new Theme(
        "plain-dark", "Plain dark",
        0xFF000000, 0xFFE6E6E6, 0xFF5A5A5A, 0xFF7A7A7A,
        0xFF5F8A5F, 0xFFB08A40, 0xFFD8B24A, 0xFFB05A5A,
        R.style.ThemeOverlay_Npltz_PlainDark
    );

    public static final Theme WARM = new Theme(
        "warm-reading", "Warm reading",
        0xFFF3E9D6, 0xFF4A3A2A, 0xFF9C5A32, 0xFF83705A,
        0xFF5B7A4A, 0xFF9A6A1E, 0xFF8A6500, 0xFFB04A3A,
        R.style.ThemeOverlay_Npltz_WarmReading
    );

    public static final Theme LIGHT = new Theme(
        "light", "Light",
        0xFFF6F6F4, 0xFF1B1B1F, 0xFF3F5B9E, 0xFF5E6E7E,
        0xFF2E7D4F, 0xFF8A5A00, 0xFF8A6A00, 0xFFB3261E,
        R.style.ThemeOverlay_Npltz_Light
    );

    public static final Theme CATPPUCCIN = new Theme(
        "catppuccin-mocha", "Catppuccin Mocha",
        0xFF1E1E2E, 0xFFCDD6F4, 0xFF89B4FA, 0xFFADD8BE,
        0xFFA6E3A1, 0xFFFAB387, 0xFFF9E2AF, 0xFFF38BA8,
        R.style.ThemeOverlay_Npltz_CatppuccinMocha
    );

    public static final Theme DRACULA = new Theme(
        "dracula", "Dracula",
        0xFF282A36, 0xFFF8F8F2, 0xFFBD93F9, 0xFF8BE9FD,
        0xFF50FA7B, 0xFFF1FA8C, 0xFFF1FA8C, 0xFFFF5555,
        R.style.ThemeOverlay_Npltz_Dracula
    );

    public static final Theme GRUVBOX = new Theme(
        "gruvbox", "Gruvbox",
        0xFF282828, 0xFFEBDBB2, 0xFFB8BB26, 0xFF83A598,
        0xFFB8BB26, 0xFFD65D0E, 0xFFFABD2F, 0xFFCC241D,
        R.style.ThemeOverlay_Npltz_Gruvbox
    );

    public static final Theme NORD = new Theme(
        "nord", "Nord",
        0xFF2E3440, 0xFFD8DEE9, 0xFF88C0D0, 0xFFA3BE8C,
        0xFFA3BE8C, 0xFFEBCB8B, 0xFFEBCB8B, 0xFFBF616A,
        R.style.ThemeOverlay_Npltz_Nord
    );

    public static final Theme ROSE_PINE = new Theme(
        "rose-pine", "Ros\u00e9 Pine",
        0xFF191724, 0xFFE0DEF4, 0xFFC4A7E7, 0xFF9CCFD8,
        0xFF31748F, 0xFFF6C177, 0xFFF6C177, 0xFFEB6F92,
        R.style.ThemeOverlay_Npltz_RosePine
    );

    public static final Theme EVERFOREST = new Theme(
        "everforest", "Everforest",
        0xFF2D353B, 0xFFD3C6AA, 0xFFA7C080, 0xFF7FBBB3,
        0xFFA7C080, 0xFFE69875, 0xFFDBBC7F, 0xFFE67E80,
        R.style.ThemeOverlay_Npltz_Everforest
    );

    public static final Theme[] ALL = {
        CATPPUCCIN, DRACULA, EVERFOREST, GRUVBOX, LIGHT, NORD, PLAIN_DARK,
        ROSE_PINE, WARM,
    };

    public static Theme byKey(String key) {
        if (key != null) {
            for (Theme theme : ALL) {
                if (theme.key.equals(key)) {
                    return theme;
                }
            }
        }
        return CATPPUCCIN;
    }

    public static int blend(int from, int to, float amount) {
        float t = Math.max(0f, Math.min(1f, amount));
        int r = Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * t);
        int g = Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * t);
        int b = Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t);
        return Color.rgb(r, g, b);
    }

    public static int alpha(int color, int alpha) {
        return Color.argb(
            Math.max(0, Math.min(255, alpha)),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        );
    }

    public static int soft(int color, int alpha) {
        return alpha(color, alpha);
    }

    public static int contrasting(int color, int dark, int light) {
        return Color.luminance(color) > 0.42f ? dark : light;
    }
}
