package com.mercadopago.android.px.testcheckout.assertions;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.testcheckout.idleresources.WaitForBusinessResult;
import com.mercadopago.android.px.testcheckout.idleresources.WaitForPaymentResult;
import com.mercadopago.android.px.testcheckout.pages.BusinessCongratsPage;
import com.mercadopago.android.px.testcheckout.pages.CallForAuthPage;
import com.mercadopago.android.px.testcheckout.pages.CardAssociationResultErrorPage;
import com.mercadopago.android.px.testcheckout.pages.CardAssociationResultSuccessPage;
import com.mercadopago.android.px.testcheckout.pages.CardPage;
import com.mercadopago.android.px.testcheckout.pages.CashPage;
import com.mercadopago.android.px.testcheckout.pages.CongratsPage;
import com.mercadopago.android.px.testcheckout.pages.CreditCardPage;
import com.mercadopago.android.px.testcheckout.pages.DebitCardPage;
import com.mercadopago.android.px.testcheckout.pages.DiscountDetailPage;
import com.mercadopago.android.px.testcheckout.pages.ExpiryDatePage;
import com.mercadopago.android.px.testcheckout.pages.ExpressPaymentPage;
import com.mercadopago.android.px.testcheckout.pages.IdentificationPage;
import com.mercadopago.android.px.testcheckout.pages.InstallmentsPage;
import com.mercadopago.android.px.testcheckout.pages.IssuerPage;
import com.mercadopago.android.px.testcheckout.pages.NamePage;
import com.mercadopago.android.px.testcheckout.pages.NoCheckoutPage;
import com.mercadopago.android.px.testcheckout.pages.OneTapPage;
import com.mercadopago.android.px.testcheckout.pages.PayerInformationBusinessNamePage;
import com.mercadopago.android.px.testcheckout.pages.PayerInformationFirstNamePage;
import com.mercadopago.android.px.testcheckout.pages.PayerInformationIdentificationPage;
import com.mercadopago.android.px.testcheckout.pages.PayerInformationLastNamePage;
import com.mercadopago.android.px.testcheckout.pages.PayerInformationPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentMethodPage;
import com.mercadopago.android.px.testcheckout.pages.PaymentTypesPage;
import com.mercadopago.android.px.testcheckout.pages.PendingPage;
import com.mercadopago.android.px.testcheckout.pages.RejectedPage;
import com.mercadopago.android.px.testcheckout.pages.ReviewAndConfirmPage;
import com.mercadopago.android.px.testcheckout.pages.ReviewPaymentMethodsPage;
import com.mercadopago.android.px.testcheckout.pages.SecurityCodePage;
import com.mercadopago.android.px.testcheckout.pages.SecurityCodeToResultsPage;
import org.hamcrest.Matcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.mercadopago.android.testlib.matchers.CustomViewMatchers.withDigitsOnlyEqualsToTextView;
import static com.mercadopago.android.testlib.matchers.CustomViewMatchers.withValueEqualToTextView;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertTrue;

public class DefaultValidator implements CheckoutValidator {

    private final WaitForPaymentResult waitForPaymentResult = new WaitForPaymentResult();

    @Override
    public void validate(@NonNull final IssuerPage issuerPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final NamePage namePage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final PaymentMethodPage paymentMethodPage) {
        //TODO fix, does not work
//        validateAmountView();
    }

    /**
     * @deprecated Use PayerInformationIdentificationPage as entry point of this flow.
     */
    @Deprecated
    @Override
    public void validate(@NonNull final PayerInformationPage payerInformationPage) {
        //TODO implement default PX Validations
    }

    @CallSuper
    @Override
    public void validate(@NonNull final PayerInformationIdentificationPage page) {
        onView(withId(com.mercadopago.android.px.R.id.mpsdkIdentificationTypeId))
            .check(matches(withValueEqualToTextView(com.mercadopago.android.px.R.id.mpsdkItemTitle)));

        onView(withId(com.mercadopago.android.px.R.id.mpsdkIdNumberView)).check(
            matches(withDigitsOnlyEqualsToTextView(com.mercadopago.android.px.R.id.mpsdkCardIdentificationNumber)));
    }

    @CallSuper
    @Override
    public void validate(@NonNull final PayerInformationBusinessNamePage page) {
        // Checks empty input or input equals to output
        onView(withId(com.mercadopago.android.px.R.id.mpsdkBusinessName)).check(matches(anyOf(withText(TextUtil.EMPTY),
            withValueEqualToTextView(com.mercadopago.android.px.R.id.mpsdkNameView))));
    }

