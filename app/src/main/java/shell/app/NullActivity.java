package shell.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;

@SuppressWarnings("JavaJniMissingFunction")
public final class NullActivity extends Activity {
    private Context base;

    public NullActivity(Context base) {
        attachBaseContext(base);
        this.base = base;
    }

    public static NullActivity create(Context a) {
        final NullActivity b = new NullActivity(a);
        b.onCreate(null);
        return b;
    }

    @Override
    public final void attachBaseContext(Context newBase) {
        if (base == null && newBase != null) {
            super.attachBaseContext(newBase);
            base = newBase;
        }
    }

    @Override
    public final Context getBaseContext() {
        return base;
    }

    @Override
    public final void onCreate(Bundle b) {
        synchronized (NullActivity.class) {
            super.onCreate(b);
            create();
        }
        setContentView(new NullView());
    }

    private native void create();

    @Override
    public final void onResume() {
        synchronized (NullActivity.class) {
            super.onResume();
            resume();
        }
    }

    private native void resume();

    @Override
    public final void onPause() {
        synchronized (NullActivity.class) {
            super.onPause();
            pause();
        }
    }

    private native void pause();

    @Override
    public final void onDestroy() {
        synchronized (NullActivity.class) {
            super.onDestroy();
            destroy();
        }
        System.gc();
    }

    private native void destroy();

    @Override
    public final void onBackPressed() {
    }

    private final class NullView extends View {
        public NullView() {
            super(NullActivity.this);
        }

        @Override
        public final void onDraw(Canvas canvas) {
        }
    }
}