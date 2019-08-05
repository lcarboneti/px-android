package com.mercadopago.android.px.testcheckout.pages;

import android.support.annotation.NonNull;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.model.PaymentMethods;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static org.hamcrest.Matchers.is;

public class PaymentMethodPage extends PageObject<CheckoutValidator> {

    protected PaymentMethodPage() {
    }

    public PaymentMethodPage(final CheckoutValidator validator) {
        super(validator);
    }

    //Payer access token 1 = "APP_USR-1505-080815-c6ea450de1bf828e39add499237d727f-312667294"
    public InstallmentsPage selectVisaCreditCardWithoutEsc(final String lastFourDigits) {
        clickViewWithTag(getFormattedTag(PaymentTypes.CREDIT_CARD, lastFourDigits));
        return new InstallmentsPage(validator);
    }

    public ReviewAndConfirmPage selectAccountMoney() {
        clickViewWithTag(PaymentTypes.ACCOUNT_MONEY);
        return new ReviewAndConfirmPage(validator);
    }

    @Deprecated
    public SecurityCodePage selectSavedDebitCard() {
        onView(withId(R.id.mpsdkGroupsList))
            .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        return new SecurityCodePage(validator);
    }

    public SecurityCodePage selectSavedDebitCard(@NonNull final String lastFourDigits) {
        clickViewWithTag(getFormattedTag(PaymentTypes.DEBIT_CARD, lastFourDigits));
        return new SecurityCodePage(validator);
    }

    public CardPage selectCard() {
        clickViewWithTag("cards");
        return new CardPage(validator);
    }

    @Deprecated
    public CreditCardPage selectCardWhenSavedPresent() {
        clickViewWithTag(PaymentTypes.CREDIT_CARD);
        return new CreditCardPage(validator);
    }

    public CashPage selectCash() {
        clickViewWithTag(PaymentTypes.TICKET);
        return new CashPage(validator);
    }

    public CashPage selectATM() {
        clickViewWithTag(PaymentTypes.ATM);
        return new CashPage(validator);
    }

    @Deprecated
    public ReviewAndConfirmPage selectTicketWithDefaultPayer(final int paymentMethodPosition) {
        return selectTicketWithDefaultPayer(paymentMethodPosition == 0 ? PaymentMethods.BRASIL.BOLBRADESCO :
            PaymentMethods.BRASIL.PEC);
    }

    @Deprecated
    public PayerInformationPage selectTicketWithoutPayer(final int paymentMethodPosition) {
         selectTicketWithoutPayer(paymentMethodPosition == 0 ? PaymentMethods.BRASIL.BOLBRADESCO :
            PaymentMethods.BRASIL.PEC);
         return new PayerInformationPage(validator);
    }

    public ReviewAndConfirmPage selectTicketWithDefaultPayer(@NonNull final String paymentType) {
        clickViewWithTag(paymentType);
        return new ReviewAndConfirmPage(validator);
    }

    public PayerInformationIdentificationPage selectTicketWithoutPayer(@NonNull final String paymentType) {
        clickViewWithTag(paymentType);
        return new PayerInformationIdentificationPage(validator);
    }

    public InstallmentsPage selectPaymentMethodToInstallments(@NonNull final String paymentMethod) {
        clickViewWithTag(paymentMethod);
        return new InstallmentsPage(validator);
    }

    public SecurityCodePage selectPaymentMethodToSecurityCode(@NonNull final String paymentMethod) {
        clickViewWithTag(paymentMethod);
        return new SecurityCodePage(validator);
    }

    public ReviewAndConfirmPage selectPaymentMethodToReviewAndConfirm(@NonNull final String paymentMethod) {
        clickViewWithTag(paymentMethod);
        return new ReviewAndConfirmPage(validator);
    }

    public DiscountDetailPage pressOnDiscountDetail() {
        onView(withId(R.id.amount_view)).perform(click());
        return new DiscountDetailPage(validator);
    }

    public OneTapPage pressBack() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new OneTapPage(validator);
    }

    @Override
    public PaymentMethodPage validate(final CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    private String getFormattedTag(@NonNull final String paymentType, @NonNull final String lastFourDigits) {
        return String.format("%1$s/%2$s", paymentType, lastFourDigits);
    }

    private void clickViewWithTag(@NonNull final String tag) {
        onView(withTagValue(is(tag))).perform(click());
    }
}