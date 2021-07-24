package org.exbin.bined.basic;

import java.util.Objects;

public class CodeAreaScrollPosition {
    private int charOffset = 0;
    private int charPosition = 0;
    private int rowOffset = 0;
    private long rowPosition = 0;

    public CodeAreaScrollPosition() {
    }

    public CodeAreaScrollPosition(long j, int i, int i2, int i3) {
        this.rowPosition = j;
        this.rowOffset = i;
        this.charPosition = i2;
        this.charOffset = i3;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CodeAreaScrollPosition codeAreaScrollPosition = (CodeAreaScrollPosition) obj;
        if (this.rowPosition != codeAreaScrollPosition.rowPosition || this.rowOffset != codeAreaScrollPosition.rowOffset || this.charPosition != codeAreaScrollPosition.charPosition) {
            return false;
        }
        if (this.charOffset == codeAreaScrollPosition.charOffset) {
            z = false;
        }
        return z;
    }

    public int getCharOffset() {
        return this.charOffset;
    }

    public int getCharPosition() {
        return this.charPosition;
    }

    public int getRowOffset() {
        return this.rowOffset;
    }

    public long getRowPosition() {
        return this.rowPosition;
    }

    public int hashCode() {
        return Objects.hash(this.rowPosition, this.rowOffset, this.charPosition, this.charOffset);
    }

    public boolean isCharPositionGreaterThan(CodeAreaScrollPosition codeAreaScrollPosition) {
        int i = this.charPosition;
        int i2 = codeAreaScrollPosition.charPosition;
        return i > i2 || (i == i2 && this.charOffset > codeAreaScrollPosition.charOffset);
    }

    public boolean isRowPositionGreaterThan(CodeAreaScrollPosition codeAreaScrollPosition) {
        long j = this.rowPosition;
        long j2 = codeAreaScrollPosition.rowPosition;
        return j > j2 || (j == j2 && this.rowOffset > codeAreaScrollPosition.rowOffset);
    }

    public void reset() {
        this.rowPosition = 0;
        this.rowOffset = 0;
        this.charPosition = 0;
        this.charOffset = 0;
    }

    public void setCharOffset(int i) {
        this.charOffset = i;
    }

    public void setCharPosition(int i) {
        this.charPosition = i;
    }

    public void setRowOffset(int i) {
        this.rowOffset = i;
    }

    public void setRowPosition(long j) {
        this.rowPosition = j;
    }

    public void setScrollPosition(CodeAreaScrollPosition codeAreaScrollPosition) {
        if (codeAreaScrollPosition == null) {
            reset();
            return;
        }
        this.rowPosition = codeAreaScrollPosition.getRowPosition();
        this.rowOffset = codeAreaScrollPosition.getRowOffset();
        this.charPosition = codeAreaScrollPosition.getCharPosition();
        this.charOffset = codeAreaScrollPosition.getCharOffset();
    }
}
