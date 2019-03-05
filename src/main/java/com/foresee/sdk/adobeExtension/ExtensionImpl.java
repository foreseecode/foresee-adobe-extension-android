package com.foresee.sdk.adobeExtension;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.ExtensionError;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.foresee.sdk.ForeSee;
import com.foresee.sdk.ForeSeeAdobeExtension;
import com.foresee.sdk.adobeExtension.logging.LogTags;
import com.foresee.sdk.common.Logging;
import com.foresee.sdk.common.utils.Util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.foresee.sdk.adobeExtension.Constants.ADOBE_IDENTITY_MID_CPP_KEY;
import static com.foresee.sdk.adobeExtension.Constants.EVENT_TYPE_ADOBE_HUB;
import static com.foresee.sdk.adobeExtension.Constants.EVENT_TYPE_ADOBE_RULES_ENGINE;
import static com.foresee.sdk.adobeExtension.Constants.EVENT_TYPE_FORESEE_EXTENSION;
import static com.foresee.sdk.adobeExtension.Constants.FORESEE_EXTENSION_NAME;
import static com.foresee.sdk.adobeExtension.Constants.RULES_DETAIL_KEY;
import static com.foresee.sdk.adobeExtension.Constants.RULES_TRIGGERED_CONSEQUENCE_KEY;
import static com.foresee.sdk.adobeExtension.Constants.SharedState.IDENTITY;

/**
 * Created by alan.wang on 2/27/19.
 */

public class ExtensionImpl extends Extension {
    // region - constants
    private static final String FORESEE_CLIENT_ID_KEY = "foresee.clientId";
    private static final String FORESEE_IS_DEBUG_KEY = "foresee.isDebugLoggingEnabled";
    private static final String IDENTITY_MID_KEY = "mid";
    private static final String ACTION_TO_PERFORM_KEY = "foresee.performAction";
    private static final String ACTION_CHECK_ELIGIBILITY = "checkIfEligibleForSurvey";
    //endregion

    // region - variables
    private Application applicationContext;
    private ConcurrentLinkedQueue<Event> eventQueue;
    private ExecutorService executorService;
    private final Object executorMutex = new Object();
    private boolean didReceivedConfiguration = false;
    private boolean didSetAdobeMid = false;
    private String mid;
    // endregion

    // region - constructor
    protected ExtensionImpl(final ExtensionApi extensionApi) {
        super(extensionApi);

        ExtensionErrorCallback<ExtensionError> errorCallback = new ExtensionErrorCallback<ExtensionError>() {
            @Override
            public void error(final ExtensionError extensionError) {
                Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                        "Unable to register listener: " + extensionError.getErrorName());
            }
        };

        boolean result;
        // register a listener for shared state changes
        extensionApi.registerEventListener(
                EVENT_TYPE_ADOBE_HUB,
                Constants.EVENT_SOURCE_ADOBE_SHARED_STATE,
                ExtensionListener.class,
                errorCallback);

        // register a listener for RulesEngine response events
        extensionApi.registerEventListener(
                EVENT_TYPE_ADOBE_RULES_ENGINE,
                Constants.EVENT_SOURCE_ADOBE_RESPONSE_CONTENT,
                ExtensionListener.class,
                errorCallback);

        // register a listener for ForeSeeAdobeExtension request events
        extensionApi.registerEventListener(
                EVENT_TYPE_FORESEE_EXTENSION,
                Constants.EVENT_SOURCE_FORESEE_REQUEST_CONTENT,
                ExtensionListener.class,
                errorCallback);

        applicationContext = (Application) getAdobeContext().getApplicationContext();

