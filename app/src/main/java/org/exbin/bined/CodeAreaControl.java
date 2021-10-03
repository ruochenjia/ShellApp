package org.exbin.bined;

public interface CodeAreaControl extends DataProvider {
    boolean canPaste();

    void clearSelection();

    void copy();

    void cut();

    void delete();

    boolean hasSelection();

    void paste();

    void selectAll();
}
