package org.exbin.bined.android;

import android.graphics.Canvas;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.basic.CodeAreaScrollPosition;
import org.exbin.bined.basic.MovementDirection;
import org.exbin.bined.basic.PositionScrollVisibility;
import org.exbin.bined.basic.ScrollingDirection;

public interface CodeAreaPainter {
    void attach();

    CodeAreaScrollPosition computeCenterOnScrollPosition(CodeAreaCaretPosition codeAreaCaretPosition);

    CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition codeAreaCaretPosition, MovementDirection movementDirection);

    PositionScrollVisibility computePositionScrollVisibility(CodeAreaCaretPosition codeAreaCaretPosition);

    CodeAreaScrollPosition computeRevealScrollPosition(CodeAreaCaretPosition codeAreaCaretPosition);

    CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition codeAreaScrollPosition, ScrollingDirection scrollingDirection);

    void detach();

    int getMouseCursorShape(int i, int i2);

    BasicCodeAreaZone getPositionZone(int i, int i2);

    boolean isInitialized();

    CodeAreaCaretPosition mousePositionToClosestCaretPosition(int i, int i2, CaretOverlapMode caretOverlapMode);

    void paintComponent(Canvas canvas);

    void paintCursor(Canvas canvas);

    void paintMainArea(Canvas canvas);

    void rebuildColors();

    void reset();

    void resetCaret();

    void resetColors();

    void resetFont();

    void resetLayout();

    void updateScrollBars();
}