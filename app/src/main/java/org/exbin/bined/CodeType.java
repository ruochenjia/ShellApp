package org.exbin.bined;

public enum CodeType {
    BINARY(2, 8),
    OCTAL(8, 3),
    DECIMAL(10, 3),
    HEXADECIMAL(16, 2);
    
    private final int base;
    private final int maxDigitsForByte;

    CodeType(int i, int i2) {
        this.base = i;
        this.maxDigitsForByte = i2;
    }

    public int getBase() {
        return this.base;
    }

    public int getMaxDigitsForByte() {
        return this.maxDigitsForByte;
    }
}