package org.exbin.bined.android.basic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeCharactersCase;
import org.exbin.bined.CodeType;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationModeChangedListener;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaAndroidControl;
import org.exbin.bined.android.CodeAreaAndroidUtils;
import org.exbin.bined.android.CodeAreaCommandHandler;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.android.CodeAreaPainter;
import org.exbin.bined.android.Font;
import org.exbin.bined.android.basic.color.BasicCodeAreaColorsProfile;
import org.exbin.bined.android.basic.color.BasicColorsCapableCodeAreaPainter;
import org.exbin.bined.basic.BasicBackgroundPaintMode;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.basic.HorizontalScrollUnit;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.basic.VerticalScrollUnit;

public class CodeArea extends CodeAreaCore implements DefaultCodeArea, CodeAreaAndroidControl {
    private BasicBackgroundPaintMode borderPaintMode = BasicBackgroundPaintMode.STRIPED;
    private final DefaultCodeAreaCaret caret = new DefaultCodeAreaCaret(this);
    private final List<CaretMovedListener> caretMovedListeners = new ArrayList<>();
    private Charset charset = Charset.forName(CodeAreaAndroidUtils.DEFAULT_ENCODING);
    private ClipboardHandlingMode clipboardHandlingMode = ClipboardHandlingMode.PROCESS;
    private CodeCharactersCase codeCharactersCase = CodeCharactersCase.UPPER;
    private Font codeFont;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private EditationMode editationMode = EditationMode.EXPANDING;
    private final List<EditationModeChangedListener> editationModeChangedListeners = new ArrayList<>();
    private EditationOperation editationOperation = EditationOperation.OVERWRITE;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;
    private int maxBytesPerRow = 16;
    private int maxRowPositionLength = 0;
    private int minRowPositionLength = 0;
    private CodeAreaPainter painter;
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    private final List<ScrollingListener> scrollingListeners = new ArrayList<>();
    private final SelectionRange selection = new SelectionRange();
    private final List<SelectionChangedListener> selectionChangedListeners = new ArrayList<>();
    private boolean showMirrorCursor = true;
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.PIXEL;
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    private int wrappingBytesGroupSize = 0;

