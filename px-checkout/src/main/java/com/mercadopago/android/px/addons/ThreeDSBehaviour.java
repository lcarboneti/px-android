package com.mercadopago.android.px.addons;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public interface ThreeDSBehaviour {

    void initialize(@NonNull final InitializeCallback initializeCallback);

    void transaction(@NonNull final TransactionCallback callback, @NonNull final AppCompatActivity activity);

    enum Status {
        TIMEOUT,
        CANCELLED
    }

    interface TransactionCallback {

        void onSuccess();

        void onError(@Nullable final Exception exception);

        void onTransactionApiFailed(@Nullable final Throwable e);

        void onTransactionFailed(@NonNull final Status status);

        void onTransactionFailed(@NonNull final String statusCode);
    }

    interface InitializeCallback {

        void success();

        void error(@Nullable final Exception e);
    }
}