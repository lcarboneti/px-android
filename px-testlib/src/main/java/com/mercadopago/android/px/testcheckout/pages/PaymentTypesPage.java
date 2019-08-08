package com.mercadopago.android.px.testcheckout.pages;

import com.mercadopago.android.px.R;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class PaymentTypesPage extends PageObject<CheckoutValidator> {
    public PaymentTypesPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public PaymentTypesPage(CheckoutValidator validator) {
        super(validator);
    }

    public CardAssociationResultSuccessPage selectDebitCardTypeToCardAssociationResultSuccessPage() {
        final int debit = R.string.px_debit_payment_type;
        onView(withText(debit)).perform(click());
        return new CardAssociationResultSuccessPage(validator);
    }

    public CardAssociationResultSuccessPage selectCreditCardTypeToCardAssociationResultSuccessPage() {
        final int credit = R.string.px_credit_payment_type;
        onView(withText(credit)).perform(click());
        return new CardAssociationResultSuccessPage(validator);
    }

    @Override
    public PaymentTypesPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
