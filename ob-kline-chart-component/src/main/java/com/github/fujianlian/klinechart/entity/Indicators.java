package com.github.fujianlian.klinechart.entity;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Indicators {
    private List<Integer> calcParams;
    private List<MAIndicator> items;

    public List<Integer> getCalcParams() {
        return calcParams;
    }

    private double accelerationFactor;
    private double maxAccelerationFactor;

    public void setCalcParams(List<Integer> calcParams) {
        this.calcParams = calcParams;
    }

    public List<MAIndicator> getItems() {
        List<MAIndicator> list = new ArrayList<>();
        if (items != null) {
            for (MAIndicator ma : items) {
                if (ma.isEnable() && isCorrectCalcParam(ma.getValue())) {
                    list.add(ma);
                }
            }
        }
        return list;
    }

    private boolean isCorrectCalcParam(String value) {
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        try {
            return Integer.parseInt(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void setItems(List<MAIndicator> items) {
        this.items = items;
    }

    public double getAccelerationFactor() {
        return accelerationFactor;
    }

    public void setAccelerationFactor(double accelerationFactor) {
        this.accelerationFactor = accelerationFactor;
    }

    public double getMaxAccelerationFactor() {
        return maxAccelerationFactor;
    }

    public void setMaxAccelerationFactor(double maxAccelerationFactor) {
        this.maxAccelerationFactor = maxAccelerationFactor;
    }
}
