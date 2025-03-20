package com.github.fujianlian.klinechart.entity;

public enum DrawType {

    //趋势线
    TREND_LINE("TrendLine", 2),

    //水平线
    HORIZONTAL_LINE("HorizontalLine", 1),

    //射线
    RAY("Ray", 2),

    //垂直线
    VERTICAL_LINE("VerticalLine", 2),

    //价格线
    PRICE_LINE("PriceLine", 1),

    //水平通道：由三点确认，两端开口的平行四边形区域
    PARALLEL("Parallel", 3),

    //矩形
    RECTANGLE("Rectangle", 2),

    //平行四边形
    PARALLELOGRAM("Parallelogram", 3),

    //斐波那契数列
    FIBONACCI("Fibonacci", 2),

    //三浪
    THREE_WAVES("ThreeWaves", 4),

    //五浪
    FIVE_WAVES("FiveWaves", 6);

    private String type;
    private int pointSize;

    DrawType(String type, int pointSize) {
        this.type = type;
        this.pointSize = pointSize;
    }

    public String getType() {
        return type;
    }

    public int getPointSize() {
        return pointSize;
    }
}
