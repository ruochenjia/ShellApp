package org.exbin.bined.android.basic;

import java.util.Timer;
import java.util.TimerTask;

import org.exbin.bined.CodeAreaCaret;
import org.exbin.bined.CodeAreaCaretPosition;
import org.exbin.bined.CodeAreaSection;
import org.exbin.bined.CodeAreaUtils;
import org.exbin.bined.DefaultCodeAreaCaretPosition;
import org.exbin.bined.basic.BasicCodeAreaSection;

public class DefaultCodeAreaCaret implements CodeAreaCaret {
    private static final int DEFAULT_BLINK_RATE = 450;
    private static final int DOUBLE_CURSOR_WIDTH = 2;
    private int blinkRate = 0;
    private Timer blinkTimer = null;
    private final DefaultCodeAreaCaretPosition caretPosition = new DefaultCodeAreaCaretPosition();
    private final CodeArea codeArea;
    private boolean cursorVisible = true;
    private CursorRenderingMode renderingMode = CursorRenderingMode.NEGATIVE;

    /* access modifiers changed from: package-private */
    /* renamed from: org.exbin.bined.android.basic.DefaultCodeAreaCaret$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorShape;

        static {
            int[] iArr = new int[CursorShape.values().length];
            $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorShape = iArr;
            try {
                iArr[CursorShape.INSERT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorShape[CursorShape.OVERWRITE.ordinal()] = DefaultCodeAreaCaret.DOUBLE_CURSOR_WIDTH;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorShape[CursorShape.MIRROR.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: private */
    public class Blink extends TimerTask {
        private Blink() {
        }

        /* synthetic */ Blink(DefaultCodeAreaCaret defaultCodeAreaCaret, AnonymousClass1 r2) {
            this();
        }

        public void run() {
            DefaultCodeAreaCaret defaultCodeAreaCaret = DefaultCodeAreaCaret.this;
            defaultCodeAreaCaret.cursorVisible = !defaultCodeAreaCaret.cursorVisible;
            DefaultCodeAreaCaret.this.notifyCaredChanged();
        }
    }

    public enum CursorRenderingMode {
        PAINT,
        XOR,
        NEGATIVE
    }

    public enum CursorShape {
        INSERT,
        OVERWRITE,
        MIRROR
    }

    public DefaultCodeAreaCaret(CodeArea codeArea2) {
        this.codeArea = CodeAreaUtils.requireNonNull(codeArea2);
        privateSetBlinkRate(DEFAULT_BLINK_RATE);
    }

    public static int getCursorThickness(CursorShape cursorShape, int i, int i2) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$android$basic$DefaultCodeAreaCaret$CursorShape[cursorShape.ordinal()]) {
            case 1:
                return DOUBLE_CURSOR_WIDTH;
            case DOUBLE_CURSOR_WIDTH /*{ENCODED_INT: 2}*/:
            case 3:
                return i;
            default:
                return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyCaredChanged() {
        this.codeArea.notifyCaretChanged();
    }

    private void privateSetBlinkRate(int i) {
        synchronized (this) {
            if (i >= 0) {
                this.blinkRate = i;
                Timer timer = this.blinkTimer;
                if (timer != null) {
                    if (i == 0) {
                        timer.cancel();
                        this.blinkTimer = null;
                        this.cursorVisible = true;
                        notifyCaredChanged();
                    } else {
                        timer.cancel();
                        Timer timer2 = new Timer();
                        this.blinkTimer = timer2;
                        timer2.scheduleAtFixedRate(new Blink(this, null), (long) i, (long) i);
                    }
                } else if (i > 0) {
                    Timer timer3 = new Timer();
                    this.blinkTimer = timer3;
                    timer3.scheduleAtFixedRate(new Blink(this, null), (long) i, (long) i);
                }
            } else {
                throw new IllegalArgumentException("Blink rate cannot be negative");
            }
        }
    }

    public int getBlinkRate() {
        return this.blinkRate;
    }

    @Override // org.exbin.bined.CodeAreaCaret
    public CodeAreaCaretPosition getCaretPosition() {
        return this.caretPosition;
    }

    public int getCodeOffset() {
        return this.caretPosition.getCodeOffset();
    }

    public long getDataPosition() {
        return this.caretPosition.getDataPosition();
    }

    public CursorRenderingMode getRenderingMode() {
        return this.renderingMode;
    }

    @Override // org.exbin.bined.CodeAreaCaret
    public CodeAreaSection getSection() {
        return this.caretPosition.getSection().orElse(BasicCodeAreaSection.CODE_MATRIX);
    }

    public boolean isCursorVisible() {
        return this.cursorVisible;
    }

    public void resetBlink() {
        if (this.blinkTimer != null) {
            this.cursorVisible = true;
            resetTimer();
        }
    }

    public void resetTimer() {
        synchronized (this) {
            this.blinkTimer.cancel();
            Timer timer = new Timer();
            this.blinkTimer = timer;
            Blink blink = new Blink(this, null);
            int i = this.blinkRate;
            timer.scheduleAtFixedRate(blink, (long) i, (long) i);
        }
    }

    public void setBlinkRate(int i) {
        privateSetBlinkRate(i);
    }

    @Override // org.exbin.bined.CodeAreaCaret
    public void setCaretPosition(long j) {
        this.caretPosition.setDataPosition(j);
        this.caretPosition.setCodeOffset(0);
        resetBlink();
    }

    @Override // org.exbin.bined.CodeAreaCaret
    public void setCaretPosition(long j, int i) {
        this.caretPosition.setDataPosition(j);
        this.caretPosition.setCodeOffset(i);
        resetBlink();
    }

    public void setCaretPosition(long j, int i, CodeAreaSection codeAreaSection) {
        this.caretPosition.setDataPosition(j);
        this.caretPosition.setCodeOffset(i);
        this.caretPosition.setSection(codeAreaSection);
        resetBlink();
    }

    @Override // org.exbin.bined.CodeAreaCaret
    public void setCaretPosition(CodeAreaCaretPosition codeAreaCaretPosition) {
        if (codeAreaCaretPosition != null) {
            this.caretPosition.setPosition(codeAreaCaretPosition);
        } else {
            this.caretPosition.reset();
        }
        resetBlink();
    }

    public void setCodeOffset(int i) {
        this.caretPosition.setCodeOffset(i);
        resetBlink();
    }

    public void setDataPosition(long j) {
        this.caretPosition.setDataPosition(j);
        resetBlink();
    }

    public void setRenderingMode(CursorRenderingMode cursorRenderingMode) {
        CodeAreaUtils.requireNonNull(cursorRenderingMode);
        this.renderingMode = cursorRenderingMode;
        notifyCaredChanged();
    }

    public void setSection(CodeAreaSection codeAreaSection) {
        this.caretPosition.setSection(codeAreaSection);
        resetBlink();
    }
}
