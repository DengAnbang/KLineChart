package com.github.fujianlian.klinechart.utils;

import android.content.Context;
import android.util.TypedValue;

public class ThemeUtils {
    public static int getThemeColor(Context context, int attrName) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrName, typedValue, true);
        return typedValue.data;
    }

    public static int getThemeDrawable(Context context, int attrName) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrName, typedValue, true);
        return typedValue.resourceId;
    }
}
