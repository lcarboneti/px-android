package com.mercadopago.android.px.internal.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentData;
import java.math.BigDecimal;

public final class PaymentDataHelper {

    private PaymentDataHelper() {
    }

    public static BigDecimal getPrettyAmountToPay(@NonNull final PaymentData paymentData) {
        if (hasPayerCostWithMultipleInstallments(paymentData.getPayerCost())) {
            return paymentData.getPayerCost().getTotalAmount();
        } else if (paymentData.getDiscount() != null) {
            return paymentData.getRawAmount().subtract(paymentData.getDiscount().getCouponAmount());
        }
        return paymentData.getRawAmount();
    }

    private static boolean hasPayerCostWithMultipleInstallments(@Nullable final PayerCost payerCost) {
        return payerCost != null && payerCost.hasMultipleInstallments();
    }
}
