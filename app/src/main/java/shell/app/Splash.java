package shell.app;

import com.mindrex.gm.GmSystem;

public final class Splash {
    private static final CharSequence[] splash = new CharSequence[]{
            "Never give up once you started",
            "Spending Time != Quality",
            "Learning would never make you sad",
            "Everyday is a new fresh beginning of you life"
    };
    private static final CharSequence firstRun = "A simple root-access file manager for android";

    public static CharSequence getRandom() {
        if (Application.firstRun())
            return firstRun;
        return splash[GmSystem.defaultRandom.nextInt(splash.length)];
    }

    private Splash() {
    }
}