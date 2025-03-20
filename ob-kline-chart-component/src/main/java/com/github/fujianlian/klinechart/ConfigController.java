package com.github.fujianlian.klinechart;

import com.github.fujianlian.klinechart.entity.Indicators;
import com.github.fujianlian.klinechart.entity.LocalValues;

import java.util.HashMap;
import java.util.Map;

public class ConfigController {
    private static volatile ConfigController sInstance = null;
    private Map<String, Indicators> mIndicatorsMap = new HashMap<>();
    private LocalValues mLocalValues;
    private LocalValuesProvider mLocalValuesProvider;
    private long mDiffServerTime = 0;

    public static Indicators getIndicators(String key) {
        return getInstance().getInnerIndicators(key);
    }

    public static ConfigController getInstance() {
        if (sInstance == null) {
            synchronized (ConfigController.class) {
                if (sInstance == null) {
                    sInstance = new ConfigController();
                }
            }
        }
        return sInstance;
    }

    private ConfigController() {

    }

    public void setIndicatorsMap(Map<String, Indicators> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        mIndicatorsMap = map;
    }

    private Indicators getInnerIndicators(String key) {
        return mIndicatorsMap.get(key);
    }

    public LocalValues getLocalValues() {
        if (mLocalValues == null && mLocalValuesProvider != null) {
            return mLocalValuesProvider.provideLocalValues();
        }
        return mLocalValues;
    }

    public void setLocalValues(LocalValues localValues) {
        this.mLocalValues = localValues;
    }

    public void setLocalValuesProvider(LocalValuesProvider localValuesProvider) {
        this.mLocalValuesProvider = localValuesProvider;
    }

    public interface LocalValuesProvider {
        LocalValues provideLocalValues();
    }

    public void setDiffServerTime(long diffServerTime) {
        this.mDiffServerTime = diffServerTime;
    }

    public long getDiffServerTime() {
        return mDiffServerTime;
    }
}
