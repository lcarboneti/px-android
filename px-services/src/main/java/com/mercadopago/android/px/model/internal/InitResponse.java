package com.mercadopago.android.px.model.internal;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.preferences.CheckoutPreference;

public final class InitResponse extends PaymentMethodSearch {

    /* optional param, if the request went by id, then is returned by id */
    @Nullable private CheckoutPreference preference;

    @Nullable
    public CheckoutPreference getCheckoutPreference() {
        return preference;
    }
}
