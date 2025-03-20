package com.github.fujianlian.klinechart;

public class KChartConstant {

    public static final String TEXT_EMPTY_STATUS = "--";
    public static final String TEXT_VALUE_SPLICE = ":";
    public static final float POINT_ARC_RADIUS = 8.0F;
    public static final float POINT_ARC_RADIUS2 = 3.5F;

    public static final int PERIOD_MIN_VALUE_OLD = 1;
    public static final int PERIOD_MAX_VALUE_OLD = 999;

    public static class IndicatorParamsSize {
        public static final int MA = 4;
        public static final int EMA = 3;
        public static final int BOLL = 1;
        public static final int MACD = 3;
        public static final int KDJ = 3;
        public static final int RSI = 3;
        public static final int WR = 3;
        public static final int ROC = 2;
        public static final int STOCHRSI = 4;
        public static final int DMI = 2;
    }

    public static class MainIndex {
        public static final int HIDE = -1;
        public static final int MA = 0;
        public static final int EMA = 1;
        public static final int BOLL = 2;
        public static final int SAR = 3;
    }

    public static class Main {
        public static final String MA = "MA";
        public static final String EMA = "EMA";
        public static final String  BOLL = "BOLL";
        public static final String SAR = "SAR";
    }

    public static class MainConfigName {
        public static final String SAR = "SAR(APP)";
    }

    public static class Boll {
        public static final String MID = "BOLL";
        public static final String UP = "UB";
        public static final String DN = "LB";
    }

    public static class Sub {
        public static final String VOL = "VOL";
        public static final String MACD = "MACD";
        public static final String KDJ = "KDJ";
        public static final String RSI = "RSI";
        public static final String WR = "WR";
        public static final String OBV = "OBV";
        public static final String STOCHRSI = "StochRSI";
        public static final String MASTOCHRSI = "MAStochRSI";
        public static final String MAOBV = "MAOBV";
        public static final String ROC = "ROC";
        public static final String MAROC = "MAROC";
        public static final String CCI = "CCI";
        public static final String TRIX = "TRIX";
        public static final String DMI = "DMI";
    }

    public static class DMIIndicatorsName {
        public static final String DI = "DI";
        public static final String PLUS_DI = "PDI";
        public static final String MINUS_DI = "MDI";
        public static final String ADX = "ADX";
        public static final String ADXR = "ADXR";
    }

    public static class SelectorStyle {
        public static final String POPUP = "POPUP";
        public static final String TOP_POPUP = "TOP_POPUP";
        public static final String NO_POPUP = "NO_POPUP";
    }

    /**
     * 默认主图顶部ma bull等文字中间的间距,单位为dp
     */
    public static final float DEFAULT_TOP_MA_OR_BULL_SPACE_DP = 12f;

    public static final String CACHE_INDICATOR_LINE_KEY_SPLICE = "_";

    public static class DefaultCalcParams {
        public static final int OBV_MA_PERIOD = 30;
        public static final int ROC_PERIOD = 12;
        public static final int ROC_MA_PERIOD = 6;
        public static final int CCI_PERIOD = 14;
    }

    public static class SARCalcParams {
        public static final double ACCELERATION_FACTOR = 0.02;
        public static final double MAX_ACCELERATION_FACTOR = 0.2;
    }

    public static class StochRSIDefaultCalcParams {
        public static final int RSI_PERIOD = 14;
        public static final int STOCH_PERIOD = 14;
        public static final int K_PERIOD = 3;
        public static final int D_PERIOD = 3;
    }

    public static class DMIDefaultCalcParams {
        public static final int DI = 14;
        public static final int ADX = 14;
        public static final int PERIOD_MIN_VALUE = 2;
        public static final int PERIOD_MAX_VALUE = 1000;
    }

    public static class TRIXDefaultCalcParams {
        public static final int TRIX = 12;
        public static final int PERIOD_MIN_VALUE = 2;
        public static final int PERIOD_MAX_VALUE = 60;
    }

}
