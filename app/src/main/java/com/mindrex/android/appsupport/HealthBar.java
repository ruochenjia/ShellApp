package com.mindrex.android.appsupport;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.mindrex.shell.R;

@SuppressWarnings("unused")
public final class HealthBar extends StandardView {
    private int barColor = 0;
    private int backgroundColor = 0x7f323232;
    private String label = "";
    private float health = 1f;

    public HealthBar() throws RuntimeException {
        this(ContextUtils.getContext());
    }

    public HealthBar(Context context) {
        this(context, null);
    }

    public HealthBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HealthBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public final void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(resources.getColor(R.color.black));
        canvas.drawColor(backgroundColor);
        paint.setColor(barColor);
        canvas.drawRect(new Rect(0, 0, (int) (getRight() * health), getBottom()), paint);
        paint.setColor(resources.getColor(R.color.white));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(32f);
        canvas.drawText(label, 50, 30, paint);
    }

    public final void setBarColor(int i) {
        barColor = i;
        invalidate();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            invalidateOutline();
    }

    @Override
    public final void setBackgroundColor(int i) {
        backgroundColor = i;
        invalidate();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            invalidateOutline();
    }

    public final void setLabel(String s) {
        label = s;
        invalidate();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            invalidateOutline();
    }

    public final void setLabel(int i) {
        setLabel(resources.getString(i));
    }

    public final void setHealth(float f) {
        health = f;
        invalidate();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            invalidateOutline();
    }
}