package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.constants.ProcessingModes;
import com.mercadopago.android.px.internal.repository.IssuersRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.IssuersService;
import com.mercadopago.android.px.model.Issuer;
import java.util.List;

import static com.mercadopago.android.px.services.BuildConfig.API_ENVIRONMENT;
import static com.mercadopago.android.px.services.BuildConfig.API_VERSION;

public class IssuersServiceImp implements IssuersRepository {

    @NonNull private final IssuersService issuersService;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;

    public IssuersServiceImp(@NonNull final IssuersService issuersService,
        @NonNull final PaymentSettingRepository paymentSettingRepository) {
        this.issuersService = issuersService;
        this.paymentSettingRepository = paymentSettingRepository;
    }

    @Override
    public MPCall<List<Issuer>> getIssuers(final String paymentMethodId, final String bin) {
        return issuersService.getIssuers(API_ENVIRONMENT,
            API_VERSION, paymentSettingRepository.getPublicKey(),
            paymentSettingRepository.getPrivateKey(), paymentMethodId, bin, ProcessingModes.AGGREGATOR);
    }
}
