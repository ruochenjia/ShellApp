package shell.app.xposed;

import androidx.annotation.CallSuper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Deprecated
public abstract class ModuleBase implements IXposedHookLoadPackage {
    public static ClassLoader classLoader;
    public static String packageName;
    public static boolean isFirstApplication;
    public static String processName;

    static {
        classLoader = XposedModule.class.getClassLoader();
        packageName = "android.package.name";
        isFirstApplication = false;
        processName = "android.process.name";
    }

    private boolean init;

    public ModuleBase() {
        init = false;
    }

    @CallSuper
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam a) throws Exception {
        classLoader = a.classLoader;
        packageName = a.packageName;
        isFirstApplication = a.isFirstApplication;
        processName = a.processName;
        synchronized (this) {
            if (!preInit(a)) init = false;
            if (!init(a)) init = false;
            if (!initObjects()) init = false;
        }
        init = true;
    }

    protected abstract boolean preInit(Object args) throws Exception;

    protected abstract boolean init(Object args) throws Exception;

    protected abstract boolean initObjects() throws Exception;

    public final boolean init() {
        return init;
    }
}