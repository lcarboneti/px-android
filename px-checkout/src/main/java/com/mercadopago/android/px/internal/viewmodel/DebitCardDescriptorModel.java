package com.mercadopago.android.px.internal.viewmodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.textformatter.AmountLabeledFormatter;
import com.mercadopago.android.px.internal.util.textformatter.SpannableFormatter;
import com.mercadopago.android.px.internal.util.textformatter.TextFormatter;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.PayerCost;

/**
 * Model used to instantiate PaymentMethodDescriptorView for payment methods with payer costs. This model is used for
 * debit_card
 */
public final class DebitCardDescriptorModel extends PaymentMethodDescriptorView.Model {

    private final String currencyId;
    private final AmountConfiguration amountConfiguration;

    @NonNull
    public static PaymentMethodDescriptorView.Model createFrom(
        @NonNull final String currencyId,
        @NonNull final AmountConfiguration amountConfiguration, final boolean disabledPaymentMethod) {
        return new DebitCardDescriptorModel(currencyId, amountConfiguration, disabledPaymentMethod);
    }

    private DebitCardDescriptorModel(@NonNull final String currencyId,
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
        }
    }

    private void updateInstallment(@NonNull final SpannableStringBuilder spannableStringBuilder,
        @NonNull final Context context,
        @NonNull final TextView textView) {

        if (amountConfiguration.allowSplit()) {
            final Spannable amount = TextFormatter.withCurrencyId(currencyId)
                .amount(getCurrent().getInstallmentAmount())
                .normalDecimals()
                .into(textView)
                .toSpannable();

            final AmountLabeledFormatter amountLabeledFormatter =
                new AmountLabeledFormatter(spannableStringBuilder, context)
                    .withTextColor(ContextCompat.getColor(context, R.color.ui_meli_black))
                    .withSemiBoldStyle();
            amountLabeledFormatter.apply(amount);
        }
    }

    @NonNull
    private PayerCost getCurrent() {
        return amountConfiguration.getCurrentPayerCost(userWantToSplit, payerCostSelected);
    }
}
