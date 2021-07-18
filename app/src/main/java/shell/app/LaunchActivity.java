package shell.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.mindrex.shell.R;
import com.topjohnwu.superuser.Shell;

import javafxports.android.FXFragment;

public final class LaunchActivity extends ActivityFramework {
    public LaunchActivity() {
    }

    @Override
    public final void onCreate(Bundle b) {
        super.onCreate(b);
        //setContentView(R.layout.splash);
        final RelativeLayout a = new RelativeLayout(this, null, 0);
        a.setId(R.id.a);
        setContentView(a);
        if (b == null)
            getFragmentManager().beginTransaction()
                    .add(R.id.a, new Fragment())
                    .commit();
    }

    public static final class Fragment extends FXFragment {
        public Fragment() {
            setName("shell.app.jfx.Test");
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
//        final Animation a = new AlphaAnimation(0, 1);
//        final int i = 3000;
//        a.setFillAfter(true);
//        a.setDuration(i);
//        getContentView().startAnimation(a);
        handler.postDelayed(this, 5000);
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