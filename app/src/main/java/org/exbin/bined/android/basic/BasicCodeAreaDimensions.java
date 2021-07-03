package org.exbin.bined.android.basic;

import android.graphics.Rect;
import org.exbin.bined.basic.BasicCodeAreaZone;

public class BasicCodeAreaDimensions {
    private int charactersPerPage;
    private int charactersPerRect;
    private final Rect componentRectangle = new Rect();
    private int dataViewHeight;
    private final Rect dataViewInnerRectangle = new Rect();
    private final Rect dataViewRectangle = new Rect();
    private int dataViewWidth;
    private int headerAreaHeight;
    private final Rect headerAreaRectangle = new Rect();
    private int horizontalScrollBarSize;
    private int lastCharOffset;
    private int lastRowOffset;
    private final Rect mainAreaRectangle = new Rect();
    private final Rect rowPositionAreaRectangle = new Rect();
    private int rowPositionAreaWidth;
    private int rowsPerPage;
    private int rowsPerRect;
    private int scrollPanelHeight;
    private final Rect scrollPanelRectangle = new Rect();
    private int scrollPanelWidth;
    private int scrollPanelX;
    private int scrollPanelY;
    private int verticalScrollBarSize;

    private int computeCharactersPerPage(BasicCodeAreaMetrics basicCodeAreaMetrics) {
        int characterWidth = basicCodeAreaMetrics.getCharacterWidth();
        if (characterWidth == 0) {
            return 0;
        }
        return this.dataViewWidth / characterWidth;
    }

    private int computeCharactersPerRectangle(BasicCodeAreaMetrics basicCodeAreaMetrics) {
        int characterWidth = basicCodeAreaMetrics.getCharacterWidth();
        if (characterWidth == 0) {
            return 0;
        }
        return ((this.dataViewWidth + characterWidth) - 1) / characterWidth;
    }

    private int computeRowsPerPage(BasicCodeAreaMetrics basicCodeAreaMetrics) {
        int rowHeight = basicCodeAreaMetrics.getRowHeight();
        if (rowHeight == 0) {
            return 0;
        }
        return this.dataViewHeight / rowHeight;
    }

    private int computeRowsPerRectangle(BasicCodeAreaMetrics basicCodeAreaMetrics) {
        int rowHeight = basicCodeAreaMetrics.getRowHeight();
        if (rowHeight == 0) {
            return 0;
        }
        return ((this.dataViewHeight + rowHeight) - 1) / rowHeight;
    }

    private static void modifyRect(Rect rect, int i, int i2, int i3, int i4) {
        rect.set(i, i2, i + i3, i2 + i4);
    }

    public int getCharactersPerPage() {
        return this.charactersPerPage;
    }

    public int getCharactersPerRect() {
        return this.charactersPerRect;
    }

    public Rect getComponentRectangle() {
        return this.componentRectangle;
    }

    public int getDataViewHeight() {
        return this.dataViewHeight;
    }

    public Rect getDataViewInnerRectangle() {
        return this.dataViewInnerRectangle;
    }

    public Rect getDataViewRectangle() {
        return this.dataViewRectangle;
    }

    public int getDataViewWidth() {
        return this.dataViewWidth;
    }

    public int getHeaderAreaHeight() {
        return this.headerAreaHeight;
    }

    public Rect getHeaderAreaRectangle() {
        return this.headerAreaRectangle;
    }

    public int getHorizontalScrollBarSize() {
        return this.horizontalScrollBarSize;
    }

    public int getLastCharOffset() {
        return this.lastCharOffset;
    }

    public int getLastRowOffset() {
        return this.lastRowOffset;
    }

    public Rect getMainAreaRectangle() {
        return this.mainAreaRectangle;
    }

    public BasicCodeAreaZone getPositionZone(int i, int i2) {
        int i3 = this.scrollPanelY;
        if (i2 <= i3) {
            return i < this.rowPositionAreaWidth ? BasicCodeAreaZone.TOP_LEFT_CORNER : BasicCodeAreaZone.HEADER;
        }
        if (i < this.rowPositionAreaWidth) {
            return i2 >= i3 + this.scrollPanelHeight ? BasicCodeAreaZone.BOTTOM_LEFT_CORNER : BasicCodeAreaZone.ROW_POSITIONS;
        }
        int i4 = this.scrollPanelX;
        int i5 = this.scrollPanelWidth;
        return (i < i4 + i5 || i2 >= this.scrollPanelHeight + i3) ? i2 >= i3 + this.scrollPanelHeight ? i >= i4 + i5 ? BasicCodeAreaZone.SCROLLBAR_CORNER : BasicCodeAreaZone.HORIZONTAL_SCROLLBAR : BasicCodeAreaZone.CODE_AREA : BasicCodeAreaZone.VERTICAL_SCROLLBAR;
    }

