package shell.app;

import android.content.Intent;

import com.mindrex.android.appsupport.StandardService;

public final class RestartAppService extends StandardService {
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
        return super.onStartCommand(intent, flags, startId);
    }
}