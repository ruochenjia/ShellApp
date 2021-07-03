package org.exbin.bined;

import androidx.annotation.NonNull;

public class RowNumberLength {
    private int rowNumberLength = 8;
    @NonNull
    private RowNumberType rowNumberType = RowNumberType.SPECIFIED;

    public enum RowNumberType {
        AUTO,
        SPECIFIED
    }

    public int getRowNumberLength() {
        return this.rowNumberLength;
    }

    @NonNull
    public RowNumberType getRowNumberType() {
        return this.rowNumberType;
    }

    public void setRowNumberLength(int i) {
        this.rowNumberLength = i;
    }

    public void setRowNumberType(RowNumberType rowNumberType2) {
        if (rowNumberType2 != null) {
            this.rowNumberType = rowNumberType2;
            return;
        }
        throw new NullPointerException();
    }
}
