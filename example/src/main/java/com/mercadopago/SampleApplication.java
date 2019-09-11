package com.mercadopago;

import android.app.Application;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.mercadopago.android.px.internal.util.HttpClientUtil;
import com.squareup.leakcanary.LeakCanary;
import okhttp3.OkHttpClient;

public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initializeLeakCanary();
    }

    private void initializeLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
        Stetho.initializeWithDefaults(this);

        // Create client base, add interceptors
        OkHttpClient.Builder baseClient = HttpClientUtil.createBaseClient(this, 10, 10, 10)
            .addNetworkInterceptor(new StethoInterceptor());

        // return client with TLS protocol setted
        final OkHttpClient client = HttpClientUtil.enableTLS12(baseClient)
            .build();
    }
}