        // initialize the events queue
        eventQueue = new ConcurrentLinkedQueue<>();
    }
    // endregion

    // region - overwrites
    @Override
    protected String getName() {
        return FORESEE_EXTENSION_NAME;
    }

    @Override
    protected String getVersion() {
        return BuildConfig.ForeSeeSDKVersion;
    }
    // endregion

    // region - implementations
    /**
     * Get the application context from the adobe core module via reflection.
     * Until Adobe provides a formal way to retrieve the context, this is the only way
     * we can do to get it.
     *
     * @return the application context
     */
    public static Context getAdobeContext() {
        // Use reflection to get the application context
        Context context = null;
        try {
            Class cls = Class.forName("com.adobe.marketing.mobile.App");
            Field appContext = cls.getDeclaredField("appContext");
            appContext.setAccessible(true);
            context = (Context)appContext.get(null);
        } catch (Exception e) {
            Logging.foreSeeLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                    "Failed to retrieve application context: " + e.getMessage());
        }
        return context;
    }

    private void extractMidFromIdentitySharedState(final Event event) {
        Map<String, Object> identitySharedState = getApi().getSharedEventState(IDENTITY, event, null);

        if (identitySharedState == null) {
            Logging.foreSeeLog(Logging.LogLevel.INFO, LogTags.ADOBE_TAG,
                    "Identity shared state is pending, returning nil");
            return;
        }

        this.mid = (String) identitySharedState.get(IDENTITY_MID_KEY);
    }

    private ExecutorService getExecutor() {
        synchronized (executorMutex) {
            if (executorService == null) {
                executorService = Executors.newSingleThreadExecutor();
            }

            return executorService;
        }
    }

    private void queueEvent(final Event event) {
        if (event == null) {
            return;
        }

        eventQueue.add(event);
    }

    private void processEvents() {
        while (!eventQueue.isEmpty()) {
            Event eventToProcess = eventQueue.peek();

            ExtensionErrorCallback<ExtensionError> extensionErrorCallback = new ExtensionErrorCallback<ExtensionError>() {
                @Override
                public void error(final ExtensionError extensionError) {
                    Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                            String.format(Locale.CANADA,"Could not process event, an error occurred while retrieving configuration shared state: %s", extensionError.getErrorName()));
                }
            };

            Map<String, Object> configSharedState = getApi().getSharedEventState(
                    Constants.SharedState.CONFIGURATION, eventToProcess, extensionErrorCallback);

            // NOTE: configuration is mandatory processing the event, so if shared state is null (pending) stop processing events
            if (configSharedState == null) {
                Logging.foreSeeLog(Logging.LogLevel.INFO, LogTags.ADOBE_TAG,
                        "Could not process event, configuration shared state is pending");
                return;
            }

            if (!didSetAdobeMid) {
                // Identity is not a mandatory dependency for this event, just retrieve mid
                extractMidFromIdentitySharedState(eventToProcess);
                // Set a Adobe ID CPP
                if (!Util.isBlank(mid)) {
                    didSetAdobeMid = true;
                    ForeSee.addCPPValue(ADOBE_IDENTITY_MID_CPP_KEY, mid);
                }
            }

            // Try process event
            String eventType = eventToProcess.getType();
            if (Util.compareStringsIngoreCases(eventType, EVENT_TYPE_ADOBE_HUB)) {
                handleAdobeHubEvent(eventToProcess, configSharedState);
            } else if (Util.compareStringsIngoreCases(eventType, EVENT_TYPE_ADOBE_RULES_ENGINE)) {
                handleRulesConsequence(eventToProcess, configSharedState);
            } else if (Util.compareStringsIngoreCases(eventType, EVENT_TYPE_FORESEE_EXTENSION)) {
                handleForeSeeSpecificEvent(eventToProcess, configSharedState);
            }

            // event processed, remove it from the queue
            eventQueue.poll();
        }
    }

    private void handleAdobeHubEvent(Event event, Map<String, Object> configSharedState) {
        Logging.foreSeeLog(Logging.LogLevel.INFO, LogTags.ADOBE_TAG, "Handling a Adobe hub event");

        String clientId = (String) configSharedState.get(FORESEE_CLIENT_ID_KEY);
        if (!Util.isBlank(clientId)) {
            final Boolean isDebug = (Boolean) configSharedState.get(FORESEE_IS_DEBUG_KEY);
            // Try start ForeSee SDK
            if (!didReceivedConfiguration && applicationContext != null) {
                didReceivedConfiguration = true;

                // Identity is not a mandatory dependency for this event, just retrieve mid
                extractMidFromIdentitySharedState(event);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ForeSee.setDebugLogEnabled((isDebug == null) ? false : isDebug);
                        ForeSee.start(applicationContext, ForeSeeAdobeExtension.getForeSeeSDKConfigurationListener());
                    }
                });
            }
        } else {
            Logging.alwaysLog(Logging.LogLevel.ERROR, LogTags.ADOBE_TAG,
                    "Cannot initialize ForeSee SDK without a valid client id");
        }
    }

    private void handleRulesConsequence(Event event, Map<String, Object> configSharedState) {
        Logging.foreSeeLog(Logging.LogLevel.INFO, LogTags.ADOBE_TAG, "Handling a rules consequence");
        Map<String, Object> eventData = event.getEventData();

        Map<String, Object> triggeredConsequence = (HashMap<String, Object>) eventData.get(RULES_TRIGGERED_CONSEQUENCE_KEY);
        if (triggeredConsequence == null || triggeredConsequence.isEmpty()) {
            return;
        }

        Map<String, Object> detail = (Map<String, Object>)triggeredConsequence.get(RULES_DETAIL_KEY);
        if (detail == null || detail.isEmpty()) {
            return;
        }

        String actionToPerform = (String) detail.get(ACTION_TO_PERFORM_KEY);
        if (Util.isBlank(actionToPerform)) {
            Logging.alwaysLog(Logging.LogLevel.INFO, LogTags.ADOBE_TAG,
                    "Not a request consequence");
            return;
        }

        if (Util.compareStringsIngoreCases(actionToPerform, ACTION_CHECK_ELIGIBILITY)) {
            ForeSee.checkIfEligibleForSurvey();
        }
    }

    private void handleForeSeeSpecificEvent(Event event, Map<String, Object> configSharedState) {
        Logging.foreSeeLog(Logging.LogLevel.INFO, LogTags.ADOBE_TAG, "Handling a ForeSee specific event");
        // no-ops
    }

    void queueAndProcessEvents(Event event) {
        queueEvent(event);
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                processEvents();
            }
        });
    }
    // endregion
}
