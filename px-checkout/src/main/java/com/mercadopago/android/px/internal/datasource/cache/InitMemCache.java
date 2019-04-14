package com.mercadopago.android.px.internal.datasource.cache;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.services.Callback;

public class InitMemCache implements InitCache {

    private InitResponse init;

    @NonNull
    @Override
    public MPCall<InitResponse> get() {
        return new MPCall<InitResponse>() {
            @Override
            public void enqueue(final Callback<InitResponse> callback) {
                resolve(callback);
            }

            @Override
            public void execute(final Callback<InitResponse> callback) {
                resolve(callback);
            }
        };
    }

    /* default */ void resolve(final Callback<InitResponse> callback) {
        if (isCached()) {
            callback.success(init);
        } else {
            callback.failure(new ApiException());
        }
    }

    @Override
    public void put(@NonNull final InitResponse init) {
        this.init = init;
    }

    @Override
    public void evict() {
        init = null;
    }

    @Override
    public boolean isCached() {
        return init != null;
    }
}
