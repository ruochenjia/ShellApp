package org.exbin.auxiliary.paged_data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface EditableBinaryData extends BinaryData {
    void clear();

    void fillData(long j, long j2);

    void fillData(long j, long j2, byte b);

    OutputStream getDataOutputStream();

    long insert(long j, InputStream inputStream, long j2) throws IOException;

    void insert(long j, long j2);

    void insert(long j, BinaryData binaryData);

    void insert(long j, BinaryData binaryData, long j2, long j3);

    void insert(long j, byte[] bArr);

    void insert(long j, byte[] bArr, int i, int i2);

    void insertUninitialized(long j, long j2);

    void loadFromStream(InputStream inputStream) throws IOException;

    void remove(long j, long j2);

    void replace(long j, BinaryData binaryData);

    void replace(long j, BinaryData binaryData, long j2, long j3);

    void replace(long j, byte[] bArr);

    void replace(long j, byte[] bArr, int i, int i2);

    void setByte(long j, byte b);

    void setDataSize(long j);
}
