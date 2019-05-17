package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.TokenRepository;
import com.mercadopago.android.px.internal.services.GatewayService;
import com.mercadopago.android.px.internal.util.EscUtil;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Device;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.events.EscFrictionEventTracker;

public class TokenizeService implements TokenRepository {

    @NonNull private final GatewayService gatewayService;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final IESCManager escManager;
    @NonNull private final Device device;

    public TokenizeService(@NonNull final GatewayService gatewayService,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final IESCManager escManager,
        @NonNull final Device device) {
        this.gatewayService = gatewayService;
        this.paymentSettingRepository = paymentSettingRepository;
        this.escManager = escManager;
        this.device = device;
    }

    @Override
    public MPCall<Token> createToken(@NonNull final Card card) {
        return new MPCall<Token>() {
            private final String esc = escManager.getESC(card.getId(), card.getFirstSixDigits(), card.getLastFourDigits());

            @Override
            public void enqueue(final Callback<Token> callback) {
                serviceCallWrapp(card.getId(), esc).enqueue(wrap(card, esc, callback));
            }

            @Override
            public void execute(final Callback<Token> callback) {
                serviceCallWrapp(card.getId(), esc).enqueue(wrap(card, esc, callback));
            }
        };
    }

    /* default */ Callback<Token> wrap(@NonNull final Card card, final String esc, final Callback<Token> callback) {
        return new Callback<Token>() {
            @Override
            public void success(final Token token) {
                //TODO move to esc manager  / Token repo
                token.setLastFourDigits(card.getLastFourDigits());
                escManager.saveESCWith(card.getId(), token.getEsc());
                paymentSettingRepository.configure(token);
                callback.success(token);
            }

            @Override
            public void failure(final ApiException apiException) {
                //TODO move to esc manager  / Token repo
                if (EscUtil.isInvalidEscForApiException(apiException)) {
                    paymentSettingRepository.configure((Token) null);
                    escManager.deleteESCWith(card.getId());
                    EscFrictionEventTracker.create(card.getId(), esc, apiException).track();
                }

                callback.failure(apiException);
            }
        };
    }

    /* default */ MPCall<Token> serviceCallWrapp(@NonNull final String cardId, @NonNull final String esc) {
        return gatewayService.createToken(paymentSettingRepository.getPublicKey(),
            paymentSettingRepository.getPrivateKey(),
            SavedESCCardToken.createWithEsc(cardId, esc, device));
    }
}