    /* access modifiers changed from: package-private */
    /* renamed from: org.exbin.bined.android.basic.CodeArea$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$EditationMode;
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$basic$CodeAreaViewMode;

        static {
            int[] iArr = new int[EditationMode.values().length];
            $SwitchMap$org$exbin$bined$EditationMode = iArr;
            try {
                iArr[EditationMode.READ_ONLY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$exbin$bined$EditationMode[EditationMode.INPLACE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$exbin$bined$EditationMode[EditationMode.CAPPED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$exbin$bined$EditationMode[EditationMode.EXPANDING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            int[] iArr2 = new int[CodeAreaViewMode.values().length];
            $SwitchMap$org$exbin$bined$basic$CodeAreaViewMode = iArr2;
            try {
                iArr2[CodeAreaViewMode.CODE_MATRIX.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$CodeAreaViewMode[CodeAreaViewMode.TEXT_PREVIEW.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    public CodeArea(Context context, AttributeSet attributeSet) {
        super(context, attributeSet, DefaultCodeAreaCommandHandler.createDefaultCodeAreaCommandHandlerFactory(context));
        DefaultCodeAreaPainter defaultCodeAreaPainter = new DefaultCodeAreaPainter(this);
        this.painter = defaultCodeAreaPainter;
        defaultCodeAreaPainter.attach();
        init();
    }

    public CodeArea(Context context, AttributeSet attributeSet, CodeAreaCommandHandler.CodeAreaCommandHandlerFactory codeAreaCommandHandlerFactory) {
        super(context, attributeSet, codeAreaCommandHandlerFactory);
        DefaultCodeAreaPainter defaultCodeAreaPainter = new DefaultCodeAreaPainter(this);
        this.painter = defaultCodeAreaPainter;
        defaultCodeAreaPainter.attach();
        init();
    }

    private void init() {
        this.caret.setSection(BasicCodeAreaSection.CODE_MATRIX);
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public void addCaretMovedListener(CaretMovedListener caretMovedListener) {
        this.caretMovedListeners.add(caretMovedListener);
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public void addEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener) {
        this.editationModeChangedListeners.add(editationModeChangedListener);
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void addScrollingListener(ScrollingListener scrollingListener) {
        this.scrollingListeners.add(scrollingListener);
    }

    @Override // org.exbin.bined.capability.SelectionCapable
    public void addSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        this.selectionChangedListeners.add(selectionChangedListener);
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void centerOnCursor() {
        centerOnPosition(this.caret.getCaretPosition());
    }

    public void centerOnPosition(long j, int i, CodeAreaSection codeAreaSection) {
        centerOnPosition(new DefaultCodeAreaCaretPosition(j, i, codeAreaSection));
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void centerOnPosition(CodeAreaCaretPosition codeAreaCaretPosition) {
        CodeAreaScrollPosition computeCenterOnScrollPosition;
        if (isInitialized() && (computeCenterOnScrollPosition = this.painter.computeCenterOnScrollPosition(codeAreaCaretPosition)) != null) {
            setScrollPosition(computeCenterOnScrollPosition);
            resetPainter();
            updateScrollBars();
            notifyScrolled();
        }
    }

    @Override // org.exbin.bined.capability.SelectionCapable, org.exbin.bined.android.CodeAreaCore, org.exbin.bined.CodeAreaControl
    public void clearSelection() {
        this.selection.clearSelection();
        notifySelectionChanged();
        repaint();
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition codeAreaCaretPosition, MovementDirection movementDirection) {
        return this.painter.computeMovePosition(codeAreaCaretPosition, movementDirection);
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition codeAreaScrollPosition, ScrollingDirection scrollingDirection) {
        return this.painter.computeScrolling(codeAreaScrollPosition, scrollingDirection);
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public EditationOperation getActiveOperation() {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$EditationMode[this.editationMode.ordinal()]) {
            case 1:
                return EditationOperation.INSERT;
            case 2:
                return EditationOperation.OVERWRITE;
            case 3:
            case 4:
                return this.editationOperation;
            default:
                throw new IllegalStateException("Unexpected code type: " + this.editationMode.name());
        }
    }

    public CodeAreaSection getActiveSection() {
        return this.caret.getSection();
    }

    @Override // org.exbin.bined.android.capability.BackgroundPaintCapable
    public BasicBackgroundPaintMode getBackgroundPaintMode() {
        return this.borderPaintMode;
    }

    @Override // org.exbin.bined.android.capability.BasicColorsCapable
    public BasicCodeAreaColorsProfile getBasicColors() {
        CodeAreaPainter codeAreaPainter = this.painter;
        if (codeAreaPainter instanceof BasicColorsCapableCodeAreaPainter) {
            return ((BasicColorsCapableCodeAreaPainter) codeAreaPainter).getBasicColors();
        }
        return null;
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public DefaultCodeAreaCaret getCaret() {
        return this.caret;
    }

    public CodeAreaCaretPosition getCaretPosition() {
        return this.caret.getCaretPosition();
    }

    @Override // org.exbin.bined.capability.CharsetCapable
    public Charset getCharset() {
        return this.charset;
    }

    @Override // org.exbin.bined.capability.ClipboardCapable
    public ClipboardHandlingMode getClipboardHandlingMode() {
        return this.clipboardHandlingMode;
    }

    @Override // org.exbin.bined.capability.CodeCharactersCaseCapable
    public CodeCharactersCase getCodeCharactersCase() {
        return this.codeCharactersCase;
    }

    @Override // org.exbin.bined.android.capability.FontCapable
    public Font getCodeFont() {
        return CodeAreaUtils.requireNonNull(this.codeFont);
    }

    public int getCodeOffset() {
        return this.caret.getCodeOffset();
    }

    @Override // org.exbin.bined.capability.CodeTypeCapable
    public CodeType getCodeType() {
        return this.codeType;
    }

    public long getDataPosition() {
        return this.caret.getDataPosition();
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public EditationMode getEditationMode() {
        return this.editationMode;
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public EditationOperation getEditationOperation() {
        return this.editationOperation;
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return this.horizontalScrollBarVisibility;
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return this.horizontalScrollUnit;
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public int getMaxBytesPerRow() {
        return this.maxBytesPerRow;
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public int getMaxRowPositionLength() {
        return this.maxRowPositionLength;
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public int getMinRowPositionLength() {
        return this.minRowPositionLength;
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public int getMouseCursorShape(int i, int i2) {
        return this.painter.getMouseCursorShape(i, i2);
    }

    public CodeAreaPainter getPainter() {
        return this.painter;
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public BasicCodeAreaZone getPositionZone(int i, int i2) {
        return this.painter.getPositionZone(i, i2);
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public RowWrappingMode getRowWrapping() {
        return this.rowWrapping;
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public CodeAreaScrollPosition getScrollPosition() {
        return this.scrollPosition;
    }

    @Override // org.exbin.bined.capability.SelectionCapable
    public SelectionRange getSelection() {
        return this.selection;
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return this.verticalScrollBarVisibility;
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public VerticalScrollUnit getVerticalScrollUnit() {
        return this.verticalScrollUnit;
    }

    @Override // org.exbin.bined.capability.ViewModeCapable
    public CodeAreaViewMode getViewMode() {
        return this.viewMode;
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public int getWrappingBytesGroupSize() {
        return this.wrappingBytesGroupSize;
    }

    @Override // org.exbin.bined.capability.SelectionCapable, org.exbin.bined.android.CodeAreaCore, org.exbin.bined.CodeAreaControl
    public boolean hasSelection() {
        return !this.selection.isEmpty();
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public boolean isEditable() {
        return this.editationMode != EditationMode.READ_ONLY;
    }

    public boolean isInitialized() {
        return this.painter.isInitialized();
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public boolean isShowMirrorCursor() {
        return this.showMirrorCursor;
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public CodeAreaCaretPosition mousePositionToClosestCaretPosition(int i, int i2, CaretOverlapMode caretOverlapMode) {
        return this.painter.mousePositionToClosestCaretPosition(i, i2, caretOverlapMode);
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public void notifyCaretChanged() {
        repaint();
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public void notifyCaretMoved() {
        for (CaretMovedListener caretMovedListener : this.caretMovedListeners) {
            caretMovedListener.caretMoved(this.caret.getCaretPosition());
        }
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void notifyScrolled() {
        for (ScrollingListener scrollingListener : this.scrollingListeners) {
            scrollingListener.scrolled();
        }
    }

    public void notifySelectionChanged() {
        for (SelectionChangedListener selectionChangedListener : this.selectionChangedListeners) {
            selectionChangedListener.selectionChanged(this.selection);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {
            if (!isInitialized()) {
                setCodeFont(Font.fromPaint(new Paint()));
            }
            paintComponent(canvas);
        }
    }

    @Override // org.exbin.bined.android.CodeAreaAndroidControl
    public void paintComponent(Canvas canvas) {
        this.painter.paintComponent(canvas);
    }

    @Override // org.exbin.bined.android.CodeAreaCore
    public void paintView(Canvas canvas) {
        if (!isInitialized()) {
            setCodeFont(Font.fromPaint(new Paint()));
        }
        paintComponent(canvas);
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public void removeCaretMovedListener(CaretMovedListener caretMovedListener) {
        this.caretMovedListeners.remove(caretMovedListener);
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public void removeEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener) {
        this.editationModeChangedListeners.remove(editationModeChangedListener);
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void removeScrollingListener(ScrollingListener scrollingListener) {
        this.scrollingListeners.remove(scrollingListener);
    }

    @Override // org.exbin.bined.capability.SelectionCapable
    public void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener) {
        this.selectionChangedListeners.remove(selectionChangedListener);
    }

    @Override // org.exbin.bined.android.CodeAreaCore
    public void repaint() {
        super.repaint();
    }

    @Override // org.exbin.bined.android.CodeAreaAndroidControl
    public void reset() {
        resetPainter();
    }

    @Override // org.exbin.bined.android.CodeAreaAndroidControl
    public void resetColors() {
        this.painter.resetColors();
        repaint();
    }

    @Override // org.exbin.bined.android.CodeAreaCore
    public void resetPainter() {
        this.painter.reset();
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void revealCursor() {
        revealPosition(this.caret.getCaretPosition());
    }

    public void revealPosition(long j, int i, CodeAreaSection codeAreaSection) {
        revealPosition(new DefaultCodeAreaCaretPosition(j, i, codeAreaSection));
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void revealPosition(CodeAreaCaretPosition codeAreaCaretPosition) {
        CodeAreaScrollPosition computeRevealScrollPosition;
        if (isInitialized() && (computeRevealScrollPosition = this.painter.computeRevealScrollPosition(codeAreaCaretPosition)) != null) {
            setScrollPosition(computeRevealScrollPosition);
            resetPainter();
            updateScrollBars();
            notifyScrolled();
        }
    }

    @Override // org.exbin.bined.android.capability.BackgroundPaintCapable
    public void setBackgroundPaintMode(BasicBackgroundPaintMode basicBackgroundPaintMode) {
        this.borderPaintMode = basicBackgroundPaintMode;
        updateLayout();
    }

    @Override // org.exbin.bined.android.capability.BasicColorsCapable
    public void setBasicColors(BasicCodeAreaColorsProfile basicCodeAreaColorsProfile) {
        CodeAreaPainter codeAreaPainter = this.painter;
        if (codeAreaPainter instanceof BasicColorsCapableCodeAreaPainter) {
            ((BasicColorsCapableCodeAreaPainter) codeAreaPainter).setBasicColors(basicCodeAreaColorsProfile);
        }
    }

    public void setCaretPosition(long j) {
        this.caret.setCaretPosition(j);
        notifyCaretMoved();
    }

    public void setCaretPosition(long j, int i) {
        this.caret.setCaretPosition(j, i);
        notifyCaretMoved();
    }

    public void setCaretPosition(CodeAreaCaretPosition codeAreaCaretPosition) {
        this.caret.setCaretPosition(codeAreaCaretPosition);
        notifyCaretMoved();
    }

    @Override // org.exbin.bined.capability.CharsetCapable
    public void setCharset(Charset charset2) {
        CodeAreaUtils.requireNonNull(charset2);
        this.charset = charset2;
        reset();
        repaint();
    }

    @Override // org.exbin.bined.capability.ClipboardCapable
    public void setClipboardHandlingMode(ClipboardHandlingMode clipboardHandlingMode2) {
        this.clipboardHandlingMode = clipboardHandlingMode2;
    }

    @Override // org.exbin.bined.capability.CodeCharactersCaseCapable
    public void setCodeCharactersCase(CodeCharactersCase codeCharactersCase2) {
        this.codeCharactersCase = codeCharactersCase2;
        updateLayout();
        repaint();
    }

    @Override // org.exbin.bined.android.capability.FontCapable
    public void setCodeFont(Font font) {
        this.codeFont = font;
        this.painter.resetFont();
        repaint();
    }

    @Override // org.exbin.bined.capability.CodeTypeCapable
    public void setCodeType(CodeType codeType2) {
        this.codeType = codeType2;
        updateLayout();
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public void setEditationMode(EditationMode editationMode2) {
        boolean z = editationMode2 != this.editationMode;
        this.editationMode = editationMode2;
        if (z) {
            for (EditationModeChangedListener editationModeChangedListener : this.editationModeChangedListeners) {
                editationModeChangedListener.editationModeChanged(editationMode2, getActiveOperation());
            }
            this.caret.resetBlink();
            notifyCaretChanged();
            repaint();
        }
    }

    @Override // org.exbin.bined.capability.EditationModeCapable
    public void setEditationOperation(EditationOperation editationOperation2) {
        EditationOperation activeOperation = getActiveOperation();
        this.editationOperation = editationOperation2;
        EditationOperation activeOperation2 = getActiveOperation();
        if (activeOperation != activeOperation2) {
            for (EditationModeChangedListener editationModeChangedListener : this.editationModeChangedListeners) {
                editationModeChangedListener.editationModeChanged(this.editationMode, activeOperation2);
            }
            this.caret.resetBlink();
            notifyCaretChanged();
            repaint();
        }
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public void setHorizontalScrollBarVisibility(ScrollBarVisibility scrollBarVisibility) {
        this.horizontalScrollBarVisibility = scrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit2) {
        this.horizontalScrollUnit = horizontalScrollUnit2;
        int charPosition = this.scrollPosition.getCharPosition();
        if (horizontalScrollUnit2 == HorizontalScrollUnit.CHARACTER) {
            this.scrollPosition.setCharOffset(0);
        }
        resetPainter();
        this.scrollPosition.setCharPosition(charPosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public void setMaxBytesPerRow(int i) {
        this.maxBytesPerRow = i;
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public void setMaxRowPositionLength(int i) {
        this.maxRowPositionLength = i;
        updateLayout();
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public void setMinRowPositionLength(int i) {
        this.minRowPositionLength = i;
        updateLayout();
    }

    public void setPainter(CodeAreaPainter codeAreaPainter) {
        CodeAreaUtils.requireNonNull(codeAreaPainter);
        this.painter.detach();
        this.painter = codeAreaPainter;
        codeAreaPainter.attach();
        reset();
        repaint();
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public void setRowWrapping(RowWrappingMode rowWrappingMode) {
        this.rowWrapping = rowWrappingMode;
        updateLayout();
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void setScrollPosition(CodeAreaScrollPosition codeAreaScrollPosition) {
        this.scrollPosition.setScrollPosition(codeAreaScrollPosition);
    }

    @Override // org.exbin.bined.capability.SelectionCapable
    public void setSelection(long j, long j2) {
        this.selection.setSelection(j, j2);
        notifySelectionChanged();
        repaint();
    }

    @Override // org.exbin.bined.capability.SelectionCapable
    public void setSelection(SelectionRange selectionRange) {
        CodeAreaUtils.requireNonNull(selectionRange);
        this.selection.setSelection(selectionRange);
        notifySelectionChanged();
        repaint();
    }

    @Override // org.exbin.bined.capability.CaretCapable
    public void setShowMirrorCursor(boolean z) {
        this.showMirrorCursor = z;
        repaint();
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public void setVerticalScrollBarVisibility(ScrollBarVisibility scrollBarVisibility) {
        this.verticalScrollBarVisibility = scrollBarVisibility;
        resetPainter();
        updateScrollBars();
    }

    @Override // org.exbin.bined.capability.BasicScrollingCapable
    public void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit2) {
        this.verticalScrollUnit = verticalScrollUnit2;
        long rowPosition = this.scrollPosition.getRowPosition();
        if (verticalScrollUnit2 == VerticalScrollUnit.ROW) {
            this.scrollPosition.setRowOffset(0);
        }
        resetPainter();
        this.scrollPosition.setRowPosition(rowPosition);
        updateScrollBars();
        notifyScrolled();
    }

    @Override // org.exbin.bined.capability.ViewModeCapable
    public void setViewMode(CodeAreaViewMode codeAreaViewMode) {
        if (codeAreaViewMode != this.viewMode) {
            this.viewMode = codeAreaViewMode;
            switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$CodeAreaViewMode[codeAreaViewMode.ordinal()]) {
                case 1:
                    getCaret().setSection(BasicCodeAreaSection.CODE_MATRIX);
                    reset();
                    notifyCaretMoved();
                    break;
                case 2:
                    getCaret().setSection(BasicCodeAreaSection.TEXT_PREVIEW);
                    reset();
                    notifyCaretMoved();
                    break;
                default:
                    reset();
                    break;
            }
            updateLayout();
        }
    }

    @Override // org.exbin.bined.capability.RowWrappingCapable
    public void setWrappingBytesGroupSize(int i) {
        this.wrappingBytesGroupSize = i;
        updateLayout();
    }

    @Override // org.exbin.bined.android.CodeAreaAndroidControl, org.exbin.bined.android.CodeAreaCore
    public void updateLayout() {
        this.painter.resetLayout();
    }

    @Override // org.exbin.bined.capability.ScrollingCapable
    public void updateScrollBars() {
        this.painter.updateScrollBars();
        repaint();
    }
}
