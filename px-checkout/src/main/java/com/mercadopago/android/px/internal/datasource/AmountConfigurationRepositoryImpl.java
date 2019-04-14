package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.services.Callback;
import java.util.ArrayList;

public class AmountConfigurationRepositoryImpl implements AmountConfigurationRepository {

    /* default */ @Nullable ConfigurationSolver configurationSolver;

    @NonNull private final InitRepository initRepository;
    private final UserSelectionRepository userSelectionRepository;

    public AmountConfigurationRepositoryImpl(@NonNull final InitRepository initRepository,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.initRepository = initRepository;
        this.userSelectionRepository = userSelectionRepository;
    }

    @NonNull
    @Override
    public AmountConfiguration getCurrentConfiguration() throws IllegalStateException {
        init();

        if (userSelectionRepository.getCard() != null) { // Saved card
            return configurationSolver.getAmountConfigurationFor(userSelectionRepository.getCard().getId());
        } else if (PaymentTypes.isAccountMoney(userSelectionRepository.getPaymentMethod().getPaymentTypeId())) {
            return configurationSolver.getAmountConfigurationFor(userSelectionRepository.getPaymentMethod().getId());
        } else {
            throw new IllegalStateException(
                "Payer costs shouldn't be requested without a selected card or account money");
        }
    }

    @NonNull
    @Override
    public AmountConfiguration getConfigurationFor(@NonNull final String customOptionId) {
        init();
        return configurationSolver
            .getAmountConfigurationFor(customOptionId, configurationSolver.getConfigurationHashFor(customOptionId));
    }

    private void init() {
        if (configurationSolver != null) {
            return;
        }

        initRepository.getInit().execute(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                configurationSolver =
                    new ConfigurationSolverImpl(paymentMethodSearch.getDefaultAmountConfiguration(),
                        paymentMethodSearch.getCustomSearchItems());
            }

            @Override
            public void failure(final ApiException apiException) {
                configurationSolver = new ConfigurationSolverImpl(TextUtil.EMPTY, new ArrayList<CustomSearchItem>());
            }
        });
    }
}
