package com.mercadopago.android.px.testcheckout.assertions;

import android.support.annotation.NonNull;
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
import com.mercadopago.android.testlib.assertions.Validator;

public interface CheckoutValidator extends Validator {

    void validate(@NonNull final CardPage cardPage);

    void validate(@NonNull final CashPage cashPage);

    void validate(@NonNull final CongratsPage congratsPage);

    void validate(@NonNull final BusinessCongratsPage congratsPage);

    void validate(@NonNull final CreditCardPage creditCardPage);

    void validate(@NonNull final DebitCardPage debitCardPage);

    void validate(@NonNull final ExpiryDatePage expiryDatePage);

    void validate(@NonNull final IdentificationPage identificationPage);

    void validate(@NonNull final InstallmentsPage installmentsPage);

    void validate(@NonNull final IssuerPage issuerPage);

    void validate(@NonNull final NamePage namePage);

    void validate(@NonNull final PaymentMethodPage paymentMethodPage);

    /**
     * @deprecated Use PayerInformationIdentificationPage as entry point of this flow.
     */
    @Deprecated
    void validate(@NonNull final PayerInformationPage payerInformationPage);

    void validate(@NonNull final PayerInformationIdentificationPage payerInformationPage);

    void validate(@NonNull final PayerInformationBusinessNamePage payerInformationPage);

    void validate(@NonNull final PayerInformationFirstNamePage payerInformationPage);

    void validate(@NonNull final PayerInformationLastNamePage payerInformationPage);

    void validate(@NonNull final ReviewAndConfirmPage reviewAndConfirmPage);

    void validate(@NonNull final SecurityCodePage securityCodePage);

    void validate(@NonNull final NoCheckoutPage noCheckoutPage);

    void validate(@NonNull final ReviewPaymentMethodsPage reviewPaymentMethodsPage);

    void validate(@NonNull final DiscountDetailPage discountDetailPage);

    void validate(@NonNull final SecurityCodeToResultsPage securityCodeToResultsPage);

    void validate(@NonNull final CallForAuthPage callForAuthPage);

    void validate(@NonNull final PendingPage pendingPage);

    void validate(@NonNull final RejectedPage rejectedPage);

    void validate(@NonNull final ExpressPaymentPage expressPaymentPage);

    void validate(@NonNull final CardAssociationResultSuccessPage cardAssociationResultSuccessPage);

    void validate(@NonNull final CardAssociationResultErrorPage cardAssociationResultSuccessPage);

    void validate(@NonNull final PaymentTypesPage paymentTypesPage);

    /**
     * @deprecated this page does not exists anymore.
     */
    @Deprecated
    void validate(@NonNull final OneTapPage oneTapPage);

}