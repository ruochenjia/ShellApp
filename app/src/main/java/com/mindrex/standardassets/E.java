package com.mindrex.standardassets;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import com.mindrex.debugger.Log;
import com.mindrex.shell.R;

public abstract class E {
    public static void print(Exception e) {
        print(ContextUtils.getContext(), e);
    }

    public static void print(Context context, Exception e) {
        if (context == null) print(e);
        else new Toast(context)
                .setDuration(0)
                .setView(new LinearLayout(context) {
                    private View getThis() {
                        final OutlineTextView a = new OutlineTextView(context);
                        a.outlineWidth = 1.3f;
                        a.setText(e.toString());
                        a.setTextColor(Color.BLACK.toInt());
                        a.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
                        addViewInLayout(a, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        setBackgroundColor(Color.RED.toInt());
                        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        return this;
                    }
                }.getThis())
                .show();
    }

    public static void log(Throwable e) {
        Log.c(e);
    }

    @SuppressWarnings("unused")
    private static final class Toast {
        public final Context context;
        public final Resources resources;
        private final android.widget.Toast base;
        private CharSequence text;

        public Toast(Context context) {
            base = new android.widget.Toast(context);
            this.context = context;
            this.resources = context.getResources();
        }

        public Toast(@NonNull Context context, CharSequence s, int duration) {
            this.context = context;
            this.resources = context.getResources();
            base = new android.widget.Toast(context);
            base.setText(s);
            base.setDuration(duration);
            this.text = s;
        }

        public Toast(@NonNull Context context, int i, int duration) {
            this.context = context;
            this.resources = context.getResources();
            base = new android.widget.Toast(context);
            base.setText(i);
            base.setDuration(duration);
            this.text = resources.getString(i);
        }

        public final Toast setDuration(int duration) {
            base.setDuration(duration);
            return this;
        }

        public final Toast setText(CharSequence s) {
            base.setText(s);
            this.text = s;
            return this;
        }

        public final Toast setText(int i) {
            return setText(resources.getString(i));
        }

        public final Toast setView(View v) {
            base.setView(v);
            return this;
        }

        public final void show() {
            base.show();
        }

        public final View getView() {
            return base.getView();
        }
    }
}