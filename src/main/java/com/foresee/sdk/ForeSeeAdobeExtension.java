package com.foresee.sdk;

import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;
import com.foresee.sdk.adobeExtension.ExtensionImpl;
import com.foresee.sdk.adobeExtension.logging.LogTags;
import com.foresee.sdk.common.Logging;

/**
 * Created by alan.wang on 2/27/19.
 */

public class ForeSeeAdobeExtension {
    private static ForeSee.ForeSeeSDKConfigurationListener foreSeeSDKConfigurationListener = null;

    private ForeSeeAdobeExtension() {

    }

    /**
     * Register the ForeSee extension
     */
    public static void registerExtension() {
        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                        "Failed to register the ForeSee extension: "+ extensionError.getErrorName());
            }
        };

        if (!MobileCore.registerExtension(ExtensionImpl.class, errorCallback)) {
            Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                    "Failed to register the ForeSee extension");
        }
    }

    /**
     * Set a ForeSee SDK configuration listener
     * This method only works when called before {@link ForeSeeAdobeExtension#registerExtension()}
     * @param listener - a ForeSee SDK configuration listener
     */
    public static void setForeSeeSDKConfigurationListener(ForeSee.ForeSeeSDKConfigurationListener listener) {
        if (listener != null) {
            foreSeeSDKConfigurationListener = listener;
        } else {
            Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                    "ForeSee SDK configuration listener cannot be null");
        }
    }

    /**
     * Get the ForeSee SDK configuration listener, or null if it's not set
     * @return a ForeSee SDK configuration listener
     */
    public static ForeSee.ForeSeeSDKConfigurationListener getForeSeeSDKConfigurationListener() {
       return foreSeeSDKConfigurationListener;
    }

}