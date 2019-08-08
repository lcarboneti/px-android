package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.SummaryAmountRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.services.InstallmentService;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.DifferentialPricing;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.HashMap;
import java.util.Map;

import static com.mercadopago.android.px.services.BuildConfig.API_ENVIRONMENT;

public class SummaryAmountService implements SummaryAmountRepository {

    @NonNull private final InstallmentService installmentService;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final AdvancedConfiguration advancedConfiguration;
    @NonNull private final UserSelectionRepository userSelectionRepository;

    public SummaryAmountService(@NonNull final InstallmentService installmentService,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final AdvancedConfiguration advancedConfiguration,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.installmentService = installmentService;
        this.paymentSettingRepository = paymentSettingRepository;
        this.advancedConfiguration = advancedConfiguration;
        this.userSelectionRepository = userSelectionRepository;
    }

    @NonNull
    @Override
    public MPCall<SummaryAmount> getSummaryAmount(@NonNull final String bin) {
        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
        final DifferentialPricing differentialPricing = checkoutPreference.getDifferentialPricing();
        final Integer differentialPricingId = differentialPricing == null ? null : differentialPricing.getId();
        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
        final Issuer issuer = userSelectionRepository.getIssuer();

        final Map<String, Object> body = new HashMap<>();
        body.put("site_id", checkoutPreference.getSite().getId());
        body.put("transaction_amount", checkoutPreference.getTotalAmount());
        body.put("marketplace", checkoutPreference.getMarketplace());
        if (TextUtil.isNotEmpty(checkoutPreference.getPayer().getEmail())) {
            body.put("email", checkoutPreference.getPayer().getEmail());
        }
        body.put("product_id", advancedConfiguration.getDiscountParamsConfiguration().getProductId());
        body.put("payment_method_id", paymentMethod.getId());
        body.put("payment_type", paymentMethod.getPaymentTypeId());
        body.put("bin", bin);
        body.put("issuer_id", issuer.getId());
        body.put("labels", advancedConfiguration.getDiscountParamsConfiguration().getLabels());
        body.put("default_installments", checkoutPreference.getDefaultInstallments());
        body.put("max_installments", checkoutPreference.getMaxInstallments());
        body.put("differential_pricing_id", differentialPricingId);
        body.put("processing_modes", paymentMethod.getProcessingModes());
        body.put("branch_id", checkoutPreference.getBranchId());
        body.put("charges", paymentSettingRepository.getPaymentConfiguration().getCharges());

        return installmentService.createSummaryAmount(API_ENVIRONMENT, body,
            paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey());
    }
}