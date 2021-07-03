package org.exbin.bined.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.exbin.auxiliary.paged_data.BinaryData;
import org.exbin.bined.CodeAreaControl;
import org.exbin.bined.DataChangedListener;
import org.exbin.bined.android.basic.DefaultCodeAreaCommandHandler;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;

import java.util.ArrayList;
import java.util.List;

public abstract class CodeAreaCore extends ViewGroup implements CodeAreaControl {
    private CodeAreaCommandHandler commandHandler;
    private BinaryData contentData;
    private final List<DataChangedListener> dataChangedListeners;
    protected final PrimaryView primaryView;

    private class PrimaryView extends View {
        public PrimaryView(CodeAreaCore codeAreaCore, Context context, AttributeSet attributeSet) {
            this(context, attributeSet, null);
        }

        public PrimaryView(Context context, AttributeSet attributeSet, CodeAreaCommandHandler.CodeAreaCommandHandlerFactory codeAreaCommandHandlerFactory) {
            super(context, attributeSet);
            setFocusable(true);
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (canvas != null) {
                CodeAreaCore.this.paintView(canvas);
            }
        }

        /* access modifiers changed from: protected */
        public void onFocusChanged(boolean z, int i, Rect rect) {
            super.onFocusChanged(z, i, rect);
            CodeAreaCore.this.repaint();
        }

        public boolean onKeyDown(int i, KeyEvent keyEvent) {
            CodeAreaCore.this.commandHandler.keyTyped(i, keyEvent);
            return super.onKeyDown(i, keyEvent);
        }

        public boolean onKeyUp(int i, KeyEvent keyEvent) {
            CodeAreaCore.this.commandHandler.keyPressed(keyEvent);
            return super.onKeyUp(i, keyEvent);
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
        }

        /* access modifiers changed from: protected */
        public void onSizeChanged(int i, int i2, int i3, int i4) {
            super.onSizeChanged(i, i2, i3, i4);
            CodeAreaCore.this.resetPainter();
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            CodeAreaCore.this.onTouchEvent(motionEvent);
            return false;
        }
    }

    public CodeAreaCore(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, null);
    }

    public CodeAreaCore(Context context, AttributeSet attributeSet, CodeAreaCommandHandler.CodeAreaCommandHandlerFactory codeAreaCommandHandlerFactory) {
        super(context, attributeSet);
        this.dataChangedListeners = new ArrayList<>();
        this.commandHandler = codeAreaCommandHandlerFactory == null ? new DefaultCodeAreaCommandHandler(context, this) : codeAreaCommandHandlerFactory.createCommandHandler(this);
        init();
        PrimaryView primaryView2 = new PrimaryView(this, context, attributeSet);
        this.primaryView = primaryView2;
        addView(primaryView2, new RelativeLayout.LayoutParams(-1, -2));
    }

    private void init() {
    }

    public void addDataChangedListener(DataChangedListener dataChangedListener) {
        this.dataChangedListeners.add(dataChangedListener);
    }

    @Override // org.exbin.bined.CodeAreaControl
    public boolean canPaste() {
        return this.commandHandler.canPaste();
    }

    @Override // org.exbin.bined.CodeAreaControl
    public void clearSelection() {
        this.commandHandler.clearSelection();
    }

    @Override // org.exbin.bined.CodeAreaControl
    public void copy() {
        this.commandHandler.copy();
    }

    public void copyAsCode() {
        this.commandHandler.copyAsCode();
    }

    @Override // org.exbin.bined.CodeAreaControl
    public void cut() {
        this.commandHandler.cut();
    }

    @Override // org.exbin.bined.CodeAreaControl
    public void delete() {
        this.commandHandler.delete();
    }

    public CodeAreaCommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    @Override // org.exbin.bined.DataProvider
    public BinaryData getContentData() {
        return this.contentData;
    }

    @Override // org.exbin.bined.DataProvider
    public long getDataSize() {
        BinaryData binaryData = this.contentData;
        if (binaryData == null) {
            return 0;
        }
        return binaryData.getDataSize();
    }

    @Override // org.exbin.bined.CodeAreaControl
    public boolean hasSelection() {
        if (this instanceof SelectionCapable) {
            return ((SelectionCapable) this).hasSelection();
        }
        return false;
    }

    public void notifyDataChanged() {
        resetPainter();
        for (DataChangedListener dataChangedListener : this.dataChangedListeners) {
            dataChangedListener.dataChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (z) {
            this.primaryView.layout(i, i2, i3, i4);
            resetPainter();
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        this.commandHandler.moveCaret((int) motionEvent.getX(), (int) motionEvent.getY(), CodeAreaCommandHandler.SelectingMode.NONE);
        ((ScrollingCapable) this).revealCursor();
        return true;
    }

    public abstract void paintView(Canvas canvas);

    @Override // org.exbin.bined.CodeAreaControl
    public void paste() {
        this.commandHandler.paste();
    }

    public void pasteFromCode() {
        this.commandHandler.pasteFromCode();
    }

    public void removeDataChangedListener(DataChangedListener dataChangedListener) {
        this.dataChangedListeners.remove(dataChangedListener);
    }

    public void repaint() {
        /* class org.exbin.bined.android.CodeAreaCore.AnonymousClass1 */
        ((Activity) getContext()).runOnUiThread(() -> {
            CodeAreaCore.this.invalidate();
            CodeAreaCore.this.primaryView.invalidate();
        });
    }

    public abstract void resetPainter();

    @Override // org.exbin.bined.CodeAreaControl
    public void selectAll() {
        this.commandHandler.selectAll();
    }

    public void setCommandHandler(CodeAreaCommandHandler codeAreaCommandHandler) {
        this.commandHandler = codeAreaCommandHandler;
    }

    public void setContentData(BinaryData binaryData) {
        this.contentData = binaryData;
        notifyDataChanged();
        repaint();
    }

    public abstract void updateLayout();
}
