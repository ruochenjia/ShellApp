package shell.app;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mindrex.shell.BuildConfig;
import com.mindrex.standardassets.E;
import com.mindrex.standardassets.StandardActivity;
import com.mindrex.standardassets.Storage;

import java.io.File;
import java.util.List;

import static com.mindrex.io.FileUtil.deleteDir;

// Notice: This file might have been changed from original.

public abstract class ActivityFramework extends StandardActivity implements View.OnClickListener {
    public static final int versionCode = BuildConfig.VERSION_CODE;
    public static final String versionName = BuildConfig.VERSION_NAME;
    public static String packageName = BuildConfig.APPLICATION_ID;
    public static String appName;

    public static final String storagePath;
    public static final String externalSdPath;
    public static final String rootDirectory;
    public static final String dataDirectory;
    public static final String cacheDirectory;
    public static final String systemDirectory;

    public static String downloadFolderPath;
    public static final String applicationPath;
    public static final String applicationDataPath;
    public static final String applicationCachePath;
    public static final File applicationFolder;
    public static final File applicationDataFolder;
    public static final File applicationCacheFolder;
    public static File applicationExternalFolder;
    public static File applicationExternalCacheFolder;
    public static File applicationExternalDataFolder;

    static {
        storagePath = Storage.STORAGE_INTERNAL;
        externalSdPath = Storage.STORAGE_EXTERNAL;
        rootDirectory = Storage.DIRECTORY_ROOT;
        dataDirectory = Storage.DIRECTORY_DATA;
        cacheDirectory = Storage.DIRECTORY_CACHE;
        systemDirectory = Storage.DIRECTORY_SYSTEM;
        downloadFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        applicationPath = Storage.APPLICATION_DATA;
        applicationDataPath = Storage.APPLICATION_ROOT;
        applicationCachePath = Storage.APPLICATION_CACHE;
        applicationFolder = new File(applicationPath);
        applicationDataFolder = new File(applicationDataPath);
        applicationCacheFolder = new File(applicationCachePath);
    }

    public ActivityFramework() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationExternalFolder = getExternalFilesDir(null);
        applicationExternalCacheFolder = getExternalCacheDir();
        applicationExternalDataFolder = getExternalFilesDir("..");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) {
            newConfig.fontScale = 1;
            DisplayMetrics a = getResources().getDisplayMetrics();
            WindowManager b = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            b.getDefaultDisplay().getMetrics(a);
            a.scaledDensity = newConfig.fontScale * a.density;
            getResources().updateConfiguration(newConfig, a);
        }
    }

    @NonNull
    @Override
    public final String[] permissionsRequired() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            };
        } else return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }

    public final boolean isDarkModeOn() {
        final View a = new View(getApplicationContext());
        final int b = a.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return b == Configuration.UI_MODE_NIGHT_YES;
    }

    @Deprecated
    public final void killBackgroundProcess() {
        final ActivityManager a = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> b = a.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo i : b) {
            android.os.Process.killProcess(i.pid);
        }
    }

    @Deprecated
    protected void setupFolders() {
    }

    @SuppressWarnings("UnusedReturnValue")
    protected static Boolean clearWebViewData(final Context context) {
        try {
            final File a = new File(applicationDataFolder, "app_webview");
            if (a.exists()) return deleteDir(a);
        } catch (Exception e) {
            E.print(context, e);
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected static Boolean clearCookies(final Context context) {
        try {
            final File a = new File(applicationDataPath, "app_webview/default/cookies");
            if (a.exists()) return a.delete();
        } catch (Exception e) {
            E.print(context, e);
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected static Boolean clearAppCache(final Context context) {
        try {
            final File a = applicationCacheFolder;
            if (a.exists()) return deleteDir(a);
        } catch (Exception e) {
            E.print(context, e);
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected static Boolean clearAppData(final Context context) {
        try {
            final File a = applicationFolder;
            return deleteDir(a);
        } catch (Exception e) {
            E.print(context, e);
        }
        return false;
    }

    @Override
    public void onClick(View view) {
    }
}