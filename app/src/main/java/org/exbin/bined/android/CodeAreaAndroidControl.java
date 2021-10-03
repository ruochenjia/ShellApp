package org.exbin.bined.android;

import android.graphics.Canvas;

public interface CodeAreaAndroidControl {
    void paintComponent(Canvas canvas);

    void reset();

    void resetColors();

    void updateLayout();
}