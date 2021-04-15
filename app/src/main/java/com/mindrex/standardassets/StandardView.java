package com.mindrex.standardassets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mindrex.annotation.Call;
import com.mindrex.util.vector.Pos2;

@Call(BaseView.class)
@SuppressWarnings("unused")
public class StandardView extends View implements BaseView {
    public static final int CLICK_ACTION_THRESHOLD = 200;

    public final Context context;
    public final Resources resources;
    public final Paint paint;
    protected final Handler handler;

    public TouchEventListener touchEventListener = null;
    public OnClickListener onClickListener = null;
    public OnDrawListener onDrawListener = null;
    public OnMovementListener onMovementListener = null;
    public boolean allowInvalidate = true;
    public boolean showContentDescriptionOnLongClick = false;

    private Canvas canvas = new Canvas();
    private final Pos2 startPos = new Pos2(-1, -1);
    private final Runnable longClicked = () -> isLongClick = true;
    private boolean isLongClick = false;

    public StandardView(Context context) {
        this(context, null);
    }

    public StandardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StandardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context == null ? ContextUtils.getContext() : context, attrs, defStyleAttr, false);
    }

    private StandardView(@NonNull Context ctx, AttributeSet atr, int i, boolean e) {
        super(ctx, atr, i);
        this.context = ctx;
        this.resources = ctx.getResources();
        this.paint = new Paint(0);
        this.handler = new Handler(looper, this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (onDrawListener != null) onDrawListener.onDraw(this, canvas);
        this.canvas = canvas;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean v;
        if (touchEventListener != null)
            v = touchEventListener.onTouchEvent(this, event);
        else v = true;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isLongClick = false;
            startPos.x = event.getX();
            startPos.y = event.getY();
            handler.postDelayed(longClicked, ViewConfiguration.getLongPressTimeout());
            return v;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            handler.removeCallbacks(longClicked);
            final Pos2 pos = new Pos2(event.getX(), event.getY());
            final float a = Math.abs(startPos.x - pos.x);
            final float b = Math.abs(startPos.y - pos.y);
            if (!(a > CLICK_ACTION_THRESHOLD || b > CLICK_ACTION_THRESHOLD)) {
                if (isLongClick) onLongClick(startPos);
                else onClick(startPos);
            } else onMovement(startPos, pos);
            return v;
        } else return v;
    }

    protected void onClick(Pos2 pos) {
        if (onClickListener != null) onClickListener.onClick(this, pos);
    }

    protected void onLongClick(Pos2 pos) {
        if (onClickListener != null) onClickListener.onLongClick(this, pos);
        if (showContentDescriptionOnLongClick)
            Toast.makeText(context, getContentDescription(), Toast.LENGTH_SHORT).show();
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
        return resources;
    }

    @NonNull
    @Override
    public final Canvas getCanvas() {
        return canvas;
    }

    @NonNull
    @Override
    public final Paint getPaint() {
        return paint;
    }

    @Override
    public final Handler getHandler() {
        return handler;
    }

    public static StateListDrawable createSelector(int defaultColor, int selectedColor) {
        final StateListDrawable a = new StateListDrawable();
        a.setExitFadeDuration(500);
        a.setAlpha(0x7f);
        a.addState(new int[]{android.R.attr.state_selected}, new ColorDrawable(selectedColor));
        a.addState(new int[]{}, new ColorDrawable(defaultColor));
        return a;
    }

    public interface TouchEventListener {
        boolean onTouchEvent(View view, MotionEvent event);
    }

    public interface OnClickListener {
        void onClick(View view, Pos2 pos);

        default void onLongClick(View view, Pos2 pos) {
        }
    }

    public interface OnDrawListener {
        void onDraw(View view, Canvas canvas);

        default void onInvalidate(View view) {
        }
    }

    public interface OnMovementListener {
        void onMovement(Pos2 start, Pos2 end);
    }

    @NonNull
    @Override
    public String toString() {
        return "StandardView{" +
                "context=" + context +
                ", resources=" + resources +
                ", paint=" + paint +
                ", handler=" + handler +
                ", touchEventListener=" + touchEventListener +
                ", onClickListener=" + onClickListener +
                ", onDrawListener=" + onDrawListener +
                ", onMovementListener=" + onMovementListener +
                ", allowInvalidate=" + allowInvalidate +
                ", showContentDescriptionOnLongClick=" + showContentDescriptionOnLongClick +
                ", canvas=" + canvas +
                ", startPos=" + startPos +
                ", longClicked=" + longClicked +
                ", isLongClick=" + isLongClick +
                '}';
    }
}