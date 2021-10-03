package shell.app;

import android.content.Intent;

import com.mindrex.android.appsupport.SupportService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class RestartAppService extends SupportService {
    public RestartAppService() {
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(() -> {
            final Intent a = getPackageManager().getLaunchIntentForPackage(getPackageName());
            a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            stopSelf();
        }, 1000);
        return 1;
    }
}