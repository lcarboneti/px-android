package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.repository.CheckoutPreferenceRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.PreferenceService;
import com.mercadopago.android.px.preferences.CheckoutPreference;

public final class CheckoutPreferenceService implements CheckoutPreferenceRepository {

    @NonNull private final PreferenceService preferenceService;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;

    public CheckoutPreferenceService(
        @NonNull final PreferenceService preferenceService,
        @NonNull final PaymentSettingRepository paymentSettingRepository) {
        this.preferenceService = preferenceService;
        this.paymentSettingRepository = paymentSettingRepository;
    }

    /**
     * Retrieve CheckoutPreference by Id.
     *
     * @param checkoutPreferenceId id to retrieve CheckoutPreference.
     * @return Call
     */
    @Override
    public MPCall<CheckoutPreference> getCheckoutPreference(@NonNull final String checkoutPreferenceId) {
        return preferenceService.getPreference(checkoutPreferenceId, paymentSettingRepository.getPublicKey());
    }
}
