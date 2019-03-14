package com.mercadopago.android.px.internal.viewmodel.drawables;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.internal.viewmodel.CardDrawerConfiguration;

public class SavedCardDrawableFragmentItem extends DrawableFragmentItem {

    @NonNull public final String paymentMethodId;
    @NonNull public final CardDrawerConfiguration card;

    public SavedCardDrawableFragmentItem(@NonNull final String paymentMethodId,
        @NonNull final CardDrawerConfiguration card, @NonNull final String cardId) {
        this.paymentMethodId = paymentMethodId;
        this.card = card;
        id = cardId;
    }

    @Override
    public Fragment draw(@NonNull final PaymentMethodFragmentDrawer drawer) {
        return drawer.draw(this);
    }
}