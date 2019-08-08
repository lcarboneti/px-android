package com.mercadopago.android.px.testcheckout.flows;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.testcheckout.assertions.DefaultValidator;
import com.mercadopago.android.px.testcheckout.input.Card;
import com.mercadopago.android.px.testcheckout.pages.BusinessCongratsPage;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.DebitCardPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;
import com.mercadopago.android.px.testcheckout.pages.RejectedPage;
import com.mercadopago.android.px.testcheckout.pages.ReviewAndConfirmPage;
import com.mercadopago.android.px.testcheckout.pages.SecurityCodePage;
import java.math.BigDecimal;
import java.util.Collections;

public class SavedCardTestFlow extends TestFlow {

    private static final String PUBLIC_KEY = "APP_USR-648a260d-6fd9-4ad7-9284-90f22262c18d";
    private static final String CVV_NUMBER = "123";
    public static final String PAYER_1_WITH_CARDS_ACCESS_TOKEN =
        "APP_USR-1505-080815-c6ea450de1bf828e39add499237d727f-312667294";
    public static final String PAYER_1_CARD_VISA_ID = "279999120";
    public static final String PAYER_1_CARD_VISA_LAST_FOUR = "5678";
    private String payerWithCardAccessToken = PAYER_1_WITH_CARDS_ACCESS_TOKEN;
    private String cardId;
    private String paymentMethodId;
    private String paymentTypeId;

    public SavedCardTestFlow(@NonNull final String cardId, @NonNull final String paymentMethodId,
        @NonNull final Context context) {
        this.cardId = cardId;
        this.paymentMethodId = paymentMethodId;
        this.context = context;
        this.checkout = getMercadoPagoCheckout().build();
    }

    public SavedCardTestFlow(String paymentTypeId, @NonNull final Context context) {
        payerWithCardAccessToken = null;
        this.paymentTypeId = paymentTypeId;
        this.context = context;
        this.checkout = getMercadoPagoCheckout().build();
    }

    public SavedCardTestFlow(@NonNull final MercadoPagoCheckout mercadoPagoCheckout,
        @NonNull final Context context) {
        payerWithCardAccessToken = null;
        this.context = context;
        this.checkout = mercadoPagoCheckout;
    }

    public CongratsPage runDefaultCardIdPaymentFlow() {
        startCheckout();
        new SecurityCodePage(new DefaultValidator())
            .enterSecurityCodeForNewCard(CVV_NUMBER);
        return new ReviewAndConfirmPage().pressConfirmButton();
    }

    @Deprecated
    public CongratsPage runInvalidDefaultCardIdPaymentFlow() {
        final PaymentMethodPage paymentMethodPage = new PaymentMethodPage(null);
        startCheckout();

        paymentMethodPage
            .selectSavedDebitCard()
            .enterSecurityCodeForNewCard(CVV_NUMBER);
        return new ReviewAndConfirmPage().pressConfirmButton();
    }

    public CongratsPage runSavedCardFlowWithoutEsc(@NonNull final String lastFourDigits) {
        startCheckout();

        return new PaymentMethodPage(new DefaultValidator()).selectVisaCreditCardWithoutEsc(lastFourDigits)
            .selectInstallmentsForSavedCard(1)
            .enterSecurityCodeForSavedCard(CVV_NUMBER)
            .pressConfirmButton();
    }

    public BusinessCongratsPage runBusinessSavedCardFlowWithoutEsc(@NonNull final String lastFourDigits) {
        startCheckout();

        return new PaymentMethodPage(new DefaultValidator()).selectVisaCreditCardWithoutEsc(lastFourDigits)
            .selectInstallmentsForSavedCard(1)
            .enterSecurityCodeForSavedCard(CVV_NUMBER)
            .pressConfirmButtonforBusiness();
    }

    public BusinessCongratsPage runBusinessSavedCardFlowWithEscAccepted(@NonNull final String lastFourDigits) {
        startCheckout();

        return new PaymentMethodPage(new DefaultValidator()).selectVisaCreditCardWithoutEsc(lastFourDigits)
            .selectInstallmentsForSavedCardWithEsc(1)
            .pressConfirmButtonforBusiness();
    }

