package org.exbin.bined.capability;

import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.ScrollingListener;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.ScrollingDirection;

public interface ScrollingCapable {
    void addScrollingListener(ScrollingListener scrollingListener);

    void centerOnCursor();

    void centerOnPosition(CodeAreaCaretPosition codeAreaCaretPosition);

    CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition codeAreaScrollPosition, ScrollingDirection scrollingDirection);

    CodeAreaScrollPosition getScrollPosition();

    void notifyScrolled();

    void removeScrollingListener(ScrollingListener scrollingListener);

    void revealCursor();

    void revealPosition(CodeAreaCaretPosition codeAreaCaretPosition);

    void setScrollPosition(CodeAreaScrollPosition codeAreaScrollPosition);

    void updateScrollBars();
}