    public Rect getRowPositionAreaRectangle() {
        return this.rowPositionAreaRectangle;
    }

    public int getRowPositionAreaWidth() {
        return this.rowPositionAreaWidth;
    }

    public int getRowsPerPage() {
        return this.rowsPerPage;
    }

    public int getRowsPerRect() {
        return this.rowsPerRect;
    }

    public int getScrollPanelHeight() {
        return this.scrollPanelHeight;
    }

    public Rect getScrollPanelRectangle() {
        return this.scrollPanelRectangle;
    }

    public int getScrollPanelWidth() {
        return this.scrollPanelWidth;
    }

    public int getScrollPanelX() {
        return this.scrollPanelX;
    }

    public int getScrollPanelY() {
        return this.scrollPanelY;
    }

    public int getVerticalScrollBarSize() {
        return this.verticalScrollBarSize;
    }

    public void recomputeSizes(BasicCodeAreaMetrics basicCodeAreaMetrics, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        modifyRect(this.componentRectangle, i, i2, i3, i4);
        this.verticalScrollBarSize = i6;
        this.horizontalScrollBarSize = i7;
        this.rowPositionAreaWidth = basicCodeAreaMetrics.getCharacterWidth() * (i5 + 1);
        int fontHeight = basicCodeAreaMetrics.getFontHeight() + (basicCodeAreaMetrics.getFontHeight() / 4);
        this.headerAreaHeight = fontHeight;
        int i8 = this.rowPositionAreaWidth;
        this.scrollPanelX = i + i8;
        this.scrollPanelY = i2 + fontHeight;
        int i9 = i3 - i8;
        this.scrollPanelWidth = i9;
        int i10 = i4 - fontHeight;
        this.scrollPanelHeight = i10;
        this.dataViewWidth = i9 - i6;
        this.dataViewHeight = i10 - i7;
        this.charactersPerRect = computeCharactersPerRectangle(basicCodeAreaMetrics);
        this.charactersPerPage = computeCharactersPerPage(basicCodeAreaMetrics);
        this.rowsPerRect = computeRowsPerRectangle(basicCodeAreaMetrics);
        this.rowsPerPage = computeRowsPerPage(basicCodeAreaMetrics);
        this.lastCharOffset = basicCodeAreaMetrics.isInitialized() ? this.dataViewWidth % basicCodeAreaMetrics.getCharacterWidth() : 0;
        this.lastRowOffset = basicCodeAreaMetrics.isInitialized() ? this.dataViewHeight % basicCodeAreaMetrics.getRowHeight() : 0;
        int i11 = this.rowPositionAreaWidth;
        boolean z = true;
        boolean z2 = i11 + i6 <= i3;
        int i12 = this.scrollPanelY;
        if (i12 + i7 > i4) {
            z = false;
        }
        if (!z2 || !z) {
            this.mainAreaRectangle.setEmpty();
        } else {
            modifyRect(this.mainAreaRectangle, i + i11, i12, (i3 - i11) - getVerticalScrollBarSize(), (i4 - this.scrollPanelY) - getHorizontalScrollBarSize());
        }
        if (z2) {
            int i13 = this.rowPositionAreaWidth;
            modifyRect(this.headerAreaRectangle, i + i13, i2, (i3 - i13) - getVerticalScrollBarSize(), this.headerAreaHeight);
        } else {
            this.headerAreaRectangle.setEmpty();
        }
        if (z) {
            int i14 = this.scrollPanelY;
            modifyRect(this.rowPositionAreaRectangle, i, i14, this.rowPositionAreaWidth, (i4 - i14) - getHorizontalScrollBarSize());
        } else {
            this.rowPositionAreaRectangle.setEmpty();
        }
        modifyRect(this.scrollPanelRectangle, this.scrollPanelX, this.scrollPanelY, this.scrollPanelWidth, this.scrollPanelHeight);
        int i15 = this.scrollPanelX;
        int i16 = this.scrollPanelY;
        int i17 = this.dataViewWidth;
        if (i17 < 0) {
            i17 = 0;
        }
        int i18 = this.dataViewHeight;
        if (i18 < 0) {
            i18 = 0;
        }
        modifyRect(this.dataViewRectangle, i15, i16, i17, i18);
        int i19 = this.dataViewWidth;
        if (i19 < 0) {
            i19 = 0;
        }
        int i20 = this.dataViewHeight;
        if (i20 < 0) {
            i20 = 0;
        }
        modifyRect(this.dataViewInnerRectangle, 0, 0, i19, i20);
    }
}