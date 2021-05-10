package shell.app;

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
}