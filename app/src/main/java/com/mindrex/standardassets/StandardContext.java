package com.mindrex.standardassets;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.os.Handler;

import androidx.annotation.NonNull;

public class StandardContext extends ContextWrapper implements ContextFrame {
    public static native Context getDefaultContext();

    protected final Handler handler;

    StandardContext(Context a) {
        super(a);
        this.handler = new Handler(looper, this);
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    @Override
    public final Handler getHandler() {
        return handler;
    }

    @Deprecated
    @Override
    public final SharedPreferences getSharedPreferences(String name, int mode) {
        try {
            return (SharedPreferences) getConfiguration(name);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    @Override
    public final Resources getResources() {
        return super.getResources();
    }

    @NonNull
    @Override
    public String toString() {
        return "StandardContext{" +
                "handler=" + handler +
                '}';
    }
}