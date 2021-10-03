package org.exbin.auxiliary.paged_data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PagedData implements EditableBinaryData {
    public static final int DEFAULT_PAGE_SIZE = 4096;
    public static final long MAX_DATA_SIZE = Long.MAX_VALUE;
    private final List<byte[]> data = new ArrayList();
    private int pageSize = DEFAULT_PAGE_SIZE;

    public PagedData() {
    }

    public PagedData(int i) {
        this.pageSize = i;
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void clear() {
        this.data.clear();
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public PagedData copy() {
        PagedData pagedData = new PagedData();
        pagedData.insert(0, this);
        return pagedData;
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public PagedData copy(long j, long j2) {
        PagedData pagedData = new PagedData();
        pagedData.insertUninitialized(0, j2);
        pagedData.replace(0, this, j, j2);
        return pagedData;
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public void copyToArray(long j, byte[] bArr, int i, int i2) {
        int i3 = i2;
        while (i3 > 0) {
            byte[] page = getPage((int) (j / ((long) this.pageSize)));
            int i4 = this.pageSize;
            int i5 = (int) (j % ((long) i4));
            int i6 = i4 - i5;
            if (i6 > i3) {
                i6 = i3;
            }
            try {
                System.arraycopy(page, i5, bArr, i, i6);
                i3 -= i6;
                i += i6;
                j += (long) i6;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new OutOfBoundsException(e);
            }
        }
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public void dispose() {
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() == obj.getClass()) {
            PagedData pagedData = (PagedData) obj;
            long dataSize = getDataSize();
            if (pagedData.getDataSize() != dataSize) {
                return false;
            }
            long j = 0;
            int i = 0;
            while (dataSize > 0) {
                int i2 = this.pageSize;
                int i3 = (int) (j % ((long) i2));
                int i4 = pagedData.pageSize;
                int i5 = (int) (j % ((long) i4));
                int i6 = dataSize > ((long) (i2 - i3)) ? i2 - i3 : (int) dataSize;
                int i7 = i6 > i4 - i5 ? i4 - i5 : i6;
                for (int i8 = 0; i8 < i7; i8++) {
                    if (this.data.get(i)[i3 + i8] != pagedData.data.get(0)[i5 + i8]) {
                        return false;
                    }
                }
                dataSize -= (long) i7;
                j = ((long) i7) + j;
                i++;
            }
            return true;
        } else if (!(obj instanceof BinaryData)) {
            return false;
        } else {
            BinaryData binaryData = (BinaryData) obj;
            long dataSize2 = getDataSize();
            if (binaryData.getDataSize() != dataSize2) {
                return false;
            }
            int i9 = this.pageSize;
            int i10 = dataSize2 > ((long) i9) ? i9 : (int) dataSize2;
            byte[] bArr = new byte[i10];
            int i11 = (int) dataSize2;
            int i12 = 0;
            int i13 = 0;
            while (i11 > 0) {
                int i14 = i11 > i10 ? i10 : i11;
                binaryData.copyToArray((long) i12, bArr, 0, i14);
                for (int i15 = 0; i15 < i14; i15++) {
                    if (this.data.get(i13)[i15] != bArr[i15]) {
                        return false;
                    }
                }
                i11 -= i14;
                i12 += i14;
                i13++;
            }
            return true;
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void fillData(long j, long j2) {
        fillData(j, j2, (byte) 0);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void fillData(long j, long j2, byte b) {
        if (j2 < 0) {
            throw new IllegalArgumentException("Length of filled block must be nonnegative");
        } else if (j < 0) {
            throw new IllegalArgumentException("Position of filler block must be nonnegative");
        } else if (j + j2 <= getDataSize()) {
            while (j2 > 0) {
                byte[] page = getPage((int) (j / ((long) this.pageSize)));
                int i = (int) (j % ((long) this.pageSize));
                int length = page.length - i;
                if (((long) length) > j2) {
                    length = (int) j2;
                }
                Arrays.fill(page, i, i + length, b);
                j2 -= (long) length;
                j += (long) length;
            }
        } else {
            throw new OutOfBoundsException("Filled block must be inside existing data");
        }
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public byte getByte(long j) {
        try {
            return getPage((int) (j / ((long) this.pageSize)))[(int) (j % ((long) this.pageSize))];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new OutOfBoundsException(e);
        }
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public InputStream getDataInputStream() {
        return new PagedDataInputStream(this);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public OutputStream getDataOutputStream() {
        return new PagedDataOutputStream(this);
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public long getDataSize() {
        int i = 0;
        int size = this.data.size() > 1 ? (this.data.size() - 1) * this.pageSize : 0;
        if (this.data.size() > 0) {
            List<byte[]> list = this.data;
            i = list.get(list.size() - 1).length;
        }
        return (long) (size + i);
    }

    public byte[] getPage(int i) {
        try {
            return this.data.get(i);
        } catch (IndexOutOfBoundsException e) {
            throw new OutOfBoundsException(e);
        }
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public int getPagesCount() {
        return this.data.size();
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(getDataSize()));
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public long insert(long j, InputStream inputStream, long j2) throws IOException {
        if (j2 <= MAX_DATA_SIZE - getDataSize()) {
            if (j > getDataSize()) {
                setDataSize(j);
            }
            int i = this.pageSize;
            int i2 = (int) (j % ((long) i));
            byte[] bArr = new byte[i];
            long j3 = 0;
            long j4 = j;
            while (true) {
                if (j2 != -1 && j2 <= 0) {
                    return j3;
                }
                int i3 = this.pageSize - i2;
                if (j2 >= 0 && j2 < ((long) i3)) {
                    i3 = (int) j2;
                }
                if (i2 <= 0 || i3 <= i2) {
                    i2 = i3;
                }
                int i4 = 0;
                while (i2 > 0) {
                    int read = inputStream.read(bArr, i4, i2);
                    if (read == -1) {
                        break;
                    }
                    i4 += read;
                    i2 -= read;
                }
                insert(j4, bArr, 0, i4);
                j4 += (long) i4;
                j2 -= (long) i4;
                j3 += (long) i4;
                i2 = 0;
            }
        } else {
            throw new DataOverflowException("Maximum array size overflow");
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, long j2) {
        insertUninitialized(j, j2);
        fillData(j, j2);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, BinaryData binaryData) {
        long dataSize = binaryData.getDataSize();
        insertUninitialized(j, dataSize);
        replace(j, binaryData, 0, dataSize);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, BinaryData binaryData, long j2, long j3) {
        insertUninitialized(j, j3);
        replace(j, binaryData, j2, j3);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, byte[] bArr) {
        insert(j, bArr, 0, bArr.length);
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insert(long j, byte[] bArr, int i, int i2) {
        if (i2 > 0) {
            insertUninitialized(j, (long) i2);
            int i3 = i2;
            while (i3 > 0) {
                byte[] page = getPage((int) (j / ((long) this.pageSize)));
                int i4 = this.pageSize;
                int i5 = (int) (j % ((long) i4));
                int i6 = i4 - i5;
                if (i6 > i3) {
                    i6 = i3;
                }
                try {
                    System.arraycopy(bArr, i, page, i5, i6);
                    i += i6;
                    i3 -= i6;
                    j += (long) i6;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new OutOfBoundsException(e);
                }
            }
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void insertUninitialized(long j, long j2) {
        byte[] bArr;
        if (j2 < 0) {
            throw new IllegalArgumentException("Length of inserted block must be nonnegative");
        } else if (j >= 0) {
            long dataSize = getDataSize();
            if (j > dataSize) {
                throw new OutOfBoundsException("Inserted block must be inside or directly after existing data");
            } else if (j2 > MAX_DATA_SIZE - getDataSize()) {
                throw new DataOverflowException("Maximum array size overflow");
            } else if (j >= dataSize) {
                setDataSize(j + j2);
            } else if (j2 > 0) {
                long j3 = dataSize + j2;
                setDataSize(j3);
                long j4 = dataSize - j;
                long j5 = j3 - j2;
                long j6 = j3;
                while (j4 > 0) {
                    byte[] page = getPage((int) (j5 / ((long) this.pageSize)));
                    int i = this.pageSize;
                    int i2 = (int) (j5 % ((long) i));
                    if (i2 == 0) {
                        page = getPage((int) ((j5 - 1) / ((long) i)));
                        i2 = page.length;
                    }
                    byte[] page2 = getPage((int) (j6 / ((long) this.pageSize)));
                    int i3 = this.pageSize;
                    int i4 = (int) (j6 % ((long) i3));
                    if (i4 == 0) {
                        byte[] page3 = getPage((int) ((j6 - 1) / ((long) i3)));
                        i4 = page3.length;
                        bArr = page3;
                    } else {
                        bArr = page2;
                    }
                    int i5 = i2 > i4 ? i4 : i2;
                    if (((long) i5) > j4) {
                        i5 = (int) j4;
                    }
                    System.arraycopy(page, i2 - i5, bArr, i4 - i5, i5);
                    j4 -= (long) i5;
                    j5 -= (long) i5;
                    j6 -= (long) i5;
                }
            }
        } else {
            throw new IllegalArgumentException("Position of inserted block must be nonnegative");
        }
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void loadFromStream(InputStream inputStream) throws IOException {
        this.data.clear();
        byte[] bArr = new byte[this.pageSize];
        int i = 0;
        while (true) {
            int read = inputStream.read(bArr, i, bArr.length - i);
            if (read <= 0) {
                break;
            } else if (read + i < this.pageSize) {
                i += read;
            } else {
                this.data.add(bArr);
                bArr = new byte[this.pageSize];
                i = 0;
            }
        }
        if (i > 0) {
            byte[] bArr2 = new byte[i];
            System.arraycopy(bArr, 0, bArr2, 0, i);
            this.data.add(bArr2);
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void remove(long j, long j2) {
        if (j2 < 0) {
            throw new IllegalArgumentException("Length of removed block must be nonnegative");
        } else if (j < 0) {
            throw new IllegalArgumentException("Position of removed block must be nonnegative");
        } else if (j + j2 > getDataSize()) {
            throw new OutOfBoundsException("Removed block must be inside existing data");
        } else if (j2 > 0) {
            replace(j, this, j + j2, (getDataSize() - j) - j2);
            setDataSize(getDataSize() - j2);
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
        } else if (!(binaryData instanceof PagedData)) {
            long j4 = j2;
            while (j3 > 0) {
                byte[] page = getPage((int) (j / ((long) this.pageSize)));
                int i = this.pageSize;
                int i2 = (int) (j % ((long) i));
                int i3 = i - i2;
                if (((long) i3) > j3) {
                    i3 = (int) j3;
                }
                binaryData.copyToArray(j4, page, i2, i3);
                j3 -= (long) i3;
                j += (long) i3;
                j4 += (long) i3;
            }
        } else if (binaryData != this || j2 > j || j2 + j3 < j) {
            while (j3 > 0) {
                byte[] page2 = getPage((int) (j / ((long) this.pageSize)));
                int i4 = (int) (j % ((long) this.pageSize));
                byte[] page3 = ((PagedData) binaryData).getPage((int) (j2 / ((long) ((PagedData) binaryData).getPageSize())));
                int pageSize2 = (int) (j2 % ((long) ((PagedData) binaryData).getPageSize()));
                int i5 = this.pageSize - i4;
                int pageSize3 = i5 > ((PagedData) binaryData).getPageSize() - pageSize2 ? ((PagedData) binaryData).getPageSize() - pageSize2 : i5;
                if (((long) pageSize3) > j3) {
                    pageSize3 = (int) j3;
                }
                try {
                    System.arraycopy(page3, pageSize2, page2, i4, pageSize3);
                    j3 -= (long) pageSize3;
                    j += (long) pageSize3;
                    j2 += (long) pageSize3;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new OutOfBoundsException(e);
                }
            }
        } else {
            long j5 = j + (j3 - 1);
            long j6 = (j3 - 1) + j2;
            while (j3 > 0) {
                byte[] page4 = getPage((int) (j5 / ((long) this.pageSize)));
                int i6 = ((int) (j5 % ((long) this.pageSize))) + 1;
                byte[] page5 = ((PagedData) binaryData).getPage((int) (j6 / ((long) ((PagedData) binaryData).getPageSize())));
                int pageSize4 = ((int) (j6 % ((long) ((PagedData) binaryData).getPageSize()))) + 1;
                int i7 = i6 > pageSize4 ? pageSize4 : i6;
                if (((long) i7) > j3) {
                    i7 = (int) j3;
                }
                System.arraycopy(page5, pageSize4 - i7, page4, i6 - i7, i7);
                j3 -= (long) i7;
                j5 -= (long) i7;
                j6 -= (long) i7;
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
            int i3 = i2;
            while (i3 > 0) {
                byte[] page = getPage((int) (j / ((long) this.pageSize)));
                int i4 = this.pageSize;
                int i5 = (int) (j % ((long) i4));
                int i6 = i4 - i5;
                if (i6 > i3) {
                    i6 = i3;
                }
                try {
                    System.arraycopy(bArr, i, page, i5, i6);
                    i3 -= i6;
                    j += (long) i6;
                    i += i6;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new OutOfBoundsException(e);
                }
            }
            return;
        }
        throw new OutOfBoundsException("Data can be replaced only inside or at the end");
    }

    @Override // org.exbin.auxiliary.paged_data.BinaryData
    public void saveToStream(OutputStream outputStream) throws IOException {
        for (byte[] bArr : this.data) {
            outputStream.write(bArr);
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void setByte(long j, byte b) {
        try {
            getPage((int) (j / ((long) this.pageSize)))[(int) (j % ((long) this.pageSize))] = (byte) b;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new OutOfBoundsException(e);
        }
    }

    @Override // org.exbin.auxiliary.paged_data.EditableBinaryData
    public void setDataSize(long j) {
        if (j >= 0) {
            long dataSize = getDataSize();
            if (j > dataSize) {
                int i = this.pageSize;
                int i2 = (int) (dataSize / ((long) i));
                int i3 = (int) (dataSize % ((long) i));
                long j2 = j - dataSize;
                if (i3 > 0) {
                    byte[] page = getPage(i2);
                    int i4 = this.pageSize;
                    if (((long) i3) + j2 <= ((long) i4)) {
                        i4 = ((int) j2) + i3;
                    }
                    byte[] bArr = new byte[i4];
                    System.arraycopy(page, 0, bArr, 0, i3);
                    setPage(i2, bArr);
                    j2 -= (long) (i4 - i3);
                }
                while (j2 > 0) {
                    int i5 = this.pageSize;
                    if (j2 <= ((long) i5)) {
                        i5 = (int) j2;
                    }
                    this.data.add(new byte[i5]);
                    j2 -= (long) i5;
                }
            } else if (j < dataSize) {
                int i6 = this.pageSize;
                int i7 = (int) (j / ((long) i6));
                int i8 = (int) (j % ((long) i6));
                if (i8 > 0) {
                    byte[] bArr2 = new byte[i8];
                    System.arraycopy(getPage(i7), 0, bArr2, 0, i8);
                    setPage(i7, bArr2);
                    i7++;
                }
                for (int size = this.data.size() - 1; size >= i7; size--) {
                    this.data.remove(size);
                }
            }
        } else {
            throw new InvalidParameterException("Size cannot be negative");
        }
    }

    public void setPage(int i, byte[] bArr) {
        try {
            this.data.set(i, bArr);
        } catch (IndexOutOfBoundsException e) {
            throw new OutOfBoundsException(e);
        }
    }
}
