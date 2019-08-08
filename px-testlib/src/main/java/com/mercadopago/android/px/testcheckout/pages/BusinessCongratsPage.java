package com.mercadopago.android.px.testcheckout.pages;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.test.InstrumentationRegistry;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;
import org.hamcrest.Matchers;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.mercadopago.android.testlib.matchers.UtilityMatchers.withBackgroundColor;

public class BusinessCongratsPage extends PageObject<CheckoutValidator> {

    public BusinessCongratsPage(final CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public BusinessCongratsPage validate(final CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public void clickButtonWithText(@NonNull final String text) {
        onView(withText(Matchers.containsString(text))).perform(click());
    }

    public void clickButtonWithText(@StringRes final int resId) {
        onView(withText(resId)).perform(click());
    }

    public void clickPrimaryButton() {
        onView(withId(com.mercadopago.android.px.R.id.px_button_primary)).perform(click());
    }

    public boolean isSuccess() {
        onView(withId(com.mercadopago.android.px.R.id.headerContainer))
            .check(matches(withBackgroundColor(
                InstrumentationRegistry.getTargetContext().getResources()
                    .getColor(com.mercadopago.android.px.R.color.ui_components_success_color))));
        return true;
    }
}
