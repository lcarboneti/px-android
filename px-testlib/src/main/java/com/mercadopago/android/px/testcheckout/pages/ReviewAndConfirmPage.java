package com.mercadopago.android.px.testcheckout.pages;

import android.support.annotation.NonNull;
import android.support.test.espresso.action.ViewActions;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;
import com.mercadopago.android.testlib.utils.NestedScroll;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class ReviewAndConfirmPage extends PageObject<CheckoutValidator> {

    public ReviewAndConfirmPage() {
    }

    public ReviewAndConfirmPage(final CheckoutValidator validator) {
        super(validator);
    }

    public CongratsPage pressConfirmButton() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new CongratsPage(validator);
    }

    public BusinessCongratsPage pressConfirmButtonforBusiness() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new BusinessCongratsPage(validator);
    }

    public SecurityCodePage pressConfirmButtonWithInvalidEsc() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new SecurityCodePage(validator);
    }

    public RejectedPage pressConfirmButtonAndReject() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new RejectedPage(validator);
    }

    public PendingPage pressConfirmButtonAndPending() {
        onView(withId(R.id.floating_confirm)).perform(click());
        return new PendingPage(validator);
    }

    @Override
    public ReviewAndConfirmPage validate(final CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public NoCheckoutPage pressBackWithExclusion() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new NoCheckoutPage(validator);
    }

    public PaymentMethodPage pressBack() {
        onView(isRoot()).perform(ViewActions.pressBack());
        return new PaymentMethodPage(validator);
    }

    @NonNull
    public PaymentMethodPage clickChangePaymentMethod() {
        onView(withText(R.string.px_change_payment)).perform(NestedScroll.nestedScrollTo()).perform(click());
        return new PaymentMethodPage(validator);
    }

    /**
     * @deprecated use clickModifyPayerInformation instead.
     */
    @Deprecated
    @NonNull
    public PayerInformationPage pressModifyPayerInformation() {
        onView(withText(R.string.px_payer_information_modify)).perform(NestedScroll.nestedScrollTo()).perform(click());
        return new PayerInformationPage(validator);
    }

    @NonNull
    public PayerInformationIdentificationPage clickModifyPayerInformation() {
        onView(withText(R.string.px_payer_information_modify)).perform(NestedScroll.nestedScrollTo()).perform(click());
        return new PayerInformationIdentificationPage(validator);
    }
}