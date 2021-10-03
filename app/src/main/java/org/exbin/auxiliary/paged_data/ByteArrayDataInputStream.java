package org.exbin.auxiliary.paged_data;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayDataInputStream extends InputStream implements SeekableStream, FinishableStream {
    private final ByteArrayData data;
    private long position = 0;

    public ByteArrayDataInputStream(ByteArrayData byteArrayData) {
        this.data = byteArrayData;
    }

    @Override // java.io.InputStream
    public int available() throws IOException {
        return (int) (this.data.getDataSize() - this.position);
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
    public void close() throws IOException {
        finish();
    }

    @Override // org.exbin.auxiliary.paged_data.FinishableStream
    public long finish() throws IOException {
        long dataSize = this.data.getDataSize();
        this.position = dataSize;
        return dataSize;
    }

    @Override // org.exbin.auxiliary.paged_data.FinishableStream
    public long getProcessedSize() {
        return this.position;
    }

    @Override // org.exbin.auxiliary.paged_data.SeekableStream
    public long getStreamSize() {
        return this.data.getDataSize();
    }

    @Override // java.io.InputStream
    public int read() throws IOException {
        try {
            ByteArrayData byteArrayData = this.data;
            long j = this.position;
            this.position = 1 + j;
            return byteArrayData.getByte(j);
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr, int i, int i2) throws IOException {
        if (bArr.length == 0 || i2 == 0) {
            return 0;
        }
        byte[] data2 = this.data.getData();
        if (i + i2 > data2.length && (i2 = data2.length - i) <= 0) {
            return -1;
        }
        System.arraycopy(data2, (int) this.position, bArr, i, i2);
        this.position += (long) i2;
        return i2;
    }

    @Override // org.exbin.auxiliary.paged_data.SeekableStream
    public void seek(long j) throws IOException {
        this.position = j;
    }
}
