package org.exbin.bined.capability;

import org.exbin.bined.RowWrappingMode;

public interface RowWrappingCapable {
    int getMaxBytesPerRow();

    int getMaxRowPositionLength();

    int getMinRowPositionLength();

    RowWrappingMode getRowWrapping();

    int getWrappingBytesGroupSize();

    void setMaxBytesPerRow(int i);

    void setMaxRowPositionLength(int i);

    void setMinRowPositionLength(int i);

    void setRowWrapping(RowWrappingMode rowWrappingMode);

    void setWrappingBytesGroupSize(int i);
}