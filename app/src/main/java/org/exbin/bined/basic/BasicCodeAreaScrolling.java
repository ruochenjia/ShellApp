package org.exbin.bined.basic;

import java.util.Optional;
import org.exbin.bined.DataProvider;
import org.exbin.bined.ScrollBarVisibility;
import org.exbin.bined.capability.BasicScrollingCapable;

public class BasicCodeAreaScrolling {
    private int horizontalExtentDifference;
    private ScrollBarVisibility horizontalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private HorizontalScrollUnit horizontalScrollUnit = HorizontalScrollUnit.PIXEL;
    private int horizontalScrollbarHeight;
    private final CodeAreaScrollPosition maximumScrollPosition = new CodeAreaScrollPosition();
    private ScrollBarVerticalScale scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
    private final CodeAreaScrollPosition scrollPosition = new CodeAreaScrollPosition();
    private final ScrollViewDimension scrollViewDimension = new ScrollViewDimension(0, 0);
    private int verticalExtentDifference;
    private ScrollBarVisibility verticalScrollBarVisibility = ScrollBarVisibility.IF_NEEDED;
    private VerticalScrollUnit verticalScrollUnit = VerticalScrollUnit.ROW;
    private int verticalScrollbarWidth;

    /* access modifiers changed from: package-private */
    /* renamed from: org.exbin.bined.basic.BasicCodeAreaScrolling$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit;
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$basic$ScrollingDirection;
        static final /* synthetic */ int[] $SwitchMap$org$exbin$bined$basic$VerticalScrollUnit;

