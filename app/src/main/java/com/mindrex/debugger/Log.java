package com.mindrex.debugger;

import com.mindrex.io.CharsetV2;
import com.mindrex.standardassets.ContextUtils;
import com.mindrex.standardassets.Storage;
import com.mindrex.util.Sfx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class Log extends Thread {
    private static final LinkedBlockingQueue<Object> queue;
    private static final SimpleDateFormat timerFormat;
    private static final SimpleDateFormat timeFormat1;

    public static final byte ERROR = 0;
    public static final byte WARNING = 1;
    public static final byte DEBUG = 2;
    public static final byte VERBOSE = 3;
    public static final byte INFO = 4;

    static String logStorage;
    static String logStorage1;
    static boolean state;

    static {
        queue = new LinkedBlockingQueue<>();
        timerFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        timeFormat1 = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        state = false;
    }

    public static void c(Throwable e) {
        c((String) null, e);
    }

    public static void c(Thread t, Throwable e) {
        if (t == null) c(e);
        else c("Thread: " + t.getName(), e);
    }

    public static void c(Object o, Throwable e) {
        c(o.getClass(), e);
    }

    public static void c(Class<?> c, Throwable e) {
        c(c.getName(), e);
    }

    public static void c(String s, Throwable e) {
        final StringBuilder a = new StringBuilder();
        b(e, a);
        e(s, a.toString());
    }

    static void b(Throwable e, StringBuilder s) {
        s.append(e.toString());
        for (StackTraceElement a : e.getStackTrace()) s.append("\n\tat ").append(a.toString());
        final Throwable b = e.getCause();
        if (b != null) b(b, s.append("\nCaused by: "));
    }

    public static void d(String s) {
        d((String) null, s);
    }

    public static void d(String t, String s) {
        print(t, s, DEBUG);
    }

    public static void d(Class<?> c, String s) {
        d(c.getName(), s);
    }

    public static void d(Object o, String s) {
        d(o.getClass(), s);
    }

    public static void e(String s) {
        e((String) null, s);
    }

    public static void e(String t, String s) {
        print(t, s, ERROR);
    }

    public static void e(Class<?> c, String s) {
        e(c.getName(), s);
    }

    public static void e(Object o, String s) {
        e(o.getClass(), s);
    }

    public static void i(String s) {
        i((String) null, s);
    }

    public static void i(String t, String s) {
        print(t, s, INFO);
    }

    public static void i(Class<?> c, String s) {
        i(c.getName(), s);
    }

    public static void i(Object o, String s) {
        i(o.getClass(), s);
    }

    public static void w(String s) {
        w((String) null, s);
    }

    public static void w(String t, String s) {
        print(t, s, WARNING);
    }

    public static void w(Class<?> c, String s) {
        w(c.getName(), s);
    }

    public static void w(Object o, String s) {
        w(o.getClass(), s);
    }

    public static void print(String t, String s, byte type) {
        if (!stopCommand) {
            final StringBuilder a = new StringBuilder();
            a.append(timerFormat.format(new Date(System.currentTimeMillis())));
            switch (type) {
                case ERROR:
                    a.append(" [ERROR] ");
                    break;
                case WARNING:
                    a.append(" [WARNING] ");
                    break;
                case DEBUG:
                    a.append(" [DEBUG] ");
                    break;
                case VERBOSE:
                    a.append(" [VERBOSE] ");
                    break;
                case INFO:
                    a.append(" [INFO] ");
                    break;
                default:
                    a.append(" [UNKNOWN] ");
                    break;
            }
            if (t != null) a.append("<").append(t).append("> ");
            queue.offer(a.append(s).append("\n").toString());
        }
    }

    public static boolean stopCommand = false;

    public static void attachStorageContext() {
        if (!state && !stopCommand) {
            logStorage = Storage.STORAGE_INTERNAL + "/logs/";
            logStorage1 = Storage.APPLICATION_ROOT + "/logs/";
            final Log a = new Log();
            a.setPriority(7);
            a.setDaemon(true);
            a.start();
            state = true;
        }
    }

    static OutputStream out(boolean z) {
        OutputStream a;
        try {
            final File c = new File(z ? logStorage + "crash-reports/" : logStorage);
            if (!c.exists()) if (!c.mkdirs()) throw new Exception();
            final File d = new File(c, ContextUtils.getApplicationContext().getPackageName() + "-" + timeFormat1.format(new Date(System.currentTimeMillis())) + ".txt");
            a = new FileOutputStream(d, true);
        } catch (Exception e) {
            try {
                final File c = new File(z ? logStorage1 + "crash-reports/" : logStorage1);
                if (!c.exists()) Sfx.ass(c.mkdirs());
                final File d = new File(c, timeFormat1.format(new Date(System.currentTimeMillis())) + ".txt");
                a = new FileOutputStream(d);
            } catch (Exception r) {
                throw new Error(r);
            }
        }
        return a;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public final void run() {
        final OutputStream a = out(false);
        try {
            boolean z = false;
            while (!stopCommand) {
                final String s = (String) queue.poll(1, TimeUnit.SECONDS);
                if (s != null) {
                    a.write(s.getBytes(CharsetV2.UTF_8));
                    z = true;
                } else if (z) {
                    a.flush();
                    z = false;
                }
            }
            a.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void interrupt() {
        if (stopCommand) {
            super.interrupt();
            Runtime.getRuntime().gc();
        }
    }

    private Log() {
    }
}