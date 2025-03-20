package com.github.fujianlian.klinechart.entity;

public class ChartPoint {
    public enum Color {
        COLOR_0("#F15866"),
        COLOR_1("#F99007"),
        COLOR_2("#F8CB6F"),
        COLOR_3("#37B985"),
        COLOR_4("#9AC5EC"),
        COLOR_5("#DD93E2");

        private String color;

        Color(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    public enum LineWidth {
        WIDTH_0(0.5F),
        WIDTH_1(1.0F),
        WIDTH_2(1.5F),
        WIDTH_3(2.0F);

        private float width;

        LineWidth(float width) {
            this.width = width;
        }

        public float getWidth() {
            return width;
        }
    }

    public enum LineStyle {
        STYLE_0(3, 1),
        STYLE_1(2, 1),
        STYLE_2(1, 1),
        STYLE_3(0, 0);

        private int dashWidth;
        private int dashGap;

        LineStyle(int dashWidth, int dashGap) {
            this.dashWidth = dashWidth;
            this.dashGap = dashGap;
        }

        public int getDashWidth() {
            return dashWidth;
        }

        public int getDashGap() {
            return dashGap;
        }
    }
}
