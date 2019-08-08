package com.mercadopago.android.px.testcheckout.pages;

import android.support.test.InstrumentationRegistry;
import com.mercadopago.android.px.testcheckout.assertions.CheckoutValidator;
import com.mercadopago.android.testlib.pages.PageObject;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.mercadopago.android.testlib.matchers.UtilityMatchers.withBackgroundColor;

public class RejectedPage extends PageObject<CheckoutValidator> {

    public RejectedPage() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    public RejectedPage(CheckoutValidator validator) {
        super(validator);
    }

    @Override
    public RejectedPage validate(CheckoutValidator validator) {
        validator.validate(this);
        return this;
    }

    public boolean isError() {
        onView(withId(com.mercadopago.android.px.R.id.headerContainer))
            .check(matches(withBackgroundColor(
                InstrumentationRegistry.getTargetContext().getResources()
                    .getColor(com.mercadopago.android.px.R.color.ui_components_error_color))));
        return true;
    }

}
