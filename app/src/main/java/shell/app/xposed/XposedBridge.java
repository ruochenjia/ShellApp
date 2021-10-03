package shell.app.xposed;

import com.mindrex.gm.GmSystem;

public final class XposedBridge {
    public static final int main = 1;

    public static int main(String... args) {
        XposedHelper.init.main(args);
        return main;
    }

    public static void load(String libc) {
        chknme(libc);
        GmSystem.load(libc);
    }

    private static void chknme(String s) {
        final class a {
            public static final String boid = "boid";
            public static final String def = "def";
            public static final String coreio = "coreio";
            public static final String nativei = "nativei";
        }
        switch (s) {
            case a.boid:
            case a.def:
            case a.coreio:
            case a.nativei:
                return;
            default:
                throw new IllegalArgumentException();
        }
    }
}