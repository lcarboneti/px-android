package com.mercadopago.android.px.preferences;

import com.mercadopago.android.px.internal.util.PreferenceValidator;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import org.junit.Test;

import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubActivePreferenceAndPayer;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubBuilderOneItemAndPayer;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubExpiredPreference;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubInactivePreferenceAndPayer;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithAllPaymentTypesExcluded;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithNegativeInstallmentsNumbers;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithOneItemValidActiveAndSomePaymentTypesExcluded;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithOneItemValidActiveButAllPaymentTypesExcluded;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithOneItemValidButInstallmentsDefaultNumberAndInstallmentsNumberNegative;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithPositiveDefaultInstallmentsNumberAndNegativeMaxInstallmentsNumber;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithPositiveInstallmentsNumber;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithPositiveMaxInstallmentsNumberAndNegativeDefaultInstallmentsNumber;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceWithSomePaymentTypesExcluded;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class CheckoutPreferenceTest {

    ///////////////////PAYMENTS_TYPES tests///////////////////
    @Test
    public void testWhenValidatePreferenceWithAllPaymentsTypesExcludedReturnFalse() {
        final CheckoutPreference preference = stubPreferenceWithAllPaymentTypesExcluded();
        assertFalse(preference.validPaymentTypeExclusion());
    }

    @Test
    public void testWhenValidatePreferenceWithSomePaymentsTypesExcludedReturnTrue() {
        final CheckoutPreference preference = stubPreferenceWithSomePaymentTypesExcluded();
        assertTrue(preference.validPaymentTypeExclusion());
    }

    ///////////////////INSTALLMENTS tests///////////////////
    @Test
    public void testWhenValidatePreferenceWithPositiveDefaultInstallmentsNumberAndNegativeMaxInstallmentsNumberReturnFalse() {
        final CheckoutPreference preference =
            stubPreferenceWithPositiveDefaultInstallmentsNumberAndNegativeMaxInstallmentsNumber();
        assertFalse(preference.validInstallmentsPreference());
    }

    @Test
    public void testWhenValidatePreferenceWithPositiveMaxInstallmentsNumberAndNegativeDefaultInstallmentsNumberReturnFalse() {
        final CheckoutPreference preference =
            stubPreferenceWithPositiveMaxInstallmentsNumberAndNegativeDefaultInstallmentsNumber();
        assertFalse(preference.validInstallmentsPreference());
    }

    @Test
    public void testWhenValidatePreferenceWithMaxInstallmentsNumberPositiveReturnTrue() {
        final CheckoutPreference preference = stubPreferenceWithPositiveInstallmentsNumber();
        assertTrue(preference.validInstallmentsPreference());
    }

    @Test
    public void testWhenValidatePreferenceWithNegativeMaxInstallmentsNumberReturnFalse() {
        final CheckoutPreference preference = stubPreferenceWithNegativeInstallmentsNumbers();
        assertFalse(preference.validInstallmentsPreference());
    }

    ///////////////////EXCEPTIONS tests///////////////////

    @Test
    public void testWhenValidatePreferenceValidNoThrowExceptionReturnTrue() {
        final CheckoutPreference preference = stubPreferenceWithOneItemValidActiveAndSomePaymentTypesExcluded();
        boolean valid = true;

        try {
            PreferenceValidator.validate(preference, null);
        } catch (final CheckoutPreferenceException e) {
            valid = false;
        } finally {
            assertTrue(valid);
        }
    }

    @Test
    public void testWhenValidatePreferenceWithAllPaymentTypesExcludedThrowExceptionReturnTrue() {
        final CheckoutPreference preference = stubPreferenceWithOneItemValidActiveButAllPaymentTypesExcluded();

        try {
            PreferenceValidator.validate(preference, null);
        } catch (final CheckoutPreferenceException e) {
            assertEquals(CheckoutPreferenceException.EXCLUDED_ALL_PAYMENT_TYPES, e.getErrorCode());
        }
    }

    @Test
    public void testWhenValidatePreferenceWithInstallmentsDefaultNumberAndInstallmentsNumberNegativeThrowExceptionReturnTrue() {
        final CheckoutPreference preference =
            stubPreferenceWithOneItemValidButInstallmentsDefaultNumberAndInstallmentsNumberNegative();

        try {
            PreferenceValidator.validate(preference, null);
        } catch (final CheckoutPreferenceException e) {
            assertEquals(CheckoutPreferenceException.INVALID_INSTALLMENTS, e.getErrorCode());
        }
    }

    @Test
    public void testWhenValidatePreferenceWithPreferenceExpiredThrowExceptionReturnTrue() {
        final CheckoutPreference preference = stubExpiredPreference();

        try {
            PreferenceValidator.validate(preference, null);
        } catch (final CheckoutPreferenceException e) {
            assertEquals(CheckoutPreferenceException.EXPIRED_PREFERENCE, e.getErrorCode());
        }
    }

    @Test
    public void testWhenPreferenceIsActiveReturnTrue() {
        final CheckoutPreference preference = stubActivePreferenceAndPayer();
        assertTrue(preference.isActive());
    }

    @Test
    public void testWhenPreferenceIsNotActiveReturnFalse() {
        final CheckoutPreference preference = stubInactivePreferenceAndPayer();
        assertFalse(preference.isActive());
    }

    @Test
    public void testWhenPreferenceIsNotExpiredReturnFalse() {
        final CheckoutPreference preference = stubActivePreferenceAndPayer();
        assertFalse(preference.isExpired());
    }

    @Test
    public void testWhenPreferenceIsExpiredReturnTrue() {
        final CheckoutPreference preference = stubExpiredPreference();
        assertTrue(preference.isExpired());
    }

    @Test
    public void testWhenValidatePreferenceWithNullExpirationDateToReturnFalse() {
        final CheckoutPreference preference = stubBuilderOneItemAndPayer().setExpirationDate(null).build();
        assertFalse(preference.isExpired());
    }
}