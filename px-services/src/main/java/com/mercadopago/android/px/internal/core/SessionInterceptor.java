package com.mercadopago.android.px.internal.core;

import android.content.Context;
import android.support.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class SessionInterceptor implements Interceptor {

    @NonNull private final Context context;

    private static final String SESSION_ID_HEADER = "X-Session-Id";

    public SessionInterceptor(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Request originalRequest = chain.request();
        final Request request = originalRequest.newBuilder()
            .header(SESSION_ID_HEADER, new ApplicationModule(context).getSessionIdProvider().getSessionId())
            .build();
        return chain.proceed(request);
    }
}