package com.verint.xm.adobeextensionsample;

import android.app.Application;
import android.util.Log;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Identity;
import com.adobe.marketing.mobile.Lifecycle;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Signal;
import com.adobe.marketing.mobile.UserProfile;

import com.verint.xm.sdk.AdobeExtension;

public class SampleApplication extends Application {

    static final String TAG = "VerintSample";

    @Override
    public void onCreate() {
        super.onCreate();

        registerAdobeExtension();
    }

    private void registerAdobeExtension() {
        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.DEBUG);
        try {
            AdobeExtension.registerExtension();

            Identity.registerExtension();
            Lifecycle.registerExtension();
            Signal.registerExtension();
            UserProfile.registerExtension();
            MobileCore.start(new AdobeCallback() {
                @Override
                public void call(Object o) {
                    MobileCore.configureWithAppID("launch-your-appID");
                }
            });
        } catch (Exception ex) {
            Log.d(TAG, "Failed to register the Adobe extension: " + ex.getMessage());
        }
    }
}
