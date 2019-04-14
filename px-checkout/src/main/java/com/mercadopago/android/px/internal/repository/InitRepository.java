package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.internal.InitResponse;

public interface InitRepository {

    @NonNull
    MPCall<InitResponse> getInit();
}
