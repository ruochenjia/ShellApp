package shell.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.mindrex.shell.R;

public final class AboutAppActivity extends ActivityFramework {
    public AboutAppActivity() {
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_app);
        final AppCompatTextView a = findViewById(R.id.appVersion);
        a.setText(versionName);
    }

    @Override
    public final void onPause() {
        getContentView().setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final void onResume() {
        super.onResume();
        getContentView().setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }


    @NonNull
    @Override
    public final String toString() {
        return "null";
    }
}