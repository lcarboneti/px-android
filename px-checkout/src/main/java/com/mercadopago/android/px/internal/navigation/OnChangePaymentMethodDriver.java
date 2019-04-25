package com.mercadopago.android.px.internal.navigation;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.configuration.InternalConfiguration;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.viewmodel.CheckoutStateModel;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.Payment;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_CHANGE_PAYMENT_METHOD;

public class OnChangePaymentMethodDriver {

    @NonNull private final InternalConfiguration internalConfiguration;
    @NonNull private final CheckoutStateModel checkoutStateModel;
    @NonNull private final PaymentRepository paymentRepository;

    public OnChangePaymentMethodDriver(@NonNull final InternalConfiguration internalConfiguration,
        @NonNull final CheckoutStateModel checkoutStateModel, @NonNull final PaymentRepository paymentRepository) {
        this.internalConfiguration = internalConfiguration;
        this.checkoutStateModel = checkoutStateModel;
        this.paymentRepository = paymentRepository;
    }

    public void drive(final ChangePaymentMethodDriverCallback callback) {
        if (internalConfiguration.shouldExitOnPaymentMethodChange()) {
            final IPaymentDescriptor payment = paymentRepository.getPayment();
            if (payment instanceof Payment) {
                callback.driveToFinishWithPaymentResult(RESULT_CHANGE_PAYMENT_METHOD, (Payment) payment);
            } else {
                // Should we track this?
                callback.driveToFinishWithoutPaymentResult(RESULT_CHANGE_PAYMENT_METHOD);
            }
        } else {
            if (!checkoutStateModel.isExpressCheckout) {
                callback.driveToShowPaymentMethodSelection();
            }
        }
    }

    public interface ChangePaymentMethodDriverCallback {
        void driveToFinishWithPaymentResult(Integer resultCode, Payment payment);

        void driveToFinishWithoutPaymentResult(Integer resultCode);

        void driveToShowPaymentMethodSelection();
    }
}