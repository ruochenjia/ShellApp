package org.exbin.bined.color;

public enum BasicCodeAreaColorGroup implements CodeAreaColorGroup {
    MAIN("main"),
    SELECTION("selection");
    
    private final String groupId;

    BasicCodeAreaColorGroup(String str) {
        this.groupId = str;
    }

    @Override // org.exbin.bined.color.CodeAreaColorGroup
    public String getId() {
        return this.groupId;
    }
}