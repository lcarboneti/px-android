package com.mercadopago.android.px.internal.features;

import com.mercadopago.android.px.tracking.internal.model.Reason;

public interface SecurityCode {
    interface Actions {
        void setReason(Reason reason);

        void trackAbort();
    }
}