    public CongratsPage runSavedCardFlowWithEscAccepted(@NonNull final String lastFourDigits) {
        startCheckout();

        return new PaymentMethodPage(new DefaultValidator()).selectVisaCreditCardWithoutEsc(lastFourDigits)
            .selectInstallmentsForSavedCardWithEsc(1)
            .pressConfirmButton();
    }

    public RejectedPage runSavedCardFlowWithEscRejected(@NonNull final String lastFourDigits) {
        startCheckout();

        return new PaymentMethodPage(new DefaultValidator()).selectVisaCreditCardWithoutEsc(lastFourDigits)
            .selectInstallmentsForSavedCardWithEsc(1)
            .pressConfirmButtonAndReject();
    }

    public CongratsPage runSavedCardFlowWithInvalidEsc(@NonNull final String lastFourDigits) {
        startCheckout();

        return new PaymentMethodPage(new DefaultValidator())
            .selectVisaCreditCardWithoutEsc(lastFourDigits)
            .selectInstallmentsForSavedCardWithEsc(1)
            .pressConfirmButtonWithInvalidEsc()
            .enterSecurityCodeForSavedCard(CVV_NUMBER)
            .pressConfirmButton();
    }

    public CongratsPage runNewCardPaymentFlow(@NonNull final Card card) {
        startCheckout();
        return new DebitCardPage()
            .enterCreditCardNumber(card.cardNumber())
            .enterCardholderName(card.cardHolderName())
            .enterExpiryDate(card.expDate())
            .enterSecurityCodeForNewCard(card.escNumber())
            .enterIdentificationNumberToReviewAndConfirm(card.cardHolderIdentityNumber())
            .pressConfirmButton();
    }

    private MercadoPagoCheckout.Builder getMercadoPagoCheckout() {
        final CheckoutPreference checkoutPreference = getCheckoutPreference(getFakeItem());
        return new MercadoPagoCheckout.Builder(PUBLIC_KEY, checkoutPreference, new PaymentConfiguration.Builder(
            new PaymentProcessor() {
                @Override
                public void startPayment(@NonNull final CheckoutData data, @NonNull final Context context,
                    @NonNull final OnPaymentListener paymentListener) {
                    paymentListener.onPaymentFinished(new BusinessPayment.Builder(
                        BusinessPayment.Decorator.APPROVED,
                        "", "", "", ""
                    ).setPrimaryButton(new ExitAction("asd", 123)).build());
                }

                @Override
                public int getPaymentTimeout() {
                    return 0;
                }

                @Override
                public boolean shouldShowFragmentOnPayment() {
                    return false;
                }

                @Nullable
                @Override
                public Bundle getFragmentBundle(@NonNull final CheckoutData data, @NonNull final Context context) {
                    return null;
                }

                @Nullable
                @Override
                public Fragment getFragment(@NonNull final CheckoutData data, @NonNull final Context context) {
                    return null;
                }
            }).build())
            .setPrivateKey(payerWithCardAccessToken);
    }

    @NonNull
    private CheckoutPreference getCheckoutPreference(final Item item) {
        final CheckoutPreference checkoutPreference = new CheckoutPreference.Builder(
            Sites.ARGENTINA, "a@a.a", Collections.singletonList(item))
            .build();
        if (paymentTypeId != null) {
            checkoutPreference.getPaymentPreference().setDefaultPaymentTypeId(paymentTypeId);
            checkoutPreference.getPaymentPreference().setDefaultCardId(cardId);
        } else {
            checkoutPreference.getPaymentPreference().setDefaultPaymentMethodId(paymentMethodId);
            checkoutPreference.getPaymentPreference().setDefaultCardId(cardId);
        }
        return checkoutPreference;
    }

    @NonNull
    private Item getFakeItem() {
        return new Item.Builder("sarasa", 1, new BigDecimal(100)).setId("sarasa").build();
    }
}