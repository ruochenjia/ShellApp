package org.exbin.bined.capability;

import org.exbin.bined.ClipboardHandlingMode;

public interface ClipboardCapable {
    ClipboardHandlingMode getClipboardHandlingMode();

    void setClipboardHandlingMode(ClipboardHandlingMode clipboardHandlingMode);
}