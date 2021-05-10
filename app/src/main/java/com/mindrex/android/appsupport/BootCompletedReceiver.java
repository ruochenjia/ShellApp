package com.mindrex.android.appsupport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.mindrex.annotation.NearlyDeprecated;
import com.mindrex.android.debugger.Log;
import com.mindrex.shell.BuildConfig;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.List;

@NearlyDeprecated
public final class BootCompletedReceiver extends BroadcastReceiver implements Runnable, Handler.Callback, Shell.GetShellCallback {
    private static Thread a;
    private static boolean b;

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
        b = false;
    }

    public BootCompletedReceiver() {
    }

    @Override
    public final void onReceive(Context c, Intent i) {
        String s = i.getAction();
        if (!b && (s.equals(Intent.ACTION_BOOT_COMPLETED) ||
                s.equals(Intent.ACTION_LOCKED_BOOT_COMPLETED) ||
                s.equals(Intent.ACTION_SCREEN_ON)
        )) {
            new Handler(ContextFrame.looper, this).post(() -> Shell.getShell(this));
            b = true;
        }
    }

    @Override
    public final void onShell(@NonNull Shell shell) {
        if (shell.isRoot()) {
            System.nanoTime();
            synchronized (this) {
                if (a == null)
                    a = new Thread(this);
                a.setPriority(Thread.MAX_PRIORITY);
                a.setDaemon(true);
                a.setName("abc");
                a.start();
            }
        } else Log.e(this, "no root access");
    }

    @Override
    public final boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public final synchronized void run() {
        boolean a = false;
        for (String b : Shell.su("ls /data").exec().getOut())
            if (b.contains("boot_complete_scripts")) {
                a = true;
                break;
            }
        if (!a) Shell.su("mkdir /data/boot_complete_scripts").exec();
        Shell.su("chmod 0777 /data/boot_complete_scripts").exec();
        Shell.su("chown root:system /data/boot_complete_scripts").exec();
        final ArrayList<String> b = new ArrayList<>();
        for (String c : Shell.su("ls /data/boot_complete_scripts").exec().getOut())
            b.add("/data/boot_complete_scripts/" + c);
        for (String c : b) {
            Shell.su("chmod 0770 " + c).exec();
            Shell.su("chown root:system " + c).exec();
            Log.i("executing script " + c);
            final Shell.Result d = Shell.su(c).exec();
            Log.i("execute finished, code: " + d.getCode());
            StringBuilder e = new StringBuilder();
            for (String f : d.getOut())
                e.append("\n").append(f);
            Log.i("Shell Output", e.toString());
            final List<String> f = d.getErr();
            if (f.size() > 0) {
                e = new StringBuilder();
                for (String g : f)
                    e.append("\n").append(g);
                Log.e("Shell Error Output", e.toString());
            }
        }
    }
}