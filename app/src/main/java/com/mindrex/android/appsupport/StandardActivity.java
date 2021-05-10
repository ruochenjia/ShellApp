package com.mindrex.android.appsupport;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

@SuppressWarnings("unused")
public abstract class StandardActivity extends AppCompatActivity implements ContextFrame {
    public static final int FULL_SCREEN_SETTING;

    private View contentView;
    protected final Handler handler;

    static {
        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE;
        } else {
            FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }
    }

    public StandardActivity() {
        this.handler = new Handler(looper, this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        final Configuration a = new Configuration(newBase.getResources().getConfiguration());
        a.densityDpi = 420;
        a.fontScale = 1f;
        applyOverrideConfiguration(a);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SDK_INT >= Build.VERSION_CODES.Q) {
            final StrictMode.VmPolicy.Builder a = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(a.build());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SDK_INT >= Build.VERSION_CODES.N) {
            if (arePermissionsDenied()) {
                if (SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        final Intent a = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        a.addCategory(Intent.CATEGORY_DEFAULT);
                        a.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                        startActivityForResult(a, 4321);
                    } catch (Exception e) {
                        E.print(this, e);
                    }
                }
                requestPermissions(permissionsRequired(), 1234);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (isTaskRoot())
            ContextUtils.ctx.destroy();
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) {
            newConfig.fontScale = 1;
            final DisplayMetrics a = getResources().getDisplayMetrics();
            final WindowManager b = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            b.getDefaultDisplay().getMetrics(a);
            a.scaledDensity = newConfig.fontScale * a.density;
            getResources().updateConfiguration(newConfig, a);
        }
    }

    @NonNull
    protected String[] permissionsRequired() {
        return new String[0];
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public final void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1234 && grantResults.length > 0) {
            if (arePermissionsDenied()) {
                moveTaskToBack(false);
                StandardApplication.killSelf();
            }
        }
    }

    @Override
    public final void setContentView(View contentView) {
        setContentView(contentView, contentView.getLayoutParams());
    }

    @Override
    public final void setContentView(int layoutResID) {
        setContentView(LayoutInflater.from(this).inflate(layoutResID, null));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        this.contentView = view;
        if (params == null)
            super.setContentView(view);
        else super.setContentView(view, params);
    }

    @NonNull
    @Override
    public Context getContext() {
        return getBaseContext() == null ? ContextUtils.getContext() : this;
    }

    @Override
    public final Handler getHandler() {
        return handler;
    }

    public final View getContentView() {
        return contentView;
    }

    @Deprecated
    @Override
    public final SharedPreferences getSharedPreferences(String name, int mode) {
        try {
            return (SharedPreferences) getConfiguration(name);
        } catch (Exception e) {
            return null;
        }
    }

    public final void removeTempFiles() {
        try {
            Utils.removeTempFiles();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("NewApi")
    private boolean arePermissionsDenied() {
        for (int i = 0; i < permissionsRequired().length; i++) {
            if (checkSelfPermission(permissionsRequired()[i]) == PackageManager.PERMISSION_DENIED)
                return true;
        }
        if (SDK_INT >= Build.VERSION_CODES.R)
            return !Environment.isExternalStorageManager();
        else return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "StandardActivity{" +
                "contentView=" + contentView +
                ", handler=" + handler +
                '}';
    }
}