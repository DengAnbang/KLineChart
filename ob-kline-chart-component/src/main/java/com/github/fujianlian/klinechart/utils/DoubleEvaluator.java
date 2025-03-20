package com.github.fujianlian.klinechart.utils;

import android.animation.TypeEvaluator;

public class DoubleEvaluator implements TypeEvaluator<Double> {

    @Override
    public Double evaluate(float fraction, Double startValue, Double endValue) {
        return startValue + fraction * (endValue - startValue);
    }
}
