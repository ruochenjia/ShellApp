package org.exbin.bined.android.basic.color;

import org.exbin.bined.color.CodeAreaBasicColors;
import org.exbin.bined.color.CodeAreaColorType;

public interface CodeAreaColorsProfile {
    Integer getColor(CodeAreaColorType codeAreaColorType);

    Integer getColor(CodeAreaColorType codeAreaColorType, CodeAreaBasicColors codeAreaBasicColors);

    void reinitialize();
}