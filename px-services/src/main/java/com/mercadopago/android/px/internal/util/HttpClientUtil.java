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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.mercadopago.android.px.services.BuildConfig.HTTP_CLIENT_LOG;

public final class HttpClientUtil {

    private static OkHttpClient client;
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String TLS_1_2 = "TLSv1.2";
    private static final String CACHE_DIR_NAME = "PX_OKHTTP_CACHE_SERVICES";
    private static final HttpLoggingInterceptor.Level LOGGING_INTERCEPTOR =
        HTTP_CLIENT_LOG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE;

    private HttpClientUtil() {
    }

    public static synchronized OkHttpClient getClient(@NonNull final Context context,
        final int connectTimeout,
        final int readTimeout,
        final int writeTimeout) {

        OkHttpClient.Builder baseClient;
        if (client == null) {
            baseClient = createBaseClient(context, connectTimeout, readTimeout, writeTimeout);
            client = enableTLS12(baseClient).build();
        }
        return client;
    }

    /**
     * Intended public for client implementation.
     *
     * @param context
     * @param connectTimeout
     * @param readTimeout
     * @param writeTimeout
     * @return am httpClient with TLS 1.2 support
     */
    @NonNull
    public static OkHttpClient.Builder createBaseClient(@Nullable final Context context, final int connectTimeout,
        final int readTimeout, final int writeTimeout) {

        final File cacheFile = getCacheDir(context);
        final HttpLoggingInterceptor loginInterceptor = new HttpLoggingInterceptor();
        loginInterceptor.setLevel(LOGGING_INTERCEPTOR);

        OkHttpClient.Builder baseClient = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .cache(new Cache(cacheFile, CACHE_SIZE));

        if (context != null) {
            baseClient.addInterceptor(getConnectionInterceptor(context));
            baseClient.addInterceptor(new SessionInterceptor(context));
            baseClient.addInterceptor(new ProductIdInterceptor(context));
        }

        baseClient.addInterceptor(new RequestIdInterceptor());
        baseClient.addInterceptor(new UserAgentInterceptor(BuildConfig.USER_AGENT));

        // add logging interceptor (should be last interceptor)
        baseClient.addInterceptor(loginInterceptor);

        return baseClient;
    }

    public static OkHttpClient.Builder enableTLS12(@NonNull final OkHttpClient.Builder clientBuilder) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            return internalEnableTLS12(clientBuilder);
        }
        return clientBuilder;
    }

    private static OkHttpClient.Builder internalEnableTLS12(final OkHttpClient.Builder client) {
        final X509TrustManager certificate = certificateTrustManager();
        if (certificate != null) {
            return configureProtocol(client, certificate);
        }
        return client;
    }

    private static X509TrustManager certificateTrustManager() {
        try {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            return (X509TrustManager) trustManagers[0];
        } catch (final NoSuchAlgorithmException | KeyStoreException exception) {
            //Do nothing
        }
        return null;
    }

    private static OkHttpClient.Builder configureProtocol(final OkHttpClient.Builder client,
        final X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance(TLS_1_2);
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
            client.sslSocketFactory(new TLSSocketFactory(sslContext.getSocketFactory()), trustManager);
            return client.connectionSpecs(availableConnectionSpecs());
        } catch (final Exception exception) {
            //Do Nothing
        }
        return client;
    }

    @NonNull
    private static List<ConnectionSpec> availableConnectionSpecs() {
        final ConnectionSpec connectionSpec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .cipherSuites(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384)
            .tlsVersions(TlsVersion.TLS_1_2)
            .build();

        final List<ConnectionSpec> connectionSpecsList = new ArrayList<>();
        connectionSpecsList.add(connectionSpec);

        connectionSpecsList.add(connectionSpec.CLEARTEXT);

        return connectionSpecsList;
    }

    @NonNull
    private static Interceptor getConnectionInterceptor(@NonNull final Context context) {
        return new ConnectivityStateInterceptor(context);
    }

    private static File getCacheDir(@Nullable final Context context) {
        File cacheDir;
        if (context != null) {
            cacheDir = context.getCacheDir();
            if (cacheDir == null) {
                cacheDir = context.getDir("cache", Context.MODE_PRIVATE);
            }
        } else {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "MyCache");
        }
        return new File(cacheDir, CACHE_DIR_NAME);
    }

    public static void setCustomClient(final OkHttpClient clientCustom) {
        client = clientCustom;
    }
}
