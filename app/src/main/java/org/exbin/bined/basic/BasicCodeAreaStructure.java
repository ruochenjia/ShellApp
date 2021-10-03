package org.exbin.bined.basic;

import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeType;
import org.exbin.bined.DataProvider;
import org.exbin.bined.RowWrappingMode;
import org.exbin.bined.SelectionRange;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;

public class BasicCodeAreaStructure {
    private int bytesPerRow;
    private int charactersPerRow;
    private CodeType codeType = CodeType.HEXADECIMAL;
    private long dataSize;
    private final BasicCodeAreaLayout layout = new BasicCodeAreaLayout();
    private int maxBytesPerLine;
    private RowWrappingMode rowWrapping = RowWrappingMode.NO_WRAPPING;
    private long rowsPerDocument;
    private SelectionRange selectionRange = null;
    private CodeAreaViewMode viewMode = CodeAreaViewMode.DUAL;
    private int wrappingBytesGroupSize;

    public int computeFirstCodeCharacterPos(int i) {
        return this.layout.computeFirstCodeCharacterPos(this, i);
    }

    public CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition codeAreaCaretPosition, MovementDirection movementDirection, int i) {
        return this.layout.computeMovePosition(this, codeAreaCaretPosition, movementDirection, i);
    }

    public int computePositionByte(int i) {
        return this.layout.computePositionByte(this, i);
    }

    public int getBytesPerRow() {
        return this.bytesPerRow;
    }

    public int getCharactersPerRow() {
        return this.charactersPerRow;
    }

    public CodeType getCodeType() {
        return this.codeType;
    }

    public long getDataSize() {
        return this.dataSize;
    }

    public int getMaxBytesPerLine() {
        return this.maxBytesPerLine;
    }

    public RowWrappingMode getRowWrapping() {
        return this.rowWrapping;
    }

    public long getRowsPerDocument() {
        return this.rowsPerDocument;
    }

    public SelectionRange getSelectionRange() {
        return this.selectionRange;
    }

    public CodeAreaViewMode getViewMode() {
        return this.viewMode;
    }

    public int getWrappingBytesGroupSize() {
        return this.wrappingBytesGroupSize;
    }

    public void updateCache(DataProvider dataProvider, int i) {
        this.viewMode = ((ViewModeCapable) dataProvider).getViewMode();
        this.codeType = ((CodeTypeCapable) dataProvider).getCodeType();
        this.selectionRange = ((SelectionCapable) dataProvider).getSelection();
        this.dataSize = dataProvider.getDataSize();
        this.rowWrapping = ((RowWrappingCapable) dataProvider).getRowWrapping();
        this.maxBytesPerLine = ((RowWrappingCapable) dataProvider).getMaxBytesPerRow();
        this.wrappingBytesGroupSize = ((RowWrappingCapable) dataProvider).getWrappingBytesGroupSize();
        this.bytesPerRow = this.layout.computeBytesPerRow(this, i);
        this.charactersPerRow = this.layout.computeCharactersPerRow(this);
        this.rowsPerDocument = this.layout.computeRowsPerDocument(this);
    }
}