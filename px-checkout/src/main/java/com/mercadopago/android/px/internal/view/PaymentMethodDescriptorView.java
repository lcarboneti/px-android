package com.mercadopago.android.px.internal.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;
import com.mercadopago.android.px.model.PayerCost;

public class PaymentMethodDescriptorView extends MPTextView {

    public PaymentMethodDescriptorView(final Context context) {
        this(context, null);
    }

    public PaymentMethodDescriptorView(final Context context,
        @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaymentMethodDescriptorView(final Context context, @Nullable final AttributeSet attrs,
        final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void update(@NonNull final Model model) {
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        model.updateSpannable(spannableStringBuilder, this);
        setText(spannableStringBuilder);
    }

    public abstract static class Model {

        protected int payerCostSelected = PayerCost.NO_SELECTED;
        protected boolean userWantToSplit = true;
        protected boolean disabledPaymentMethod;

        public abstract void updateSpannable(@NonNull final SpannableStringBuilder spannableStringBuilder,
            @NonNull final TextView textView);

        public final void setCurrentPayerCost(final int payerCostSelected) {
            this.payerCostSelected = payerCostSelected;
        }

        public final void setSplit(final boolean split) {
            userWantToSplit = split;
        }

        public boolean hasPayerCostList() {
            return false;
        }

        public boolean isDisabledPaymentMethod() {
            return disabledPaymentMethod;
        }
    }
}