package com.github.fujianlian.klinechart.entity;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

public class DrawingPoint implements Cloneable {
    //用来表示x轴
    @Expose
    private long time;

    //表示y轴
    @Expose
    private float price;

    @Expose(serialize = false, deserialize = false)
    private RectF rectF;

    @Expose(serialize = false, deserialize = false)
    private RectF rectF2;

    @Expose(serialize = false, deserialize = false)
    private float x;

    @Expose(serialize = false, deserialize = false)
    private float y;

    @Expose
    private float offsetY;

    @Expose
    private boolean isUseRawX = true;

    public DrawingPoint(long time, float price) {
        this.time = time;
        this.price = price;
    }

    public DrawingPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public DrawingPoint(long time, float price, float offsetY, boolean isUseRawX) {
        this.time = time;
        this.price = price;
        this.offsetY = offsetY;
        this.isUseRawX = isUseRawX;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @NonNull
    public RectF getRectF() {
        if (rectF == null) {
            rectF = new RectF();
        }
        return rectF;
    }

    @NonNull
    public RectF getRectF2() {
        if (rectF2 == null) {
            rectF2 = new RectF();
        }
        return rectF2;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public boolean isUseRawX() {
        return isUseRawX;
    }

    public void setUseRawX(boolean useRawX) {
        isUseRawX = useRawX;
    }

    @Override
    public String toString() {
        return "DrawingPoint{" +
                "time=" + time +
                ", price=" + price +
                ", rectF=" + rectF +
                '}';
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public DrawingPoint clone() {
        return new DrawingPoint(time, price, offsetY, false);
    }
}
