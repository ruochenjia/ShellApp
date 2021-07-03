package org.exbin.bined.color;

import java.util.Optional;

public enum BasicCodeAreaDecorationColorType implements CodeAreaColorType {
    LINE("decoration#", null);
    
    private final CodeAreaColorGroup group;
    private final String typeId;

    BasicCodeAreaDecorationColorType(String str, CodeAreaColorGroup codeAreaColorGroup) {
        this.typeId = str;
        this.group = codeAreaColorGroup;
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