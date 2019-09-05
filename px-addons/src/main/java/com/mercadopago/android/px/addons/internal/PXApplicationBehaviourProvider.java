package com.mercadopago.android.px.addons.internal;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.addons.SecurityBehaviour;

public final class PXApplicationBehaviourProvider {

    private static SecurityBehaviour securityBehaviour;
    private static ESCManagerBehaviour escManagerBehaviour;

    private PXApplicationBehaviourProvider() {
    }

    public static void set(final SecurityBehaviour securityBehaviour) {
        PXApplicationBehaviourProvider.securityBehaviour = securityBehaviour;
    }

    public static void set(final ESCManagerBehaviour escManagerBehaviour) {
        PXApplicationBehaviourProvider.escManagerBehaviour = escManagerBehaviour;
    }

    @NonNull
    public static SecurityBehaviour getSecurityBehaviour() {
        return securityBehaviour != null ? securityBehaviour : new SecurityDefaultBehaviour();
    }

    @NonNull
    /* default */ static ESCManagerBehaviour getEscManagerBehaviour() {
        return escManagerBehaviour != null ? escManagerBehaviour : new ESCManagerDefaultBehaviour();
    }
}