package com.mercadopago.android.px.core;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* default */ class PrefetchService {

    private static final ExecutorService EXECUTOR_QUEUE = Executors.newSingleThreadExecutor();

    private final Handler mainHandler;

    /* default */ @NonNull final Session session;
    /* default */ @NonNull CheckoutLazyInit internalCallback;
    /* default */ @NonNull final MercadoPagoCheckout checkout;

    /* default */ PrefetchService(@NonNull final MercadoPagoCheckout checkout, @NonNull final Session session,
        @NonNull final CheckoutLazyInit internalCallback) {
        this.checkout = checkout;
        this.session = session;
        this.internalCallback = internalCallback;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void prefetch() {
        EXECUTOR_QUEUE.execute(() -> {
            session.init(checkout);
            final String checkoutPreferenceId =
                session.getConfigurationModule().getPaymentSettings().getCheckoutPreferenceId();
            if (!TextUtil.isEmpty(checkoutPreferenceId)) {
                fetchPreference();
            } else {
                fetchGroups();
            }
        });
    }

    /* default */ void fetchPreference() {
        final PaymentSettingRepository paymentSettings =
            session.getConfigurationModule().getPaymentSettings();
        session.getCheckoutPreferenceRepository().getCheckoutPreference(paymentSettings.getCheckoutPreferenceId())
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
        session.getGroupsRepository().getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
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
            internalCallback.success(checkout);
        });
    }

    /* default */ void postError() {
        mainHandler.post(() -> internalCallback.failure());
    }

    public void cancel() {
        internalCallback = new CheckoutLazyInit(new MercadoPagoCheckout.Builder("", "")) {
            @Override
            public void fail(@NonNull final MercadoPagoCheckout mercadoPagoCheckout) {
                // do nothing
            }

            @Override
            public void success(@NonNull final MercadoPagoCheckout mercadoPagoCheckout) {
                // do nothing
            }
        };
    }
}