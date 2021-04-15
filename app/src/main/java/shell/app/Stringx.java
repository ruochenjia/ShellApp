package shell.app;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.stream.IntStream;

@SuppressWarnings({"unused"})
public class Stringx implements CharSequence, Comparable<Stringx> {
    private final ArrayList<CharSequence> history = new ArrayList<>();
    private String value;
    private ValueChangeListener listener;

    public Stringx(CharSequence value) {
        this(value.toString());
    }

    public Stringx(String value) {
        this.value = value;
    }

    @NonNull
    public final String toString() {
        return value;
    }

    @NonNull
    public final CharSequence toCharSequence() {
        return value;
    }

    public void setValue(CharSequence s) {
        if (!s.equals(value)) {
            if (listener != null) {
                try {
                    listener.onValueChange(value, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            history.add(s);
            if (history.size() > 10) history.remove(0);
            this.value = s.toString();
        }
    }

    public final Stringx setValueChangeListener(ValueChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public final Stringx value() {
        return new Stringx(value);
    }

    public final Stringx previousValue() throws Exception {
        int i = history.size();
        if (i > 0) return new Stringx(history.get(--i));
        else throw new Exception("No previous values found");
    }

    public final void restorePreviousValue() throws Exception {
        this.value = previousValue().toString();
    }

    public final int length() {
        return value.length();
    }

    public final boolean equals(CharSequence s) {
        return s.toString().equals(value);
    }

    public static Stringx valueOf(byte b) {
        return valueOf((Byte) b);
    }

    public static Stringx valueOf(double d) {
        return valueOf((Double) d);
    }

    public static Stringx valueOf(float f) {
        return valueOf((Float) f);
    }

    public static Stringx valueOf(int i) {
        return valueOf((Integer) i);
    }

    public static Stringx valueOf(long j) {
        return valueOf((Long) j);
    }

    public static Stringx valueOf(@NonNull Object o) {
        return new Stringx(o.toString());
    }

    public static Stringx valueOf(short s) {
        return valueOf((Short) s);
    }

    public static Stringx valueOf(boolean z) {
        return valueOf((Boolean) z);
    }

    @NonNull
    @Override
    public IntStream chars() {
        return value.chars();
    }

    @NonNull
    @Override
    public IntStream codePoints() {
        return value.codePoints();
    }

    @Override
    public char charAt(int index) {
        return value.charAt(index);
    }

    @NonNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return value.substring(start, end);
    }

    @Override
    public int compareTo(Stringx stringx) {
        return value.compareTo(stringx.value);
    }

    public interface ValueChangeListener {
        void onValueChange(CharSequence original, CharSequence current) throws Exception;
    }
}