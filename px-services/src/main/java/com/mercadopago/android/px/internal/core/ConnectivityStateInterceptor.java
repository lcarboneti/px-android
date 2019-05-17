package com.mercadopago.android.px.internal.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.util.NoConnectivityException;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

public class ConnectivityStateInterceptor implements Interceptor {

    @NonNull
    private final Context context;

    public ConnectivityStateInterceptor(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            //noinspection ConstantConditions
            if (!connectivityManager.getActiveNetworkInfo().isAvailable()
                || !connectivityManager.getActiveNetworkInfo().isConnected()) {
                throw new NoConnectivityException();
            } else {
                return chain.proceed(chain.request());
            }
        } catch (final NullPointerException e) {
            throw new NoConnectivityException();
        }
    }
}