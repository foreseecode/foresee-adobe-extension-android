package com.foresee.sdk.internal;

import android.app.Application;

import com.foresee.sdk.common.configuration.Configuration;
import com.foresee.sdk.common.utils.FsProperties;
import com.foresee.sdk.cxMeasure.tracker.PersistedState;
import com.foresee.sdk.cxMeasure.tracker.TrackingContext;

import java.util.Locale;

/**
 * Created by alan.wang on 02/27/19.
 */

public class ForeSeeAdobeExtensionFacade {

    // region Initialization

    private Application application;

    protected ForeSeeAdobeExtensionFacade() {
        // For use by ForeSeeAdobeExtensionFacade in non-compatible versions of Android only
    }

    public ForeSeeAdobeExtensionFacade(Application application) {
        this.application = application;
    }

    public static PersistedState getState() {
        return TrackingContext.Instance().getState();
    }

    // endregion
    // region - Internal utility
    public String getVersion() {
        return FsProperties.instance().getBuildVersion();
    }
    // endregion
}
