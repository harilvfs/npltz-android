// main screen, sheets, settings and app wiring.
package com.harilvfs.npltz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MainActivity extends AppCompatActivity implements CalendarView.Host {
    private static final int REQUEST_NOTIFICATIONS = 2081;
    private static final String VERSION = "1.0.0";
    private static final String TUI_REPO_URL = "https://github.com/harilvfs/npltz";
    private static final String ANDROID_REPO_URL = "https://github.com/harilvfs/npltz-android";
    private static final String DEV_GITHUB_URL = "https://github.com/harilvfs";
    private static final String DEV_ISSUES_URL = "https://github.com/harilvfs/npltz-android/issues";
    private static final String DEV_WEBSITE_URL = "https://chalisehari.com.np";
    private static final String DEV_X_URL = "https://x.com/harilvfs";
    private static final String DEV_BLUESKY_URL = "https://bsky.app/profile/chalisehari.com.np";
    private static final String DEV_EMAIL_URL = "mailto:harilvfs@chalisehari.com.np";

    private static final String KEY_YEAR = "calendar_year";
    private static final String KEY_MONTH = "calendar_month";
    private static final String KEY_DAY = "calendar_day";
    private static final String KEY_MODE = "calendar_mode";
    private static final String KEY_SETTINGS = "settings_open";
    private static final String KEY_ABOUT = "about_open";

    private Prefs prefs;
    private Theme theme;
    private boolean nepali;

    private View rootView;
    private AppBarLayout appBar;
    private MaterialToolbar toolbar;
    private androidx.core.widget.NestedScrollView homeScroll;
    private LinearLayout homeContent;
    private MaterialButton previousMonth;
    private MaterialButton nextMonth;
    private TextView headlineView;
    private TextView rangeView;
    private MaterialButtonToggleGroup modeGroup;
    private MaterialButton monthTab;
    private MaterialButton yearTab;
    private FrameLayout calendarHost;
    private MaterialCardView calendarCard;
    private MaterialCardView greetingCard;
    private ImageView greetingIcon;
    private TextView greetingTitle;
    private TextView greetingWish;
    private NepalClockView clockView;
    private View bottomActions;
    private MaterialButton todayButton;
    private MaterialButton searchButton;

    private View settingsHost;
    private View settingsOverlay;
    private MaterialToolbar settingsToolbar;
    private androidx.core.widget.NestedScrollView settingsScroll;
    private LinearLayout settingsList;
    private TextView subtitleTheme;
    private TextView subtitleFont;
    private TextView subtitleTextSize;
    private TextView subtitleLanguage;
    private TextView subtitleName;
    private TextView subtitleEvents;
    private TextView subtitleReminder;
    private TextView settingsFooter;
    private MaterialSwitch switchClock24h;
    private MaterialSwitch switchGreeting;

    private View aboutHost;
    private View aboutOverlay;
    private MaterialToolbar aboutToolbar;
    private androidx.core.widget.NestedScrollView aboutScroll;
    private LinearLayout aboutList;
    private TextView aboutFooter;

    private CalendarView calendar;
    private final Set<String> eventDays = new HashSet<>();
    private boolean settingsOpen;
    private boolean aboutOpen;
    private boolean syncingModeTabs;
    private boolean suppressChromeAnimation;
    private String lastHeadline;

    private final Handler greetingHandler = new Handler(Looper.getMainLooper());
    private final Runnable greetingTicker = new Runnable() {
        @Override
        public void run() {
            refreshGreeting();
            long now = System.currentTimeMillis();
            greetingHandler.postDelayed(this, 60000L - (now % 60000L) + 100L);
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        float scale = new Prefs(newBase).getTextScale();
        if (Math.abs(scale - 1f) < 0.001f) {
            super.attachBaseContext(newBase);
            return;
        }
        Configuration configuration = new Configuration(
            newBase.getResources().getConfiguration()
        );
        float base = configuration.fontScale <= 0f ? 1f : configuration.fontScale;
        configuration.fontScale = base * scale;
        super.attachBaseContext(newBase.createConfigurationContext(configuration));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new Prefs(this);
        theme = Theme.byKey(prefs.getTheme());
        setTheme(theme.light ? R.style.AppTheme_Light : R.style.AppTheme);
        getTheme().applyStyle(theme.styleRes, true);
        getTheme().applyStyle(R.style.ThemeOverlay_Npltz_Typography, true);
        getTheme().applyStyle(fontOverlay(prefs.getFont()), true);
        super.onCreate(savedInstanceState);

        Ui.initFonts(this);
        Ui.setFont(prefs.getFont());
        nepali = prefs.isNepali();
        applyWindowStyle();
        setContentView(R.layout.activity_main);

        rootView = findViewById(R.id.root);
        bindViews();
        setupUi();
        applyInsets();
        restoreState(savedInstanceState);

        NotificationScheduler.createChannel(this);
        NotificationScheduler.scheduleSpecialDays(this);
        EventNotifier.ensureChannel(this);
        EventNotifier.rescheduleAll(this);
        handleEventIntent(getIntent());
    }

    private int fontOverlay(String key) {
        String normalized = Ui.normalizeFont(key);
        if (Ui.FONT_SYSTEM.equals(normalized)) {
            return R.style.ThemeOverlay_Npltz_Font_System;
        }
        if (Ui.FONT_MONO.equals(normalized)) {
            return R.style.ThemeOverlay_Npltz_Font_Mono;
        }
        return R.style.ThemeOverlay_Npltz_Font_Inter;
    }

    private void bindViews() {
        appBar = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
        homeScroll = findViewById(R.id.home_scroll);
        homeContent = findViewById(R.id.home_content);
        previousMonth = findViewById(R.id.previous_month);
        nextMonth = findViewById(R.id.next_month);
        headlineView = findViewById(R.id.headline);
        rangeView = findViewById(R.id.range);
        modeGroup = findViewById(R.id.mode_group);
        monthTab = findViewById(R.id.tab_month);
        yearTab = findViewById(R.id.tab_year);
        calendarHost = findViewById(R.id.calendar_host);
        calendarCard = findViewById(R.id.calendar_card);
        greetingCard = findViewById(R.id.greeting_card);
        greetingIcon = findViewById(R.id.greeting_icon);
        greetingTitle = findViewById(R.id.greeting_title);
        greetingWish = findViewById(R.id.greeting_wish);
        clockView = findViewById(R.id.clock);
        bottomActions = findViewById(R.id.bottom_actions);
        todayButton = findViewById(R.id.today_button);
        searchButton = findViewById(R.id.search_button);

        settingsOverlay = findViewById(R.id.settings_overlay);
        settingsHost = findViewById(R.id.settings_host);
        aboutHost = findViewById(R.id.about_host);
        if (settingsOverlay != null) {
            bindSettingsViews();
        }
    }

    /**
     * Inflates the Settings and About pages once, off the critical path, so
     * opening them never hitches on first-time layout inflation.
     */
    private void prewarmPages() {
        if (rootView == null) {
            return;
        }
        rootView.postDelayed(() -> {
            ensureSettings();
            ensureAbout();
        }, 800);
    }

    private void ensureSettings() {
        if (settingsOverlay != null || settingsHost == null) {
            return;
        }
        settingsOverlay = getLayoutInflater().inflate(
            R.layout.view_settings, (ViewGroup) settingsHost, false
        );
        ((ViewGroup) settingsHost).addView(settingsOverlay);
        bindSettingsViews();
        Ui.applyTypography(settingsOverlay);
        settingsToolbar.setNavigationOnClickListener(view -> closeSettings());
        bindSettingsRows();
        switchClock24h.setChecked(prefs.isClock24Hour());
        switchGreeting.setChecked(prefs.isGreetingEnabled());
        bindSwitchListeners();
        if (lastBars != null) {
            applySettingsInsets(lastBars);
        }
    }

    private void bindSettingsViews() {
        settingsToolbar = settingsOverlay.findViewById(R.id.settings_toolbar);
        settingsScroll = settingsOverlay.findViewById(R.id.settings_scroll);
        settingsList = settingsOverlay.findViewById(R.id.settings_list);
        subtitleTheme = settingsOverlay.findViewById(R.id.subtitle_theme);
        subtitleFont = settingsOverlay.findViewById(R.id.subtitle_font);
        subtitleTextSize = settingsOverlay.findViewById(R.id.subtitle_text_size);
        subtitleLanguage = settingsOverlay.findViewById(R.id.subtitle_language);
        subtitleName = settingsOverlay.findViewById(R.id.subtitle_name);
        subtitleEvents = settingsOverlay.findViewById(R.id.subtitle_events);
        subtitleReminder = settingsOverlay.findViewById(R.id.subtitle_reminder);
        settingsFooter = settingsOverlay.findViewById(R.id.settings_footer);
        switchClock24h = settingsOverlay.findViewById(R.id.switch_clock_24h);
        switchGreeting = settingsOverlay.findViewById(R.id.switch_greeting);
    }

    private void setupUi() {
        Ui.applyTypography(rootView);

        toolbar.setNavigationOnClickListener(view -> openUrl(ANDROID_REPO_URL));
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                openSettings();
                return true;
            }
            return false;
        });
        android.view.MenuItem settingsItem = toolbar.getMenu().findItem(R.id.action_settings);
        if (settingsItem != null) {
            settingsItem.setIconTintList(ColorStateList.valueOf(Ui.color(
                toolbar,
                com.google.android.material.R.attr.colorOnSurface
            )));
        }

        previousMonth.setOnClickListener(view -> calendar.changeStep(-1));
        nextMonth.setOnClickListener(view -> calendar.changeStep(1));
        todayButton.setOnClickListener(view -> openToday());
        searchButton.setOnClickListener(view -> openSearchSheet());

        modeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || syncingModeTabs || calendar == null) {
                return;
            }
            calendar.setMode(checkedId == R.id.tab_year
                ? CalendarView.MODE_YEAR
                : CalendarView.MODE_MONTH);
        });

        calendar = new CalendarView(this, prefs, this);
        calendar.setTheme(theme);
        calendar.setCompact(isLandscape());
        calendarHost.addView(calendar, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        eventDays.clear();
        eventDays.addAll(EventStore.dayKeys(this));

        clockView.configure(prefs.isClock24Hour(), nepali);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (aboutOpen) {
                    closeAbout();
                    return;
                }
                if (settingsOpen) {
                    closeSettings();
                    return;
                }
                if (calendar != null && calendar.getMode() == CalendarView.MODE_YEAR) {
                    calendar.showMonth();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });

        refreshChrome();
        refreshGreeting();
        refreshSummaries();
    }

    private void bindSettingsRows() {
        settingsRow(R.id.row_theme, this::openThemeSheet);
        settingsRow(R.id.row_font, this::openFontSheet);
        settingsRow(R.id.row_text_size, this::openTextSizeSheet);
        settingsRow(R.id.row_language, this::openLanguageSheet);
        settingsRow(R.id.row_name, this::openNameDialog);
        settingsRow(R.id.row_events, this::openEventsSheet);
        settingsRow(R.id.row_reminder, this::openReminderDialog);
        settingsRow(R.id.row_about, this::openAbout);
        settingsRow(R.id.row_tui_repo, () -> openUrl(TUI_REPO_URL));
        settingsRow(R.id.row_android_repo, () -> openUrl(ANDROID_REPO_URL));
        settingsRow(R.id.row_report_issue, () -> openUrl(DEV_ISSUES_URL));
        settingsRow(R.id.row_report_mail, this::openBugReportMail);

        findViewById(R.id.row_clock_24h).setOnClickListener(
            view -> switchClock24h.setChecked(!switchClock24h.isChecked())
        );
        findViewById(R.id.row_greeting).setOnClickListener(
            view -> switchGreeting.setChecked(!switchGreeting.isChecked())
        );
    }

    private void settingsRow(int id, Runnable action) {
        View row = findViewById(id);
        if (row != null) {
            row.setOnClickListener(view -> action.run());
        }
    }

    private void bindSwitchListeners() {
        switchClock24h.setOnCheckedChangeListener((button, checked) -> {
            prefs.setClock24Hour(checked);
            if (clockView != null) {
                clockView.configure(checked, nepali);
            }
            GreetingWidgetSupport.refreshAll(this);
            refreshSummaries();
        });

        switchGreeting.setOnCheckedChangeListener((button, checked) -> {
            prefs.setGreetingEnabled(checked);
            if (checked) {
                startGreetingTicker();
            } else {
                stopGreetingTicker();
            }
            refreshGreeting();
            GreetingWidgetSupport.refreshAll(this);
            Toast.makeText(
                this,
                checked ? "Greeting on" : "Greeting off",
                Toast.LENGTH_SHORT
            ).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (clockView != null) {
            clockView.start();
        }
        prewarmPages();
        if (prefs.isGreetingEnabled()) {
            startGreetingTicker();
        } else {
            stopGreetingTicker();
        }
        if (calendar != null) {
            calendar.refreshToday();
        }
        if (prefs.isNotifyEnabled()) {
            NotificationScheduler.scheduleDaily(
                this,
                prefs.getNotifyHour(),
                prefs.getNotifyMinute()
            );
        }
        if (prefs.isPersistentNotifyEnabled()) {
            NotificationScheduler.showPersistent(this);
            NotificationScheduler.schedulePersistentRefresh(this);
        } else {
            NotificationScheduler.cancelPersistent(this);
        }
        WidgetSupport.refreshAll(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (clockView != null) {
            clockView.stop();
        }
        stopGreetingTicker();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (handleEventIntent(intent)) {
            return;
        }
        if (calendar != null) {
            suppressChromeAnimation = true;
            calendar.jumpToToday(false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (calendar != null) {
            outState.putInt(KEY_YEAR, calendar.getViewYear());
            outState.putInt(KEY_MONTH, calendar.getViewMonth());
            outState.putInt(KEY_DAY, calendar.getSelectedDay());
            outState.putInt(KEY_MODE, calendar.getMode());
        }
        outState.putBoolean(KEY_SETTINGS, settingsOpen);
        outState.putBoolean(KEY_ABOUT, aboutOpen);
    }

    private void restoreState(Bundle state) {
        if (state == null || calendar == null) {
            return;
        }
        int year = state.getInt(KEY_YEAR, -1);
        if (year > 0) {
            calendar.restoreState(
                year,
                state.getInt(KEY_MONTH, 1),
                state.getInt(KEY_DAY, 1),
                state.getInt(KEY_MODE, CalendarView.MODE_MONTH)
            );
        }
        if (state.getBoolean(KEY_SETTINGS, false)) {
            openSettings(false);
        }
        if (state.getBoolean(KEY_ABOUT, false)) {
            openAbout(false);
        }
    }

    @Override
    public void onCalendarChanged() {
        refreshChrome();
        refreshGreeting();
    }

    @Override
    public void onDaySelected(int year, int month, int day) {
        showDaySheet(year, month, day);
    }

    @Override
    public boolean hasEvents(int year, int month, int day) {
        return eventDays.contains(EventStore.dateKey(year, month, day));
    }

    private boolean handleEventIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        long id = intent.getLongExtra(EventNotifier.EXTRA_EVENT_ID, -1L);
        if (id <= 0) {
            return false;
        }
        intent.removeExtra(EventNotifier.EXTRA_EVENT_ID);
        EventStore.Event event = EventStore.find(this, id);
        if (event == null) {
            return false;
        }
        final int year = event.year;
        final int month = event.month;
        final int day = event.day;
        if (calendar != null) {
            suppressChromeAnimation = true;
            calendar.goTo(year, month, day);
        }
        if (rootView != null) {
            rootView.post(() -> showDaySheet(year, month, day));
        } else {
            showDaySheet(year, month, day);
        }
        return true;
    }

    private void refreshChrome() {
        if (calendar == null || headlineView == null) {
            return;
        }
        int year = calendar.getViewYear();
        int month = calendar.getViewMonth();
        boolean yearMode = calendar.getMode() == CalendarView.MODE_YEAR;

        String headline = yearMode
            ? CalendarLogic.number(year, nepali)
            : CalendarLogic.monthName(month, nepali) + " "
                + CalendarLogic.number(year, nepali);
        String range = yearMode
            ? CalendarLogic.yearAdRangeLabel(year, nepali)
            : CalendarLogic.monthAdRangeLabel(year, month, nepali);

        boolean animate = !suppressChromeAnimation
            && lastHeadline != null
            && !lastHeadline.equals(headline);
        if (animate) {
            headlineView.animate().cancel();
            headlineView.setAlpha(0.55f);
            headlineView.setTranslationY(dp(4));
            rangeView.animate().cancel();
            rangeView.setAlpha(0.55f);
        }
        headlineView.setText(headline);
        rangeView.setText(range);
        if (animate) {
            headlineView.animate()
                .alpha(1f)
                .translationY(0f)
                .withLayer()
                .setDuration(150)
                .start();
            rangeView.animate()
                .alpha(1f)
                .withLayer()
                .setDuration(170)
                .start();
        }
        lastHeadline = headline;

        syncingModeTabs = true;
        modeGroup.check(yearMode ? R.id.tab_year : R.id.tab_month);
        syncingModeTabs = false;
        suppressChromeAnimation = false;
    }

    private void refreshGreeting() {
        if (greetingCard == null) {
            return;
        }
        boolean enabled = prefs.isGreetingEnabled()
            && !(isLandscape() && calendar != null
                && calendar.getMode() == CalendarView.MODE_YEAR);
        greetingCard.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (!enabled) {
            return;
        }

        int hour = ZonedDateTime.now(ZoneId.of("Asia/Kathmandu")).getHour();
        String name = displayName(prefs.getGreetingName(this));
        String title;
        String wish;
        int accentAttr;
        int icon;

        if (hour >= 5 && hour < 12) {
            title = nepali ? "शुभ प्रभात, " : "Good morning, ";
            wish = nepali
                ? "तपाईंको दिन शुभ होस्।"
                : "Wishing you a happy day.";
            accentAttr = com.google.android.material.R.attr.colorTertiary;
            icon = R.drawable.ic_sun;
        } else if (hour >= 12 && hour < 17) {
            title = nepali ? "शुभ दिउँसो, " : "Good afternoon, ";
            wish = nepali
                ? "तपाईंको दिन राम्रो होस्।"
                : "Hope your day is going well.";
            accentAttr = androidx.appcompat.R.attr.colorPrimary;
            icon = R.drawable.ic_sun;
        } else if (hour >= 17 && hour < 21) {
            title = nepali ? "शुभ साँझ, " : "Good evening, ";
            wish = nepali
                ? "अब आराम गर्नुहोस्।"
                : "Time to relax and unwind.";
            accentAttr = com.google.android.material.R.attr.colorSecondary;
            icon = R.drawable.ic_sunset;
        } else {
            title = nepali ? "शुभ रात्री, " : "Good night, ";
            wish = nepali
                ? "मीठो सपना देख्नुहोस्।"
                : "Have a sweet dream.";
            accentAttr = androidx.appcompat.R.attr.colorPrimary;
            icon = R.drawable.widget_greeting_moon;
        }

        int accent = Ui.color(greetingTitle, accentAttr);
        SpannableStringBuilder greeting = new SpannableStringBuilder(title + name);
        greeting.setSpan(
            new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    openNameDialog();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(accent);
                    ds.setUnderlineText(false);
                }
            },
            title.length(),
            greeting.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        greetingTitle.setText(greeting);
        greetingTitle.setMovementMethod(LinkMovementMethod.getInstance());
        greetingTitle.setHighlightColor(Color.TRANSPARENT);
        greetingTitle.setTextColor(Ui.color(
            greetingTitle,
            com.google.android.material.R.attr.colorOnSurface
        ));
        greetingTitle.setMaxLines(isLandscape() ? 2 : 1);

        greetingWish.setText(wish);
        greetingWish.setTextColor(Ui.color(
            greetingWish,
            com.google.android.material.R.attr.colorOnSurfaceVariant
        ));
        greetingIcon.setImageResource(icon);
        greetingIcon.setImageTintList(ColorStateList.valueOf(accent));

        if (subtitleName != null) {
            subtitleName.setText(name);
        }
    }

    private void startGreetingTicker() {
        greetingHandler.removeCallbacks(greetingTicker);
        greetingHandler.post(greetingTicker);
    }

    private void stopGreetingTicker() {
        greetingHandler.removeCallbacks(greetingTicker);
    }

    private String displayName(String raw) {
        if (raw == null) {
            return "user";
        }
        String clean = raw.trim();
        if (clean.isEmpty()) {
            return "user";
        }
        if (clean.equalsIgnoreCase("user")) {
            return "user";
        }
        return Character.toUpperCase(clean.charAt(0)) + clean.substring(1);
    }

    private void refreshSummaries() {
        if (subtitleTheme != null) {
            subtitleTheme.setText(theme.displayName);
        }
        if (subtitleFont != null) {
            subtitleFont.setText(Ui.fontLabel(prefs.getFont()));
        }
        if (subtitleTextSize != null) {
            subtitleTextSize.setText(textScaleLabel(prefs.getTextScale()));
        }
        if (subtitleLanguage != null) {
            subtitleLanguage.setText(
                nepali ? "नेपाली (देवनागरी)" : "English (US)"
            );
        }
        if (subtitleName != null) {
            subtitleName.setText(displayName(prefs.getGreetingName(this)));
        }
        if (subtitleEvents != null) {
            subtitleEvents.setText(eventsSummary());
        }
        if (subtitleReminder != null) {
            subtitleReminder.setText(reminderSummary());
        }
        if (settingsFooter != null) {
            settingsFooter.setText("Npltz for Android \u00b7 " + VERSION);
        }
    }

    private void openToday() {
        suppressChromeAnimation = true;
        calendar.jumpToToday(false);
        NepaliDate today = calendar.getTodayBs();
        if (today != null) {
            showDaySheet(today.year, today.month, today.day);
        }
    }

    // ---------------------------------------------------------------- sheets

    private static final class Sheet {
        final BottomSheetDialog dialog;
        final View content;

        Sheet(BottomSheetDialog dialog, View content) {
            this.dialog = dialog;
            this.content = content;
        }
    }

    private Sheet sheet(int layoutRes) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(layoutRes, null);
        dialog.setContentView(content);
        Ui.applyTypography(content);
        return new Sheet(dialog, content);
    }

    private void showSheet(Sheet sheet) {
        // expand before show() so the sheet slides once, instead of animating
        // to the peek height and then re-animating to the top.
        View parent = (View) sheet.content.getParent();
        if (parent != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parent);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        sheet.dialog.show();
    }

    private void showKeyboard(Sheet sheet, EditText input) {
        Window window = sheet.dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            );
        }
        input.requestFocus();
    }

    private void showDaySheet(int year, int month, int day) {
        int days = CalendarLogic.daysInMonth(year, month);
        if (day < 1 || day > days) {
            day = 1;
        }
        NepaliDate selected = new NepaliDate(
            year,
            month,
            day,
            CalendarLogic.weekdayOfBs(year, month, day)
        );
        LocalDate ad = CalendarLogic.bsToAd(year, month, day);
        NepaliDate today = calendar.getTodayBs();
        boolean isToday = today != null
            && today.year == year && today.month == month && today.day == day;
        boolean isPast = ad != null && ad.isBefore(LocalDate.now());
        boolean ne = nepali;

        Sheet sheet = sheet(R.layout.sheet_day);
        TextView overline = sheet.content.findViewById(R.id.day_overline);
        TextView title = sheet.content.findViewById(R.id.day_title);
        TextView subtitle = sheet.content.findViewById(R.id.day_subtitle);

        overline.setText(isToday
            ? (ne ? "आज" : "TODAY")
            : (ne ? "छानिएको मिति" : "SELECTED DATE"));
        if (ne) {
            overline.setLetterSpacing(0f);
        }

        String titleText = CalendarLogic.monthName(month, ne) + " "
            + CalendarLogic.number(day, ne) + ", "
            + CalendarLogic.number(year, ne);
        title.setText(titleText);

        String weekday = CalendarLogic.dayName(selected.weekday, ne);
        String subtitleText = weekday;
        if (ad != null) {
            String adWeekday = ne ? null : CalendarLogic.adDayName(ad);
            subtitleText += " \u00b7 "
                + (adWeekday != null ? adWeekday + ", " : "")
                + CalendarLogic.adShort(ad, ne);
        }
        subtitle.setText(subtitleText);

        bindSpecialDay(sheet.content, month, day, ad, ne);

        final int eventYear = year;
        final int eventMonth = month;
        final int eventDay = day;
        List<EventStore.Event> dayEvents = EventStore.forDate(this, year, month, day);

        TextView eventsLabel = sheet.content.findViewById(R.id.day_events_label);
        TextView eventCount = sheet.content.findViewById(R.id.day_event_count);
        LinearLayout events = sheet.content.findViewById(R.id.day_events);
        MaterialCardView empty = sheet.content.findViewById(R.id.day_events_empty);
        TextView emptyText = sheet.content.findViewById(R.id.day_events_empty_text);
        MaterialButton addEvent = sheet.content.findViewById(R.id.day_add_event);

        eventsLabel.setText(ne ? "घटना र टिप्पणी" : "EVENTS & NOTES");
        if (ne) {
            eventsLabel.setLetterSpacing(0f);
        }
        eventCount.setText(dayEvents.isEmpty()
            ? (ne ? "अझै केही थप्नुहोस्" : "Nothing planned yet")
            : (ne
                ? CalendarLogic.number(dayEvents.size(), true) + " सञ्चित"
                : dayEvents.size() + (dayEvents.size() == 1
                    ? " saved item"
                    : " saved items")));

        addEvent.setOnClickListener(view -> {
            if (isPast) {
                showPastDateWarning();
                return;
            }
            sheet.dialog.dismiss();
            openEventEditor(eventYear, eventMonth, eventDay, null, false);
        });

        if (dayEvents.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            events.setVisibility(View.GONE);
            emptyText.setText(ne
                ? "त्यो दिनको लागि घटना वा टिप्पणी थप्न + थिच्नुहोस्।"
                : "Tap + to add an event or a note for this date.");
        } else {
            empty.setVisibility(View.GONE);
            events.setVisibility(View.VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(this);
            for (EventStore.Event event : dayEvents) {
                View row = inflater.inflate(R.layout.item_day_event_row, events, false);
                ((TextView) row.findViewById(R.id.item_event_title)).setText(event.title);

                String meta = NotificationScheduler.timeLabel(this, event.hour, event.minute);
                if (!event.note.isEmpty()) {
                    meta += " \u00b7 " + event.note;
                }
                ((TextView) row.findViewById(R.id.item_event_meta)).setText(meta);
                row.findViewById(R.id.item_event_notified).setVisibility(
                    event.notified ? View.VISIBLE : View.GONE
                );

                row.setOnClickListener(view -> {
                    sheet.dialog.dismiss();
                    openEventEditor(eventYear, eventMonth, eventDay, event, false);
                });

                MaterialButton delete = row.findViewById(R.id.item_event_delete);
                delete.setOnClickListener(view -> {
                    EventNotifier.cancel(this, event);
                    EventStore.delete(this, event.id);
                    refreshEventCalendar();
                    Toast.makeText(
                        this,
                        ne ? "घटना मेटियो" : "Event deleted",
                        Toast.LENGTH_SHORT
                    ).show();
                    sheet.dialog.dismiss();
                    if (rootView != null) {
                        rootView.post(() -> showDaySheet(eventYear, eventMonth, eventDay));
                    }
                });

                events.addView(row);
            }
        }

        int daysLeft = days - day;
        int dayOfYear = CalendarLogic.dayOfYear(year, month, day);
        int yearDays = CalendarLogic.yearTotalDays(year);
        float fraction = yearDays > 0
            ? Math.min(1f, dayOfYear / (float) yearDays)
            : 0f;
        int percent = Math.round(fraction * 100);

        TextView meta = sheet.content.findViewById(R.id.day_meta);
        TextView percentView = sheet.content.findViewById(R.id.day_percent);
        LinearProgressIndicator progress = sheet.content.findViewById(R.id.day_progress);
        TextView footer = sheet.content.findViewById(R.id.day_footer);

        meta.setText(ne
            ? CalendarLogic.number(daysLeft, true) + " दिन बाँकी \u00b7 "
                + CalendarLogic.monthName(month, true)
            : daysLeft + (daysLeft == 1 ? " day" : " days")
                + " left in " + CalendarLogic.monthName(month));
        percentView.setText(ne
            ? CalendarLogic.number(percent, true) + "%"
            : percent + "%");
        progress.setProgressCompat(percent, true);

        int week = CalendarLogic.weekOfYear(year, month, day);
        int weeks = CalendarLogic.weeksInYear(year);
        footer.setText(ne
            ? "दिन " + CalendarLogic.number(dayOfYear, true)
                + " / " + CalendarLogic.number(yearDays, true)
                + " \u00b7 हप्ता " + CalendarLogic.number(week, true)
                + "/" + CalendarLogic.number(weeks, true)
            : "Day " + dayOfYear + " of " + yearDays
                + " \u00b7 Week " + week + "/" + weeks);

        MaterialButton goToday = sheet.content.findViewById(R.id.day_go_today);
        goToday.setVisibility(isToday ? View.GONE : View.VISIBLE);
        goToday.setOnClickListener(view -> {
            sheet.dialog.dismiss();
            suppressChromeAnimation = true;
            calendar.jumpToToday(false);
        });

        MaterialButton done = sheet.content.findViewById(R.id.day_done);
        done.setOnClickListener(view -> sheet.dialog.dismiss());

        showSheet(sheet);
    }

    private void bindSpecialDay(View content, int month, int day,
                                LocalDate ad, boolean ne) {
        MaterialCardView card = content.findViewById(R.id.day_special_card);
        ImageView icon = content.findViewById(R.id.day_special_icon);
        TextView overline = content.findViewById(R.id.day_special_overline);
        TextView title = content.findViewById(R.id.day_special_title);
        TextView date = content.findViewById(R.id.day_special_date);
        TextView greeting = content.findViewById(R.id.day_special_greeting);

        int iconRes;
        int accent;
        if (CalendarLogic.isNewYear(month, day)) {
            iconRes = R.drawable.ic_star;
            accent = theme.warning;
            overline.setText(ne ? "उत्सव" : "CELEBRATION");
            title.setText(ne ? "नयाँ वर्ष" : "Nepali New Year");
            greeting.setText(ne
                ? "शुभ नयाँ वर्ष!"
                : "Wishing you a bright new year.");
        } else if (CalendarLogic.isBirthday(month, day)) {
            iconRes = R.drawable.ic_cake;
            accent = theme.error;
            overline.setText(ne ? "उत्सव" : "CELEBRATION");
            title.setText(ne ? "हरिको जन्मदिन" : "Hari's birthday");
            greeting.setText(ne
                ? "जन्मदिनको शुभकामना, हरि!"
                : "Happy birthday, Hari! Have a wonderful year.");
        } else {
            card.setVisibility(View.GONE);
            return;
        }

        if (ne) {
            overline.setLetterSpacing(0f);
        }
        String specialDate = CalendarLogic.monthName(month, ne) + " "
            + CalendarLogic.number(day, ne);
        if (ad != null) {
            specialDate += " \u00b7 " + CalendarLogic.adShort(ad, ne);
        }

        card.setVisibility(View.VISIBLE);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(accent));
        overline.setTextColor(accent);
        date.setText(specialDate);
        greeting.setTextColor(accent);
    }

    private void showPastDateWarning() {
        boolean ne = nepali;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(ne ? "यहाँ घटना थप्न सकिँदैन" : "Can't add events here")
            .setMessage(ne
                ? "बितिसकेका दिनहरूमा घटना थप्न सकिँदैन। यो आज र आउँदो दिनहरूको "
                    + "लागि मात्र काम गर्छ।"
                : "Events can't be added to a day that has already passed. "
                    + "They work only for today and upcoming days.")
            .setPositiveButton(ne ? "ठीक छ" : "OK", null)
            .show();
    }

    private void openEventEditor(int year, int month, int day, EventStore.Event existing,
                                 boolean returnToSettings) {
        boolean editing = existing != null;
        boolean ne = nepali;
        LocalDate ad = CalendarLogic.bsToAd(year, month, day);
        if (!editing && ad != null && ad.isBefore(LocalDate.now())) {
            showPastDateWarning();
            return;
        }

        Sheet sheet = sheet(R.layout.sheet_event);
        TextView overline = sheet.content.findViewById(R.id.event_overline);
        TextView title = sheet.content.findViewById(R.id.event_title);
        TextView subtitle = sheet.content.findViewById(R.id.event_subtitle);
        TextInputLayout titleLayout = sheet.content.findViewById(R.id.event_title_input);
        EditText titleInput = sheet.content.findViewById(R.id.event_title_edit);
        TextInputLayout noteLayout = sheet.content.findViewById(R.id.event_note_input);
        EditText noteInput = sheet.content.findViewById(R.id.event_note_edit);
        TextView timeLabel = sheet.content.findViewById(R.id.event_time_label);
        MaterialCardView timeRow = sheet.content.findViewById(R.id.event_time_row);
        TextView timeValue = sheet.content.findViewById(R.id.event_time_value);
        MaterialButton delete = sheet.content.findViewById(R.id.event_delete);
        MaterialButton cancel = sheet.content.findViewById(R.id.event_cancel);
        MaterialButton save = sheet.content.findViewById(R.id.event_save);

        overline.setText(editing
            ? (ne ? "घटना सम्पादन" : "EDIT EVENT")
            : (ne ? "नयाँ घटना" : "NEW EVENT"));
        if (ne) {
            overline.setLetterSpacing(0f);
        }
        title.setText(editing
            ? (ne ? "घटना सम्पादन गर्नुहोस्" : "Edit event")
            : (ne ? "घटना थप्नुहोस्" : "New event"));

        String dateTitle = CalendarLogic.monthName(month, ne) + " "
            + CalendarLogic.number(day, ne) + ", "
            + CalendarLogic.number(year, ne);
        String dateSubtitle = dateTitle;
        if (ad != null) {
            dateSubtitle += " \u00b7 " + CalendarLogic.adShort(ad, ne);
        }
        subtitle.setText(dateSubtitle);
        timeLabel.setText(ne ? "समय" : "TIME");

        titleLayout.setHint(ne ? "शीर्षक" : "Title");
        noteLayout.setHint(ne ? "टिप्पणी (वैकल्पिक)" : "Note (optional)");
        titleInput.setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(EventStore.MAX_TITLE)
        });
        noteInput.setFilters(new InputFilter[] {
            new InputFilter.LengthFilter(EventStore.MAX_NOTE)
        });

        if (editing) {
            titleInput.setText(existing.title);
            titleInput.setSelection(titleInput.getText().length());
            noteInput.setText(existing.note);
        }

        final int[] when = new int[] {
            editing ? existing.hour : defaultEventHour(year, month, day),
            editing ? existing.minute : defaultEventMinute(year, month, day)
        };
        timeValue.setText(NotificationScheduler.timeLabel(this, when[0], when[1]));

        timeRow.setOnClickListener(view -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(prefs.isClock24Hour()
                    ? TimeFormat.CLOCK_24H
                    : TimeFormat.CLOCK_12H)
                .setHour(when[0])
                .setMinute(when[1])
                .setTitleText(ne ? "घटना समय" : "Event time")
                .build();
            picker.addOnPositiveButtonClickListener(done -> {
                when[0] = picker.getHour();
                when[1] = picker.getMinute();
                timeValue.setText(NotificationScheduler.timeLabel(
                    this,
                    when[0],
                    when[1]
                ));
            });
            picker.show(getSupportFragmentManager(), "event-time");
        });

        delete.setVisibility(editing ? View.VISIBLE : View.GONE);
        delete.setOnClickListener(view -> {
            EventNotifier.cancel(this, existing);
            EventStore.delete(this, existing.id);
            refreshEventCalendar();
            Toast.makeText(
                this,
                ne ? "घटना मेटियो" : "Event deleted",
                Toast.LENGTH_SHORT
            ).show();
            sheet.dialog.dismiss();
            postAfterSheet(returnToSettings, year, month, day);
        });

        cancel.setOnClickListener(view -> sheet.dialog.dismiss());
        save.setOnClickListener(view -> {
            String eventTitle = titleInput.getText().toString().trim();
            if (eventTitle.isEmpty()) {
                titleLayout.setError(ne
                    ? "शीर्षक लेख्नुहोस्"
                    : "Add a title");
                titleInput.requestFocus();
                return;
            }
            titleLayout.setError(null);
            String note = noteInput.getText().toString().trim();
            boolean timeChanged = existing != null
                && (existing.hour != when[0] || existing.minute != when[1]);
            EventStore.Event event = existing;
            if (event == null) {
                event = new EventStore.Event(
                    0,
                    year,
                    month,
                    day,
                    when[0],
                    when[1],
                    eventTitle,
                    note
                );
            } else {
                event.title = eventTitle;
                event.note = note;
                event.hour = when[0];
                event.minute = when[1];
            }
            if (timeChanged) {
                event.notified = false;
            }
            EventStore.Event saved = EventStore.save(this, event);
            if (saved == null) {
                Toast.makeText(
                    this,
                    ne ? "घटना सुरक्षित गर्न सकिएन" : "Could not save this event",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }
            if (editing) {
                EventNotifier.cancel(this, existing);
            }
            boolean armed = EventNotifier.schedule(this, saved);
            if (!EventNotifier.canNotify(this)) {
                requestNotificationPermission();
            }
            refreshEventCalendar();
            Toast.makeText(
                this,
                armed
                    ? (ne
                        ? "घटना सुरक्षित \u00b7 सूचना सेट भयो"
                        : "Event saved \u00b7 reminder set")
                    : (ne ? "घटना सुरक्षित" : "Event saved"),
                Toast.LENGTH_SHORT
            ).show();
            sheet.dialog.dismiss();
            postAfterSheet(returnToSettings, year, month, day);
        });

        showSheet(sheet);
        showKeyboard(sheet, titleInput);
    }

    private void postAfterSheet(boolean returnToSettings, int year, int month, int day) {
        Runnable action = returnToSettings
            ? this::openEventsSheet
            : () -> showDaySheet(year, month, day);
        if (rootView != null) {
            rootView.post(action);
        } else {
            action.run();
        }
    }

    private int defaultEventHour(int year, int month, int day) {
        NepaliDate today = calendar != null ? calendar.getTodayBs() : CalendarLogic.todayBs();
        if (today != null && today.year == year && today.month == month && today.day == day) {
            int minutes = ZonedDateTime.now().getHour() * 60 + ZonedDateTime.now().getMinute();
            minutes = ((minutes / 30) + 1) * 30;
            if (minutes >= 24 * 60) {
                minutes = 23 * 60 + 59;
            }
            return minutes / 60;
        }
        return 9;
    }

    private int defaultEventMinute(int year, int month, int day) {
        NepaliDate today = calendar != null ? calendar.getTodayBs() : CalendarLogic.todayBs();
        if (today != null && today.year == year && today.month == month && today.day == day) {
            int minutes = ZonedDateTime.now().getHour() * 60 + ZonedDateTime.now().getMinute();
            minutes = ((minutes / 30) + 1) * 30;
            if (minutes >= 24 * 60) {
                minutes = 23 * 60 + 59;
            }
            return minutes % 60;
        }
        return 0;
    }

    private void openEventsSheet() {
        boolean ne = nepali;
        Sheet sheet = sheet(R.layout.sheet_events);
        TextView title = sheet.content.findViewById(R.id.events_title);
        TextView summary = sheet.content.findViewById(R.id.events_summary);
        MaterialCardView empty = sheet.content.findViewById(R.id.events_empty);
        TextView emptyText = sheet.content.findViewById(R.id.events_empty_text);
        LinearLayout list = sheet.content.findViewById(R.id.events_list);
        MaterialButton done = sheet.content.findViewById(R.id.events_done);

        title.setText(ne ? "घटना र टिप्पणी" : "Events & notes");

        List<EventStore.Event> all = EventStore.all(this);
        long now = System.currentTimeMillis();
        List<EventStore.Event> upcoming = new ArrayList<>();
        List<EventStore.Event> past = new ArrayList<>();
        for (EventStore.Event event : all) {
            if (EventNotifier.triggerMillis(event) > now) {
                upcoming.add(event);
            } else {
                past.add(event);
            }
        }

        summary.setText(all.isEmpty()
            ? (ne ? "अझै केही थपिएको छैन" : "Nothing saved yet")
            : (ne
                ? (upcoming.isEmpty()
                    ? CalendarLogic.number(past.size(), true) + " सम्पन्न"
                    : CalendarLogic.number(upcoming.size(), true) + " आउँदा"
                        + (past.isEmpty()
                            ? ""
                            : " \u00b7 " + CalendarLogic.number(past.size(), true)
                                + " सम्पन्न"))
                : (upcoming.isEmpty()
                    ? past.size() + " done"
                    : upcoming.size() + " upcoming"
                        + (past.isEmpty() ? "" : " \u00b7 " + past.size() + " done"))));

        if (all.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
            emptyText.setText(ne
                ? "पात्रोबाट दिन छानेर + थिच्नुहोस्।"
                : "Open a date on the calendar and tap + to add one.");
        } else {
            empty.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            if (!upcoming.isEmpty()) {
                addEventGroup(sheet, ne ? "आउँदा" : "UPCOMING", upcoming);
            }
            if (!past.isEmpty()) {
                Collections.reverse(past);
                addEventGroup(sheet, ne ? "पहिले" : "PAST", past);
            }
        }

        done.setOnClickListener(view -> sheet.dialog.dismiss());
        showSheet(sheet);
    }

    private void addEventGroup(Sheet sheet, String label, List<EventStore.Event> events) {
        boolean ne = nepali;
        LinearLayout list = sheet.content.findViewById(R.id.events_list);

        MaterialTextView section = new MaterialTextView(this);
        section.setText(label);
        androidx.core.widget.TextViewCompat.setTextAppearance(
            section,
            R.style.TextAppearance_Npltz_Overline
        );
        if (ne) {
            section.setLetterSpacing(0f);
        }
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sectionParams.topMargin = dp(18);
        list.addView(section, sectionParams);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (EventStore.Event event : events) {
            View row = inflater.inflate(R.layout.item_event_row, list, false);
            TextView dayView = row.findViewById(R.id.item_event_day);
            dayView.setText(CalendarLogic.number(event.day, ne));
            ((TextView) row.findViewById(R.id.item_event_title)).setText(event.title);

            String date = CalendarLogic.monthName(event.month, ne) + " "
                + CalendarLogic.number(event.day, ne) + ", "
                + CalendarLogic.number(event.year, ne);
            ((TextView) row.findViewById(R.id.item_event_meta)).setText(
                date + " \u00b7 "
                    + NotificationScheduler.timeLabel(this, event.hour, event.minute)
            );

            TextView note = row.findViewById(R.id.item_event_note);
            if (event.note.isEmpty()) {
                note.setVisibility(View.GONE);
            } else {
                note.setVisibility(View.VISIBLE);
                note.setText(event.note);
            }

            row.findViewById(R.id.item_event_notified).setVisibility(
                event.notified ? View.VISIBLE : View.GONE
            );

            row.setOnClickListener(view -> {
                sheet.dialog.dismiss();
                if (rootView != null) {
                    rootView.post(() -> openEventEditor(
                        event.year,
                        event.month,
                        event.day,
                        event,
                        true
                    ));
                }
            });

            MaterialButton delete = row.findViewById(R.id.item_event_delete);
            delete.setOnClickListener(view -> {
                EventNotifier.cancel(this, event);
                EventStore.delete(this, event.id);
                refreshEventCalendar();
                Toast.makeText(
                    this,
                    ne ? "घटना मेटियो" : "Event deleted",
                    Toast.LENGTH_SHORT
                ).show();
                sheet.dialog.dismiss();
                if (rootView != null) {
                    rootView.post(this::openEventsSheet);
                }
            });

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rowParams.topMargin = dp(8);
            list.addView(row, rowParams);
        }
    }

    private void refreshEventCalendar() {
        eventDays.clear();
        eventDays.addAll(EventStore.dayKeys(this));
        if (calendar != null) {
            calendar.invalidate();
        }
        refreshSummaries();
    }

    private String eventsSummary() {
        List<EventStore.Event> all = EventStore.all(this);
        if (all.isEmpty()) {
            return "No events yet";
        }
        long now = System.currentTimeMillis();
        int upcoming = 0;
        for (EventStore.Event event : all) {
            if (EventNotifier.triggerMillis(event) > now) {
                upcoming++;
            }
        }
        if (upcoming > 0) {
            return upcoming + " upcoming \u00b7 " + all.size() + " total";
        }
        return nepali
            ? CalendarLogic.number(all.size(), true) + " सम्पन्न"
            : all.size() + " done";
    }

    // -------------------------------------------------------------- settings

    private void openSettings() {
        openSettings(true);
    }

    private void openSettings(boolean animate) {
        if (settingsHost == null) {
            return;
        }
        ensureSettings();
        if (settingsOverlay == null) {
            return;
        }
        settingsOpen = true;
        refreshSummaries();
        syncChrome();
        // the overlay is opaque and covers the screen; leaving the app bar in
        // place avoids a full reflow (visible jump) of the page.
        settingsHost.setVisibility(View.VISIBLE);
        settingsOverlay.setVisibility(View.VISIBLE);
        settingsOverlay.bringToFront();
        if (animate) {
            settingsOverlay.setAlpha(0f);
            settingsOverlay.setTranslationY(dp(12));
            settingsOverlay.animate()
                .alpha(1f)
                .translationY(0f)
                .withLayer()
                .setDuration(200)
                .start();
        } else {
            settingsOverlay.setAlpha(1f);
            settingsOverlay.setTranslationY(0f);
        }
    }

    private void closeSettings() {
        if (settingsOverlay == null) {
            return;
        }
        settingsOpen = false;
        settingsOverlay.animate()
            .alpha(0f)
            .translationY(dp(12))
            .withLayer()
            .setDuration(140)
            .withEndAction(() -> {
                settingsOverlay.setVisibility(View.GONE);
                settingsOverlay.setAlpha(1f);
                settingsOverlay.setTranslationY(0f);
                if (settingsHost != null) {
                    settingsHost.setVisibility(View.GONE);
                }
                syncChrome();
            })
            .start();
    }

    private String reminderSummary() {
        if (!prefs.isNotifyEnabled()) {
            return "Off";
        }
        return "On \u00b7 " + NotificationScheduler.timeLabel(
            this,
            prefs.getNotifyHour(),
            prefs.getNotifyMinute()
        );
    }

    private void openNameDialog() {
        boolean ne = nepali;
        Sheet sheet = sheet(R.layout.sheet_name);
        TextInputLayout inputLayout = sheet.content.findViewById(R.id.name_input);
        EditText input = sheet.content.findViewById(R.id.name_edit);
        MaterialButton cancel = sheet.content.findViewById(R.id.name_cancel);
        MaterialButton save = sheet.content.findViewById(R.id.name_save);

        inputLayout.setHint(ne ? "तपाईंको नाम" : "Your name");
        input.setText(prefs.getSavedGreetingName());
        input.setHint(displayName(prefs.getSystemGreetingName(this)));
        input.setSelection(input.getText().length());

        cancel.setOnClickListener(view -> sheet.dialog.dismiss());
        save.setOnClickListener(view -> saveGreetingName(input, sheet));
        input.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveGreetingName(input, sheet);
                return true;
            }
            return false;
        });

        showSheet(sheet);
        showKeyboard(sheet, input);
    }

    private void saveGreetingName(EditText input, Sheet sheet) {
        prefs.setGreetingName(input.getText().toString());
        refreshGreeting();
        refreshSummaries();
        sheet.dialog.dismiss();
        GreetingWidgetSupport.refreshAll(this);
        Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
    }

    private void openSearchSheet() {
        boolean ne = nepali;
        Sheet sheet = sheet(R.layout.sheet_search);
        TextView title = sheet.content.findViewById(R.id.search_title);
        TextInputLayout inputLayout = sheet.content.findViewById(R.id.search_input);
        EditText input = sheet.content.findViewById(R.id.search_edit);
        TextView status = sheet.content.findViewById(R.id.search_status);
        MaterialButton cancel = sheet.content.findViewById(R.id.search_cancel);
        MaterialButton jump = sheet.content.findViewById(R.id.search_jump);

        title.setText(ne ? "खोज" : "Search");
        inputLayout.setHint(ne ? "मिति खोज्नुहोस्" : "Search dates");

        final Runnable update = () -> {
            String raw = input.getText().toString();
            boolean empty = raw.trim().isEmpty();
            GotoResult result = resolveGoto(raw);
            if (empty) {
                status.setText("YYYY  \u00b7  YYYY-MM  \u00b7  YYYY-MM-DD");
                status.setTextColor(Ui.color(
                    status,
                    com.google.android.material.R.attr.colorOnSurfaceVariant
                ));
            } else if (result.valid) {
                status.setText(ne ? "जान तयार" : "Ready to jump");
                status.setTextColor(Ui.color(
                    status,
                    androidx.appcompat.R.attr.colorPrimary
                ));
            } else {
                status.setText(result.error);
                status.setTextColor(Ui.color(
                    status,
                    androidx.appcompat.R.attr.colorError
                ));
            }
            jump.setEnabled(result.valid);
            jump.setAlpha(result.valid ? 1f : 0.45f);
        };

        jump.setOnClickListener(view -> {
            GotoResult result = resolveGoto(input.getText().toString());
            if (!result.valid) {
                return;
            }
            if (result.day > 0) {
                calendar.goTo(result.year, result.month, result.day);
            } else if (result.month > 0) {
                calendar.goTo(result.year, result.month, 1);
            } else {
                calendar.goTo(result.year, 1, 1);
            }
            sheet.dialog.dismiss();
        });
        cancel.setOnClickListener(view -> sheet.dialog.dismiss());

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                update.run();
            }
        });
        input.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO && jump.isEnabled()) {
                jump.performClick();
                return true;
            }
            return false;
        });

        update.run();
        showSheet(sheet);
        showKeyboard(sheet, input);
    }

    private GotoResult resolveGoto(String raw) {
        String value = raw == null ? "" : raw.trim()
            .replace('/', '-')
            .replace('.', '-')
            .replace(' ', '-');
        if (value.isEmpty()) {
            return GotoResult.invalid(
                nepali ? "वर्षबाट सुरु गर्नुहोस्।" : "Start with a year."
            );
        }

        String[] parts = value.split("-");
        if (parts.length < 1 || parts.length > 3) {
            return GotoResult.invalid(
                nepali
                    ? "YYYY, YYYY-MM वा YYYY-MM-DD प्रयोग गर्नुहोस्।"
                    : "Use YYYY, YYYY-MM or YYYY-MM-DD."
            );
        }

        int year;
        int month = 0;
        int day = 0;
        try {
            year = Integer.parseInt(parts[0]);
            if (parts.length >= 2) {
                month = Integer.parseInt(parts[1]);
            }
            if (parts.length == 3) {
                day = Integer.parseInt(parts[2]);
            }
        } catch (NumberFormatException exception) {
            return GotoResult.invalid(
                nepali
                    ? "अंक मात्र प्रयोग गर्नुहोस्, जस्तै 2083 वा 2083-07।"
                    : "Use numbers only, for example 2083 or 2083-07."
            );
        }

        if (year < CalendarLogic.BS_EPOCH_YEAR || year > CalendarLogic.BS_MAX_YEAR) {
            return GotoResult.invalid(
                nepali
                    ? "वर्ष 1975-2090 वि.सं. हुनुपर्छ।"
                    : "Year must be " + CalendarLogic.BS_EPOCH_YEAR + "-"
                        + CalendarLogic.BS_MAX_YEAR + " BS."
            );
        }

        if (parts.length == 1) {
            return new GotoResult(true, year, 0, 0, null);
        }

        if (month < 1 || month > 12) {
            return GotoResult.invalid(
                nepali
                    ? "महिना 1 (बैशाख) देखि 12 (चैत) सम्म हुनुपर्छ।"
                    : "Month must be 1 (Baisakh) to 12 (Chaitra)."
            );
        }

        if (parts.length == 2) {
            return new GotoResult(true, year, month, 0, null);
        }

        int days = CalendarLogic.daysInMonth(year, month);
        if (day < 1 || day > days) {
            return GotoResult.invalid(
                nepali
                    ? CalendarLogic.monthName(month, true) + " "
                        + CalendarLogic.number(year, true) + " मा "
                        + CalendarLogic.number(days, true) + " दिन छन्।"
                    : CalendarLogic.monthName(month) + " " + year + " has " + days + " days."
            );
        }
        return new GotoResult(true, year, month, day, null);
    }

    private static final class GotoResult {
        final boolean valid;
        final int year;
        final int month;
        final int day;
        final String error;

        GotoResult(boolean valid, int year, int month, int day, String error) {
            this.valid = valid;
            this.year = year;
            this.month = month;
            this.day = day;
            this.error = error;
        }

        static GotoResult invalid(String error) {
            return new GotoResult(false, 0, 0, 0, error);
        }
    }

    // ---------------------------------------------------- theme / font / size

    private Sheet openOptionsSheet(String overline, String title, String subtitle) {
        Sheet sheet = sheet(R.layout.sheet_options);
        ((TextView) sheet.content.findViewById(R.id.options_overline)).setText(overline);
        ((TextView) sheet.content.findViewById(R.id.options_title)).setText(title);
        ((TextView) sheet.content.findViewById(R.id.options_subtitle)).setText(subtitle);
        return sheet;
    }

    private void openThemeSheet() {
        Sheet sheet = openOptionsSheet(
            "APPEARANCE",
            "Theme",
            "One palette across the whole app."
        );
        LinearLayout list = sheet.content.findViewById(R.id.options_list);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Theme option : Theme.ALL) {
            View row = inflater.inflate(R.layout.item_theme_row, list, false);
            boolean active = option.key.equals(theme.key);

            tintSwatch(row.findViewById(R.id.swatch_primary), option.primary);
            tintSwatch(row.findViewById(R.id.swatch_secondary), option.secondary);
            tintSwatch(row.findViewById(R.id.swatch_warning), option.warning);
            tintSwatch(row.findViewById(R.id.swatch_error), option.error);

            ((TextView) row.findViewById(R.id.option_name)).setText(option.displayName);
            TextView subtitle = row.findViewById(R.id.option_subtitle);
            if (option.key.equals(Theme.LIGHT.key)) {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText("Not recommended \u00b7 may make you blind");
            } else {
                subtitle.setVisibility(View.GONE);
            }
            row.findViewById(R.id.option_check).setVisibility(
                active ? View.VISIBLE : View.GONE
            );

            row.setOnClickListener(view -> {
                sheet.dialog.dismiss();
                applyTheme(option);
            });
            list.addView(row);
        }

        showSheet(sheet);
    }

    private void tintSwatch(View view, int color) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(color));
    }

    private void openFontSheet() {
        Sheet sheet = openOptionsSheet("APPEARANCE", "Font", "Picks the app typeface.");
        LinearLayout list = sheet.content.findViewById(R.id.options_list);
        String current = Ui.getFont();

        addOptionRow(list, "Aa", "Inter", "App default",
            Ui.FONT_INTER.equals(current),
            Ui.typefaceFor(this, Ui.FONT_INTER, Ui.MEDIUM),
            17f,
            () -> applyFont(Ui.FONT_INTER));
        addOptionRow(list, "Aa", "System", "Uses your system font",
            Ui.FONT_SYSTEM.equals(current),
            Ui.typefaceFor(this, Ui.FONT_SYSTEM, Ui.MEDIUM),
            17f,
            () -> applyFont(Ui.FONT_SYSTEM));
        addOptionRow(list, "Aa", "Mono", "Monospaced terminal style",
            Ui.FONT_MONO.equals(current),
            Ui.typefaceFor(this, Ui.FONT_MONO, Ui.MEDIUM),
            17f,
            () -> applyFont(Ui.FONT_MONO));

        showSheet(sheet);
    }

    private void openTextSizeSheet() {
        Sheet sheet = openOptionsSheet(
            "APPEARANCE",
            "Text size",
            "Scales labels and the calendar."
        );
        LinearLayout list = sheet.content.findViewById(R.id.options_list);
        float current = prefs.getTextScale();

        addOptionRow(list, "Aa", "Small", "90%",
            Math.abs(current - 0.90f) < 0.001f,
            Ui.typefaceFor(this, Ui.getFont(), Ui.MEDIUM),
            18f * 0.90f,
            () -> applyTextScale(0.90f));
        addOptionRow(list, "Aa", "Default", "100%",
            Math.abs(current - 1.00f) < 0.001f,
            Ui.typefaceFor(this, Ui.getFont(), Ui.MEDIUM),
            18f,
            () -> applyTextScale(1.00f));
        addOptionRow(list, "Aa", "Large", "110%",
            Math.abs(current - 1.10f) < 0.001f,
            Ui.typefaceFor(this, Ui.getFont(), Ui.MEDIUM),
            18f * 1.10f,
            () -> applyTextScale(1.10f));
        addOptionRow(list, "Aa", "Extra large", "120%",
            Math.abs(current - 1.20f) < 0.001f,
            Ui.typefaceFor(this, Ui.getFont(), Ui.MEDIUM),
            18f * 1.20f,
            () -> applyTextScale(1.20f));

        showSheet(sheet);
    }

    private void openLanguageSheet() {
        Sheet sheet = openOptionsSheet(
            "APPEARANCE",
            "Language",
            "Calendar names, dates and labels."
        );
        LinearLayout list = sheet.content.findViewById(R.id.options_list);
        String current = prefs.getScript();

        addOptionRow(list, "A", "English (US)", null,
            "en".equals(current),
            Ui.typefaceFor(this, Ui.FONT_INTER, Ui.MEDIUM),
            17f,
            () -> applyScript("en"));
        addOptionRow(list, "\u0905", "नेपाली (देवनागरी)", null,
            "ne".equals(current),
            Ui.typefaceFor(this, Ui.FONT_SYSTEM, Ui.MEDIUM),
            17f,
            () -> applyScript("ne"));

        showSheet(sheet);
    }

    private void addOptionRow(LinearLayout list, CharSequence preview, String name,
                              String subtitle, boolean active, Typeface typeface,
                              float previewSizeSp, Runnable pick) {
        View row = LayoutInflater.from(this)
            .inflate(R.layout.item_option_row, list, false);
        TextView previewView = row.findViewById(R.id.option_preview);
        previewView.setText(preview);
        if (typeface != null) {
            previewView.setTypeface(typeface);
        }
        if (previewSizeSp > 0f) {
            previewView.setTextSize(TypedValue.COMPLEX_UNIT_SP, previewSizeSp);
        }
        ((TextView) row.findViewById(R.id.option_name)).setText(name);
        TextView subtitleView = row.findViewById(R.id.option_subtitle);
        if (subtitle == null || subtitle.isEmpty()) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setVisibility(View.VISIBLE);
            subtitleView.setText(subtitle);
        }
        row.findViewById(R.id.option_check).setVisibility(
            active ? View.VISIBLE : View.GONE
        );
        row.setOnClickListener(view -> pick.run());
        list.addView(row);
    }

    private String textScaleLabel(float scale) {
        if (Math.abs(scale - 0.90f) < 0.01f) {
            return "Small";
        }
        if (Math.abs(scale - 1.10f) < 0.01f) {
            return "Large";
        }
        if (Math.abs(scale - 1.20f) < 0.01f) {
            return "Extra large";
        }
        return "Default";
    }

    private void applyTheme(Theme option) {
        if (option.key.equals(theme.key)) {
            return;
        }
        prefs.setTheme(option.key);
        WidgetSupport.refreshAll(this);
        recreate();
    }

    private void applyFont(String key) {
        String normalized = Ui.normalizeFont(key);
        if (normalized.equals(Ui.getFont())) {
            return;
        }
        prefs.setFont(normalized);
        Ui.setFont(normalized);
        recreate();
    }

    private void applyTextScale(float scale) {
        float clamped = Math.max(0.85f, Math.min(1.25f, scale));
        if (Math.abs(clamped - prefs.getTextScale()) < 0.001f) {
            return;
        }
        prefs.setTextScale(clamped);
        recreate();
    }

    private void applyScript(String key) {
        String normalized = "ne".equals(key) ? "ne" : "en";
        if (normalized.equals(prefs.getScript())) {
            return;
        }
        prefs.setScript(normalized);
        nepali = "ne".equals(normalized);
        NotificationScheduler.refresh(this);
        WidgetSupport.refreshAll(this);
        recreate();
    }

    // ------------------------------------------------------- notifications

    private void openReminderDialog() {
        boolean ne = nepali;
        Sheet sheet = sheet(R.layout.sheet_reminder);
        MaterialSwitch toggle = sheet.content.findViewById(R.id.switch_reminder);
        MaterialSwitch permanent = sheet.content.findViewById(R.id.switch_permanent);
        MaterialCardView timeCard = sheet.content.findViewById(R.id.reminder_time_card);
        TextView timeValue = sheet.content.findViewById(R.id.reminder_time_value);
        MaterialButton change = sheet.content.findViewById(R.id.reminder_change);
        TextView status = sheet.content.findViewById(R.id.reminder_status);
        MaterialButton test = sheet.content.findViewById(R.id.reminder_test);
        MaterialButton done = sheet.content.findViewById(R.id.reminder_done);

        toggle.setChecked(prefs.isNotifyEnabled());
        permanent.setChecked(prefs.isPersistentNotifyEnabled());
        timeValue.setText(NotificationScheduler.timeLabel(
            this,
            prefs.getNotifyHour(),
            prefs.getNotifyMinute()
        ));
        status.setText(reminderStatus());

        toggle.setOnCheckedChangeListener((buttonView, checked) -> {
            prefs.setNotifyEnabled(checked);
            if (checked) {
                if (!NotificationScheduler.canNotify(this)) {
                    requestNotificationPermission();
                }
                NotificationScheduler.scheduleDaily(
                    this,
                    prefs.getNotifyHour(),
                    prefs.getNotifyMinute()
                );
                Toast.makeText(this, "Daily reminder on", Toast.LENGTH_SHORT).show();
            } else {
                NotificationScheduler.cancel(this);
                Toast.makeText(this, "Daily reminder off", Toast.LENGTH_SHORT).show();
            }
            status.setText(reminderStatus());
            refreshSummaries();
        });

        View.OnClickListener pickTime = view -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(prefs.isClock24Hour()
                    ? TimeFormat.CLOCK_24H
                    : TimeFormat.CLOCK_12H)
                .setHour(prefs.getNotifyHour())
                .setMinute(prefs.getNotifyMinute())
                .setTitleText(ne ? "सम्झना समय" : "Reminder time")
                .build();
            picker.addOnPositiveButtonClickListener(selected -> {
                int hour = picker.getHour();
                int minute = picker.getMinute();
                prefs.setNotifyTime(hour, minute);
                timeValue.setText(NotificationScheduler.timeLabel(this, hour, minute));
                if (prefs.isNotifyEnabled()) {
                    NotificationScheduler.scheduleDaily(this, hour, minute);
                }
                status.setText(reminderStatus());
                refreshSummaries();
            });
            picker.show(getSupportFragmentManager(), "reminder-time");
        };
        change.setOnClickListener(pickTime);
        timeCard.setOnClickListener(pickTime);

        permanent.setOnCheckedChangeListener((buttonView, checked) -> {
            prefs.setPersistentNotifyEnabled(checked);
            if (checked) {
                boolean allowed = NotificationScheduler.canNotify(this);
                if (!allowed) {
                    requestNotificationPermission();
                }
                NotificationScheduler.showPersistent(this);
                NotificationScheduler.schedulePersistentRefresh(this);
                if (allowed) {
                    AlertPlayer.play(this);
                }
                Toast.makeText(
                    this,
                    "Permanent notification on",
                    Toast.LENGTH_SHORT
                ).show();
            } else {
                NotificationScheduler.cancelPersistent(this);
                Toast.makeText(
                    this,
                    "Permanent notification off",
                    Toast.LENGTH_SHORT
                ).show();
            }
        });

        test.setOnClickListener(view -> {
            if (!NotificationScheduler.canNotify(this)) {
                requestNotificationPermission();
                Toast.makeText(
                    this,
                    "Allow notifications first",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }
            boolean sent = NotificationScheduler.showToday(this);
            if (sent) {
                AlertPlayer.play(this);
            }
            Toast.makeText(
                this,
                sent ? "Notification sent" : "Notifications are disabled",
                Toast.LENGTH_SHORT
            ).show();
        });
        done.setOnClickListener(view -> sheet.dialog.dismiss());

        showSheet(sheet);
    }

    private String reminderStatus() {
        if (!prefs.isNotifyEnabled()) {
            return "Off";
        }
        return "Next: " + NotificationScheduler.nextTriggerLabel(
            this,
            prefs.getNotifyHour(),
            prefs.getNotifyMinute()
        );
    }

    // ---------------------------------------------------------------- about

    private void ensureAbout() {
        if (aboutOverlay != null || aboutHost == null) {
            return;
        }
        aboutOverlay = getLayoutInflater().inflate(
            R.layout.view_about, (ViewGroup) aboutHost, false
        );
        ((ViewGroup) aboutHost).addView(aboutOverlay);
        aboutToolbar = aboutOverlay.findViewById(R.id.about_toolbar);
        aboutScroll = aboutOverlay.findViewById(R.id.about_scroll);
        aboutList = aboutOverlay.findViewById(R.id.about_list);
        aboutFooter = aboutOverlay.findViewById(R.id.about_footer);
        Ui.applyTypography(aboutOverlay);
        aboutToolbar.setNavigationOnClickListener(view -> closeAbout());

        View.OnClickListener link = view -> {
            String url = view.getTag() instanceof String ? (String) view.getTag() : null;
            if (url != null) {
                openUrl(url);
            }
        };
        tagRow(aboutOverlay, R.id.link_github, DEV_GITHUB_URL);
        tagRow(aboutOverlay, R.id.link_x, DEV_X_URL);
        tagRow(aboutOverlay, R.id.link_bluesky, DEV_BLUESKY_URL);
        tagRow(aboutOverlay, R.id.link_mail, DEV_EMAIL_URL);
        tagRow(aboutOverlay, R.id.link_website, DEV_WEBSITE_URL);
        row(aboutOverlay, R.id.link_github, link);
        row(aboutOverlay, R.id.link_x, link);
        row(aboutOverlay, R.id.link_bluesky, link);
        row(aboutOverlay, R.id.link_mail, link);
        row(aboutOverlay, R.id.link_website, link);

        if (aboutFooter != null) {
            aboutFooter.setText("Npltz for Android \u00b7 " + VERSION);
        }
        if (lastBars != null) {
            applyAboutInsets(lastBars);
        }
    }

    private void tagRow(View parent, int id, String url) {
        View row = parent.findViewById(id);
        if (row != null) {
            row.setTag(url);
        }
    }

    private void row(View parent, int id, View.OnClickListener listener) {
        View row = parent.findViewById(id);
        if (row != null) {
            row.setOnClickListener(listener);
        }
    }

    private void openAbout() {
        openAbout(true);
    }

    private void openAbout(boolean animate) {
        if (aboutHost == null) {
            return;
        }
        ensureAbout();
        if (aboutOverlay == null) {
            return;
        }
        aboutOpen = true;
        syncChrome();
        aboutHost.setVisibility(View.VISIBLE);
        aboutOverlay.setVisibility(View.VISIBLE);
        aboutOverlay.bringToFront();
        if (animate) {
            aboutOverlay.setAlpha(0f);
            aboutOverlay.setTranslationY(dp(12));
            aboutOverlay.animate()
                .alpha(1f)
                .translationY(0f)
                .withLayer()
                .setDuration(200)
                .start();
        } else {
            aboutOverlay.setAlpha(1f);
            aboutOverlay.setTranslationY(0f);
        }
    }

    private void closeAbout() {
        if (aboutOverlay == null) {
            return;
        }
        aboutOpen = false;
        aboutOverlay.animate()
            .alpha(0f)
            .translationY(dp(12))
            .withLayer()
            .setDuration(140)
            .withEndAction(() -> {
                aboutOverlay.setVisibility(View.GONE);
                aboutOverlay.setAlpha(1f);
                aboutOverlay.setTranslationY(0f);
                if (aboutHost != null) {
                    aboutHost.setVisibility(View.GONE);
                }
                syncChrome();
            })
            .start();
    }

    // the today/search pill has 6dp elevation, so it would float above any
    // overlay screen. it is bottom-anchored and nothing depends on its bounds,
    // so hiding it causes no reflow. it slides in and out instead of popping,
    // so coming back from settings/about never looks like a jump.
    private void syncChrome() {
        if (bottomActions == null) {
            return;
        }
        if (settingsOpen || aboutOpen) {
            hideBottomBar();
        } else {
            showBottomBar();
        }
    }

    private void hideBottomBar() {
        if (bottomActions.getVisibility() != View.VISIBLE) {
            return;
        }
        bottomActions.animate().cancel();
        bottomActions.animate()
            .alpha(0f)
            .translationY(dp(20))
            .withLayer()
            .setDuration(130)
            .withEndAction(() -> {
                if ((settingsOpen || aboutOpen)
                    && bottomActions.getVisibility() == View.VISIBLE) {
                    bottomActions.setVisibility(View.GONE);
                }
                bottomActions.setAlpha(1f);
                bottomActions.setTranslationY(0f);
            })
            .start();
    }

    private void showBottomBar() {
        if (bottomActions.getVisibility() == View.VISIBLE) {
            return;
        }
        bottomActions.animate().cancel();
        bottomActions.setAlpha(0f);
        bottomActions.setTranslationY(dp(20));
        bottomActions.setVisibility(View.VISIBLE);
        bottomActions.animate()
            .alpha(1f)
            .translationY(0f)
            .withLayer()
            .setDuration(160)
            .start();
    }

    private void applyAboutInsets(androidx.core.graphics.Insets bars) {
        if (aboutToolbar == null) {
            return;
        }
        int baseActionBar = actionBarSize();
        ViewGroup.LayoutParams aboutToolbarParams = aboutToolbar.getLayoutParams();
        if (aboutToolbarParams != null) {
            aboutToolbarParams.height = baseActionBar + bars.top;
            aboutToolbar.setLayoutParams(aboutToolbarParams);
        }
        aboutToolbar.setPadding(
            dp(8) + bars.left,
            bars.top,
            dp(8) + bars.right,
            0
        );
        aboutList.setPadding(
            dp(20) + bars.left,
            dp(8),
            dp(20) + bars.right,
            0
        );
        aboutScroll.setPadding(
            0,
            0,
            0,
            dp(32) + bars.bottom
        );
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (RuntimeException exception) {
            Toast.makeText(this, "No browser available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openBugReportMail() {
        String subject = "[Npltz Android] Bug report";
        String body = "Description:\n(what happened?)\n\n\n"
            + "Steps to reproduce:\n1. \n2. \n\n\n"
            + "Expected behaviour:\n(what did you expect to happen?)\n\n\n"
            + "Actual behaviour:\n(what actually happened?)\n\n\n"
            + "--------------------------------\n"
            + "App version: Npltz for Android " + VERSION + "\n"
            + "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
            + "Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")\n";
        Uri uri = Uri.parse(DEV_EMAIL_URL)
            .buildUpon()
            .appendQueryParameter("subject", subject)
            .appendQueryParameter("body", body)
            .build();
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (RuntimeException exception) {
            Toast.makeText(this, "No mail app available", Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------------------- windowing

    private void applyWindowStyle() {
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(theme.bg));
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        WindowCompat.setDecorFitsSystemWindows(window, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(attributes);
        }

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
            window,
            window.getDecorView()
        );
        controller.setAppearanceLightStatusBars(theme.light);
        controller.setAppearanceLightNavigationBars(theme.light);
    }

    private androidx.core.graphics.Insets lastBars;
    private androidx.core.graphics.Insets appliedBars;
    private boolean appliedLandscape;

    private void applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
            );
            lastBars = bars;
            boolean landscape = isLandscape();
            // insets get re-dispatched whenever a dialog opens or closes, focus
            // changes, etc. re-applying identical values would force the top
            // and bottom bars to relayout and visibly jump, so bail out early.
            if (appliedBars != null
                && appliedBars.equals(bars)
                && appliedLandscape == landscape) {
                return insets;
            }
            appliedBars = bars;
            appliedLandscape = landscape;
            int page = dp(landscape ? 16 : 20);

            int baseActionBar = actionBarSize();
            ViewGroup.LayoutParams toolbarParams = toolbar.getLayoutParams();
            if (toolbarParams != null) {
                toolbarParams.height = baseActionBar + bars.top;
                toolbar.setLayoutParams(toolbarParams);
            }
            toolbar.setPadding(
                dp(8) + bars.left,
                bars.top,
                dp(8) + bars.right,
                0
            );

            homeContent.setPadding(
                page + bars.left,
                dp(landscape ? 2 : 4),
                page + bars.right,
                0
            );
            homeScroll.setPadding(0, 0, 0, 0);
            ViewGroup.LayoutParams scrollParams = homeScroll.getLayoutParams();
            if (scrollParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams margin = (ViewGroup.MarginLayoutParams) scrollParams;
                margin.bottomMargin = dp(landscape ? 76 : 80) + bars.bottom;
                homeScroll.setLayoutParams(margin);
            }

            if (settingsOverlay != null) {
                applySettingsInsets(bars);
            }
            if (aboutOverlay != null) {
                applyAboutInsets(bars);
            }

            ViewGroup.LayoutParams raw = bottomActions.getLayoutParams();
            if (raw instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) raw;
                params.leftMargin = dp(16) + bars.left;
                params.rightMargin = dp(16) + bars.right;
                params.bottomMargin = dp(16) + bars.bottom;
                bottomActions.setLayoutParams(params);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(rootView);
    }

    private void applySettingsInsets(androidx.core.graphics.Insets bars) {
        if (settingsToolbar == null) {
            return;
        }
        int baseActionBar = actionBarSize();
        ViewGroup.LayoutParams settingsToolbarParams =
            settingsToolbar.getLayoutParams();
        if (settingsToolbarParams != null) {
            settingsToolbarParams.height = baseActionBar + bars.top;
            settingsToolbar.setLayoutParams(settingsToolbarParams);
        }
        settingsToolbar.setPadding(
            dp(8) + bars.left,
            bars.top,
            dp(8) + bars.right,
            0
        );
        settingsList.setPadding(
            dp(20) + bars.left,
            dp(8),
            dp(20) + bars.right,
            0
        );
        settingsScroll.setPadding(
            0,
            0,
            0,
            dp(32) + bars.bottom
        );
    }

    private int actionBarSize() {
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(
            androidx.appcompat.R.attr.actionBarSize,
            value,
            true
        );
        return TypedValue.complexToDimensionPixelSize(
            value.data,
            getResources().getDisplayMetrics()
        );
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[] {Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_NOTIFICATIONS
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_NOTIFICATIONS) {
            return;
        }
        boolean granted = grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted && prefs.isNotifyEnabled()) {
            NotificationScheduler.scheduleDaily(
                this,
                prefs.getNotifyHour(),
                prefs.getNotifyMinute()
            );
        }
        if (granted && prefs.isPersistentNotifyEnabled()) {
            NotificationScheduler.showPersistent(this);
            NotificationScheduler.schedulePersistentRefresh(this);
        }
        if (granted) {
            EventNotifier.rescheduleAll(this);
        }
        Toast.makeText(
            this,
            granted ? "Notifications allowed" : "Notifications stay off",
            Toast.LENGTH_SHORT
        ).show();
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation
            == Configuration.ORIENTATION_LANDSCAPE;
    }

    private int dp(float value) {
        return Ui.dp(this, value);
    }
}
