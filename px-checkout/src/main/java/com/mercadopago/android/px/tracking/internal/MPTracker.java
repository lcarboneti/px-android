package com.mercadopago.android.px.tracking.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.CheckoutType;
import com.mercadopago.android.px.model.Event;
import com.mercadopago.android.px.model.ScreenViewEvent;
import com.mercadopago.android.px.tracking.PXEventListener;
import com.mercadopago.android.px.tracking.PXTrackingListener;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class MPTracker {

    private static final String ATTR_EXTRA_INFO = "extra_info";
    private static final String ATTR_FLOW_DETAIL = "flow_detail";
    private static final String ATTR_FLOW_NAME = "flow";
    private static final String ATTR_SESSION_ID = "session_id";
    private static final String ATTR_SESSION_TIME = "session_time";
    private static final String ATTR_CHECKOUT_TYPE = "checkout_type";
    private static final String ATTR_SECURITY_ENABLED = "security_enabled";

    private static MPTracker trackerInstance;

    @Deprecated
    @Nullable private PXEventListener mPXEventListener;

    @Nullable private PXTrackingListener pxTrackingListener;

    /**
     * Added in 4.3.0 version - temporal replacement for tracking additional params.
     */
    @Nullable private Map<String, ?> flowDetail;
    /**
     * Added in 4.3.0 version - temporal replacement for tracking additional params.
     */
    @Nullable private String flowName;

    @Nullable private String sessionId;

    @CheckoutType @Nullable private String checkoutType;

    private long initSessionTimestamp;

    private boolean securityEnabled;

    private MPTracker() {
        // do nothing
    }

    public static synchronized MPTracker getInstance() {
        if (trackerInstance == null) {
            trackerInstance = new MPTracker();
        }
        return trackerInstance;
    }

    /**
     * Set listener to track library's screens and events in the app.
     *
     * @param pxEventListener implementing the tracking methods
     * @deprecated Deprecated due to new tracking implementation standards. Use {@link com.mercadopago.android.px.tracking.internal.MPTracker#setPXTrackingListener(PXTrackingListener)}
     * instead.
     */
    @Deprecated
    public void setTracksListener(@Nullable final PXEventListener pxEventListener) {
        mPXEventListener = pxEventListener;
    }

    /**
     * Set listener to track library's screens and events in the app.
     *
     * @param pxTrackingListener implementing the tracking methods
     */
    public void setPXTrackingListener(@Nullable final PXTrackingListener pxTrackingListener) {
        this.pxTrackingListener = pxTrackingListener;
    }

    /**
     * Set a map to add information to the library's screen and event tracks.
     *
     * @param flowDetail A map with extra information about the flow in your app that uses the checkout.
     */
    public void setFlowDetail(@NonNull final Map<String, ?> flowDetail) {
        this.flowDetail = flowDetail;
    }

    /**
     * Set a name to identify the flow in your app that opens the checkout.
     *
     * @param flowName The name that identifies your flow
     */
    public void setFlowName(@Nullable final String flowName) {
        this.flowName = flowName;
    }

    /**
     * Set a session id to identify differents user's session.
     *
     * @param sessionId The id that identifies a session
     */
    public void setSessionId(@Nullable final String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Set if the user will be challenged with security validation or not
     *
     * @param securityEnabled indicates if the user will be challenged with fingerprint/pin/pattern when pays
     */
    public void setSecurityEnabled(final boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    /**
     * This method tracks a list of events in one request
     *
     * @param event Event to track
     * @deprecated Old tracking listener.
     */
    @Deprecated
    public void trackEvent(final Event event) {
        if (event.getType().equals(Event.TYPE_SCREEN_VIEW)) {
            final ScreenViewEvent screenViewEvent = (ScreenViewEvent) event;
            trackOldView(screenViewEvent.getScreenId());
            //New listener tracking compatible.
            trackViewCompat(screenViewEvent.getScreenId());
        }
    }

    private void trackViewCompat(@NonNull final String path) {
        if (pxTrackingListener != null) {
            final Map<String, Object> data = new HashMap<>();
            addAdditionalFlowInfo(data);
            pxTrackingListener.onView(path, data);
        }
    }

    private void trackOldView(@NonNull final String screenName) {
        if (mPXEventListener != null) {
            mPXEventListener.onScreenLaunched(screenName, new HashMap<String, String>());
        }
    }

    public void trackView(@NonNull final String path, @NonNull final Map<String, Object> data) {
        addAdditionalFlowInfo(data);
        if (pxTrackingListener != null) {
            pxTrackingListener.onView(path, data);
        }
        //Old tracking Compatibility.
        trackOldView(path);
    }

    public void trackEvent(@NonNull final String path, @NonNull final Map<String, Object> data) {
        // Event friction case needs to add flow detail in a different way. We ignore this case for now.
        if (!FrictionEventTracker.PATH.equals(path)) {
            addAdditionalFlowInfo(data);
        } else {
            addAdditionalFlowIntoExtraInfo(data);
        }
        if (pxTrackingListener != null) {
            pxTrackingListener.onEvent(path, data);
        }
    }

    private void addAdditionalFlowIntoExtraInfo(@NonNull final Map<String, Object> data) {
        if (data.containsKey(ATTR_EXTRA_INFO)) {
            final Object o = data.get(ATTR_EXTRA_INFO);
            try {
                final Map<String, Object> value = (Map<String, Object>) o;
                value.put(ATTR_FLOW_NAME, flowName);
                value.put(ATTR_SESSION_ID, sessionId);
                value.put(ATTR_SESSION_TIME, getSecondsAfterInit());
                data.put(ATTR_CHECKOUT_TYPE, checkoutType);
                value.put(ATTR_SECURITY_ENABLED, securityEnabled);
            } catch (final ClassCastException e) {
                // do nothing.
            }
        }
    }

    private void addAdditionalFlowInfo(@NonNull final Map<String, Object> data) {
        data.put(ATTR_FLOW_DETAIL, flowDetail);
        data.put(ATTR_FLOW_NAME, flowName);
        data.put(ATTR_SESSION_ID, sessionId);
        data.put(ATTR_SESSION_TIME, getSecondsAfterInit());
        data.put(ATTR_CHECKOUT_TYPE, checkoutType);
        data.put(ATTR_SECURITY_ENABLED, securityEnabled);
    }

    private long getSecondsAfterInit() {
        if (initSessionTimestamp == 0) {
            initializeSessionTime();
        }
        final long milliseconds = Calendar.getInstance().getTime().getTime() - initSessionTimestamp;
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
    }

    public void initializeSessionTime() {
        initSessionTimestamp = Calendar.getInstance().getTime().getTime();
    }

    public void hasExpressCheckout(final boolean hasExpressCheckout) {
        if (hasExpressCheckout) {
            checkoutType = CheckoutType.ONE_TAP;
        } else {
            checkoutType = CheckoutType.TRADITIONAL;
        }
    }
}