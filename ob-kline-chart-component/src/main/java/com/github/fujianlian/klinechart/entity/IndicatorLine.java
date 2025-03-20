package com.github.fujianlian.klinechart.entity;


import android.graphics.Path;

import androidx.annotation.NonNull;

public class IndicatorLine {
    private Path path;
    private String color;
    private float width;
    private boolean isFirst = true;


    @NonNull
    public Path getPath() {
        if (path == null) {
            path = new Path();
        }
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }
}
