package com.mercadopago.android.px.addons.internal;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.mercadopago.android.px.addons.ThreeDSBehaviour;

public final class ThreeDSDefaultBehaviour implements ThreeDSBehaviour {

    @Override
    public void initialize(@NonNull final InitializeCallback initializeCallback) {
        initializeCallback.success();
    }

    @Override
    public void transaction(@NonNull final TransactionCallback callback,
        @NonNull final AppCompatActivity activity) {
        callback.onSuccess();
    }
}