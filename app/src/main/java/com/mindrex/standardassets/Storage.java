package com.mindrex.standardassets;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Storage {
    public static final String STORAGE_INTERNAL;
    public static final String STORAGE_EXTERNAL;

    public static final String DIRECTORY_ROOT;
    public static final String DIRECTORY_DATA;
    public static final String DIRECTORY_CACHE;
    public static final String DIRECTORY_SYSTEM;
    public static final String DIRECTORY_VENDOR;
    public static final String DIRECTORY_PRODUCT;

    public static final String APPLICATION_DATA;
    public static final String APPLICATION_CACHE;
    public static final String APPLICATION_ROOT;

    static {
        STORAGE_INTERNAL = Environment.getExternalStorageDirectory().getAbsolutePath();
        DIRECTORY_ROOT = Utils.getDirectory(null, "/");
        DIRECTORY_DATA = Utils.getDirectory("ANDROID_DATA", "/data");
        DIRECTORY_CACHE = Utils.getDirectory("ANDROID_CACHE", "/cache");
        DIRECTORY_SYSTEM = Utils.getDirectory("ANDROID_ROOT", "/system");
        DIRECTORY_VENDOR = Utils.getDirectory("VENDOR_ROOT", "/vendor");
        DIRECTORY_PRODUCT = Utils.getDirectory("PRODUCT_ROOT", "/product");
        final Context a = ContextUtils.getApplicationContext();
        APPLICATION_DATA = a.getFilesDir().getAbsolutePath();
        APPLICATION_CACHE = a.getCacheDir().getAbsolutePath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            APPLICATION_ROOT = a.getDataDir().getAbsolutePath();
        else APPLICATION_ROOT = APPLICATION_DATA + File.separator + "..";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final List<String> b = getExternalSdPaths(a);
            if (b.size() > 0) STORAGE_EXTERNAL = b.get(0);
            else STORAGE_EXTERNAL = null;
        } else STORAGE_EXTERNAL = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static List<String> getExternalSdPaths(Context ctx) {
        final ArrayList<String> a = new ArrayList<>();
        for (File f : ctx.getExternalFilesDirs("external")) {
            if (f != null && !f.equals(ctx.getExternalFilesDir("external"))) {
                final int b = f.getAbsolutePath().lastIndexOf("/Android");
                if (b >= 0) {
                    String path = f.getAbsolutePath().substring(0, b);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (Exception e) {
                        E.print(e);
                    }
                    a.add(path);
                }
            }
        }
        return a;
    }
}