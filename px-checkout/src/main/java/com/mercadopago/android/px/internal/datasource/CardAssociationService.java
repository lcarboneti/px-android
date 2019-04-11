package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.constants.ProcessingModes;
import com.mercadopago.android.px.internal.services.CardService;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Issuer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mercadopago.android.px.services.BuildConfig.API_ENVIRONMENT;
import static com.mercadopago.android.px.services.BuildConfig.API_VERSION;

public class CardAssociationService {
    @NonNull private final CardService cardService;

    public CardAssociationService(@NonNull final CardService cardService) {
        this.cardService = cardService;
    }

    public MPCall<Card> associateCardToUser(@NonNull final String accessToken, @NonNull final String cardTokenId,
        @NonNull final String paymentMethodId, @Nullable final Long issuerId) {
        final HashMap<String, Object> body = new HashMap<>();
        body.put("card_token_id", cardTokenId);
        final Map<String, Object> paymentMethodBody = new HashMap<>();
        paymentMethodBody.put("id", paymentMethodId);
        final Map<String, Object> issuerBody = new HashMap<>();
        issuerBody.put("id", issuerId);
        body.put("payment_method", paymentMethodBody);
        body.put("issuer", issuerBody);

        return cardService.assignCard(API_ENVIRONMENT,
            API_VERSION, accessToken, body);
    }

    public MPCall<List<Issuer>> getCardIssuers(@NonNull final String accessToken, @NonNull final String paymentMethodId,
        @NonNull final String bin) {
        return cardService
            .getCardIssuers(API_ENVIRONMENT,
                API_VERSION, accessToken, paymentMethodId, bin, ProcessingModes.AGGREGATOR);
    }
}
