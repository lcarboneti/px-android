package com.mercadopago.android.px.tracking.internal.views;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.util.Logger;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import java.util.HashMap;
import java.util.Map;

public abstract class ViewTracker {

    /* default */ static final String BASE_VIEW_PATH = "/px_checkout";
    /* default */ static final String ADD_PAYMENT_METHOD_PATH = "/add_payment_method";
    /* default */ static final String PAYMENTS_PATH = "/payments";

    private static final String TAG = ViewTracker.class.getSimpleName().toUpperCase();

    public final void track() {
        final String viewPath = getViewPath();
        final Map<String, Object> data = getData();
        MPTracker.getInstance().trackView(viewPath, data);
        Logger.debug(TAG, viewPath);
        Logger.debug(TAG, data.toString());
    }

    @NonNull
    public abstract String getViewPath();

    protected Map<String, Object> empty() {
        return new HashMap<>();
    }

    @NonNull
    public Map<String, Object> getData() {
        return new HashMap<>();
    }
}