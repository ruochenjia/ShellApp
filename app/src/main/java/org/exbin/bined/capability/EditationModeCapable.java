package org.exbin.bined.capability;

import org.exbin.bined.EditationMode;
import org.exbin.bined.EditationModeChangedListener;
import org.exbin.bined.EditationOperation;

public interface EditationModeCapable {
    void addEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener);

    EditationOperation getActiveOperation();

    EditationMode getEditationMode();

    EditationOperation getEditationOperation();

    boolean isEditable();

    void removeEditationModeChangedListener(EditationModeChangedListener editationModeChangedListener);

    void setEditationMode(EditationMode editationMode);

    void setEditationOperation(EditationOperation editationOperation);
}