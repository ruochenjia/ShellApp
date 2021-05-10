package shell.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.mindrex.shell.BuildConfig;
import com.mindrex.shell.R;
import com.topjohnwu.superuser.Shell;

public final class LaunchActivity extends ActivityFramework {
    public LaunchActivity() {
    }

    @Override
    public final void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.splash);
    }

    @Override
    public final void onResume() {
        super.onResume();
        final Animation a = new AlphaAnimation(0, 1);
        final int i = 3000;
        a.setFillAfter(true);
        a.setDuration(i);
        getContentView().startAnimation(a);
        handler.postDelayed(this, i);
    }

    @Override
    public final void run() {
        Shell.getShell(shell -> {
            final Intent a = new Intent(this, MainActivity.class);
            a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            finish();
        });
    }
}