package org.exbin.bined.color;

import java.util.Optional;

public enum CodeAreaBasicColors implements CodeAreaColorType {
    TEXT_COLOR("textColor", BasicCodeAreaColorGroup.MAIN),
    TEXT_BACKGROUND("textBackground", BasicCodeAreaColorGroup.MAIN),
    SELECTION_COLOR("selectionColor", BasicCodeAreaColorGroup.SELECTION),
    SELECTION_BACKGROUND("selectionBackground", BasicCodeAreaColorGroup.SELECTION),
    SELECTION_MIRROR_COLOR("selectionMirrorColor", BasicCodeAreaColorGroup.SELECTION),
    SELECTION_MIRROR_BACKGROUND("selectionMirrorBackground", BasicCodeAreaColorGroup.SELECTION),
    ALTERNATE_COLOR("alternateColor", BasicCodeAreaColorGroup.MAIN),
    ALTERNATE_BACKGROUND("alternateBackground", BasicCodeAreaColorGroup.MAIN),
    CURSOR_COLOR("cursorColor", null),
    CURSOR_NEGATIVE_COLOR("cursorNegativeColor", null);
    
    private final BasicCodeAreaColorGroup group;
    private final String typeId;

    CodeAreaBasicColors(String str, BasicCodeAreaColorGroup basicCodeAreaColorGroup) {
        this.typeId = str;
        this.group = basicCodeAreaColorGroup;
    }

    @Override // org.exbin.bined.color.CodeAreaColorType
    public Optional<CodeAreaColorGroup> getGroup() {
        return Optional.ofNullable(this.group);
    }

    @Override // org.exbin.bined.color.CodeAreaColorType
    public String getId() {
        return this.typeId;
    }
}