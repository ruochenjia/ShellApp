package org.exbin.bined.android.basic;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import java.nio.ByteBuffer;
import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.auxiliary.paged_data.EditableBinaryData;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.ClipboardHandlingMode;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.CodeType;
import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationOperation;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.android.CodeAreaCommandHandler;
import org.exbin.bined.android.CodeAreaCore;
import org.exbin.bined.basic.BasicCodeAreaSection;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.CodeAreaViewMode;
import org.exbin.bined.basic.EnterKeyHandlingMode;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.ScrollingDirection;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;

public class DefaultCodeAreaCommandHandler implements CodeAreaCommandHandler {
    private static final char DELETE_CHAR = 127;
    public static final int LAST_CONTROL_CODE = 31;
    public static final int NO_MODIFIER = 0;
    private final ClipDescription binaryDataFlavor;
    private boolean canPaste = false;
    private final ClipboardManager clipboard;
    private final CodeAreaCore codeArea;
    private final boolean codeTypeSupported;
    private final EnterKeyHandlingMode enterKeyHandlingMode = EnterKeyHandlingMode.PLATFORM_SPECIFIC;
    private final boolean viewModeSupported;

    /* access modifiers changed from: package-private */
    /* renamed from: org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler$4  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$CodeType;
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$EditationOperation;

        static {
            int[] iArr = new int[CodeType.values().length];
            $SwitchMap$org$exbin$bined$CodeType = iArr;
            try {
                iArr[CodeType.BINARY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$exbin$bined$CodeType[CodeType.DECIMAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$exbin$bined$CodeType[CodeType.OCTAL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$exbin$bined$CodeType[CodeType.HEXADECIMAL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            int[] iArr2 = new int[EditationOperation.values().length];
            $SwitchMap$org$exbin$bined$EditationOperation = iArr2;
            try {
                iArr2[EditationOperation.INSERT.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$exbin$bined$EditationOperation[EditationOperation.OVERWRITE.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    public DefaultCodeAreaCommandHandler(Context context, CodeAreaCore codeAreaCore) {
        this.codeArea = codeAreaCore;
        this.codeTypeSupported = codeAreaCore instanceof CodeTypeCapable;
        this.viewModeSupported = codeAreaCore instanceof ViewModeCapable;
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService("clipboard");
        this.clipboard = clipboardManager;
        /* class org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler.AnonymousClass1 */
        clipboardManager.addPrimaryClipChangedListener(DefaultCodeAreaCommandHandler.this::updateCanPaste);
        this.binaryDataFlavor = new ClipDescription("Binary Data", new String[]{CodeAreaUtils.MIME_CLIPBOARD_BINARY});
    }

    private void clearClipboardData() {
    }

    public static CodeAreaCommandHandler.CodeAreaCommandHandlerFactory createDefaultCodeAreaCommandHandlerFactory(final Context context) {
        /* class org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler.AnonymousClass2 */// org.exbin.bined.android.CodeAreaCommandHandler.CodeAreaCommandHandlerFactory
        return codeAreaCore -> new DefaultCodeAreaCommandHandler(context, codeAreaCore);
    }

    private void deleteSelection() {
        BinaryData contentData = this.codeArea.getContentData();
        if (contentData != null) {
            if (contentData instanceof EditableBinaryData) {
                SelectionRange selection = ((SelectionCapable) this.codeArea).getSelection();
                if (!selection.isEmpty()) {
                    long first = selection.getFirst();
                    ((EditableBinaryData) contentData).remove(first, (selection.getLast() - first) + 1);
                    this.codeArea.clearSelection();
                    ((CaretCapable) this.codeArea).getCaret().setCaretPosition(first);
                    revealCursor();
                    updateScrollBars();
                    return;
                }
                return;
            }
            throw new IllegalStateException("Data is not editable");
        }
    }

    private CodeType getCodeType() {
        return this.codeTypeSupported ? ((CodeTypeCapable) this.codeArea).getCodeType() : CodeType.HEXADECIMAL;
    }

    private void notifyCaretMoved() {
        ((CaretCapable) this.codeArea).notifyCaretMoved();
    }

    private void notifyScrolled() {
        ((ScrollingCapable) this.codeArea).notifyScrolled();
    }

    private void pressedCharAsCode(char c) {
        byte b;
        CodeAreaCaretPosition caretPosition = ((CaretCapable) this.codeArea).getCaret().getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        CodeType codeType = getCodeType();
        if (CodeAreaUtils.isValidCodeKeyValue(c, codeOffset, codeType)) {
            EditationMode editationMode = ((EditationModeCapable) this.codeArea).getEditationMode();
            if (this.codeArea.hasSelection() && editationMode != EditationMode.INPLACE) {
                deleteSelection();
            }
            int lowerCase = (c < '0' || c > '9') ? (Character.toLowerCase(c) - 'a') + 10 : c - '0';
            BinaryData binaryData = CodeAreaUtils.requireNonNull(this.codeArea.getContentData(), "Content data is null");
            EditationOperation activeOperation = ((EditationModeCapable) this.codeArea).getActiveOperation();
            if (editationMode == EditationMode.EXPANDING && activeOperation == EditationOperation.INSERT) {
                if (codeOffset > 0) {
                    byte b2 = binaryData.getByte(dataPosition);
                    switch (AnonymousClass4.$SwitchMap$org$exbin$bined$CodeType[codeType.ordinal()]) {
                        case 1:
                            b = (byte) ((255 >> codeOffset) & b2);
                            break;
                        case 2:
                            b = (byte) (b2 % (codeOffset == 1 ? (byte) 100 : 10));
                            break;
                        case 3:
                            b = (byte) (b2 % (codeOffset == 1 ? (byte) 64 : 8));
                            break;
                        case 4:
                            b = (byte) (b2 & 15);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected code type " + codeType.name());
                    }
                    if (b > 0) {
                        ((EditableBinaryData) binaryData).insert(dataPosition + 1, 1);
                        ((EditableBinaryData) binaryData).setByte(dataPosition, (byte) (binaryData.getByte(dataPosition) - b));
                        ((EditableBinaryData) binaryData).setByte(dataPosition + 1, b);
                    }
                } else {
                    ((EditableBinaryData) binaryData).insert(dataPosition, 1);
                }
                setCodeValue(lowerCase);
            } else {
                if (editationMode == EditationMode.EXPANDING && activeOperation == EditationOperation.OVERWRITE && dataPosition == this.codeArea.getDataSize()) {
                    ((EditableBinaryData) binaryData).insert(dataPosition, 1);
                }
                setCodeValue(lowerCase);
            }
            this.codeArea.notifyDataChanged();
            move(0, MovementDirection.RIGHT);
            revealCursor();
        }
    }

    private void pressedCharInPreview(char c) {
        if (isValidChar(c)) {
            EditationMode editationMode = ((EditationModeCapable) this.codeArea).getEditationMode();
            long dataPosition = ((CaretCapable) this.codeArea).getCaret().getCaretPosition().getDataPosition();
            byte[] charToBytes = charToBytes(c);
            if (editationMode != EditationMode.INPLACE || ((long) charToBytes.length) + dataPosition <= this.codeArea.getDataSize()) {
                if (this.codeArea.hasSelection() && editationMode != EditationMode.INPLACE) {
                    deleteSelection();
                }
                BinaryData binaryData = CodeAreaUtils.requireNonNull(this.codeArea.getContentData(), "Content data is null");
                EditationOperation activeOperation = ((EditationModeCapable) this.codeArea).getActiveOperation();
                if (((editationMode == EditationMode.EXPANDING && activeOperation == EditationOperation.OVERWRITE) || editationMode == EditationMode.INPLACE) && dataPosition < this.codeArea.getDataSize()) {
                    int length = charToBytes.length;
                    ((EditableBinaryData) binaryData).remove(dataPosition, ((long) length) + dataPosition > this.codeArea.getDataSize() ? (int) (this.codeArea.getDataSize() - dataPosition) : length);
                }
                ((EditableBinaryData) binaryData).insert(dataPosition, charToBytes);
                this.codeArea.notifyDataChanged();
                ((CaretCapable) this.codeArea).getCaret().setCaretPosition((((long) charToBytes.length) + dataPosition) - 1);
                move(0, MovementDirection.RIGHT);
                revealCursor();
            }
        }
    }

    private void revealCursor() {
        ((ScrollingCapable) this.codeArea).revealCursor();
        this.codeArea.repaint();
    }

    private void setClipboardContent(ClipData clipData) {
        clearClipboardData();
        try {
            this.clipboard.setPrimaryClip(clipData);
        } catch (IllegalStateException e) {
            clearClipboardData();
        }
    }

    private void setCodeValue(int i) {
        CodeAreaCaretPosition caretPosition = ((CaretCapable) this.codeArea).getCaret().getCaretPosition();
        long dataPosition = caretPosition.getDataPosition();
        int codeOffset = caretPosition.getCodeOffset();
        BinaryData binaryData = CodeAreaUtils.requireNonNull(this.codeArea.getContentData(), "Content data is null");
        EditableBinaryData editableBinaryData = (EditableBinaryData) binaryData;
        editableBinaryData.setByte(dataPosition, CodeAreaUtils.setCodeValue(binaryData.getByte(dataPosition), i, codeOffset, getCodeType()));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCanPaste() {
        this.canPaste = this.clipboard.hasPrimaryClip();
    }

    private void updateScrollBars() {
        ((ScrollingCapable) this.codeArea).updateScrollBars();
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void backSpacePressed() {
        if (((EditationModeCapable) this.codeArea).isEditable()) {
            BinaryData binaryData = CodeAreaUtils.requireNonNull(this.codeArea.getContentData(), "Content data is null");
            if (this.codeArea.hasSelection()) {
                deleteSelection();
                this.codeArea.notifyDataChanged();
                return;
            }
            DefaultCodeAreaCaret defaultCodeAreaCaret = (DefaultCodeAreaCaret) ((CaretCapable) this.codeArea).getCaret();
            long dataPosition = defaultCodeAreaCaret.getDataPosition();
            if (dataPosition > 0 && dataPosition <= this.codeArea.getDataSize()) {
                defaultCodeAreaCaret.setCodeOffset(0);
                move(0, MovementDirection.LEFT);
                defaultCodeAreaCaret.setCodeOffset(0);
                ((EditableBinaryData) binaryData).remove(dataPosition - 1, 1);
                this.codeArea.notifyDataChanged();
                revealCursor();
                updateScrollBars();
            }
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public boolean canPaste() {
        return this.canPaste;
    }

    public byte[] charToBytes(char c) {
        ByteBuffer encode = ((CharsetCapable) this.codeArea).getCharset().encode(Character.toString(c));
        byte[] bArr = new byte[encode.remaining()];
        encode.get(bArr, 0, bArr.length);
        return bArr;
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void clearSelection() {
        ((SelectionCapable) this.codeArea).clearSelection();
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void copy() {
        BinaryData contentData;
        SelectionRange selection = ((SelectionCapable) this.codeArea).getSelection();
        if (!selection.isEmpty() && (contentData = this.codeArea.getContentData()) != null) {
            long first = selection.getFirst();
            contentData.copy(first, (selection.getLast() - first) + 1);
            setClipboardContent(new ClipData(this.binaryDataFlavor, new ClipData.Item(new Intent() {
                /* class org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler.AnonymousClass3 */

                public Uri getData() {
                    return super.getData();
                }
            })));
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void copyAsCode() {
        BinaryData contentData;
        SelectionRange selection = ((SelectionCapable) this.codeArea).getSelection();
        if (!selection.isEmpty() && (contentData = this.codeArea.getContentData()) != null) {
            long first = selection.getFirst();
            contentData.copy(first, (selection.getLast() - first) + 1);
            ((CodeTypeCapable) this.codeArea).getCodeType();
            ((CodeCharactersCaseCapable) this.codeArea).getCodeCharactersCase();
            setClipboardContent(null);
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void cut() {
        if (((EditationModeCapable) this.codeArea).isEditable() && !((SelectionCapable) this.codeArea).getSelection().isEmpty()) {
            copy();
            deleteSelection();
            this.codeArea.notifyDataChanged();
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void delete() {
        if (((EditationModeCapable) this.codeArea).isEditable()) {
            deleteSelection();
            this.codeArea.notifyDataChanged();
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void deletePressed() {
        if (((EditationModeCapable) this.codeArea).isEditable()) {
            if (this.codeArea.hasSelection()) {
                deleteSelection();
                this.codeArea.notifyDataChanged();
                updateScrollBars();
                notifyCaretMoved();
                revealCursor();
                return;
            }
            BinaryData binaryData = CodeAreaUtils.requireNonNull(this.codeArea.getContentData(), "Content data is null");
            DefaultCodeAreaCaret defaultCodeAreaCaret = (DefaultCodeAreaCaret) ((CaretCapable) this.codeArea).getCaret();
            long dataPosition = defaultCodeAreaCaret.getDataPosition();
            if (dataPosition < this.codeArea.getDataSize()) {
                ((EditableBinaryData) binaryData).remove(dataPosition, 1);
                this.codeArea.notifyDataChanged();
                if (defaultCodeAreaCaret.getCodeOffset() > 0) {
                    defaultCodeAreaCaret.setCodeOffset(0);
                }
                updateScrollBars();
                notifyCaretMoved();
                revealCursor();
            }
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void enterPressed() {
        if (((EditationModeCapable) this.codeArea).isEditable() && ((CaretCapable) this.codeArea).getCaret().getSection() == BasicCodeAreaSection.TEXT_PREVIEW) {
            String sequence = this.enterKeyHandlingMode.getSequence();
            if (!sequence.isEmpty()) {
                pressedCharInPreview(sequence.charAt(0));
                if (sequence.length() == 2) {
                    pressedCharInPreview(sequence.charAt(1));
                }
            }
        }
    }

    public boolean isValidChar(char c) {
        return ((CharsetCapable) this.codeArea).getCharset().canEncode();
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void keyPressed(KeyEvent keyEvent) {
        if (this.codeArea.isEnabled()) {
            switch (keyEvent.getKeyCode()) {
                case 4:
                    if (((EditationModeCapable) this.codeArea).getEditationMode() == EditationMode.EXPANDING) {
                        backSpacePressed();
                        return;
                    }
                    return;
                case 19:
                    move(keyEvent.getModifiers(), MovementDirection.UP);
                    undoSequenceBreak();
                    revealCursor();
                    return;
                case 20:
                    move(keyEvent.getModifiers(), MovementDirection.DOWN);
                    undoSequenceBreak();
                    revealCursor();
                    return;
                case 21:
                    move(keyEvent.getModifiers(), MovementDirection.LEFT);
                    undoSequenceBreak();
                    revealCursor();
                    return;
                case 22:
                    move(keyEvent.getModifiers(), MovementDirection.RIGHT);
                    undoSequenceBreak();
                    revealCursor();
                    return;
                case 61:
                    if (this.viewModeSupported && ((ViewModeCapable) this.codeArea).getViewMode() == CodeAreaViewMode.DUAL) {
                        move(keyEvent.getModifiers(), MovementDirection.SWITCH_SECTION);
                        undoSequenceBreak();
                        revealCursor();
                        return;
                    }
                    return;
                case 66:
                    enterPressed();
                    return;
                case 67:
                    if (((EditationModeCapable) this.codeArea).getEditationMode() == EditationMode.EXPANDING) {
                        deletePressed();
                        return;
                    }
                    return;
                case 92:
                    scroll(ScrollingDirection.PAGE_UP);
                    move(keyEvent.getModifiers(), MovementDirection.PAGE_UP);
                    undoSequenceBreak();
                    revealCursor();
                    return;
                case 93:
                    scroll(ScrollingDirection.PAGE_DOWN);
                    move(keyEvent.getModifiers(), MovementDirection.PAGE_DOWN);
                    undoSequenceBreak();
                    return;
                case 122:
                    if ((keyEvent.getModifiers() & 28672) > 0) {
                        move(keyEvent.getModifiers(), MovementDirection.DOC_START);
                    } else {
                        move(keyEvent.getModifiers(), MovementDirection.ROW_START);
                    }
                    undoSequenceBreak();
                    revealCursor();
                    return;
                case 123:
                    if ((keyEvent.getModifiers() & 28672) > 0) {
                        move(keyEvent.getModifiers(), MovementDirection.DOC_END);
                    } else {
                        move(keyEvent.getModifiers(), MovementDirection.ROW_END);
                    }
                    undoSequenceBreak();
                    revealCursor();
                    return;
                case 124:
                    EditationMode editationMode = ((EditationModeCapable) this.codeArea).getEditationMode();
                    if (editationMode == EditationMode.EXPANDING || editationMode == EditationMode.CAPPED) {
                        switch (AnonymousClass4.$SwitchMap$org$exbin$bined$EditationOperation[((EditationModeCapable) this.codeArea).getEditationOperation().ordinal()]) {
                            case 1:
                                ((EditationModeCapable) this.codeArea).setEditationOperation(EditationOperation.OVERWRITE);
                                return;
                            case 2:
                                ((EditationModeCapable) this.codeArea).setEditationOperation(EditationOperation.INSERT);
                                return;
                            default:
                                return;
                        }
                    } else {
                        return;
                    }
                default:
                    if (((ClipboardCapable) this.codeArea).getClipboardHandlingMode() != ClipboardHandlingMode.PROCESS) {
                        return;
                    }
                    if ((keyEvent.getModifiers() & 28672) > 0 && keyEvent.getKeyCode() == 31) {
                        copy();
                    } else if ((keyEvent.getModifiers() & 28672) > 0 && keyEvent.getKeyCode() == 52) {
                        cut();
                    } else if ((keyEvent.getModifiers() & 28672) > 0 && keyEvent.getKeyCode() == 50) {
                        paste();
                    } else if ((keyEvent.getModifiers() & 28672) > 0 && keyEvent.getKeyCode() == 29) {
                        this.codeArea.selectAll();
                    }
            }
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void keyTyped(int i, KeyEvent keyEvent) {
        char c = (char) i;
        if (c == 0 || !((EditationModeCapable) this.codeArea).isEditable()) {
            return;
        }
        if (((CaretCapable) this.codeArea).getCaret().getSection() == BasicCodeAreaSection.CODE_MATRIX) {
            pressedCharAsCode(c);
        } else if (c > 31 && c != 127) {
            pressedCharInPreview(c);
        }
    }

    public void move(int i, MovementDirection movementDirection) {
        DefaultCodeAreaCaret defaultCodeAreaCaret = (DefaultCodeAreaCaret) ((CaretCapable) this.codeArea).getCaret();
        CodeAreaCaretPosition caretPosition = defaultCodeAreaCaret.getCaretPosition();
        CodeAreaCaretPosition computeMovePosition = ((CaretCapable) this.codeArea).computeMovePosition(caretPosition, movementDirection);
        if (!caretPosition.equals(computeMovePosition)) {
            defaultCodeAreaCaret.setCaretPosition(computeMovePosition);
            updateSelection((i & 193) > 0 ? CodeAreaCommandHandler.SelectingMode.SELECTING : CodeAreaCommandHandler.SelectingMode.NONE, computeMovePosition);
            notifyCaretMoved();
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void moveCaret(int i, int i2, CodeAreaCommandHandler.SelectingMode selectingMode) {
        CodeAreaCaretPosition mousePositionToClosestCaretPosition = ((CaretCapable) this.codeArea).mousePositionToClosestCaretPosition(i, i2, CaretOverlapMode.PARTIAL_OVERLAP);
        if (mousePositionToClosestCaretPosition != null) {
            ((CaretCapable) this.codeArea).getCaret().setCaretPosition(mousePositionToClosestCaretPosition);
            updateSelection(selectingMode, mousePositionToClosestCaretPosition);
            notifyCaretMoved();
            undoSequenceBreak();
            this.codeArea.repaint();
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void paste() {
        if (!((EditationModeCapable) this.codeArea).isEditable()) {
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void pasteFromCode() {
        if (!((EditationModeCapable) this.codeArea).isEditable()) {
        }
    }

    public void scroll(ScrollingDirection scrollingDirection) {
        CodeAreaScrollPosition scrollPosition = ((ScrollingCapable) this.codeArea).getScrollPosition();
        CodeAreaScrollPosition computeScrolling = ((ScrollingCapable) this.codeArea).computeScrolling(scrollPosition, scrollingDirection);
        if (!scrollPosition.equals(computeScrolling)) {
            ((ScrollingCapable) this.codeArea).setScrollPosition(computeScrolling);
            this.codeArea.resetPainter();
            notifyScrolled();
            updateScrollBars();
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void selectAll() {
        long dataSize = this.codeArea.getDataSize();
        if (dataSize > 0) {
            ((SelectionCapable) this.codeArea).setSelection(0, dataSize);
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void undoSequenceBreak() {
    }

    public void updateSelection(CodeAreaCommandHandler.SelectingMode selectingMode, CodeAreaCaretPosition codeAreaCaretPosition) {
        DefaultCodeAreaCaret defaultCodeAreaCaret = (DefaultCodeAreaCaret) ((CaretCapable) this.codeArea).getCaret();
        SelectionRange selection = ((SelectionCapable) this.codeArea).getSelection();
        if (selectingMode == CodeAreaCommandHandler.SelectingMode.SELECTING) {
            ((SelectionCapable) this.codeArea).setSelection(selection.getStart(), defaultCodeAreaCaret.getDataPosition());
        } else {
            ((SelectionCapable) this.codeArea).setSelection(defaultCodeAreaCaret.getDataPosition(), defaultCodeAreaCaret.getDataPosition());
        }
    }

    @Override // org.exbin.bined.android.CodeAreaCommandHandler
    public void wheelScroll(int i, CodeAreaCommandHandler.ScrollbarOrientation scrollbarOrientation) {
        scroll(ScrollingDirection.UP);
    }
}