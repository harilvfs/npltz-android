package com.harilvfs.npltz;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

// renders the packaged changelog.md asset (generated from CHANGELOG.md at
// build time) into plain themed text for the settings changelog page.
final class Changelog {
    private static final String ASSET = "changelog.md";
    private static final Pattern HASH_LINK = Pattern.compile("^\\[[0-9a-f]{7}]\\([^)]*\\)\\s*");
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[([^\\]]*)]\\([^)]*\\)");
    private static final Pattern BARE_BRACKET = Pattern.compile("\\[([^\\]]*)]");
    private static final Pattern EMPHASIS = Pattern.compile("\\*{1,2}");

    private Changelog() {
    }

    // returns the formatted changelog, or null when the asset is missing.
    static CharSequence load(Context context) {
        String markdown = read(context);
        if (markdown == null) {
            return null;
        }
        SpannableStringBuilder out = new SpannableStringBuilder();
        boolean atGap = true;
        for (String raw : markdown.split("\n")) {
            String line = raw.trim();
            if (line.isEmpty()) {
                if (!atGap) {
                    out.append("\n");
                    atGap = true;
                }
                continue;
            }
            if (line.startsWith("<!--")) {
                continue;
            }
            if (line.startsWith("### ")) {
                out.append("\n");
                append(out, clean(line.substring(4)), 1.05f, Typeface.BOLD);
                atGap = false;
                continue;
            }
            if (line.startsWith("## ")) {
                if (out.length() > 0) {
                    out.append("\n");
                }
                append(out, clean(line.substring(3)), 1.2f, Typeface.BOLD);
                atGap = false;
                continue;
            }
            if (line.startsWith("# ")) {
                continue;
            }
            String body;
            boolean bullet = false;
            if (line.startsWith("- ")) {
                bullet = true;
                body = line.substring(2);
            } else {
                body = line;
            }
            body = HASH_LINK.matcher(body).replaceFirst("");
            append(out, (bullet ? "\u2022  " : "") + clean(body), 1.0f, Typeface.NORMAL);
            atGap = false;
        }
        return out;
    }

    private static void append(
        SpannableStringBuilder out, String text, float size, int style
    ) {
        int start = out.length();
        out.append(text).append("\n");
        out.setSpan(new RelativeSizeSpan(size), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new StyleSpan(style), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static String clean(String line) {
        line = MARKDOWN_LINK.matcher(line).replaceAll("$1");
        line = BARE_BRACKET.matcher(line).replaceAll("$1");
        line = EMPHASIS.matcher(line).replaceAll("");
        return line.trim();
    }

    private static String read(Context context) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            context.getAssets().open(ASSET), StandardCharsets.UTF_8
        ))) {
            StringBuilder out = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                out.append(buffer, 0, read);
            }
            return out.toString();
        } catch (Exception exception) {
            return null;
        }
    }
}
