package com.mindrex.android.appsupport;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.annotation.Call;
import com.mindrex.shell.R;
import com.mindrex.util.vector.Pos2;

@Call({BaseView.class, StandardView.class})
@SuppressWarnings("unused")
public class OutlineTextView extends AppCompatTextView implements BaseView {
    public final Context context;
    public final Resources resources;
    public final TextPaint paint;
    protected final Handler handler;

    public StandardView.TouchEventListener touchEventListener = null;
    public StandardView.OnClickListener onClickListener = null;
    public StandardView.OnDrawListener onDrawListener = null;
    public StandardView.OnMovementListener onMovementListener = null;
    public boolean allowInvalidate = true;
    public int outlineColor;
    public float outlineWidth;

    private Canvas canvas = new Canvas();
    private final Pos2 startPos = new Pos2(-1, -1);
    private final Runnable longClicked = () -> isLongClick = true;
    private boolean isLongClick = false;

    public OutlineTextView() throws RuntimeException {
        this(ContextUtils.getContext());
    }

    public OutlineTextView(Context context) {
        this(context, null);
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.resources = context.getResources();
        this.paint = super.getPaint();
        this.handler = new Handler(looper, this);
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OutlineTextView);
            this.outlineColor = a.getColor(R.styleable.OutlineTextView_textOutlineColor, getCurrentTextColor());
            this.outlineWidth = a.getFloat(R.styleable.OutlineTextView_textOutlineWidth, 1.5f);
            a.recycle();
        } else {
            this.outlineColor = resources.getColor(R.color.black);
            this.outlineWidth = 1.5f;
        }
        this.outlineWidth = Utils.dpToPixel(outlineWidth);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (outlineWidth > 0) {
            paint.setStyle(Paint.Style.FILL);
            super.onDraw(canvas);
            final int currentTextColor = getCurrentTextColor();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(outlineWidth);
            setTextColor(outlineColor);
            super.onDraw(canvas);
            setTextColor(currentTextColor);
        } else super.onDraw(canvas);
        if (onDrawListener != null) onDrawListener.onDraw(this, canvas);
        this.canvas = canvas;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchEventListener == null || touchEventListener.onTouchEvent(this, event);
    }

    protected void onClick(Pos2 pos) {
        if (onClickListener != null) onClickListener.onClick(this, pos);
    }

    protected void onLongClick(Pos2 pos) {
        if (onClickListener != null) onClickListener.onLongClick(this, pos);
    }

    protected void onMovement(Pos2 start, Pos2 end) {
        if (onMovementListener != null) onMovementListener.onMovement(start, end);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.canvas = canvas;
    }

    @Override
    public void invalidate() {
        if (allowInvalidate) {
            super.invalidate();
            if (onDrawListener != null) onDrawListener.onInvalidate(this);
        }
    }

    @NonNull
    @Override
    public final Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }

    @NonNull
    @Override
    public final Canvas getCanvas() {
        return canvas;
    }

    @NonNull
    @Override
    public final TextPaint getPaint() {
        return paint;
    }

    @Override
    public String toString() {
        return "OutlineTextView{" +
                "context=" + context +
                ", resources=" + resources +
                ", paint=" + paint +
                ", handler=" + handler +
                ", touchEventListener=" + touchEventListener +
                ", onClickListener=" + onClickListener +
                ", onDrawListener=" + onDrawListener +
                ", onMovementListener=" + onMovementListener +
                ", allowInvalidate=" + allowInvalidate +
                ", outlineColor=" + outlineColor +
                ", outlineWidth=" + outlineWidth +
                ", canvas=" + canvas +
                ", startPos=" + startPos +
                ", longClicked=" + longClicked +
                ", isLongClick=" + isLongClick +
                '}';
    }
}