package com.github.fujianlian.klinechart.entity;

import androidx.annotation.NonNull;

import com.github.fujianlian.klinechart.MarkItemType;

import java.util.HashMap;
import java.util.Map;

public class LocalValues {
    private final Map<String, String> values = new HashMap<>();

    public LocalValues(String time,
                       String open,
                       String high,
                       String low,
                       String close,
                       String change,
                       String changeRate,
                       String vol,
                       String amount,
                       String range,
                       String orders
    ) {
        values.put(MarkItemType.TIME, time);
        values.put(MarkItemType.OPEN, open);
        values.put(MarkItemType.HIGH, high);
        values.put(MarkItemType.LOW, low);
        values.put(MarkItemType.CLOSE, close);
        values.put(MarkItemType.CHANGE, change);
        values.put(MarkItemType.CHANGE_RATE, changeRate);
        values.put(MarkItemType.VOL, vol);
        values.put(MarkItemType.AMOUNT, amount);
        values.put(MarkItemType.RANGE, range);
        values.put(MarkItemType.ORDERS, orders);
    }

    @NonNull
    public String getText(String type) {
        String text = values.get(type);
        return text == null ? "" : text;
    }
}
