package com.mindrex.android.appsupport;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.mindrex.android.debugger.Log;
import com.mindrex.util.Configuration;
import com.mindrex.util.Sfx;

@SuppressWarnings("unused")
public final class ContextUtils {
    public static Context getContext() {
        final Context a = (Context) ctx.base;
        return a == null ? StandardContext.getDefaultContext() : a;
    }

    public static Context getApplicationContext() {
        return getContext().getApplicationContext();
    }

    public static Resources getResources() {
        return getApplicationContext().getResources();
    }

    public static Configuration getConfiguration(String name) throws Exception {
        return ContextFrame.wrap(getContext()).getConfiguration(name);
    }

    static final ContextUtils ctx;

    static {
        ctx = new ContextUtils();
    }

    private Object base;

    private ContextUtils() {
    }

    public final void attachBase(Object obj) {
        if (base == null) {
            Sfx.ass(Sfx.nonnull(obj) instanceof Context);
            this.base = obj;
        } else Log.attachStorageContext();
    }

    public final void destroy() {
        if (base != null) {
            base = null;
            Log.stopCommand = true;
            Runtime.getRuntime().gc();
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "ContextUtils{" +
                "base=" + base +
                '}';
    }
}