package org.exbin.auxiliary.paged_data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BinaryData {
    BinaryData copy();

    BinaryData copy(long j, long j2);

    void copyToArray(long j, byte[] bArr, int i, int i2);

    void dispose();

    byte getByte(long j);

    InputStream getDataInputStream();

    long getDataSize();

    boolean isEmpty();

    void saveToStream(OutputStream outputStream) throws IOException;
}
