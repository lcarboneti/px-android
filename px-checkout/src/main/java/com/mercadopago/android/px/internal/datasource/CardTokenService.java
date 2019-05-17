package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.repository.CardTokenRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.GatewayService;
import com.mercadopago.android.px.internal.util.EscUtil;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.Device;
import com.mercadopago.android.px.model.SavedCardToken;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.requests.SecurityCodeIntent;
import com.mercadopago.android.px.services.Callback;

public class CardTokenService implements CardTokenRepository {

    /* default */ @NonNull final PaymentSettingRepository paymentSettingRepository;
    /* default */ @NonNull final IESCManager escManager;
    @NonNull private final Device device;
    @NonNull private final GatewayService gatewayService;

    public CardTokenService(@NonNull final GatewayService gatewayService,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final Device device,
        @NonNull final IESCManager escManager) {
        this.gatewayService = gatewayService;
        this.paymentSettingRepository = paymentSettingRepository;
        this.device = device;
        this.escManager = escManager;
    }

    /* default */ Callback<Token> wrap(@NonNull final CardToken card, final Callback<Token> callback) {
        final CardInfo cardInfo = CardInfo.create(card);
        return new Callback<Token>() {
            @Override
            public void success(final Token token) {
                escManager.saveESCWith(cardInfo.getFirstSixDigits(), cardInfo.getLastFourDigits(), token.getEsc());
                paymentSettingRepository.configure(token);
                callback.success(token);
            }

            @Override
            public void failure(final ApiException apiException) {
                if (EscUtil.isInvalidEscForApiException(apiException)) {
                    paymentSettingRepository.configure((Token) null);
                    escManager.deleteESCWith(cardInfo.getFirstSixDigits(), cardInfo.getLastFourDigits());
                }

                callback.failure(apiException);
            }
        };
    }


    @Override
    public MPCall<Token> createTokenAsync(final CardToken cardToken) {
        cardToken.setDevice(device);
        cardToken.setRequireEsc(escManager.isESCEnabled());
        return new MPCall<Token>() {
            @Override
            public void enqueue(final Callback<Token> callback) {
                gatewayService
                    .createToken(paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey(),
                        cardToken).enqueue(wrap(cardToken, callback));
            }

            @Override
            public void execute(final Callback<Token> callback) {
                gatewayService
                    .createToken(paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey(),
                        cardToken).execute(wrap(cardToken, callback));
            }
        };
    }

    @Override
    public MPCall<Token> createToken(final SavedCardToken savedCardToken) {
        savedCardToken.setDevice(device);
        return gatewayService
            .createToken(paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey(),
                savedCardToken);
    }

    @Override
    public MPCall<Token> createToken(final SavedESCCardToken savedESCCardToken) {
        savedESCCardToken.setDevice(device);
        return gatewayService
            .createToken(paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey(),
                savedESCCardToken);
    }

    @Override
    public MPCall<Token> cloneToken(final String tokenId) {
        return gatewayService
            .cloneToken(tokenId, paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey());
    }

    @Override
    public MPCall<Token> putSecurityCode(final String securityCode, final String tokenId) {
        final SecurityCodeIntent securityCodeIntent = new SecurityCodeIntent();
        securityCodeIntent.setSecurityCode(securityCode);
        return gatewayService
            .updateToken(tokenId, paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey(),
                securityCodeIntent);
    }
}
