package com.mindrex.android.appsupport;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.mindrex.android.debugger.Log;

import org.json.JSONObject;

@TargetApi(Build.VERSION_CODES.P)
public final class ComponentFactory extends AppComponentFactory {
    private final androidx.core.app.AppComponentFactory base;

    public ComponentFactory() {
        this.base = new androidx.core.app.AppComponentFactory();
    }

    @Override
    public final ClassLoader instantiateClassLoader(ClassLoader a, ApplicationInfo b) {
        try {
            Log.i(this, "Instantiate Class Loader: " + new JSONObject()
                    .put("Class Loader", a.getClass().getName())
                    .put("Package Name", b.packageName));
        } catch (Exception e) {
            Log.c(this, e);
        }
        return a;
    }

    @Override
    public final Application instantiateApplication(ClassLoader a, String b) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            Log.i(this, "Instantiate Application: " + new JSONObject()
                    .put("Class Loader", a.getClass().getName())
                    .put("Application", b));
        } catch (Exception e) {
            Log.c(this, e);
        }
        return base.instantiateApplication(a, b);
    }

    @Override
    public final Activity instantiateActivity(ClassLoader a, String b, Intent c) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            Log.i(this, "Instantiate Activity: " + new JSONObject()
                    .put("Class Loader", a.getClass().getName())
                    .put("Activity", b)
                    .put("Intent", c.getDataString()));
        } catch (Exception e) {
            Log.c(this, e);
        }
        return base.instantiateActivity(a, b, c);
    }

    @Override
    public final BroadcastReceiver instantiateReceiver(ClassLoader a, String b, Intent c) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            Log.i(this, "Instantiate Receiver: " + new JSONObject()
                    .put("Class Loader", a.getClass().getName())
                    .put("Receiver", b)
                    .put("Intent", c.getDataString())
            );
        } catch (Exception e) {
            Log.c(this, e);
        }
        return base.instantiateReceiver(a, b, c);
    }

    @Override
    public final Service instantiateService(ClassLoader a, String b, Intent c) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            Log.i(this, "Instantiate Service: " + new JSONObject()
                    .put("Class Loader", a.getClass().getName())
                    .put("Service", b)
                    .put("Intent", c.getDataString()));
        } catch (Exception e) {
            Log.c(this, e);
        }
        return base.instantiateService(a, b, c);
    }

    @Override
    public final ContentProvider instantiateProvider(ClassLoader a, String b) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            Log.i(this, "Instantiate Provider: " + new JSONObject()
                    .put("Class Loader", a.getClass().toString())
                    .put("Provider", b));
        } catch (Exception e) {
            Log.c(this, e);
        }
        return base.instantiateProvider(a, b);
    }

    @Override
    public final String toString() {
        return "ComponentFactory{" +
                "base=" + base +
                '}';
    }
}