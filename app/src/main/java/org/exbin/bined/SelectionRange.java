package org.exbin.bined;

public class SelectionRange {
    private long end;
    private long start;

    public SelectionRange() {
        this(0, 0);
    }

    public SelectionRange(long j, long j2) {
        if (j < 0 || j2 < 0) {
            throw new IllegalArgumentException("Selection (" + j + ", " + j2 + ") with negative range is not allowed");
        }
        this.start = j;
        this.end = j2;
    }

    public void clearSelection() {
        this.end = this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public long getFirst() {
        long j = this.start;
        long j2 = this.end;
        return Math.min(j, j2);
    }

    public long getLast() {
        long j = this.start;
        long j2 = this.end;
        return j < j2 ? j2 - 1 : j - 1;
    }

    public long getLength() {
        long j = this.start;
        long j2 = this.end;
        return j < j2 ? j2 - j : j - j2;
    }

    public long getStart() {
        return this.start;
    }

    public boolean isEmpty() {
        return this.start == this.end;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x001a A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isInSelection(long r8) {
        /*
            r7 = this;
            long r2 = r7.start
            long r4 = r7.end
            r0 = 1
            int r1 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r1 >= 0) goto L_0x0012
            int r1 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1))
            if (r1 < 0) goto L_0x001a
            int r1 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1))
            if (r1 >= 0) goto L_0x001a
        L_0x0011:
            return r0
        L_0x0012:
            int r1 = (r8 > r4 ? 1 : (r8 == r4 ? 0 : -1))
            if (r1 < 0) goto L_0x001a
            int r1 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1))
            if (r1 < 0) goto L_0x0011
        L_0x001a:
            r0 = 0
            goto L_0x0011
        */
        //throw new UnsupportedOperationException("Method not decompiled: org.exbin.bined.SelectionRange.isInSelection(long):boolean");
        return (this.start < this.end) ? ((r8 >= this.start && r8 < this.end)) : ((r8 >= this.end && r8 < this.start));
    }

    public void setEnd(long j) {
        if (this.start >= 0) {
            this.end = j;
            return;
        }
        throw new IllegalArgumentException("Selection with negative range end (" + j + ") is not allowed");
    }

    public void setSelection(long j, long j2) {
        setStart(j);
        setEnd(j2);
    }

    public void setSelection(SelectionRange selectionRange) {
        if (selectionRange == null) {
            this.end = 0;
            this.start = 0;
            return;
        }
        setStart(selectionRange.getStart());
        setEnd(selectionRange.getEnd());
    }

    public void setStart(long j) {
        if (j >= 0) {
            this.start = j;
            return;
        }
        throw new IllegalArgumentException("Selection with negative range start (" + j + ") is not allowed");
    }
}
