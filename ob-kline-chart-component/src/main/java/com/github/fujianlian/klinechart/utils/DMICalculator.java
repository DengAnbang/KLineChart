package com.github.fujianlian.klinechart.utils;

public class DMICalculator {
    public static class DMIResult {
        double[] plusDI;
        double[] minusDI;
        double[] DX;
        double[] ADX;
        double[] ADXR;

        public double[] getPlusDI() {
            return plusDI;
        }

        public void setPlusDI(double[] plusDI) {
            this.plusDI = plusDI;
        }

        public double[] getMinusDI() {
            return minusDI;
        }

        public void setMinusDI(double[] minusDI) {
            this.minusDI = minusDI;
        }

        public double[] getDX() {
            return DX;
        }

        public void setDX(double[] DX) {
            this.DX = DX;
        }

        public double[] getADX() {
            return ADX;
        }

        public void setADX(double[] ADX) {
            this.ADX = ADX;
        }

        public double[] getADXR() {
            return ADXR;
        }

        public void setADXR(double[] ADXR) {
            this.ADXR = ADXR;
        }
    }

    public static DMIResult calcDMI(double[] high, double[] low, double[] close, int period, int adxPeriod) {
        int adxStartIndex = period + adxPeriod - 1;
        int adxrStartIndex = period + adxPeriod + period - 2;
        int dataSize = high.length;

        double[] trueRange = new double[dataSize];
        double[] plusDM = new double[dataSize];
        double[] minusDM = new double[dataSize];
        double[] plusDI = new double[dataSize];
        double[] minusDI = new double[dataSize];
        double[] dx = new double[dataSize];
        double[] adx = new double[dataSize];
        double[] adxr = new double[dataSize];

        double averagePlusDM = 0;
        double averageMinusDM = 0;
        double averageTrueRange = 0;
        double averageDX = 0;
        for (int i = 1; i < dataSize; i++) {
            trueRange[i] = Math.max(
                    high[i] - low[i],
                    Math.max(Math.abs(high[i] - close[i - 1]), Math.abs(low[i] - close[i - 1]))
            );

            double upMove = high[i] - high[i - 1];
            double downMove = low[i - 1] - low[i];
            plusDM[i] = (upMove > downMove && upMove > 0) ? upMove : 0;
            minusDM[i] = (downMove > upMove && downMove > 0) ? downMove : 0;

            if (i <= period) {
                averagePlusDM += plusDM[i];
                averageMinusDM += minusDM[i];
                averageTrueRange += trueRange[i];
                if (i == period) {
                    averagePlusDM = averagePlusDM / period;
                    averageMinusDM = averageMinusDM / period;
                    averageTrueRange = averageTrueRange / period;
                }
            } else {
                averagePlusDM = calculateMovingAverage(averagePlusDM, plusDM[i], period);
                averageMinusDM = calculateMovingAverage(averageMinusDM, minusDM[i], period);
                averageTrueRange = calculateMovingAverage(averageTrueRange, trueRange[i], period);
            }

            if (i >= period) {
                plusDI[i] = 100 * averagePlusDM / averageTrueRange;
                minusDI[i] = 100 * averageMinusDM / averageTrueRange;

                dx[i] = (Math.abs(plusDI[i] - minusDI[i]) / (plusDI[i] + minusDI[i])) * 100;

                if (i <= adxStartIndex) {
                    averageDX += dx[i];
                    if (i == adxStartIndex) {
                        averageDX = averageDX / adxPeriod;
                        adx[i] = averageDX;
                    }
                } else {
                    averageDX = calculateMovingAverage(averageDX, dx[i], adxPeriod);
                    adx[i] = averageDX;
                }

                if (i >= adxrStartIndex) {
                    adxr[i] = (adx[i] + adx[i - period + 1]) / 2.0;
                }
            }
        }

        DMIResult dmiResult = new DMIResult();
        dmiResult.plusDI = plusDI;
        dmiResult.minusDI = minusDI;
        dmiResult.ADX = adx;
        dmiResult.ADXR = adxr;
        return dmiResult;
    }

    private static double calculateMovingAverage(double preValue, double curValue, int period) {
        return (preValue * (period - 1) + curValue) / period;
    }
}