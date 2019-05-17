package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CustomSearchItem;

public final class CustomSearchItemToCardMapper extends Mapper<CustomSearchItem, Card> {

    @Override
    public Card map(@NonNull final CustomSearchItem val) {
        final Card card = new Card();
        card.setId(val.getId());
        card.setIssuer(val.getIssuer());
        card.setLastFourDigits(val.getLastFourDigits());
        card.setFirstSixDigits(val.getFirstSixDigits());
        return card;
    }
}
