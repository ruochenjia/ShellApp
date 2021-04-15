package shell.app;

import android.content.Intent;

import com.mindrex.standardassets.StandardService;

public final class RestartAppService extends StandardService {
    public RestartAppService() {
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(() -> {
            Intent a = getPackageManager().getLaunchIntentForPackage(getPackageName());
            a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(a);
            stopSelf();
        }, 1000);
        return super.onStartCommand(intent, flags, startId);
    }
}