package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.model.BusinessPayment;

public class BusinessModelMapper extends Mapper<BusinessPayment, BusinessPaymentModel> {

    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final PaymentRepository paymentRepository;

    public BusinessModelMapper(@NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final PaymentRepository paymentRepository) {

        this.paymentSettingRepository = paymentSettingRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public BusinessPaymentModel map(@NonNull final BusinessPayment val) {
        return new BusinessPaymentModel(val,
            paymentSettingRepository.getSite().getCurrencyId(),
            paymentRepository.getPaymentDataList());
    }
}
