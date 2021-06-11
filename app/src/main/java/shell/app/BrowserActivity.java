package shell.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mindrex.android.appsupport.htmlviewer.HtmlViewer;
import com.mindrex.shell.R;

@Deprecated
public final class BrowserActivity extends BrowserBaseActivity {
    private boolean doubleBackToExitPressedOnce = false;
    private HtmlViewer viewer;

    @Override
    public final void onCreate(Bundle a) {
        super.onCreate(a);
        viewer = new HtmlViewer(this, null);
        setContentView(viewer);
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
                viewer.goBack();
                return true;
            case R.id.itemNext:
                viewer.goForward();
                return true;
            case R.id.reload:
                viewer.reload();
                return true;
            case R.id.home:
                viewer.home();
                return true;
            case R.id.settings:
                viewer.openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(a);
        }
    }

    @Override
    public final void onBackPressed() {
        viewer.goBack();
        if (doubleBackToExitPressedOnce)
            finish();
        else {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.back_key_exit_app_notice, Toast.LENGTH_SHORT).show();
            handler.postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    @Override
    public final void onPause() {
        getContentView().setVisibility(View.GONE);
        super.onPause();
    }
}