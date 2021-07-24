package org.exbin.bined.capability;

import java.nio.charset.Charset;

public interface CharsetCapable {
    Charset getCharset();

    void setCharset(Charset charset);
}