package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.features.express.slider.SplitPaymentHeaderAdapter;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.ExpressMetadata;

public class SplitHeaderMapper extends Mapper<ExpressMetadata, SplitPaymentHeaderAdapter.Model> {

    @NonNull private final String currencyId;
    @NonNull private final AmountConfigurationRepository amountConfigurationRepository;

    public SplitHeaderMapper(@NonNull final String currencyId,
        @NonNull final AmountConfigurationRepository amountConfigurationRepository) {
        this.currencyId = currencyId;
        this.amountConfigurationRepository = amountConfigurationRepository;
    }

    @Override
    public SplitPaymentHeaderAdapter.Model map(@NonNull final ExpressMetadata val) {
        if (val.isCard()) {
            final AmountConfiguration config =
                amountConfigurationRepository.getConfigurationFor(val.getCard().getId());
            return config.allowSplit() ? new SplitPaymentHeaderAdapter.SplitModel(currencyId,
                config.getSplitConfiguration())
                : new SplitPaymentHeaderAdapter.Empty();
        }
        return new SplitPaymentHeaderAdapter.Empty();
    }
}