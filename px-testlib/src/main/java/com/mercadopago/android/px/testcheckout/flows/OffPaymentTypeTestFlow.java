package com.mercadopago.android.px.testcheckout.flows;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;
import com.mercadopago.android.px.testcheckout.pages.ReviewAndConfirmPage;

public class OffPaymentTypeTestFlow extends TestFlow {

    public OffPaymentTypeTestFlow() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public OffPaymentTypeTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout,
        @NonNull final Context context) {
        super(mercadoPagoCheckout, context);
    }

    @NonNull
    public CongratsPage runOffPaymentTypeFlow(@NonNull final String paymentMethodName,
        final CheckoutValidator validator) {

        startCheckout();

        return new PaymentMethodPage(validator)
            .selectCash()
            .selectMethod(paymentMethodName)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runATMOffPaymentTypeFlow(@NonNull final String paymentMethodName,
        final CheckoutValidator validator) {

        startCheckout();

        return new PaymentMethodPage(validator)
            .selectATM()
            .selectMethod(paymentMethodName)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runOffPaymentTypeFlow(@NonNull final String paymentMethodName) {
        return runOffPaymentTypeFlow(paymentMethodName, null);
    }

    @NonNull
    public CongratsPage runOffPaymentMethodFromPaymentMethodFlow(@NonNull final String paymentMethodName,
        @NonNull final PaymentMethodPage paymentMethodPage, final CheckoutValidator validator) {
        return paymentMethodPage
            .selectCash()
            .selectMethod(paymentMethodName)
            .pressConfirmButton();
    }

    @NonNull
    public CongratsPage runOffPaymentMethodFromPaymentMethodFlow(@NonNull final String paymentMethodName,
        @NonNull final PaymentMethodPage paymentMethodPage) {
        return runOffPaymentMethodFromPaymentMethodFlow(paymentMethodName, paymentMethodPage, null);
    }

    @NonNull
    public ReviewAndConfirmPage runOffPaymentTypeFlowUntilReviewAndConfirm(@NonNull final String paymentMethodName) {
        return runOffPaymentTypeFlowUntilReviewAndConfirm(paymentMethodName, null);
    }

    @NonNull
    public ReviewAndConfirmPage runOffPaymentTypeFlowUntilReviewAndConfirm(@NonNull final String paymentMethodName,
        final CheckoutValidator validator) {

        startCheckout();

        return new PaymentMethodPage(validator)
            .selectCash()
            .selectMethod(paymentMethodName);
    }
}
