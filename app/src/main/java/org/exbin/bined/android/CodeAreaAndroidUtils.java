package org.exbin.bined.android;

import android.graphics.Color;
import android.graphics.Rect;

public class CodeAreaAndroidUtils {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int EXCEPTION1_CODE_POINT = 142;
    public static final int EXCEPTION2_CODE_POINT = 158;
    public static final int INV_SPACE_CODE_POINT = 127;
    public static int MAX_COMPONENT_VALUE = 255;
    public static final int MAX_MONOSPACE_CODE_POINT = 451;
    public static final int MIN_MONOSPACE_CODE_POINT = 31;

    private CodeAreaAndroidUtils() {
    }

    public static boolean areSameColors(Integer num, Integer num2) {
        return (num == null && num2 == null) || (num != null && num.equals(num2));
    }

    public static int computeGrayColor(int i) {
        int red = ((Color.red(i) + Color.green(i)) + Color.blue(i)) / 3;
        return Color.rgb(red, red, red);
    }

    public static Rect computeIntersection(Rect rect, Rect rect2) {
        if (rect == null || rect2 == null || rect2.left > rect.right || rect2.right < rect.left || rect2.top > rect.bottom || rect2.bottom < rect.top) {
            return null;
        }
        return new Rect(Math.max(rect.left, rect2.left), Math.max(rect.top, rect2.top), Math.min(rect.right, rect2.right), Math.min(rect.bottom, rect2.bottom));
    }

    public static int computeOddColorComponent(int i) {
        return (i > 64 ? -16 : 16) + i;
    }

    public static int createNegativeColor(int i) {
        return Color.rgb(MAX_COMPONENT_VALUE - Color.red(i), MAX_COMPONENT_VALUE - Color.green(i), MAX_COMPONENT_VALUE - Color.blue(i));
    }

    public static int createOddColor(int i) {
        return Color.rgb(computeOddColorComponent(Color.red(i)), computeOddColorComponent(Color.green(i)), computeOddColorComponent(Color.blue(i)));
    }

    public static boolean isMonospaceFullWidthCharater(char c) {
        return c > 31 && c < 451 && c != 127 && c != 142 && c != 158;
    }
}