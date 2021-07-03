package org.exbin.bined.android.basic;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mindrex.android.appsupport.TwoDimensionScrollView;

import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.CharsetStreamTranslator;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.PositionCodeType;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.Font;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.android.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.BasicCodeAreaLayout;
import org.exbin.bined.basic.BasicCodeAreaScrolling;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.BasicCodeAreaStructure;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollBarVerticalScale;
import org.exbin.bined.basic.ScrollingDirection;

import java.nio.charset.Charset;
import java.util.Arrays;

public class DefaultCodeAreaPainter implements CodeAreaPainter, BasicColorsCapableCodeAreaPainter {
    private BasicBackgroundPaintMode backgroundPaintMode;
    private volatile boolean caretChanged = true;
    private final char[] charMapping = new char[256];
    private Charset charMappingCharset = null;
    private Charset charset;
    protected final CodeArea codeArea;
    private CodeCharactersCase codeCharactersCase;
    private BasicCodeAreaColorsProfile colorsProfile = new BasicCodeAreaColorsProfile();
    private CursorDataCache cursorDataCache = null;
    private final View dataView;
    private int dataViewOffsetX = 0;
    private int dataViewOffsetY = 0;
    private final BasicCodeAreaDimensions dimensions = new BasicCodeAreaDimensions();
    private EditationOperation editationOperation;
    private Font font;
    private volatile boolean fontChanged = false;
    private volatile boolean initialized = false;
    private final BasicCodeAreaLayout layout = new BasicCodeAreaLayout();
    private volatile boolean layoutChanged = true;
    private int maxRowPositionLength;
    private final BasicCodeAreaMetrics metrics = new BasicCodeAreaMetrics();
    private int minRowPositionLength;
    private Paint paint = null;
    private long paintCounter = 0;
    private volatile boolean resetColors = true;
    private RowDataCache rowDataCache = null;
    private int rowPositionLength;
    private int scrollOffsetX = 0;
    private int scrollOffsetY = 0;
    private final TwoDimensionScrollView scrollPanel;
    private final BasicCodeAreaScrolling scrolling = new BasicCodeAreaScrolling();
    private final ScrollingState scrollingState = ScrollingState.NO_SCROLLING;
    private final BasicCodeAreaStructure structure = new BasicCodeAreaStructure();
    private final BasicCodeAreaVisibility visibility = new BasicCodeAreaVisibility();

    /* access modifiers changed from: package-private */
    /* renamed from: org.exbin.bined.android.basic.DefaultCodeAreaPainter$5  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorRenderingMode;

        static {
            int[] iArr = new int[DefaultCodeAreaCaret.CursorRenderingMode.values().length];
            $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorRenderingMode = iArr;
            try {
                iArr[DefaultCodeAreaCaret.CursorRenderingMode.PAINT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorRenderingMode[DefaultCodeAreaCaret.CursorRenderingMode.XOR.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorRenderingMode[DefaultCodeAreaCaret.CursorRenderingMode.NEGATIVE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CursorDataCache {
        Rectangle caretRect;
        char[] cursorChars;
        int cursorCharsLength;
        byte[] cursorData;
        int cursorDataLength;
        Rectangle mirrorCursorRect;

        private CursorDataCache() {
            this.caretRect = new Rectangle();
            this.mirrorCursorRect = new Rectangle();
        }
    }

    /* access modifiers changed from: private */
    public static class Rectangle {
        public int x;

        private Rectangle() {
        }
    }

    /* access modifiers changed from: private */
    public static class RowDataCache {
        char[] headerChars;
        char[] rowCharacters;
        byte[] rowData;
        char[] rowPositionCode;

        private RowDataCache() {
        }
    }

    /* access modifiers changed from: protected */
    public enum ScrollingState {
        NO_SCROLLING,
        SCROLLING_BY_SCROLLBAR,
        SCROLLING_BY_MOVEMENT
    }

