package org.exbin.bined.basic;

import org.exbin.auxiliary.paged_data.PagedData;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeType;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.RowWrappingMode;

public class BasicCodeAreaLayout {

    /* renamed from: org.exbin.bined.basic.BasicCodeAreaLayout$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$basic$MovementDirection;

        static {
            int[] iArr = new int[MovementDirection.values().length];
            $SwitchMap$org$exbin$bined$basic$MovementDirection = iArr;
            try {
                iArr[MovementDirection.LEFT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.RIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.UP.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.DOWN.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.ROW_START.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.ROW_END.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.PAGE_UP.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.PAGE_DOWN.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.DOC_START.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.DOC_END.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$MovementDirection[MovementDirection.SWITCH_SECTION.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
        }
    }

    public int computeBytesPerRow(BasicCodeAreaStructure basicCodeAreaStructure, int i) {
        int i2;
        int i3;
        CodeAreaViewMode viewMode = basicCodeAreaStructure.getViewMode();
        CodeType codeType = basicCodeAreaStructure.getCodeType();
        int maxBytesPerLine = basicCodeAreaStructure.getMaxBytesPerLine();
        int wrappingBytesGroupSize = basicCodeAreaStructure.getWrappingBytesGroupSize();
        if (basicCodeAreaStructure.getRowWrapping() == RowWrappingMode.WRAPPING) {
            int i4 = 0;
            if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
                i4 = codeType.getMaxDigitsForByte() + 1;
            }
            if (viewMode != CodeAreaViewMode.CODE_MATRIX) {
                i4++;
            }
            i2 = i / i4;
            if (maxBytesPerLine > 0 && i2 > maxBytesPerLine) {
                i2 = maxBytesPerLine;
            }
            if (wrappingBytesGroupSize > 1 && (i3 = i2 % wrappingBytesGroupSize) > 0) {
                i2 -= i3;
            }
        } else {
            i2 = maxBytesPerLine;
        }
        return Math.max(i2, 1);
    }

    public int computeCharactersPerRow(BasicCodeAreaStructure basicCodeAreaStructure) {
        CodeAreaViewMode viewMode = basicCodeAreaStructure.getViewMode();
        int bytesPerRow = basicCodeAreaStructure.getBytesPerRow();
        int i = 0;
        if (viewMode != CodeAreaViewMode.TEXT_PREVIEW) {
            i = computeLastCodeCharPos(basicCodeAreaStructure, bytesPerRow - 1) + 1;
        }
        if (viewMode == CodeAreaViewMode.CODE_MATRIX) {
            return i;
        }
        int i2 = i + bytesPerRow;
        return viewMode == CodeAreaViewMode.DUAL ? i2 + 1 : i2;
    }

    public int computeFirstCodeCharacterPos(BasicCodeAreaStructure basicCodeAreaStructure, int i) {
        return (basicCodeAreaStructure.getCodeType().getMaxDigitsForByte() + 1) * i;
    }

    public int computeLastCodeCharPos(BasicCodeAreaStructure basicCodeAreaStructure, int i) {
        CodeType codeType = basicCodeAreaStructure.getCodeType();
        return (codeType.getMaxDigitsForByte() + ((codeType.getMaxDigitsForByte() + 1) * i)) - 1;
    }

    public CodeAreaCaretPosition computeMovePosition(BasicCodeAreaStructure basicCodeAreaStructure, CodeAreaCaretPosition codeAreaCaretPosition, MovementDirection movementDirection, int i) {
        CodeType codeType = basicCodeAreaStructure.getCodeType();
        int bytesPerRow = basicCodeAreaStructure.getBytesPerRow();
        long dataSize = basicCodeAreaStructure.getDataSize();
        CodeAreaSection orElse = codeAreaCaretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
        DefaultCodeAreaCaretPosition defaultCodeAreaCaretPosition = new DefaultCodeAreaCaretPosition(codeAreaCaretPosition.getDataPosition(), codeAreaCaretPosition.getCodeOffset(), orElse);
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$MovementDirection[movementDirection.ordinal()]) {
            case 1:
                if (orElse == BasicCodeAreaSection.TEXT_PREVIEW) {
                    if (codeAreaCaretPosition.getDataPosition() > 0) {
                        defaultCodeAreaCaretPosition.setDataPosition(codeAreaCaretPosition.getDataPosition() - 1);
                        break;
                    }
                } else {
                    int codeOffset = codeAreaCaretPosition.getCodeOffset();
                    if (codeOffset <= 0) {
                        if (codeAreaCaretPosition.getDataPosition() > 0) {
                            defaultCodeAreaCaretPosition.setDataPosition(codeAreaCaretPosition.getDataPosition() - 1);
                            defaultCodeAreaCaretPosition.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                            break;
                        }
                    } else {
                        defaultCodeAreaCaretPosition.setCodeOffset(codeOffset - 1);
                        break;
                    }
                }
                break;
            case 2:
                if (orElse == BasicCodeAreaSection.TEXT_PREVIEW) {
                    if (codeAreaCaretPosition.getDataPosition() < dataSize) {
                        defaultCodeAreaCaretPosition.setDataPosition(codeAreaCaretPosition.getDataPosition() + 1);
                        break;
                    }
                } else {
                    int codeOffset2 = codeAreaCaretPosition.getCodeOffset();
                    if (codeAreaCaretPosition.getDataPosition() >= dataSize || codeOffset2 >= codeType.getMaxDigitsForByte() - 1) {
                        if (codeAreaCaretPosition.getDataPosition() < dataSize) {
                            defaultCodeAreaCaretPosition.setDataPosition(codeAreaCaretPosition.getDataPosition() + 1);
                            defaultCodeAreaCaretPosition.setCodeOffset(0);
                            break;
                        }
                    } else {
                        defaultCodeAreaCaretPosition.setCodeOffset(codeOffset2 + 1);
                        break;
                    }
                }
                break;
            case 3:
                if (codeAreaCaretPosition.getDataPosition() >= ((long) bytesPerRow)) {
                    defaultCodeAreaCaretPosition.setDataPosition(codeAreaCaretPosition.getDataPosition() - ((long) bytesPerRow));
                    break;
                }
                break;
            case 4:
                if (codeAreaCaretPosition.getDataPosition() + ((long) bytesPerRow) < dataSize || (codeAreaCaretPosition.getDataPosition() + ((long) bytesPerRow) == dataSize && codeAreaCaretPosition.getCodeOffset() == 0)) {
                    defaultCodeAreaCaretPosition.setDataPosition(codeAreaCaretPosition.getDataPosition() + ((long) bytesPerRow));
                    break;
                }
            case 5:
                long dataPosition = codeAreaCaretPosition.getDataPosition();
                defaultCodeAreaCaretPosition.setDataPosition(dataPosition - (dataPosition % ((long) bytesPerRow)));
                defaultCodeAreaCaretPosition.setCodeOffset(0);
                break;
            case 6:
                long dataPosition2 = codeAreaCaretPosition.getDataPosition();
                long j = ((long) (bytesPerRow - 1)) - (dataPosition2 % ((long) bytesPerRow));
                if (dataPosition2 > PagedData.MAX_DATA_SIZE - j || dataPosition2 + j > dataSize) {
                    defaultCodeAreaCaretPosition.setDataPosition(dataSize);
                } else {
                    defaultCodeAreaCaretPosition.setDataPosition(dataPosition2 + j);
                }
                if (orElse != BasicCodeAreaSection.TEXT_PREVIEW) {
                    if (defaultCodeAreaCaretPosition.getDataPosition() != dataSize) {
                        defaultCodeAreaCaretPosition.setCodeOffset(codeType.getMaxDigitsForByte() - 1);
                    } else {
                        defaultCodeAreaCaretPosition.setCodeOffset(0);
                    }
                    break;
                }
                break;
            case 7:
                long dataPosition3 = codeAreaCaretPosition.getDataPosition();
                long j2 = bytesPerRow * i;
                if (dataPosition3 >= j2) {
                    defaultCodeAreaCaretPosition.setDataPosition(dataPosition3 - j2);
                } else {
                    defaultCodeAreaCaretPosition.setDataPosition(dataPosition3 % ((long) bytesPerRow));
                }
                break;
            case 8:
                long dataPosition4 = codeAreaCaretPosition.getDataPosition();
                long j3 = bytesPerRow * i;
                if (dataPosition4 <= dataSize - j3) {
                    defaultCodeAreaCaretPosition.setDataPosition(dataPosition4 + j3);
                    break;
                } else {
                    long j4 = dataPosition4 % ((long) bytesPerRow);
                    long j5 = dataSize - (dataSize % ((long) bytesPerRow));
                    if (j5 != dataSize - j4) {
                        if (j5 > dataSize - j4) {
                            if (j5 > ((long) bytesPerRow)) {
                                defaultCodeAreaCaretPosition.setDataPosition(j4 + (j5 - ((long) bytesPerRow)));
                                break;
                            }
                        } else {
                            defaultCodeAreaCaretPosition.setDataPosition(j4 + j5);
                            break;
                        }
                    } else {
                        defaultCodeAreaCaretPosition.setDataPosition(dataSize);
                        defaultCodeAreaCaretPosition.setCodeOffset(0);
                        break;
                    }
                }
                break;
            case 9:
                defaultCodeAreaCaretPosition.setDataPosition(0);
                defaultCodeAreaCaretPosition.setCodeOffset(0);
                break;
            case 10:
                defaultCodeAreaCaretPosition.setDataPosition(dataSize);
                defaultCodeAreaCaretPosition.setCodeOffset(0);
                break;
            case 11:
                BasicCodeAreaSection basicCodeAreaSection = orElse == BasicCodeAreaSection.TEXT_PREVIEW ? BasicCodeAreaSection.CODE_MATRIX : BasicCodeAreaSection.TEXT_PREVIEW;
                if (basicCodeAreaSection == BasicCodeAreaSection.TEXT_PREVIEW) {
                    defaultCodeAreaCaretPosition.setCodeOffset(0);
                }
                defaultCodeAreaCaretPosition.setSection(basicCodeAreaSection);
                break;
            default:
                throw new IllegalStateException("Unexpected movement direction " + movementDirection.name());
        }
        return defaultCodeAreaCaretPosition;
    }

    public int computePositionByte(BasicCodeAreaStructure basicCodeAreaStructure, int i) {
        return i / (basicCodeAreaStructure.getCodeType().getMaxDigitsForByte() + 1);
    }

    public int computePositionX(int i, int i2) {
        return i * i2;
    }

    public long computeRowsPerDocument(BasicCodeAreaStructure basicCodeAreaStructure) {
        long dataSize = basicCodeAreaStructure.getDataSize();
        int bytesPerRow = basicCodeAreaStructure.getBytesPerRow();
        return ((long) (dataSize % ((long) bytesPerRow) > 0 ? 1 : 0)) + (dataSize / ((long) bytesPerRow));
    }
}
