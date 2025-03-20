package com.github.fujianlian.klinechart.utils;

public class KLineSAR {
    public static double[] sar(double[] high,
                               double[] low,
                               double accelerationFactor,
                               double maxAccelerationFactor) {

        int length = high.length;
        double[] sar = new double[length];
        double[] trend = new double[length];
        double[] ep = new double[length];
        double acceleration = accelerationFactor;
        double maxAcceleration = maxAccelerationFactor;

        int position = -1;
        double currentHigh = 0.0;
        double currentLow = 0.0;

        for (int i = 0; i < length; i++) {
            if (i == 0) {
                trend[i] = 1;
                sar[i] = low[i];
                currentHigh = high[i];
                currentLow = low[i];
            } else {
                if (trend[i - 1] == 1) {
                    if (low[i] <= sar[i - 1]) {
                        trend[i] = -1;
                        sar[i] = currentHigh;
                        currentLow = low[i];
                        acceleration = accelerationFactor;
                        position = i - 1;
                    } else {
                        trend[i] = 1;
                        sar[i] = sar[i - 1] + acceleration * (currentHigh - sar[i - 1]);
                        if (sar[i] > low[i - 1]) {
                            sar[i] = low[i - 1];
                        }
                        if (currentHigh < high[i]) {
                            currentHigh = high[i];
                            ep[i] = currentHigh;
                            acceleration += accelerationFactor;
                            if (acceleration > maxAcceleration) {
                                acceleration = maxAcceleration;
                            }
                        } else {
                            ep[i] = currentHigh;
                        }
                    }
                } else {
                    if (high[i] >= sar[i - 1]) {
                        trend[i] = 1;
                        sar[i] = currentLow;
                        currentHigh = high[i];
                        acceleration = accelerationFactor;
                        position = i - 1;
                    } else {
                        trend[i] = -1;
                        sar[i] = sar[i - 1] + acceleration * (currentLow - sar[i - 1]);
                        if (sar[i] < high[i - 1]) {
                            sar[i] = high[i - 1];
                        }
                        if (currentLow > low[i]) {
                            currentLow = low[i];
                            ep[i] = currentLow;
                            acceleration += accelerationFactor;
                            if (acceleration > maxAcceleration) {
                                acceleration = maxAcceleration;
                            }
                        } else {
                            ep[i] = currentLow;
                        }
                    }
                }
            }
        }

        return sar;
    }
}
