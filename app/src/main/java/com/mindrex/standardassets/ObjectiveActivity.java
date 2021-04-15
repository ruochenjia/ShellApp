package com.mindrex.standardassets;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

abstract class ObjectiveActivity extends AppCompatActivity implements Activity, ContextFrame {
    private boolean activityInitialized = false;

    public ObjectiveActivity() {
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onPreCreate();
    }

    @Override
    public void onPreCreate() {
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!activityInitialized) {
            onCreate();
            activityInitialized = true;
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void mkdir(File f) {
        Activity.mkdir(f);
    }
}

interface Activity {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void mkdir (File f) {
        if (!f.exists()) f.mkdirs();
    }

    void onPreCreate();

    void onCreate();

    void onPause();

    void onResume();

    void onDestroy();
}