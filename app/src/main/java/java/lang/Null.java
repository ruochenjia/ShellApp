package java.lang;

import java.io.Serializable;

public final class Null implements Serializable {
    private static final long serialVersionUID = 8238422008192580722L;
    private static final Object Null = null;

    private Null() {
    }

    @SuppressWarnings("unchecked")
    public static <E> E getInstance() {
        return (E) Null;
    }
}