package com.mercadopago.android.px.internal.core;

import android.support.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class UserAgentInterceptor implements Interceptor {

    private static final String HEADER_KEY = "User-Agent";
    private final String userAgent;

    public UserAgentInterceptor(@NonNull final String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Request originalRequest = chain.request();
        final Request requestWithUserAgent = originalRequest.newBuilder()
            .header(HEADER_KEY, userAgent)
            .build();
        return chain.proceed(requestWithUserAgent);
    }
}