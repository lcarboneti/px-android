package com.mercadopago.android.px.model.internal;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration;
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
/**
 * Checkout params contains feature specific params and metadata about integration.
 * and additional configurations (like discount specific params)
 */
public final class CheckoutParams {

    /**
     * Feature specific params.
     */
    @NonNull private final Collection<String> cardsWithEsc;
    @NonNull private final Collection<PaymentTypeChargeRule> charges;
    @NonNull private final DiscountParamsConfiguration discountParamsConfiguration;

    /**
     * Opt-in and feature related descriptors params. This represents metadata about integration that will help to
     * the backend to decide if it should turn on or off certain flow / features.
     */
    private final boolean supportsExpress;
    private final boolean supportsSplit;
    // here will be more.


    /* default */ CheckoutParams(@NonNull final Builder builder) {
        cardsWithEsc = builder.cardsWithEsc;
        charges = builder.charges;
        discountParamsConfiguration = builder.discountParamsConfiguration;
        supportsSplit = builder.supportsSplit;
        supportsExpress = builder.supportsExpress;
    }

    public static final class Builder {

        /* default */ DiscountParamsConfiguration discountParamsConfiguration =
            new DiscountParamsConfiguration.Builder().build();

        /* default */ Collection<String> cardsWithEsc = new ArrayList<>();
        /* default */ Collection<PaymentTypeChargeRule> charges = new ArrayList<>();
        /* default */ boolean supportsSplit;
        /* default */ boolean supportsExpress;

        public Builder setDiscountConfiguration(final DiscountParamsConfiguration discountParamsConfiguration) {
            this.discountParamsConfiguration = discountParamsConfiguration;
            return this;
        }

        public Builder setCardWithEsc(@NonNull final Collection<String> cardsWithEsc) {
            this.cardsWithEsc.addAll(cardsWithEsc);
            return this;
        }

        public Builder setCharges(@NonNull final Collection<PaymentTypeChargeRule> charges) {
            this.charges.addAll(charges);
            return this;
        }

        public Builder setSupportsSplit(final boolean supportsSplit) {
            this.supportsSplit = supportsSplit;
            return this;
        }

        public Builder setSupportsExpress(final boolean supportsExpress) {
            this.supportsExpress = supportsExpress;
            return this;
        }

        public CheckoutParams build() {
            return new CheckoutParams(this);
        }
    }
}
