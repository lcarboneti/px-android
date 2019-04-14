package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.features.installments.PayerCostSolver;
import com.mercadopago.android.px.internal.repository.BankDealsRepository;
import com.mercadopago.android.px.internal.repository.CardTokenRepository;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.IssuersRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.SummaryAmountRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.services.Callback;
import java.lang.reflect.Type;
import java.util.List;
import retrofit2.http.HEAD;

public class GuessingCardPaymentPresenter extends GuessingCardPresenter implements SummaryAmountListener {

    @NonNull /* default */ final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final UserSelectionRepository userSelectionRepository;
    @NonNull private final InitRepository initRepository;
    @NonNull private final IssuersRepository issuersRepository;
    @NonNull private final CardTokenRepository cardTokenRepository;
    @NonNull private final BankDealsRepository bankDealsRepository;
    @NonNull private final IdentificationRepository identificationRepository;
    @NonNull private final AdvancedConfiguration advancedConfiguration;
    @NonNull private final SummaryAmountRepository summaryAmountRepository;
    @NonNull private final IssuersSolver issuersSolver;
    @NonNull private final PayerCostSolver payerCostSolver;
    @Nullable private List<BankDeal> bankDealList;

    protected PaymentRecovery paymentRecovery;

    public GuessingCardPaymentPresenter(@NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final InitRepository initRepository,
        @NonNull final IssuersRepository issuersRepository,
        @NonNull final CardTokenRepository cardTokenRepository,
        @NonNull final BankDealsRepository bankDealsRepository,
        @NonNull final IdentificationRepository identificationRepository,
        @NonNull final AdvancedConfiguration advancedConfiguration,
        @NonNull final PaymentRecovery paymentRecovery,
        @NonNull final SummaryAmountRepository summaryAmountRepository,
        @NonNull final IssuersSolver issuersSolver,
        @NonNull final PayerCostSolver payerCostSolver) {
        super();
        this.userSelectionRepository = userSelectionRepository;
        this.paymentSettingRepository = paymentSettingRepository;
        this.initRepository = initRepository;
        this.issuersRepository = issuersRepository;
        this.cardTokenRepository = cardTokenRepository;
        this.bankDealsRepository = bankDealsRepository;
        this.identificationRepository = identificationRepository;
        this.advancedConfiguration = advancedConfiguration;
        this.paymentRecovery = paymentRecovery;
        this.summaryAmountRepository = summaryAmountRepository;
        this.issuersSolver = issuersSolver;
        this.payerCostSolver = payerCostSolver;
    }

    @Override
    public void initialize() {
        getView().onValidStart();
        resolveBankDeals();
        getPaymentMethods();
        if (recoverWithCardHolder()) {
            fillRecoveryFields();
        }
    }

    private void fillRecoveryFields() {
        getView().setCardholderName(paymentSettingRepository.getToken().getCardHolder().getName());
        getView().setIdentificationNumber(
            paymentSettingRepository.getToken().getCardHolder().getIdentification().getNumber());
    }

    @Nullable
    @Override
    public PaymentMethod getPaymentMethod() {
        return userSelectionRepository.getPaymentMethod();
    }

    @Override
    public void setPaymentMethod(@Nullable final PaymentMethod paymentMethod) {
        userSelectionRepository.select(paymentMethod, null);
        if (paymentMethod == null) {
            clearCardSettings();
        }
    }

