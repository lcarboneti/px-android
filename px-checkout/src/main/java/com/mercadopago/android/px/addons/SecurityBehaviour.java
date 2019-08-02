package com.mercadopago.android.px.addons;

import android.support.annotation.NonNull;

public interface SecurityBehaviour {
    boolean isSecurityEnabled();

    void validateBiometrics(Listener listener);

    interface Listener {
        void onSuccess();

        void onFail(@NonNull final String message);
    }
}