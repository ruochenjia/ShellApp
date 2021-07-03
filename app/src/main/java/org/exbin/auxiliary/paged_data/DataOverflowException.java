package org.exbin.auxiliary.paged_data;

import androidx.annotation.Nullable;

public class DataOverflowException extends RuntimeException {
    private static final long serialVersionUID = 5527815029635931912L;

    public DataOverflowException() {
    }

    public DataOverflowException(@Nullable String str) {
        super(str);
    }

    public DataOverflowException(@Nullable String str, @Nullable Throwable th) {
        super(str, th);
    }

    public DataOverflowException(@Nullable Throwable th) {
        super(th);
    }
}
