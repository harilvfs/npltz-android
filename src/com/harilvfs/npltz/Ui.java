// small type helpers. Layout, colour and motion belong to Material 3.
package com.harilvfs.npltz;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.color.MaterialColors;

public final class Ui {
    public static final int NORMAL = 0;
    public static final int MEDIUM = 1;
    public static final int BOLD = 2;

    public static final String FONT_INTER = "inter";
    public static final String FONT_SYSTEM = "system";
    public static final String FONT_MONO = "mono";

    private static String fontKey = FONT_INTER;
    private static Typeface interNormal;
    private static Typeface interMedium;
    private static Typeface interBold;

    private Ui() {
    }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static int color(View view, int attr) {
        return MaterialColors.getColor(view, attr);
    }

    public static void initFonts(Context context) {
        ensureInter(context);
    }

    public static void setFont(String key) {
        fontKey = normalizeFont(key);
    }

    public static String getFont() {
        return fontKey;
    }

    public static String normalizeFont(String key) {
        if (FONT_SYSTEM.equals(key) || FONT_MONO.equals(key) || FONT_INTER.equals(key)) {
            return key;
        }
        return FONT_INTER;
    }

    public static String fontLabel(String key) {
        String normalized = normalizeFont(key);
        if (FONT_SYSTEM.equals(normalized)) {
            return "System";
        }
        if (FONT_MONO.equals(normalized)) {
            return "Mono";
        }
        return "Inter";
    }

    private static void ensureInter(Context context) {
        if (interNormal != null || context == null) {
            return;
        }
        try {
            Resources resources = context.getResources();
            interNormal = resources.getFont(R.font.inter_regular);
            interMedium = resources.getFont(R.font.inter_medium);
            interBold = resources.getFont(R.font.inter_bold);
        } catch (RuntimeException ignored) {
            interNormal = systemTypeface(NORMAL);
            interMedium = systemTypeface(MEDIUM);
            interBold = systemTypeface(BOLD);
        }
    }

    public static Typeface typeface(int weight) {
        return typefaceFor(null, fontKey, weight);
    }

    public static Typeface typefaceFor(Context context, String key, int weight) {
        String normalized = normalizeFont(key);
        if (FONT_SYSTEM.equals(normalized)) {
            return systemTypeface(weight);
        }
        if (FONT_MONO.equals(normalized)) {
            return monoTypeface(weight);
        }
        ensureInter(context);
        Typeface typeface = weight == BOLD ? interBold : weight == MEDIUM ? interMedium : interNormal;
        return typeface != null ? typeface : systemTypeface(weight);
    }

    public static Typeface mono(int style) {
        return Typeface.create(Typeface.MONOSPACE, style);
    }

    private static Typeface systemTypeface(int weight) {
        if (weight == MEDIUM) {
            return Typeface.create("sans-serif-medium", Typeface.NORMAL);
        }
        if (weight == BOLD) {
            return Typeface.create("sans-serif", Typeface.BOLD);
        }
        return Typeface.create("sans-serif", Typeface.NORMAL);
    }

    private static Typeface monoTypeface(int weight) {
        return Typeface.create("monospace", weight == NORMAL ? Typeface.NORMAL : Typeface.BOLD);
    }

    public static float sp(Context context, float value) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            value,
            context.getResources().getDisplayMetrics()
        );
    }

    // the bundled inter faces are wired through the material type roles; when
    // the user picks system or mono, walk the inflated tree once and swap the
    // typeface. no per-frame work, no global rebuilds.
    public static void applyTypography(View root) {
        if (FONT_INTER.equals(fontKey)) {
            return;
        }
        applyTypography(root, root.getContext());
    }

    private static void applyTypography(View view, Context context) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            Typeface current = text.getTypeface();
            int weight = NORMAL;
            if (current != null) {
                if (current.equals(interBold) || current.isBold()) {
                    weight = BOLD;
                } else if (current.equals(interMedium)) {
                    weight = MEDIUM;
                }
            }
            text.setTypeface(typefaceFor(context, fontKey, weight));
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTypography(group.getChildAt(i), context);
            }
        }
    }

    public static boolean isLightTheme(Context context) {
        return (context.getResources().getConfiguration().uiMode
            & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
            == android.content.res.Configuration.UI_MODE_NIGHT_NO;
    }
}
