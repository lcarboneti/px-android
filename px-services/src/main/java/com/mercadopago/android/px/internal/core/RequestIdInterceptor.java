package com.mercadopago.android.px.internal.core;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.util.UUID;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class RequestIdInterceptor implements Interceptor {

    private static final String HEADER_KEY = "X-Request-Id";

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Request originalRequest = chain.request();
        final Request request = originalRequest.newBuilder()
            .header(HEADER_KEY, UUID.randomUUID().toString())
            .build();
        return chain.proceed(request);
    }
}