    @CallSuper
    @Override
    public void validate(@NonNull final PayerInformationFirstNamePage page) {
        // Checks empty input or input equals to output
        onView(withId(com.mercadopago.android.px.R.id.mpsdkName)).check(matches(anyOf(withText(TextUtil.EMPTY),
            withValueEqualToTextView(com.mercadopago.android.px.R.id.mpsdkNameView))));
    }

    @CallSuper
    @Override
    public void validate(@NonNull final PayerInformationLastNamePage page) {
        onView(withId(com.mercadopago.android.px.R.id.mpsdkLastnameView))
            .check(matches(withValueEqualToTextView(com.mercadopago.android.px.R.id.mpsdkLastName)));
    }

    @Override
    public void validate(@NonNull final ReviewAndConfirmPage reviewAndConfirmPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final SecurityCodePage securityCodePage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final NoCheckoutPage noCheckoutPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CardPage cardPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CashPage cashPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CreditCardPage creditCardPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final DebitCardPage debitCardPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final ExpiryDatePage expiryDatePage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final IdentificationPage identificationPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final InstallmentsPage installmentsPage) {
        //TODO fix, does not work
//        validateAmountView();
    }

    @Override
    public void validate(@NonNull final ReviewPaymentMethodsPage reviewPaymentMethodsPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final DiscountDetailPage discountDetailPage) {
        final Matcher<View> discountDetailLine = withId(com.mercadopago.android.px.R.id.px_discount_detail_line);
        final Matcher<View> discountSubDetails = withId(com.mercadopago.android.px.R.id.px_discount_sub_details);
        onView(discountDetailLine).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(discountSubDetails).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(discountSubDetails)
            .check(matches(withText(com.mercadopago.android.px.R.string.px_we_apply_the_best_available_discount)));
    }

    @Override
    public void validate(@NonNull final SecurityCodeToResultsPage securityCodeToResultsPage) {
        //TODO implement default PX Validations
    }

    @Deprecated
    @Override
    public void validate(@NonNull final OneTapPage oneTapPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CallForAuthPage callForAuthPage) {
        //TODO implement default PX Validations
    }

    @Override
    public void validate(@NonNull final CongratsPage congratsPage) {
        waitForPaymentResult.start();
        assertTrue(congratsPage.isSuccess());
        waitForPaymentResult.stop();

    }

    public void validate(@NonNull final BusinessCongratsPage congratsPage) {
        final WaitForBusinessResult result = new WaitForBusinessResult();
        result.start();
        assertTrue(congratsPage.isSuccess());
        result.stop();
    }

    @Override
    public void validate(@NonNull final PendingPage pendingPage) {
        waitForPaymentResult.start();
        assertTrue(pendingPage.isPending());
        waitForPaymentResult.stop();    }

    @Override
    public void validate(@NonNull final RejectedPage rejectedPage) {
        waitForPaymentResult.start();
        assertTrue(rejectedPage.isError());
        waitForPaymentResult.stop();
    }

    @Override
    public void validate(@NonNull final ExpressPaymentPage expressPaymentPage) {
        //TODO implement default PX Validations
    }

    @CallSuper
    @Override
    public void validate(@NonNull final CardAssociationResultSuccessPage cardAssociationResultSuccessPage) {
        onView(ViewMatchers.withId(com.mercadopago.android.px.R.id.mpsdkCardAssociationResultSuccessBadge))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Override
    public void validate(@NonNull final CardAssociationResultErrorPage cardAssociationResultErrorPage) {
        //TODO implement default PX Validations
    }

    @CallSuper
    @Override
    public void validate(@NonNull final PaymentTypesPage paymentTypesPage) {
        onView(withId(com.mercadopago.android.px.R.id.mpsdkActivityPaymentTypesRecyclerView))
            .check(matches(hasMinimumChildCount(2)));
    }

    private void validateAmountView() {
        final Matcher<View> amountDescription = withId(com.mercadopago.android.px.R.id.amount_description);
        final Matcher<View> maxCouponAmount = withId(com.mercadopago.android.px.R.id.max_coupon_amount);
        final Matcher<View> amountBeforeDiscount =
            withId(com.mercadopago.android.px.R.id.amount_before_discount);
        final Matcher<View> finalAmount = withId(com.mercadopago.android.px.R.id.final_amount);
        final Matcher<View> arrow = withId(com.mercadopago.android.px.R.id.blue_arrow);
        onView(amountDescription).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(maxCouponAmount).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(amountBeforeDiscount).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(finalAmount).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(arrow).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(amountDescription).check(matches(withText(
            getInstrumentation().getTargetContext().getString(com.mercadopago.android.px.R.string.px_total_to_pay))));
    }
}
