package shell.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.mindrex.shell.BuildConfig;
import com.mindrex.shell.R;
import com.topjohnwu.superuser.Shell;

public final class LaunchActivity extends ActivityFramework {
    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        );
    }

    public LaunchActivity() {
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    @Override
    public final String toString() {
        return "null";
    }
}