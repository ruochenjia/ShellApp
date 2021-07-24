package org.exbin.bined;

public enum PositionCodeType {
    OCTAL(8, 3),
    DECIMAL(10, 3),
    HEXADECIMAL(16, 2);
    
    private final int base;
    private final double baseLog;
    private final int maxDigitsForByte;

    private PositionCodeType(int i, int i2) {
        this.base = i;
        this.baseLog = Math.log((double) i);
        this.maxDigitsForByte = i2;
    }

    public int getBase() {
        return this.base;
    }

    public double getBaseLog() {
        return this.baseLog;
    }

    public int getMaxDigitsForByte() {
        return this.maxDigitsForByte;
    }
}
