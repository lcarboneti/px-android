package com.mercadopago.android.px.model.internal;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.preferences.CheckoutPreference;

public final class InitResponse extends PaymentMethodSearch {

    @NonNull private CheckoutPreference preference;

    @NonNull private ResponseSite site;

    @NonNull
    public CheckoutPreference getCheckoutPreference() {
        return preference;
    }

    @NonNull
    public ResponseSite getSite() {
        return site;
    }

    public static class ResponseSite {
        private String id;

        public String getId() {
            return id;
        }
    }
}
