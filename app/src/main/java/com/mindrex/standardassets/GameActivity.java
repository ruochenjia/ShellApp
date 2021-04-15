package com.mindrex.standardassets;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.mindrex.util.Sfx;

import java.io.File;

@SuppressWarnings("unused")
public abstract class GameActivity extends ObjectiveActivity implements Runnable {
    public static float ratioX, ratioY;
    public static int screenX, screenY;

    protected final Context context;

    public GameActivity() {
        this.context = getContext();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        final Configuration a = new Configuration(newBase.getResources().getConfiguration());
        a.navigationHidden = Configuration.NAVIGATIONHIDDEN_YES;
        a.densityDpi = 420;
        a.fontScale = 1f;
        applyOverrideConfiguration(a);
    }

    @Override
    public final void onPreCreate() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(StandardActivity.FULL_SCREEN_SETTING);
        Sfx.nonnull(getSupportActionBar()).hide();
        final Point a = new Point();
        getWindowManager().getDefaultDisplay().getSize(a);
        screenX = a.x;
        screenY = a.y;
        ratioX = 1920f / screenX;
        ratioY = 1080f / screenY;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

    @Override
    public void run() {
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (arePermissionsDenied())
                requestPermissions(permissionsRequired(), 1234);
        }
    }

    @Override
    public void onDestroy() {
        Runtime.getRuntime().gc();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) {
            newConfig.fontScale = 1;
            final DisplayMetrics a = getResources().getDisplayMetrics();
            final WindowManager b = (WindowManager) getSystemService(WINDOW_SERVICE);
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

    public final void hideTitleBar() {
        try {
            Sfx.nonnull(getSupportActionBar()).hide();
        } catch (Exception e) {
            E.print(this, e);
        }
    }

    public final void removeTempFiles() {
        try {
            Utils.removeTempFiles();
        } catch (Exception e) {
            E.print(this, e);
        }
    }

    @Override
    public void mkdir(File f) {
        super.mkdir(f);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("NewApi")
    private boolean arePermissionsDenied() {
        final String[] s = permissionsRequired();
        int PERMISSION_COUNT = s.length;
        for (int i = 0; i < PERMISSION_COUNT; i++) {
            if (checkSelfPermission(permissionsRequired()[i]) == PackageManager.PERMISSION_DENIED)
                return true;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "GameActivity{" +
                "context=" + context +
                '}';
    }
}