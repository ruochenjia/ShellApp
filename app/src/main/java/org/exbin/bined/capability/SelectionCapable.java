package org.exbin.bined.capability;

import org.exbin.bined.SelectionChangedListener;
import org.exbin.bined.SelectionRange;

public interface SelectionCapable {
    void addSelectionChangedListener(SelectionChangedListener selectionChangedListener);

    void clearSelection();

    SelectionRange getSelection();

    boolean hasSelection();

    void removeSelectionChangedListener(SelectionChangedListener selectionChangedListener);

    void setSelection(long j, long j2);

    void setSelection(SelectionRange selectionRange);
}