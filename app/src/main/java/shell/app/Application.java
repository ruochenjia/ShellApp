package shell.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import com.mindrex.android.appsupport.CommandUtils;
import com.mindrex.android.appsupport.Storage;
import com.mindrex.android.appsupport.SupportApplication;
import com.mindrex.io.File;
import com.mindrex.util.FilePermission;

public final class Application extends SupportApplication {
    static {
        System.loadLibrary("native-lib");
    }

    private static Boolean firstRun = null;

    static boolean firstRun() {
        if (firstRun == null) {
            final File a = new File(Storage.APPLICATION_DATA, ".first_run");
            if (a.exists())
                return firstRun = false;
            else {
                //noinspection ResultOfMethodCallIgnored
                a.createNewFile();
                a.setMode(FilePermission.OWNER_READ | FilePermission.OWNER_WRITE);
                return firstRun = true;
            }
        } else return firstRun;
    }

    @Override
    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        CommandUtils.getShell();
    }

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