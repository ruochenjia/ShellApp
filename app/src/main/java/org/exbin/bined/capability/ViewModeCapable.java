package org.exbin.bined.capability;

import org.exbin.bined.basic.CodeAreaViewMode;

public interface ViewModeCapable {
    CodeAreaViewMode getViewMode();

    void setViewMode(CodeAreaViewMode codeAreaViewMode);
}