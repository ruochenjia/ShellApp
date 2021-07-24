package org.exbin.auxiliary.paged_data;

import java.io.IOException;

public interface SeekableStream {
    long getStreamSize();

    void seek(long j) throws IOException;
}
