package org.exbin.bined.android.basic;

import org.exbin.bined.basic.BasicCodeAreaLayout;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.CodeAreaViewMode;

public class BasicCodeAreaVisibility {
    private int charactersPerCodeSection;
    private int codeLastCharPos;
    private boolean codeSectionVisible;
    private int previewCharPos;
    private int previewRelativeX;
    private boolean previewSectionVisible;
    private int skipRestFromChar;
    private int skipRestFromCode;
    private int skipRestFromPreview;
    private int skipToChar;
    private int skipToCode;
    private int skipToPreview;
    private int splitLinePos;

    public int getCharactersPerCodeSection() {
        return this.charactersPerCodeSection;
    }

    public int getCodeLastCharPos() {
        return this.codeLastCharPos;
    }

    public int getMaxRowDataChars() {
        return this.skipRestFromChar - this.skipToChar;
    }

    public int getPreviewCharPos() {
        return this.previewCharPos;
    }

    public int getPreviewRelativeX() {
        return this.previewRelativeX;
    }

    public int getSkipRestFromChar() {
        return this.skipRestFromChar;
    }

    public int getSkipRestFromCode() {
        return this.skipRestFromCode;
    }

    public int getSkipRestFromPreview() {
        return this.skipRestFromPreview;
    }

    public int getSkipToChar() {
        return this.skipToChar;
    }

    public int getSkipToCode() {
        return this.skipToCode;
    }

    public int getSkipToPreview() {
        return this.skipToPreview;
    }

    public int getSplitLinePos() {
        return this.splitLinePos;
    }

    public boolean isCodeSectionVisible() {
        return this.codeSectionVisible;
    }

    public boolean isPreviewSectionVisible() {
        return this.previewSectionVisible;
    }

    public void recomputeCharPositions(BasicCodeAreaMetrics basicCodeAreaMetrics, BasicCodeAreaStructure basicCodeAreaStructure, BasicCodeAreaDimensions basicCodeAreaDimensions, BasicCodeAreaLayout basicCodeAreaLayout, BasicCodeAreaScrolling basicCodeAreaScrolling) {
        boolean z = true;
        int bytesPerRow = basicCodeAreaStructure.getBytesPerRow();
        int characterWidth = basicCodeAreaMetrics.getCharacterWidth();
        int maxDigitsForByte = basicCodeAreaStructure.getCodeType().getMaxDigitsForByte() + 1;
        this.previewRelativeX = this.previewCharPos * characterWidth;
        CodeAreaViewMode viewMode = basicCodeAreaStructure.getViewMode();
        int horizontalScrollX = basicCodeAreaScrolling.getHorizontalScrollX(characterWidth);
        int dataViewWidth = basicCodeAreaDimensions.getDataViewWidth() + horizontalScrollX;
        this.charactersPerCodeSection = basicCodeAreaLayout.computeFirstCodeCharacterPos(basicCodeAreaStructure, bytesPerRow);
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            this.codeLastCharPos = (bytesPerRow * maxDigitsForByte) - 1;
        } else {
            this.codeLastCharPos = 0;
        }
        if (viewMode == CodeAreaViewMode.DUAL) {
            this.previewCharPos = maxDigitsForByte * bytesPerRow;
        } else {
            this.previewCharPos = 0;
        }
        this.skipToCode = 0;
        this.skipToChar = 0;
        this.skipToPreview = 0;
        this.skipRestFromCode = -1;
        this.skipRestFromChar = -1;
        this.skipRestFromPreview = -1;
        this.codeSectionVisible = viewMode != CodeAreaViewMode.TEXT_PREVIEW;
        if (viewMode == CodeAreaViewMode.CODE_MATRIX) {
            z = false;
        }
        this.previewSectionVisible = z;
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            int i = horizontalScrollX / characterWidth;
            this.skipToChar = Math.max(i, 0);
            int i2 = ((dataViewWidth + characterWidth) - 1) / characterWidth;
            this.skipRestFromChar = Math.min(i2, basicCodeAreaStructure.getCharactersPerRow());
            this.skipToCode = basicCodeAreaStructure.computePositionByte(this.skipToChar);
            int computePositionByte = basicCodeAreaStructure.computePositionByte(this.skipRestFromChar - 1) + 1;
            this.skipRestFromCode = Math.min(computePositionByte, bytesPerRow);
        }
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.TEXT_PREVIEW) {
            int i3 = this.previewCharPos;
            int i4 = (horizontalScrollX / characterWidth) - i3;
            this.skipToPreview = Math.max(i4, 0);
            int i5 = this.skipToPreview;
            if (i5 > 0) {
                this.skipToChar = i5 + i3;
            }
            int i6 = (((dataViewWidth + characterWidth) - 1) / characterWidth) - i3;
            this.skipRestFromPreview = Math.min(i6, bytesPerRow);
            int i7 = this.skipRestFromPreview;
            if (i7 >= 0) {
                this.skipRestFromChar = i7 + i3;
            }
        }
    }
}