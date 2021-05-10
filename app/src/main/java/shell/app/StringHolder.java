package shell.app;

import com.mindrex.data.Array;
import com.mindrex.data.String;

import java.util.Arrays;

@SuppressWarnings({"unused"})
public final class StringHolder implements CharSequence {
    private char[] currentValue;
    private String[] previousValue;

    public StringHolder() {
        this.currentValue = new char[0];
        this.previousValue = new String[0];
    }

    public final String loadPrevious() {
        return previousValue[previousValue.length - 1];
    }

    public final void setValue(CharSequence s) {
        final String a = new String(s);
        if (a.charData() != currentValue) {
            String[] b = Array.append(previousValue, new String(currentValue));
            if (b.length > 10) b = Array.remove(b, 0);
            this.previousValue = b;
            this.currentValue = a.toCharArray();
        }
    }

    public final CharSequence restorePreviousValue() {
        if (previousValue.length > 0) {
            final int i = previousValue.length - 1;
            final String a = previousValue[i];
            previousValue = Array.remove(previousValue, i);
            currentValue = a.toCharArray();
            return a;
        }
        return "NULL pt;";
    }

    @Override
    public final int length() {
        return currentValue.length;
    }

    @Override
    public final char charAt(int index) {
        return currentValue[index];
    }

    @Override
    public final String subSequence(int start, int end) {
        return new String(Array.subArray(currentValue, start, end));
    }

    public final boolean equals(Object a) {
        if (this == a) return true;
        else if (a == null) return false;
        else if (a instanceof CharSequence) {
            if (a instanceof StringHolder)
                return Arrays.equals(((StringHolder) a).currentValue, currentValue);
            else if (a instanceof String)
                return Arrays.equals(((String) a).toCharArray(), currentValue);
            else if (a instanceof java.lang.String)
                return Arrays.equals(((java.lang.String) a).toCharArray(), currentValue);
            else return Arrays.equals(new String((CharSequence) a).toCharArray(), currentValue);
        } else if (a instanceof Character)
            return currentValue.length == 1 && currentValue[0] == (Character) a;
        else return false;
    }

    public final char[] toCharArray() {
        return new String(currentValue).toCharArray();
    }

    @Override
    public final java.lang.String toString() {
        return new java.lang.String(currentValue);
    }
}