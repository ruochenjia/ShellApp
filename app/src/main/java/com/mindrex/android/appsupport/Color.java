package com.mindrex.android.appsupport;

import com.mindrex.data.Array;
import com.mindrex.util.vector.Pos3;
import com.mindrex.util.vector.Pos4;

import java.util.Random;

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class Color {
    public static final Color BLACK = parse(0xff000000);
    public static final Color BLUE = parse(0xff0000ff);
    public static final Color CYAN = parse(0xff00ffff);
    public static final Color DKGRAY = parse(-12303292);
    public static final Color GRAY = parse(-7829368);
    public static final Color GREEN = parse(0xff00ff00);
    public static final Color LTGRAY = parse(-3355444);
    public static final Color MAGENTA = parse(0xffff00ff);
    public static final Color RED = parse(0xffff0000);
    public static final Color TRANSPARENT = parse(0);
    public static final Color WHITE = parse(0xffffffff);
    public static final Color YELLOW = parse(0xffffff00);

    public short a;
    public short r;
    public short g;
    public short b;

    public Color(Pos4 color) {
        this.a = color.a.shortValue();
        this.r = color.x.shortValue();
        this.g = color.y.shortValue();
        this.b = color.z.shortValue();
    }

    public static Color parse(int color) {
        return new Color(new Pos4((color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, (color >> 24) & 0xFF));
    }

    public static Color parse(Pos3 rbg) {
        return new Color(new Pos4(rbg, 0xff));
    }

    public static Color parse(String code) {
        short r = 0xff;
        short g = 0xff;
        short b = 0xff;
        short a = 0xff;
        if (code.length() == 9 || code.length() == 7) code = code.substring(1);
        if (code.length() == 8) {
            a = Short.parseShort(code.substring(0, 2), 16);
            code = code.substring(2);
        }
        if (code.length() == 6) {
            r = Short.parseShort(code.substring(0, 2), 16);
            g = Short.parseShort(code.substring(2, 4), 16);
            b = Short.parseShort(code.substring(4, 6), 16);
        }
        return new Color(new Pos4(r, g, b, a));
    }

    public static Color randomColor(Random random) {
        return randomColor(random, new Color[]{
                parse(new Pos3(0xff, 0x00, 0x00)),
                parse(new Pos3(0xff, 0xff, 0x00)),
                parse(new Pos3(0xff, 0x00, 0xff)),
                parse(new Pos3(0x00, 0xff, 0x00)),
                parse(new Pos3(0x00, 0xff, 0xff)),
                parse(new Pos3(0x00, 0x00, 0xff)),
                parse(new Pos3(0x00, 0x00, 0x00)),
                parse(new Pos3(0xff, 0xff, 0xff)),
                parse(new Pos3(0xbf, 0xbf, 0xbf)),
                parse(new Pos3(0xbf, 0xff, 0x00))
        });
    }

    public static Color randomColor(Random random, Color[] colors) {
        return Array.pickRandom(colors, random);
    }

    public final int toInt() {
        int a = this.a;
        int r = this.r;
        int g = this.g;
        int b = this.b;
        a = (a << 24) & 0xff000000;
        r = (r << 16) & 0x00ff0000;
        g = (g << 8) & 0x0000ff00;
        b = b & 0x000000ff;
        return a | r | g | b;
    }

    public final String toString() {
        return "#" + Integer.toHexString(a) + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b);
    }

    public final Pos4 toVector() {
        return new Pos4(toRgbVector(), a);
    }

    public final Pos3 toRgbVector() {
        return new Pos3(r, g, b);
    }
}