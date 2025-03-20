package com.github.fujianlian.klinechart.entity;

public enum TimeQuantum {
    MIN("Min"),
    SECOND1("Second1"),
    MIN1("Min1"),
    MIN3("Min3"),
    MIN5("Min5"),
    MIN10("Min10"),
    MIN15("Min15"),
    MIN30("Min30"),
    HOUR1("Min60"),
    HOUR2("Hour2"),
    HOUR4("Hour4"),
    HOUR6("Hour6"),
    HOUR8("Hour8"),
    HOUR12("Hour12"),
    DAY1("Day1"),
    DAY2("Day2"),
    DAY3("Day3"),
    DAY5("Day5"),
    WEEK1("Week1"),
    MONTH1("Month1");

    private final String interval;

    TimeQuantum(String interval) {
        this.interval = interval;
    }

    public String getInterval() {
        return interval;
    }
}