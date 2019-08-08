package com.mercadopago.android.px.internal.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.Date;

import static com.mercadopago.android.px.internal.util.TextUtil.isNotEmpty;

public final class PreferenceValidator {

    private PreferenceValidator() {
    }

    public static void validate(@NonNull final CheckoutPreference preference, @Nullable final String privateKey)
        throws CheckoutPreferenceException {
        final Date now = new Date();
        final Date expirationDateTo = preference.getExpirationDateTo();
        final Date expirationDateFrom = preference.getExpirationDateFrom();

        final boolean isExpired = expirationDateTo != null && now.after(expirationDateTo);
        final boolean isActive = expirationDateFrom == null || now.after(expirationDateFrom);
        final boolean validInstallments = preference.getPaymentPreference().installmentPreferencesValid();
        final boolean validPaymentTypeExclusion = preference.getPaymentPreference().excludedPaymentTypesValid();
        final boolean validPayer = isNotEmpty(preference.getPayer().getEmail()) || isNotEmpty(privateKey);

        if (!Item.areItemsValid(preference.getItems())) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.INVALID_ITEM);
        } else if (!validPayer) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.NO_EMAIL_FOUND);
        } else if (isExpired) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.EXPIRED_PREFERENCE);
        } else if (!isActive) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.INACTIVE_PREFERENCE);
        } else if (!validInstallments) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.INVALID_INSTALLMENTS);
        } else if (!validPaymentTypeExclusion) {
            throw new CheckoutPreferenceException(CheckoutPreferenceException.EXCLUDED_ALL_PAYMENT_TYPES);
        }
    }
}