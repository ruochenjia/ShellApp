package org.exbin.auxiliary.paged_data;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class ByteArrayEditableData extends ByteArrayData implements EditableBinaryData {
    public static final int BUFFER_SIZE = 1024;
    public static final int MAX_ARRAY_LENGTH = 2147483642;

    public ByteArrayEditableData() {
        super();
    }

    public ByteArrayEditableData(byte[] bArr) {
        super(bArr);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void clear() {
        this.data = new byte[0];
    }

    @Override
    // org.exbin.auxiliary.paged_data.ByteArrayData, org.exbin.auxiliary.paged_data.BinaryData
    public BinaryData copy() {
        return new ByteArrayEditableData(Arrays.copyOf(this.data, this.data.length));
    }

    @Override
    // org.exbin.auxiliary.paged_data.ByteArrayData, org.exbin.auxiliary.paged_data.BinaryData
    public BinaryData copy(long j, long j2) {
        if (j + j2 <= ((long) this.data.length)) {
            return new ByteArrayEditableData(Arrays.copyOfRange(this.data, (int) j, (int) (j + j2)));
        }
        throw new OutOfBoundsException("Attempt to copy outside of data");
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void fillData(long j, long j2) {
        fillData(j, j2, (byte) 0);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void fillData(long j, long j2, byte b) {
        if (j2 > 0) {
            try {
                Arrays.fill(this.data, (int) j, (int) (j + j2), b);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new OutOfBoundsException(e);
            }
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public OutputStream getDataOutputStream() {
        return new ByteArrayDataOutputStream(this);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public long insert(long j, InputStream inputStream, long j2) {
        if (j2 <= ((long) (MAX_ARRAY_LENGTH - this.data.length))) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byte[] bArr = new byte[BUFFER_SIZE];
                while (inputStream.available() > 0 && j2 > 0) {
                    int length = bArr.length;
                    if (((long) length) > j2) {
                        length = (int) j2;
                    }
                    int read = inputStream.read(bArr, 0, length);
                    if (read <= 0) {
                        break;
                    }
                    byteArrayOutputStream.write(bArr, 0, read);
                    j2 -= read;
                }
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                if (((long) byteArray.length) + j > getDataSize()) {
                    setDataSize(((long) byteArray.length) + j);
                }
                replace(j, byteArray);
                long length2 = byteArray.length;
                byteArrayOutputStream.close();
                return length2;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            throw new DataOverflowException("Maximum array size overflow");
        }
        throw null;
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, long j2) {
        if (j > ((long) this.data.length)) {
            throw new OutOfBoundsException("Data can be inserted only inside or at the end");
        } else if (j2 > ((long) (MAX_ARRAY_LENGTH - this.data.length))) {
            throw new DataOverflowException("Maximum array size overflow");
        } else if (j2 > 0) {
            byte[] bArr = new byte[((int) (((long) this.data.length) + j2))];
            System.arraycopy(this.data, 0, bArr, 0, (int) j);
            System.arraycopy(this.data, (int) j, bArr, (int) (j + j2), (int) (((long) this.data.length) - j));
            this.data = bArr;
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, BinaryData binaryData) {
        if (j > ((long) this.data.length)) {
            throw new OutOfBoundsException("Data can be inserted only inside or at the end");
        } else if (binaryData.getDataSize() > ((long) (MAX_ARRAY_LENGTH - this.data.length))) {
            throw new DataOverflowException("Maximum array size overflow");
        } else if (binaryData instanceof ByteArrayData) {
            insert(j, ((ByteArrayData) binaryData).data);
        } else {
            insert(j, binaryData, 0, binaryData.getDataSize());
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, BinaryData binaryData, long j2, long j3) {
        if (j > ((long) this.data.length)) {
            throw new OutOfBoundsException("Data can be inserted only inside or at the end");
        } else if (j3 > ((long) (MAX_ARRAY_LENGTH - this.data.length))) {
            throw new DataOverflowException("Maximum array size overflow");
        } else if (binaryData instanceof ByteArrayData) {
            if (j2 > 2147483647L) {
                throw new OutOfBoundsException("Out of range");
            }
            insert(j, ((ByteArrayData) binaryData).data, (int) j2, (int) j3);
        } else if (j3 > 0) {
            byte[] bArr = new byte[((int) (((long) this.data.length) + j3))];
            System.arraycopy(this.data, 0, bArr, 0, (int) j);
            for (int i = 0; ((long) i) < j3; i++) {
                bArr[(int) (((long) i) + j)] = binaryData.getByte(((long) i) + j2);
            }
            System.arraycopy(this.data, (int) j, bArr, (int) (j + j3), (int) (((long) this.data.length) - j));
            this.data = bArr;
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, byte[] bArr) {
        if (j > ((long) this.data.length)) {
            throw new OutOfBoundsException("Data can be inserted only inside or at the end");
        } else if (bArr.length <= MAX_ARRAY_LENGTH - this.data.length) {
            int length = bArr.length;
            if (length > 0) {
                byte[] bArr2 = new byte[(this.data.length + length)];
                System.arraycopy(this.data, 0, bArr2, 0, (int) j);
                try {
                    System.arraycopy(bArr, 0, bArr2, (int) j, length);
                    System.arraycopy(this.data, (int) j, bArr2, (int) (((long) length) + j), (int) (((long) this.data.length) - j));
                    this.data = bArr2;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new OutOfBoundsException(e);
                }
            }
        } else {
            throw new DataOverflowException("Maximum array size overflow");
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, byte[] bArr, int i, int i2) {
        if (j > ((long) this.data.length)) {
            throw new OutOfBoundsException("Data can be inserted only inside or at the end");
        } else if (i2 > MAX_ARRAY_LENGTH - this.data.length) {
            throw new DataOverflowException("Maximum array size overflow");
        } else if (i2 > 0) {
            byte[] bArr2 = new byte[(this.data.length + i2)];
            System.arraycopy(this.data, 0, bArr2, 0, (int) j);
            try {
                System.arraycopy(bArr, i, bArr2, (int) j, i2);
                System.arraycopy(this.data, (int) j, bArr2, (int) (((long) i2) + j), (int) (((long) this.data.length) - j));
                this.data = bArr2;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new OutOfBoundsException(e);
            }
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insertUninitialized(long j, long j2) {
        if (j > ((long) this.data.length)) {
            throw new OutOfBoundsException("Data can be inserted only inside or at the end");
        } else if (j2 > ((long) (MAX_ARRAY_LENGTH - this.data.length))) {
            throw new DataOverflowException("Maximum array size overflow");
        } else if (j2 > 0) {
            byte[] bArr = new byte[((int) (((long) this.data.length) + j2))];
            System.arraycopy(this.data, 0, bArr, 0, (int) j);
            System.arraycopy(this.data, (int) j, bArr, (int) (j + j2), (int) (((long) this.data.length) - j));
            this.data = bArr;
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void loadFromStream(InputStream inputStream) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byte[] bArr = new byte[BUFFER_SIZE];
            while (inputStream.available() > 0) {
                int read = inputStream.read(bArr);
                if (read > 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                }
            }
            this.data = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw null;
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void remove(long j, long j2) {
        if (j + j2 > ((long) this.data.length)) {
            throw new OutOfBoundsException("Cannot remove from " + j + " with length " + j2);
        } else if (j2 > 0) {
            byte[] bArr = new byte[((int) (((long) this.data.length) - j2))];
            System.arraycopy(this.data, 0, bArr, 0, (int) j);
            System.arraycopy(this.data, (int) (j + j2), bArr, (int) j, (int) ((((long) this.data.length) - j) - j2));
            this.data = bArr;
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void replace(long j, BinaryData binaryData) {
        replace(j, binaryData, 0, binaryData.getDataSize());
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void replace(long j, BinaryData binaryData, long j2, long j3) {
        if (j + j3 > getDataSize()) {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        } else if (binaryData instanceof ByteArrayData) {
            replace(j, ((ByteArrayData) binaryData).data, (int) j2, (int) j3);
        } else {
            while (j3 > 0) {
                setByte(j, binaryData.getByte(j2));
                j++;
                j2++;
                j3--;
            }
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void replace(long j, byte[] bArr) {
        replace(j, bArr, 0, bArr.length);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void replace(long j, byte[] bArr, int i, int i2) {
        if (((long) i2) + j <= getDataSize()) {
            try {
                System.arraycopy(bArr, i, this.data, (int) j, i2);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new OutOfBoundsException(e);
            }
        } else {
            throw new OutOfBoundsException("Data can be replaced only inside or at the end");
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void setByte(long j, byte b) {
        try {
            this.data[(int) j] = b;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new OutOfBoundsException(e);
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void setDataSize(long j) {
        if (j < 0) {
            throw new InvalidParameterException("Size cannot be negative");
        } else if (((long) this.data.length) == j) {
        } else {
            if (j < ((long) this.data.length)) {
                this.data = Arrays.copyOfRange(this.data, 0, (int) j);
                return;
            }
            byte[] bArr = new byte[((int) j)];
            System.arraycopy(this.data, 0, bArr, 0, this.data.length);
            this.data = bArr;
        }
    }
}
