package com.mercadopago.android.px.internal.viewmodel;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.util.DiscountHelper;
import com.mercadopago.android.px.model.Discount;

public class DiscountDescriptionLocalized implements ILocalizedCharSequence {

    private final Discount discount;

    public DiscountDescriptionLocalized(@NonNull final Discount discount) {
        this.discount = discount;
    }

    @Override
    public CharSequence get(@NonNull final Context context) {
        return DiscountHelper.getDiscountDescription(context, discount);
    }
}
