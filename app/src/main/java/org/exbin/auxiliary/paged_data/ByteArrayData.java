package org.exbin.auxiliary.paged_data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ByteArrayData implements BinaryData {
    protected byte[] data;

    public ByteArrayData() {
        this(new byte[0]);
    }

    public ByteArrayData(@NonNull byte[] data) {
        this.data = data;
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public BinaryData copy() {
        byte[] bArr = this.data;
        return new ByteArrayData(Arrays.copyOf(bArr, bArr.length));
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public BinaryData copy(long j, long j2) {
        byte[] bArr = this.data;
        if (j + j2 <= ((long) bArr.length)) {
            return new ByteArrayData(Arrays.copyOfRange(bArr, (int) j, (int) (j + j2)));
        }
        throw new OutOfBoundsException("Attemt to copy outside of data");
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public void copyToArray(long j, byte[] bArr, int i, int i2) {
        try {
            System.arraycopy(this.data, (int) j, bArr, i, i2);
        } catch (IndexOutOfBoundsException e) {
            throw new OutOfBoundsException(e);
        }
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public void dispose() {
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() == obj.getClass()) {
            return Arrays.equals(this.data, ((ByteArrayData) obj).data);
        }
        if (!(obj instanceof BinaryData)) {
            return false;
        }
        BinaryData binaryData = (BinaryData) obj;
        long dataSize = getDataSize();
        if (binaryData.getDataSize() != dataSize) {
            return false;
        }
        int i = dataSize > 1024 ? ByteArrayEditableData.BUFFER_SIZE : (int) dataSize;
        byte[] bArr = new byte[i];
        int i2 = (int) dataSize;
        int i3 = 0;
        while (i2 > 0) {
            int i4 = Math.min(i2, i);
            binaryData.copyToArray((long) i3, bArr, 0, i4);
            for (int i5 = 0; i5 < i4; i5++) {
                if (this.data[i3 + i5] != bArr[i5]) {
                    return false;
                }
            }
            i2 -= i4;
            i3 += i4;
        }
        return true;
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public byte getByte(long j) {
        try {
            return this.data[(int) j];
        } catch (IndexOutOfBoundsException e) {
            throw new OutOfBoundsException(e);
        }
    }

    public byte[] getData() {
        return this.data;
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public InputStream getDataInputStream() {
        return new ByteArrayDataInputStream(this);
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public long getDataSize() {
        return (long) this.data.length;
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public boolean isEmpty() {
        return this.data.length == 0;
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public void saveToStream(OutputStream outputStream) throws IOException {
        outputStream.write(this.data);
    }

    public void setData(byte[] bArr) {
        if (bArr != null) {
            this.data = bArr;
            return;
        }
        throw new NullPointerException();
    }
}
