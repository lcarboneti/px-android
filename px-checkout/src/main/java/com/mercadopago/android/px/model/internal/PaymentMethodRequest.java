package com.mercadopago.android.px.model.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.preferences.CheckoutPreference;

@SuppressWarnings("unused")
public class PaymentMethodRequest {

    @NonNull private final CheckoutPreference preference;

    @NonNull private final CheckoutParams checkoutParams;

    /* default */ PaymentMethodRequest(final Builder builder) {
        preference = builder.preference;
        checkoutParams = builder.checkoutParams;
    }

    public static class Builder {
        /* default */ CheckoutPreference preference;
        /* default */ CheckoutParams checkoutParams;

        public Builder setCheckoutPreference(@Nullable final CheckoutPreference preference) {
            this.preference = preference;
            return this;
        }

        public Builder setCheckoutParams(final CheckoutParams params) {
            checkoutParams = params;
            return this;
        }

        public PaymentMethodRequest build() {
            return new PaymentMethodRequest(this);
        }
    }
}
