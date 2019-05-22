package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.viewmodel.AccountMoneyDescriptorModel;
import com.mercadopago.android.px.internal.viewmodel.CreditCardDescriptorModel;
import com.mercadopago.android.px.internal.viewmodel.DebitCardDescriptorModel;
import com.mercadopago.android.px.internal.viewmodel.EmptyInstallmentsDescriptorModel;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PaymentTypes;
import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDescriptorMapper
    extends Mapper<List<ExpressMetadata>, List<PaymentMethodDescriptorView.Model>> {

    @NonNull private final PaymentSettingRepository paymentConfiguration;
    @NonNull private final AmountConfigurationRepository amountConfigurationRepository;
    @NonNull private DisabledPaymentMethodRepository disabledPaymentMethodRepository;

    public PaymentMethodDescriptorMapper(@NonNull final PaymentSettingRepository paymentConfiguration,
        @NonNull final AmountConfigurationRepository amountConfigurationRepository,
        @NonNull final DisabledPaymentMethodRepository disabledPaymentMethodRepository) {
        this.paymentConfiguration = paymentConfiguration;
        this.amountConfigurationRepository = amountConfigurationRepository;
        this.disabledPaymentMethodRepository = disabledPaymentMethodRepository;
    }

    @Override
    public List<PaymentMethodDescriptorView.Model> map(@NonNull final List<ExpressMetadata> expressMetadataList) {
        final List<PaymentMethodDescriptorView.Model> models = new ArrayList<>();

        for (final ExpressMetadata expressMetadata : expressMetadataList) {
            models.add(createInstallmentsDescriptorModel(expressMetadata));
        }
        //Last card is Add new payment method card
        models.add(createAddNewPaymentModel());

        return models;
    }

    private PaymentMethodDescriptorView.Model createInstallmentsDescriptorModel(final ExpressMetadata expressMetadata) {
        final String paymentTypeId = expressMetadata.getPaymentTypeId();
        final CardMetadata cardMetadata = expressMetadata.getCard();

        if (PaymentTypes.isCreditCardPaymentType(paymentTypeId)) {
            //This model is useful for Credit Card only
            return CreditCardDescriptorModel
                .createFrom(paymentConfiguration.getSite().getCurrencyId(),
                    amountConfigurationRepository.getConfigurationFor(cardMetadata.getId()),
                    disabledPaymentMethodRepository.hasPaymentMethodId(cardMetadata.getId()));
        } else if (PaymentTypes.isCardPaymentType(paymentTypeId)) {
            return DebitCardDescriptorModel
                .createFrom(paymentConfiguration.getSite().getCurrencyId(),
                    amountConfigurationRepository.getConfigurationFor(cardMetadata.getId()),
                    disabledPaymentMethodRepository.hasPaymentMethodId(cardMetadata.getId()));
        } else if (PaymentTypes.isAccountMoney(expressMetadata.getPaymentMethodId())) {
            return AccountMoneyDescriptorModel.createFrom(expressMetadata.getAccountMoney(),
                disabledPaymentMethodRepository.hasPaymentMethodId(expressMetadata.getPaymentMethodId()));
        } else {
            return EmptyInstallmentsDescriptorModel.create();
        }
    }

    private PaymentMethodDescriptorView.Model createAddNewPaymentModel() {
        return EmptyInstallmentsDescriptorModel.create();
    }
}
