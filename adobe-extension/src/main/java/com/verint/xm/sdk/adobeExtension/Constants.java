package com.verint.xm.sdk.adobeExtension;

/**
 * Created by alan.wang on 2/27/19.
 */

class Constants {
    static final String VERINT_EXTENSION_NAME = "com.verint.xm.sdk.adobeextension";
    static final String ADOBE_IDENTITY_MID_CPP_KEY = "OMTR_MCID";
    static final String EVENT_TYPE_VERINT_EXTENSION = "com.verint.xm.sdk.eventType.adobeExtension";
    static final String EVENT_TYPE_ADOBE_RULES_ENGINE = "com.adobe.eventType.rulesEngine";
    public static final String EVENT_TYPE_ADOBE_HUB = "com.adobe.eventType.hub";
    static final String EVENT_SOURCE_VERINT_REQUEST_CONTENT = "com.verint.xm.sdk.eventSource.requestContent";
    static final String EVENT_SOURCE_VERINT_RESPONSE_CONTENT = "com.verint.xm.sdk.eventSource.responseContent";
    static final String EVENT_SOURCE_ADOBE_RESPONSE_CONTENT = "com.adobe.eventSource.responseContent";
    static final String EVENT_SOURCE_ADOBE_SHARED_STATE = "com.adobe.eventSource.sharedState";
    static final String RULES_TRIGGERED_CONSEQUENCE_KEY = "triggeredconsequence";
    static final String RULES_DETAIL_KEY = "detail";

    class SharedState {
        static final String STATE_OWNER = "stateowner";
        static final String CONFIGURATION = "com.adobe.module.configuration";
        static final String IDENTITY = "com.adobe.module.identity";

        private SharedState(){}
    }

    private Constants(){}
}
