package com.mindrex.standardassets;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class StandardService extends Service implements ContextFrame {
    protected final Handler handler;

    public StandardService() {
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
    public SharedPreferences getSharedPreferences(String name, int mode) {
        try {
            return (SharedPreferences) getConfiguration(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "StandardService{" +
                "handler=" + handler +
                '}';
    }
}