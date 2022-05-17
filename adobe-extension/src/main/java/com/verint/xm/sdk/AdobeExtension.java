package com.verint.xm.sdk;

import android.app.Application;

import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;
import com.verint.xm.sdk.adobeExtension.ExtensionImpl;
import com.verint.xm.sdk.adobeExtension.logging.LogTags;
import com.verint.xm.sdk.adobeExtension.logging.Logging;

import java.lang.reflect.Field;

/**
 * Created by alan.wang on 2/27/19.
 */

public class AdobeExtension {
    private static com.verint.xm.sdk.Core.VerintSDKListener verintSDKConfigurationListener = null;

    private AdobeExtension() {

    }

    /**
     * Register the Verint extension
     */
    public static void registerExtension() {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                        "Failed to register the Verint extension: "+ extensionError.getErrorName());
            }
        };

        if (!MobileCore.registerExtension(ExtensionImpl.class, errorCallback)) {
            Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                    "Failed to register the Verint extension");
        }

        Core.startWithLateEnableFlag(getAdobeContext(), AdobeExtension.getVerintSDKConfigurationListener());
    }

    /**
     * Set a Verint SDK configuration listener
     * This method only works when called before {@link AdobeExtension#registerExtension()}
     * @param listener - a Verint SDK configuration listener
     */
    public static void setVerintSDKConfigurationListener(Core.VerintSDKListener listener) {
        if (listener != null) {
            verintSDKConfigurationListener = listener;
        } else {
            Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                    "Verint SDK configuration listener cannot be null");
        }
    }

    /**
     * Get the Verint SDK configuration listener, or null if it's not set
     * @return a Verint SDK configuration listener
     */
    public static Core.VerintSDKListener getVerintSDKConfigurationListener() {
       return verintSDKConfigurationListener;
    }

    private static Application getAdobeContext() {
        // Use reflection to get the application context
        Application context = null;
        try {
            Class cls = Class.forName("com.adobe.marketing.mobile.App");
            Field appContext = cls.getDeclaredField("appContext");
            appContext.setAccessible(true);
            context = (Application) appContext.get(null);
        } catch (Exception e) {
            Logging.internalLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                    "Failed to retrieve application context: " + e.getMessage());
        }
        return context;
    }
}