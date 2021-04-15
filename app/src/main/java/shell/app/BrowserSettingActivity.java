package shell.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.mindrex.shell.R;
import com.mindrex.util.Sfx;

import java.util.Objects;

public final class BrowserSettingActivity extends BrowserBaseActivity {
    public BrowserSettingActivity() {
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Sfx.nonnull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final PreferenceFragment a = new PreferenceFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.settingLayout, a).commit();
    }

    @Override
    public final void onResume() {
        super.onResume();
        getContentView().setVisibility(View.VISIBLE);
    }

    @Override
    public final void onBackPressed() {
        saveSettings();
        finish();
    }

    @Override
    public final void onDestroy() {
        saveSettings();
        super.onDestroy();
    }

    @Override
    public final void onPause() {
        saveSettings();
        getContentView().setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public final boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    public static final class PreferenceFragment extends PreferenceFragmentCompat {
        private PreferenceFragment() {
        }

        @Override
        public final void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public final void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.browser_settings, rootKey);
            final SwitchPreference allowJavascript = findPreference("javascriptSwitch");
            final SwitchPreference loadsImagesAutomatically = findPreference("loadsImagesAutomatically");
            final SwitchPreference allowContentAccess = findPreference("allowContentAccess");
            final SwitchPreference allowFileAccess = findPreference("allowFileAccess");
            final SwitchPreference allowLocationAccess = findPreference("allowLocationAccess");
            final SwitchPreference enableAppCache = findPreference("enableAppCache");
            final SwitchPreference enableDomStorage = findPreference("enableDomStorage");
            final SwitchPreference setBuiltInZoomControls = findPreference("setBuiltInZoomControls");
            final SwitchPreference setDisplayZoomControls = findPreference("setDisplayZoomControls");
            final SwitchPreference loadWithOverviewMode = findPreference("loadWithOverviewMode");
            final SwitchPreference useWideViewPort = findPreference("useWideViewPort");
            final SwitchPreference allowCookies = findPreference("cookieSwitch");
            final SwitchPreference allowThirdPartyCookies = findPreference("thirdPartyCookieSwitch");
            final SwitchPreference automaticVpn = findPreference("automaticVpn");
            final SwitchPreference blockWebViewAds = findPreference("blockWebViewAds");
            final SwitchPreference keepScreenOn = findPreference("keepScreenOn");
            final Preference homePageUrl = findPreference("homePageUrl");
            final Preference appStartPageUrl = findPreference("appStartPageUrl");
            final Preference downloadFolder = findPreference("downloadFolder");
            final Preference clearCookies = findPreference("clearCookies");
            final Preference clearBrowsingData = findPreference("clearBrowsingData");
            final Preference clearAppCache = findPreference("clearAppCache");
            final Preference clearAppData = findPreference("clearAppData");
            final Preference additionalFunctions = findPreference("additionalFunctions");
            final Preference about = findPreference("about");

            allowJavascript.setChecked(BrowserActivity.allowJavascript);
            loadsImagesAutomatically.setChecked(BrowserActivity.loadsImagesAutomatically);
            allowContentAccess.setChecked(BrowserActivity.allowContentAccess);
            allowFileAccess.setChecked(BrowserActivity.allowFileAccess);
            allowLocationAccess.setChecked(BrowserActivity.allowLocationAccess);
            enableAppCache.setChecked(BrowserActivity.enableAppCache);
            enableDomStorage.setChecked(BrowserActivity.enableDomStorage);
            setBuiltInZoomControls.setChecked(BrowserActivity.setBuiltInZoomControls);
            setDisplayZoomControls.setChecked(BrowserActivity.setDisplayZoomControls);
            loadWithOverviewMode.setChecked(BrowserActivity.loadWithOverviewMode);
            useWideViewPort.setChecked(BrowserActivity.useWideViewPort);
            allowCookies.setChecked(BrowserActivity.allowCookie);
            allowThirdPartyCookies.setChecked(BrowserActivity.allowThirdPartyCookie);
            automaticVpn.setChecked(BrowserActivity.automaticVpn);
            blockWebViewAds.setChecked(BrowserActivity.blockWebViewAds);
            homePageUrl.setSummary(BrowserActivity.homePage);
            appStartPageUrl.setSummary(BrowserActivity.newTabPage);
            downloadFolder.setSummary(downloadFolderPath);

            allowJavascript.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.allowJavascript = (Boolean) newValue;
                return true;
            });
            loadsImagesAutomatically.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.loadsImagesAutomatically = (Boolean) newValue;
                return true;
            });
            allowContentAccess.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.allowContentAccess = (Boolean) newValue;
                return true;
            });
            allowFileAccess.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.allowFileAccess = (Boolean) newValue;
                return true;
            });
            allowLocationAccess.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.allowLocationAccess = (Boolean) newValue;
                return true;
            });
            enableAppCache.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.enableAppCache = (Boolean) newValue;
                return true;
            });
            enableDomStorage.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.enableDomStorage = (Boolean) newValue;
                return true;
            });
            setBuiltInZoomControls.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.setBuiltInZoomControls = (Boolean) newValue;
                return true;
            });
            setDisplayZoomControls.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.setDisplayZoomControls = (Boolean) newValue;
                return true;
            });
            loadWithOverviewMode.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.loadWithOverviewMode = (Boolean) newValue;
                return true;
            });
            useWideViewPort.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.useWideViewPort = (Boolean) newValue;
                return true;
            });
            allowCookies.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.allowCookie = (Boolean) newValue;
                return true;
            });
            allowThirdPartyCookies.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.allowThirdPartyCookie = (Boolean) newValue;
                return true;
            });
            automaticVpn.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.automaticVpn = (Boolean) newValue;
                return true;
            });
            blockWebViewAds.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.blockWebViewAds = (Boolean) newValue;
                return true;
            });
            keepScreenOn.setOnPreferenceChangeListener((preference, newValue) -> {
                BrowserActivity.keepScreenOn = (Boolean) newValue;
                return true;
            });
            homePageUrl.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder a = new AlertDialog.Builder(preference.getContext());
                a.setTitle(R.string.set_home_page_url);
                final AppCompatEditText b = new AppCompatEditText(preference.getContext());
                b.setInputType(InputType.TYPE_CLASS_TEXT);
                b.setHint(R.string.change_home_page_url);
                b.setText(BrowserActivity.homePage);
                a.setView(b);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                    BrowserActivity.homePage = Objects.requireNonNull(b.getText()).toString();
                    homePageUrl.setSummary(BrowserActivity.homePage);
                });
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            });
            appStartPageUrl.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder a = new AlertDialog.Builder(preference.getContext());
                a.setTitle(R.string.set_app_start_page_url);
                final AppCompatEditText b = new AppCompatEditText(preference.getContext());
                b.setInputType(InputType.TYPE_CLASS_TEXT);
                b.setHint(R.string.change_app_start_page_url);
                b.setText(BrowserBaseActivity.newTabPage);
                a.setView(b);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                    BrowserBaseActivity.newTabPage = Objects.requireNonNull(b.getText()).toString();
                    appStartPageUrl.setSummary(BrowserSettingActivity.newTabPage);
                });
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            });
            downloadFolder.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder a = new AlertDialog.Builder(preference.getContext());
                a.setTitle(R.string.set_download_folder);
                final AppCompatEditText b = new AppCompatEditText(preference.getContext());
                b.setInputType(InputType.TYPE_CLASS_TEXT);
                b.setHint(R.string.change_download_folder);
                b.setText(downloadFolderPath);
                a.setView(b);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> {
                    downloadFolderPath = Objects.requireNonNull(b.getText()).toString();
                    downloadFolder.setSummary(downloadFolderPath);
                });
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            });
            clearCookies.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder a = new AlertDialog.Builder(preference.getContext());
                a.setTitle(R.string.clear_cookies);
                a.setMessage(R.string.confirm_clear_cookies);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> clearCookies(getContext()));
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            });
            clearBrowsingData.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder a = new AlertDialog.Builder(preference.getContext());
                a.setTitle(R.string.clear_browsing_data);
                a.setMessage(R.string.confirm_clear_browsing_data);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> clearWebViewData(getContext()));
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            });
            clearAppCache.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder a = new AlertDialog.Builder(preference.getContext());
                a.setTitle(R.string.clear_app_cache);
                a.setMessage(R.string.confirm_clear_app_cache);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> clearAppCache(getContext()));
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            });
            clearAppData.setOnPreferenceClickListener(preference -> {
                final AlertDialog.Builder a = new AlertDialog.Builder(preference.getContext());
                a.setTitle(R.string.clear_app_data);
                a.setMessage(R.string.confirm_clear_app_data);
                a.setPositiveButton(R.string.selection_confirm, (dialog, which) -> clearAppData(getContext()));
                a.setNegativeButton(R.string.selection_cancel, (dialog, which) -> dialog.cancel());
                a.show();
                return true;
            });
            additionalFunctions.setOnPreferenceClickListener(preference -> {
                throw  null;
            });
            about.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), AboutAppActivity.class));
                return true;
            });
        }


        public final <T extends Preference> T findPreference(String s) {
            return Sfx.nonnull(super.findPreference(s));
        }

        @NonNull
        @Override
        public final String toString() {
            return "null";
        }
    }

    @NonNull
    @Override
    public final String toString() {
        return "null";
    }
}