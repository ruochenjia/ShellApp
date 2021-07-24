package org.exbin.bined;

import androidx.annotation.NonNull;

import java.util.Optional;

public interface CodeAreaCaretPosition {
    int getCodeOffset();

    long getDataPosition();

    @NonNull
    Optional<CodeAreaSection> getSection();

    void reset();
}
