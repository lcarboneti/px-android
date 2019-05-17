package com.mercadopago.android.px.tracking.internal.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.model.ApiErrorData;
import com.mercadopago.android.px.tracking.internal.model.AvailableMethod;
import com.mercadopago.android.px.tracking.internal.views.ViewTracker;
import java.util.HashMap;
import java.util.Map;

public class FrictionEventTracker extends EventTracker {

    public static final String PATH = "/friction";
    private static final String ATTR_PATH = "path";
    private static final String ATTR_ATTRIBUTABLE = "attributable_to";
    private static final String VALUE_ATTRIBUTABLE = "mercadopago";
    private static final String ATTR_EXTRA_INFO = "extra_info";

    @NonNull private final String path;
    @NonNull private final Id fId;
    @NonNull private final Style style;

    @NonNull private final Map<Object, Object> extraInfo;

    public static EventTracker with(@NonNull final Id id,
        @NonNull final ViewTracker viewTracker,
        @NonNull final Style style,
        @Nullable final PaymentMethod paymentMethod) {

        final FrictionEventTracker frictionEventTracker =
            new FrictionEventTracker(viewTracker.getViewPath(), id, style);

        if (paymentMethod != null) {
            frictionEventTracker.extraInfo.putAll(AvailableMethod.from(paymentMethod).toMap());
        }

        return frictionEventTracker;
    }

    public static EventTracker with(@NonNull final String path,
        @NonNull final Id fId,
        @NonNull final Style style,
        @NonNull final MercadoPagoError mercadoPagoError) {
        final FrictionEventTracker frictionEventTracker = new FrictionEventTracker(path, fId, style);
        frictionEventTracker.extraInfo.put("api_error", new ApiErrorData(mercadoPagoError).toMap());
        return frictionEventTracker;
    }

    public static EventTracker with(@NonNull final String path,
        @NonNull final Id fId,
        @NonNull final Style style,
        @NonNull final String stacktrace) {
        final FrictionEventTracker frictionEventTracker = new FrictionEventTracker(path, fId, style);
        frictionEventTracker.extraInfo.put("stacktrace", stacktrace);
        return frictionEventTracker;
    }

    public static EventTracker with(@NonNull final String path,
        @NonNull final Id fId,
        @NonNull final Style style,
        @NonNull final Map<?, ?> metadata) {
        final FrictionEventTracker frictionEventTracker = new FrictionEventTracker(path, fId, style);
        frictionEventTracker.extraInfo.putAll(metadata);
        return frictionEventTracker;
    }

    public static EventTracker with(final Id id, final ViewTracker view,
        final Style style) {
        return new FrictionEventTracker(view.getViewPath(), id, style);
    }

    public enum Id {

        GENERIC("px_generic_error"),
        SILENT("px_silent_error"),
        INVALID_BIN("invalid_bin"),
        INVALID_CC_NUMBER("invalid_cc_number"),
        INVALID_NAME("invalid_name"),
        INVALID_EXP_DATE("invalid_expiration_date"),
        INVALID_CVV("invalid_cvv"),
        INVALID_DOCUMENT("invalid_document_number"),
        INVALID_STATUS_DETAIL("invalid_status_detail"),
        INVALID_ESC("invalid_esc"),
        INVALID_FINGERPRINT("invalid_fingerprint");

        private static final String ATTR = "id";

        /* default */ final String value;

        /* default */ Id(final String value) {
            this.value = value;
        }
    }

    public enum Style {
        SNACKBAR("snackbar"),
        SCREEN("screen"),
        CUSTOM_COMPONENT("custom_component"),
        NON_SCREEN("non_screen");

        private static final String ATTR = "style";

        /* default */ final String value;

        /* default */ Style(final String value) {
            this.value = value;
        }

    }

    protected FrictionEventTracker(@NonNull final String path,
        @NonNull final Id fId,
        @NonNull final Style style) {
        this.path = path;
        this.fId = fId;
        this.style = style;
        extraInfo = new HashMap<>();
    }

    @NonNull
    @Override
    public final Map<String, Object> getEventData() {
        final Map<String, Object> eventData = super.getEventData();
        eventData.put(Id.ATTR, fId.value);
        eventData.put(Style.ATTR, style.value);
        eventData.put(ATTR_PATH, path);
        eventData.put(ATTR_ATTRIBUTABLE, VALUE_ATTRIBUTABLE);
        eventData.put(ATTR_EXTRA_INFO, extraInfo);
        return eventData;
    }

    @NonNull
    @Override
    public final String getEventPath() {
        return PATH;
    }
}