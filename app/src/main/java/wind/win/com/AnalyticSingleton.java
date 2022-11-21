
package wind.win.com;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.appsflyer.AppsFlyerLib;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.HashMap;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class AnalyticSingleton {
    private static AnalyticSingleton instance;
    private final Context applicationContext;

    private AnalyticSingleton(Context applicationContext){
        this.applicationContext = applicationContext;
    }

    public static synchronized AnalyticSingleton getInstance(Context applicationContext){
        if(instance == null){
            instance = new AnalyticSingleton(applicationContext);
        }
        return instance;

    }

    //Log Firebase Analytics
    public void logActivity(Activity activity){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, activity.getClass().getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, activity.getClass().getSimpleName());
        FirebaseAnalytics.getInstance(applicationContext).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }


    public void logEvent(String eventName){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, eventName);
        FirebaseAnalytics.getInstance(applicationContext).logEvent(eventName, bundle);


        //Appsflyer event
        HashMap<String, Object> eventValues = new HashMap<>();
        eventValues.put("af_visitor_id",  applicationContext.getSharedPreferences("save", 0)
                .getString("visitor_id", "no_visitor_id"));

        AppsFlyerLib.getInstance().logEvent(applicationContext, eventName, eventValues);
    }
}

