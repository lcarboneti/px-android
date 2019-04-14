package com.mercadopago.android.px.core;

import android.os.Handler;
import android.os.Looper;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.PreferenceService;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;

import static com.mercadopago.android.px.services.BuildConfig.API_VERSION;

/* default */ class PrefetchService {

    private final Handler mainHandler;

    /* default */ final Session session;
    /* default */ final CheckoutLazyInit checkoutLazyInitCallback;
    /* default */ final MercadoPagoCheckout checkout;
    private Thread currentFetch;

    /* default */ PrefetchService(final MercadoPagoCheckout checkout, final Session session,
        final CheckoutLazyInit checkoutLazyInitCallback) {
        session.init(checkout);
        this.checkout = checkout;
        this.session = session;
        this.checkoutLazyInitCallback = checkoutLazyInitCallback;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void prefetch() {
        final PaymentSettingRepository paymentSettings =
            session.getConfigurationModule().getPaymentSettings();

        // TODO: use executor service
        currentFetch = new Thread(() -> {

            final String checkoutPreferenceId =
                paymentSettings.getCheckoutPreferenceId();
            if (!TextUtil.isEmpty(checkoutPreferenceId)) {
                fetchPreference();
            } else {
                fetchGroups();
            }
        });
        currentFetch.start();
    }

    /* default */ void fetchPreference() {
        //TODO ADD PREFERENCE SERVICE.
        final PaymentSettingRepository paymentSettings =
            session.getConfigurationModule().getPaymentSettings();

        session.getRetrofitClient().create(PreferenceService.class)
            .getPreference(API_VERSION, paymentSettings.getCheckoutPreferenceId(),
                paymentSettings.getPublicKey())
            .execute(
                new Callback<CheckoutPreference>() {
                    @Override
                    public void success(final CheckoutPreference checkoutPreference) {
                        paymentSettings.configure(checkoutPreference);
                        fetchGroups();
                    }

                    @Override
                    public void failure(final ApiException apiException) {
                        //TODO Track
                        postError();
                    }
                });
    }

    /* default */ void fetchGroups() {
        session.getInitRepository().getInit().execute(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                postSuccess();
            }

            @Override
            public void failure(final ApiException apiException) {
                //TODO Track
                postError();
            }
        });
    }

    /* default */ void postSuccess() {
        mainHandler.post(() -> {
            checkout.prefetch = true;
            checkoutLazyInitCallback.success(checkout);
        });
    }

    /* default */ void postError() {
        mainHandler.post(() -> checkoutLazyInitCallback.failure());
    }

    public void cancel() {
        if (currentFetch != null && currentFetch.isAlive() && !currentFetch.isInterrupted()) {
            currentFetch.interrupt();
        }
    }
}
