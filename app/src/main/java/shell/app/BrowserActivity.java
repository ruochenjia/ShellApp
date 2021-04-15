package shell.app;

import android.annotation.SuppressLint;
import android.content.Context;
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
    /*private WebView webView;
    private AppCompatEditText link;
    private AppCompatImageButton searchButton;
    private ProgressBar progressBar;
    private AppCompatImageView securityIcon;
    private View errorPage;*/

    private boolean pageInitialized = false;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean allowRefreshWebViewContent = false;

    /*static String url;
    private String linkUrl;

    static {
        File a = new File(downloadFolderPath);
        if (!a.isDirectory()) a.delete();
        if (!a.exists()) a.mkdirs();
    }

    public BrowserActivity() {
    }*/

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
        /*if (allowRefreshWebViewContent)
            setupWebView(webView);
        if (!pageInitialized) {
            try {
                linkUrl = getLinkUrl();
            } catch (Exception e) {
                // empty data
            }
            initializeWebView();
            pageInitialized = true;
            allowRefreshWebViewContent = true;
        }*/
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

    /*@Override
    public final void onDestroy() {
        saveSettings();
        super.onDestroy();
    }
*/
    @Override
    public final void onPause() {
        getContentView().setVisibility(View.GONE);
        super.onPause();
    }

    /*public final void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
        try {
            final DownloadManager.Request a = new DownloadManager.Request(Uri.parse(url));
            a.setMimeType(mimeType);
            final String b = CookieManager.getInstance().getCookie(url);
            a.addRequestHeader("cookie", b);
            a.addRequestHeader("User-Agent", userAgent);
            a.setDescription(getResources().getString(R.string.download_file_notification));
            a.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
            a.allowScanningByMediaScanner();
            a.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            a.setDestinationInExternalPublicDir(downloadFolderPath.replace(storagePath, "/"), URLUtil.guessFileName(url, contentDisposition, mimeType));
            DownloadManager c = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            c.enqueue(a);
            Toast.makeText(getApplicationContext(), R.string.download_start_notice, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            E.print(this, e);
        }
    }*/

    @NonNull
    @Override
    public final Context getContext() {
        return this;
    }

    /*final void initializeWebView() {
        webView = findViewById(R.id.webView);
        link = findViewById(R.id.addressBar);
        searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);
        securityIcon = findViewById(R.id.securityIcon);
        errorPage = findViewById(R.id.errorPage);
        final AppCompatImageButton[] buttons = new AppCompatImageButton[]{
                findViewById(R.id.clearTextButton),
                findViewById(R.id.backButton),
                findViewById(R.id.forwardButton),
                findViewById(R.id.reloadButton),
                findViewById(R.id.homeButton),
                findViewById(R.id.bookmarkButton),
                findViewById(R.id.settingButton)
        };
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        securityIcon.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.VISIBLE);
        errorPage.setVisibility(View.GONE);
        if (showHomeButton) buttons[4].setVisibility(View.VISIBLE);
        else buttons[4].setVisibility(View.GONE);
        setupWebView(webView);
        if (linkUrl != null) {
            if (linkUrl.startsWith("content://"))
                try {
                    final String a = Utils.saveTempFile(getContentResolver().openInputStream(Uri.parse(linkUrl)));
                    final String b = "file://" + a;
                    link.setText(b);
                } catch (Exception e) {
                    E.print(this, e);
                }
            else {
                link.setText(linkUrl);
                loadWebPage();
            }
        } else if (newTabPage != null)
            loadWebPage(newTabPage);

        searchButton.setOnClickListener(view -> {
            try {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            loadWebPage();
        });
        buttons[0].setOnClickListener(view -> link.setText(""));
        buttons[1].setOnClickListener(view -> {
            if (webView.canGoBack()) webView.goBack();
            else
                Toast.makeText(view.getContext(), R.string.cannot_go_back_message, Toast.LENGTH_SHORT).show();
        });
        buttons[2].setOnClickListener(view -> {
            if (webView.canGoForward())
                webView.goForward();
            else
                Toast.makeText(view.getContext(), R.string.cannot_go_forward_message, Toast.LENGTH_SHORT).show();
        });
        buttons[3].setOnClickListener(view -> webView.reload());
        buttons[4].setOnClickListener(view -> loadWebPage(homePage));
        buttons[5].setOnClickListener(view -> startActivity(new Intent(BrowserActivity.this, BrowserBookmarkActivity.class)));
        buttons[6].setOnClickListener(view -> startActivity(new Intent(BrowserActivity.this, BrowserSettingActivity.class)));
        ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout)).setOnRefreshListener(() -> {
            webView.reload();
            new Handler(Looper.myLooper()).postDelayed(() -> ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout)).setRefreshing(false), 500);
        });
        link.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                searchButton.callOnClick();
                return true;
            } else
                return false;
        });
    }

    final void loadWebPage() {
        errorPage.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        final String keyword = Objects.requireNonNull(link.getText()).toString();
        if (isUrlOrSearch(keyword))
            if (keyword.toLowerCase().contains("://") || keyword.toLowerCase().contains("about:"))
                url = keyword;
            else
                url = "http://" + keyword;
        else url = "https://www.google.com/search?q=" + keyword;
        loadWebPage(url);
    }

    final void loadWebPage(String url) {
        errorPage.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        try {
            webView.loadUrl(url);
        } catch (Exception e) {
            E.print(this, e);
        }
    }

    @SuppressLint("AddJavascriptInterface")
    final void setupWebView(WebView webView) {
        allowRefreshWebViewContent = false;
        loadSettings();
        if (blockWebViewAds) AdBlocker.init(this);
        final WebSettings a = webView.getSettings();
        a.setAppCacheEnabled(enableAppCache);
        a.setDatabaseEnabled(enableDatabase);
        a.setJavaScriptEnabled(allowJavascript);
        a.setJavaScriptCanOpenWindowsAutomatically(javaScriptCanOpenWindowsAutomatically);
        a.setLoadsImagesAutomatically(loadsImagesAutomatically);
        a.setSupportZoom(setSupportZoom);
        a.setBuiltInZoomControls(setBuiltInZoomControls);
        a.setDisplayZoomControls(setDisplayZoomControls);
        a.setLoadWithOverviewMode(loadWithOverviewMode);
        a.setUseWideViewPort(useWideViewPort);
        a.setDomStorageEnabled(enableDomStorage);
        a.setAllowContentAccess(allowContentAccess);
        a.setAllowFileAccess(allowFileAccess);
        a.setAllowFileAccessFromFileURLs(allowFileAccessFromFileURLs);
        a.setAllowUniversalAccessFromFileURLs(allowUniversalAccessFromFileURLs);
        a.setGeolocationEnabled(allowLocationAccess);
        a.setBlockNetworkImage(blockNetworkImage);
        a.setBlockNetworkLoads(blockNetworkLoads);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setKeepScreenOn(keepScreenOn);
        webView.setWebViewClient(new WebViewClient() {
            private final Map<String, Boolean> loadedUrls = new HashMap<>();

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public final boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                final String url = request.getUrl().toString();
                view.loadUrl(url);
                CookieManager.getInstance().setAcceptCookie(allowCookie);
                CookieManager.getInstance().setAcceptThirdPartyCookies(view, allowThirdPartyCookie);
                return true;
            }

            @Override
            public final boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                CookieManager.getInstance().setAcceptCookie(allowCookie);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(view, allowThirdPartyCookie);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public final WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                final String a = request.getUrl().toString();
                boolean b;
                if (!loadedUrls.containsKey(a)) {
                    b = AdBlocker.isAd(a);
                    loadedUrls.put(a, b);
                } else try {
                    b = loadedUrls.get(a);
                } catch (NullPointerException e) {
                    b = false;
                }
                return b ? AdBlocker.createEmptyResource() : null;
            }

            @Override
            public final WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean b;
                if (!loadedUrls.containsKey(url)) {
                    b = AdBlocker.isAd(url);
                    loadedUrls.put(url, b);
                } else try {
                    b = loadedUrls.get(url);
                } catch (NullPointerException e) {
                    b = false;
                }
                return b ? AdBlocker.createEmptyResource() : null;
            }

            @Override
            public final void onPageStarted(WebView view, String url, Bitmap favicon) {
                BrowserActivity.url = url;
                errorPage.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public final void onPageFinished(WebView view, String url) {
                BrowserActivity.url = url;
                link.setText(url);
                if (Application.isNetworkAvailable(BrowserActivity.this))
                    if (url.contains("https://")) {
                        securityIcon.setImageResource(R.drawable.connection_safe);
                        securityIcon.setVisibility(View.VISIBLE);
                    } else if (url.contains("http://")) {
                        securityIcon.setImageResource(R.drawable.connection_danger);
                        securityIcon.setVisibility(View.VISIBLE);
                    } else securityIcon.setVisibility(View.INVISIBLE);
                else securityIcon.setVisibility(View.INVISIBLE);
                view.loadUrl("javascript:" + new StringBuilder()
                        .append("(function() {")
                        .append("window.HTMLOUT.setHtml(\"<html>\" + document.getElementsByTagName(\"html\")[0].innerHTML + \"</html>\");")
                        .append("}) ();"));
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public final void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (error.getDescription().equals("net::ERR_UNKNOWN_URL_SCHEME"))
                    try {
                        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                        view.getContext().startActivity(intent);
                    } catch (Exception e) {
                        E.print(view.getContext(), e);
                    } finally {
                        if (view.canGoBack()) view.goBack();
                        else view.loadUrl("about:blank");
                    }
                else if (error.getDescription().equals("net::ERR_CONNECTION_REFUSED") && error.getErrorCode() == -6)
                    view.stopLoading();
                else {
                    securityIcon.setImageResource(R.drawable.connection_danger);
                    securityIcon.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    errorPage.setVisibility(View.VISIBLE);
                    final AppCompatTextView a = findViewById(R.id.errorDescription);
                    final AppCompatTextView b = findViewById(R.id.errorMessage);
                    final AppCompatTextView c = findViewById(R.id.errorCode);
                    a.setText(a.getText().toString().replace("$page_url", request.getUrl().toString()));
                    b.setText(b.getText().toString().replace("$page_error_message", error.getDescription()));
                    c.setText(c.getText().toString().replace("$page_error_code", Integer.toString(error.getErrorCode())));
                }
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
                    webView.setVisibility(View.GONE);
                    errorPage.setVisibility(View.VISIBLE);
                    final AppCompatTextView a = findViewById(R.id.errorDescription);
                    final AppCompatTextView b = findViewById(R.id.errorMessage);
                    final AppCompatTextView c = findViewById(R.id.errorCode);
                    a.setText(a.getText().toString().replace("$page_url", failingUrl));
                    b.setText(b.getText().toString().replace("$page_error_message", description));
                    c.setText(c.getText().toString().replace("$page_error_code", Integer.toString(errorCode)));
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

            public final void onHideCustomView() {
                ((FrameLayout) getWindow().getDecorView()).removeView(this.customView);
                this.customView = null;
                getWindow().getDecorView().setSystemUiVisibility(this.originalSystemUiVisibility);
                setRequestedOrientation(this.originalOrientation);
                assert this.customViewCallback != null;
                this.customViewCallback.onCustomViewHidden();
                this.customViewCallback = null;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }

            @Override
            public final void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paracustomViewCallback) {
                if (this.customView != null) onHideCustomView();
                else {
                    this.customView = paramView;
                    this.originalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                    this.originalOrientation = getRequestedOrientation();
                    this.customViewCallback = paracustomViewCallback;
                    ((FrameLayout) getWindow().getDecorView()).addView(this.customView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    assert this.customView != null;
                    this.customView.setOnSystemUiVisibilityChangeListener(visibility -> updateControls());
                }
            }

            @Override
            public final Bitmap getDefaultVideoPoster() {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            }

            final void updateControls() {
                if (customView != null) {
                    final FrameLayout.LayoutParams a = (FrameLayout.LayoutParams) this.customView.getLayoutParams();
                    a.bottomMargin = 0;
                    a.topMargin = 0;
                    a.leftMargin = 0;
                    a.rightMargin = 0;
                    a.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    a.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    this.customView.setLayoutParams(a);
                    getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isDarkModeOn())
                a.setForceDark(WebSettings.FORCE_DARK_ON);
            else a.setForceDark(WebSettings.FORCE_DARK_OFF);
        }
        if (pageInitialized) {
            webView.reload();
            allowRefreshWebViewContent = true;
        }
    }

    final Boolean isUrlOrSearch(String keyword) {
        return !keyword.contains("\u0020") && (keyword.contains(".") || keyword.contains("://") || keyword.startsWith("about:") || keyword.contains("/"));
    }

    final String getLinkUrl() throws Exception {
        final Intent i = this.getIntent();
        if (i != null)
            if (i.getAction() != null)
                if (i.getAction().equals(Intent.ACTION_VIEW)) {
                    final Uri a = i.getData();
                    if (a != null) return a.toString();
                    else
                        throw new Exception(getResources().getString(R.string.intent_data_empty_message));
                } else
                    throw new IllegalArgumentException(getResources().getString(R.string.unsupported_intent_action_message));
            else throw new Exception("Intent action must not be null");
        else throw new Exception("Intent cannot be null");
    }

    public static final class AdBlocker {
        private static final Set<String> AD_HOSTS = new HashSet<>();

        public static void init(Context context) {
            new shell.app.Handler().post(() -> {
                try {
                    final File a = new File(ActivityFramework.applicationCacheFolder, "ad_hosts.txt");
                    try (InputStream b = context.getResources().openRawResource(R.raw.ad_hosts); OutputStream c = new FileOutputStream(a)) {
                        final byte[] d = new byte[8192];
                        int e;
                        while ((e = b.read(d)) > 0) {
                            c.write(d, 0, e);
                        }
                    }
                    try (FileInputStream b = new FileInputStream(a); Scanner c = new Scanner(b, "UTF-8")) {
                        while (c.hasNextLine()) {
                            AD_HOSTS.add(c.nextLine());
                        }
                        if (c.ioException() != null) {
                            throw c.ioException();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        public static boolean isAd(String url) {
            try {
                return isAdHost(new URL(url).getHost());
            } catch (Exception e) {
                Log.c(e);
                return false;
            }
        }

        private static boolean isAdHost(String host) {
            if (TextUtils.isEmpty(host))
                return false;
            int index = host.indexOf(".");
            return index >= 0 && (AD_HOSTS.contains(host) || index + 1 < host.length() && isAdHost(host.substring(index + 1)));
        }

        public static WebResourceResponse createEmptyResource() {
            return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
        }
    }*/

    /*@NotNull
    @NonNull
    @Override
    public final String toString() {
        return "BrowserActivity{" +
                "webView=" + webView +
                ", link=" + link +
                ", searchButton=" + searchButton +
                ", progressBar=" + progressBar +
                ", securityIcon=" + securityIcon +
                ", errorPage=" + errorPage +
                ", pageInitialized=" + pageInitialized +
                ", doubleBackToExitPressedOnce=" + doubleBackToExitPressedOnce +
                ", allowRefreshWebViewContent=" + allowRefreshWebViewContent +
                ", linkUrl='" + linkUrl + '\'' +
                '}';
    }*/

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