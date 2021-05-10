package com.mindrex.android.edit;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Editable;
import android.text.Selection;
import android.view.MotionEvent;

public final class SelectionController {
    protected TextEditor target;
    private final Handle left;
    private final Handle right;
    private int name = 0;

    public SelectionController(TextEditor ce) {
        target = ce;
        left = new Handle();
        right = new Handle();
    }

    public final Editable getText() {
        return target.getEditableText();
    }

    public final int getEnd() {
        return Selection.getSelectionEnd(getText());
    }

    public final int getStart() {
        return Selection.getSelectionStart(getText());
    }

    public final void setStart(int st) {
        Selection.setSelection(getText(), st, getEnd());
        dirty();
    }

    public final void setEnd(int ed) {
        Selection.setSelection(getText(), getStart(), ed);
        dirty();
    }

    public final void dirty() {
        target.invalidate();
    }

    static boolean isInBounds(float x, float y, RectF rect) {
        return (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom);
    }

    static float[] relativePosition(float x, float y, RectF rect) {
        return new float[]{x - rect.left, y - rect.top};
    }

    public final boolean onTouchEvent(MotionEvent event) {
        if (getStart() != getEnd()) {
            final float x = event.getX();
            final float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isInBounds(x, y, left.getBounds())) {
                        name = 1;
                        return true;
                    } else if (isInBounds(x, y, right.getBounds())) {
                        name = 2;
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int newIndex = target.getCharOffsetByThumb(x, target.getLineByThumbY(y - left.getBounds().height()));
                    switch (name) {
                        case 0:
                            return false;
                        case 1:
                            if (newIndex < getEnd()) {
                                setStart(newIndex);
                            } else {
                                name = 2;
                                Selection.setSelection(getText(), getEnd(), newIndex);
                                dirty();
                            }
                            break;
                        case 2:
                            if (newIndex > getStart()) {
                                setEnd(newIndex);
                            } else {
                                name = 1;
                                Selection.setSelection(getText(), newIndex, getStart());
                                dirty();
                            }
                    }
                    target.getStates().setModification(true);
                    return true;
                case MotionEvent.ACTION_UP:
                    name = 0;
                    break;
            }
        }
        return false;
    }

    public final void onDraw(Canvas canvas) {
        {
            final int i = getStart();
            final int line = target.getLineByIndex(i);
            final float top = target.getLineTopOnScreen(line);
            //float bottom = top + target.getStyles().getLineHeight() * 2;
            final float xOffset = target.getLastOffset() + target.measureText(target.getLineStart(line), i);
            left.draw(canvas, top, xOffset);
        }
        {
            final int i = getEnd();
            final int line = target.getLineByIndex(i);
            final float top = target.getLineTopOnScreen(line);
            //float bottom = top + target.getStyles().getLineHeight() * 2;
            final float xOffset = target.getLastOffset() + target.measureText(target.getLineStart(line), i);
            right.draw(canvas, top, xOffset);
        }
    }

    public final Handle getLeftHandle() {
        return left;
    }

    public final Handle getRightHandle() {
        return right;
    }

    public final class Handle {
        private final Paint mPaint;
        private RectF mBounds;
        private final float r;

        public Handle() {
            mPaint = new Paint();
            mPaint.setColor(target.getStyles().getHandleColor());
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(52.0f);
            r = (mPaint.descent() - mPaint.ascent()) / 2;
            setBounds(0, 0, 0, 0);
        }

        public final void setHandleColor(int color) {
            mPaint.setColor(color);
        }

        public final void draw(Canvas canvas, float top, float centerX) {
            float cy = top + target.getStyles().getLineHeight() + r;
            canvas.drawCircle(centerX, cy, r, mPaint);
            setBounds(centerX - r, cy - r, centerX + r, cy + r);
        }

        private void setBounds(float left, float top, float right, float bottom) {
            mBounds = new RectF(left, top, right, bottom);
        }

        public final RectF getBounds() {
            return mBounds;
        }
    }
}