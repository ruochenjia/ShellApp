package shell.app;

import static com.mindrex.android.appsupport.StandardActivity.FULL_SCREEN_SETTING;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mindrex.data.Array;
import com.mindrex.io.TextReader;
import com.mindrex.shell.R;
import com.mindrex.android.appsupport.E;
import com.mindrex.android.appsupport.LinearLayout;
import com.mindrex.android.appsupport.StandardApplication;
import com.mindrex.android.appsupport.Utils;
import com.mindrex.util.Configuration;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class HtmlViewer extends LinearLayout {
    private final ProgressBar progressBar;
    private final AppCompatImageView securityIcon;
    private final AppCompatEditText addressBar;
    private final AppCompatImageButton searchButton;
    private final WebView webView;
    private final Configuration config;

    private boolean isLoading;
    private boolean pageInitialized;

    private final boolean adBlockEnabled;
    private final boolean allowCookies;
    private final boolean allowThirdPartyCookies;
    private final String homePage;
    private final String searchEngineUrl;

    static {
        if (true) throw null;
    }

    {
        isLoading = false;
        pageInitialized = false;
    }

    public HtmlViewer(Context context, AttributeSet attrs, int defStyleAttr) throws Exception {
        super(context, attrs, defStyleAttr);
        this.config = getConfiguration("HtmlViewerConfig");
        this.adBlockEnabled = config.getBoolean("ad_block_enabled", false);
        this.allowCookies = config.getBoolean("allow_cookies", true);
        this.allowThirdPartyCookies = config.getBoolean("allow_third_party_cookies", false);
        this.homePage = (String) config.getString("home_page", "about:blank");
        this.searchEngineUrl = (String) config.getString("search_engine_url", "https://www.google.com/search?q=$search");
        setOrientation(VERTICAL);
        setKeepScreenOn(config.getBoolean("keep_screen_on", false));
        final TypedValue t = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, t, true);
        final LinearLayout a = new LinearLayout(context, attrs, defStyleAttr);
        a.setOrientation(VERTICAL);
        this.progressBar = new ProgressBar(context, attrs, R.style.Widget_AppCompat_ProgressBar_Horizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(GONE);
        a.addView(progressBar, 0, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPixel(8)));
        final RelativeLayout b = new RelativeLayout(context, attrs, defStyleAttr);
        this.securityIcon = new AppCompatImageView(context, attrs, defStyleAttr);
        securityIcon.setVisibility(INVISIBLE);
        securityIcon.setId(R.id.a);
        securityIcon.setContentDescription(resources.getString(R.string.security_icon));
        RelativeLayout.LayoutParams c = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        c.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        securityIcon.setLayoutParams(c);
        this.searchButton = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        searchButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.search_arrow));
        searchButton.setId(R.id.c);
        searchButton.setContentDescription(resources.getString(R.string.search));
        c = new RelativeLayout.LayoutParams(Utils.dpToPixel(50), ViewGroup.LayoutParams.MATCH_PARENT);
        c.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        searchButton.setLayoutParams(c);
        this.addressBar = new AppCompatEditText(context, attrs, defStyleAttr);
        searchButton.setOnClickListener(view -> querySearch(addressBar.getText()));
        addressBar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        addressBar.setHint(R.string.address_bar_hint);
        addressBar.setId(R.id.b);
        c = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        c.addRule(RelativeLayout.LEFT_OF, R.id.c);
        c.addRule(RelativeLayout.RIGHT_OF, R.id.a);
        addressBar.setLayoutParams(c);
        addressBar.setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                searchButton.callOnClick();
                return true;
            } else return false;
        });
        final AppCompatImageButton d = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        d.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.clear));
        d.setContentDescription(resources.getString(R.string.clear));
        d.setOnClickListener(view -> addressBar.setText(""));
        c = new RelativeLayout.LayoutParams(Utils.dpToPixel(50), ViewGroup.LayoutParams.MATCH_PARENT);
        c.addRule(RelativeLayout.LEFT_OF, R.id.c);
        d.setLayoutParams(c);
        b.addView(securityIcon, 0);
        b.addView(addressBar, 1);
        b.addView(searchButton, 2);
        b.addView(d, 3);
        a.addView(b, 1, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addViewInLayout(a, 0, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPixel(50), 1));
        this.webView = new WebView(context, attrs);
        final SwipeRefreshLayout e = new SwipeRefreshLayout(context, attrs);
        e.addView(webView, 0, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        e.setOnRefreshListener(() -> {
            webView.reload();
            handler.postDelayed(() -> e.setRefreshing(false), 500);
        });
        addViewInLayout(e, 1, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 13));
        final LinearLayout f = new LinearLayout(context, attrs, defStyleAttr);
        f.setOrientation(HORIZONTAL);
        AppCompatImageButton g = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        g.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_back));
        g.setContentDescription(resources.getString(R.string.back));
        g.setOnClickListener(view -> {
            if (webView.canGoBack()) webView.goBack();
            else
                Toast.makeText(context, R.string.cannot_go_back_message, Toast.LENGTH_SHORT).show();
        });
        g.setOnLongClickListener(Utils::toastContentDescription);
        f.addView(g, 0, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        g = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        g.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.arrow_next));
        g.setContentDescription(resources.getString(R.string.back));
        g.setOnClickListener(view -> {
            if (webView.canGoForward()) webView.goForward();
            else
                Toast.makeText(context, R.string.cannot_go_forward_message, Toast.LENGTH_SHORT).show();
        });
        g.setOnLongClickListener(Utils::toastContentDescription);
        f.addView(g, 1, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        g = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        g.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.reload));
        g.setContentDescription(resources.getString(R.string.reload));
        g.setOnClickListener(view -> {
            if (isLoading) webView.stopLoading();
            else webView.reload();
        });
        g.setOnLongClickListener(Utils::toastContentDescription);
        f.addView(g, 2, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        g = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        g.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home));
        g.setContentDescription(resources.getString(R.string.home));
        g.setOnClickListener(view -> webView.loadUrl(homePage));
        g.setOnLongClickListener(Utils::toastContentDescription);
        f.addView(g, 3, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        g = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        g.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bookmark));
        g.setContentDescription(resources.getString(R.string.bookmarks));
        g.setOnClickListener(view -> context.startActivity(new Intent(context, BrowserBookmarkActivity.class)));
        g.setOnLongClickListener(Utils::toastContentDescription);
        f.addView(g, 4, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        g = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        g.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.tab));
        g.setContentDescription(resources.getString(R.string.tabs));
        g.setOnClickListener(view -> {
        });
        g.setOnLongClickListener(Utils::toastContentDescription);
        f.addView(g, 5, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        g = new AppCompatImageButton(context, attrs, R.style.Widget_AppCompat_Button_Borderless);
        g.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.settings));
        g.setContentDescription(resources.getString(R.string.settings));
        g.setOnClickListener(view -> context.startActivity(new Intent(context, BrowserSettingActivity.class)));
        g.setOnLongClickListener(Utils::toastContentDescription);
        f.addView(g, 6, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        addViewInLayout(f, 2, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPixel(50)));
    }

    @Override
    protected final void onDraw(Canvas canvas) {
        if (!pageInitialized) {
            if (adBlockEnabled) AdBlocker.init(resources, handler);
            initializeWebView();
            pageInitialized = true;
        }
        super.onDraw(canvas);
    }

    @Override
    public final boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }


    private String parseHostName(String url) {
        if (url.startsWith("https://")) url = url.replace("https://", "");
        else if (url.startsWith("http://")) url = url.replace("http://", "");
        else if (url.contains("://")) url = url.substring(url.indexOf("://") + 3);
        return url;
    }

    private void querySearch(CharSequence s) {
        if (s != null) webView.loadUrl(fixUrl(s.toString()));
    }

    private String fixUrl(String s) {
        if (s.contains("\u0020")) s = searchEngineUrl.replace("$search", s);
        else if (!s.contains("://")) {
            if (s.contains(".") || s.endsWith("/") || s.equals("localhost")) s = "http://" + s;
            else if (!s.startsWith("about:") || !s.startsWith("javascript:"))
                s = searchEngineUrl.replace("$search", s);
        }
        return s;
    }

    public final WebView getWebView() {
        return webView;
    }

    public final WebSettings getWebSettings() {
        return getWebView().getSettings();
    }

    public final boolean canGoBack() {
        return webView.canGoBack();
    }

    public final void goBack() {
        webView.goBack();
    }

    public final boolean canGoForward() {
        return webView.canGoForward();
    }

    public final void goForward() {
        webView.goForward();
    }

    public final void reload() {
        webView.reload();
    }

    public final void toHome() {
        webView.loadUrl(homePage);
    }

    public final void updateWebSettings(WebSettings s) {
        if (s == null) s = getWebSettings();
        s.setAppCacheEnabled(config.getBoolean("enable_app_cache", true));
        s.setDatabaseEnabled(config.getBoolean("enable_database", true));
        s.setJavaScriptEnabled(config.getBoolean("enable_javascript", true));
        s.setDomStorageEnabled(config.getBoolean("enable_dom_storage", true));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            s.setSafeBrowsingEnabled(config.getBoolean("enable_safe_browsing", true));
        s.setSupportZoom(config.getBoolean("set_support_zoom", true));
        s.setBuiltInZoomControls(config.getBoolean("enable_built_in_zoom_controls", false));
        s.setDisplayZoomControls(config.getBoolean("display_zoom_controls", false));
        s.setLoadsImagesAutomatically(config.getBoolean("load_images_automatically", true));
        s.setUseWideViewPort(config.getBoolean("use_wide_view_port", true));
        s.setAllowContentAccess(config.getBoolean("allow_content_access", false));
        s.setAllowFileAccess(config.getBoolean("allow_file_access", false));
        s.setGeolocationEnabled(config.getBoolean("enable_geolocation", false));
        s.setBlockNetworkImage(config.getBoolean("block_network_image", false));
        s.setBlockNetworkLoads(config.getBoolean("block_network_loads", false));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if ((resources.getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                s.setForceDark(WebSettings.FORCE_DARK_ON);
            else s.setForceDark(WebSettings.FORCE_DARK_OFF);
        }
    }

    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
        try {
            final DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            r.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));
            r.addRequestHeader("User-Agent", userAgent);
            r.setDescription(resources.getString(R.string.download_file_notification));
            r.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
            r.allowScanningByMediaScanner();
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(r);
            Toast.makeText(context, R.string.download_start_notice, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            E.print(context, e);
        }
    }

    private void initializeWebView() {
        updateWebSettings(webView.getSettings());
        webView.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient() {
            private final Map<String, Boolean> loadedUrls = new HashMap<>();

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public final boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrlLoading(view, request.getUrl().toString());
            }

            @Override
            public final boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                CookieManager.getInstance().setAcceptCookie(allowCookies);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(view, allowThirdPartyCookies);
                return true;
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public final WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }

            @Override
            public final WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean b;
                if (!loadedUrls.containsKey(url)) {
                    b = AdBlocker.isAd(url);
                    loadedUrls.put(url, b);
                } else {
                    final Boolean c = loadedUrls.get(url);
                    b = c != null && c;
                }
                return b ? AdBlocker.createEmptyResource() : null;
            }

            @Override
            public final void onPageStarted(WebView view, String url, Bitmap favicon) {
                addressBar.setText(url);
                if (!StandardApplication.isNetworkAvailable(view.getContext())) {
                    view.stopLoading();
                    Toast.makeText(view.getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
                isLoading = true;
            }

            @Override
            public final void onPageFinished(WebView view, String url) {
                addressBar.setText(url);
                if (StandardApplication.isNetworkAvailable(view.getContext()))
                    if (url.contains("https://")) {
                        securityIcon.setImageResource(R.drawable.connection_safe);
                        securityIcon.setVisibility(View.VISIBLE);
                    } else if (url.contains("http://")) {
                        securityIcon.setImageResource(R.drawable.connection_danger);
                        securityIcon.setVisibility(View.VISIBLE);
                    } else securityIcon.setVisibility(View.INVISIBLE);
                else securityIcon.setVisibility(View.INVISIBLE);
                isLoading = false;
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public final void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
            }

            @Override
            public final void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (description.equals("net::ERR_UNKNOWN_URL_SCHEME"))
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(failingUrl));
                        view.getContext().startActivity(intent);
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
                    } finally {
                        if (view.canGoBack()) view.goBack();
                        else view.loadUrl("about:blank");
                    }
                else if (description.equals("net::ERR_CONNECTION_REFUSED") && errorCode == -6)
                    view.stopLoading();
                else {
                    securityIcon.setImageResource(R.drawable.connection_danger);
                    securityIcon.setVisibility(View.VISIBLE);
                    Toast.makeText(view.getContext(), resources.getString(R.string.page_load_failed_message)
                            .replace("$host_name", parseHostName(failingUrl))
                            .replace("$msg", description)
                            .replace("$err_code", String.valueOf(errorCode)), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public final void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                view.stopLoading();
                securityIcon.setImageResource(R.drawable.connection_danger);
                securityIcon.setVisibility(View.VISIBLE);
            }

            @Override
            public final void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                view.stopLoading();
                securityIcon.setImageResource(R.drawable.connection_danger);
                securityIcon.setVisibility(View.VISIBLE);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public final void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE)
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                if (progress == 100 && progressBar.getVisibility() == ProgressBar.VISIBLE)
                    progressBar.setVisibility(ProgressBar.GONE);
            }

            @Override
            public final void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }

            @Nullable
            private View customView;
            @Nullable
            private WebChromeClient.CustomViewCallback customViewCallback;
            private int originalOrientation;
            private int originalSystemUiVisibility;

            @Override
            public final void onHideCustomView() {
                if (context instanceof Activity) {
                    final Activity a = (Activity) context;
                    ((FrameLayout) a.getWindow().getDecorView()).removeView(customView);
                    customView = null;
                    a.getWindow().getDecorView().setSystemUiVisibility(originalSystemUiVisibility);
                    a.setRequestedOrientation(originalOrientation);
                    if (customViewCallback != null) customViewCallback.onCustomViewHidden();
                    customViewCallback = null;
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                }
            }

            @Override
            public final void onShowCustomView(View v, WebChromeClient.CustomViewCallback callBack) {
                if (context instanceof Activity) {
                    final Activity a = (Activity) context;
                    if (customView != null) onHideCustomView();
                    else {
                        customView = v;
                        originalSystemUiVisibility = a.getWindow().getDecorView().getSystemUiVisibility();
                        originalOrientation = a.getRequestedOrientation();
                        customViewCallback = callBack;
                        ((FrameLayout) a.getWindow().getDecorView()).addView(customView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        a.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
                        a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                        if (customView != null)
                            customView.setOnSystemUiVisibilityChangeListener(visibility -> {
                                final FrameLayout.LayoutParams b = (FrameLayout.LayoutParams) this.customView.getLayoutParams();
                                b.bottomMargin = 0;
                                b.topMargin = 0;
                                b.leftMargin = 0;
                                b.rightMargin = 0;
                                b.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                b.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                this.customView.setLayoutParams(b);
                                a.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
                                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                            });
                    }
                }
            }

            @Override
            public final Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            }

            @Override
            public final void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public final boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                final AlertDialog.Builder a = new AlertDialog.Builder(view.getContext());
                a.setTitle(view.getTitle());
                a.setMessage(message);
                a.setPositiveButton(R.string.close, (dialog, which) -> dialog.dismiss());
                a.show();
                return true;
            }
        });
        webView.setDownloadListener(this::onDownloadStart);
    }

    private static final class AdBlocker {
        private static final Set<String> AD_HOSTS = new HashSet<>();

        public static void init(Resources resources, Handler handler) {
            handler.post(() -> {
                try {
                    final TextReader a = new TextReader(resources.openRawResource(R.raw.ad_hosts));
                    AD_HOSTS.addAll(new Array<>(a.readLines()));
                    a.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        public static boolean isAd(String url) {
            try {
                return isAdHost(new URL(url).getHost());
            } catch (Exception e) {
                return false;
            }
        }

        private static boolean isAdHost(String host) {
            if (host.equals("")) return false;
            else {
                final int index = host.indexOf(".");
                return index >= 0 && (AD_HOSTS.contains(host) || index + 1 < host.length() && isAdHost(host.substring(index + 1)));
            }
        }

        public static WebResourceResponse createEmptyResource() {
            return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
        }
    }
}