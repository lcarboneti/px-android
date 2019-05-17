package com.mercadopago.android.px.internal.viewmodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.textformatter.AmountLabeledFormatter;
import com.mercadopago.android.px.internal.util.textformatter.CFTFormatter;
import com.mercadopago.android.px.internal.util.textformatter.InterestFormatter;
import com.mercadopago.android.px.internal.util.textformatter.PayerCostFormatter;
import com.mercadopago.android.px.internal.util.textformatter.SpannableFormatter;
import com.mercadopago.android.px.internal.util.textformatter.TextFormatter;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.PayerCost;
import java.math.BigDecimal;

/**
 * Model used to instantiate PaymentMethodDescriptorView for payment methods with payer costs. This model is used for
 * credit_card
 */
public final class CreditCardDescriptorModel extends PaymentMethodDescriptorView.Model {

    private final String currencyId;
    private final AmountConfiguration amountConfiguration;

    @NonNull
    public static PaymentMethodDescriptorView.Model createFrom(
        @NonNull final String currencyId,
        @NonNull final AmountConfiguration amountConfiguration,
        final boolean disabledPaymentMethod) {
        return new CreditCardDescriptorModel(currencyId, amountConfiguration, disabledPaymentMethod);
    }

    @Override
    public boolean hasPayerCostList() {
        return amountConfiguration.getAppliedPayerCost(userWantToSplit).size() > 1;
    }

    private CreditCardDescriptorModel(@NonNull final String currencyId,
        @NonNull final AmountConfiguration amountConfiguration, final boolean disabledPaymentMethod) {
        this.currencyId = currencyId;
        this.amountConfiguration = amountConfiguration;
        this.disabledPaymentMethod = disabledPaymentMethod;
    }

    @Override
    public void updateSpannable(@NonNull final SpannableStringBuilder spannableStringBuilder,
        @NonNull final TextView textView) {
        final Context context = textView.getContext();
        if (disabledPaymentMethod) {
            final SpannableFormatter amountLabeledFormatter =
                new SpannableFormatter(spannableStringBuilder, context)
                    .withTextColor(ContextCompat.getColor(context, R.color.ui_meli_grey));
            amountLabeledFormatter.apply(R.string.px_payment_method_disable_card_title);
        } else {
            updateInstallment(spannableStringBuilder, context, textView);
            updateTotalAmountDescriptionSpannable(spannableStringBuilder, context);
            updateInterestDescriptionSpannable(spannableStringBuilder, context);
            updateCFTSpannable(spannableStringBuilder, context);
        }
    }

    private void updateInstallment(@NonNull final SpannableStringBuilder spannableStringBuilder,
        @NonNull final Context context,
        @NonNull final TextView textView) {

        final Spannable amount = TextFormatter.withCurrencyId(currencyId)
            .amount(getCurrent().getInstallmentAmount())
            .normalDecimals()
            .into(textView)
            .toSpannable();

        final AmountLabeledFormatter amountLabeledFormatter =
            new AmountLabeledFormatter(spannableStringBuilder, context)
                .withInstallment(getCurrent().getInstallments())
                .withTextColor(ContextCompat.getColor(context, R.color.ui_meli_black))
                .withSemiBoldStyle();
        amountLabeledFormatter.apply(amount);
    }

    /**
     * Updates total amount the user will pay with credit card, only if there are interests involved.
     */
    private void updateTotalAmountDescriptionSpannable(@NonNull final SpannableStringBuilder spannableStringBuilder,
        @NonNull final Context context) {
        if (BigDecimal.ZERO.compareTo(getCurrent().getInstallmentRate()) < 0) {
            final PayerCostFormatter payerCostFormatter =
                new PayerCostFormatter(spannableStringBuilder, context,
                    getCurrent(), currencyId)
                    .withTextColor(ContextCompat.getColor(context, R.color.ui_meli_grey));
            payerCostFormatter.apply();
        }
    }

    private void updateInterestDescriptionSpannable(@NonNull final SpannableStringBuilder spannableStringBuilder,
        @NonNull final Context context) {
        if (getCurrent().getInstallments() > 1 && BigDecimal.ZERO.compareTo(getCurrent().getInstallmentRate()) == 0) {
            final InterestFormatter interestFormatter = new InterestFormatter(spannableStringBuilder, context)
                .withTextColor(ContextCompat.getColor(context, R.color.px_discount_description));
            interestFormatter.apply();
        }
    }

    private void updateCFTSpannable(@NonNull final SpannableStringBuilder spannableStringBuilder,
        @NonNull final Context context) {
        final CFTFormatter cftFormatter = new CFTFormatter(spannableStringBuilder, context, getCurrent())
            .withTextColor(ContextCompat.getColor(context, R.color.ui_meli_grey));
        cftFormatter.build();
    }

    @NonNull
    private PayerCost getCurrent() {
        return amountConfiguration.getCurrentPayerCost(userWantToSplit, payerCostSelected);
    }
}
