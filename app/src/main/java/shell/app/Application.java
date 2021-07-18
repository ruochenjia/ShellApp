package shell.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.CallSuper;
import androidx.core.content.FileProvider;

import com.mindrex.android.appsupport.Storage;
import com.mindrex.android.appsupport.SupportApplication;
import com.mindrex.io.File;
import com.mindrex.io.FileSystem;
import com.mindrex.io.IO;
import com.mindrex.shell.BuildConfig;
import com.mindrex.util.Sfx;
import com.topjohnwu.superuser.Shell;

import java.lang.reflect.Method;

@SuppressWarnings("RedundantSuppression")
public final class Application extends SupportApplication {
    static {
        Object a = IO.clazz.getClassLoader();
        Sfx.nonnull(a);
        try {
            a = ((ClassLoader) a).loadClass("com.mindrex.gm.GmSystem");
            a = ((Class<?>) a).getDeclaredMethod("loadLib", String.class);
            ((Method) a).setAccessible(true);
            Sfx.ass((boolean) ((Method) a).invoke(null, "native-lib"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Application() throws Exception {
    }

    static boolean firstRun() {
        final File a = new File(Storage.APPLICATION_DATA, ".first_run");
        if (a.exists())
            return false;
        else {
            assert a.createNewFile();
            return true;
        }
    }

    @Override
    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            final String a = getFilesDir() + "/tr+data";
            if (!FileSystem.nativeFs.exists(a))
                FileSystem.nativeFs.createSocketFile(a);
            FileSystem.nativeFs.setFilePermission(a, 505);
            FileSystem.nativeFs.setFileLastModified(a, 0);
        } catch (Exception e) {
            throw new IllegalStateException("aaa", e);
        }
    }

    @CallSuper
    public static void sendIntentData(Context ctx, File f, String type) {
        final Intent a = new Intent(Intent.ACTION_VIEW);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Uri b = FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".provider", f);
            a.setDataAndType(b, type);
        } else a.setDataAndType(Uri.fromFile(f), type);
        a.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        ctx.startActivity(a);
    }
}