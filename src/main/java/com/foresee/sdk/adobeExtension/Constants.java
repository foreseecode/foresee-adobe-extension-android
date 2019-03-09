package com.foresee.sdk.adobeExtension;

/**
 * Created by alan.wang on 2/27/19.
 */

class Constants {
    static final String FORESEE_EXTENSION_NAME = "com.foresee.sdk.adobeextension";
    static final String ADOBE_IDENTITY_MID_CPP_KEY = "OMTR_MCID";
    static final String EVENT_TYPE_FORESEE_EXTENSION = "com.foresee.sdk.eventType.adobeExtension";
    static final String EVENT_TYPE_ADOBE_RULES_ENGINE = "com.adobe.eventType.rulesEngine";
    static final String EVENT_TYPE_ADOBE_HUB = "com.adobe.eventType.hub";
    static final String EVENT_SOURCE_FORESEE_REQUEST_CONTENT = "com.foresee.sdk.eventSource.requestContent";
    static final String EVENT_SOURCE_FORESEE_RESPONSE_CONTENT = "com.foresee.sdk.eventSource.responseContent";
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
