package com.mindrex.android.appsupport;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextPaint;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.android.debugger.Log;
import com.mindrex.util.vector.Pos3;

public abstract class E {
    public static void print(Exception e) {
        print(ContextUtils.getContext(), e);
    }

    public static void print(Context context, Exception e) {
        final Toast a = new Toast(context);
        a.setDuration(Toast.LENGTH_LONG);
        final AppCompatTextView b = new AppCompatTextView(context) {
            private final TextPaint a;
            private final TextPaint b;

            {
                a = new TextPaint(0);
                a.setTextSize(Utils.dpToPixel(16f));
                a.setTextAlign(Paint.Align.CENTER);
                a.setTypeface(Typeface.DEFAULT_BOLD);
                b = new TextPaint(a);
                a.setColor(Color.WHITE.toInt());
                b.setColor(Color.BLACK.toInt());
                a.setStyle(Paint.Style.FILL);
                b.setStyle(Paint.Style.STROKE);
                b.setStrokeWidth(2);
            }

            @Override
            public final void onDraw(Canvas canvas) {
                getPaint().set(a);
                super.onDraw(canvas);
                getPaint().set(b);
                super.onDraw(canvas);
            }

            @Override
            public final TextPaint getPaint() {
                return a;
            }
        };
        b.setBackground(new ColorDrawable(Color.parse(new Pos3(255, 127, 0)).toInt()));
        b.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        b.setText(e.toString());
        a.setView(b);
        a.show();
    }

    public static void log(Throwable e) {
        Log.c("null", e);
    }
}