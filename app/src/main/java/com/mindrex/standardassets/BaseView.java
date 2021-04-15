package com.mindrex.standardassets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;

import com.mindrex.annotation.Call;

@SuppressWarnings("unused")
@Call(View.class)
interface BaseView extends ContextFrame {
    @NonNull
    Canvas getCanvas();

    @NonNull
    Paint getPaint();

    Handler getHandler();

    @Deprecated
    @Override
    default SharedPreferences getSharedPreferences(String name, int i) {
        try {
            return (SharedPreferences) getConfiguration(name);
        } catch (Exception e) {
            return null;
        }
    }

    default BaseView getView() {
        return this;
    }
}