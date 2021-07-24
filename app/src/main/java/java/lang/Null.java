package java.lang;

import androidx.annotation.NonNull;

import java.io.Serializable;

public final class Null implements Serializable {
    private static final long serialVersionUID = 8238422008192580722L;

    private Null() {
    }

    @NonNull
    public static <E> E getInstance() {
        return null;
    }
}