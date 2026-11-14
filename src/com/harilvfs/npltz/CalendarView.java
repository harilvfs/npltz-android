// draws and handles the month and year calendar.
package com.harilvfs.npltz;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CalendarView extends View {
    public interface Host {

        void onCalendarChanged();

        void onDaySelected(int year, int month, int day);

        boolean hasEvents(int year, int month, int day);
    }

    public static final int MODE_MONTH = 0;
    public static final int MODE_YEAR = 1;

    private final Host host;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint.FontMetrics fontMetrics = new Paint.FontMetrics();
    private final Path starPath = new Path();
    private final Path heartPath = new Path();
    private final Typeface sans;
    private final Typeface sansMedium;
    private final Typeface sansBold;
    private final boolean nepali;

    private final List<DayHit> dayHits = new ArrayList<>();
    private final List<MonthHit> monthHits = new ArrayList<>();

    private final GestureDetector gestures;

    private Theme theme;
    private int mode = MODE_MONTH;
    private boolean compact;
    private int viewYear = CalendarLogic.BS_EPOCH_YEAR;
    private int viewMonth = 1;
    private int selectedDay = -1;
    private LocalDate todayAd;
    private NepaliDate todayBs;

    // ad day-of-month for the visible bs month, computed once per month
    // instead of allocating a LocalDate for every cell on every draw pass.
    private final int[] adDayCache = new int[32];
    private int adCacheYear = -1;
    private int adCacheMonth = -1;

    private float density;
    private float scaledDensity;

    private int pressedDay = -1;
    private int pressedMonth = -1;
    private float pressedX;
    private float pressedY;
    private float downX;
    private float downY;
    private float touchSlop;
    private boolean flingHandled;
    private boolean horizontalSwipe;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshToday();
            handler.postDelayed(this, 30000L);
        }
    };

    public CalendarView(Context context, Prefs prefs, Host host) {
        super(context);
        this.host = host;
        this.theme = Theme.byKey(prefs.getTheme());
        this.nepali = prefs.isNepali();
        this.density = getResources().getDisplayMetrics().density;
        this.scaledDensity = getResources().getDisplayMetrics().scaledDensity;

        if (nepali) {
            sans = Typeface.create("sans-serif", Typeface.NORMAL);
            sansMedium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            sansBold = Typeface.create("sans-serif", Typeface.BOLD);
        } else {
            sans = Ui.typeface(Ui.NORMAL);
            sansMedium = Ui.typeface(Ui.MEDIUM);
            sansBold = Ui.typeface(Ui.BOLD);
        }
        todayAd = LocalDate.now();
        todayBs = CalendarLogic.adToBs(todayAd);
        if (todayBs != null) {
            viewYear = todayBs.year;
            viewMonth = todayBs.month;
            selectedDay = todayBs.day;
        }

        setPadding(dp(8), dp(6), dp(8), dp(10));
        setClickable(true);
        setFocusable(true);
        touchSlop = android.view.ViewConfiguration.get(context).getScaledTouchSlop();
        gestures = new GestureDetector(context, new GestureListener());
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        invalidate();
    }

    public void setCompact(boolean compact) {
        if (this.compact == compact) {
            return;
        }
        this.compact = compact;
        if (compact) {
            setPadding(dp(4), dp(2), dp(4), dp(4));
        } else {
            setPadding(dp(8), dp(6), dp(8), dp(10));
        }
        requestLayout();
        invalidate();
    }

    private float headerHeight() {
        return dp(compact ? 14 : 30);
    }

    private float cellHeight() {
        return dp(compact ? 24 : 60);
    }

    public int getMode() {
        return mode;
    }

    public int getDesiredHeight() {
        int width = getWidth() > 0 ? getWidth() : dp(328);
        return mode == MODE_YEAR ? yearHeight(width) : monthHeight();
    }

    public int getViewYear() {
        return viewYear;
    }

    public int getViewMonth() {
        return viewMonth;
    }

    public int getSelectedDay() {
        return selectedDay;
    }

    public NepaliDate getTodayBs() {
        return todayBs;
    }

    // restores the view to a saved position without playing the entry
    // animations. used after an activity recreation (theme, font, text size,
    // language or rotation).
    public void restoreState(int year, int month, int day, int newMode) {
        if (year >= CalendarLogic.BS_EPOCH_YEAR && year <= CalendarLogic.BS_MAX_YEAR
            && month >= 1 && month <= 12) {
            viewYear = year;
            viewMonth = month;
        }
        mode = newMode == MODE_YEAR ? MODE_YEAR : MODE_MONTH;
        if (day > 0 && CalendarLogic.isValidBsDate(viewYear, viewMonth, day)) {
            selectedDay = day;
        } else {
            selectedDay = selectedForMonth(viewYear, viewMonth);
        }
        requestLayout();
        invalidate();
        notifyChanged();
    }

    public void setMode(int newMode) {
        int next = newMode == MODE_YEAR ? MODE_YEAR : MODE_MONTH;
        if (next == mode) {
            return;
        }
        mode = next;
        if (mode == MODE_MONTH) {
            selectedDay = selectedForMonth(viewYear, viewMonth);
        }
        requestLayout();
        invalidate();
        notifyChanged();
        animateModeIn();
    }

    public void showMonth() {
        setMode(MODE_MONTH);
    }

    public void jumpToToday() {
        jumpToToday(true);
    }

    public void jumpToToday(boolean animate) {
        if (todayBs != null) {
            viewYear = todayBs.year;
            viewMonth = todayBs.month;
            selectedDay = todayBs.day;
        }
        mode = MODE_MONTH;
        requestLayout();
        invalidate();
        notifyChanged();
        if (animate) {
            animateRefreshIn();
        }
    }

    public void goTo(int year, int month, int day) {
        if (year < CalendarLogic.BS_EPOCH_YEAR || year > CalendarLogic.BS_MAX_YEAR) {
            return;
        }
        if (month < 1 || month > 12) {
            return;
        }
        viewYear = year;
        viewMonth = month;
        mode = MODE_MONTH;
        if (day > 0 && CalendarLogic.isValidBsDate(year, month, day)) {
            selectedDay = day;
        } else {
            selectedDay = selectedForMonth(year, month);
        }
        requestLayout();
        invalidate();
        notifyChanged();
        animateRefreshIn();
    }

    public void changeStep(int delta) {
        if (mode == MODE_YEAR) {
            int year = viewYear + delta;
            if (year >= CalendarLogic.BS_EPOCH_YEAR && year <= CalendarLogic.BS_MAX_YEAR) {
                viewYear = year;
                invalidate();
                notifyChanged();
                animateStepIn(delta);
            }
            return;
        }
        int index = viewYear * 12 + (viewMonth - 1) + delta;
        int year = index / 12;
        int month = index % 12 + 1;
        if (year < CalendarLogic.BS_EPOCH_YEAR || year > CalendarLogic.BS_MAX_YEAR) {
            return;
        }
        viewYear = year;
        viewMonth = month;
        selectedDay = selectedForMonth(year, month);
        requestLayout();
        invalidate();
        notifyChanged();
        animateStepIn(delta);
    }

    public void refreshToday() {
        LocalDate now = LocalDate.now();
        if (now.equals(todayAd)) {
            return;
        }
        NepaliDate previous = todayBs;
        todayAd = now;
        todayBs = CalendarLogic.adToBs(now);
        if (previous != null && todayBs != null) {
            if (mode == MODE_MONTH
                && viewYear == previous.year && viewMonth == previous.month) {
                viewYear = todayBs.year;
                viewMonth = todayBs.month;
                selectedDay = todayBs.day;
            } else if (mode == MODE_YEAR && viewYear == previous.year) {
                viewYear = todayBs.year;
            }
        }
        requestLayout();
        invalidate();
        notifyChanged();
        animateRefreshIn();
    }

    private void animateModeIn() {
        animate().cancel();
        setAlpha(0.55f);
        setScaleX(0.99f);
        setScaleY(0.99f);
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .withLayer()
            .setDuration(160)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void animateStepIn(int delta) {
        animate().cancel();
        float shift = (getWidth() > 0 ? getWidth() : dp(320)) * 0.045f;
        setTranslationX(delta > 0 ? shift : -shift);
        setAlpha(0.55f);
        animate()
            .translationX(0f)
            .alpha(1f)
            .withLayer()
            .setDuration(170)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void animateRefreshIn() {
        animate().cancel();
        setAlpha(0.6f);
        setScaleX(0.995f);
        setScaleY(0.995f);
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .withLayer()
            .setDuration(150)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    private void notifyChanged() {
        if (host != null) {
            host.onCalendarChanged();
        }
    }

    private int selectedForMonth(int year, int month) {
        if (todayBs != null && todayBs.year == year && todayBs.month == month) {
            return todayBs.day;
        }
        return 1;
    }

    private int adDayOfMonth(int day) {
        if (adCacheYear != viewYear || adCacheMonth != viewMonth) {
            int days = CalendarLogic.daysInMonth(viewYear, viewMonth);
            for (int d = 1; d <= Math.min(days, adDayCache.length - 1); d++) {
                LocalDate ad = CalendarLogic.bsToAd(viewYear, viewMonth, d);
                adDayCache[d] = ad == null ? -1 : ad.getDayOfMonth();
            }
            adCacheYear = viewYear;
            adCacheMonth = viewMonth;
        }
        return day >= 1 && day < adDayCache.length ? adDayCache[day] : -1;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, 30000L);
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(ticker);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        handler.removeCallbacks(ticker);
        if (visibility == VISIBLE) {
            handler.postDelayed(ticker, 30000L);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        density = getResources().getDisplayMetrics().density;
        scaledDensity = getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            width = dp(328);
        }
        int height = mode == MODE_YEAR ? yearHeight(width) : monthHeight();
        setMeasuredDimension(
            resolveSize(width, widthMeasureSpec),
            resolveSize(height, heightMeasureSpec)
        );
    }

    private int rowsFor(int year, int month) {
        int days = CalendarLogic.daysInMonth(year, month);
        int start = CalendarLogic.monthStartWeekday(year, month);
        if (days <= 0 || start < 0) {
            return 6;
        }
        return Math.max(5, (start + days + 6) / 7);
    }

    private int monthHeight() {
        return getPaddingTop() + getPaddingBottom() + (int) headerHeight()
            + rowsFor(viewYear, viewMonth) * (int) cellHeight();
    }

    private int yearHeight(int width) {
        int inner = width - getPaddingLeft() - getPaddingRight();
        int columns = inner >= dp(560) ? 4 : 3;
        int rows = (12 + columns - 1) / columns;
        return getPaddingTop() + getPaddingBottom()
            + rows * dp(100) + (rows - 1) * dp(10);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            flingHandled = false;
            horizontalSwipe = false;
            updatePressed(downX, downY);
        } else if (action == MotionEvent.ACTION_MOVE) {
            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            if (!horizontalSwipe
                && Math.abs(dx) > touchSlop
                && Math.abs(dx) > Math.abs(dy) * 1.25f) {
                horizontalSwipe = true;

                // take clear horizontal drags before the scroll view does.
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                clearPressed();
                if (!flingHandled && Math.abs(dx) >= dp(36)) {
                    flingHandled = true;
                    changeStep(dx < 0 ? 1 : -1);
                }
            }
        }
        boolean handled = gestures.onTouchEvent(event);
        if (action == MotionEvent.ACTION_UP) {
            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            if (!flingHandled
                && Math.abs(dx) > dp(32)
                && Math.abs(dx) > Math.abs(dy) * 1.25f) {
                flingHandled = true;
                changeStep(dx < 0 ? 1 : -1);
                handled = true;
            }
            if (horizontalSwipe) {
                handled = true;
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            clearPressed();
            horizontalSwipe = false;
        }
        return handled || super.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (horizontalSwipe || flingHandled) {
                return true;
            }
            handleTap(event.getX(), event.getY());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent first, MotionEvent second,
                               float velocityX, float velocityY) {
            if (first == null || second == null || flingHandled) {
                return false;
            }
            float dx = second.getX() - first.getX();
            float dy = second.getY() - first.getY();
            if (Math.abs(dx) > dp(36) && Math.abs(dx) > Math.abs(dy) * 1.25f) {
                flingHandled = true;
                changeStep(dx < 0 ? 1 : -1);
                return true;
            }
            return false;
        }
    }

    private void updatePressed(float x, float y) {
        pressedDay = -1;
        pressedMonth = -1;
        pressedX = x;
        pressedY = y;
        if (mode == MODE_MONTH) {
            for (DayHit hit : dayHits) {
                if (hit.rect.contains(x, y)) {
                    pressedDay = hit.day;
                    break;
                }
            }
        } else {
            for (MonthHit hit : monthHits) {
                if (hit.rect.contains(x, y)) {
                    pressedMonth = hit.month;
                    break;
                }
            }
        }
        if (pressedDay >= 0 || pressedMonth >= 0) {
            invalidate();
        }
    }

    private void clearPressed() {
        if (pressedDay < 0 && pressedMonth < 0) {
            return;
        }
        pressedDay = -1;
        pressedMonth = -1;
        invalidate();
    }

    private void handleTap(float x, float y) {
        if (mode == MODE_MONTH) {
            for (DayHit hit : dayHits) {
                if (hit.rect.contains(x, y)) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    selectedDay = hit.day;
                    invalidate();
                    notifyChanged();
                    if (host != null) {
                        host.onDaySelected(viewYear, viewMonth, hit.day);
                    }
                    return;
                }
            }
        } else {
            for (MonthHit hit : monthHits) {
                if (hit.rect.contains(x, y)) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    viewMonth = hit.month;
                    mode = MODE_MONTH;
                    selectedDay = selectedForMonth(viewYear, viewMonth);
                    requestLayout();
                    invalidate();
                    notifyChanged();
                    animateModeIn();
                    return;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        if (mode == MODE_YEAR) {
            drawYear(canvas);
        } else {
            drawMonth(canvas);
        }
    }

    private void drawMonth(Canvas canvas) {
        int days = CalendarLogic.daysInMonth(viewYear, viewMonth);
        int start = CalendarLogic.monthStartWeekday(viewYear, viewMonth);
        if (days <= 0 || start < 0) {
            return;
        }

        int innerLeft = getPaddingLeft();
        int innerRight = getWidth() - getPaddingRight();
        int gridTop = getPaddingTop() + (int) headerHeight();
        float cellWidth = (innerRight - innerLeft) / 7f;
        float cellHeight = cellHeight();

        drawWeekdays(canvas, innerLeft, getPaddingTop(), innerRight - innerLeft);

        int rows = rowsFor(viewYear, viewMonth);
        int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 7; col++) {
                int day = row * 7 + col - start + 1;
                if (day < 1 || day > days) {
                    continue;
                }

                float cellLeft = innerLeft + col * cellWidth;
                float cellTop = gridTop + row * cellHeight;
                DayHit hit = obtainDayHit(count++);
                hit.day = day;
                hit.rect.set(cellLeft, cellTop, cellLeft + cellWidth, cellTop + cellHeight);

                boolean isToday = todayBs != null
                    && todayBs.year == viewYear
                    && todayBs.month == viewMonth
                    && todayBs.day == day;
                boolean isSelected = selectedDay == day;

                drawDay(canvas, hit.rect, day, col, isToday, isSelected);
            }
        }
        trimHits(dayHits, count);
    }

    private DayHit obtainDayHit(int index) {
        DayHit hit;
        if (index < dayHits.size()) {
            hit = dayHits.get(index);
        } else {
            hit = new DayHit();
            dayHits.add(hit);
        }
        return hit;
    }

    private MonthHit obtainMonthHit(int index) {
        MonthHit hit;
        if (index < monthHits.size()) {
            hit = monthHits.get(index);
        } else {
            hit = new MonthHit();
            monthHits.add(hit);
        }
        return hit;
    }

    private static void trimHits(List<?> hits, int count) {
        while (hits.size() > count) {
            hits.remove(hits.size() - 1);
        }
    }

    private void drawDay(Canvas canvas, RectF rect, int day, int column, boolean isToday,
                         boolean isSelected) {
        float centerX = rect.centerX();
        float circleY = rect.centerY() - dp(compact ? 2 : 7);
        float radius = dp(compact ? 10.5f : 19);

        boolean newYear = CalendarLogic.isNewYear(viewMonth, day);
        boolean birthday = CalendarLogic.isBirthday(viewMonth, day);
        boolean special = newYear || birthday;
        int accent = newYear ? theme.warning : theme.error;

        if (special && !isSelected && !isToday) {
            fillCircle(canvas, centerX, circleY, radius, Theme.soft(accent, 0x12));
            strokeCircle(canvas, centerX, circleY, radius, Theme.soft(accent, 0x8A), 1.2f);
        }

        if (isSelected) {
            fillCircle(canvas, centerX, circleY, radius, theme.primary);
            if (pressedDay == day) {
                fillCircle(canvas, centerX, circleY, radius,
                    Theme.soft(theme.onPrimary, 0x24));
            }
        } else if (isToday) {
            strokeCircle(canvas, centerX, circleY, radius, theme.primary, 1.2f);
            if (pressedDay == day) {
                fillCircle(canvas, centerX, circleY, radius,
                    Theme.soft(theme.primary, 0x1F));
            }
        } else if (pressedDay == day) {
            fillCircle(canvas, centerX, circleY, radius, Theme.soft(theme.primary, 0x1F));
        }

        if (special && (isSelected || isToday)) {
            strokeCircle(canvas, centerX, circleY, radius + dp(2.5f),
                Theme.soft(accent, 0xB0), 1.2f);
        }

        int dayColor;
        if (isSelected) {
            dayColor = theme.onPrimary;
        } else if (isToday) {
            dayColor = theme.primary;
        } else if (special) {
            dayColor = accent;
        } else if (column == 6) {
            dayColor = theme.error;
        } else if (column == 0) {
            dayColor = theme.yellow;
        } else {
            dayColor = theme.onSurface;
        }

        int adDay = compact ? -1 : adDayOfMonth(day);
        int adColor;
        if (isToday) {
            adColor = theme.primary;
        } else if (column == 6) {
            adColor = Theme.soft(theme.error, 0xB0);
        } else if (column == 0) {
            adColor = Theme.soft(theme.yellow, 0xB0);
        } else {
            adColor = theme.onSurfaceVariant;
        }

        float daySize = compact ? 12.5f : 15f;
        String dayLabel = CalendarLogic.number(day, nepali);
        drawText(canvas, dayLabel, centerX, circleY, daySize,
            dayColor, isSelected || isToday || special ? sansBold : sansMedium,
            Paint.Align.CENTER);

        if (adDay > 0 && !compact) {
            float adSize = 10f;
            String adLabel = CalendarLogic.number(adDay, nepali);
            drawText(canvas, adLabel, centerX,
                rect.centerY() + dp(18), adSize, adColor, sans,
                Paint.Align.CENTER);
        }

        boolean hasEvent = host != null && host.hasEvents(viewYear, viewMonth, day);
        if (hasEvent) {

            float offset = radius + dp(compact ? 3f : 3.5f);
            float dotX = centerX + offset * 0.7071f;
            float dotY = Math.max(
                dp(compact ? 3f : 5f),
                circleY - offset * 0.7071f
            );
            fillCircle(canvas, dotX, dotY, dp(compact ? 1.7f : 2.2f), theme.primary);
        }

        if (special && !compact) {
            float markY = rect.centerY() + dp(27.5f);
            if (newYear) {
                drawStar(canvas, centerX, markY, dp(3.8f), accent);
            } else {
                drawHeart(canvas, centerX, markY, dp(2.9f), accent);
            }
        }
    }

    private void drawWeekdays(Canvas canvas, float left, float top, float width) {
        float cellWidth = width / 7f;
        for (int i = 0; i < 7; i++) {
            int color = i == 6
                ? theme.error
                : i == 0 ? theme.yellow : theme.onSurfaceVariant;
            String label = CalendarLogic.weekdayShort(i, nepali);
            float size = compact ? 8.5f : 10f;
            paint.setLetterSpacing(0f);
            drawText(canvas, label, left + (i + 0.5f) * cellWidth,
                top + dp(compact ? 12 : 19), size, color, sansMedium, Paint.Align.CENTER);
        }
    }

    private void drawYear(Canvas canvas) {
        int innerLeft = getPaddingLeft();
        int innerRight = getWidth() - getPaddingRight();
        int innerTop = getPaddingTop();
        int inner = innerRight - innerLeft;
        int columns = inner >= dp(560) ? 4 : 3;
        int rows = (12 + columns - 1) / columns;
        float gap = dp(10);
        float cardWidth = (inner - gap * (columns - 1)) / columns;
        float cardHeight = dp(100);

        for (int month = 1; month <= 12; month++) {
            int index = month - 1;
            int row = index / columns;
            int column = index % columns;
            float left = innerLeft + column * (cardWidth + gap);
            float top = innerTop + row * (cardHeight + gap);
            MonthHit hit = obtainMonthHit(month - 1);
            hit.month = month;
            RectF card = hit.rect;
            card.set(left, top, left + cardWidth, top + cardHeight);

            boolean current = month == viewMonth;
            int fill = current ? theme.primaryContainer : theme.surfaceContainerHigh;
            fillRound(canvas, card, dp(16), fill);
            if (pressedMonth == month) {
                fillRound(canvas, card, dp(16), Theme.soft(theme.primary, 0x1F));
            }

            int nameColor = current ? theme.onPrimaryContainer : theme.onSurface;
            String monthLabel = CalendarLogic.monthName(month, nepali);
            float nameSize = 12f;
            drawText(canvas, monthLabel, card.centerX(),
                top + dp(16), nameSize, nameColor, sansMedium, Paint.Align.CENTER);

            int start = CalendarLogic.monthStartWeekday(viewYear, month);
            int days = CalendarLogic.daysInMonth(viewYear, month);
            if (start < 0 || days <= 0) {
                continue;
            }

            int miniRows = (start + days + 6) / 7;
            float cellWidth = (cardWidth - dp(10)) / 7f;
            float gridTop = top + dp(25);
            float cellHeight = (cardHeight - dp(29)) / miniRows;
            float gridLeft = left + dp(5);
            float daySize = Math.min(7.5f, Math.max(5.5f,
                cellHeight / density * 0.62f));

            for (int day = 1; day <= days; day++) {
                int cell = start + day - 1;
                int cellRow = cell / 7;
                int cellColumn = cell % 7;
                float x = gridLeft + (cellColumn + 0.5f) * cellWidth;
                float y = gridTop + (cellRow + 0.5f) * cellHeight;

                boolean today = todayBs != null
                    && todayBs.year == viewYear
                    && todayBs.month == month
                    && todayBs.day == day;
                int color;
                if (today) {
                    color = current ? theme.onPrimaryContainer : theme.primary;
                } else if (cellColumn == 6) {
                    color = current ? Theme.soft(theme.error, 0xD0) : theme.error;
                } else if (cellColumn == 0) {
                    color = current
                        ? Theme.soft(theme.yellow, 0xD0)
                        : theme.yellow;
                } else {
                    color = current ? theme.onPrimaryContainer : theme.onSurfaceVariant;
                }
                drawText(canvas, CalendarLogic.number(day, nepali), x, y, daySize,
                    color, today ? sansBold : sans, Paint.Align.CENTER);
            }

        }
        trimHits(monthHits, 12);
    }

    private int dp(float value) {
        return Math.round(value * density);
    }

    private void drawText(Canvas canvas, String text, float x, float centerY, float sizeSp,
                          int color, Typeface typeface, Paint.Align align) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(typeface);
        paint.setTextSize(sizeSp * scaledDensity);
        paint.setColor(color);
        paint.setTextAlign(align);
        paint.getFontMetrics(fontMetrics);
        float baseline = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f;
        canvas.drawText(text, x, baseline, paint);
    }

    private void fillCircle(Canvas canvas, float cx, float cy, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(cx, cy, radius, paint);
    }

    private void strokeCircle(Canvas canvas, float cx, float cy, float radius, int color,
                              float widthDp) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(widthDp));
        paint.setColor(color);
        canvas.drawCircle(cx, cy, radius - dp(widthDp) / 2f, paint);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float outerRadius, int color) {
        starPath.reset();
        float innerRadius = outerRadius * 0.46f;
        for (int i = 0; i < 10; i++) {
            double angle = -Math.PI / 2 + i * Math.PI / 5;
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = cx + (float) Math.cos(angle) * r;
            float y = cy + (float) Math.sin(angle) * r;
            if (i == 0) {
                starPath.moveTo(x, y);
            } else {
                starPath.lineTo(x, y);
            }
        }
        starPath.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawPath(starPath, paint);
    }

    private void drawHeart(Canvas canvas, float cx, float cy, float size, int color) {
        heartPath.reset();
        heartPath.moveTo(cx, cy + size);
        heartPath.cubicTo(
            cx - size * 1.8f, cy - size * 0.4f,
            cx - size * 0.9f, cy - size * 1.4f,
            cx, cy - size * 0.55f
        );
        heartPath.cubicTo(
            cx + size * 0.9f, cy - size * 1.4f,
            cx + size * 1.8f, cy - size * 0.4f,
            cx, cy + size
        );
        heartPath.close();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawPath(heartPath, paint);
    }

    private void fillRound(Canvas canvas, RectF rect, float radius, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        float safe = Math.min(radius, Math.min(rect.width(), rect.height()) / 2f);
        canvas.drawRoundRect(rect, safe, safe, paint);
    }

    private static final class DayHit {
        int day;
        final RectF rect = new RectF();
    }

    private static final class MonthHit {
        int month;
        final RectF rect = new RectF();
    }
}
