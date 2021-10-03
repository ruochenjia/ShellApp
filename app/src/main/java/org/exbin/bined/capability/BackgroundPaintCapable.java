package org.exbin.bined.capability;

import org.exbin.bined.basic.BasicBackgroundPaintMode;

public interface BackgroundPaintCapable {
    BasicBackgroundPaintMode getBackgroundPaintMode();

    void setBackgroundPaintMode(BasicBackgroundPaintMode basicBackgroundPaintMode);
}