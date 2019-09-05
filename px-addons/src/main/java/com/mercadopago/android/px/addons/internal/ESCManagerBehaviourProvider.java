package com.mercadopago.android.px.addons.internal;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;

public final class ESCManagerBehaviourProvider {

    private ESCManagerBehaviourProvider() {
    }

    public static ESCManagerBehaviour get(@NonNull final String sessionId, final boolean escEnabled) {
        if (escEnabled) {
            final ESCManagerBehaviour escManagerBehaviour = PXApplicationBehaviourProvider.getEscManagerBehaviour();
            escManagerBehaviour.setSessionId(sessionId);
            return escManagerBehaviour;
        } else {
            return new ESCManagerDefaultBehaviour();
        }
    }
}