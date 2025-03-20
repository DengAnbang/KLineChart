package com.github.fujianlian.klinechart.draw;

import android.graphics.PointF;

final class PointUtil {

    private PointUtil() {
    }

    static float findDistance(PointF p, PointF q) {
        return (float) Math.sqrt(Math.pow((q.x - p.x), 2)
                + Math.pow((q.y - p.y), 2));
    }

}