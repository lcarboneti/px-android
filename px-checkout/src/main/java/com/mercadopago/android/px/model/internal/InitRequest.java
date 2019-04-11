package com.mercadopago.android.px.model.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.preferences.CheckoutPreference;

/**
 * DTO that represents init informaton from checkout.
 */
@SuppressWarnings("unused")
public final class InitRequest {

    /**
     * When when there is a "close" preference this value is not null.
     */
    @Nullable private final String preferenceId;
    /**
     * When it's an "open" preference, this value is not null. Open preference means a fictional preference (does not
     * exists in backend)
     */
    @Nullable private final CheckoutPreference preference;

    /**
     * When integrator already have a merchant order we will inform to our backend the id. Otherwise null. This avoid
     * the creation of a new merchant order
     */
    @Nullable private final String merchantOrderId;

    /**
     * Specific feature related params.
     */
    @NonNull private final CheckoutParams checkoutParams;

    /* default */ InitRequest(final Builder builder) {
        preference = builder.preference;
        preferenceId = builder.preferenceId;
        checkoutParams = builder.checkoutParams;
        merchantOrderId = null; // Not implemented yet.
    }

    public static class Builder {
        /* default */ @Nullable CheckoutPreference preference;
        /* default */ @Nullable String preferenceId;
        /* default */ CheckoutParams checkoutParams = new CheckoutParams.Builder().build();

        public Builder setCheckoutPreference(@Nullable final CheckoutPreference preference) {
            this.preference = preference;
            return this;
        }

        public Builder setCheckoutPreferenceId(@Nullable final String preferenceId) {
            this.preferenceId = preferenceId;
            return this;
        }

        public Builder setCheckoutParams(@NonNull final CheckoutParams params) {
            checkoutParams = params;
            return this;
        }

        public InitRequest build() {
            return new InitRequest(this);
        }
    }
}
