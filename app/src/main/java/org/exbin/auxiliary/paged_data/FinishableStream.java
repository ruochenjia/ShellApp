package org.exbin.auxiliary.paged_data;

import java.io.IOException;

public interface FinishableStream {
    long finish() throws IOException;

    long getProcessedSize();
}
