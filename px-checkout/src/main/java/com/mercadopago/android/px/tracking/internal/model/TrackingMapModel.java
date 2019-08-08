package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.util.JsonUtil;
import java.util.Map;

public class TrackingMapModel {

    public static Map<String, Object> toMap(final Object object) {
        return JsonUtil.getInstance().getMapFromObject(object);
    }

    @NonNull
    public Map<String, Object> toMap() {
        return sanitizeMap(toMap(this));
    }

    @NonNull
    protected Map<String, Object> sanitizeMap(@NonNull final Map<String, Object> map) {
        return map;
    }
}