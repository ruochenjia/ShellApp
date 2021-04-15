package com.mindrex.standardassets;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.mindrex.shell.BuildConfig;
import com.mindrex.util.Configuration;
import com.mindrex.util.DefaultConfiguration;
import com.mindrex.util.Sfx;

@SuppressWarnings("unused")
interface ContextFrame extends Sfx, Handler.Callback {
    static ContextFrame wrap(Context a) {
        return new StandardContext(a);
    }

    Looper looper = Looper.myLooper();

    Context getContext();

    Resources getResources();

    Handler getHandler();

    SharedPreferences getSharedPreferences(String name, int i);

    default Configuration getConfiguration(String name) throws Exception {
        return new SharedPreferences(name);
    }

    default Looper getLooper() {
        return looper;
    }

    default boolean debug() {
        return BuildConfig.DEBUG;
    }

    @Override
    default boolean handleMessage(@NonNull Message message) {
        return false;
    }
}