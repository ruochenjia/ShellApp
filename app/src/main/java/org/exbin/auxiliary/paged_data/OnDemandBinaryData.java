package org.exbin.auxiliary.paged_data;

import java.util.concurrent.Future;

public interface OnDemandBinaryData {
    boolean isAvailable(long j, long j2);

    Future retrieveData(long j, long j2);
}
