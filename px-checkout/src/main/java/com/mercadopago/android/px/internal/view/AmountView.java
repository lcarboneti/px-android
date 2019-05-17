package com.mercadopago.android.px.internal.view;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.util.DiscountHelper;
import com.mercadopago.android.px.internal.util.textformatter.TextFormatter;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.Site;
import java.math.BigDecimal;

public class AmountView extends LinearLayoutCompat {

    /* default */
    @Nullable
    OnClick callback;

    private TextView amountDescription;
    private View amountContainer;
    private TextView amountBeforeDiscount;
    private TextView maxCouponAmount;
    private TextView finalAmount;
    private View line;
    private View arrow;
    private View mainContainer;

    public interface OnClick {
        void onDetailClicked(@NonNull final DiscountConfigurationModel discountModel);
    }

    public AmountView(@NonNull final Context context) {
        super(context);
        init();
    }

    public void setOnClickListener(final OnClick callback) {
        this.callback = callback;
    }

    public AmountView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmountView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void show(@NonNull final DiscountConfigurationModel discountModel, @NonNull final BigDecimal totalAmount,
        @NonNull final Site site) {
        if (!discountModel.isAvailable()) {
            showNotAvailableDiscount(discountModel, totalAmount, site);
        } else if (discountModel.hasValidDiscount()) {
            showWithDiscount(discountModel, totalAmount, site);
        } else {
            show(totalAmount, site);
        }
    }

    private void init() {
        inflate(getContext(), R.layout.px_amount_layout, this);
        mainContainer = findViewById(R.id.main_container);
        line = findViewById(R.id.line);
        amountDescription = findViewById(R.id.amount_description);
        amountBeforeDiscount = findViewById(R.id.amount_before_discount);
        finalAmount = findViewById(R.id.final_amount);
        maxCouponAmount = findViewById(R.id.max_coupon_amount);
        arrow = findViewById(R.id.blue_arrow);
        amountContainer = findViewById(R.id.amount_container);
        amountBeforeDiscount.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        configureElevation();
    }

    private void configureElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(getContext().getResources().getDimension(R.dimen.px_xxs_margin));
            line.setVisibility(GONE);
        } else {
            line.setVisibility(VISIBLE);
        }
    }

    private void showWithDiscount(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final BigDecimal totalAmount, @NonNull final Site site) {
        showDiscount(discountModel, totalAmount, site);
        showEffectiveAmount(totalAmount.subtract(discountModel.getDiscount().getCouponAmount()), site);
    }

    private void showNotAvailableDiscount(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final BigDecimal totalAmount, @NonNull final Site site) {
        configureViewsVisibilityWhenNotAvailableDiscount(discountModel);
        amountDescription.setText(R.string.px_used_up_discount_row);
        amountDescription.setTextColor(getResources().getColor(R.color.px_form_text));
        showEffectiveAmount(totalAmount, site);
    }

    private void show(@NonNull final BigDecimal totalAmount, @NonNull final Site site) {
        configureViewsVisibilityDefault();
        final String mainVerb = getContext().getString(Session.getSession(getContext()).getMainVerb());
        amountDescription.setText(getContext().getString(R.string.px_total_to_pay, mainVerb));
        amountDescription.setTextColor(getResources().getColor(R.color.px_form_text));
        showEffectiveAmount(totalAmount, site);
    }

    private void configureViewsVisibilityWhenNotAvailableDiscount(
        @NonNull final DiscountConfigurationModel discountModel) {
        amountBeforeDiscount.setVisibility(GONE);
        maxCouponAmount.setVisibility(GONE);
        arrow.setVisibility(VISIBLE);
        configureOnOnDetailClickedEvent(discountModel);
    }

    private void configureViewsVisibilityDefault() {
        amountBeforeDiscount.setVisibility(GONE);
        maxCouponAmount.setVisibility(GONE);

        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) amountContainer.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        amountContainer.setLayoutParams(params);
        arrow.setVisibility(GONE);
    }

    private void showDiscount(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final BigDecimal totalAmount, @NonNull final Site site) {
        configureDiscountAmountDescription(discountModel.getDiscount(), discountModel.getCampaign());
        configureViewsVisibilityWhenDiscount(totalAmount, site);
        configureOnOnDetailClickedEvent(discountModel);
    }

    private void configureOnOnDetailClickedEvent(@NonNull final DiscountConfigurationModel discountModel) {
        mainContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (callback != null) {
                    callback.onDetailClicked(discountModel);
                }
            }
        });
    }

    private void configureViewsVisibilityWhenDiscount(@NonNull final BigDecimal totalAmount,
        @NonNull final Site site) {
        arrow.setVisibility(VISIBLE);
        amountBeforeDiscount.setVisibility(VISIBLE);
        TextFormatter.withCurrencyId(site.getCurrencyId())
            .withSpace()
            .amount(totalAmount)
            .normalDecimals()
            .into(amountBeforeDiscount);

        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) amountContainer.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
        }

        amountContainer.setLayoutParams(params);
    }

    private void configureDiscountAmountDescription(final Discount discount, final Campaign campaign) {
        amountDescription.setVisibility(VISIBLE);
        amountDescription.setTextColor(getResources().getColor(R.color.px_discount_description));
        configureDiscountOffMessage(discount);
        configureMaxCouponAmountMessage(campaign);
    }

    private void showEffectiveAmount(@NonNull final BigDecimal totalAmount, @NonNull final Site site) {
        TextFormatter.withCurrencyId(site.getCurrencyId())
            .withSpace()
            .amount(totalAmount)
            .normalDecimals()
            .into(finalAmount);
    }

    private void configureMaxCouponAmountMessage(final Campaign campaign) {
        if (campaign.hasMaxCouponAmount()) {
            maxCouponAmount.setVisibility(VISIBLE);
            maxCouponAmount.setText(R.string.px_with_max_coupon_amount);
        } else {
            maxCouponAmount.setVisibility(GONE);
        }
    }

    private void configureDiscountOffMessage(final Discount discount) {
        amountDescription.setText(DiscountHelper.getDiscountDescription(getContext(), discount));
    }
}
