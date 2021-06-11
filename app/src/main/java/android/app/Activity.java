package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;

public abstract class Activity extends ContextThemeWrapper implements Handler.Callback {
    private Activity base;

    protected Activity(Activity base) {
        super(base, base == null ? null : base.getTheme());
        this.base = base;
    }

    public void attachBaseContext(Context base) {
        if (this.base != null)
            base = this.base;
        else attachBaseActivity(getBase());
        super.attachBaseContext(base);
    }

    public void attachBaseActivity(Activity base) {
        if (this.base == null)
            this.base = base;
    }

    public abstract Activity getBase();

    @Override
    public Context getBaseContext() {
        if (base == null)
            attachBaseActivity(getBase());
        return base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public abstract void onCreate(Bundle b) throws Exception;

    public abstract void onPause() throws Exception;

    public abstract void onResume() throws Exception;

    public abstract void onDestroy() throws Exception;

    public void finish() {
        getBase().finish();
    }

    public boolean isTaskRoot() {
        return getBase().isTaskRoot();
    }

    public Window getWindow() {
        return getBase().getWindow();
    }

    public Resources getResources() {
        return getBase().getResources();
    }

    public void setContentView(View view) {
        setContentView(view, null);
    }

    public void setContentView(int layoutResID) {
        setContentView(LayoutInflater.from(getBase()).inflate(layoutResID, null, false));
    }

    public void setRequestedOrientation(int i) {
        getBase().setRequestedOrientation(i);
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (params == null)
            getBase().setContentView(view);
        else getBase().setContentView(view, params);
    }

    public abstract View getContentView();

    public int getRequestedOrientation() {
        return getBase().getRequestedOrientation();
    }

    public Intent getIntent() {
        return getBase().getIntent();
    }

    public void onSaveInstanceState(Bundle b) {
        getBase().onSaveInstanceState(b);
    }

    public void onConfigurationChanged(Configuration cfg) {
        getBase().onConfigurationChanged(cfg);
    }

    public void onRestoreInstanceState(Bundle b) {
        getBase().onRestoreInstanceState(b);
    }

    public boolean onCreateOptionsMenu(Menu m) {
        return getBase().onCreateOptionsMenu(m);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return getBase().onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        getBase().onBackPressed();
    }

    public boolean equals(Object a) {
        return false;
    }

    @NonNull
    public abstract Object clone();

    public abstract int hashCode();

    @NonNull
    public abstract String toString();
}