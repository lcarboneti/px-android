package com.mercadopago.android.px.internal.datasource.cache;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.services.Callback;

public class InitCacheCoordinator implements InitCache {

    @NonNull private final InitDiskCache initDiskCache;
    @NonNull private final InitMemCache initMemCache;

    public InitCacheCoordinator(@NonNull final InitDiskCache initDiskCache, @NonNull final InitMemCache initMemCache) {
        this.initDiskCache = initDiskCache;
        this.initMemCache = initMemCache;
    }

    @NonNull
    @Override
    public MPCall<InitResponse> get() {
        if (initMemCache.isCached()) {
            return initMemCache.get();
        } else {
            return new MPCall<InitResponse>() {
                @Override
                public void enqueue(final Callback<InitResponse> callback) {
                    initDiskCache.get().enqueue(getCallbackDisk(callback));
                }

                @Override
                public void execute(final Callback<InitResponse> callback) {
                    initDiskCache.get().execute(getCallbackDisk(callback));
                }
            };
        }
    }

    /* default */ Callback<InitResponse> getCallbackDisk(final Callback<InitResponse> callback) {
        return new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                initMemCache.put(paymentMethodSearch);
                callback.success(paymentMethodSearch);
            }

            @Override
            public void failure(final ApiException apiException) {
                callback.failure(apiException);
            }
        };
    }

    @Override
    public void put(@NonNull final InitResponse groups) {
        initMemCache.put(groups);
        initDiskCache.put(groups);
    }

    @Override
    public void evict() {
        initDiskCache.evict();
        initMemCache.evict();
    }

    @Override
    public boolean isCached() {
        return initMemCache.isCached() || initDiskCache.isCached();
    }
}
