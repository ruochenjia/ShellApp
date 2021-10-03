package shell.app.xposed;

public class XposedHelper {
    private static XposedHelper ins;

    public static XposedHelper getInstance() {
        if (ins == null) {
            ins = new XposedHelper();
            init(ins);
        }
        return ins;
    }

    public interface init {
        static void main(String... a) {
            ins = getInstance();
        }
    }

    private static native void init(XposedHelper a);

    private XposedHelper() {
    }

    static {
        XposedBridge.load("boid");
    }
}