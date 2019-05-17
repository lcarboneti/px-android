package com.mercadopago.android.px.core;

import android.os.Handler;
import android.os.Looper;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;

import static com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker.Id.GENERIC;
import static com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker.Style.NON_SCREEN;

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

    /* default */ void prefetch() {
        // TODO: use executor service
        currentFetch = new Thread(this::initCall);
        currentFetch.start();
    }

    /* default */ void initCall() {
        session.getInitRepository().getInit().execute(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                postSuccess();
            }

            @Override
            public void failure(final ApiException apiException) {
                postError(apiException);
            }
        });
    }

    /* default */ void postSuccess() {
        mainHandler.post(() -> {
            checkout.prefetch = true;
            checkoutLazyInitCallback.success(checkout);
        });
    }

    /* default */ void postError(final ApiException apiException) {
        mainHandler.post(() -> {
            FrictionEventTracker.with("/px_checkout/lazy_init", GENERIC, NON_SCREEN,
                new MercadoPagoError(apiException, ApiUtil.RequestOrigin.POST_INIT));
            checkoutLazyInitCallback.failure();
        });
    }

    /* default */ void cancel() {
        if (currentFetch != null && currentFetch.isAlive() && !currentFetch.isInterrupted()) {
            currentFetch.interrupt();
        }
    }
}
