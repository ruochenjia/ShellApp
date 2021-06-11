package shell.app;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.mindrex.android.appsupport.SupportActivity;
import com.mindrex.shell.BuildConfig;
import com.mindrex.android.appsupport.Storage;
import com.mindrex.util.Sfx;

import java.util.List;

// Notice: This file might have been changed from original.

public abstract class ActivityFramework extends SupportActivity {
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

    static {
        storagePath = Storage.STORAGE_INTERNAL;
        externalSdPath = Storage.STORAGE_EXTERNAL;
        rootDirectory = Storage.DIRECTORY_ROOT;
        dataDirectory = Storage.DIRECTORY_DATA;
        cacheDirectory = Storage.DIRECTORY_CACHE;
        systemDirectory = Storage.DIRECTORY_SYSTEM;
        downloadFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    private Boolean isTaskRoot = null;
    private ActionBar actionBar = null;

    public ActivityFramework() {
        super(0);
    }

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        isTaskRoot = isTaskRoot();
        final ActionBar a = super.getSupportActionBar();
        if (!isTaskRoot) {
            if (a != null)
                a.setDisplayHomeAsUpEnabled(true);
        }
        actionBar = a;
    }

    @Override
    public final boolean isTaskRoot() {
        if (isTaskRoot != null)
            return isTaskRoot;
        else return super.isTaskRoot();
    }

    @NonNull
    @Override
    public final ActionBar getSupportActionBar() {
        if (actionBar != null)
            return actionBar;
        final ActionBar a = super.getSupportActionBar();
        Sfx.nonnull(a);
        return a;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBar != null && !isTaskRoot) {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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

    @Deprecated
    public final void killBackgroundProcess() {
        final ActivityManager a = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> b = a.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo i : b)
            android.os.Process.killProcess(i.pid);
    }
}