    @Override
    public void getIdentificationTypesAsync() {
        identificationRepository.getIdentificationTypes().enqueue(
            new TaggedCallback<List<IdentificationType>>(ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES) {
                @Override
                public void onSuccess(final List<IdentificationType> identificationTypes) {
                    resolveIdentificationTypes(identificationTypes);
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error, ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES);
                        setFailureRecovery(() -> getIdentificationTypesAsync());
                    }
                }
            });
    }

    @Override
    public void getPaymentMethods() {
        getView().showProgress();
        initRepository.getInit().enqueue(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                if (isViewAttached()) {
                    getView().hideProgress();
                    final PaymentPreference paymentPreference =
                        paymentSettingRepository.getCheckoutPreference().getPaymentPreference();
                    paymentMethodGuessingController = new PaymentMethodGuessingController(
                        paymentPreference.getSupportedPaymentMethods(paymentMethodSearch.getPaymentMethods()),
                        getPaymentTypeId(),
                        paymentPreference.getExcludedPaymentTypes());
                    startGuessingForm();
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                if (isViewAttached()) {
                    getView().hideProgress();
                    setFailureRecovery(() -> getPaymentMethods());
                }
            }
        });
    }

    @Nullable
    @Override
    public String getPaymentTypeId() {
        return userSelectionRepository.getPaymentType();
    }

    private void resolveBankDeals() {
        if (advancedConfiguration.isBankDealsEnabled()) {
            getBankDealsAsync();
        } else {
            getView().hideBankDeals();
        }
    }

    @Nullable
    @Override
    public List<BankDeal> getBankDealsList() {
        return bankDealList;
    }

    private void setBankDealsList(@Nullable final List<BankDeal> bankDealsList) {
        bankDealList = bankDealsList;
    }

    @Override
    public void onIssuerSelected(final Long issuerId) {
        // Empty body, this behavior only exists on CardStoragePresenter
    }

    @Override
    public void onSaveInstanceState(final Bundle outState, final String cardSideState, final boolean lowResActive) {
        if (getPaymentMethod() != null) {
            super.onSaveInstanceState(outState, cardSideState, lowResActive);
            outState.putString(BANK_DEALS_LIST_BUNDLE, JsonUtil.getInstance().toJson(getBankDealsList()));
            outState.putString(PAYMENT_TYPES_LIST_BUNDLE, JsonUtil.getInstance().toJson(getPaymentTypes()));
        }
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.getString(PAYMENT_METHOD_BUNDLE) != null) {
            final String paymentMethodBundleJson = savedInstanceState.getString(PAYMENT_METHOD_BUNDLE);
            if (!TextUtil.isEmpty(paymentMethodBundleJson)) {
                List<PaymentType> paymentTypesList;
                try {
                    final Type listType = new TypeToken<List<PaymentType>>() {
                    }.getType();
                    paymentTypesList = JsonUtil.getInstance().getGson().fromJson(
                        savedInstanceState.getString(PAYMENT_TYPES_LIST_BUNDLE), listType);
                } catch (final Exception ex) {
                    paymentTypesList = null;
                }
                setPaymentTypesList(paymentTypesList);
                List<BankDeal> bankDealsList;
                try {
                    final Type listType = new TypeToken<List<BankDeal>>() {
                    }.getType();
                    bankDealsList = JsonUtil.getInstance().getGson().fromJson(
                        savedInstanceState.getString(BANK_DEALS_LIST_BUNDLE), listType);
                } catch (final Exception ex) {
                    bankDealsList = null;
                }
                setBankDealsList(bankDealsList);
                setPaymentRecovery(JsonUtil.getInstance()
                    .fromJson(savedInstanceState.getString(PAYMENT_RECOVERY_BUNDLE), PaymentRecovery.class));
                super.onRestoreInstanceState(savedInstanceState);
            }
        }
    }

    /* default */ void getBankDealsAsync() {
        bankDealsRepository
            .getBankDealsAsync().enqueue(new TaggedCallback<List<BankDeal>>(ApiUtil.RequestOrigin.GET_BANK_DEALS) {
            @Override
            public void onSuccess(final List<BankDeal> bankDeals) {
                resolveBankDeals(bankDeals);
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                if (isViewAttached()) {
                    setFailureRecovery(() -> getBankDealsAsync());
                }
            }
        });
    }

    /* default */ void resolveBankDeals(final List<BankDeal> bankDeals) {
        if (isViewAttached()) {
            if (bankDeals == null || bankDeals.isEmpty()) {
                getView().hideBankDeals();
            } else {
                bankDealList = bankDeals;
                getView().showBankDeals();
            }
        }
    }

    @Override
    public void resolveTokenRequest(final Token token) {
        this.token = token;
        paymentSettingRepository.configure(token);
        getIssuers();
    }

    private void getIssuers() {
        final PaymentMethod paymentMethod = getPaymentMethod();
        if (paymentMethod != null) {
            issuersRepository.getIssuers(paymentMethod.getId(), bin).enqueue(
                new TaggedCallback<List<Issuer>>(ApiUtil.RequestOrigin.GET_ISSUERS) {
                    @Override
                    public void onSuccess(final List<Issuer> issuers) {
                        issuersSolver.solve(GuessingCardPaymentPresenter.this, issuers);
                    }

                    @Override
                    public void onFailure(final MercadoPagoError error) {
                        setFailureRecovery(() -> getIssuers());
                        if (isViewAttached()) {
                            getView().showError(error, ApiUtil.RequestOrigin.GET_ISSUERS);
                        }
                    }
                });
        }
    }

    private void getInstallments() {
        // Fetch installments and save a default installment if it exists
        summaryAmountRepository.getSummaryAmount(bin).enqueue(new Callback<SummaryAmount>() {
            @Override
            public void success(final SummaryAmount summaryAmount) {
                final AmountConfiguration amountConfiguration =
                    summaryAmount.getAmountConfiguration(summaryAmount.getDefaultAmountConfiguration());

                if (amountConfiguration == null) {
                    getView().finishCardFlow();
                    return;
                }

                payerCostSolver.solveDefaultInstallment(amountConfiguration.getPayerCosts());

                // All set -  card info - If payer cost is null, user must select installments
                getView().finishCardFlow();
            }

            @Override
            public void failure(final ApiException apiException) {
                if(isViewAttached()) {
                    final String origin = ApiUtil.RequestOrigin.POST_SUMMARY_AMOUNT;
                    getView().showApiExceptionError(apiException, origin);
                    setFailureRecovery(() -> getInstallments());
                }
            }
        });
    }

    public PaymentRecovery getPaymentRecovery() {
        return paymentRecovery;
    }

    public void setPaymentRecovery(final PaymentRecovery paymentRecovery) {
        this.paymentRecovery = paymentRecovery;
        if (recoverWithCardHolder()) {
            saveCardholderName(paymentSettingRepository.getToken().getCardHolder().getName());
            saveIdentificationNumber(
                paymentSettingRepository.getToken().getCardHolder().getIdentification().getNumber());
        }
    }

    private boolean recoverWithCardHolder() {
        return paymentRecovery != null && paymentSettingRepository.getToken() != null &&
            paymentSettingRepository.getToken().getCardHolder() != null;
    }

    @Override
    public void createToken() {
        cardTokenRepository
            .createTokenAsync(cardToken).enqueue(new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
            @Override
            public void onSuccess(final Token token) {
                resolveTokenRequest(token);
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                resolveTokenCreationError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
            }
        });
    }

    @Override
    public void onDefaultInstallmentSet() {
        getInstallments();
    }

    @Override
    public void onIssuerWithoutDefaultInstallment() {
        if (isViewAttached()) {
            getView().finishCardFlow();
        }
    }

    @Override
    public void onMultipleIssuers(final List<Issuer> issuers) {
        if (isViewAttached()) {
            getView().finishCardFlow(issuers);
        }
    }
}