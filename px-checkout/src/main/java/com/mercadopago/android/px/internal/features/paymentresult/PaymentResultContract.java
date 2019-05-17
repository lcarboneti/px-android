package com.mercadopago.android.px.internal.features.paymentresult;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.ApiException;

public interface PaymentResultContract {

    interface PaymentResultView extends MvpView {

        void setPropPaymentResult(@NonNull final String currencyId,
            @NonNull final PaymentResult paymentResult,
            final boolean showLoading);

        void setPropInstruction(@NonNull final Instruction instruction,
            @NonNull final String processingModeString,
            final boolean showLoading);

        void notifyPropsChanged();

        void showApiExceptionError(ApiException exception, String requestOrigin);

        void showInstructionsError();

        void openLink(String url);

        void finishWithResult(final int resultCode);

        void changePaymentMethod();

        void recoverPayment();

        void copyToClipboard(@NonNull final String content);
    }

    interface Actions {

        void freshStart();

        void onAbort();
    }
}
