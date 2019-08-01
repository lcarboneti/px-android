package com.mercadopago.android.px.addons;

import android.support.annotation.NonNull;

public interface SecurityBehaviour {
    boolean isSecurityEnabled();

    void validateBiometrics(Listener listener);

    interface Listener {
        void success();

        void fail(@NonNull final String message);
    }
}