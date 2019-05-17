package com.mercadopago.android.px.internal.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.mercadopago.android.px.internal.core.ConnectivityStateInterceptor;
import com.mercadopago.android.px.internal.core.ProductIdInterceptor;
import com.mercadopago.android.px.internal.core.RequestIdInterceptor;
import com.mercadopago.android.px.internal.core.SessionInterceptor;
import com.mercadopago.android.px.internal.core.TLSSocketFactory;
import com.mercadopago.android.px.internal.core.UserAgentInterceptor;
import com.mercadopago.android.px.services.BuildConfig;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.mercadopago.android.px.services.BuildConfig.HTTP_CLIENT_LOG;

public final class HttpClientUtil {

    private static OkHttpClient client;
    private static OkHttpClient customClient;
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String CACHE_DIR_NAME = "PX_OKHTTP_CACHE_SERVICES";
    private static final HttpLoggingInterceptor.Level LOGGING_INTERCEPTOR =
        HTTP_CLIENT_LOG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE;

    private HttpClientUtil() {
    }

    public static synchronized OkHttpClient getClient(@NonNull final Context context,
        final int connectTimeout,
        final int readTimeout,
        final int writeTimeout) {

        if (customClientSet()) {
            return customClient;
        } else {
            if (client == null) {
                client = createClient(context, connectTimeout, readTimeout, writeTimeout);
            }
            return client;
        }
    }

    /**
     * Intended public for client implementation.
     *
     * @param connectTimeout
     * @param readTimeout
     * @param writeTimeout
     * @return am httpClient with TLS 1.1 support
     */
    @SuppressWarnings("unused")
    @NonNull
    public static OkHttpClient createClient(final int connectTimeout,
        final int readTimeout,
        final int writeTimeout) {
        return createClient(null, connectTimeout, readTimeout, writeTimeout);
    }

    /**
     * Intended public for client implementation.
     *
     * @param context
     * @param connectTimeout
     * @param readTimeout
     * @param writeTimeout
     * @return am httpClient with TLS 1.1 support
     */
    @NonNull
    public static OkHttpClient createClient(@Nullable final Context context, final int connectTimeout,
        final int readTimeout,
        final int writeTimeout) {
        // Set log info
        final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS);

        // Set cache size
        if (context != null) {
            okHttpClientBuilder.addInterceptor(getConnectionInterceptor(context));
            okHttpClientBuilder.addInterceptor(new SessionInterceptor(context));

            try {
                final Cache cache =
                    new Cache(new File(String.format("%s%s", context.getCacheDir().getPath(), CACHE_DIR_NAME)),
                        CACHE_SIZE);
                okHttpClientBuilder.cache(cache);
            } catch (final Exception e) {
                // do nothing
            }
        }

        // Custom interceptors
        okHttpClientBuilder.addInterceptor(new ProductIdInterceptor());
        okHttpClientBuilder.addInterceptor(new RequestIdInterceptor());
        okHttpClientBuilder.addInterceptor(new UserAgentInterceptor(BuildConfig.USER_AGENT));

        // add logging interceptor (should be last interceptor)
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(LOGGING_INTERCEPTOR);
        okHttpClientBuilder.addInterceptor(interceptor);

        // Set client
        OkHttpClient client = okHttpClientBuilder.build();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            try {
                client = TLSSocketFactory.enforceTls(client);
            } catch (final Exception e) {
                // Do nothing
            }
        }

        return client;
    }

    @NonNull
    private static Interceptor getConnectionInterceptor(@NonNull final Context context) {
        return new ConnectivityStateInterceptor(context);
    }

    /**
     * Intended for testing proposes.
     *
     * @param client custom client
     */
    @VisibleForTesting
    public static void setCustomClient(final OkHttpClient client) {
        customClient = TLSSocketFactory.enforceTls(client);
    }

    /**
     * Intended for testing proposes.
     */
    @VisibleForTesting
    public static void removeCustomClient() {
        customClient = null;
    }

    private static boolean customClientSet() {
        return customClient != null;
    }
}
