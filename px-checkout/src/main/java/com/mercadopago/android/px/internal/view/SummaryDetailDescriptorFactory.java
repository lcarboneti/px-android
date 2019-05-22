package com.mercadopago.android.px.internal.view;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.DiscountAmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.DiscountDescriptionLocalized;
import com.mercadopago.android.px.internal.viewmodel.DiscountDetailColor;
import com.mercadopago.android.px.internal.viewmodel.DiscountDetailDrawable;
import com.mercadopago.android.px.internal.viewmodel.ItemDetailColor;
import com.mercadopago.android.px.internal.viewmodel.ItemLocalized;
import com.mercadopago.android.px.internal.viewmodel.SoldOutDiscountDetailColor;
import com.mercadopago.android.px.internal.viewmodel.SoldOutDiscountLocalized;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.internal.SummaryInfo;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.ArrayList;
import java.util.List;

public class SummaryDetailDescriptorFactory {

    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final DiscountConfigurationModel discountModel;
    @NonNull private final SummaryInfo summaryInfo;

    public SummaryDetailDescriptorFactory(@NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final DiscountConfigurationModel discountModel,
        @NonNull final SummaryInfo summaryInfo) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.discountModel = discountModel;
        this.summaryInfo = summaryInfo;
    }

    public List<AmountDescriptorView.Model> create() {
        final String currencyId = paymentSettingRepository.getSite().getCurrencyId();
        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();

        final List<AmountDescriptorView.Model> list = new ArrayList<>();

        if (discountModel.getDiscount() != null) {
            list.add(new AmountDescriptorView.Model(new ItemLocalized(summaryInfo),
                new AmountLocalized(checkoutPreference.getTotalAmount(), currencyId), new ItemDetailColor()));
            list.add(new AmountDescriptorView.Model(new DiscountDescriptionLocalized(discountModel.getDiscount()),
                new DiscountAmountLocalized(discountModel.getDiscount().getCouponAmount(), currencyId),
                new DiscountDetailColor())
                .setDetailDrawable(new DiscountDetailDrawable()).enableListener());
        }

        if (!discountModel.isAvailable()) {
            list.add(new AmountDescriptorView.Model(new ItemLocalized(summaryInfo),
                new AmountLocalized(checkoutPreference.getTotalAmount(), currencyId), new ItemDetailColor()));
            list.add(new AmountDescriptorView.Model(new SoldOutDiscountLocalized(), new SoldOutDiscountDetailColor())
                .setDetailDrawable(new DiscountDetailDrawable(), new SoldOutDiscountDetailColor())
                .enableListener());
        }

        return list;
    }
}
