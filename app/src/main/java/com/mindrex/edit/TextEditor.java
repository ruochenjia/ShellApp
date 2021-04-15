package com.mindrex.edit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mindrex.shell.R;
import com.mindrex.standardassets.ContextUtils;
import com.mindrex.standardassets.StandardView;

import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class TextEditor extends StandardView implements TextWatcher {
    private EditorText text;
    private final EditorStyle style;
    private boolean editable;
    private final EditorTouch state;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector detectorScale;
    private SelectionController selectionController;
    private final InputMethodManager inputMethodManager;
    private EditorInputConnection editorInputConnection;
    private ActionMode actionMode;
    private boolean height_wrap;

    private boolean eventToController = false;
    private float offsetX;
    private int CurrLine;
    private int selectionStart;
    private int selectionEnd;
    private float tabSize;
    private float mlw;

    {
        actionMode = null;
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(50.0f);
        height_wrap = false;
        style = new EditorStyle();
        style.setTextSize(50.0f);
        style.setLineNumberGravity(Gravity.END);
        state = new EditorTouch(this);
        gestureDetector = new GestureDetector(context, state);
        detectorScale = new ScaleGestureDetector(context, state);
        gestureDetector.setOnDoubleTapListener(state);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        this.setText("");
        this.setEditable(true);
        super.setFocusable(true);
        super.setFocusableInTouchMode(true);
    }

    public TextEditor() {
        this(ContextUtils.getContext());
    }

    public TextEditor(Context context) {
        this(context, null);
    }

    public TextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextEditor(Context context, AttributeSet attrs, int styleResId) {
        super(context, attrs, styleResId);
    }

    @Override
    public final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        offsetX = -state.getOffsetX();
        drawCurrLineBackground(canvas);
        if (style.showLineNumber) {
            drawLineNumbers(canvas);
            drawDividerLine(canvas);
        }
        drawText_debug(canvas);
        if (selectionController != null && getStart() != getEnd())
            selectionController.onDraw(canvas);
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST)
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.EXACTLY-1, MeasureSpec.getSize(widthMeasureSpec));
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            int height = (int) style.getLineHeight() * getLineCount();
            int aHeight = MeasureSpec.getSize(heightMeasureSpec);
            if (height > aHeight) height = aHeight;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.EXACTLY-1, height);
            height_wrap = true;
        } else height_wrap = false;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected final void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateScrollMaxY();
    }

    @Override
    public final boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        else if (selectionController != null) {
            if (getStart() != getEnd())
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        eventToController = selectionController.onTouchEvent(event);
                        if (eventToController) return true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        if (eventToController) return selectionController.onTouchEvent(event);
                        break;
                    default:
                        break;
                }
        } else eventToController = false;
        final int i = event.getPointerCount();
        if (i == 1) return gestureDetector.onTouchEvent(event) || detectorScale.onTouchEvent(event);
        else if (i == 2) return detectorScale.onTouchEvent(event);
        else return false;
    }

    @NonNull
    @Override
    public final String toString() {
        return getText();
    }

    public final void setText(List<String> lines) {
        final String[] a = lines.toArray(new String[0]);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final String b = String.join("\n", a);
            setText(b);
        } else {
            final StringBuilder c = new StringBuilder();
            for (int i = 0; i < a.length; i++) {
                if (i == a.length - 1) c.append(a[i]);
                else c.append(a[i]).append("\n");
            }
            setText(c.toString());
        }
    }

    public void setText(CharSequence s) {
        if (text != null)
            text.removeWatcher(this);
        text = new EditorText(s);
        text.addWatcher(this);
        state.resetForNewText();
        Selection.setSelection(text, 0);
        requestLayout();
        invalidate();
        inputMethodManager.restartInput(this);
    }

    public final void setText(int i) {
        setText(resources.getString(i));
    }

    public final EditorText getEditableText() {
        return text;
    }

    public final String getText() {
        return text.toString();
    }

    public final EditorStyle getStyles() {
        return style;
    }

    public final EditorTouch getStates() {
        return state;
    }

    public final boolean canUndo() {
        return text.undoManager.canUndo();
    }

    public final boolean canRedo() {
        return text.undoManager.canRedo();
    }

    public final void redo() {
        text.undoManager.redo(text);
    }

    public final void undo() {
        text.undoManager.undo(text);
    }

    public final void notifySelChange() {
        if (editorInputConnection != null && !editorInputConnection.isBatchEdit() && hasFocus()) {
            final int a = Selection.getSelectionStart(text);
            final int b = Selection.getSelectionEnd(text);
            inputMethodManager.updateSelection(this, a, b, BaseInputConnection.getComposingSpanStart(text), BaseInputConnection.getComposingSpanEnd(text));
        }
    }

    public final float getLastOffset() {
        return offsetX;
    }

    public final float measureText(int st, int ed) {
        return paint.measureText(text.subSequence(st, ed).toString().replace("\t", "    "));
    }

    private void drawText_debug(Canvas canvas) {
        paint.setColor(style.defaultTextColor);
        final int m = getLastVisibleLine();
        selectionStart = Selection.getSelectionStart(text);
        selectionEnd = Selection.getSelectionEnd(text);
        tabSize = 4 * paint.measureText(" ");
        mlw = 0;
        for (int i = getFirstVisibleLine(); i <= m; i++) {
            drawLine_debug(text, i, offsetX, getLineBaseLineOnScreen(i), canvas);
        }
        state.setScrollMaxX((int) Math.ceil(mlw + paint.measureText(Integer.toString(getLineCount())) + 20) - getWidth() / 2);
    }

    private void drawLine_debug(CharSequence s, int li, float x, float y, Canvas canvas) {
        final int a = getLineStart(li);
        final int b = getLineEnd(li);
        final int c = selectionStart;
        final int d = selectionEnd;
        if (c != d && c <= b && d >= a) {
            final int left = Math.max(c, a);
            final int right = Math.min(d, b);
            final float startX = offsetX + paint.measureText(text.subSequence(a, left).toString().replace("\t", "    "));
            float endX = startX + paint.measureText(text.subSequence(left, right).toString().replace("\t", "    "));
            if (startX == endX) endX += paint.measureText("  ");
            paint.setColor(style.dividerLineColor);
            paint.setAlpha(120);
            canvas.drawRect(startX, getLineTopOnScreen(li), endX, getLineBottomOnScreen(li), paint);
            paint.setAlpha(255);
            paint.setColor(style.defaultTextColor);
        }
        final String r = text.subSequence(a, b).toString().replace("\t", "    ");
        canvas.drawText(r, x, y, paint);
        if (a <= c && b >= c && li == CurrLine) {
            final float xf = measureText(a, c) + x;
            drawCursorIfHereIs(c, c - 1, xf, li, canvas);
        }
        if (a <= d && b >= d && getLineByIndex(d) == li) {
            final float xf = measureText(a, d) + x;
            drawCursorIfHereIs(d, d - 1, xf, li, canvas);
        }
        mlw = Math.max(mlw, paint.measureText(r));
    }

    private void drawCursorIfHereIs(int cursorPos, int index, float offsetX, int line, Canvas c) {
        if (cursorPos == index + 1) {
            final int i = paint.getColor();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            c.drawLine(offsetX, getLineTopOnScreen(line), offsetX, getLineBottomOnScreen(line), paint);
            paint.setColor(i);
        }
    }


    private void drawCurrLineBackground(Canvas canvas) {
        final int a = Selection.getSelectionStart(text);
        final int b = getLineByIndex(a);
        CurrLine = b;
        if (getStart() == getEnd()) {
            paint.setColor(style.lineColor);
            final float top = getLineTopOnScreen(b);
            final float bot = top + style.getLineHeight();
            canvas.drawRect(0, top, getWidth(), bot, paint);
        }
    }

    private void drawLineNumbers(Canvas canvas) {
        final int m = getLastVisibleLine();
        float x = 0;
        final float width = paint.measureText(Integer.toString(getLineCount()));
        drawLineNumberBackground(canvas, offsetX, offsetX + width + 10);
        paint.setColor(style.lineNumberColor);
        int gv = style.lnGravity;
        switch (gv) {
            case Gravity.START:
                x = offsetX;
                paint.setTextAlign(Paint.Align.LEFT);
                break;
            case Gravity.END:
                x = offsetX + width;
                paint.setTextAlign(Paint.Align.RIGHT);
                break;
            case Gravity.CENTER:
                paint.setTextAlign(Paint.Align.CENTER);
                x = offsetX + width / 2;
                break;
        }
        x += 5;
        for (int i = getFirstVisibleLine(); i <= m; i++) {
            canvas.drawText(Integer.toString(i + 1), x, getLineBaseLineOnScreen(i), paint);
        }
        offsetX += width + 10;
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawDividerLine(Canvas canvas) {
        paint.setColor(style.dividerLineColor);
        canvas.drawRect(offsetX, 0, offsetX + 5, getHeight(), paint);
        offsetX += 10;
    }

    private void drawLineNumberBackground(Canvas canvas, float from, float to) {
        paint.setColor(style.lineNumberBackground);
        canvas.drawRect(from, 0, to, getHeight(), paint);
    }

    public final int getLineByThumbY(float thumbY) {
        final float j = thumbY + getOffsetOnScreen();
        final int i = (int) j / (int) style.getLineHeight() + getFirstVisibleLine();
        if (i < 0) return -1;
        else if (i >= getLineCount()) return -2;
        else return i;
    }

    public final int getStart() {
        return Selection.getSelectionStart(text);
    }

    public final int getEnd() {
        return Selection.getSelectionEnd(text);
    }

    public final void select(int i) {
        select(i, i);
    }

    public final void select(int st, int ed) {
        Selection.setSelection(text, st, ed);
        invalidate();
    }

    public final int getCharOffsetByThumb(float thumbX, int line) {
        if (line == -1) line = 0;
        else if (line == -2) line = getLineCount() - 1;
        float x = thumbX - getLastOffset();
        int st = getLineStart(line);
        int ed = getLineEnd(line);
        int j = st;
        float drawX = 0;
        while (drawX < x && j < ed) {
            drawX = measureText(st, j);
            j++;
        }
        if (st != ed && st < j && drawX > x) j--;
        return j;
    }

    private int getLeftPosition(int i) {
        if (i != 0) return i - 1;
        else return 0;
    }

    private int getRightPosition(int i) {
        if (i != text.length()) return i + 1;
        else return text.length();
    }

    private int getUpPosition(int i) {
        final int line = getLineByIndex(i);
        if (line == 0) return 0;
        else {
            int currSt = getLineStart(line);
            int lastSt = getLineStart(line - 1);
            int lastEd = getLineEnd(line - 1);
            int offset = i - currSt;
            int n = offset + lastSt;
            if (n > lastEd) n = lastEd;
            return n;
        }
    }

    private int getDownPosition(int i) {
        final int line = getLineByIndex(i);
        if (line >= getLineCount() - 1) return text.length();
        else {
            int currSt = getLineStart(line);
            int nxtSt = getLineStart(line + 1);
            int nxtEd = getLineEnd(line + 1);
            int offset = i - currSt;
            int n = offset + nxtSt;
            if (n > nxtEd) n = nxtEd;
            return n;
        }
    }

    public final void moveSelectionUp() {
        Selection.setSelection(text, getUpPosition(Selection.getSelectionStart(text)));
        invalidate();
    }

    public final void moveSelectionDown() {
        Selection.setSelection(text, getDownPosition(Selection.getSelectionStart(text)));
        invalidate();
    }

    public final void moveSelectionLeft() {
        Selection.setSelection(text, getLeftPosition(Selection.getSelectionStart(text)));
        invalidate();
    }

    public final void moveSelectionRight() {
        Selection.setSelection(text, getRightPosition(Selection.getSelectionStart(text)));
        invalidate();
    }

    public final void createSelectionControllerIfNeed() {
        if (selectionController == null)
            selectionController = new SelectionController(this);
    }

    private void ensureActionModeFinished() {
        if (actionMode != null) {
            try {
                actionMode.finish();
            } catch (Exception e) {
                // Timber.tag("CodeEditor").w(e, "Exception occurred when finishing last ActionMode:");
            }
            actionMode = null;
        }
    }

    public final void startSelectMode() {
        ensureActionModeFinished();
        actionMode = startActionMode(new SelectMode());
    }

    public final void stopActionMode() {
        ensureActionModeFinished();
    }

    @Override
    public final void computeScroll() {
        if (state.getScroller().computeScrollOffset()) invalidate();
        super.computeScroll();
    }

    private void calculateScrollMaxY() {
        state.setScrollMaxY(getLineCount() * ((int) style.getLineHeight()) - getHeight() / 2);
    }

    public final int getFirstVisibleLine() {
        final int i = state.getOffsetY() / ((int) style.getLineHeight());
        return Math.max(i, 0);
    }

    public final int getLastVisibleLine() {
        final int l = (int) Math.ceil((state.getOffsetY() + getHeight()) / style.getLineHeight());
        return (l < getLineCount() ? l : getLineCount() - 1);
    }

    public final long getLineBaseLineOnScreen(int line) {
        return (long) getLineBaseline(line - getFirstVisibleLine()) - getOffsetOnScreen();
    }

    public final long getLineTopOnScreen(int line) {
        return (long) style.getLineTop(line - getFirstVisibleLine()) - getOffsetOnScreen();
    }

    public final long getLineBottomOnScreen(int line) {
        return (long) style.getLineBottom(line - getFirstVisibleLine()) - getOffsetOnScreen();
    }

    final long getOffsetOnScreen() {
        final long offY = state.getOffsetY();
        final long fl = getFirstVisibleLine();
        return offY - (long) style.getLineHeight() * fl;
    }

    public final float getLineBaseline(int line) {
        final float top = style.top;
        final float bottom = style.bottom;
        return style.getLineHeight() * (line + 0.5f) + (top - bottom) / 2;
    }

    public final int getLineCount() {
        return text.getLineCount();
    }

    public final int getLineStart(int line) {
        int i = text.lineManager.getLineStart(line);
        if (line != 0) i++;
        return i;
    }

    public final int getLineEnd(int line) {
        return text.lineManager.getLineEnd(line);
    }

    public final int getLineByIndex(int charOffset) {
        try {
            if (text.charAt(charOffset) == '\n' && charOffset != 0) charOffset--;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.getLineByIndex(charOffset);
    }

    private boolean flagReplace = false;

    @Override
    public final void onReplace(Editable doc) {
        flagReplace = true;
    }

    @Override
    public final void onInsert(Editable doc, int index, CharSequence textToInsert) {
        if (height_wrap) requestLayout();
        calculateScrollMaxY();
        notifySelChange();
        invalidate();
    }

    @Override
    public final void onDelete(Editable doc, int index, CharSequence textDeleted) {
        if (flagReplace) flagReplace = false;
        else {
            notifySelChange();
            calculateScrollMaxY();
            if (height_wrap) requestLayout();
            invalidate();
        }
    }

    public final boolean isEditable() {
        return editable;
    }

    public final void setEditable(boolean b) {
        editable = b;
    }

    @Override
    public final boolean onCheckIsTextEditor() {
        return isEditable() && isEnabled();
    }

    @Override
    public final InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = EditorInfo.TYPE_CLASS_TEXT;
        outAttrs.initialSelStart = Selection.getSelectionStart(text);
        outAttrs.initialSelEnd = Selection.getSelectionEnd(text);
        text.resetBatchEdit();
        return onCheckIsTextEditor() ? (editorInputConnection = new EditorInputConnection(this)) : null;
    }

    private ClipboardManager getCm() {
        return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    private void selectAll() {
        select(0, text.length());
    }

    private void cut() {
        getCm().setPrimaryClip(ClipData.newPlainText("aaa", text.subSequence(getStart(), getEnd())));
        text.delete(getStart(), getEnd());
    }

    private void copy() {
        getCm().setPrimaryClip(ClipData.newPlainText("aaa", text.subSequence(getStart(), getEnd())));
    }

    private void paste() {
        final CharSequence s = Objects.requireNonNull(getCm().getPrimaryClip()).getItemAt(0).getText().toString();
        text.replace(getStart(), getEnd(), s);
    }

    private final class SelectMode implements ActionMode.Callback {
        @Override
        public final boolean onCreateActionMode(ActionMode var1, Menu var2) {
            if (getStart() == getEnd())
                return false;
            else {
                var1.setTitle("ad");
                int flag = MenuItem.SHOW_AS_ACTION_IF_ROOM;
                var2.add(0, 0, 0, resources.getString(R.string.select_all)).setShowAsActionFlags(flag).setAlphabeticShortcut('a').setIcon(ContextCompat.getDrawable(context, R.drawable.select_all));
                var2.add(0, 1, 0, resources.getString(R.string.cut)).setShowAsActionFlags(flag).setAlphabeticShortcut('x').setIcon(ContextCompat.getDrawable(context, R.drawable.cut));
                var2.add(0, 2, 0, resources.getString(R.string.copy)).setShowAsActionFlags(flag).setAlphabeticShortcut('c').setIcon(ContextCompat.getDrawable(context, R.drawable.copy));
                var2.add(0, 3, 0, resources.getString(R.string.paste)).setShowAsActionFlags(flag).setAlphabeticShortcut('v').setIcon(ContextCompat.getDrawable(context, R.drawable.paste));
                return true;
            }
        }

        @Override
        public final boolean onPrepareActionMode(ActionMode p1, Menu p2) {
            return false;
        }

        @Override
        public final boolean onActionItemClicked(ActionMode actionMode, MenuItem p2) {
            switch (p2.getItemId()) {
                case 0:
                    selectAll();
                    break;
                case 1:
                    cut();
                    actionMode.finish();
                    break;
                case 2:
                    copy();
                    actionMode.finish();
                    break;
                case 3:
                    paste();
                    actionMode.finish();
                    break;
            }
            return true;
        }

        @Override
        public final void onDestroyActionMode(ActionMode p1) {
            actionMode = null;
            state.setModification(false);
            select(getStart());
        }
    }

    public final class EditorStyle {
        private int defaultTextColor = Color.BLACK;
        private int selectedTextBackground = resources.getColor(R.color.selected_text_background);
        private int lineColor = resources.getColor(R.color.selected_text_background);
        private int lineNumberColor = resources.getColor(R.color.contents);
        private int dividerLineColor = resources.getColor(R.color.contents);
        private int lineNumberBackground = resources.getColor(R.color.text_editor_border);
        private int handleColor = resources.getColor(R.color.app_theme_half_transparent);
        private boolean showLineNumber = true;
        private boolean distancePixelOrDouble;
        private float distance;
        private float top, bottom, ascent, descent;
        private int lnGravity;

        private EditorStyle() {
            distancePixelOrDouble = true;
            distance = 0;
        }

        public final void setTypeface(Typeface typeface) {
            if (typeface == null) typeface = Typeface.MONOSPACE;
            paint.setTypeface(typeface);
            calculateScrollMaxY();
            invalidate();
        }

        public final Typeface getTypeface() {
            return paint.getTypeface();
        }

        public final void setTextSize(float size) {
            paint.setTextSize(size);
            final Paint.FontMetrics fm = paint.getFontMetrics();
            top = Math.abs(fm.top);
            bottom = Math.abs(fm.bottom);
            ascent = Math.abs(fm.ascent);
            descent = Math.abs(fm.descent);
            if (text != null) calculateScrollMaxY();
            invalidate();
        }

        public final float getTextSize() {
            return paint.getTextSize();
        }

        public final void setTextScaleX(float scaleX) {
            paint.setTextScaleX(scaleX);
            invalidate();
        }

        public final float getTextScaleX() {
            return paint.getTextScaleX();
        }

        public final float getTextSkewX() {
            return paint.getTextSkewX();
        }

        public final void setTextSkewX(float skewX) {
            paint.setTextSkewX(skewX);
            invalidate();
        }

        public final float getLineHeight() {
            return (getLineRealHeight() + getLineDistancePixel());
        }

        public final float getLineTop(int line) {
            return getLineHeight() * line;
        }

        public final float getLineBottom(int line) {
            return getLineTop(line) + getLineHeight();
        }

        public final float getLineRealHeight() {
            return (float) Math.ceil(descent) + (float) Math.ceil(ascent);
        }

        public final float getLineDistancePixel() {
            return (distancePixelOrDouble ? distance : getLineRealHeight() * distance);
        }

        public final void setLineDistancePixel(float pixel) {
            if (pixel < 0) throw new IllegalArgumentException("under zero is not allowed");
            else {
                distancePixelOrDouble = true;
                distance = pixel;
                calculateScrollMaxY();
                invalidate();
            }
        }

        public final void setLineDistanceDouble(float d) {
            if (d >= 0 && d <= 1) {
                distancePixelOrDouble = false;
                distance = d;
            } else throw new IllegalArgumentException("only 0~1 are accepted");
            calculateScrollMaxY();
            invalidate();
        }

        public final void setShowLineNumber(boolean s) {
            if (s != showLineNumber) {
                showLineNumber = s;
                invalidate();
            }
        }

        public final void setDefaultTextColor(int color) {
            this.defaultTextColor = color;
            invalidate();
        }

        public final int getDefaultTextColor() {
            return this.defaultTextColor;
        }

        public final void setLineNumberColor(int c) {
            lineNumberColor = c;
            if (showLineNumber) invalidate();
        }

        public final int getLineNumberColor() {
            return lineNumberColor;
        }

        public final void setDividerColor(int c) {
            dividerLineColor = c;
            if (showLineNumber) invalidate();
        }

        public final int getDividerColor() {
            return dividerLineColor;
        }

        public final void setLineNumberBackground(int c) {
            lineNumberBackground = c;
            if (showLineNumber) invalidate();
        }

        public final int getLineNumberBackground() {
            return lineNumberBackground;
        }

        public final void setCurrentLineColor(int c) {
            lineColor = c;
            if (getStart() == getEnd()) invalidate();
        }

        public final int getCurrentLineColor() {
            return lineColor;
        }

        public final void setHandleColor(int c) {
            handleColor = c;
            if (selectionController != null) {
                selectionController.getLeftHandle().setHandleColor(c);
                selectionController.getRightHandle().setHandleColor(c);
                if (getStart() != getEnd()) invalidate();
            }
        }

        public final int getHandleColor() {
            return handleColor;
        }

        public final void setSelectedTextBackgroundColor(int c) {
            selectedTextBackground = c;
            if (getStart() != getEnd()) invalidate();
        }

        public final int getSelectedTextBackgroundColor() {
            return selectedTextBackground;
        }

        public final void setLineNumberGravity(int gravity) {
            switch (gravity) {
                case Gravity.CENTER:
                case Gravity.START:
                case Gravity.END:
                    if (gravity != lnGravity) {
                        lnGravity = gravity;
                        invalidate();
                    }
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    setLineNumberGravity(Gravity.CENTER);
                    break;
                default:
                    throw new IllegalArgumentException("Given gravity mask not supported");
            }
        }

        public final int getLineNumberGravity() {
            return lnGravity;
        }
    }

    public static final class EditorText extends SpannableStringBuilder {
        public final WatcherTransformer transformer;
        public final UndoManger undoManager;
        public final LineManager lineManager;
        private int nestedBatchEdit;

        private EditorText(CharSequence src) {
            super(src);
            transformer = new WatcherTransformer(this);
            addWatcher(undoManager = new UndoManger());
            addWatcher(lineManager = new LineManager(this));
            if (this.length() != 0)
                lineManager.onInsert(this, 0, this);
            nestedBatchEdit = 0;
            transformer.addE(lineManager);
        }

        public final void addWatcher(TextWatcher r) {
            transformer.add(r);
        }

        public final void removeWatcher(TextWatcher r) {
            transformer.remove(r);
        }

        public final void beginBatchEdit() {
            nestedBatchEdit++;
            undoManager.setBatchEdit(nestedBatchEdit > 0);
        }

        public final void endBatchEdit() {
            nestedBatchEdit--;
            if (nestedBatchEdit < 0) nestedBatchEdit = 0;
            undoManager.setBatchEdit(nestedBatchEdit > 0);
        }

        public final void resetBatchEdit() {
            nestedBatchEdit = 0;
            undoManager.setBatchEdit(false);
        }

        public final int getLineCount() {
            return lineManager.getLineCount();
        }

        public final int getLineByIndex(int charIndex) {
            return lineManager.getLineByIndex(charIndex);
        }
    }

    static final class EditorInputConnection extends BaseInputConnection {
        private int nestedBE;
        private final TextEditor ce;

        private EditorInputConnection(TextEditor ce) {
            super(ce, true);
            this.ce = ce;
            nestedBE = 0;
        }

        @Override
        public final Editable getEditable() {
            return ce.isEditable() && ce.isEnabled() ? ce.getEditableText() : null;
        }

        @Override
        public final boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DEL:
                        deleteKey();
                        return true;
                    case KeyEvent.KEYCODE_ENTER:
                        //Insert a '\n'
                        commitText("\n", 1);
                        return true;
                    case KeyEvent.KEYCODE_SHIFT_LEFT:
                    case KeyEvent.KEYCODE_SHIFT_RIGHT:
                        //TODO
                        //The input method want to select text
                        //We should also handle the event which contains "DPAD" in its name
                        return true;
                    default:
                        //Unknown key event
                        //If debug is enabled,we will make a tip
                        //Debug.debug(event.keyCodeToString(event.getKeyCode()));
                        return super.sendKeyEvent(event);
                }
            } else return super.sendKeyEvent(event);
        }

        private void deleteKey() {
            int start = ce.getStart();
            int end = ce.getEnd();
            if (start == end) deleteSurroundingText(1, 0);
            else ce.getEditableText().delete(start, end);
        }

        @Override
        public final boolean beginBatchEdit() {
            ce.getEditableText().beginBatchEdit();
            nestedBE++;
            return isBatchEdit();
        }

        public final boolean isBatchEdit() {
            return nestedBE > 0;
        }

        @Override
        public final boolean endBatchEdit() {
            ce.getEditableText().endBatchEdit();
            nestedBE--;
            if (nestedBE < 0) nestedBE = 0;
            else if (nestedBE == 0) ce.notifySelChange();
            return isBatchEdit();
        }

        @Override
        public final boolean setSelection(int start, int end) {
            final EditorText text = ce.getEditableText();
            if (start == end && start == 0) {
                Selection.setSelection(text, Selection.getSelectionStart(text) - 1);
                ce.notifySelChange();
                return true;
            } else {
                boolean r = super.setSelection(start, end);
                if (r) ce.notifySelChange();
                return r;
            }
        }
    }
}