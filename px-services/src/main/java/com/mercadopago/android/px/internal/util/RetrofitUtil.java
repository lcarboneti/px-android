package com.mercadopago.android.px.internal.util;

import android.content.Context;
import com.mercadopago.android.px.internal.adapters.ErrorHandlingCallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitUtil {

    private static final String MP_API_BASE_URL = "https://api.mercadopago.com";

    private RetrofitUtil() {
    }

    public static Retrofit getRetrofitClient(final Context context) {
        return getRetrofit(context, MP_API_BASE_URL);
    }

    private static Retrofit getRetrofit(final Context context,
        final String baseUrl) {

        return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(JsonUtil.getInstance().getGson()))
            .client(HttpClientUtil.getClient(context))
            .addCallAdapterFactory(new ErrorHandlingCallAdapter.ErrorHandlingCallAdapterFactory())
            .build();
    }
}
