package org.exbin.bined;

import java.util.Objects;
import java.util.Optional;

public class DefaultCodeAreaCaretPosition implements CodeAreaCaretPosition {
    private int codeOffset = 0;
    private long dataPosition = 0;
    private CodeAreaSection section = null;

    public DefaultCodeAreaCaretPosition() {
    }

    public DefaultCodeAreaCaretPosition(long j, int i, CodeAreaSection codeAreaSection) {
        this.dataPosition = j;
        this.codeOffset = i;
        this.section = codeAreaSection;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultCodeAreaCaretPosition defaultCodeAreaCaretPosition = (DefaultCodeAreaCaretPosition) obj;
        return Objects.equals(Long.valueOf(this.dataPosition), Long.valueOf(defaultCodeAreaCaretPosition.dataPosition)) && Objects.equals(Integer.valueOf(this.codeOffset), Integer.valueOf(defaultCodeAreaCaretPosition.codeOffset)) && Objects.equals(this.section, defaultCodeAreaCaretPosition.section);
    }

    @Override // org.exbin.bined.CodeAreaCaretPosition
    public int getCodeOffset() {
        return this.codeOffset;
    }

    @Override // org.exbin.bined.CodeAreaCaretPosition
    public long getDataPosition() {
        return this.dataPosition;
    }

    @Override // org.exbin.bined.CodeAreaCaretPosition
    public Optional<CodeAreaSection> getSection() {
        return Optional.ofNullable(this.section);
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.dataPosition), Integer.valueOf(this.codeOffset), this.section);
    }

    @Override // org.exbin.bined.CodeAreaCaretPosition
    public void reset() {
        this.dataPosition = 0;
        this.codeOffset = 0;
        this.section = null;
    }

    public void setCodeOffset(int i) {
        this.codeOffset = i;
    }

    public void setDataPosition(long j) {
        this.dataPosition = j;
    }

    public void setPosition(CodeAreaCaretPosition codeAreaCaretPosition) {
        this.dataPosition = codeAreaCaretPosition.getDataPosition();
        this.codeOffset = codeAreaCaretPosition.getCodeOffset();
        this.section = codeAreaCaretPosition.getSection().orElse(null);
    }

    public void setSection(CodeAreaSection codeAreaSection) {
        this.section = codeAreaSection;
    }
}
