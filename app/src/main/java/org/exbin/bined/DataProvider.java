package org.exbin.bined;

import org.exbin.auxiliary.paged_data.BinaryData;

public interface DataProvider {
    BinaryData getContentData();

    long getDataSize();
}