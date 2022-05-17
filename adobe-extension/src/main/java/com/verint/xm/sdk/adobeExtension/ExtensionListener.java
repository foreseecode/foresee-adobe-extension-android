package com.verint.xm.sdk.adobeExtension;

import static com.verint.xm.sdk.adobeExtension.Constants.EVENT_TYPE_ADOBE_HUB;
import static com.verint.xm.sdk.adobeExtension.Constants.EVENT_TYPE_ADOBE_RULES_ENGINE;
import static com.verint.xm.sdk.adobeExtension.Constants.EVENT_TYPE_VERINT_EXTENSION;
import static com.verint.xm.sdk.adobeExtension.Constants.SharedState.CONFIGURATION;
import static com.verint.xm.sdk.adobeExtension.Constants.SharedState.STATE_OWNER;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;
import com.verint.xm.sdk.common.utils.Util;

import java.util.Map;

/**
 * Created by alan.wang on 2/27/19.
 */

public class ExtensionListener extends com.adobe.marketing.mobile.ExtensionListener {

    protected ExtensionListener(final ExtensionApi extension, final String type, final String source) {
        super(extension, type, source);
    }

    @Override
    public void hear(final Event event) {
        Map<String, Object> eventData = event.getEventData();
        String eventType = event.getType();

        if (eventData == null || eventType == null) {
            return;
        }

        final ExtensionImpl parentExtension = getParentExtension();
        if (parentExtension == null) {
            return;
        }

        // Check if we should queue and process the event
        if (Util.compareStringsIngoreCases(eventType, EVENT_TYPE_ADOBE_HUB)) {
            if (Util.compareStringsIngoreCases(CONFIGURATION, (String) eventData.get(STATE_OWNER))) {
                parentExtension.queueAndProcessEvents(event);
            }
        } else if (Util.compareStringsIngoreCases(eventType, EVENT_TYPE_ADOBE_RULES_ENGINE)) {
            parentExtension.queueAndProcessEvents(event);
        } else if (Util.compareStringsIngoreCases(eventType, EVENT_TYPE_VERINT_EXTENSION)) {
            parentExtension.queueAndProcessEvents(event);
        }
    }

    @Override
    protected ExtensionImpl getParentExtension() {
        return (ExtensionImpl) super.getParentExtension();
    }

    @Override
    public void onUnregistered() {
        super.onUnregistered();
    }
}