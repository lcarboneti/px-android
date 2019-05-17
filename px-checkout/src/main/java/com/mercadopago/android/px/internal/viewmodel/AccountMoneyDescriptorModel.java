package com.mercadopago.android.px.internal.viewmodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.textformatter.AmountLabeledFormatter;
import com.mercadopago.android.px.internal.util.textformatter.SpannableFormatter;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.model.AccountMoneyMetadata;

public class AccountMoneyDescriptorModel extends PaymentMethodDescriptorView.Model {

    private final AccountMoneyMetadata accountMoneyMetadata;

    @NonNull
    public static PaymentMethodDescriptorView.Model createFrom(
        @NonNull final AccountMoneyMetadata accountMoneyMetadata, final boolean disabledPaymentMethod) {
        return new AccountMoneyDescriptorModel(accountMoneyMetadata, disabledPaymentMethod);
    }

    /* default */ AccountMoneyDescriptorModel(@NonNull final AccountMoneyMetadata accountMoneyMetadata,
        final boolean disabledPaymentMethod) {
        this.accountMoneyMetadata = accountMoneyMetadata;
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
            amountLabeledFormatter.apply(R.string.px_payment_method_disable_account_money_title);
        } else if (TextUtil.isEmpty(accountMoneyMetadata.displayInfo.sliderTitle)) {
            spannableStringBuilder.append(TextUtil.SPACE);
        } else {
            final AmountLabeledFormatter amountLabeledFormatter =
                new AmountLabeledFormatter(spannableStringBuilder, context)
                    .withTextColor(ContextCompat.getColor(context, R.color.ui_meli_grey));
            amountLabeledFormatter.apply(accountMoneyMetadata.displayInfo.sliderTitle);
        }
    }
}