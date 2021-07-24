package org.exbin.bined.color;

import java.util.Optional;

public interface CodeAreaColorType {
    Optional<CodeAreaColorGroup> getGroup();

    String getId();
}