package com.github.fujianlian.klinechart.utils;

import android.content.Context;
import android.graphics.Paint;

import androidx.annotation.NonNull;

public class ViewUtil {
    static public int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    static public int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static float getTextBaseLine(@NonNull Paint paint, float centerY) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        return centerY + (textHeight / 2) - fontMetrics.bottom;
    }
}
