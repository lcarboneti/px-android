package com.mercadopago.android.px.internal.datasource.cache;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.internal.InitResponse;

public interface InitCache {

    @NonNull
    MPCall<InitResponse> get();

    void put(@NonNull final InitResponse groups);

    void evict();

    boolean isCached();
}
