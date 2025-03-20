package com.github.fujianlian.klinechart.entity;

import com.github.fujianlian.klinechart.base.IValueFormatter;

import java.util.List;

public class KChartConfig {
    private IValueFormatter formatter;

    //是否为分时线
    private boolean isTimeLine;

    //主图MA/EMA/BOLL
    private List<String> mainNames;

    //子图
    private List<String> subNames;

    private boolean showSubChart;

    private long timeInterval;

    private boolean showCustomDraw;

    private String interval;

    private boolean isFutures;

    public KChartConfig(IValueFormatter formatter, boolean isTimeLine,
                        List<String> mainNames, List<String> subNames, boolean showSubChart,
                        long timeInterval, boolean showCustomDraw, String interval,
                        boolean isFutures
    ) {
        this.formatter = formatter;
        this.isTimeLine = isTimeLine;
        this.mainNames = mainNames;
        this.subNames = subNames;
        this.showSubChart = showSubChart;
        this.timeInterval = timeInterval;
        this.showCustomDraw = showCustomDraw;
        this.interval = interval;
        this.isFutures = isFutures;
    }

    public IValueFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(IValueFormatter formatter) {
        this.formatter = formatter;
    }

    public boolean isTimeLine() {
        return isTimeLine;
    }

    public void setTimeLine(boolean timeLine) {
        isTimeLine = timeLine;
    }

    public List<String> getMainNames() {
        return mainNames;
    }

    public void setMainNames(List<String> mainNames) {
        this.mainNames = mainNames;
    }

    public List<String> getSubNames() {
        return subNames;
    }

    public void setSubNames(List<String> subNames) {
        this.subNames = subNames;
    }

    public boolean isShowSubChart() {
        return showSubChart;
    }

    public void setShowSubChart(boolean showSubChart) {
        this.showSubChart = showSubChart;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public String getInterval() {
        return interval;
    }

    public boolean isShowCustomDraw() {
        return showCustomDraw;
    }

    public boolean isFutures() {
        return isFutures;
    }
}
