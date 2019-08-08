package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.Map;

@SuppressWarnings("unused")
public final class InitData extends TrackingMapModel {

    @Nullable private final String checkoutPreferenceId;
    @Nullable private final CheckoutPreference checkoutPreference;
    private final boolean splitEnabled;
    private final boolean escEnabled;
    private final boolean expressEnabled;

    private InitData(@Nullable final String preferenceId,
                     @Nullable final CheckoutPreference preference,
                     @NonNull final PaymentConfiguration paymentConfiguration,
                     @NonNull final AdvancedConfiguration advancedConfiguration) {
        checkoutPreferenceId = preferenceId;
        checkoutPreference = preference;
        escEnabled = advancedConfiguration.isEscEnabled();
        expressEnabled = advancedConfiguration.isExpressPaymentEnabled();
        splitEnabled = preference != null && paymentConfiguration.getPaymentProcessor().supportsSplitPayment(preference);
    }

    public static InitData from(@NonNull final PaymentSettingRepository paymentSettingRepository) {
        return new InitData(paymentSettingRepository.getCheckoutPreferenceId(),
            paymentSettingRepository.getCheckoutPreference(),
            paymentSettingRepository.getPaymentConfiguration(),
            paymentSettingRepository.getAdvancedConfiguration());
    }

    @NonNull
    @Override
    protected Map<String, Object> sanitizeMap(@NonNull final Map<String, Object> map) {
        final Map<String, Object> preference = (Map<String, Object>) map.get("checkout_preference");
        if (preference != null) {
            preference.remove("payer");
        }
        return map;
    }
}