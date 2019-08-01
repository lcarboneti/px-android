package com.mercadopago.android.px.addons.internal;

import com.mercadopago.android.px.addons.SecurityBehaviour;

class SecurityDefaultBehaviour implements SecurityBehaviour {
    @Override
    public boolean isSecurityEnabled() {
        return false;
    }

    @Override
    public void validateBiometrics(final Listener listener) {
        listener.success();
    }
}
