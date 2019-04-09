package com.mercadopago.android.px.model.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration;
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unused")
public final class CheckoutParams {

    @NonNull private final Collection<String> cardsWithEsc;
    @NonNull private final Collection<PaymentTypeChargeRule> charges;
    @NonNull private final DiscountParamsConfiguration discountParamsConfiguration;
    private final boolean hasSplitPaymentProcessor;
    private final boolean expressPaymentEnabled;

    private CheckoutParams(@NonNull final Builder builder) {
        cardsWithEsc = builder.cardsWithEsc;
        charges = builder.charges;
        discountParamsConfiguration = builder.discountParamsConfiguration;
        hasSplitPaymentProcessor = builder.hasSplitPaymentProcessor;
        expressPaymentEnabled = builder.expressPaymentEnabled;
    }

    public static final class Builder {

        /* default */ DiscountParamsConfiguration discountParamsConfiguration =
            new DiscountParamsConfiguration.Builder().build();

        /* default */ Collection<String> cardsWithEsc = Collections.emptyList();
        /* default */ Collection<PaymentTypeChargeRule> charges = Collections.emptyList();
        /* default */ boolean hasSplitPaymentProcessor;
        /* default */ boolean expressPaymentEnabled;

        public Builder setDiscountConfiguration(final DiscountParamsConfiguration discountParamsConfiguration) {
            this.discountParamsConfiguration = discountParamsConfiguration;
            return this;
        }

        public Builder setCardWithEsc(@NonNull final Collection<String> cardsWithEsc) {
            this.cardsWithEsc = cardsWithEsc;
            return this;
        }

        public Builder setCharges(@Nullable final Collection<PaymentTypeChargeRule> charges) {
            this.charges = charges;
            return this;
        }

        public Builder setHasSplit(final boolean hasSplitPaymentProcessor) {
            this.hasSplitPaymentProcessor = hasSplitPaymentProcessor;
            return this;
        }

        public Builder setHasExpressPayment(final boolean expressPaymentEnabled) {
            this.expressPaymentEnabled = expressPaymentEnabled;
            return this;
        }

        public CheckoutParams build() {
            return new CheckoutParams(this);
        }
    }
}
