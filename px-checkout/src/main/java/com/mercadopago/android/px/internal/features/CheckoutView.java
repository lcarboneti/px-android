package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

public interface CheckoutView extends MvpView {

    void startExpressPaymentRecoveryFlow(@NonNull PaymentRecovery paymentRecovery);

    void showError(MercadoPagoError error);

    void showProgress();

    void showReviewAndConfirm(final boolean isUniquePaymentMethod);

    void showPaymentMethodSelection();

    void showPaymentResult(PaymentResult paymentResult);

    void finishWithPaymentResult();

    void finishWithPaymentResult(Integer customResultCode);

    void finishWithPaymentResult(Payment payment);

    void finishWithPaymentResult(Integer customResultCode, Payment payment);

    void cancelCheckout();

    void cancelCheckout(MercadoPagoError mercadoPagoError);

    void cancelCheckout(Integer customResultCode, Boolean paymentMethodEdited);

    void startPaymentRecoveryFlow(PaymentRecovery paymentRecovery);

    void trackScreen();

    void showPaymentProcessor();

    void showPaymentProcessorWithAnimation();

    boolean isActive();

    void showBusinessResult(BusinessPaymentModel model);

    void showOneTap();

    void hideProgress();

    void exitCheckout(int resCode);

    void transitionOut();

    void showSavedCardFlow(Card card);

    void showNewCardFlow();

    void showReviewAndConfirmAndRecoverPayment(final boolean isUniquePaymentMethod,
        @NonNull final PostPaymentAction postPaymentAction);

    void startPayment();
}
