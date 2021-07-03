package org.exbin.bined.android.basic;

import org.exbin.bined.android.capability.BackgroundPaintCapable;
import org.exbin.bined.android.capability.BasicColorsCapable;
import org.exbin.bined.android.capability.FontCapable;
import org.exbin.bined.capability.BasicScrollingCapable;
import org.exbin.bined.capability.CaretCapable;
import org.exbin.bined.capability.CharsetCapable;
import org.exbin.bined.capability.ClipboardCapable;
import org.exbin.bined.capability.CodeCharactersCaseCapable;
import org.exbin.bined.capability.CodeTypeCapable;
import org.exbin.bined.capability.EditationModeCapable;
import org.exbin.bined.capability.RowWrappingCapable;
import org.exbin.bined.capability.ScrollingCapable;
import org.exbin.bined.capability.SelectionCapable;
import org.exbin.bined.capability.ViewModeCapable;

public interface DefaultCodeArea extends SelectionCapable, CaretCapable, BasicScrollingCapable, ScrollingCapable, ViewModeCapable, CodeTypeCapable, EditationModeCapable, CharsetCapable, CodeCharactersCaseCapable, FontCapable, BackgroundPaintCapable, RowWrappingCapable, ClipboardCapable, BasicColorsCapable {
}