package com.mercadopago.android.px.tracking.internal.events;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.util.Logger;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import java.util.HashMap;
import java.util.Map;

public abstract class EventTracker {

    /* default */ static final String BASE_PATH = "/px_checkout";
    private static final String TAG = EventTracker.class.getSimpleName().toUpperCase();

    public final void track() {
        final String eventPath = getEventPath();
        final Map<String, Object> eventData = getEventData();
        MPTracker.getInstance().trackEvent(eventPath, eventData);
        Logger.debug(TAG, eventPath);
        Logger.debug(TAG, eventData.toString());
    }

    @NonNull
    public abstract String getEventPath();

    @NonNull
    public Map<String, Object> getEventData() {
        return new HashMap<>();
    }
}