package com.github.fujianlian.klinechart.base;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fujianlian.klinechart.BaseKLineChartView;


/**
 * 画图的基类 根据实体来画图形
 * Created by tifezh on 2016/6/14.
 */
public interface IChartDraw<T> {

    void setTypeface(Typeface customTypeface);

    /**
     * 需要滑动 物体draw方法
     *  @param lastPoint 上一个点
     * @param curPoint  当前点
     * @param lastX     上一个点的x坐标
     * @param curX      当前点的X坐标
     * @param canvas    canvas
     * @param view      k线图View
     * @param position  当前点的位置
     */
    void drawTranslated(@Nullable T lastPoint, @NonNull T curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, boolean isNewest, float animationPrice);


    void drawIndicatorLine(@NonNull T point, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, float x, boolean isFirst, boolean isLast);

    /**
     * 画分时线
     *
     * @param view   K线View
     * @param canvas 画布
     * @param path   分时线path
     * @param startX 分时线起点X轴
     * @param curX   分时线终点X轴
     */
    void drawTimeLine(@NonNull BaseKLineChartView view, @NonNull Canvas canvas, Path path, float startX, float curX);

    /**
     * 绘制分时线最后一个节点的呼吸灯
     *
     * @param view            K线View
     * @param canvas          画布
     * @param stopX           最后一个数据点的X轴
     * @param stopValue       最后一个数据点的Y轴
     * @param haloRadiusRatio 光晕半径系数
     */
    void drawBreathingLight(@NonNull BaseKLineChartView view, @NonNull Canvas canvas, float stopX, float stopValue, float haloRadiusRatio);

    /**
     * @param canvas
     * @param view
     * @param position 该点的位置
     * @param x        x的起始坐标
     * @param y        y的起始坐标
     */
    void drawText(@NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position, float x, float y);

    /**
     * 获取当前实体中最大的值
     *
     * @param point
     * @return
     */
    float getMaxValue(T point);

    /**
     * 获取当前实体中最小的值
     *
     * @param point
     * @return
     */
    float getMinValue(T point);

    /**
     * 获取value格式化器
     */
    IValueFormatter getValueFormatter();
}
