package org.exbin.bined.android.basic.color;

import android.graphics.Color;

import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.color.BasicCodeAreaDecorationColorType;
import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;

public class BasicCodeAreaColorsProfile implements CodeAreaColorsProfile {
    private Integer alternateBackground;
    private Integer alternateColor;
    private Integer cursorColor;
    private Integer cursorNegativeColor;
    private Integer decorationLine;
    private Integer selectionBackground;
    private Integer selectionColor;
    private Integer selectionMirrorBackground;
    private Integer selectionMirrorColor;
    private Integer textBackground;
    private Integer textColor;

    public int getAlternateBackground() {
        return this.alternateBackground;
    }

    @Override
    public Integer getColor(CodeAreaColorType codeAreaColorType) {
        if (codeAreaColorType == CodeAreaBasicColors.TEXT_COLOR) {
            return this.textColor;
        }
        if (codeAreaColorType == CodeAreaBasicColors.TEXT_BACKGROUND) {
            return this.textBackground;
        }
        if (codeAreaColorType == CodeAreaBasicColors.SELECTION_COLOR) {
            return this.selectionColor;
        }
        if (codeAreaColorType == CodeAreaBasicColors.SELECTION_BACKGROUND) {
            return this.selectionBackground;
        }
        if (codeAreaColorType == CodeAreaBasicColors.SELECTION_MIRROR_COLOR) {
            return this.selectionMirrorColor;
        }
        if (codeAreaColorType == CodeAreaBasicColors.SELECTION_MIRROR_BACKGROUND) {
            return this.selectionMirrorBackground;
        }
        if (codeAreaColorType == CodeAreaBasicColors.ALTERNATE_COLOR) {
            return this.alternateColor;
        }
        if (codeAreaColorType == CodeAreaBasicColors.ALTERNATE_BACKGROUND) {
            return this.alternateBackground;
        }
        if (codeAreaColorType == CodeAreaBasicColors.CURSOR_COLOR) {
            return this.cursorColor;
        }
        if (codeAreaColorType == CodeAreaBasicColors.CURSOR_NEGATIVE_COLOR) {
            return this.cursorNegativeColor;
        }
        if (codeAreaColorType == BasicCodeAreaDecorationColorType.LINE) {
            return this.decorationLine;
        }
        return null;
    }

    @Override
    public Integer getColor(CodeAreaColorType codeAreaColorType, CodeAreaBasicColors codeAreaBasicColors) {
        Integer color = getColor(codeAreaColorType);
        if (color != null) {
            return color;
        }
        if (codeAreaBasicColors == null) {
            return null;
        }
        return getColor(codeAreaBasicColors);
    }

    public int getCursorColor() {
        return this.cursorColor;
    }

    public int getCursorNegativeColor() {
        return this.cursorNegativeColor;
    }

    public int getDecorationLine() {
        return this.decorationLine;
    }

    public int getSelectionBackground() {
        return this.selectionBackground;
    }

    public int getSelectionColor() {
        return this.selectionColor;
    }

    public int getSelectionMirrorBackground() {
        return this.selectionMirrorBackground;
    }

    public int getSelectionMirrorColor() {
        return this.selectionMirrorColor;
    }

    public int getTextBackground() {
        return this.textBackground;
    }

    public int getTextColor() {
        return this.textColor;
    }

    @Override // org.exbin.bined.android.basic.color.CodeAreaColorsProfile
    public void reinitialize() {
        this.textColor = null;
        this.textColor = -16777216;
        this.textBackground = null;
        this.textBackground = -1;
        this.selectionColor = null;
        this.selectionColor = -1;
        this.selectionBackground = null;
        this.selectionBackground = Color.rgb(96, 96, 255);
        this.selectionMirrorColor = this.selectionColor;
        this.selectionMirrorBackground = CodeAreaAndroidUtils.computeGrayColor(this.selectionBackground);
        this.cursorColor = null;
        this.cursorColor = -16777216;
        this.cursorNegativeColor = CodeAreaAndroidUtils.createNegativeColor(this.cursorColor);
        this.decorationLine = -7829368;
        this.alternateColor = this.textColor;
        this.alternateBackground = CodeAreaAndroidUtils.createOddColor(this.textBackground);
    }
}