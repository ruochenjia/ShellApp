package java.lang;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mindrex.util.Castable;
import com.mindrex.util.Markable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Locale;

public class String extends string implements Cloneable, CharSequence, Serializable, Comparable<String>, Castable, Markable {
    private static final long serialVersionUID = 493749190461822424L;

    public static final class Builder {
    }

    public String(byte[] a, Charset b) {
    }

    public String(byte[] a, int b, int c, Charset d) {
    }

    public String(char[] a) {
    }

    public String(char[] a, int b, int c) {
    }

    public static native String valueOf(Object a);

    public static native String valueOf(Object a, int b, int c);

    public native boolean isEmpty();

    public native String substring(int a);

    public native String substring(int a, int b);

    public native char[] toCharArray();

    public native String toLowerCase();

    public native String toLowerCase(Locale a);

    public native boolean equalsIgnoreCase(String a);

    public native boolean startsWith(String a);

    public native boolean endsWith(String a);

    public native boolean contains(CharSequence a);

    public native String replace(CharSequence a, CharSequence b);

    public native int indexOf(String a);

    @Override
    public native int compareTo(String s);

    @Override
    public native int length();

    @Override
    public native char charAt(int i);

    @Override
    public native CharSequence subSequence(int i, int i1);

    @Override
    public native String clone();

    @Override
    public native <E> E castTo(@NonNull Class<E> clazz);

    @Override
    public native void readObject(@NonNull ObjectInputStream a) throws Throwable;

    @Override
    public native void writeObject(@NonNull ObjectOutputStream a) throws Throwable;

    @NonNull
    @Override
    public String toString() {
        return Null.getInstance();
    }

    @Override
    public native boolean equals(@Nullable Object obj);

    @Override
    public native int hashCode();

    @Override
    protected final void finalize() {
        throw (String) "\\finalize\\";
    }
}