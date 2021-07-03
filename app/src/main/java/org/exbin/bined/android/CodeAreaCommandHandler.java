package org.exbin.bined.android;

import android.view.KeyEvent;

public interface CodeAreaCommandHandler {
    interface CodeAreaCommandHandlerFactory {
        CodeAreaCommandHandler createCommandHandler(CodeAreaCore codeAreaCore);
    }

    enum ScrollbarOrientation {
        HORIZONTAL,
        VERTICAL
    }

    enum SelectingMode {
        NONE,
        SELECTING
    }

    void backSpacePressed();

    boolean canPaste();

    void clearSelection();

    void copy();

    void copyAsCode();

    void cut();

    void delete();

    void deletePressed();

    void enterPressed();

    void keyPressed(KeyEvent keyEvent);

    void keyTyped(int i, KeyEvent keyEvent);

    void moveCaret(int i, int i2, SelectingMode selectingMode);

    void paste();

    void pasteFromCode();

    void selectAll();

    void undoSequenceBreak();

    void wheelScroll(int i, ScrollbarOrientation scrollbarOrientation);
}