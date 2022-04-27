package da.ssuwiv.testappart;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.onesignal.OneSignal;

import java.io.IOException;

public class SplashActivity extends AppCompatActivity {

    private static final String APPSFLYER_ID = "ccZd4vWxhN9uMEZ3iJ8B53";


    private SharedPreferences save;
    private ValueCallback<Uri[]> uploadMessage = null;
    private String advertId = "";
    private WebView webView;
    public static final int REQUEST_SELECT_FILE = 100;

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        save = getSharedPreferences("save", MODE_PRIVATE);
        advertId = save.getString("adId", "");
        if(advertId.isEmpty()){
            AsyncTask.execute(() -> {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException | IOException e) {
                    e.printStackTrace();
                }

                try {
                    assert idInfo != null;
                    advertId = idInfo.getId();
                    save.edit()
                            .putString("adId", advertId)
                            .apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        setOnesignal(advertId);
        initAppsflyer();



        webView = findViewById(R.id.webView);
        setupWebView(webView);

       // AnalyticSingleton.getInstance(this).logEvent("open_web");
        webView.loadUrl("https://www.google.com/");
    }


    private void initAppsflyer(){
        AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(false);
        AppsFlyerLib.getInstance().init(APPSFLYER_ID, null, this);
        AppsFlyerLib.getInstance().start(this);
    }


    private void setOnesignal(String advertId){
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.setExternalUserId(advertId);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView){

        WebSettings webViewSettings = webView.getSettings();

        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setUserAgentString(webView.getSettings().getUserAgentString().replace(")*", ""));
        webViewSettings.setAllowContentAccess(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        CookieManager.getInstance().setAcceptCookie(true);
        webViewSettings.setSupportZoom(false);
        webViewSettings.setAppCacheEnabled(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setSavePassword(true);
        webViewSettings.setSaveFormData(true);
        webViewSettings.setDatabaseEnabled(true);
        webViewSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webViewSettings.setMixedContentMode(0);
        webViewSettings.setAllowFileAccess(true);
        webViewSettings.setAllowFileAccessFromFileURLs(true);
        webViewSettings.setAllowUniversalAccessFromFileURLs(true);
        webViewSettings.setUseWideViewPort(true);
        webViewSettings.setLoadWithOverviewMode(true);
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webViewSettings.setLoadsImagesAutomatically(true);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setSaveEnabled(true);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(s));
                startActivity(i);
            }
        });

        WebChromeClient webChromeClient = new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView1, ValueCallback<Uri[]> filePathCallBack, FileChooserParams fileChooserParams){

                uploadMessage = filePathCallBack;
                Intent intent = fileChooserParams.createIntent();
                try{
                startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    return false;
                }
                return true;
            }
        };

        webView.setWebChromeClient(webChromeClient);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                if(url.startsWith("mailto")){
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_EMAIL, url.replace("mailto", ""));
                    getApplicationContext().startActivity(Intent.createChooser(intent, "Send email"));
                    return true;
                }

                if(url.startsWith("tel:")){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    getApplicationContext().startActivity(intent);
                    return true;
                }

                if(url.startsWith("https://diia.app")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.getData().getAuthority();
                    view.getContext().startActivity(intent);
                    return true;
                }

                if(url.startsWith("https://t.me/joinchat")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(intent);
                    return true;
                }


                if(url.startsWith(String.valueOf(Uri.parse(url).getHost().equals("localhost")))){
                    Intent intent = new Intent(SplashActivity.this, GameActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null)
                return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
            uploadMessage = null;
        }
    }

    @Override
    public void onBackPressed(){
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            new AlertDialog.Builder(getApplicationContext())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
     //   AnalyticSingleton.getInstance(this).logActivity(this);
    }
}