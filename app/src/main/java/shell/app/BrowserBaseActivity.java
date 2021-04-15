package shell.app;

import android.widget.Toast;

import com.mindrex.io.CharsetV2;
import com.mindrex.io.TextReader;
import com.mindrex.io.TextWriter;
import com.mindrex.shell.R;
import com.mindrex.standardassets.Utils;

import org.json.JSONObject;

import java.io.File;

public abstract class BrowserBaseActivity extends ActivityFramework {
    protected static boolean allowJavascript = true;
    protected static boolean allowCookie = true;
    protected static boolean allowThirdPartyCookie = false;
    protected static boolean loadsImagesAutomatically = true;
    protected static boolean setSupportZoom = true;
    protected static boolean setBuiltInZoomControls = false;
    protected static boolean setDisplayZoomControls = false;
    protected static boolean loadWithOverviewMode = true;
    protected static boolean useWideViewPort = true;
    protected static boolean javaScriptCanOpenWindowsAutomatically = true;
    protected static boolean enableAppCache = true;
    protected static boolean enableDatabase = true;
    protected static boolean enableDomStorage = true;
    protected static boolean allowContentAccess = false;
    protected static boolean allowFileAccess = false;
    protected static boolean allowLocationAccess = true;
    protected static boolean allowFileAccessFromFileURLs = true;
    protected static boolean allowUniversalAccessFromFileURLs = true;
    protected static boolean blockNetworkImage = false;
    protected static boolean blockNetworkLoads = false;
    protected static boolean automaticVpn = false;
    protected static boolean blockWebViewAds = false;
    protected static boolean keepScreenOn = false;
    protected static String homePage = "https://www.google.com/";
    protected static String newTabPage = "about:blank";
    protected static Boolean showHomeButton = false;

    protected final void saveSettings() {
        /*try {
            final JSONObject a = new JSONObject();
            final JSONObject b = new JSONObject();
            final JSONObject c = new JSONObject();
            a.put("allow_javascript", allowJavascript)
                    .put("allow_cookies", allowCookie)
                    .put("allow_third_party_cookies", allowThirdPartyCookie)
                    .put("load_images_automatically", loadsImagesAutomatically)
                    .put("set_support_zoom", setSupportZoom)
                    .put("set_built_in_zoom_controls", setBuiltInZoomControls)
                    .put("set_display_zoom_controls", setDisplayZoomControls)
                    .put("load_with_overview_mode", loadWithOverviewMode)
                    .put("use_wide_view_port", useWideViewPort)
                    .put("javascript_can_open_window_automatically", javaScriptCanOpenWindowsAutomatically)
                    .put("enable_dom_storage", enableDomStorage)
                    .put("enable_app_cache", enableAppCache)
                    .put("enable_database", enableDatabase)
                    .put("allow_content_access", allowContentAccess)
                    .put("allow_file_access", allowFileAccess)
                    .put("allow_location_access", allowLocationAccess)
                    .put("allow_file_access_from_file_urls", allowFileAccessFromFileURLs)
                    .put("allow_universal_access_from_file_urls", allowUniversalAccessFromFileURLs)
                    .put("block_network_image", blockNetworkImage)
                    .put("block_network_loads", blockNetworkLoads)
                    .put("aps", automaticVpn)
                    .put("block_ads", blockWebViewAds)
                    .put("keep_screen_on", keepScreenOn)
                    .put("home_page", homePage)
                    .put("new_tab_page", newTabPage)
                    .put("download_folder", downloadFolderPath)
                    .put("show_home_button", showHomeButton);
            c.put("app_name", appName)
                    .put("version_name", versionName)
                    .put("package_name", packageName)
                    .put("deprecated_launcher_icon", 1)
                    .put("version_code", versionCode);
            b.put("format_version", versionCode)
                    .put("app_info", c)
                    .put("options", a);
            new TextWriter(new File(applicationPath, "html_viewer_settings")).write(b.toString(2), CharsetV2.ASCII);
        } catch (Exception e) {
            Toast.makeText(this, R.string.config_saving_fail, Toast.LENGTH_SHORT).show();
        }*/
    }

    protected final void loadSettings() {
       /* try {
            final JSONObject b = new JSONObject(Utils.joinStringLines(new TextReader(new File(applicationPath, "html_viewer_settings")).readLines(CharsetV2.ASCII)));
            final JSONObject a = (JSONObject) b.get("options");
            if ((int) b.get("format_version") == versionCode) {
                allowJavascript = a.get("allow_javascript").toString().equals("true");
                allowCookie = a.get("allow_cookies").toString().equals("true");
                allowThirdPartyCookie = a.get("allow_third_party_cookies").toString().equals("true");
                loadsImagesAutomatically = a.get("load_images_automatically").toString().equals("true");
                setSupportZoom = a.get("set_support_zoom").toString().equals("true");
                setBuiltInZoomControls = a.get("set_built_in_zoom_controls").toString().equals("true");
                setDisplayZoomControls = a.get("set_display_zoom_controls").toString().equals("true");
                loadWithOverviewMode = a.get("load_with_overview_mode").toString().equals("true");
                useWideViewPort = a.get("use_wide_view_port").toString().equals("true");
                javaScriptCanOpenWindowsAutomatically = a.get("javascript_can_open_window_automatically").toString().equals("true");
                enableDomStorage = a.get("enable_dom_storage").toString().equals("true");
                enableAppCache = a.get("enable_app_cache").toString().equals("true");
                enableDatabase = a.get("enable_database").toString().equals("true");
                allowContentAccess = a.get("allow_content_access").toString().equals("true");
                allowFileAccess = a.get("allow_file_access").toString().equals("true");
                allowLocationAccess = a.get("allow_location_access").toString().equals("true");
                allowFileAccessFromFileURLs = a.get("allow_file_access_from_file_urls").toString().equals("true");
                allowUniversalAccessFromFileURLs = a.get("allow_universal_access_from_file_urls").toString().equals("true");
                blockNetworkImage = a.get("block_network_image").toString().equals("true");
                blockNetworkLoads = a.get("block_network_loads").toString().equals("true");
                homePage = a.get("home_page").toString();
                newTabPage = a.get("new_tab_page").toString();
                downloadFolderPath = a.get("download_folder").toString();
                automaticVpn = a.get("aps").toString().equals("true");
                blockWebViewAds = a.get("block_ads").toString().equals("true");
                keepScreenOn = a.get("keep_screen_on").toString().equals("true");
                showHomeButton = a.get("show_home_button").toString().equals("true");
            } else saveSettings();
        } catch (Exception e) {
            saveSettings();
            Toast.makeText(this, R.string.config_invalid_layout, Toast.LENGTH_SHORT).show();
        }*/
    }
}