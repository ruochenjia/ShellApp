package shell.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mindrex.debugger.Log;
import com.mindrex.standardassets.ContextUtils;
import com.mindrex.standardassets.E;
import com.mindrex.util.Configuration;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public final class InternetProvider extends VpnService implements android.os.Handler.Callback {
    private Handler handler;
    private Thread thread;
    private ParcelFileDescriptor base;
    private DatagramChannel channel;
    private String label, address, route, dnsServer1, dnsServer2, dnsServer3, dnsServer4;
    private String[] blacklist, whitelist;
    private boolean running;

    public InternetProvider() {
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(this, "service started");
        if (handler == null) handler = new Handler(getMainLooper(), this);
        if (thread != null) thread.interrupt();
        try {
            initializeConfiguration();
        } catch (Exception e) {
            throw new Error(e);
        }
        thread = new Thread(this::run);
        thread.setPriority(8);
        thread.setName(label);
        thread.setDaemon(true);
        thread.start();
        return START_STICKY;
    }

    public final void initializeConfiguration() throws Exception {
        final Configuration a = ContextUtils.getConfiguration("InternetProvider");
        label = a.getString("label", "InternetProvider");
        address = a.getString("address", "172.16.0.1");
        route = a.getString("route", "0.0.0.0");
        dnsServer1 = a.getString("dns1", "1.1.1.1");
        dnsServer2 = a.getString("dns2", "1.0.0.1");
        dnsServer3 = a.getString("dns3", "8.8.8.8");
        dnsServer4 = a.getString("dns4", "8.8.4.4");
        blacklist = a.getStringArray("blacklist", new String[0]);
        whitelist = a.getStringArray("whitelist", new String[0]);
    }

    @Override
    public final boolean handleMessage(@NonNull Message message) {
        return true;
    }


    public final void run() {
        Log.i(this, "thread started");
        running = true;
        synchronized (this) {
            final Builder a = new Builder();
            try {
                a.setSession(label)
                        .addAddress(address, 24)
                        .addDnsServer(dnsServer1)
                        .addDnsServer(dnsServer2)
                        .addDnsServer(dnsServer3)
                        .addDnsServer(dnsServer4)
                        .addRoute("0.0.0.0", 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        for (String s : blacklist) a.addDisallowedApplication(s);
                    } catch (Exception e) {
                        Log.c(thread, e);
                    }
                    try {
                        for (String s : whitelist) a.addAllowedApplication(s);
                    } catch (Exception e) {
                        Log.c(thread, e);
                    }
                }
            } catch (Exception e) {
                Log.c(thread, e);
                throw new Error(e);
            }
            base = a.establish();
            synchronized (this) {
                try {
                    connect();
                } catch (Exception e) {
                    Log.c(thread, e);
                }
            }
        }
    }

    public final void connect() throws Exception {
        Log.i(this, "prepare connection");
        final InputStream i = new FileInputStream(base.getFileDescriptor());
        final OutputStream o = new FileOutputStream(base.getFileDescriptor());
        final ByteBuffer f = ByteBuffer.allocate(32767);
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8087));
        protect(channel.socket());
        int t = 0;
        while (running) {
            boolean e = true;
            int a = i.read(f.array());
            if (a > 0) {
                f.limit(a);
                channel.write(f);
                f.clear();
                e = false;
                if (t < 1) t = 1;
            }
            try {
                a = channel.read(f);
            } catch (PortUnreachableException x) {
                Log.c(thread, x);
            }
            if (a > 0) {
                if ((int) f.get(0) != 0) {
                    o.write(f.array(), 0, a);
                }
                f.clear();
                e = false;
                if (t > 0) t = 0;
            }
            if (e) {
                try {
                    Thread.sleep(100);
                } catch (Exception x) {
                    Log.c(thread, x);
                }
                if (t > 0) t += 100;
                else t -= 100;

                if (t < -15000) {
                    f.put((byte) 0).limit(1);
                    for (byte b = 0; b < 2; b++) {
                        f.position(0);
                        channel.write(f);
                    }
                    f.clear();
                    t = 1;
                }
                if (t > 20000) {
                    throw new IllegalStateException("Timed out");
                }
            }
            try {
                Thread.sleep(100);
            } catch (Exception x) {
                Log.c(thread, x);
            }
        }
    }

    @Override
    public void onDestroy() {
        //close();
        super.onDestroy();
    }

    public final void close() {
        try {
            running = false;
            base.close();
            channel.close();
            thread.interrupt();
        } catch (Exception e) {
            Log.c(this, e);
            E.print(this, e);
        }
    }

    public static final class StartupReceiver extends BroadcastReceiver {
        public StartupReceiver() {
        }

        @Override
        public final void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                ContextCompat.startForegroundService(context, new Intent(context, InternetProvider.class));
            }
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "InternetProvider{" +
                "handler=" + handler +
                ", thread=" + thread +
                ", base=" + base +
                ", channel=" + channel +
                ", label='" + label + '\'' +
                ", address='" + address + '\'' +
                ", route='" + route + '\'' +
                ", dnsServer1='" + dnsServer1 + '\'' +
                ", dnsServer2='" + dnsServer2 + '\'' +
                ", dnsServer3='" + dnsServer3 + '\'' +
                ", dnsServer4='" + dnsServer4 + '\'' +
                '}';
    }
}