package com.mercadopago.android.px.model.internal;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration;
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public final class CheckoutParams {

    @NonNull private final Collection<String> cardsWithEsc;
    @NonNull private final Collection<PaymentTypeChargeRule> charges;
    @NonNull private final DiscountParamsConfiguration discountParamsConfiguration;
    private final boolean supportsExpress;
    private final boolean supportsSplit;

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
