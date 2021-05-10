package com.mindrex.android.debugger;

import android.os.Build;

import com.mindrex.io.CharsetV2;
import com.topjohnwu.superuser.Shell;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CrashHandler {
    public static void crash(Throwable e) {
        Log.stopCommand = true;
        final StringBuilder a = new StringBuilder()
                .append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS", Locale.ROOT).format(new Date(System.currentTimeMillis())))
                .append(" App Crash\n[DEVICE INFORMATION]\n")
                .append(collectDeviceInfo().toString())
                .append("\n[ERROR DETAIL]\n");
        Log.b(e, a);
        try {
            final OutputStream b = Log.out(true);
            b.write(a.toString().getBytes(CharsetV2.UTF_8));
            b.flush();
            b.close();
        } catch (Exception b) {
            throw new Error(b);
        }
        System.exit(0);
    }

    public static DeviceInfo collectDeviceInfo() {
        final DeviceInfo a = new DeviceInfo();
        a.osVersion = System.getProperty("os.version");
        a.apiLevel = Build.VERSION.SDK_INT;
        a.device = Build.DEVICE;
        a.model = Build.MODEL;
        a.product = Build.PRODUCT;
        a.cpuAbi = Build.CPU_ABI;
        a.cpuAbi2 = Build.CPU_ABI2;
        a.root = Shell.rootAccess();
        return a;
    }

    public static final class DeviceInfo {
        public String osVersion;
        public int apiLevel;
        public String device;
        public String model;
        public String product;
        public String cpuAbi;
        public String cpuAbi2;
        public boolean root;

        private DeviceInfo() {
        }

        public final String toString() {
            return "OS Version: " + osVersion +
                    "\nAPI Level: " + apiLevel +
                    "\nDevice: " + device +
                    "\nModel: " + model +
                    "\nProduct: " + product +
                    "\nCPU ABI: " + cpuAbi +
                    "\nCPU ABI2: " + cpuAbi2 +
                    "\nRoot: " + root;
        }
    }
}