        static {
            int[] iArr = new int[ScrollingDirection.values().length];
            $SwitchMap$org$exbin$bined$basic$ScrollingDirection = iArr;
            try {
                iArr[ScrollingDirection.UP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$ScrollingDirection[ScrollingDirection.DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$ScrollingDirection[ScrollingDirection.LEFT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$ScrollingDirection[ScrollingDirection.RIGHT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$ScrollingDirection[ScrollingDirection.PAGE_UP.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$ScrollingDirection[ScrollingDirection.PAGE_DOWN.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            int[] iArr2 = new int[VerticalScrollUnit.values().length];
            $SwitchMap$org$exbin$bined$basic$VerticalScrollUnit = iArr2;
            try {
                iArr2[VerticalScrollUnit.PIXEL.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$VerticalScrollUnit[VerticalScrollUnit.ROW.ordinal()] = 2;
            } catch (NoSuchFieldError e8) {
            }
            int[] iArr3 = new int[HorizontalScrollUnit.values().length];
            $SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit = iArr3;
            try {
                iArr3[HorizontalScrollUnit.PIXEL.ordinal()] = 1;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit[HorizontalScrollUnit.CHARACTER.ordinal()] = 2;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    public static final class ScrollViewDimension {
        private int height;
        private int width;

        public ScrollViewDimension(int i, int i2) {
            this.width = i;
            this.height = i2;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }
    }

    private PositionScrollVisibility checkBottomScrollVisibility(long j, int i, int i2, int i3) {
        int rowOffset = this.scrollPosition.getRowOffset() + i2;
        long rowPosition = this.scrollPosition.getRowPosition() + ((long) i);
        if (i2 > 0) {
            rowPosition--;
        }
        if (rowOffset >= i3) {
            rowPosition++;
        }
        return j <= rowPosition ? PositionScrollVisibility.VISIBLE : (rowOffset <= 0 || rowOffset == i3 || j != rowPosition + 1) ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.PARTIAL;
    }

    private PositionScrollVisibility checkLeftScrollVisibility(int i, int i2) {
        int charPosition = this.scrollPosition.getCharPosition();
        return this.horizontalScrollUnit != HorizontalScrollUnit.PIXEL ? i < charPosition ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE : (i > charPosition || (i == charPosition && this.scrollPosition.getCharOffset() == 0)) ? PositionScrollVisibility.VISIBLE : (i != charPosition || this.scrollPosition.getCharOffset() <= 0) ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.PARTIAL;
    }

    private PositionScrollVisibility checkRightScrollVisibility(int i, int i2, int i3, int i4) {
        int charOffset = this.scrollPosition.getCharOffset() + i3;
        int charPosition = this.scrollPosition.getCharPosition() + i2;
        if (i3 > 0) {
            charPosition--;
        }
        if (charOffset >= i4) {
            charPosition++;
        }
        return i <= charPosition ? PositionScrollVisibility.VISIBLE : (charOffset <= 0 || charOffset == i4 || i != charPosition + 1) ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.PARTIAL;
    }

    private PositionScrollVisibility checkTopScrollVisibility(long j) {
        return this.verticalScrollUnit == VerticalScrollUnit.ROW ? j < this.scrollPosition.getRowPosition() ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.VISIBLE : (j > this.scrollPosition.getRowPosition() || (j == this.scrollPosition.getRowPosition() && this.scrollPosition.getRowOffset() == 0)) ? PositionScrollVisibility.VISIBLE : (j != this.scrollPosition.getRowPosition() || this.scrollPosition.getRowOffset() <= 0) ? PositionScrollVisibility.NOT_VISIBLE : PositionScrollVisibility.PARTIAL;
    }

    private boolean computeFitsHorizontally(int i, int i2) {
        return i2 <= i;
    }

    private boolean computeFitsVertically(int i, long j, int i2) {
        boolean z = true;
        if (j > ((long) (((i + i2) - 1) / i2))) {
            return false;
        }
        if (((long) i2) * j > ((long) i)) {
            z = false;
        }
        return z;
    }

    private CodeAreaScrollPosition createScrollPosition(int i, int i2, int i3) {
        CodeAreaScrollPosition codeAreaScrollPosition = new CodeAreaScrollPosition();
        setHorizontalScrollPosition(codeAreaScrollPosition, i, i2, i3);
        return codeAreaScrollPosition;
    }

    private int recomputeScrollViewHeight(int i, int i2, long j) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$VerticalScrollUnit[this.verticalScrollUnit.ordinal()]) {
            case 1:
                if (j > ((long) (Integer.MAX_VALUE / i2))) {
                    this.scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                    this.verticalExtentDifference = 0;
                    return Integer.MAX_VALUE;
                }
                this.scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                int i3 = ((int) (((long) i2) * j)) - i;
                this.verticalExtentDifference = 0;
                return i3;
            case 2:
                if (j > ((long) (Integer.MAX_VALUE - i))) {
                    this.scrollBarVerticalScale = ScrollBarVerticalScale.SCALED;
                    this.verticalExtentDifference = 0;
                    return Integer.MAX_VALUE;
                }
                this.scrollBarVerticalScale = ScrollBarVerticalScale.NORMAL;
                int i4 = i / i2;
                int i5 = (int) (((long) i) + (j - ((long) i4)));
                this.verticalExtentDifference = i - i4;
                return i5;
            default:
                throw new IllegalStateException("Unexpected scrolling unit " + this.verticalScrollUnit);
        }
    }

    private int recomputeScrollViewWidth(int i, int i2, int i3, int i4) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit[this.horizontalScrollUnit.ordinal()]) {
            case 1:
                this.horizontalExtentDifference = 0;
                return i3;
            case 2:
                int i5 = i / i2;
                int i6 = (i4 - i5) + i;
                this.horizontalExtentDifference = i - i5;
                return i6;
            default:
                throw new IllegalStateException("Unexpected scrolling unit " + this.horizontalScrollUnit);
        }
    }

    private void setHorizontalScrollPosition(CodeAreaScrollPosition codeAreaScrollPosition, int i, int i2, int i3) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit[this.horizontalScrollUnit.ordinal()]) {
            case 1:
                if (i2 > i3) {
                    i2 %= i3;
                }
                codeAreaScrollPosition.setCharPosition(i);
                codeAreaScrollPosition.setCharOffset(i2);
                return;
            case 2:
                codeAreaScrollPosition.setCharPosition(i);
                codeAreaScrollPosition.setCharOffset(0);
                return;
            default:
                throw new IllegalStateException("Unexpected horizontal scrolling unit " + this.horizontalScrollUnit);
        }
    }

    public Optional<CodeAreaScrollPosition> computeCenterOnScrollPosition(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int charPosition;
        CodeAreaScrollPosition codeAreaScrollPosition = new CodeAreaScrollPosition();
        codeAreaScrollPosition.setScrollPosition(this.scrollPosition);
        long j2 = j - ((long) (i3 / 2));
        int i9 = ((((i3 & 1) == 0 ? i8 : 0) + (i3 * i8)) - i6) / 2;
        if (this.verticalScrollUnit == VerticalScrollUnit.ROW) {
            i9 = 0;
        } else if (i9 <= 0) {
            i9 = 0;
        }
        if (j2 < 0) {
            j2 = 0;
            i9 = 0;
        } else if (j2 > this.maximumScrollPosition.getRowPosition() || (j2 == this.maximumScrollPosition.getRowPosition() && i9 > this.maximumScrollPosition.getRowOffset())) {
            j2 = this.maximumScrollPosition.getRowPosition();
            i9 = this.maximumScrollPosition.getRowOffset();
        }
        codeAreaScrollPosition.setRowPosition(j2);
        codeAreaScrollPosition.setRowOffset(i9);
        int i10 = i - (i4 / 2);
        if ((i4 & 1) != 0) {
            i8 = 0;
        }
        int i11 = (((i4 * i7) + i8) - i5) / 2;
        if (this.horizontalScrollUnit == HorizontalScrollUnit.CHARACTER) {
            i11 = 0;
        } else if (i11 <= 0) {
            i11 = 0;
        }
        if (i10 < 0) {
            charPosition = 0;
            i11 = 0;
        } else if (i10 > this.maximumScrollPosition.getCharPosition() || (i10 == this.maximumScrollPosition.getCharPosition() && i11 > this.maximumScrollPosition.getCharOffset())) {
            charPosition = this.maximumScrollPosition.getCharPosition();
            i11 = this.maximumScrollPosition.getCharOffset();
        } else {
            charPosition = i10;
        }
        codeAreaScrollPosition.setCharPosition(charPosition);
        codeAreaScrollPosition.setCharOffset(i11);
        return Optional.of(codeAreaScrollPosition);
    }

    public PositionScrollVisibility computePositionScrollVisibility(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        PositionScrollVisibility checkTopScrollVisibility = checkTopScrollVisibility(j);
        if (checkTopScrollVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        boolean z = checkTopScrollVisibility == PositionScrollVisibility.PARTIAL;
        PositionScrollVisibility checkBottomScrollVisibility = checkBottomScrollVisibility(j, i3, i6, i8);
        if (checkBottomScrollVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        boolean z2 = checkBottomScrollVisibility == PositionScrollVisibility.PARTIAL;
        PositionScrollVisibility checkLeftScrollVisibility = checkLeftScrollVisibility(i, i7);
        if (checkLeftScrollVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        boolean z3 = checkLeftScrollVisibility == PositionScrollVisibility.PARTIAL;
        PositionScrollVisibility checkRightScrollVisibility = checkRightScrollVisibility(i, i4, i5, i7);
        if (checkRightScrollVisibility == PositionScrollVisibility.NOT_VISIBLE) {
            return PositionScrollVisibility.NOT_VISIBLE;
        }
        return (((z | false) | z2) | z3) | (checkRightScrollVisibility == PositionScrollVisibility.PARTIAL) ? PositionScrollVisibility.PARTIAL : PositionScrollVisibility.VISIBLE;
    }

    public Optional<CodeAreaScrollPosition> computeRevealScrollPosition(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        CodeAreaScrollPosition codeAreaScrollPosition = new CodeAreaScrollPosition();
        codeAreaScrollPosition.setScrollPosition(this.scrollPosition);
        boolean z = false;
        if (checkBottomScrollVisibility(j, i3, i6, i8) != PositionScrollVisibility.VISIBLE) {
            int i9 = this.verticalScrollUnit == VerticalScrollUnit.ROW ? 0 : i3 == 0 ? 0 : i8 - i6;
            long j2 = j - ((long) i3);
            if (this.verticalScrollUnit == VerticalScrollUnit.ROW && i6 > 0) {
                j2++;
            }
            codeAreaScrollPosition.setRowPosition(j2);
            codeAreaScrollPosition.setRowOffset(i9);
            z = true;
        }
        if (checkTopScrollVisibility(j) != PositionScrollVisibility.VISIBLE) {
            codeAreaScrollPosition.setRowPosition(j);
            codeAreaScrollPosition.setRowOffset(0);
            z = true;
        }
        if (checkRightScrollVisibility(i, i4, i5, i7) != PositionScrollVisibility.VISIBLE) {
            setHorizontalScrollPosition(codeAreaScrollPosition, i - i4, this.horizontalScrollUnit != HorizontalScrollUnit.PIXEL ? 0 : i4 < 1 ? 0 : i7 - (i5 % i7), i7);
            z = true;
        }
        if (checkLeftScrollVisibility(i, i7) != PositionScrollVisibility.VISIBLE) {
            setHorizontalScrollPosition(codeAreaScrollPosition, i, 0, i7);
            z = true;
        }
        return z ? Optional.of(codeAreaScrollPosition) : Optional.empty();
    }

    public CodeAreaScrollPosition computeScrolling(CodeAreaScrollPosition codeAreaScrollPosition, ScrollingDirection scrollingDirection, int i, long j) {
        CodeAreaScrollPosition codeAreaScrollPosition2 = new CodeAreaScrollPosition();
        codeAreaScrollPosition2.setScrollPosition(codeAreaScrollPosition);
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$ScrollingDirection[scrollingDirection.ordinal()]) {
            case 1:
                if (codeAreaScrollPosition.getRowPosition() != 0) {
                    codeAreaScrollPosition2.setRowPosition(codeAreaScrollPosition.getRowPosition() - 1);
                } else {
                    codeAreaScrollPosition2.setRowOffset(0);
                }
                break;
            case 2:
                if (this.maximumScrollPosition.isRowPositionGreaterThan(codeAreaScrollPosition)) {
                    codeAreaScrollPosition2.setRowPosition(codeAreaScrollPosition.getRowPosition() + 1);
                    break;
                }
                break;
            case 3:
                if (codeAreaScrollPosition.getCharPosition() != 0) {
                    codeAreaScrollPosition2.setCharPosition(codeAreaScrollPosition.getCharPosition() - 1);
                } else {
                    codeAreaScrollPosition2.setCharOffset(0);
                }
                break;
            case 4:
                if (this.maximumScrollPosition.isCharPositionGreaterThan(codeAreaScrollPosition)) {
                    codeAreaScrollPosition2.setCharPosition(codeAreaScrollPosition.getCharPosition() + 1);
                    break;
                }
                break;
            case 5:
                if (codeAreaScrollPosition.getRowPosition() >= ((long) i)) {
                    codeAreaScrollPosition2.setRowPosition(codeAreaScrollPosition.getRowPosition() - ((long) i));
                } else {
                    codeAreaScrollPosition2.setRowPosition(0);
                    codeAreaScrollPosition2.setRowOffset(0);
                }
                break;
            case 6:
                if (codeAreaScrollPosition.getRowPosition() > j - ((long) (i * 2))) {
                    if (j <= ((long) i)) {
                        codeAreaScrollPosition2.setRowPosition(0);
                    } else {
                        codeAreaScrollPosition2.setRowPosition(j - ((long) i));
                    }
                } else {
                    codeAreaScrollPosition2.setRowPosition(codeAreaScrollPosition.getRowPosition() + ((long) i));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected scrolling direction type: " + scrollingDirection.name());
        }
        return codeAreaScrollPosition2;
    }

    public ScrollViewDimension computeViewDimension(int i, int i2, BasicCodeAreaLayout basicCodeAreaLayout, BasicCodeAreaStructure basicCodeAreaStructure, int i3, int i4) {
        int charactersPerRow = basicCodeAreaStructure.getCharactersPerRow();
        int computePositionX = basicCodeAreaLayout.computePositionX(charactersPerRow, i3);
        boolean computeFitsHorizontally = computeFitsHorizontally(i, computePositionX);
        long rowsPerDocument = basicCodeAreaStructure.getRowsPerDocument();
        boolean computeFitsVertically = computeFitsVertically(i2, rowsPerDocument, i4);
        if (!computeFitsVertically) {
            computeFitsHorizontally = computeFitsHorizontally(i - this.verticalScrollbarWidth, computePositionX);
        }
        if (!computeFitsHorizontally) {
            computeFitsVertically = computeFitsVertically(i2 - this.horizontalScrollbarHeight, rowsPerDocument, i4);
        }
        if (computeFitsHorizontally) {
            this.scrollViewDimension.width = computePositionX;
            this.verticalExtentDifference = 0;
        } else {
            this.scrollViewDimension.width = recomputeScrollViewWidth(i, i3, computePositionX, charactersPerRow);
        }
        if (computeFitsVertically) {
            this.scrollViewDimension.height = (int) (((long) i4) * rowsPerDocument);
            this.horizontalExtentDifference = 0;
        } else {
            this.scrollViewDimension.height = recomputeScrollViewHeight(i2, i4, rowsPerDocument);
        }
        return this.scrollViewDimension;
    }

    public int getHorizontalExtentDifference() {
        return this.horizontalExtentDifference;
    }

    public ScrollBarVisibility getHorizontalScrollBarVisibility() {
        return this.horizontalScrollBarVisibility;
    }

    public HorizontalScrollUnit getHorizontalScrollUnit() {
        return this.horizontalScrollUnit;
    }

    public int getHorizontalScrollValue(int i) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit[this.horizontalScrollUnit.ordinal()]) {
            case 1:
                return (this.scrollPosition.getCharPosition() * i) + this.scrollPosition.getCharOffset();
            case 2:
                return this.scrollPosition.getCharPosition() * i;
            default:
                throw new IllegalStateException("Unexpected horizontal scroll unit: " + this.horizontalScrollUnit.name());
        }
    }

    public int getHorizontalScrollX(int i) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit[this.horizontalScrollUnit.ordinal()]) {
            case 1:
                return (this.scrollPosition.getCharPosition() * i) + this.scrollPosition.getCharOffset();
            case 2:
                return this.scrollPosition.getCharPosition() * i;
            default:
                throw new IllegalStateException("Unexpected horizontal scrolling unit " + this.horizontalScrollUnit);
        }
    }

    public CodeAreaScrollPosition getMaximumScrollPosition() {
        return this.maximumScrollPosition;
    }

    public ScrollBarVerticalScale getScrollBarVerticalScale() {
        return this.scrollBarVerticalScale;
    }

    public CodeAreaScrollPosition getScrollPosition() {
        return this.scrollPosition;
    }

    public int getVerticalExtentDifference() {
        return this.verticalExtentDifference;
    }

    public ScrollBarVisibility getVerticalScrollBarVisibility() {
        return this.verticalScrollBarVisibility;
    }

    public VerticalScrollUnit getVerticalScrollUnit() {
        return this.verticalScrollUnit;
    }

    public int getVerticalScrollValue(int i, long j) {
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$VerticalScrollUnit[this.verticalScrollUnit.ordinal()]) {
            case 1:
                return this.scrollBarVerticalScale == ScrollBarVerticalScale.SCALED ? (int) (this.scrollPosition.getRowPosition() * 2147483647L / j) : (int) (this.scrollPosition.getRowPosition() * (long) i + (long) this.scrollPosition.getRowOffset());
            case 2:
                return this.scrollBarVerticalScale == ScrollBarVerticalScale.SCALED ? (int) (this.scrollPosition.getRowPosition() * 2147483647L / j) : (int) this.scrollPosition.getRowPosition();
            default:
                throw new IllegalStateException("Unexpected vertical scroll unit: " + this.verticalScrollUnit.name());
        }
    }

    public void setScrollBarVerticalScale(ScrollBarVerticalScale scrollBarVerticalScale2) {
        this.scrollBarVerticalScale = scrollBarVerticalScale2;
    }

    public void setScrollPosition(CodeAreaScrollPosition codeAreaScrollPosition) {
        this.scrollPosition.setScrollPosition(codeAreaScrollPosition);
    }

    public void updateCache(DataProvider dataProvider, int i, int i2) {
        this.verticalScrollUnit = ((BasicScrollingCapable) dataProvider).getVerticalScrollUnit();
        this.verticalScrollBarVisibility = ((BasicScrollingCapable) dataProvider).getVerticalScrollBarVisibility();
        this.horizontalScrollUnit = ((BasicScrollingCapable) dataProvider).getHorizontalScrollUnit();
        this.horizontalScrollBarVisibility = ((BasicScrollingCapable) dataProvider).getHorizontalScrollBarVisibility();
        this.horizontalScrollbarHeight = i;
        this.verticalScrollbarWidth = i2;
    }

    public void updateHorizontalScrollBarValue(int i, int i2) {
        if (i2 != 0) {
            switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$HorizontalScrollUnit[this.horizontalScrollUnit.ordinal()]) {
                case 1:
                    this.scrollPosition.setCharPosition(i / i2);
                    this.scrollPosition.setCharOffset(i % i2);
                    return;
                case 2:
                    this.scrollPosition.setCharPosition(i);
                    this.scrollPosition.setCharOffset(0);
                    return;
                default:
                    throw new IllegalStateException("Unexpected horizontal scroll unit: " + this.horizontalScrollUnit.name());
            }
        }
    }

    public void updateMaximumScrollPosition(long j, int i, int i2, int i3, int i4, int i5) {
        this.maximumScrollPosition.reset();
        if (j > ((long) i)) {
            this.maximumScrollPosition.setRowPosition(j - ((long) i));
        }
        if (this.verticalScrollUnit == VerticalScrollUnit.PIXEL) {
            this.maximumScrollPosition.setRowOffset(i5);
        }
        if (i2 > i3) {
            this.maximumScrollPosition.setCharPosition(i2 - i3);
        }
        if (this.horizontalScrollUnit == HorizontalScrollUnit.PIXEL) {
            this.maximumScrollPosition.setCharOffset(i4);
        }
    }

    public void updateVerticalScrollBarValue(int i, int i2, int i3, long j) {
        if (i2 == 0) {
            this.scrollPosition.setRowPosition(0);
            this.scrollPosition.setRowOffset(0);
            return;
        }
        switch (AnonymousClass1.$SwitchMap$org$exbin$bined$basic$VerticalScrollUnit[this.verticalScrollUnit.ordinal()]) {
            case 1:
                if (this.scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    this.scrollPosition.setRowPosition((i <= 0 || j <= ((long) (i3 / i))) ? (((long) i) * j) / 2147483647L : (((long) i) * (j / ((long) i3))) + ((((long) i) * (j % ((long) i3))) / ((long) i3)));
                    if (this.verticalScrollUnit != VerticalScrollUnit.ROW) {
                        this.scrollPosition.setRowOffset(0);
                        return;
                    }
                    return;
                }
                this.scrollPosition.setRowPosition(i / i2);
                this.scrollPosition.setRowOffset(i % i2);
                return;
            case 2:
                if (this.scrollBarVerticalScale == ScrollBarVerticalScale.SCALED) {
                    this.scrollPosition.setRowPosition((i <= 0 || j <= ((long) (i3 / i))) ? (((long) i) * j) / 2147483647L : (((long) i) * (j / ((long) i3))) + ((((long) i) * (j % ((long) i3))) / ((long) i3)));
                    if (this.verticalScrollUnit != VerticalScrollUnit.ROW) {
                        this.scrollPosition.setRowOffset(0);
                        return;
                    }
                    return;
                }
                this.scrollPosition.setRowPosition(i);
                this.scrollPosition.setRowOffset(0);
                return;
            default:
                throw new IllegalStateException("Unexpected vertical scroll unit: " + this.verticalScrollUnit.name());
        }
    }
}
