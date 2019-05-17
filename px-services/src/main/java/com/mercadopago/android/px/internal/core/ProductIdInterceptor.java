package com.mercadopago.android.px.internal.core;

import android.support.annotation.NonNull;
import java.io.IOException;
import java.util.UUID;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class ProductIdInterceptor implements Interceptor {

    private static final String HEADER_KEY = "X-Product-Id";
    private static final String HEADER_VALUE = "BJEO9NVBF6RG01IIIOTG";

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Request originalRequest = chain.request();
        final Request request = originalRequest.newBuilder()
            .header(HEADER_KEY, HEADER_VALUE)
            .build();
        return chain.proceed(request);
    }
}