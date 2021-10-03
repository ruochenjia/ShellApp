package shell.app;

import androidx.annotation.NonNull;

import com.mindrex.data.Array;
import com.mindrex.data.string;

import java.util.Arrays;

@SuppressWarnings({"unused"})
final class StringHolder implements CharSequence {
    private char[] currentValue;
    private string[] previousValue;

    public StringHolder() {
        this.currentValue = new char[0];
        this.previousValue = new string[0];
    }

    public final string loadPrevious() {
        return previousValue[previousValue.length - 1];
    }

    public final void setValue(CharSequence s) {
        final string a = new string(s);
        if (a.charData() != currentValue) {
            string[] b = Array.append(previousValue, new string(currentValue));
            if (b.length > 10) b = Array.remove(b, 0);
            this.previousValue = b;
            this.currentValue = a.charData();
        }
    }

    public final CharSequence restorePreviousValue() {
        if (previousValue.length > 0) {
            final int i = previousValue.length - 1;
            final string a = previousValue[i];
            previousValue = Array.remove(previousValue, i);
            currentValue = a.charData();
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

    @NonNull
    @Override
    public final string subSequence(int start, int end) {
        return new string(Array.subArray(currentValue, start, end));
    }

    public final boolean equals(Object a) {
        if (a instanceof CharSequence) {
            if (a instanceof StringHolder)
                return Arrays.equals(((StringHolder) a).currentValue, currentValue);
            else if (a instanceof string)
                return Arrays.equals(((string) a).charData(), currentValue);
            else if (a instanceof java.lang.String)
                return Arrays.equals(((java.lang.String) a).toCharArray(), currentValue);
            else return Arrays.equals(new string((CharSequence) a).charData(), currentValue);
        } else if (a instanceof Character)
            return currentValue.length == 1 && currentValue[0] == (Character) a;
        else return false;
    }

    public final char[] toCharArray() {
        return new string(currentValue).charData();
    }

    @NonNull
    @Override
    public final String toString() {
        return new String(currentValue);
    }
}