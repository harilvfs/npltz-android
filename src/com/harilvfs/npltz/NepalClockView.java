// shows the live nepal standard time clock.
package com.harilvfs.npltz;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class NepalClockView extends LinearLayout {
    private static final ZoneId NEPAL = ZoneId.of("Asia/Kathmandu");
    private static final long MIN_TICK_DELAY = 30L;
    private static final DateTimeFormatter H24 = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US);
    private static final DateTimeFormatter H12 = DateTimeFormatter.ofPattern("hh:mm:ss", Locale.US);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TextView timeView;
    private final TextView periodView;

    private boolean is24Hour;
    private boolean nepali;
    private boolean running;
    private String lastLabel = "";

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            render();
            if (!running) {
                return;
            }
            long delay = 1000L - (System.currentTimeMillis() % 1000L);
            handler.postDelayed(this, Math.max(MIN_TICK_DELAY, delay));
        }
    };

    public NepalClockView(Context context) {
        this(context, null);
    }

    public NepalClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_nepal_clock, this, true);
        timeView = findViewById(R.id.clock_time);
        periodView = findViewById(R.id.clock_period);
        applyTypefaces();
    }

    public void configure(boolean is24Hour, boolean nepali) {
        this.is24Hour = is24Hour;
        this.nepali = nepali;
        render();
    }

    private void applyTypefaces() {
        timeView.setTypeface(Ui.typeface(Ui.MEDIUM));
        periodView.setTypeface(Ui.typeface(Ui.MEDIUM));
    }

    void start() {
        if (running) {
            return;
        }
        running = true;
        render();
        long delay = 1000L - (System.currentTimeMillis() % 1000L);
        handler.postDelayed(ticker, Math.max(MIN_TICK_DELAY, delay));
    }

    void stop() {
        running = false;
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    private void render() {
        ZonedDateTime now = ZonedDateTime.now(NEPAL);
        String clock = now.format(is24Hour ? H24 : H12);
        String period = "";
        if (is24Hour) {
            periodView.setVisibility(GONE);
        } else {
            period = now.getHour() >= 12
                ? (nepali ? "\u0905\u092a\u0930\u093e\u0939\u094d\u0928" : "PM")
                : (nepali ? "\u092a\u0942\u0930\u094d\u0935\u093e\u0939\u094d\u0928" : "AM");
            periodView.setVisibility(VISIBLE);
        }

        String label = (nepali ? "\u0928\u0947\u092a\u093e\u0932 \u0938\u092e\u092f " : "Nepal time ")
            + clock + (period.isEmpty() ? "" : " " + period);
        if (label.equals(lastLabel)) {
            return;
        }
        lastLabel = label;
        timeView.setText(nepali ? CalendarLogic.toDevanagariDigits(clock) : clock);
        if (!is24Hour) {
            periodView.setText(period);
        }
        setContentDescription(label);
    }
}
