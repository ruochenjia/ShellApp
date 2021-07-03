package org.exbin.bined.android;

import android.graphics.Paint;

public class Font {
    private int fontFlags;

    public static Font fromPaint(Paint paint) {
        Font font = new Font();
        font.setFontFlags(paint.getFlags());
        return font;
    }

    public int getFontFlags() {
        return this.fontFlags;
    }

    public void setFontFlags(int i) {
        this.fontFlags = i;
    }
}
