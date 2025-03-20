package com.github.fujianlian.klinechart.utils;

import android.annotation.SuppressLint;

import com.github.fujianlian.klinechart.entity.TimeQuantum;


import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 时间工具类
 * Created by tifezh on 2016/4/27.
 */
public class DateUtil {

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    public static SimpleDateFormat longTimeFormat = getSimpleDateFormat("yyyy-MM-dd HH:mm");
    public static SimpleDateFormat shortTimeFormat = getSimpleDateFormat("HH:mm");
    public static SimpleDateFormat DateFormat = getSimpleDateFormat("yyyy/MM/dd");

    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat getSimpleDateFormat(String pattern) {
        if (UiUtils.isRtl()) {
            return new SimpleDateFormat(pattern, Locale.ENGLISH);
        }
        return new SimpleDateFormat(pattern);
    }

    /**
     * formatCountdownTime
     *
     * @param mills                : mills second
     * @param needFormatDateNumber 是否補零</p>
     *                             {@code ture}: return format {0x,0x,0x,0x}; </p>
     *                             {@code false} : return format {x,x,x,x}</p>
     * @return return String[] { days, hour, min, s }
     */
    public static String[] formatCountdownTime(long mills, boolean needFormatDateNumber) {
        long day = mills / DAY;

        mills = mills % DAY;
        long hour = mills / HOUR;

        mills = mills % HOUR;
        long minute = mills / MINUTE;

        mills = mills % MINUTE;
        long second = mills / SECOND;

        String strDays = String.valueOf(day);
        String strHours = String.valueOf(hour);
        String strMinutes = String.valueOf(minute);
        String strSeconds = String.valueOf(second);

        if (needFormatDateNumber) {
            strDays = formatDateNumber(day);
            strHours = formatDateNumber(hour);
            strMinutes = formatDateNumber(minute);
            strSeconds = formatDateNumber(second);
        }

        return new String[]{strDays, strHours, strMinutes, strSeconds};
    }

    public static String formatDateNumber(long l) {
        if (l < 10) {
            return "0" + l;
        } else {
            return String.valueOf(l);
        }
    }

    /**
     * >1Hour && <= 1Day的周期
     *
     * @param interval k线周期
     * @return 是否符合
     */
    private static boolean withInOneDay(String interval) {
        return TimeQuantum.HOUR2.getInterval().equals(interval)
                || TimeQuantum.HOUR4.getInterval().equals(interval)
                || TimeQuantum.HOUR6.getInterval().equals(interval)
                || TimeQuantum.HOUR8.getInterval().equals(interval)
                || TimeQuantum.HOUR12.getInterval().equals(interval)
                || TimeQuantum.DAY1.getInterval().equals(interval);
    }

    /**
     * >1Day && <= 1Week的周期
     *
     * @param interval k线周期
     * @return 是否符合
     */
    private static boolean withInOneWeek(String interval) {
        return TimeQuantum.DAY2.getInterval().equals(interval)
                || TimeQuantum.DAY3.getInterval().equals(interval)
                || TimeQuantum.DAY5.getInterval().equals(interval)
                || TimeQuantum.WEEK1.getInterval().equals(interval);
    }


    private static boolean withInOnMonth(String interval) {
        return TimeQuantum.MONTH1.getInterval().equals(interval);
    }

    public static String getKLineCountdownDisplayText(String interval, String[] date) {
        if (withInOneDay(interval)) {
            return contact(date[1], ":", date[2], ":" + date[3]);
        } else if (withInOneWeek(interval) || withInOnMonth(interval)) {
            if (isLessThanHour1(date[0], date[1])) {
                return contact(date[2], ":", date[3]);
            }
            return contact(date[0], "D:", date[1], "H");
        } else {
            return contact(date[2], ":", date[3]);
        }
    }

    private static boolean isLessThanHour1(String day, String hour) {
        try {
            return Double.parseDouble(day) <= 0 && Double.parseDouble(hour) <= 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String contact(String... args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String temp : args) {
            builder.append(temp);
        }
        return builder.toString();
    }
}
