package org.exbin.bined.capability;

import org.exbin.bined.CaretMovedListener;
import org.exbin.bined.CaretOverlapMode;
import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.basic.BasicCodeAreaZone;
import org.exbin.bined.basic.MovementDirection;

public interface CaretCapable {
    void addCaretMovedListener(CaretMovedListener caretMovedListener);

    CodeAreaCaretPosition computeMovePosition(CodeAreaCaretPosition codeAreaCaretPosition, MovementDirection movementDirection);

    CodeAreaCaret getCaret();

    int getMouseCursorShape(int i, int i2);

    BasicCodeAreaZone getPositionZone(int i, int i2);

    boolean isShowMirrorCursor();

    CodeAreaCaretPosition mousePositionToClosestCaretPosition(int i, int i2, CaretOverlapMode caretOverlapMode);

    void notifyCaretChanged();

    void notifyCaretMoved();

    void removeCaretMovedListener(CaretMovedListener caretMovedListener);

    void setShowMirrorCursor(boolean z);
}