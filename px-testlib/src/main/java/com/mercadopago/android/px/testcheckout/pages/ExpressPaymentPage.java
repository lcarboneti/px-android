package com.mercadopago.android.px.testcheckout.pages;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class ExpressPaymentPage extends PageObject<CheckoutValidator> {

    public ExpressPaymentPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public ExpressPaymentPage(final CheckoutValidator validator) {
        super(validator);
    }

    public DiscountDetailPage pressOnDiscountDetail() {
        onView(withId(R.id.icon_descriptor_amount)).perform(click());
        return new DiscountDetailPage(validator);
    }

    public SecurityCodeToResultsPage pressConfirmButtonToCvv() {
        onView(withId(R.id.confirm_button)).perform(click());
        return new SecurityCodeToResultsPage(validator);
    }

    public ExpressPaymentPage swipeLeft() {
        onView(withId(R.id.payment_method_pager))
            .perform(ViewActions.swipeLeft());
        return this;
    }

    public ExpressPaymentPage swipeRight() {
        onView(withId(R.id.payment_method_pager))
            .perform(ViewActions.swipeRight());
        return this;
    }

    public ExpressPaymentPage openInstallments() {
        onView(withId(R.id.installments_header)).perform(click());
        return this;
    }

    public ExpressPaymentPage selectPayerCostAt(int position) {
        onView(withId(R.id.installments_recycler_view))
            .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        return this;
    }

    public CongratsPage pressConfirmButtonToCongratsPage() {
        onView(withId(R.id.confirm_button)).perform(click());
        return new CongratsPage(validator);
    }

    public RejectedPage pressConfirmButtonToRejectedPage() {
        onView(withId(R.id.confirm_button)).perform(click());
        return new RejectedPage(validator);
    }

    @Override
    public ExpressPaymentPage validate(final CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }
}
