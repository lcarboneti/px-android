package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.ItemsModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.LineSeparatorType;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.PaymentModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.SummaryModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.List;

public class ReviewAndConfirmBuilder {

    private Boolean hasExtraPaymentMethods;
    private PostPaymentAction postPaymentAction;

    public ReviewAndConfirmBuilder setHasExtraPaymentMethods(final boolean hasExtraPaymentMethods) {
        this.hasExtraPaymentMethods = hasExtraPaymentMethods;
        return this;
    }

    public ReviewAndConfirmBuilder setPostPaymentAction(@NonNull final PostPaymentAction postPaymentAction) {
        this.postPaymentAction = postPaymentAction;
        return this;
    }

    public Intent getIntent(@NonNull final Context context) {
        final Resources resources = context.getResources();
        final Session session = Session.getSession(context);
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final UserSelectionRepository userSelectionRepository =
            configurationModule.getUserSelectionRepository();

        final Issuer issuer = userSelectionRepository.getIssuer();

        final PaymentSettingRepository paymentSettings = configurationModule.getPaymentSettings();
        final Site site = paymentSettings.getSite();
        final String publicKey = paymentSettings.getPublicKey();
        final Token token = paymentSettings.getToken();
        final AmountRepository amountRepository = session.getAmountRepository();

        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
        final CheckoutPreference checkoutPreference = paymentSettings.getCheckoutPreference();

        final DiscountConfigurationModel discountModel = session.getDiscountRepository().getCurrentConfiguration();

        final List<Item> items = checkoutPreference.getItems();

        final String title = SummaryModel.resolveTitle(items,
            resources.getString(R.string.px_review_summary_product),
            resources.getString(R.string.px_review_summary_products));

        final boolean termsAndConditionsEnabled =
            TextUtil.isEmpty(paymentSettings.getPrivateKey());

        final TermsAndConditionsModel mercadoPagoTermsAndConditions =
            termsAndConditionsEnabled ? new TermsAndConditionsModel(
                site.getTermsAndConditionsUrl(),
                resources.getString(R.string.px_terms_and_conditions_message),
                resources.getString(R.string.px_terms_and_conditions_linked_message),
                LineSeparatorType.TOP_LINE_SEPARATOR) : null;

        final TermsAndConditionsModel discountTermsAndConditions =
            discountModel.getCampaign() != null
                ? new TermsAndConditionsModel(discountModel.getCampaign().getLegalTermsUrl(),
                resources.getString(R.string.px_discount_terms_and_conditions_message),
                resources.getString(R.string.px_discount_terms_and_conditions_linked_message),
                LineSeparatorType.BOTTOM_LINE_SEPARATOR) : null;

        final PaymentModel paymentModel =
            new PaymentModel(paymentMethod, token, issuer, hasExtraPaymentMethods);

        final SummaryModel summaryModel =
            new SummaryModel(amountRepository.getAmountToPay(), paymentMethod, site,
                userSelectionRepository.getPayerCost(), discountModel.getDiscount(), title,
                checkoutPreference.getTotalAmount(),
                amountRepository.getAppliedCharges());

        final ItemsModel itemsModel = new ItemsModel(site.getCurrencyId(), items);
        if (postPaymentAction != null) {
            return ReviewAndConfirmActivity.getIntentForAction(context,
                publicKey,
                mercadoPagoTermsAndConditions,
                paymentModel,
                summaryModel,
                itemsModel,
                discountTermsAndConditions,
                postPaymentAction);
        }
        return ReviewAndConfirmActivity.getIntent(context,
            publicKey,
            mercadoPagoTermsAndConditions,
            paymentModel,
            summaryModel,
            itemsModel,
            discountTermsAndConditions);
    }
}
