package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.services.Callback;
import java.util.Map;

public class DiscountServiceImp implements DiscountRepository {

    /* default */ ConfigurationSolver configurationSolver;
    /* default */ Map<String, DiscountConfigurationModel> discountsConfigurations;

    @Nullable private String defaultSelectedGuessingConfiguration;
    @NonNull private final InitRepository initRepository;
    private final UserSelectionRepository userSelectionRepository;

    public DiscountServiceImp(@NonNull final InitRepository initRepository,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.initRepository = initRepository;
        this.userSelectionRepository = userSelectionRepository;
    }

    @NonNull
    @Override
    public DiscountConfigurationModel getCurrentConfiguration() {
        // TODO: remove
        init();

        final Card card = userSelectionRepository.getCard();
        // Remember to prioritize the selected discount over the rest when the selector feature is added.
        if (card != null) {
            return getConfiguration(configurationSolver.getConfigurationHashFor(card.getId()));
        } else {
            final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
            if (paymentMethod == null) {
                // The user did not select any payment method, thus the dominant discount is the general config
                return getConfiguration(configurationSolver.getDefaultSelectedAmountConfiguration());
            } else {
                // Guessing card
                if (PaymentTypes.isCardPaymentType(paymentMethod.getPaymentTypeId())) {
                    // Guessing card config
                    return getConfiguration(defaultSelectedGuessingConfiguration);
                } else if (PaymentTypes.isAccountMoney(paymentMethod.getPaymentTypeId())) {
                    // Account money has it's own configuration as saved cards
                    return getConfiguration(configurationSolver.getConfigurationHashFor(paymentMethod.getId()));
                } else {
                    // Off payment not custom options
                    // The user select account money or an off payment method / everything else.
                    return getConfiguration(configurationSolver.getConfigurationHashFor(
                        paymentMethod.getId()));
                }
            }
        }
    }

    @Override
    public DiscountConfigurationModel getConfigurationFor(@NonNull final String customOptionId) {
        init();
        return getConfiguration(configurationSolver.getConfigurationHashFor(customOptionId));
    }

    private DiscountConfigurationModel getConfiguration(@Nullable final String hash) {
        // TODO: remove
        init();
        final DiscountConfigurationModel discountModel = discountsConfigurations.get(hash);
        final DiscountConfigurationModel defaultConfig =
            discountsConfigurations.get(configurationSolver.getDefaultSelectedAmountConfiguration());
        if (discountModel == null && defaultConfig == null) {
            return DiscountConfigurationModel.NONE;
        }
        return discountModel == null ? defaultConfig : discountModel;
    }

    //TODO: remove init call.
    private void init() {
        if (configurationSolver != null && discountsConfigurations != null) {
            return;
        }

        initRepository.getInit().execute(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                configurationSolver =
                    new ConfigurationSolverImpl(paymentMethodSearch.getDefaultAmountConfiguration(),
                        paymentMethodSearch.getCustomSearchItems());
                discountsConfigurations = paymentMethodSearch.getDiscountsConfigurations();
            }

            @Override
            public void failure(final ApiException apiException) {
                //TODO
            }
        });
    }

    @Override
    public void addConfigurations(@NonNull final SummaryAmount summaryAmount) {
        // TODO: remove
        init();
        discountsConfigurations.putAll(summaryAmount.getDiscountsConfigurations());
        defaultSelectedGuessingConfiguration = summaryAmount.getDefaultAmountConfiguration();
    }
}