    public DefaultCodeAreaPainter(final CodeArea codeArea2) {
        this.codeArea = codeArea2;
        /* class org.exbin.bined.android.basic.DefaultCodeAreaPainter.AnonymousClass1 */// org.exbin.bined.DataChangedListener
        codeArea2.addDataChangedListener(() -> {
            DefaultCodeAreaPainter.this.validateCaret();
            DefaultCodeAreaPainter.this.recomputeLayout();
        });
        TwoDimensionScrollView r0 = new TwoDimensionScrollView(codeArea2.getContext()) {
            /* class org.exbin.bined.android.basic.DefaultCodeAreaPainter.AnonymousClass2 */

            /* access modifiers changed from: protected */
            public void onScrollChanged(int i, int i2, int i3, int i4) {
                //DefaultCodeAreaPainter.super.onScrollChanged(i, i2, i3, i4);
                DefaultCodeAreaPainter.this.scrolling.updateHorizontalScrollBarValue(i, DefaultCodeAreaPainter.this.metrics.getCharacterWidth());
                DefaultCodeAreaPainter.this.scrolling.updateVerticalScrollBarValue(i2, DefaultCodeAreaPainter.this.metrics.getRowHeight(), Integer.MAX_VALUE, DefaultCodeAreaPainter.this.structure.getRowsPerDocument() - ((long) DefaultCodeAreaPainter.this.dimensions.getRowsPerRect()));
                DefaultCodeAreaPainter.this.scrollOffsetX = i;
                DefaultCodeAreaPainter.this.scrollOffsetY = i2;
                DefaultCodeAreaPainter defaultCodeAreaPainter = DefaultCodeAreaPainter.this;
                defaultCodeAreaPainter.dataViewOffsetX = defaultCodeAreaPainter.scrollOffsetX - DefaultCodeAreaPainter.this.dimensions.getScrollPanelX();
                DefaultCodeAreaPainter defaultCodeAreaPainter2 = DefaultCodeAreaPainter.this;
                defaultCodeAreaPainter2.dataViewOffsetY = defaultCodeAreaPainter2.scrollOffsetY - DefaultCodeAreaPainter.this.dimensions.getScrollPanelY();
                codeArea2.setScrollPosition(DefaultCodeAreaPainter.this.scrolling.getScrollPosition());
                DefaultCodeAreaPainter.this.notifyScrolled();
                codeArea2.repaint();
            }
        };
        this.scrollPanel = r0;
        View r1 = new View(codeArea2.getContext()) {
            /* class org.exbin.bined.android.basic.DefaultCodeAreaPainter.AnonymousClass3 */

            /* access modifiers changed from: protected */
            public void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                DefaultCodeAreaPainter.this.paintMainArea(canvas);
            }
        };
        this.dataView = r1;
        r0.addView(r1);
        r0.setScrollContainer(true);
    }

    private void buildCharMapping(Charset charset2) {
        for (int i = 0; i < 256; i++) {
            this.charMapping[i] = new String(new byte[]{(byte) i}, charset2).charAt(0);
        }
        this.charMappingCharset = charset2;
    }

    private int getHorizontalScrollBarSize() {
        return 10;
    }

    private Rect getMirrorCursorRect(long j, CodeAreaSection codeAreaSection) {
        CodeType codeType = this.structure.getCodeType();
        Point positionPoint = getPositionPoint(j, 0, codeAreaSection == BasicCodeAreaSection.CODE_MATRIX ? BasicCodeAreaSection.TEXT_PREVIEW : BasicCodeAreaSection.CODE_MATRIX);
        if (positionPoint == null) {
            return null;
        }
        return new Rect(positionPoint.x, positionPoint.y, ((codeAreaSection == BasicCodeAreaSection.TEXT_PREVIEW ? codeType.getMaxDigitsForByte() : 1) * this.metrics.getCharacterWidth()) + positionPoint.x, positionPoint.y + this.metrics.getRowHeight());
    }

    private int getRowPositionLength() {
        int i = this.minRowPositionLength;
        if (i > 0 && i == this.maxRowPositionLength) {
            return i;
        }
        long dataSize = this.codeArea.getDataSize();
        if (dataSize == 0) {
            return 1;
        }
        int ceil = (int) Math.ceil(Math.log(dataSize == PagedData.MAX_DATA_SIZE ? (double) dataSize : (double) (dataSize + 1)) / PositionCodeType.HEXADECIMAL.getBaseLog());
        int i2 = this.minRowPositionLength;
        if (i2 > 0 && ceil < i2) {
            ceil = this.minRowPositionLength;
        }
        int i3 = this.maxRowPositionLength;
        if (i3 > 0 && ceil > i3) {
            ceil = this.maxRowPositionLength;
        }
        if (ceil == 0) {
            ceil = 1;
        }
        return ceil;
    }

    private int getVerticalScrollBarSize() {
        return 10;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyScrolled() {
        recomputeScrollState();
        this.codeArea.notifyScrolled();
    }

    private void paintCursorRect(Canvas canvas, Rect rect, Rect rect2, Rect rect3, DefaultCodeAreaCaret.CursorRenderingMode cursorRenderingMode, DefaultCodeAreaCaret defaultCodeAreaCaret) {
        switch (AnonymousClass5.$SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorRenderingMode[cursorRenderingMode.ordinal()]) {
            case 1:
                canvas.drawRect(rect, this.paint);
                return;
            case 2:
                return;
            case 3:
                int characterWidth = this.metrics.getCharacterWidth();
                int rowHeight = this.metrics.getRowHeight();
                int maxBytesPerChar = this.metrics.getMaxBytesPerChar();
                int subFontSpace = this.metrics.getSubFontSpace();
                int scrollPanelX = this.dimensions.getScrollPanelX();
                int scrollPanelY = this.dimensions.getScrollPanelY();
                int previewRelativeX = this.visibility.getPreviewRelativeX();
                CodeAreaViewMode viewMode = this.structure.getViewMode();
                CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
                long dataSize = this.structure.getDataSize();
                CodeType codeType = this.structure.getCodeType();
                canvas.drawRect(rect2, this.paint);
                this.paint.setColor(this.colorsProfile.getCursorNegativeColor());
                BinaryData contentData = this.codeArea.getContentData();
                int charPosition = rect3.left + (scrollPosition.getCharPosition() * characterWidth) + scrollPosition.getCharOffset();
                int rowOffset = (((rowHeight * ((((rect3.top + scrollPosition.getRowOffset()) - scrollPanelY) / rowHeight) + 1)) + scrollPanelY) - subFontSpace) - scrollPosition.getRowOffset();
                long dataPosition = defaultCodeAreaCaret.getDataPosition();
                if (viewMode == CodeAreaViewMode.CODE_MATRIX || defaultCodeAreaCaret.getSection() != BasicCodeAreaSection.TEXT_PREVIEW) {
                    int i = (charPosition - scrollPanelX) / characterWidth;
                    int computeFirstCodeCharacterPos = this.structure.computeFirstCodeCharacterPos(this.structure.computePositionByte(i));
                    if (contentData == null || dataPosition >= dataSize) {
                        Arrays.fill(this.cursorDataCache.cursorChars, ' ');
                    } else {
                        CodeAreaUtils.byteToCharsCode(contentData.getByte(dataPosition), codeType, this.cursorDataCache.cursorChars, 0, this.codeCharactersCase);
                    }
                    int charPosition2 = scrollPosition.getCharPosition();
                    int i2 = i - computeFirstCodeCharacterPos;
                    drawCenteredChars(canvas, this.cursorDataCache.cursorChars, i2, 1, characterWidth, (((scrollPanelX + (computeFirstCodeCharacterPos * characterWidth)) - (charPosition2 * characterWidth)) - scrollPosition.getCharOffset()) + this.dataViewOffsetX + (i2 * characterWidth), this.dataViewOffsetY + rowOffset);
                    return;
                }
                int i3 = (charPosition - previewRelativeX) / characterWidth;
                if (dataPosition < dataSize) {
                    if (maxBytesPerChar > 1) {
                        if (((long) maxBytesPerChar) + dataPosition > dataSize) {
                            maxBytesPerChar = (int) (dataSize - dataPosition);
                        }
                        if (contentData == null) {
                            this.cursorDataCache.cursorChars[0] = (char) 32;
                        } else {
                            contentData.copyToArray(dataPosition, this.cursorDataCache.cursorData, 0, maxBytesPerChar);
                            String str = new String(this.cursorDataCache.cursorData, 0, maxBytesPerChar, this.charset);
                            if (!str.isEmpty()) {
                                this.cursorDataCache.cursorChars[0] = str.charAt(0);
                            }
                        }
                    } else {
                        Charset charset2 = this.charMappingCharset;
                        if (charset2 == null || charset2 != this.charset) {
                            buildCharMapping(this.charset);
                        }
                        if (contentData == null) {
                            this.cursorDataCache.cursorChars[0] = (char) 32;
                        } else {
                            this.cursorDataCache.cursorChars[0] = this.charMapping[contentData.getByte(dataPosition) & 255];
                        }
                    }
                    int charPosition3 = scrollPosition.getCharPosition();
                    drawCenteredChars(canvas, this.cursorDataCache.cursorChars, 0, 1, characterWidth, this.dataViewOffsetX + (((previewRelativeX + (i3 * characterWidth)) - (charPosition3 * characterWidth)) - scrollPosition.getCharOffset()), this.dataViewOffsetY + rowOffset);
                    return;
                }
                return;
            default:
                throw new IllegalStateException("Unexpected rendering mode " + cursorRenderingMode.name());
        }
    }

    private void prepareRowData(long j) {
        int i;
        int i2;
        int maxBytesPerChar = this.metrics.getMaxBytesPerChar();
        CodeAreaViewMode viewMode = this.structure.getViewMode();
        int bytesPerRow = this.structure.getBytesPerRow();
        long dataSize = this.structure.getDataSize();
        int previewCharPos = this.visibility.getPreviewCharPos();
        CodeType codeType = this.structure.getCodeType();
        int i3 = 0;
        if (j < dataSize) {
            int i4 = (bytesPerRow + maxBytesPerChar) - 1;
            if (((long) i4) + j > dataSize) {
                i4 = (int) (dataSize - j);
            }
            if (j < 0) {
                i3 = (int) (-j);
            }
            BinaryData contentData = this.codeArea.getContentData();
            if (contentData != null) {
                contentData.copyToArray(((long) i3) + j, this.rowDataCache.rowData, i3, i4 - i3);
                i = ((long) bytesPerRow) + j > dataSize ? (int) (dataSize - j) : bytesPerRow;
            } else {
                throw new IllegalStateException("Missing data on nonzero data size");
            }
        } else {
            i = 0;
            i3 = 0;
        }
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            int skipToCode = this.visibility.getSkipToCode();
            int min = Math.min(this.visibility.getSkipRestFromCode(), i);
            for (int max = Math.max(skipToCode, i3); max < min; max++) {
                byte b = this.rowDataCache.rowData[max];
                int computeFirstCodeCharacterPos = this.structure.computeFirstCodeCharacterPos(max);
                if (computeFirstCodeCharacterPos > 0) {
                    this.rowDataCache.rowCharacters[computeFirstCodeCharacterPos - 1] = (char) 32;
                }
                CodeAreaUtils.byteToCharsCode(b, codeType, this.rowDataCache.rowCharacters, computeFirstCodeCharacterPos, this.codeCharactersCase);
            }
            if (bytesPerRow > i) {
                Arrays.fill(this.rowDataCache.rowCharacters, this.structure.computeFirstCodeCharacterPos(i), this.rowDataCache.rowCharacters.length, ' ');
            }
        }
        i2 = maxBytesPerChar;
        if (previewCharPos > 0) {
            this.rowDataCache.rowCharacters[previewCharPos - 1] = (char) 32;
        }
        if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
            int skipToPreview = this.visibility.getSkipToPreview();
            int min2 = Math.min(this.visibility.getSkipRestFromPreview(), i);
            for (int i5 = skipToPreview; i5 < min2; i5++) {
                byte b2 = this.rowDataCache.rowData[i5];
                if (i2 > 1) {
                    if (((long) i2) + j > dataSize) {
                        i2 = (int) (dataSize - j);
                    }
                    String str = new String(this.rowDataCache.rowData, i5, i5 + i2 > this.rowDataCache.rowData.length ? this.rowDataCache.rowData.length - i5 : i2, this.charset);
                    if (!str.isEmpty()) {
                        this.rowDataCache.rowCharacters[previewCharPos + i5] = str.charAt(0);
                    }
                } else {
                    Charset charset2 = this.charMappingCharset;
                    if (charset2 == null || charset2 != this.charset) {
                        buildCharMapping(this.charset);
                    }
                    this.rowDataCache.rowCharacters[previewCharPos + i5] = this.charMapping[b2 & 255];
                }
            }
            if (bytesPerRow > i) {
                Arrays.fill(this.rowDataCache.rowCharacters, i + previewCharPos, previewCharPos + bytesPerRow, ' ');
            }
        }
    }

    private void recomputeDimensions() {
        int verticalScrollBarSize = getVerticalScrollBarSize();
        int horizontalScrollBarSize = getHorizontalScrollBarSize();
        this.dimensions.recomputeSizes(this.metrics, 0, 0, this.codeArea.getWidth(), this.codeArea.getHeight(), this.rowPositionLength, verticalScrollBarSize, horizontalScrollBarSize);
    }

    private void recomputeScrollState() {
        int i;
        this.scrolling.setScrollPosition(this.codeArea.getScrollPosition());
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        if (characterWidth > 0) {
            recomputeCharPositions();
        }
        Rect scrollPanelRectangle = this.dimensions.getScrollPanelRectangle();
        this.dataView.layout(0, 0, scrollPanelRectangle.width(), scrollPanelRectangle.height());
        if (rowHeight > 0 && characterWidth > 0) {
            this.scrolling.updateCache(this.codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());
            BasicCodeAreaScrolling.ScrollViewDimension computeViewDimension = this.scrolling.computeViewDimension(this.codeArea.getWidth(), this.codeArea.getHeight(), this.layout, this.structure, characterWidth, rowHeight);
            this.dataView.setMinimumWidth(computeViewDimension.getWidth());
            this.dataView.setMinimumHeight(computeViewDimension.getHeight());
            this.scrolling.updateCache(this.codeArea, getHorizontalScrollBarSize(), getVerticalScrollBarSize());
            int charactersPerRow = this.structure.getCharactersPerRow() * characterWidth;
            long dataSize = (this.structure.getDataSize() / ((long) this.structure.getBytesPerRow())) + 1;
            if (dataSize > ((long) (Integer.MAX_VALUE / rowHeight))) {
                this.scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.SCALED);
                i = Integer.MAX_VALUE;
            } else {
                this.scrolling.setScrollBarVerticalScale(ScrollBarVerticalScale.NORMAL);
                i = (int) (dataSize * ((long) rowHeight));
            }
            recomputeDimensions();
            this.dimensions.getScrollPanelRectangle();
            this.dataView.layout(0, 0, charactersPerRow, i);
            this.dataView.setMinimumWidth(charactersPerRow);
            this.dataView.setMinimumHeight(i);
        }
        final Rect scrollPanelRectangle2 = this.dimensions.getScrollPanelRectangle();
        /* class org.exbin.bined.android.basic.DefaultCodeAreaPainter.AnonymousClass4 */
        ((Activity) this.codeArea.getContext()).runOnUiThread(() -> {
            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(scrollPanelRectangle2.width(), scrollPanelRectangle2.height());
            marginLayoutParams.setMargins(scrollPanelRectangle2.left, scrollPanelRectangle2.top, 0, 0);
            DefaultCodeAreaPainter.this.scrollPanel.setLayoutParams(marginLayoutParams);
            DefaultCodeAreaPainter.this.scrollPanel.invalidate();
        });
    }

    private void renderBackgroundSequence(Canvas canvas, int i, int i2, int i3, int i4) {
        int characterWidth = this.metrics.getCharacterWidth();
        canvas.drawRect((float) ((i * characterWidth) + i3), (float) i4, (float) ((characterWidth * i2) + i3), (float) (i4 + this.metrics.getRowHeight()), this.paint);
    }

    private void updateCaret() {
        this.editationOperation = this.codeArea.getActiveOperation();
        this.caretChanged = false;
    }

    private void updateRowDataCache() {
        if (this.rowDataCache == null) {
            this.rowDataCache = new RowDataCache();
        }
        this.visibility.getMaxRowDataChars();
        this.rowDataCache.headerChars = new char[this.visibility.getCharactersPerCodeSection()];
        this.rowDataCache.rowData = new byte[((this.structure.getBytesPerRow() + this.metrics.getMaxBytesPerChar()) - 1)];
        this.rowDataCache.rowPositionCode = new char[this.rowPositionLength];
        this.rowDataCache.rowCharacters = new char[this.structure.getCharactersPerRow()];
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void validateCaret() {
        CodeAreaCaret caret = this.codeArea.getCaret();
        if (caret.getCaretPosition().getDataPosition() > this.codeArea.getDataSize()) {
            caret.setCaretPosition(null);
        }
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void attach() {
        this.codeArea.addView(this.scrollPanel, new RelativeLayout.LayoutParams(-1, -2));
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public CodeAreaScrollPosition computeCenterOnScrollPosition(CodeAreaCaretPosition codeAreaCaretPosition) {
        int bytesPerRow = this.structure.getBytesPerRow();
        int previewCharPos = this.visibility.getPreviewCharPos();
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        int dataViewWidth = this.dimensions.getDataViewWidth();
        int dataViewHeight = this.dimensions.getDataViewHeight();
        int rowsPerRect = this.dimensions.getRowsPerRect();
        int charactersPerRect = this.dimensions.getCharactersPerRect();
        long dataPosition = codeAreaCaretPosition.getDataPosition();
        int i = (int) (dataPosition % ((long) bytesPerRow));
        return this.scrolling.computeCenterOnScrollPosition(dataPosition / ((long) bytesPerRow), codeAreaCaretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX) == BasicCodeAreaSection.TEXT_PREVIEW ? i + previewCharPos : this.structure.computeFirstCodeCharacterPos(i) + codeAreaCaretPosition.getCodeOffset(), bytesPerRow, rowsPerRect, charactersPerRect, dataViewWidth, dataViewHeight, characterWidth, rowHeight).orElse(null);
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition codeAreaCaretPosition, MovementDirection movementDirection) {
        return this.structure.computeMovePosition(codeAreaCaretPosition, movementDirection, this.dimensions.getRowsPerPage());
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public PositionScrollVisibility computePositionScrollVisibility(CodeAreaCaretPosition codeAreaCaretPosition) {
        int bytesPerRow = this.structure.getBytesPerRow();
        int previewCharPos = this.visibility.getPreviewCharPos();
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        int dataViewWidth = this.dimensions.getDataViewWidth();
        int dataViewHeight = this.dimensions.getDataViewHeight();
        int rowsPerPage = this.dimensions.getRowsPerPage();
        int charactersPerPage = this.dimensions.getCharactersPerPage();
        long dataPosition = codeAreaCaretPosition.getDataPosition();
        int i = (int) (dataPosition % ((long) bytesPerRow));
        return this.scrolling.computePositionScrollVisibility(dataPosition / ((long) bytesPerRow), codeAreaCaretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX) == BasicCodeAreaSection.TEXT_PREVIEW ? i + previewCharPos : this.structure.computeFirstCodeCharacterPos(i) + codeAreaCaretPosition.getCodeOffset(), bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth, dataViewHeight, characterWidth, rowHeight);
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public CodeAreaScrollPosition computeRevealScrollPosition(CodeAreaCaretPosition codeAreaCaretPosition) {
        int bytesPerRow = this.structure.getBytesPerRow();
        int previewCharPos = this.visibility.getPreviewCharPos();
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        int dataViewWidth = this.dimensions.getDataViewWidth();
        int dataViewHeight = this.dimensions.getDataViewHeight();
        int rowsPerPage = this.dimensions.getRowsPerPage();
        int charactersPerPage = this.dimensions.getCharactersPerPage();
        long dataPosition = codeAreaCaretPosition.getDataPosition();
        int i = (int) (dataPosition % ((long) bytesPerRow));
        return this.scrolling.computeRevealScrollPosition(dataPosition / ((long) bytesPerRow), codeAreaCaretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX) == BasicCodeAreaSection.TEXT_PREVIEW ? i + previewCharPos : this.structure.computeFirstCodeCharacterPos(i) + codeAreaCaretPosition.getCodeOffset(), bytesPerRow, rowsPerPage, charactersPerPage, dataViewWidth % characterWidth, dataViewHeight % rowHeight, characterWidth, rowHeight).orElse(null);
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition codeAreaScrollPosition, ScrollingDirection scrollingDirection) {
        return this.scrolling.computeScrolling(codeAreaScrollPosition, scrollingDirection, this.dimensions.getRowsPerPage(), this.structure.getRowsPerDocument());
    }

    public void dataViewScrolled(Canvas canvas) {
        if (isInitialized()) {
            recomputeScrollState();
            if (this.metrics.getCharacterWidth() > 0) {
                recomputeCharPositions();
                paintComponent(canvas);
            }
        }
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void detach() {
        this.codeArea.removeView(this.scrollPanel);
    }

    /* access modifiers changed from: protected */
    public void drawCenteredChars(Canvas canvas, char[] cArr, int i, int i2, int i3, int i4, int i5) {
        int i6;
        int i7 = 0;
        int i8 = 0;
        while (i8 < i2) {
            char c = cArr[i + i8];
            int charWidth = this.metrics.getCharWidth(c);
            boolean z = false;
            if (!this.metrics.hasUniformLineMetrics()) {
                z = this.metrics.getCharsWidth(cArr, (i + i8) - i7, i7 + 1) == (i7 + 1) * i3;
            } else if (charWidth == i3) {
                z = true;
            }
            switch (Character.getDirectionality(c)) {
                case -1:
                case 1:
                case 2:
                case 9:
                case 13:
                case CharsetStreamTranslator.BYTE_BUFFER_SIZE:
                case 17:
                case 18:
                    z = false;
                    break;
            }
            if (z) {
                i6 = i7 + 1;
            } else {
                if (i7 > 0) {
                    drawShiftedChars(canvas, cArr, (i + i8) - i7, i7, i4 + ((i8 - i7) * i3), i5);
                    i6 = 0;
                } else {
                    i6 = i7;
                }
                drawShiftedChars(canvas, cArr, i + i8, 1, (i8 * i3) + i4 + ((i3 - charWidth) / 2), i5);
            }
            i7 = i6;
            i8++;
        }
        if (i7 > 0) {
            drawShiftedChars(canvas, cArr, (i + i8) - i7, i7, i4 + ((i8 - i7) * i3), i5);
        }
    }

    /* access modifiers changed from: protected */
    public void drawShiftedChars(Canvas canvas, char[] cArr, int i, int i2, int i3, int i4) {
        canvas.drawText(cArr, i, i2, (float) i3, (float) i4, this.paint);
    }

    public void fontChanged() {
        if (this.font == null) {
            reset();
        }
        this.charset = this.codeArea.getCharset();
        this.font = this.codeArea.getCodeFont();
        Paint paint2 = new Paint();
        this.paint = paint2;
        paint2.setTextSize(30.0f);
        this.metrics.recomputeMetrics(this.paint, this.charset);
        recomputeDimensions();
        recomputeCharPositions();
        this.initialized = true;
    }

    @Override // org.exbin.bined.android.basic.color.BasicColorsCapableCodeAreaPainter
    public BasicCodeAreaColorsProfile getBasicColors() {
        return this.colorsProfile;
    }

    /* access modifiers changed from: protected */
    public int getCharactersPerRow() {
        return this.structure.getCharactersPerRow();
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public int getMouseCursorShape(int i, int i2) {
        int scrollPanelX = this.dimensions.getScrollPanelX();
        int scrollPanelY = this.dimensions.getScrollPanelY();
        return (i < scrollPanelX || i >= scrollPanelX + this.dimensions.getScrollPanelWidth() || i2 < scrollPanelY || i2 >= scrollPanelY + this.dimensions.getScrollPanelHeight()) ? 0 : 1;
    }

    public Integer getPositionBackgroundColor(long j, int i, int i2, CodeAreaSection codeAreaSection) {
        boolean z = false;
        SelectionRange selectionRange = this.structure.getSelectionRange();
        int codeLastCharPos = this.visibility.getCodeLastCharPos();
        CodeAreaCaret caret = this.codeArea.getCaret();
        boolean z2 = selectionRange != null && selectionRange.isInSelection(((long) i) + j);
        if (!(z2 && codeAreaSection == BasicCodeAreaSection.CODE_MATRIX && i2 == codeLastCharPos)) {
            z = z2;
        }
        if (!z) {
            return null;
        }
        return codeAreaSection == caret.getSection() ? this.colorsProfile.getSelectionBackground() : this.colorsProfile.getSelectionMirrorBackground();
    }

    public Point getPositionPoint(long j, int i, CodeAreaSection codeAreaSection) {
        int computeFirstCodeCharacterPos;
        int bytesPerRow = this.structure.getBytesPerRow();
        int rowsPerRect = this.dimensions.getRowsPerRect();
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
        long rowPosition = (j / ((long) bytesPerRow)) - scrollPosition.getRowPosition();
        if (rowPosition < -1 || rowPosition > ((long) rowsPerRect)) {
            return null;
        }
        int i2 = (int) (j % ((long) bytesPerRow));
        Rect dataViewRectangle = this.dimensions.getDataViewRectangle();
        int i3 = (int) ((rowPosition * ((long) rowHeight)) + ((long) dataViewRectangle.top));
        int rowOffset = scrollPosition.getRowOffset();
        if (codeAreaSection == BasicCodeAreaSection.TEXT_PREVIEW) {
            computeFirstCodeCharacterPos = (i2 * characterWidth) + dataViewRectangle.left + this.visibility.getPreviewRelativeX();
        } else {
            computeFirstCodeCharacterPos = ((this.structure.computeFirstCodeCharacterPos(i2) + i) * characterWidth) + dataViewRectangle.left;
        }
        return new Point(computeFirstCodeCharacterPos - ((characterWidth * scrollPosition.getCharPosition()) + scrollPosition.getCharOffset()), i3 - rowOffset);
    }

    public Rect getPositionRect(long j, int i, CodeAreaSection codeAreaSection) {
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        Point positionPoint = getPositionPoint(j, i, codeAreaSection);
        if (positionPoint == null) {
            return null;
        }
        return new Rect(positionPoint.x, positionPoint.y, DefaultCodeAreaCaret.getCursorThickness(this.editationOperation == EditationOperation.INSERT ? DefaultCodeAreaCaret.CursorShape.INSERT : DefaultCodeAreaCaret.CursorShape.OVERWRITE, characterWidth, rowHeight) + positionPoint.x, rowHeight + positionPoint.y);
    }

    public Integer getPositionTextColor(long j, int i, int i2, CodeAreaSection codeAreaSection) {
        SelectionRange selectionRange = this.structure.getSelectionRange();
        CodeAreaCaret caret = this.codeArea.getCaret();
        if (!(selectionRange != null && selectionRange.isInSelection(((long) i) + j))) {
            return null;
        }
        return codeAreaSection == caret.getSection() ? this.colorsProfile.getSelectionColor() : this.colorsProfile.getSelectionMirrorColor();
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public BasicCodeAreaZone getPositionZone(int i, int i2) {
        return this.dimensions.getPositionZone(i, i2);
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int i, int i2, CaretOverlapMode caretOverlapMode) {
        DefaultCodeAreaCaretPosition defaultCodeAreaCaretPosition = new DefaultCodeAreaCaretPosition();
        CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        int rowPositionAreaWidth = this.dimensions.getRowPositionAreaWidth();
        int headerAreaHeight = this.dimensions.getHeaderAreaHeight();
        int i3 = 0;
        if (i < rowPositionAreaWidth) {
            if (caretOverlapMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                i3 = 1;
            }
            i = rowPositionAreaWidth;
        }
        int charOffset = ((((i - rowPositionAreaWidth) + scrollPosition.getCharOffset()) / characterWidth) + scrollPosition.getCharPosition()) - i3;
        if (charOffset < 0) {
            charOffset = 0;
        }
        int i4 = 0;
        if (i2 < headerAreaHeight) {
            if (caretOverlapMode == CaretOverlapMode.PARTIAL_OVERLAP) {
                i4 = 1;
            }
            i2 = headerAreaHeight;
        }
        long rowOffset = (((long) (((i2 - headerAreaHeight) + scrollPosition.getRowOffset()) / rowHeight)) + scrollPosition.getRowPosition()) - ((long) i4);
        if (rowOffset < 0) {
            rowOffset = 0;
        }
        CodeAreaViewMode viewMode = this.structure.getViewMode();
        int previewCharPos = this.visibility.getPreviewCharPos();
        int bytesPerRow = this.structure.getBytesPerRow();
        CodeType codeType = this.structure.getCodeType();
        long dataSize = this.structure.getDataSize();
        int i5 = 0;
        if ((viewMode != CodeAreaViewMode.DUAL || charOffset >= previewCharPos) && viewMode != CodeAreaViewMode.CODE_MATRIX) {
            defaultCodeAreaCaretPosition.setSection(BasicCodeAreaSection.TEXT_PREVIEW);
            if (viewMode == CodeAreaViewMode.DUAL) {
                charOffset -= previewCharPos;
            }
        } else {
            defaultCodeAreaCaretPosition.setSection(BasicCodeAreaSection.CODE_MATRIX);
            int computePositionByte = this.structure.computePositionByte(charOffset);
            if (computePositionByte >= bytesPerRow) {
                i5 = 0;
                charOffset = computePositionByte;
            } else {
                i5 = charOffset - this.structure.computeFirstCodeCharacterPos(computePositionByte);
                if (i5 >= codeType.getMaxDigitsForByte()) {
                    i5 = codeType.getMaxDigitsForByte() - 1;
                }
                charOffset = computePositionByte;
            }
        }
        if (charOffset >= bytesPerRow) {
            charOffset = bytesPerRow - 1;
        }
        long j = (rowOffset * ((long) bytesPerRow)) + ((long) charOffset);
        if (j < 0) {
            j = 0;
            i5 = 0;
        }
        if (j >= dataSize) {
            i5 = 0;
            j = dataSize;
        }
        defaultCodeAreaCaretPosition.setDataPosition(j);
        defaultCodeAreaCaretPosition.setCodeOffset(i5);
        return defaultCodeAreaCaretPosition;
    }

    public void paintBackground(Canvas canvas) {
        int bytesPerRow = this.structure.getBytesPerRow();
        long dataSize = this.structure.getDataSize();
        int rowHeight = this.metrics.getRowHeight();
        int headerAreaHeight = this.dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = this.dimensions.getRowPositionAreaWidth();
        int rowsPerRect = this.dimensions.getRowsPerRect();
        int dataViewWidth = this.dimensions.getDataViewWidth();
        int dataViewHeight = this.dimensions.getDataViewHeight();
        CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
        this.paint.setColor(this.colorsProfile.getTextBackground());
        if (this.backgroundPaintMode != BasicBackgroundPaintMode.TRANSPARENT) {
            int i = this.dataViewOffsetX;
            int i2 = this.dataViewOffsetY;
            canvas.drawRect((float) (i + rowPositionAreaWidth), (float) (i2 + headerAreaHeight), (float) (i + rowPositionAreaWidth + dataViewWidth), (float) (dataViewHeight + i2 + headerAreaHeight), this.paint);
        }
        if (this.backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long rowPosition = ((long) bytesPerRow) * scrollPosition.getRowPosition();
            int rowOffset = (headerAreaHeight - scrollPosition.getRowOffset()) + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            this.paint.setColor(this.colorsProfile.getAlternateBackground());
            long j = rowPosition;
            int i3 = 0;
            int i4 = rowOffset;
            while (i3 <= rowsPerRect / 2 && j < dataSize) {
                int i5 = this.dataViewOffsetX;
                int i6 = this.dataViewOffsetY;
                canvas.drawRect((float) (i5 + rowPositionAreaWidth), (float) (i6 + i4), (float) (i5 + rowPositionAreaWidth + dataViewWidth), (float) (i6 + i4 + rowHeight), this.paint);
                j = ((long) (bytesPerRow * 2)) + j;
                i3++;
                i4 += rowHeight * 2;
            }
        }
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void paintComponent(Canvas canvas) {
        if (!this.initialized) {
            reset();
        }
        updateCache();
        if (this.font == null) {
            fontChanged();
            this.fontChanged = false;
        }
        if (this.rowDataCache != null && this.scrollingState != ScrollingState.SCROLLING_BY_SCROLLBAR) {
            if (this.layoutChanged) {
                recomputeLayout();
            }
            paintOutsiteArea(canvas);
            paintHeader(canvas);
            paintRowPosition(canvas);
            this.scrollPanel.invalidate();
            this.dataView.invalidate();
            this.paintCounter++;
        }
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void paintCursor(Canvas canvas) {
        if (this.caretChanged) {
            updateCaret();
        }
        int maxBytesPerChar = this.metrics.getMaxBytesPerChar();
        CodeType codeType = this.structure.getCodeType();
        this.structure.getViewMode();
        if (this.cursorDataCache == null) {
            this.cursorDataCache = new CursorDataCache();
        }
        int maxDigitsForByte = codeType.getMaxDigitsForByte();
        if (this.cursorDataCache.cursorCharsLength != maxDigitsForByte) {
            this.cursorDataCache.cursorCharsLength = maxDigitsForByte;
            this.cursorDataCache.cursorChars = new char[maxDigitsForByte];
        }
        if (this.cursorDataCache.cursorDataLength != maxBytesPerChar) {
            this.cursorDataCache.cursorDataLength = maxBytesPerChar;
            this.cursorDataCache.cursorData = new byte[maxBytesPerChar];
        }
        DefaultCodeAreaCaret defaultCodeAreaCaret = this.codeArea.getCaret();
        Rect positionRect = getPositionRect(defaultCodeAreaCaret.getDataPosition(), defaultCodeAreaCaret.getCodeOffset(), defaultCodeAreaCaret.getSection());
        if (positionRect != null) {
            Rect rect = new Rect(positionRect.left + this.dataViewOffsetX, positionRect.top + this.dataViewOffsetY, positionRect.right + this.dataViewOffsetX, positionRect.bottom + this.dataViewOffsetY);
            canvas.save();
            canvas.getClipBounds();
            Rect mainAreaRectangle = this.dimensions.getMainAreaRectangle();
            mainAreaRectangle.left += this.dataViewOffsetX;
            mainAreaRectangle.right += this.dataViewOffsetX;
            mainAreaRectangle.bottom += this.dataViewOffsetY;
            mainAreaRectangle.top += this.dataViewOffsetY;
            Rect computeIntersection = CodeAreaAndroidUtils.computeIntersection(mainAreaRectangle, rect);
            if (defaultCodeAreaCaret.isCursorVisible() && computeIntersection != null && !computeIntersection.isEmpty()) {
                canvas.clipRect(computeIntersection);
                DefaultCodeAreaCaret.CursorRenderingMode renderingMode = defaultCodeAreaCaret.getRenderingMode();
                this.paint.setColor(this.colorsProfile.getCursorColor());
                paintCursorRect(canvas, computeIntersection, rect, positionRect, renderingMode, defaultCodeAreaCaret);
            }
            canvas.restore();
        }
    }

    public void paintHeader(Canvas canvas) {
        int i;
        Integer num;
        int charactersPerCodeSection = this.visibility.getCharactersPerCodeSection();
        canvas.save();
        Rect clipBounds = canvas.getClipBounds();
        Rect headerAreaRectangle = this.dimensions.getHeaderAreaRectangle();
        CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
        canvas.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(headerAreaRectangle, clipBounds) : headerAreaRectangle);
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        int scrollPanelX = this.dimensions.getScrollPanelX();
        this.paint.setColor(this.colorsProfile.getTextBackground());
        canvas.drawRect((float) headerAreaRectangle.left, (float) headerAreaRectangle.top, (float) headerAreaRectangle.right, (float) headerAreaRectangle.bottom, this.paint);
        CodeAreaViewMode viewMode = this.structure.getViewMode();
        if (viewMode == CodeAreaViewMode.DUAL || viewMode == CodeAreaViewMode.CODE_MATRIX) {
            int charPosition = (scrollPanelX - (scrollPosition.getCharPosition() * characterWidth)) - scrollPosition.getCharOffset();
            int subFontSpace = rowHeight - this.metrics.getSubFontSpace();
            this.paint.setColor(this.colorsProfile.getTextColor());
            Arrays.fill(this.rowDataCache.headerChars, ' ');
            int skipToCode = this.visibility.getSkipToCode();
            int skipRestFromCode = this.visibility.getSkipRestFromCode();
            boolean z = false;
            int i2 = 0;
            for (int i3 = skipToCode; i3 < skipRestFromCode; i3++) {
                int computeFirstCodeCharacterPos = this.structure.computeFirstCodeCharacterPos(i3);
                if (computeFirstCodeCharacterPos != i2 + 2 || z) {
                    CodeAreaUtils.longToBaseCode(this.rowDataCache.headerChars, computeFirstCodeCharacterPos, i3, CodeType.HEXADECIMAL.getBase(), 2, true, this.codeCharactersCase);
                    z = false;
                } else {
                    z = true;
                    computeFirstCodeCharacterPos = i2;
                }
                i2 = computeFirstCodeCharacterPos;
            }
            int skipToChar = this.visibility.getSkipToChar();
            int min = Math.min(this.visibility.getSkipRestFromChar(), this.visibility.getCharactersPerCodeSection());
            Integer num2 = null;
            int i4 = skipToChar;
            for (int i5 = skipToChar; i5 < min; i5++) {
                boolean z2 = false;
                if (this.rowDataCache.headerChars[i5] == ' ' && i4 == i5) {
                    i4++;
                } else {
                    Integer valueOf = this.colorsProfile.getTextColor();
                    if (!CodeAreaAndroidUtils.areSameColors(valueOf, num2)) {
                        z2 = true;
                    }
                    if (z2) {
                        if (i4 < i5) {
                            drawCenteredChars(canvas, this.rowDataCache.headerChars, i4, i5 - i4, characterWidth, charPosition + (i4 * characterWidth), subFontSpace);
                        }
                        if (!CodeAreaAndroidUtils.areSameColors(valueOf, num2)) {
                            this.paint.setColor(valueOf);
                            num = valueOf;
                        } else {
                            num = num2;
                        }
                        i4 = i5;
                        num2 = num;
                    }
                }
            }
            if (i4 < charactersPerCodeSection) {
                drawCenteredChars(canvas, this.rowDataCache.headerChars, i4, charactersPerCodeSection - i4, characterWidth, charPosition + (i4 * characterWidth), subFontSpace);
            }
            i = scrollPanelX;
        } else {
            i = scrollPanelX;
        }
        this.paint.setColor(this.colorsProfile.getDecorationLine());
        canvas.drawLine((float) headerAreaRectangle.left, (float) ((headerAreaRectangle.top + headerAreaRectangle.height()) - 1), (float) (headerAreaRectangle.left + headerAreaRectangle.width()), (float) ((headerAreaRectangle.top + headerAreaRectangle.height()) - 1), this.paint);
        int previewRelativeX = (((this.visibility.getPreviewRelativeX() + i) - (scrollPosition.getCharPosition() * characterWidth)) - scrollPosition.getCharOffset()) - (characterWidth / 2);
        if (previewRelativeX >= i) {
            canvas.drawLine((float) previewRelativeX, (float) headerAreaRectangle.top, (float) previewRelativeX, (float) (headerAreaRectangle.top + headerAreaRectangle.height()), this.paint);
        }
        canvas.restore();
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void paintMainArea(Canvas canvas) {
        if (!this.initialized) {
            reset();
        }
        if (this.fontChanged) {
            fontChanged();
            this.fontChanged = false;
        }
        if (this.scrollingState != ScrollingState.SCROLLING_BY_SCROLLBAR) {
            canvas.save();
            paintBackground(canvas);
            Rect dataViewRectangle = this.dimensions.getDataViewRectangle();
            int characterWidth = this.metrics.getCharacterWidth();
            int previewRelativeX = this.visibility.getPreviewRelativeX();
            CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
            this.paint.setColor(this.colorsProfile.getDecorationLine());
            int charPosition = (((previewRelativeX + dataViewRectangle.left) - (scrollPosition.getCharPosition() * characterWidth)) - scrollPosition.getCharOffset()) - (characterWidth / 2);
            if (charPosition >= dataViewRectangle.left) {
                canvas.drawLine((float) (this.dataViewOffsetX + charPosition), (float) (this.dataViewOffsetY + dataViewRectangle.top), (float) (charPosition + this.dataViewOffsetX), (float) (this.dataViewOffsetY + dataViewRectangle.top + dataViewRectangle.height()), this.paint);
            }
            paintRows(canvas);
            canvas.restore();
            paintCursor(canvas);
            Rect componentRectangle = this.dimensions.getComponentRectangle();
            int rowHeight = this.metrics.getRowHeight();
            int width = (componentRectangle.width() - dataViewRectangle.left) - 500;
            int height = (componentRectangle.height() - dataViewRectangle.top) - 200;
            this.paint.setColor(-256);
            int i = this.dataViewOffsetX;
            int i2 = this.dataViewOffsetY;
            canvas.drawRect((float) (i + width), (float) (i2 + height), (float) (i + width + 400), (float) (i2 + height + rowHeight), this.paint);
            this.paint.setColor(-16777216);
            char[] charArray = (String.valueOf(scrollPosition.getCharPosition()) + "+" + String.valueOf(scrollPosition.getCharOffset()) + " : " + String.valueOf(scrollPosition.getRowPosition()) + "+" + String.valueOf(scrollPosition.getRowOffset()) + " P: " + String.valueOf(this.paintCounter)).toCharArray();
            canvas.drawText(charArray, 0, charArray.length, (float) (this.dataViewOffsetX + width), (float) (this.dataViewOffsetY + height + rowHeight), this.paint);
        }
    }

    public void paintOutsiteArea(Canvas canvas) {
        int headerAreaHeight = this.dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = this.dimensions.getRowPositionAreaWidth();
        Rect componentRectangle = this.dimensions.getComponentRectangle();
        int characterWidth = this.metrics.getCharacterWidth();
        this.paint.setColor(this.colorsProfile.getTextBackground());
        canvas.drawRect((float) componentRectangle.left, (float) componentRectangle.top, (float) componentRectangle.width(), (float) headerAreaHeight, this.paint);
        this.paint.setColor(this.colorsProfile.getDecorationLine());
        canvas.drawLine(0.0f, (float) (headerAreaHeight - 1), (float) rowPositionAreaWidth, (float) (headerAreaHeight - 1), this.paint);
        int i = rowPositionAreaWidth - (characterWidth / 2);
        if (i >= 0) {
            canvas.drawLine((float) i, 0.0f, (float) i, (float) headerAreaHeight, this.paint);
        }
    }

    public void paintRowBackground(Canvas canvas, long j, int i, int i2) {
        int computePositionByte;
        BasicCodeAreaSection basicCodeAreaSection;
        int i3;
        int previewCharPos = this.visibility.getPreviewCharPos();
        CodeAreaViewMode viewMode = this.structure.getViewMode();
        int charactersPerRow = this.structure.getCharactersPerRow();
        int skipToChar = this.visibility.getSkipToChar();
        int skipRestFromChar = this.visibility.getSkipRestFromChar();
        Integer num = null;
        int i4 = skipToChar;
        int i5 = skipToChar;
        while (i5 < skipRestFromChar) {
            if (i5 < previewCharPos || viewMode == CodeAreaViewMode.CODE_MATRIX) {
                computePositionByte = this.structure.computePositionByte(i5);
                basicCodeAreaSection = BasicCodeAreaSection.CODE_MATRIX;
            } else {
                basicCodeAreaSection = BasicCodeAreaSection.TEXT_PREVIEW;
                computePositionByte = i5 - previewCharPos;
            }
            Integer positionBackgroundColor = getPositionBackgroundColor(j, computePositionByte, i5, basicCodeAreaSection);
            if (!CodeAreaAndroidUtils.areSameColors(positionBackgroundColor, num)) {
                if (i4 < i5 && num != null) {
                    int i6 = this.dataViewOffsetX;
                    int i7 = this.dataViewOffsetY;
                    renderBackgroundSequence(canvas, i6 + i4, i7 + i5, i6 + i, i7 + i2);
                }
                if (CodeAreaAndroidUtils.areSameColors(positionBackgroundColor, num)) {
                    positionBackgroundColor = num;
                } else if (positionBackgroundColor != null) {
                    this.paint.setColor(positionBackgroundColor);
                }
                i3 = i5;
            } else {
                positionBackgroundColor = num;
                i3 = i4;
            }
            i5++;
            num = positionBackgroundColor;
            i4 = i3;
        }
        if (i4 < charactersPerRow && num != null) {
            int i8 = this.dataViewOffsetX;
            int i9 = this.dataViewOffsetY;
            renderBackgroundSequence(canvas, i8 + i4, i9 + charactersPerRow, i8 + i, i9 + i2);
        }
    }

    public void paintRowPosition(Canvas canvas) {
        int bytesPerRow = this.structure.getBytesPerRow();
        long dataSize = this.structure.getDataSize();
        int rowHeight = this.metrics.getRowHeight();
        int characterWidth = this.metrics.getCharacterWidth();
        int subFontSpace = this.metrics.getSubFontSpace();
        int rowsPerRect = this.dimensions.getRowsPerRect();
        int headerAreaHeight = this.dimensions.getHeaderAreaHeight();
        int rowPositionAreaWidth = this.dimensions.getRowPositionAreaWidth();
        canvas.save();
        Rect dataViewRectangle = this.dimensions.getDataViewRectangle();
        Rect clipBounds = canvas.getClipBounds();
        Rect rowPositionAreaRectangle = this.dimensions.getRowPositionAreaRectangle();
        canvas.clipRect(clipBounds != null ? CodeAreaAndroidUtils.computeIntersection(rowPositionAreaRectangle, clipBounds) : rowPositionAreaRectangle);
        this.paint.setColor(this.colorsProfile.getTextBackground());
        canvas.drawRect((float) rowPositionAreaRectangle.left, (float) rowPositionAreaRectangle.top, (float) rowPositionAreaRectangle.right, (float) rowPositionAreaRectangle.bottom, this.paint);
        CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
        if (this.backgroundPaintMode == BasicBackgroundPaintMode.STRIPED) {
            long rowPosition = ((long) bytesPerRow) * scrollPosition.getRowPosition();
            int rowOffset = (headerAreaHeight - scrollPosition.getRowOffset()) + ((scrollPosition.getRowPosition() & 1) > 0 ? 0 : rowHeight);
            this.paint.setColor(this.colorsProfile.getAlternateBackground());
            for (int i = 0; i <= rowsPerRect / 2 && rowPosition < dataSize; i++) {
                canvas.drawRect(0.0f, (float) rowOffset, (float) rowPositionAreaWidth, (float) (rowOffset + rowHeight), this.paint);
                rowOffset += rowHeight * 2;
                rowPosition = ((long) (bytesPerRow * 2)) + rowPosition;
            }
        }
        long rowPosition2 = scrollPosition.getRowPosition();
        int rowOffset2 = scrollPosition.getRowOffset();
        this.paint.setColor(this.colorsProfile.getTextColor());
        Rectangle rectangle = new Rectangle();
        long j = ((long) bytesPerRow) * rowPosition2;
        int i2 = ((headerAreaHeight + rowHeight) - subFontSpace) - rowOffset2;
        for (int i3 = 0; i3 <= rowsPerRect && j <= dataSize; i3++) {
            CodeAreaUtils.longToBaseCode(this.rowDataCache.rowPositionCode, 0, j < 0 ? 0 : j, this.structure.getCodeType().getBase(), this.rowPositionLength, true, CodeCharactersCase.UPPER);
            drawCenteredChars(canvas, this.rowDataCache.rowPositionCode, 0, this.rowPositionLength, characterWidth, rectangle.x, i2);
            i2 += rowHeight;
            j += bytesPerRow;
        }
        this.paint.setColor(this.colorsProfile.getDecorationLine());
        int i4 = rowPositionAreaWidth - (characterWidth / 2);
        if (i4 >= 0) {
            canvas.drawLine((float) i4, (float) dataViewRectangle.top, (float) i4, (float) (dataViewRectangle.top + dataViewRectangle.height()), this.paint);
        }
        canvas.drawLine((float) dataViewRectangle.left, (float) (dataViewRectangle.top - 1), (float) (dataViewRectangle.left + dataViewRectangle.width()), (float) (dataViewRectangle.top - 1), this.paint);
        canvas.restore();
    }

    public void paintRowText(Canvas canvas, long j, int i, int i2) {
        int computePositionByte;
        BasicCodeAreaSection basicCodeAreaSection;
        boolean z;
        Integer num;
        Integer num2;
        int previewCharPos = this.visibility.getPreviewCharPos();
        int charactersPerRow = this.structure.getCharactersPerRow();
        int rowHeight = this.metrics.getRowHeight();
        int characterWidth = this.metrics.getCharacterWidth();
        int subFontSpace = (rowHeight + i2) - this.metrics.getSubFontSpace();
        int skipToChar = this.visibility.getSkipToChar();
        int skipRestFromChar = this.visibility.getSkipRestFromChar();
        Integer num3 = null;
        Integer num4 = null;
        int i3 = skipToChar;
        int i4 = skipToChar;
        while (i4 < skipRestFromChar) {
            if (i4 >= previewCharPos) {
                basicCodeAreaSection = BasicCodeAreaSection.TEXT_PREVIEW;
                computePositionByte = i4 - previewCharPos;
            } else {
                computePositionByte = this.structure.computePositionByte(i4);
                basicCodeAreaSection = BasicCodeAreaSection.CODE_MATRIX;
            }
            Integer positionTextColor = getPositionTextColor(j, computePositionByte, i4, basicCodeAreaSection);
            if (positionTextColor == null) {
                positionTextColor = this.colorsProfile.getTextColor();
            }
            if (!CodeAreaAndroidUtils.areSameColors(positionTextColor, num4)) {
                num = num4 == null ? positionTextColor : num4;
                z = true;
            } else {
                z = false;
                num = num4;
            }
            if (z) {
                if (!CodeAreaAndroidUtils.areSameColors(num3, num)) {
                    this.paint.setColor(num);
                } else {
                    num = num3;
                }
                if (i4 > i3) {
                    drawCenteredChars(canvas, this.rowDataCache.rowCharacters, i3, i4 - i3, characterWidth, this.dataViewOffsetX + i + (i3 * characterWidth), this.dataViewOffsetY + subFontSpace);
                }
                if (!CodeAreaAndroidUtils.areSameColors(num, positionTextColor)) {
                    this.paint.setColor(positionTextColor);
                    num = positionTextColor;
                }
                num2 = num;
                i3 = i4;
            } else {
                positionTextColor = num;
                num2 = num3;
            }
            i4++;
            num4 = positionTextColor;
            num3 = num2;
        }
        if (i3 < charactersPerRow) {
            if (!CodeAreaAndroidUtils.areSameColors(num3, num4)) {
                this.paint.setColor(num4);
            }
            drawCenteredChars(canvas, this.rowDataCache.rowCharacters, i3, charactersPerRow - i3, characterWidth, this.dataViewOffsetX + i + (i3 * characterWidth), this.dataViewOffsetY + subFontSpace);
        }
    }

    public void paintRows(Canvas canvas) {
        int bytesPerRow = this.structure.getBytesPerRow();
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        int scrollPanelX = this.dimensions.getScrollPanelX();
        int scrollPanelY = this.dimensions.getScrollPanelY();
        int rowsPerRect = this.dimensions.getRowsPerRect();
        CodeAreaScrollPosition scrollPosition = this.scrolling.getScrollPosition();
        int charPosition = (scrollPanelX - (characterWidth * scrollPosition.getCharPosition())) - scrollPosition.getCharOffset();
        int rowOffset = scrollPosition.getRowOffset();
        this.paint.setColor(this.colorsProfile.getTextColor());
        long rowPosition = scrollPosition.getRowPosition() * ((long) bytesPerRow);
        int i = scrollPanelY - rowOffset;
        for (int i2 = 0; i2 <= rowsPerRect; i2++) {
            prepareRowData(rowPosition);
            paintRowBackground(canvas, rowPosition, charPosition, i);
            paintRowText(canvas, rowPosition, charPosition, i);
            i += rowHeight;
            rowPosition += bytesPerRow;
        }
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void rebuildColors() {
        this.colorsProfile.reinitialize();
    }

    public void recomputeCharPositions() {
        this.visibility.recomputeCharPositions(this.metrics, this.structure, this.dimensions, this.layout, this.scrolling);
        updateRowDataCache();
    }

    public void recomputeLayout() {
        this.rowPositionLength = getRowPositionLength();
        recomputeDimensions();
        this.dataViewOffsetX = this.scrollOffsetX - this.dimensions.getScrollPanelX();
        this.dataViewOffsetY = this.scrollOffsetY - this.dimensions.getScrollPanelY();
        this.scrollPanel.layout(this.dimensions.getRowPositionAreaWidth(), this.dimensions.getHeaderAreaHeight(), this.dimensions.getRowPositionAreaWidth() + this.dimensions.getDataViewWidth(), this.dimensions.getHeaderAreaHeight() + this.dimensions.getDataViewHeight());
        int charactersPerPage = this.dimensions.getCharactersPerPage();
        this.structure.updateCache(this.codeArea, charactersPerPage);
        this.codeCharactersCase = this.codeArea.getCodeCharactersCase();
        this.backgroundPaintMode = this.codeArea.getBackgroundPaintMode();
        boolean showMirrorCursor = this.codeArea.isShowMirrorCursor();
        this.minRowPositionLength = this.codeArea.getMinRowPositionLength();
        this.maxRowPositionLength = this.codeArea.getMaxRowPositionLength();
        int rowsPerPage = this.dimensions.getRowsPerPage();
        long rowsPerDocument = this.structure.getRowsPerDocument();
        int charactersPerRow = this.structure.getCharactersPerRow();
        if (this.metrics.isInitialized()) {
            this.scrolling.updateMaximumScrollPosition(rowsPerDocument, rowsPerPage, charactersPerRow, charactersPerPage, this.dimensions.getLastCharOffset(), this.dimensions.getLastRowOffset());
        }
        recomputeScrollState();
        this.layoutChanged = false;
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void reset() {
        resetColors();
        resetFont();
        resetLayout();
        resetCaret();
        recomputeScrollState();
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void resetCaret() {
        this.caretChanged = true;
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void resetColors() {
        this.resetColors = true;
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void resetFont() {
        this.fontChanged = true;
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void resetLayout() {
        this.layoutChanged = true;
    }

    @Override // org.exbin.bined.android.basic.color.BasicColorsCapableCodeAreaPainter
    public void setBasicColors(BasicCodeAreaColorsProfile basicCodeAreaColorsProfile) {
        this.colorsProfile = basicCodeAreaColorsProfile;
    }

    /* access modifiers changed from: protected */
    public void updateCache() {
        synchronized (this) {
            if (this.resetColors) {
                this.resetColors = false;
                this.colorsProfile.reinitialize();
            }
        }
    }

    @Override // org.exbin.bined.android.CodeAreaPainter
    public void updateScrollBars() {
        int characterWidth = this.metrics.getCharacterWidth();
        int rowHeight = this.metrics.getRowHeight();
        long rowsPerDocument = this.structure.getRowsPerDocument();
        recomputeScrollState();
        this.scrollPanel.getVerticalScrollbarPosition();
        this.scrollPanel.setVerticalScrollbarPosition(this.scrolling.getVerticalScrollValue(rowHeight, rowsPerDocument));
        this.scrolling.getHorizontalScrollValue(characterWidth);
    }
}
