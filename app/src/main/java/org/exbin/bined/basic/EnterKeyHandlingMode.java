package org.exbin.bined.basic;

public enum EnterKeyHandlingMode {
    PLATFORM_SPECIFIC(""),
    CR("\r"),
    LF("\n"),
    CRLF("\r\n"),
    IGNORE("");
    
    private final String sequence;

    EnterKeyHandlingMode(String str) {
        this.sequence = str;
    }

    public String getSequence() {
        return this == PLATFORM_SPECIFIC ? System.lineSeparator() : this.sequence;
    }
}