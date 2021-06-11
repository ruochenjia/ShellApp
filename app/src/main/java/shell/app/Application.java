package shell.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.CallSuper;
import androidx.core.content.FileProvider;

import com.mindrex.android.appsupport.SupportApplication;
import com.mindrex.gm.GmSystem;
import com.mindrex.shell.BuildConfig;
import com.mindrex.util.Sfx;
import com.topjohnwu.superuser.Shell;

import java.io.File;

@SuppressWarnings("RedundantSuppression")
public final class Application extends SupportApplication {
    static {
        Sfx.ass(GmSystem.loadLib("native-lib"), "Could not load native library");
    }

    public Application() throws Exception {
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
    public void onTrimMemory(int level) {
        level = -1;
        super.onTrimMemory(TRIM_MEMORY_COMPLETE);
    }

    @Override
    public final void onCreate() {
        super.onCreate();
      //  if (debug())
        //    startDaemon(getFilesDir() + "/test_skt");
    }

    private static native int startDaemon(String skt);

    @CallSuper
    public static void sendIntentData(final Context context, final File f, final String intentType) {
        sendIntentData(context, f, intentType, false);
    }

    public static void sendIntentData(final Context context, final File f, final String intentType, final boolean grantWritePermission) {
        final Intent a = new Intent(Intent.ACTION_VIEW);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            final Uri b = FileProvider.getUriForFile(context, ActivityFramework.packageName + ".provider", f);
            a.setDataAndType(b, intentType);
        } else a.setDataAndType(Uri.fromFile(f), intentType);
        a.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (grantWritePermission) a.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.startActivity(a);
    }
}