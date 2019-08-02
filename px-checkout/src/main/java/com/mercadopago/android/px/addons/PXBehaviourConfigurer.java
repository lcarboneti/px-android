package com.mercadopago.android.px.addons;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.addons.internal.PXApplicationBehaviourProvider;

public final class PXBehaviourConfigurer {

    private SecurityBehaviour securityBehaviour;
    private ESCManagerBehaviour escManagerBehaviour;
    private ThreeDSBehaviour threeDSBehaviour;

    public PXBehaviourConfigurer with(@NonNull final ESCManagerBehaviour escManagerBehaviour) {
        this.escManagerBehaviour = escManagerBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final SecurityBehaviour securityBehaviour) {
        this.securityBehaviour = securityBehaviour;
        return this;
    }

    public PXBehaviourConfigurer with(@NonNull final ThreeDSBehaviour threeDSBehaviour) {
        this.threeDSBehaviour = threeDSBehaviour;
        return this;
    }

    public void configure() {
        PXApplicationBehaviourProvider.set(securityBehaviour);
        PXApplicationBehaviourProvider.set(escManagerBehaviour);
        PXApplicationBehaviourProvider.set(threeDSBehaviour);
    }
}