package org.exbin.bined;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CharsetStreamTranslator extends InputStream {
    public static final int BYTE_BUFFER_SIZE = 16;
    private final CharBuffer charBuffer;
    private final CharsetDecoder decoder;
    private final CharsetEncoder encoder;
    private boolean endOfInput;
    private final ByteBuffer inputBuffer;
    private final ByteBuffer outputBuffer;
    private final InputStream source;

    public CharsetStreamTranslator(Charset charset, Charset charset2, InputStream inputStream) {
        this(charset, charset2, inputStream, 16);
    }

    public CharsetStreamTranslator(Charset charset, Charset charset2, InputStream inputStream, int i) {
        this.endOfInput = false;
        this.source = inputStream;
        CharsetDecoder newDecoder = charset.newDecoder();
        this.decoder = newDecoder;
        newDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        CharsetEncoder newEncoder = charset2.newEncoder();
        this.encoder = newEncoder;
        newEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        int maxCharsPerByte = (int) newDecoder.maxCharsPerByte();
        int maxBytesPerChar = (int) newEncoder.maxBytesPerChar();
        int maxOutputCharSize = maxBytesPerChar;
        if (maxBytesPerChar < 0) {
            maxOutputCharSize = 1;
        }
        this.charBuffer = CharBuffer.allocate(i * 8);
        ByteBuffer allocate = ByteBuffer.allocate(i);
        this.inputBuffer = allocate;
        allocate.limit(0);
        ByteBuffer allocate2 = ByteBuffer.allocate(maxOutputCharSize * i * 8);
        this.outputBuffer = allocate2;
        allocate2.limit(0);
    }

    private void loadFromInput() {
        byte[] array = this.inputBuffer.array();
        int remaining = this.inputBuffer.remaining();
        if (remaining > 0) {
            System.arraycopy(array, this.inputBuffer.position(), array, 0, remaining);
            this.inputBuffer.rewind();
            this.inputBuffer.limit(remaining);
            this.inputBuffer.position(remaining);
        } else {
            this.inputBuffer.rewind();
            this.inputBuffer.limit(0);
        }
        int position = this.inputBuffer.position();
        int capacity = this.inputBuffer.capacity() - this.inputBuffer.position();
        this.inputBuffer.limit(position + capacity);
        while (true) {
            if (capacity <= 0) {
                break;
            }
            try {
                int read = this.source.read(array, position, capacity);
                if (read < 0) {
                    this.inputBuffer.limit(position);
                    this.endOfInput = true;
                    break;
                }
                position += read;
                capacity -= read;
            } catch (IOException e) {
                Logger.getLogger(CharsetStreamTranslator.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        this.inputBuffer.rewind();
    }

    @Override // java.io.InputStream
    public int available() {
        int remaining = this.outputBuffer.remaining();
        return remaining > 0 ? remaining : this.endOfInput ? 1 : 0;
    }

    public void processNext() {
        this.charBuffer.rewind();
        CharBuffer charBuffer2 = this.charBuffer;
        charBuffer2.limit(charBuffer2.capacity());
        do {
            loadFromInput();
            if (this.inputBuffer.remaining() != 0) {
                this.decoder.reset();
                CoderResult decode = this.decoder.decode(this.inputBuffer, this.charBuffer, this.endOfInput);
                if (decode.isOverflow()) {
                    throw new UnsupportedOperationException("Not supported yet.");
                } else if (decode.isError()) {
                    if (this.charBuffer.position() == 0 && this.inputBuffer.remaining() > 0) {
                        ByteBuffer byteBuffer = this.inputBuffer;
                        byteBuffer.position(byteBuffer.position() + 1);
                    }
                } else if (decode.isUnmappable()) {
                    throw new IllegalStateException("Unmappable character should be handled automatically");
                } else if (decode.isMalformed()) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            } else {
                return;
            }
        } while (this.charBuffer.position() == 0);
        int position = this.charBuffer.position();
        this.charBuffer.rewind();
        this.charBuffer.limit(position);
        ByteBuffer byteBuffer2 = this.outputBuffer;
        byteBuffer2.limit(byteBuffer2.capacity());
        this.outputBuffer.clear();
        while (this.charBuffer.remaining() > 0) {
            this.encoder.reset();
            CoderResult encode = this.encoder.encode(this.charBuffer, this.outputBuffer, this.endOfInput);
            if (encode.isOverflow()) {
                throw new UnsupportedOperationException("Not supported yet.");
            } else if (encode.isUnmappable()) {
                throw new IllegalStateException("Unmappable character should be handled automatically");
            } else if (encode.isError()) {
                throw new UnsupportedOperationException("Not supported yet.");
            } else if (encode.isMalformed()) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        int position2 = this.outputBuffer.position();
        this.outputBuffer.rewind();
        this.outputBuffer.limit(position2);
    }

    @Override // java.io.InputStream
    public int read() {
        if (!(this.outputBuffer.remaining() > 0)) {
            if (this.endOfInput) {
                return -1;
            }
            processNext();
            if (this.outputBuffer.remaining() == 0) {
                return -1;
            }
        }
        return this.outputBuffer.get();
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr, int i, int i2) {
        int i3 = 0;
        int i4 = i2;
        while (i3 < i4) {
            int remaining = this.outputBuffer.remaining();
            if (remaining == 0) {
                if (!this.endOfInput) {
                    processNext();
                    int remaining2 = this.outputBuffer.remaining();
                    if (remaining2 == 0) {
                        if (i3 > 0) {
                            return i3;
                        }
                    }
                } else if (i3 > 0) {
                    return i3;
                }
                return -1;
            }
            if (i4 <= remaining) {
                remaining = i4;
            }
            this.outputBuffer.get(bArr, i, remaining);
            i += remaining;
            i4 -= remaining;
            i3 += remaining;
        }
        return i3;
    }
}
