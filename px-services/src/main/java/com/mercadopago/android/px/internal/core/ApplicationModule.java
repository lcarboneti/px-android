package com.mercadopago.android.px.internal.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.RetrofitUtil;
import java.io.File;
import retrofit2.Retrofit;

public class ApplicationModule implements PreferenceComponent {

    private static final String SHARED_PREFERENCE_NAME = "com.mercadopago.checkout.store";

    @NonNull
    private final Context context;

    public ApplicationModule(@NonNull final Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @NonNull
    public SessionIdProvider getSessionIdProvider() {
        return new SessionIdProvider(getSharedPreferences());
    }

    @NonNull
    public SessionIdProvider newSessionProvider(final String sessionId) {
        return new SessionIdProvider(getSharedPreferences(), sessionId);
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public JsonUtil getJsonUtil() {
        return JsonUtil.getInstance();
    }

    public FileManager getFileManager() {
        return new FileManager();
    }

    public File getCacheDir() {
        return context.getCacheDir();
    }

    public Retrofit getRetrofitClient() {
        return RetrofitUtil.getRetrofitClient(context);
    }
}
