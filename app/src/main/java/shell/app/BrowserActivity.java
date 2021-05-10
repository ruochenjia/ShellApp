package shell.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mindrex.shell.R;

public final class BrowserActivity extends BrowserBaseActivity {
    private boolean pageInitialized = false;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean allowRefreshWebViewContent = false;

    private HtmlViewer viewer;

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            viewer = new HtmlViewer(this, null, 0);
            setContentView(viewer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        getContentView().setVisibility(View.VISIBLE);
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_browser, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem a) {
        switch (a.getItemId()) {
            case R.id.itemBack:
                if (viewer.canGoBack()) viewer.goBack();
                return true;
            case R.id.itemNext:
                if (viewer.canGoForward()) viewer.goForward();
                return true;
            case R.id.reload:
                viewer.reload();
                return true;
            case R.id.home:
                viewer.toHome();
                return true;
            case R.id.settings:
                startActivity(new Intent(BrowserActivity.this, BrowserSettingActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(a);
        }
    }

    @Override
    public final void onBackPressed() {
        if (viewer.canGoBack())
            viewer.goBack();
        else {
            if (doubleBackToExitPressedOnce)
                finish();
            else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, R.string.back_key_exit_app_notice, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        }
    }

    @Override
    public final void onPause() {
        getContentView().setVisibility(View.GONE);
        super.onPause();
    }

    @Override
    public String toString() {
        return "BrowserActivity{" +
                "pageInitialized=" + pageInitialized +
                ", doubleBackToExitPressedOnce=" + doubleBackToExitPressedOnce +
                ", allowRefreshWebViewContent=" + allowRefreshWebViewContent +
                ", viewer=" + viewer +
                '}';
    }
}