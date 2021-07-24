package org.exbin.bined;

public interface CodeAreaCaret {
    CodeAreaCaretPosition getCaretPosition();

    CodeAreaSection getSection();

    void setCaretPosition(long j);

    void setCaretPosition(long j, int i);

    void setCaretPosition(CodeAreaCaretPosition codeAreaCaretPosition);
}
