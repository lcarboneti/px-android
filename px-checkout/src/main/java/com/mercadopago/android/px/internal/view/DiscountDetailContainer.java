package com.mercadopago.android.px.internal.view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.DiscountHelper;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.util.textformatter.TextFormatter;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import javax.annotation.Nonnull;

public class DiscountDetailContainer extends CompactComponent<DiscountDetailContainer.Props, Void> {

    public static final class Props {

        @NonNull
        /* default */ final DialogTitleType dialogTitleType;
        @NonNull
        /* default */ final DiscountConfigurationModel discountModel;

        public Props(@NonNull final DialogTitleType dialogTitleType,
            @Nonnull final DiscountConfigurationModel discountModel) {
            this.dialogTitleType = dialogTitleType;
            this.discountModel = discountModel;
        }

        public enum DialogTitleType {
            BIG, SMALL
        }
    }

    public DiscountDetailContainer(@NonNull final DiscountDetailContainer.Props props) {
        super(props);
    }

    @Nullable
    @Override
    public View render(@Nonnull final ViewGroup parent) {
        addDiscountTitle(parent);
        addDiscountDetail(parent);
        return null;
    }

    private void addDiscountDetail(@NonNull final ViewGroup parent) {
        final View discountView =
            new DiscountDetail(new DiscountDetail.Props(props.discountModel))
                .render(parent);

        parent.addView(discountView);
    }

    private void addDiscountTitle(final ViewGroup parent) {
        final MPTextView title = getTitleTextView(parent);

        if (!props.discountModel.isAvailable()) {
            configureNotAvailableDiscountTitle(title);
        } else {
            configureOffTitle(title, props.discountModel.getDiscount());
        }
        parent.addView(title);
    }

    private void configureNotAvailableDiscountTitle(final MPTextView textView) {
        textView.setText(R.string.px_used_up_discount_title);
    }

    private void configureOffTitle(final MPTextView textView, final Discount discount) {
        textView.setText(DiscountHelper.getDiscountDescription(textView.getContext(), discount));
    }

    private MPTextView getTitleTextView(final ViewGroup parent) {
        return props.dialogTitleType.equals(Props.DialogTitleType.BIG) ? (MPTextView) ViewUtils.inflate(parent,
            R.layout.px_view_big_modal_title)
            : (MPTextView) ViewUtils.inflate(parent, R.layout.px_view_small_modal_title);
    }
}