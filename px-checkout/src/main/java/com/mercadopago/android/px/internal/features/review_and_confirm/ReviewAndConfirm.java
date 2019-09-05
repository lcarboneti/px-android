package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.addons.model.SecurityValidationData;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

public interface ReviewAndConfirm {

    interface View extends MvpView {

        void showCardCVVRequired(@NonNull final Card card);

        void cancelCheckoutAndInformError(@NonNull final MercadoPagoError mercadoPagoError);

        void showPaymentProcessor();

        void showResult(@NonNull BusinessPaymentModel businessPaymentModel);

        void showResult(@NonNull PaymentResult paymentResult);

        void startLoadingButton(final int paymentTimeout, @NonNull final PayButtonViewModel payButtonViewModel);

        void cancelLoadingButton();

        void finishLoading(@NonNull final ExplodeDecorator decorator);

        void hideConfirmButton();

        void startPaymentRecoveryFlow(PaymentRecovery recovery);

        void showErrorScreen(@NonNull final MercadoPagoError error);

        void showConfirmButton();

        void showErrorSnackBar(@NonNull final MercadoPagoError error);

        void showDynamicDialog(@NonNull final DynamicDialogCreator creator,
            @NonNull final DynamicDialogCreator.CheckoutData checkoutData);

        void reloadBody();

        void finishAndChangePaymentMethod();

        void setPayButtonText(@NonNull final PayButtonViewModel payButtonViewModel);

        void startSecurityValidation(@NonNull final SecurityValidationData data);
    }

    interface Action extends PaymentServiceHandler {

        void startSecuredPayment();

        void trackSecurityFriction();

        void onPaymentConfirm();

        //TODO unify Checkout Activity
        void onCardFlowResponse();

        void onCardFlowCancel();

        void onError(@NonNull final MercadoPagoError mercadoPagoError);

        void recoverFromFailure();

        void executePostPaymentAction(@NonNull PostPaymentAction postPaymentAction);

        void onViewResumed(View view);

        void hasFinishPaymentAnimation();

        void changePaymentMethod();
    }
}