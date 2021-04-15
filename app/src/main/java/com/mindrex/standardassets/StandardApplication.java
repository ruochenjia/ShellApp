package com.mindrex.standardassets;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.multidex.MultiDexApplication;

import com.mindrex.debugger.CrashHandler;
import com.mindrex.debugger.Log;

public abstract class StandardApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks, ComponentCallbacks, ContextFrame, Thread.UncaughtExceptionHandler {
    protected final Handler handler;

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPrePaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPostPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPreSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ContextUtils.ctx.attachBase(base);
        if (!debug()) Log.stopCommand = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtils.ctx.attachBase(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        synchronized (this) {
            Log.c(t, e);
            if (t.getName().equals("main"))
                CrashHandler.crash(e);
        }
        System.exit(-1);
    }

    @NonNull
    @Override
    public final Context getContext() {
        return getBaseContext() == null ? ContextUtils.getContext() : this;
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
    public String toString() {
        return "StandardApplication{" +
                "handler=" + handler +
                '}';
    }

    public static void killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @SuppressWarnings("unused")
    public static void disableSelf(Context ctx) {
        ctx.getPackageManager().setApplicationEnabledSetting(ctx.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
    }

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager a = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo b = a.getActiveNetworkInfo();
        if (b == null) return false;
        else return b.isConnected();
    }

    public StandardApplication() throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(this);
        System.in.close();
        System.out.close();
        this.handler = new Handler(looper, this);
    }
}