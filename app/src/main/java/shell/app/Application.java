package shell.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.mindrex.shell.BuildConfig;
import com.mindrex.android.appsupport.StandardApplication;
import com.topjohnwu.superuser.Shell;

import java.io.File;

@SuppressWarnings("RedundantSuppression")
public final class Application extends StandardApplication {
    static {
        System.loadLibrary("native-lib");
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    public Application() throws Exception {
    }

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