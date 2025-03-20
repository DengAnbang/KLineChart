package com.github.fujianlian.klinechart.utils;

import androidx.annotation.NonNull;

import com.github.fujianlian.klinechart.ConfigController;
import com.github.fujianlian.klinechart.KChartConstant;
import com.github.fujianlian.klinechart.entity.Indicators;

import java.util.List;


public class CalcParamsUtil {
    public static String getCalcParamsDisplayText(String name, String... calcParams) {
        if (calcParams == null || calcParams.length == 0) {
            return name;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name).append("(");

        for (int i = 0; i < calcParams.length; i++) {
            builder.append(calcParams[i]);
            if (i != calcParams.length - 1) {
                builder.append(",");
            }
        }

        builder.append(")");

        return builder.toString();
    }

    public static double[] getSARAccelerationFactor() {
        double[] periods = new double[]{
                KChartConstant.SARCalcParams.ACCELERATION_FACTOR,
                KChartConstant.SARCalcParams.MAX_ACCELERATION_FACTOR,
        };
        Indicators sar = ConfigController.getIndicators(KChartConstant.MainConfigName.SAR);
        if (sar == null) {
            return periods;
        }
        double accelerationFactor = sar.getAccelerationFactor();
        double maxAccelerationFactor = sar.getMaxAccelerationFactor();
        return new double[]{accelerationFactor, maxAccelerationFactor};
    }

    public static int[] getROCPeriod() {
        int[] periods = new int[]{
                KChartConstant.DefaultCalcParams.ROC_PERIOD,
                KChartConstant.DefaultCalcParams.ROC_MA_PERIOD,
        };
        Indicators indicators = ConfigController.getIndicators(KChartConstant.Sub.ROC);
        if (indicators == null) {
            return periods;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.ROC) {
            return periods;
        }
        periods[0] = calcParams.get(0);
        periods[1] = calcParams.get(1);
        return periods;
    }

    public static int getOBVPeriod() {
        int period = KChartConstant.DefaultCalcParams.OBV_MA_PERIOD;
        Indicators indicators = ConfigController.getIndicators(KChartConstant.Sub.OBV);
        if (indicators == null) {
            return period;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (KChartListUtils.isEmpty(calcParams)) {
            return period;
        }
        return calcParams.get(0);
    }

    public static int getCCIPeriod() {
        int period = KChartConstant.DefaultCalcParams.CCI_PERIOD;
        Indicators indicators = ConfigController.getIndicators(KChartConstant.Sub.CCI);
        if (indicators == null) {
            return period;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (KChartListUtils.isEmpty(calcParams)) {
            return period;
        }
        return calcParams.get(0);
    }

    public static int getTRIXPeriod() {
        int period = KChartConstant.TRIXDefaultCalcParams.TRIX;
        Indicators indicators = ConfigController.getIndicators(KChartConstant.Sub.TRIX);
        if (indicators == null) {
            return period;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (KChartListUtils.isEmpty(calcParams)) {
            return period;
        }
        return calcParams.get(0);
    }

    public static int[] getStochRSIParams() {
        int[] defaultParams = new int[]{
                KChartConstant.StochRSIDefaultCalcParams.RSI_PERIOD,
                KChartConstant.StochRSIDefaultCalcParams.STOCH_PERIOD,
                KChartConstant.StochRSIDefaultCalcParams.K_PERIOD,
                KChartConstant.StochRSIDefaultCalcParams.D_PERIOD
        };
        Indicators indicators = ConfigController.getIndicators(KChartConstant.Sub.STOCHRSI);
        if (indicators == null) {
            return defaultParams;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.STOCHRSI) {
            return defaultParams;
        }
        return new int[]{
                calcParams.get(0),
                calcParams.get(1),
                calcParams.get(2),
                calcParams.get(3)
        };
    }

    @NonNull
    public static int[] getDMIPeriod() {
        int[] defaultParams = new int[]{
                KChartConstant.DMIDefaultCalcParams.DI,
                KChartConstant.DMIDefaultCalcParams.ADX,
        };
        Indicators indicators = ConfigController.getIndicators(KChartConstant.Sub.DMI);
        if (indicators == null) {
            return defaultParams;
        }
        List<Integer> calcParams = indicators.getCalcParams();
        if (calcParams == null || calcParams.size() < KChartConstant.IndicatorParamsSize.DMI) {
            return defaultParams;
        }
        return new int[]{
                calcParams.get(0),
                calcParams.get(1)
        };
    }
}
