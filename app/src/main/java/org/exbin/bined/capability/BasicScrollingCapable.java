package org.exbin.bined.capability;

import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.basic.HorizontalScrollUnit;
import org.exbin.bined.basic.VerticalScrollUnit;

public interface BasicScrollingCapable {
    ScrollBarVisibility getHorizontalScrollBarVisibility();

    HorizontalScrollUnit getHorizontalScrollUnit();

    ScrollBarVisibility getVerticalScrollBarVisibility();

    VerticalScrollUnit getVerticalScrollUnit();

    void setHorizontalScrollBarVisibility(ScrollBarVisibility scrollBarVisibility);

    void setHorizontalScrollUnit(HorizontalScrollUnit horizontalScrollUnit);

    void setVerticalScrollBarVisibility(ScrollBarVisibility scrollBarVisibility);

    void setVerticalScrollUnit(VerticalScrollUnit verticalScrollUnit);
}