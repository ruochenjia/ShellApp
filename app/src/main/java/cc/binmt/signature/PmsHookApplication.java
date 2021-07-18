package cc.binmt.signature;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.mindrex.util.Sfx;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class PmsHookApplication extends Application implements InvocationHandler {
    private static final int GET_SIGNATURES = 0x00000040;

    private Object base;
    private byte[][] sign;
    private String appPkgName = "";

    @Override
    public final void attachBaseContext(Context base) {
        try {
            hook(base);
            Log.i("PmsHook", "Success");
        } catch (Exception e) {
            Log.e("PmsHook", "Failed, error detail: " + e.toString());
        }
        super.attachBaseContext(base);
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getPackageInfo".equals(method.getName())) {
            String pkgName = (String) args[0];
            Integer flag = (Integer) args[1];
            if ((flag & GET_SIGNATURES) != 0 && appPkgName.equals(pkgName)) {
                PackageInfo info = (PackageInfo) method.invoke(base, args);
                Sfx.nonnull(info).signatures = new Signature[this.sign.length];
                for (int i = 0; i < info.signatures.length; i++) {
                    info.signatures[i] = new Signature(this.sign[i]);
                }
                return info;
            }
        }
        return method.invoke(base, args);
    }

    private void hook(Context context) throws Exception {
        String data = "### Signatures Data ###";
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(Base64.decode(data, Base64.DEFAULT)));
        byte[][] sign = new byte[is.read() & 0xFF][];
        for (int i = 0; i < sign.length; i++) {
            sign[i] = new byte[is.readInt()];
            is.readFully(sign[i]);
        }
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);
        Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        this.base = sPackageManager;
        this.sign = sign;
        this.appPkgName = context.getPackageName();
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(), new Class<?>[]{iPackageManagerInterface}, this);
        sPackageManagerField.set(currentActivityThread, proxy);
        PackageManager pm = context.getPackageManager();
        Field mPmField = pm.getClass().getDeclaredField("mPM");
        mPmField.setAccessible(true);
        mPmField.set(pm, proxy);
    }
}