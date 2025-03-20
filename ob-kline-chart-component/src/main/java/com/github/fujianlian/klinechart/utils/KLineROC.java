package com.github.fujianlian.klinechart.utils;

public class KLineROC {

    public static double[][] roc(double[] close, int period, int maPeriod) {
        int len = close.length;
        double[] roc = new double[len];
        double[] maroc = new double[len];
        double sum = 0;
        for (int i = period; i < len; i++) {
            double change = close[i] - close[i - period];
            roc[i] = change / close[i - period] * 100;

            sum += roc[i];
            if (i >= period + maPeriod) {
                maroc[i] = sum / maPeriod;
                sum -= roc[i - maPeriod];
            }
        }
        return new double[][]{roc, maroc};
    }

}