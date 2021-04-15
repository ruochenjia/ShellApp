package com.mindrex.edit;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.Selection;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.OverScroller;

import com.mindrex.debugger.Log;
import com.mindrex.standardassets.E;

import java.util.Objects;

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class EditorTouch implements OnGestureListener, OnDoubleTapListener, OnScaleGestureListener {
    private final TextEditor editor;
    private final OverScroller scroller;
    private boolean scrollable;
    private boolean dragMode;
    private boolean scaleEnabled;
    private boolean scale;
    private int scrollMaxX;
    private int scrollMaxY;
    private boolean modification;

    public EditorTouch(TextEditor e) {
        this.editor = e;
        this.scroller = new OverScroller(editor.getContext());
        scroller.startScroll(0, 0, 0, 0, 0);
        this.scrollable = true;
        this.dragMode = false;
        this.scaleEnabled = true;
        this.scale = false;
        this.modification = false;
    }

    public final void setDrag(boolean dragMode) {
        this.dragMode = dragMode;
    }

    public final boolean isDragMode() {
        return this.dragMode;
    }

    public final void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    public final boolean canScroll() {
        return this.scrollable;
    }

    public final void setScaleEnabled(boolean scale) {
        this.scaleEnabled = scale;
    }

    public final boolean canScaleByThumb() {
        return this.scaleEnabled;
    }

    public final void resetForNewText() {
        scroller.forceFinished(true);
        scroller.startScroll(0, 0, 0, 0, 0);
        this.scrollMaxX = 0;
        this.scrollMaxY = 0;
        this.modification = false;
    }

    public final boolean showSoftInputForEditor() {
        if (!editor.hasFocus()) {
            editor.requestFocus();
            if (!editor.hasFocus())
                editor.requestFocusFromTouch();
        }
        boolean r = editor.hasFocus();
        if (r)
            r = ((InputMethodManager) editor.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editor, 0);
        return r;
    }

    public final OverScroller getScroller() {
        return scroller;
    }

    public final void setScrollMaxX(int maxX) {
        if (maxX < 0) maxX = 0;
        this.scrollMaxX = Math.max(maxX, scroller.getCurrX());
    }

    public final void setScrollMaxY(int maxY) {
        if (maxY < 0) maxY = 0;
        this.scrollMaxY = maxY;
        if (getOffsetY() > maxY) {
            scroller.startScroll(getOffsetX(), getOffsetY(), 0, maxY - getOffsetY(), 0);
            editor.invalidate();
        }
    }

    public final int getOffsetX() {
        return scroller.getCurrX();
    }

    public final int getOffsetY() {
        return scroller.getCurrY();
    }

    public final void setModification(boolean m) {
        this.modification = m;
    }

    private void focusTo(float x, float y, float oldSize, float newSize) {
        if ((int) oldSize != (int) newSize) {
            final float rx = x, ry = y;
            x = getOffsetX() + x;
            y = getOffsetY() + y;
            x *= (newSize / oldSize);
            y *= (newSize / oldSize);
            x = x - rx;
            y = y - ry;
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            scroller.startScroll(0, 0, (int) x, (int) y, 0);
        }
    }

    @Override
    public final boolean onScale(ScaleGestureDetector detector) {
        final float newSize = detector.getScaleFactor() * editor.getStyles().getTextSize();
        if (newSize > 100) {
            focusTo(detector.getFocusX(), detector.getFocusY(), editor.getStyles().getTextSize(), 100);
            editor.getStyles().setTextSize(100);
            return false;
        } else if (newSize < 20) {
            focusTo(detector.getFocusX(), detector.getFocusY(), editor.getStyles().getTextSize(), 20);
            editor.getStyles().setTextSize(20);
            return false;
        } else {
            focusTo(detector.getFocusX(), detector.getFocusY(), editor.getStyles().getTextSize(), newSize);
            editor.getStyles().setTextSize(newSize);
            scale = true;
            return scaleEnabled;
        }
    }

    @Override
    public final boolean onScaleBegin(ScaleGestureDetector detector) {
        scale = scaleEnabled;
        return scaleEnabled;
    }

    @Override
    public final void onScaleEnd(ScaleGestureDetector detector) {
        scale = false;
    }

    @Override
    public final boolean onSingleTapConfirmed(MotionEvent event) {
        if (scale) return false;
        else if (editor.isEditable() && editor.isEnabled()) {
            boolean result = showSoftInputForEditor();
            if (!result) Log.d("Unable to show soft input for editor: " + editor.toString());
        }
        if (modification) {
            modification = false;
            editor.stopActionMode();
        } else {
            Selection.setSelection(editor.getEditableText(), editor.getCharOffsetByThumb(event.getX(), editor.getLineByThumbY(event.getY())));
            editor.notifySelChange();
            editor.invalidate();
        }
        return true;
    }

    @Override
    public final boolean onDoubleTap(MotionEvent event) {
        if (!scale) try {
            final ClipboardManager cm = (ClipboardManager) editor.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            editor.getEditableText().append(Objects.requireNonNull(cm.getPrimaryClip()).getItemAt(0).getText().toString());
            scroller.startScroll(scroller.getCurrX(), scroller.getCurrY(), 0, scrollMaxY - getOffsetY(), 0);
        } catch (Exception e) {
            E.print(e);
        }
        return false;
    }

    @Override
    public final boolean onDoubleTapEvent(MotionEvent event) {
        return !scale;
    }

    @Override
    public final boolean onDown(MotionEvent event) {
        return editor.isEnabled();
    }

    @Override
    public final void onShowPress(MotionEvent event) {
    }

    @Override
    public final boolean onSingleTapUp(MotionEvent event) {
        return true;
    }

    @Override
    public final boolean onScroll(MotionEvent event0, MotionEvent event1, float dx, float dy) {
        if (!scrollable || scale) return false;
        else if (scroller.getCurrX() + dx > scrollMaxX)
            dx = scrollMaxX - scroller.getCurrX();
        else if (scroller.getCurrX() + dx < 0)
            dx = scroller.getCurrX();
        if (scroller.getCurrY() + dy > scrollMaxY)
            dy = scrollMaxY - scroller.getCurrY();
        else if (scroller.getCurrY() + dy < 0)
            dy = scroller.getCurrY();
        scroller.forceFinished(true);
        scroller.startScroll(scroller.getCurrX(), scroller.getCurrY(), (int) dx, (int) dy, 0);
        editor.invalidate();
        return true;
    }

    @Override
    public final void onLongPress(MotionEvent event) {
        if (editor.isEnabled() || !scale) {
            final float x = event.getX();
            final float y = event.getY();
            final int line = editor.getLineByThumbY(y);
            if (line >= 0) {
                final int lineSt = editor.getLineStart(line);
                final int lineEd = editor.getLineEnd(line);
                final int thumb = editor.getCharOffsetByThumb(x, line);
                int rand = (int) (Math.random() * (0) / 3);
                if (rand == 0) rand = 2;
                int left = thumb - rand;
                int right = thumb + rand;
                if (lineSt != lineEd) {
                    left = Math.max(left, lineSt);
                    right = Math.min(right, lineEd);
                } else {
                    int len = editor.getEditableText().length();
                    left = Math.max(left, 0);
                    right = Math.min(right, len);
                }
                editor.createSelectionControllerIfNeed();
                Selection.setSelection(editor.getEditableText(), left, right);
                modification = true;
                editor.notifySelChange();
                editor.startSelectMode();
                editor.invalidate();
            }
        }
    }

    @Override
    public final boolean onFling(MotionEvent event0, MotionEvent event1, float vx, float vy) {
        if (!scrollable || dragMode || scale) return false;
        else {
            scroller.forceFinished(true);
            scroller.fling(scroller.getCurrX(), scroller.getCurrY(), (int) -vx, (int) -vy, 0, scrollMaxX, 0, scrollMaxY, 10, 10);
            editor.invalidate();
            return true;
        }
    }
}
