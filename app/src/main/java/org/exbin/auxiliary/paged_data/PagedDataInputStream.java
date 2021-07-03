package org.exbin.auxiliary.paged_data;

import java.io.IOException;
import java.io.InputStream;

public class PagedDataInputStream extends InputStream implements SeekableStream, FinishableStream {
    private final PagedData data;
    private long position = 0;

    public PagedDataInputStream(PagedData pagedData) {
        this.data = pagedData;
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
            PagedData pagedData = this.data;
            long j = this.position;
            this.position = 1 + j;
            return pagedData.getByte(j);
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override // java.io.InputStream
    public int read(byte[] bArr, int i, int i2) throws IOException {
        if (bArr.length == 0 || i2 == 0) {
            return 0;
        }
        int i3 = i;
        int i4 = i2;
        while (i4 > 0) {
            int pageSize = (int) (this.position / ((long) this.data.getPageSize()));
            if (pageSize < this.data.getPagesCount()) {
                byte[] page = this.data.getPage(pageSize);
                int pageSize2 = (int) (this.position % ((long) this.data.getPageSize()));
                int length = page.length - pageSize2;
                if (length > i4) {
                    length = i4;
                }
                if (length != 0) {
                    System.arraycopy(page, pageSize2, bArr, i3, length);
                    i4 -= length;
                    this.position += (long) length;
                    i3 += length;
                } else if (i2 != i4) {
                    return i2 - i4;
                } else {
                    return -1;
                }
            } else if (i3 != i) {
                return i3 - i;
            } else {
                return -1;
            }
        }
        return i2;
    }

    @Override // org.exbin.auxiliary.paged_data.SeekableStream
    public void seek(long j) throws IOException {
        this.position = j;
    }
}
