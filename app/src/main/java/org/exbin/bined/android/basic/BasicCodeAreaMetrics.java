package org.exbin.bined.android.basic;

import android.graphics.Paint;
import java.nio.charset.Charset;

public class BasicCodeAreaMetrics {
    private int characterWidth;
    private int fontHeight;
    private Paint fontMetrics;
    private int maxBytesPerChar;
    private boolean monospaceFont;
    private int rowHeight;
    private int subFontSpace = 0;

    public int getCharWidth(char c) {
        return (int) this.fontMetrics.measureText(String.valueOf(c));
    }

    public int getCharacterWidth() {
        return this.characterWidth;
    }

    public int getCharsWidth(char[] cArr, int i, int i2) {
        return (int) this.fontMetrics.measureText(String.valueOf(cArr, i, i2));
    }

    public int getFontHeight() {
        return this.fontHeight;
    }

    public Paint getFontMetrics() {
        return this.fontMetrics;
    }

    public int getMaxBytesPerChar() {
        return this.maxBytesPerChar;
    }

    public int getRowHeight() {
        return this.rowHeight;
    }

    public int getSubFontSpace() {
        return this.subFontSpace;
    }

    public boolean hasUniformLineMetrics() {
        return false;
    }

    public boolean isInitialized() {
        return this.rowHeight != 0 && this.characterWidth != 0;
    }

    public boolean isMonospaceFont() {
        return this.monospaceFont;
    }

    public void recomputeMetrics(Paint paint, Charset charset) {
        boolean z = false;
        this.fontMetrics = paint;
        if (paint == null) {
            this.characterWidth = 0;
            this.fontHeight = 0;
        } else {
            int textSize = (int) paint.getTextSize();
            this.fontHeight = textSize;
            this.rowHeight = textSize;
            this.characterWidth = (int) paint.measureText("w");
            this.subFontSpace = (this.rowHeight - ((int) paint.getTextSize())) + 4;
            if (((float) this.characterWidth) == paint.measureText(" ") && ((float) this.characterWidth) == paint.measureText("i")) {
                z = true;
            }
            this.monospaceFont = z;
        }
        this.maxBytesPerChar = (int) charset.newEncoder().maxBytesPerChar();
    }
}