package org.exbin.auxiliary.paged_data;

import androidx.annotation.Nullable;

public class OutOfBoundsException extends RuntimeException {
    private static final long serialVersionUID = -778899357734733474L;

    public OutOfBoundsException() {
    }

    public OutOfBoundsException(@Nullable String str) {
        super(str);
    }

    public OutOfBoundsException(@Nullable String str, @Nullable Throwable th) {
        super(str, th);
    }

    public OutOfBoundsException(@Nullable Throwable th) {
        super(th);
    }
}
