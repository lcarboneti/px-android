package com.mercadopago.android.px.internal.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
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
    private static final int DEFAULT_TIMEOUT = 20;
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String TLS_1_2 = "TLSv1.2";
    private static final String CACHE_DIR_NAME = "PX_OKHTTP_CACHE_SERVICES";
    private static final HttpLoggingInterceptor.Level LOGGING_INTERCEPTOR =
        HTTP_CLIENT_LOG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE;

    public static synchronized OkHttpClient getClient(@NonNull final Context context) {

        if (clientDontExist()) {
            // Add Logging interceptor (should be last interceptor)
            final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(LOGGING_INTERCEPTOR);
            final File cacheFile = getCacheDir(context);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .cache(new Cache(cacheFile, CACHE_SIZE))
                .addInterceptor(getConnectionInterceptor(context))
                .addInterceptor(new SessionInterceptor(context))
                .addInterceptor(new ProductIdInterceptor(context))
                .addInterceptor(new RequestIdInterceptor())
                .addInterceptor(new UserAgentInterceptor(BuildConfig.USER_AGENT))
                .addInterceptor(interceptor);

            clientBuilder = enableTLS12(clientBuilder);
            client = clientBuilder.build();
        }
        return client;
    }

    private static OkHttpClient.Builder enableTLS12(@NonNull final OkHttpClient.Builder clientBuilder) {
        if (isTLSEnableNeeded()) {
            return internalEnableTLS12(clientBuilder);
        }
        return clientBuilder;
    }

    /**
     * True if enabling TLS is needed on current device (SDK version >= 16 and < 22)
     */
    private static boolean isTLSEnableNeeded() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Enable TLS on the OKHttp builder by setting a custom SocketFactory
     */
    private static OkHttpClient.Builder internalEnableTLS12(final OkHttpClient.Builder client) {
        final X509TrustManager certificate = certificateTrustManager();
        if (certificate != null) {
            return getOkHttpClient(client, certificate);
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

    private static OkHttpClient.Builder getOkHttpClient(final OkHttpClient.Builder client,
        final X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance(TLS_1_2);
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());

            OkHttpClient.Builder builder = client.sslSocketFactory(
                new TLSSocketFactory(sslContext.getSocketFactory()), trustManager);

            return builder.connectionSpecs(availableConnectionSpecs());
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

    /**
     * Intended for testing proposes.
     *
     * @param builderClient custom client
     */
    @VisibleForTesting
    public static void setClient(final OkHttpClient.Builder builderClient) {
        client = internalEnableTLS12(builderClient).build();
    }

    private static boolean clientDontExist() {
        return client == null;
    }

    private static File getCacheDir(final Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getDir("cache", Context.MODE_PRIVATE);
        }
        return new File(cacheDir, CACHE_DIR_NAME);
    }
}
