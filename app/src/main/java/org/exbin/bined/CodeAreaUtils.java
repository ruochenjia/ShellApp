package org.exbin.bined;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import org.exbin.auxiliary.paged_data.EditableBinaryData;

public class CodeAreaUtils {
    private static final int CODE_BUFFER_LENGTH = 16;
    public static final char[] LOWER_HEX_CODES = "0123456789abcdef".toCharArray();
    public static final String MIME_CLIPBOARD_BINARY = "application/octet-stream";
    public static final char[] UPPER_HEX_CODES = "0123456789ABCDEF".toCharArray();

    /* access modifiers changed from: package-private */
    /* renamed from: org.exbin.bined.CodeAreaUtils$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$CodeType;

        static {
            int[] iArr = new int[CodeType.values().length];
            $SwitchMap$org$exbin$bined$CodeType = iArr;
            try {
                iArr[CodeType.BINARY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$exbin$bined$CodeType[CodeType.DECIMAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$exbin$bined$CodeType[CodeType.OCTAL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$exbin$bined$CodeType[CodeType.HEXADECIMAL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private CodeAreaUtils() {
    }

    public static void byteToCharsCode(byte b, CodeType codeType, char[] cArr, int i, CodeCharactersCase codeCharactersCase) {
        char[] cArr2 = codeCharactersCase == CodeCharactersCase.UPPER ? UPPER_HEX_CODES : LOWER_HEX_CODES;
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$CodeType[codeType.ordinal()]) {
            case 1:
                int i2 = 128;
                for (int i3 = 0; i3 < 8; i3++) {
                    cArr[i + i3] = cArr2[(b & i2) > 0 ? (char) 1 : 0];
                    i2 >>= 1;
                }
                return;
            case 2:
                int i4 = b & 255;
                cArr[i] = cArr2[i4 / 100];
                cArr[i + 1] = cArr2[(i4 / 10) % 10];
                cArr[i + 2] = cArr2[i4 % 10];
                return;
            case 3:
                int i5 = b & 255;
                cArr[i] = cArr2[i5 / 64];
                cArr[i + 1] = cArr2[(i5 / 8) & 7];
                cArr[i + 2] = cArr2[i5 % 8];
                return;
            case 4:
                cArr[i] = cArr2[(b >> 4) & 15];
                cArr[i + 1] = cArr2[b & 15];
                return;
            default:
                throw new IllegalStateException("Unexpected code type: " + codeType.name());
        }
    }

    public static void byteToHexChars(char[] cArr, byte b) {
        char[] cArr2 = UPPER_HEX_CODES;
        cArr[0] = cArr2[(b >> 4) & 15];
        cArr[1] = cArr2[b & 15];
    }

    public static char[] byteToHexChars(byte b) {
        char[] cArr = new char[2];
        byteToHexChars(cArr, b);
        return cArr;
    }

    public static byte[] characterToBytes(char c, Charset charset) {
        ByteBuffer encode = charset.encode(Character.toString(c));
        byte[] bArr = new byte[encode.remaining()];
        encode.get(bArr, 0, bArr.length);
        return bArr;
    }

    public static void insertHexStringIntoData(String str, EditableBinaryData editableBinaryData, CodeType codeType) {
        int i;
        int i2;
        int maxDigitsForByte = codeType.getMaxDigitsForByte();
        byte[] bArr = new byte[16];
        int i3 = 0;
        int i4 = 0;
        for (int i5 = 0; i5 < str.length(); i5++) {
            char charAt = str.charAt(i5);
            if ((charAt == ' ' || charAt == '\t') && i3 == i5) {
                i3++;
            } else if (charAt == ' ' || charAt == '\t' || charAt == ',' || charAt == ';' || charAt == ':') {
                byte stringCodeToByte = stringCodeToByte(str.substring(i3, i5), codeType);
                if (i4 < 16) {
                    bArr[i4] = stringCodeToByte;
                    i = i4 + 1;
                } else {
                    editableBinaryData.insert(editableBinaryData.getDataSize(), bArr, 0, i4);
                    i = 0;
                }
                i3 = i5 + 1;
                i4 = i;
            } else if (i5 == i3 + maxDigitsForByte) {
                byte stringCodeToByte2 = stringCodeToByte(str.substring(i3, i5), codeType);
                if (i4 < 16) {
                    bArr[i4] = stringCodeToByte2;
                    i2 = i4 + 1;
                } else {
                    editableBinaryData.insert(editableBinaryData.getDataSize(), bArr, 0, i4);
                    i2 = 0;
                }
                i4 = i2;
                i3 = i5;
            }
        }
        if (i3 < str.length()) {
            byte stringCodeToByte3 = stringCodeToByte(str.substring(i3), codeType);
            if (i4 < 16) {
                bArr[i4] = stringCodeToByte3;
                i4++;
            } else {
                editableBinaryData.insert(editableBinaryData.getDataSize(), bArr, 0, i4);
                i4 = 0;
            }
        }
        if (i4 > 0) {
            editableBinaryData.insert(editableBinaryData.getDataSize(), bArr, 0, i4);
        }
    }

    public static boolean isValidCodeKeyValue(char c, int i, CodeType codeType) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$CodeType[codeType.ordinal()]) {
            case 1:
                return c >= '0' && c <= '1';
            case 2:
                return i == 0 ? c >= '0' && c <= '2' : c >= '0' && c <= '9';
            case 3:
                return i == 0 ? c >= '0' && c <= '3' : c >= '0' && c <= '7';
            case 4:
                if (c >= '0' && c <= '9') {
                    return true;
                }
                if (c < 'a' || c > 'f') {
                    return c >= 'A' && c <= 'F';
                }
                return true;
            default:
                throw new IllegalStateException("Unexpected code type " + codeType.name());
        }
    }

    public static int longToBaseCode(char[] cArr, int i, long j, int i2, int i3, boolean z, CodeCharactersCase codeCharactersCase) {
        char[] cArr2 = codeCharactersCase == CodeCharactersCase.UPPER ? UPPER_HEX_CODES : LOWER_HEX_CODES;
        for (int i4 = i3 - 1; i4 >= 0; i4--) {
            cArr[i + i4] = cArr2[(int) (j % ((long) i2))];
            j /= i2;
            if (!z && j == 0) {
                return i4;
            }
        }
        return 0;
    }

    public static void longToHexChars(char[] cArr, long j, int i) {
        for (int i2 = i - 1; i2 >= 0; i2--) {
            cArr[i2] = UPPER_HEX_CODES[(int) (15 & j)];
            j >>= 4;
        }
    }

    public static char[] longToHexChars(long j, int i) {
        char[] cArr = new char[i];
        longToHexChars(cArr, j, i);
        return cArr;
    }

    public static <T> T requireNonNull(T t) {
        Objects.requireNonNull(t, "Field cannot be null");
        return t;
    }

    public static <T> T requireNonNull(T t, String str) {
        Objects.requireNonNull(t, str);
        return t;
    }

    public static void requireNonNull(Object... objArr) {
        for (Object obj : objArr) {
            Objects.requireNonNull(obj, "Field cannot be null");
        }
    }

    public static byte setCodeValue(byte b, int i, int i2, CodeType codeType) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$CodeType[codeType.ordinal()]) {
            case 1:
                return (byte) (((255 - (128 >> i2)) & b) | (i << (7 - i2)));
            case 2:
                int i3 = b & 255;
                switch (i2) {
                    case 0:
                        i3 = (i3 % 100) + (i * 100);
                        if (i3 > 255) {
                            i3 = 200;
                            break;
                        }
                        break;
                    case 1:
                        i3 = (i3 % 10) + ((i3 / 100) * 100) + (i * 10);
                        if (i3 > 255) {
                            i3 -= 200;
                            break;
                        }
                        break;
                    case 2:
                        i3 = ((i3 / 10) * 10) + i;
                        if (i3 > 255) {
                            i3 -= 200;
                            break;
                        }
                        break;
                }
                return (byte) i3;
            case 3:
                int i4 = b & 255;
                switch (i2) {
                    case 0:
                        i4 = (i4 % 64) + (i * 64);
                        break;
                    case 1:
                        i4 = (i4 % 8) + ((i4 / 64) * 64) + (i * 8);
                        break;
                    case 2:
                        i4 = ((i4 / 8) * 8) + i;
                        break;
                }
                return (byte) i4;
            case 4:
                return i2 == 1 ? (byte) ((b & 240) | i) : (byte) ((b & 15) | (i << 4));
            default:
                throw new IllegalStateException("Unexpected code type " + codeType.name());
        }
    }

    public static byte stringCodeToByte(String str, CodeType codeType) {
        int i;
        int i2 = 1;
        byte b = 0;
        if (str.length() <= codeType.getMaxDigitsForByte()) {
            switch (AnonymousClass1.$SwitchMap$org$exbin$bined$CodeType[codeType.ordinal()]) {
                case 1:
                    for (int length = str.length() - 1; length >= 0; length--) {
                        switch (str.charAt(length)) {
                            case '0':
                                break;
                            case '1':
                                b = (byte) (b | i2);
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid character " + str.charAt(length));
                        }
                        i2 <<= 1;
                    }
                    return b;
                case 2:
                    int i3 = 0;
                    for (int length2 = str.length() - 1; length2 >= 0; length2--) {
                        char charAt = str.charAt(length2);
                        if (charAt < '0' || charAt > '9') {
                            throw new IllegalArgumentException("Invalid character " + charAt);
                        }
                        i3 += (charAt - '0') * i2;
                        i2 *= 10;
                    }
                    if (i3 <= 255) {
                        return (byte) i3;
                    }
                    throw new IllegalArgumentException("Number is too big " + i3);
                case 3:
                    int i4 = 0;
                    for (int length3 = str.length() - 1; length3 >= 0; length3--) {
                        char charAt2 = str.charAt(length3);
                        if (charAt2 < '0' || charAt2 > '7') {
                            throw new IllegalArgumentException("Invalid character " + charAt2);
                        }
                        i4 += (charAt2 - '0') * i2;
                        i2 <<= 3;
                    }
                    if (i4 <= 255) {
                        return (byte) i4;
                    }
                    throw new IllegalArgumentException("Number is too big " + i4);
                case 4:
                    int i5 = 1;
                    for (int length4 = str.length() - 1; length4 >= 0; length4--) {
                        char charAt3 = str.charAt(length4);
                        if (charAt3 >= '0' && charAt3 <= '9') {
                            i = charAt3 - '0';
                        } else if (charAt3 >= 'a' && charAt3 <= 'f') {
                            i = (charAt3 + '\n') - 97;
                        } else if (charAt3 < 'A' || charAt3 > 'F') {
                            throw new IllegalArgumentException("Invalid character " + charAt3);
                        } else {
                            i = (charAt3 + '\n') - 65;
                        }
                        b = (byte) (b | (i * i5));
                        i5 <<= 4;
                    }
                    return b;
                default:
                    throw new IllegalStateException("Unexpected code type: " + codeType.name());
            }
        } else {
            throw new IllegalArgumentException("String code is too long");
        }
    }
}
