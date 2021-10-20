package shell.app;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.android.appsupport.CommandUtils;
import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.io.File;
import com.mindrex.io.IOUtils;
import com.mindrex.shell.R;
import com.mindrex.util.Util;

import java.io.InputStream;
import java.io.OutputStream;

public final class ApkInstaller extends ActivityFramework {
    private Uri stream;
    private File apkFile;
    private String errCode;
    private Exception parseException;
    private CharSequence packageName;
    private CharSequence version;
    private CharSequence label;
    private Drawable icon;
    private AppCompatButton cancel;
    private AppCompatButton install;
    private AppCompatButton close;
    private AppCompatButton open;
    private AppCompatTextView textView;
    private AppCompatImageView imageView;

    public ApkInstaller() {
        super(R.layout.apk_installer);
    }

    @Override
    public final void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        cancel = findViewById(R.id.cancel);
        install = findViewById(R.id.install);
        close = findViewById(R.id.close);
        open = findViewById(R.id.open);
        textView = new AppCompatTextView(this);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        textView.setTextColor(Res.color(R.color.contents));
        textView.setText(R.string.preparing);
        imageView = new AppCompatImageView(this);
        imageView.setContentDescription(textView.getText());
        imageView.setId(R.id.a);
        imageView.setImageDrawable(Res.drawable(R.drawable.reload, getTheme()));
        final ViewGroup c = findViewById(R.id.customLayout);
        c.addView(imageView, 0, new ViewGroup.LayoutParams(Utils.dpToPixel(50), Utils.dpToPixel(50)));
        c.addView(textView, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        cancel.setOnClickListener(v -> {
            handler.removeCallbacksAndMessages(null);
            finish();
        });
        install.setOnClickListener(v -> {
            v.setVisibility(View.GONE);
            textView.setText(R.string.installing);
            imageView.setVisibility(View.VISIBLE);
            new Thread(this).start();
        });
        close.setOnClickListener(v -> finish());
        install.setEnabled(false);
        new Thread(this::parseApk).start();
    }

    private void parseApk() {
        try {
            Util.nonnull(stream = getStream());
            final File a = new File((File) getCacheDir(), "tmp.apk");
            IOUtils.transferStream(getContentResolver().openInputStream(stream), a.getOutputStream(false));
            apkFile = a;
            final PackageManager b = getPackageManager();
            final PackageInfo c = b.getPackageArchiveInfo(a.getPath(), 0);
            packageName = c.packageName;
            version = new StringBuilder(c.versionName).append(" (").append(c.versionCode).append(")");
            label = b.getApplicationLabel(c.applicationInfo);
            icon = c.applicationInfo.loadIcon(b);
        } catch (Exception e) {
            parseException = e;
        }
        handler.sendEmptyMessage(3);
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case 0:
                textView.setText(R.string.apk_installed);
                imageView.setImageDrawable(Res.drawable(R.drawable.tick, getTheme()));
                cancel.setVisibility(View.GONE);
                open.setVisibility(View.VISIBLE);
                close.setVisibility(View.VISIBLE);
                return true;
            case 1:
                textView.setText(Res.string(R.string.apk_install_failed).replace("$err_code", errCode));
                imageView.setImageDrawable(Res.drawable(R.drawable.clear, getTheme()));
                cancel.setVisibility(View.GONE);
                close.setVisibility(View.VISIBLE);
                return true;
            case 2:
                finish();
                return true;
            case 3:
                if (parseException == null) {
                    ((AppCompatTextView) findViewById(R.id.packageName)).setText(packageName);
                    ((AppCompatTextView) findViewById(R.id.apkVersion)).setText(version);
                    ((AppCompatTextView) findViewById(R.id.apkName)).setText(label);
                    ((AppCompatImageView) findViewById(R.id.apkIcon)).setImageDrawable(icon);
                    textView.setText(R.string.install_confirm_message);
                    imageView.setVisibility(View.GONE);
                    install.setEnabled(true);
                } else
                    E.alert(this, Res.string(R.string.resolve_intent_failed), parseException);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void run() {
        try {
            if (CommandUtils.hasRootAccess())
                CommandUtils.execWithException("pm install " + apkFile.getPath());
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                installPackage(apkFile.getInputStream());
            else {
                final Intent a = new Intent(Intent.ACTION_VIEW);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                a.setDataAndType(stream, "application/vnd.android.package-archive");
                a.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(a);
                handler.sendEmptyMessage(2);
                return;
            }
        } catch (Exception e) {
            String a = e.getMessage();
            if (a == null)
                a = e.toString();
            errCode = a;
            handler.sendEmptyMessage(1);
            return;
        }
        handler.sendEmptyMessage(0);
    }

    private Uri getStream() {
        final Intent a = getIntent();
        if (Intent.ACTION_VIEW.equals(a.getAction()))
            return a.getData();
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void installPackage(InputStream i) throws Exception {
        final PackageInstaller a = getPackageManager().getPackageInstaller();
        final int b = a.createSession(new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
        final PackageInstaller.Session c = a.openSession(b);
        final OutputStream o = c.openWrite("aaa", 0, -1);
        IOUtils.transferStream(i, o);
        c.fsync(o);
        i.close();
        o.close();
        PendingIntent d = PendingIntent.getBroadcast(this, b, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        c.commit(d.getIntentSender());
        c.close();
    }
}