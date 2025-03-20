package com.github.fujianlian.klinechart.entity;

import androidx.annotation.NonNull;

public class MAIndicator implements Cloneable {

    private boolean isEnable;
    private String value;
    private String width;
    private String color;
    private double calcResult;
    private String prefix;
    private boolean isNaValue = false;

    public MAIndicator(boolean isEnable, String value, String width, String color) {
        this.isEnable = isEnable;
        this.value = value;
        this.width = width;
        this.color = color;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getCalcResult() {
        return calcResult;
    }

    public void setCalcResult(double calcResult) {
        this.calcResult = calcResult;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isNaValue() {
        return isNaValue;
    }

    public void setNaValue(boolean naValue) {
        isNaValue = naValue;
    }

    public float getPaintWidth() {
        switch (width) {
            case Width.MEDIUM:
                return 1.5F;
            case Width.BOLD:
                return 2.0F;
            default:
                return 1.0F;
        }
    }

    public static class Width {
        public static final String NORMAL = "normal";
        public static final String MEDIUM = "medium";
        public static final String BOLD = "bold";
    }

    @NonNull
    @Override
    public MAIndicator clone() {
        return new MAIndicator(isEnable, value, width, color);
    }

}
