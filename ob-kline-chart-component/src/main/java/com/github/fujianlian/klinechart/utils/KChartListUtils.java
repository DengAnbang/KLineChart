package com.github.fujianlian.klinechart.utils;

import androidx.annotation.Nullable;

import java.util.List;

public final class KChartListUtils {

    private KChartListUtils() {
    }

    public static boolean isEmpty(@Nullable List list) {
        return list == null || list.size() == 0;
    }
}
