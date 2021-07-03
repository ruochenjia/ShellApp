package org.exbin.bined.android.capability;

import org.exbin.bined.basic.BasicBackgroundPaintMode;

public interface BackgroundPaintCapable {
    BasicBackgroundPaintMode getBackgroundPaintMode();

    void setBackgroundPaintMode(BasicBackgroundPaintMode basicBackgroundPaintMode);
}