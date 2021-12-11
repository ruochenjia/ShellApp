package shell.app;

import android.Manifest;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.mindrex.android.appsupport.Storage;
import com.mindrex.android.appsupport.SupportActivity;
import com.mindrex.util.Util;

// Notice: This file might have been changed from original.

public abstract class ActivityFramework extends SupportActivity {
    public static final String storagePath;
    public static final String externalSdPath;
    public static final String rootDirectory;
    public static final String dataDirectory;
    public static final String cacheDirectory;
    public static final String systemDirectory;

    static {
        storagePath = Storage.STORAGE_INTERNAL;
        externalSdPath = Storage.STORAGE_EXTERNAL;
        rootDirectory = Storage.DIRECTORY_ROOT;
        dataDirectory = Storage.DIRECTORY_DATA;
        cacheDirectory = Storage.DIRECTORY_CACHE;
        systemDirectory = Storage.DIRECTORY_SYSTEM;
    }

    private ActionBar actionBar = null;

    public ActivityFramework(int state) {
        super(state);
    }

    public ActivityFramework() {
        super(0);
    }

    @Override
    public void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        final ActionBar a = super.getSupportActionBar();
        if (!isTaskRoot()) {
            if (a != null)
                a.setDisplayHomeAsUpEnabled(true);
        }
        actionBar = a;
    }

    @NonNull
    public final ActionBar getSupportActionBar() {
        if (actionBar != null)
            return actionBar;
        final ActionBar a = super.getSupportActionBar();
        Util.nonnull(a);
        return a;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBar != null && !isTaskRoot()) {
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
        return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }
}