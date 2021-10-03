package org.exbin.auxiliary.paged_data;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayDataOutputStream extends OutputStream implements SeekableStream, FinishableStream {
    private final ByteArrayEditableData data;
    private long position = 0;

    public ByteArrayDataOutputStream(ByteArrayEditableData byteArrayEditableData) {
        this.data = byteArrayEditableData;
    }

    @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
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

    @Override // org.exbin.auxiliary.paged_data.SeekableStream
    public void seek(long j) throws IOException {
        this.position = j;
    }

    @Override // java.io.OutputStream
    public void write(int i) throws IOException {
        long dataSize = this.data.getDataSize();
        if (this.position == dataSize) {
            this.data.setDataSize(dataSize + 1);
        }
        ByteArrayEditableData byteArrayEditableData = this.data;
        long j = this.position;
        this.position = 1 + j;
        byteArrayEditableData.setByte(j, (byte) i);
    }

    @Override // java.io.OutputStream
    public void write(byte[] bArr, int i, int i2) throws IOException {
        if (i2 != 0) {
            long dataSize = this.data.getDataSize();
            long j = this.position;
            if (((long) i2) + j > dataSize) {
                this.data.setDataSize(j + ((long) i2));
            }
            System.arraycopy(bArr, i, this.data.getData(), (int) this.position, i2);
            this.position += (long) i2;
        }
    }
}
