package com.github.fujianlian.klinechart;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.github.fujianlian.klinechart.entity.TimeQuantum;

import java.util.Calendar;

public class CountdownTask extends Handler {
    private String mInterval;
    private long mStart;
    private long mCountdown;
    private final CountdownListener mCountdownListener;

    public CountdownTask(CountdownListener countdownListener) {
        super(Looper.getMainLooper());
        mCountdownListener = countdownListener;
    }

    public void begin(String interval, long timeInterval, long start) {
        if (mStart == start && interval.equals(mInterval)) {
            return;
        }
        mInterval = interval;
        mStart = start;
        removeCallbacksAndMessages(null);
        long end;
        if (TimeQuantum.MONTH1.getInterval().equals(interval)) {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(start);
            instance.add(Calendar.MONTH, 1);
            end = instance.getTimeInMillis();
        } else {
            end = start + timeInterval;
        }
        mCountdown = Math.max(0, end - getCurrentTimeMills());
        mCountdownListener.onCountdownChanged(mCountdown);
        sendEmptyMessageDelayed(0, 1000);
    }

    public void stop() {
        mStart = 0;
        mCountdown = 0;
        removeCallbacksAndMessages(null);
    }

    private long getCurrentTimeMills() {
        long serverDiffTime = ConfigController.getInstance().getDiffServerTime();
        return System.currentTimeMillis() + serverDiffTime;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        mCountdown = Math.max(0, mCountdown - 1000L);
        mCountdownListener.onCountdownChanged(mCountdown);
        sendEmptyMessageDelayed(0, 1000);
    }

    public interface CountdownListener {
        void onCountdownChanged(long countdown);
    }
}
