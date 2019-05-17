package com.mercadopago.android.px.tracking.internal.views;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import java.util.HashMap;
import java.util.Map;

public class CvvAskViewTracker extends ViewTracker {

    public static final String PATH = BASE_VIEW_PATH + PAYMENTS_PATH + "/select_method/";
    private static final String ACTION_PATH = "/cvv";

    @Nullable private final Card card;
    @NonNull private final String paymentMethodType;
    @NonNull private final Reason reason;

    public CvvAskViewTracker(@Nullable final Card card, @NonNull final String paymentMethodType,
        @NonNull final Reason reason) {
        this.card = card;
        this.paymentMethodType = paymentMethodType;
        this.reason = reason;
    }

    @NonNull
    @Override
    public Map<String, Object> getData() {
        //TODO verify recovery escenario.
        if (card != null && card.getPaymentMethod() != null) {
            final Map<String, Object> data = new HashMap<>();
            data.put("payment_method_id", card.getPaymentMethod().getId());
            data.put("card_id", card.getId());
            data.put("reason", reason.name().toLowerCase());
            return data;
        }
        return super.getData();
    }

    @NonNull
    @Override
    public String getViewPath() {
        return PATH + paymentMethodType + ACTION_PATH;
    }
}