package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.PaymentRecovery;

public enum Reason {
    ESC_CAP,
    SAVED_CARD,
    CALL_FOR_AUTH,
    DISABLED_CARD;

    public static Reason from(@NonNull final PaymentRecovery paymentRecovery) {
        if (paymentRecovery.isStatusDetailCallForAuthorize()) {
            return Reason.CALL_FOR_AUTH;
        } else if (paymentRecovery.isStatusDetailCardDisabled()) {
            return Reason.DISABLED_CARD;
        } else {
            return Reason.ESC_CAP;
        }
    }
}