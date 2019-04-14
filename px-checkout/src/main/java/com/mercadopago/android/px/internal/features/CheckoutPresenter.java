package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.configuration.InternalConfiguration;
import com.mercadopago.android.px.internal.features.providers.CheckoutProvider;
import com.mercadopago.android.px.internal.navigation.DefaultPaymentMethodDriver;
import com.mercadopago.android.px.internal.navigation.OnChangePaymentMethodDriver;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.viewmodel.CheckoutStateModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.mappers.BusinessModelMapper;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Cause;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.IPaymentDescriptorHandler;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import java.util.List;

public class CheckoutPresenter extends MvpPresenter<CheckoutView, CheckoutProvider> implements PaymentServiceHandler,
    PostPaymentAction.ActionController {

    @NonNull /* default */ final CheckoutStateModel state;

    @NonNull private final PluginRepository pluginRepository;

    @NonNull private final PaymentRepository paymentRepository;

    @NonNull
    private final InitRepository initRepository;
    @NonNull
    /* default */ final PaymentSettingRepository paymentSettingRepository;

    @NonNull
    /* default */ final UserSelectionRepository userSelectionRepository;

    @NonNull
    private final InternalConfiguration internalConfiguration;

    @NonNull
    private final BusinessModelMapper businessModelMapper;

    private transient FailureRecovery failureRecovery;

    public CheckoutPresenter(@NonNull final CheckoutStateModel persistentData,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final InitRepository initRepository,
        @NonNull final PluginRepository pluginRepository,
        @NonNull final PaymentRepository paymentRepository,
        @NonNull final InternalConfiguration internalConfiguration,
        @NonNull final BusinessModelMapper businessModelMapper) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.userSelectionRepository = userSelectionRepository;
        this.initRepository = initRepository;
        this.pluginRepository = pluginRepository;
        this.paymentRepository = paymentRepository;
        this.internalConfiguration = internalConfiguration;
        this.businessModelMapper = businessModelMapper;
        state = persistentData;
    }

    @NonNull
    public CheckoutStateModel getState() {
        return state;
    }

    public void initialize() {
        getView().showProgress();
        configurePreference();
    }

    @Override
    public void attachView(final CheckoutView view) {
        super.attachView(view);
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    private void configurePreference() {
        if (paymentSettingRepository.getCheckoutPreference() != null) {
            startCheckoutForPreference();
        } else {
            retrieveCheckoutPreference(paymentSettingRepository.getCheckoutPreferenceId());
        }
    }

    /* default */ void startCheckoutForPreference() {
        try {
            getCheckoutPreference().validate();
            getView().trackScreen();
            startCheckout();
        } catch (final CheckoutPreferenceException e) {
            final String message = getResourcesProvider().getCheckoutExceptionMessage(e);
            getView().showError(new MercadoPagoError(message, false));
        }
    }

    private void startCheckout() {
        getResourcesProvider().fetchFonts();
        retrievePaymentMethodSearch();
    }

    public void retrievePaymentMethodSearch() {
        if (isViewAttached()) {
            initRepository.getInit().enqueue(new Callback<InitResponse>() {
                @Override
                public void success(final InitResponse paymentMethodSearch) {
                    if (isViewAttached()) {
                        startFlow(paymentMethodSearch);
                    }
                }

                @Override
                public void failure(final ApiException apiException) {
                    if (isViewAttached()) {
                        getView().showError(
                            new MercadoPagoError(apiException, ApiUtil.RequestOrigin.GET_PAYMENT_METHODS));
                    }
                }
            });
        }
    }

    /* default */ void startFlow(final PaymentMethodSearch paymentMethodSearch) {

        new DefaultPaymentMethodDriver(paymentMethodSearch,
            paymentSettingRepository.getCheckoutPreference().getPaymentPreference())
            .drive(new DefaultPaymentMethodDriver.PaymentMethodDriverCallback() {
                @Override
                public void driveToCardVault(@NonNull final Card card) {
                    userSelectionRepository.select(card, null);
                    getView().showSavedCardFlow(card);
                }

                @Override
                public void driveToNewCardFlow(final String defaultPaymentTypeId) {
                    userSelectionRepository.select(defaultPaymentTypeId);
                    getView().showNewCardFlow();
                }

                @Override
                public void doNothing() {
                    noDefaultPaymentMethods(paymentMethodSearch);
                }
            });
    }

    /* default */ void noDefaultPaymentMethods(final PaymentMethodSearch paymentMethodSearch) {
        saveIsExpressCheckout(paymentMethodSearch);
        savePaymentMethodQuantity(paymentMethodSearch);

        if (state.isExpressCheckout) {
            getView().hideProgress();
            getView().showOneTap();
        } else {
            getView().showPaymentMethodSelection();
        }
    }

    public boolean isESCEnabled() {
        return paymentSettingRepository.getAdvancedConfiguration().isEscEnabled();
    }

    /* default */ void retrieveCheckoutPreference(final String checkoutPreferenceId) {
        getResourcesProvider().getCheckoutPreference(checkoutPreferenceId,
            new TaggedCallback<CheckoutPreference>(ApiUtil.RequestOrigin.GET_PREFERENCE) {
                @Override
                public void onSuccess(final CheckoutPreference checkoutPreference) {
                    paymentSettingRepository.configure(checkoutPreference);
                    if (isViewAttached()) {
                        startCheckoutForPreference();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (isViewAttached()) {
                        getView().showError(error);
                        setFailureRecovery(new FailureRecovery() {
                            @Override
                            public void recover() {
                                retrieveCheckoutPreference(checkoutPreferenceId);
                            }
                        });
                    }
                }
            });
    }

    public void onErrorCancel(@Nullable final MercadoPagoError mercadoPagoError) {
        if (isIdentificationInvalidInPayment(mercadoPagoError)) {
            getView().showPaymentMethodSelection();
        } else {
            cancelCheckout();
        }
    }

    private boolean isIdentificationInvalidInPayment(@Nullable final MercadoPagoError mercadoPagoError) {
        boolean identificationInvalid = false;
        if (mercadoPagoError != null && mercadoPagoError.isApiException()) {
            final List<Cause> causeList = mercadoPagoError.getApiException().getCause();
            if (causeList != null && !causeList.isEmpty()) {
                final Cause cause = causeList.get(0);
                if (cause.getCode().equals(ApiException.ErrorCodes.INVALID_IDENTIFICATION_NUMBER)) {
                    identificationInvalid = true;
                }
            }
        }
        return identificationInvalid;
    }

    /* default */ void onPaymentMethodSelected() {
        if (shouldSkipUserConfirmation()) {
            getView().showPaymentProcessorWithAnimation();
        } else {
            getView().showReviewAndConfirm(isUniquePaymentMethod());
        }
    }

    /* default */ boolean shouldSkipUserConfirmation() {
        return paymentSettingRepository.getPaymentConfiguration().getPaymentProcessor().shouldSkipUserConfirmation();
    }

    private void resolvePaymentFailure(final MercadoPagoError mercadoPagoError) {
        if (mercadoPagoError != null && mercadoPagoError.isPaymentProcessing()) {
            final PaymentResult paymentResult =
                new PaymentResult.Builder()
                    .setPaymentData(paymentRepository.getPaymentDataList())
                    .setPaymentStatus(Payment.StatusCodes.STATUS_IN_PROCESS)
                    .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY)
                    .build();
            getView().showPaymentResult(paymentResult);
        } else if (mercadoPagoError != null && mercadoPagoError.isInternalServerError()) {
            setFailureRecovery(new FailureRecovery() {
                @Override
                public void recover() {
                    getView().startPayment();
                }
            });
            getView().showError(mercadoPagoError);
        } else {
            // Strange that mercadoPagoError can be nullable here, but it was like this
            getView().showError(mercadoPagoError);
        }
    }

    public void onPaymentMethodSelectionError(final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onPaymentMethodSelectionCancel() {
        cancelCheckout();
    }

    public void onReviewAndConfirmCancel() {
        if (isUniquePaymentMethod()) {
            getView().cancelCheckout();
        } else {
            state.paymentMethodEdited = true;
            getView().showPaymentMethodSelection();
            //Back button in R&C
            getView().transitionOut();
        }
    }

    public void onReviewAndConfirmError(final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onPaymentResultResponse() {
        finishCheckout();
    }

    public void onCardFlowResponse() {
        if (isRecoverableTokenProcess()) {
            getView().startPayment();
        } else {
            onPaymentMethodSelected();
        }
    }

    public void onTerminalError(@NonNull final MercadoPagoError mercadoPagoError) {
        getView().cancelCheckout(mercadoPagoError);
    }

    public void onCardFlowCancel() {
        initRepository.getInit().execute(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                new DefaultPaymentMethodDriver(paymentMethodSearch,
                    paymentSettingRepository.getCheckoutPreference().getPaymentPreference()).drive(
                    new DefaultPaymentMethodDriver.PaymentMethodDriverCallback() {
                        @Override
                        public void driveToCardVault(@NonNull final Card card) {
                            cancelCheckout();
                        }

                        @Override
                        public void driveToNewCardFlow(final String defaultPaymentTypeId) {
                            cancelCheckout();
                        }

                        @Override
                        public void doNothing() {
                            state.paymentMethodEdited = true;
                            getView().showPaymentMethodSelection();
                        }
                    });
            }

            @Override
            public void failure(final ApiException apiException) {
                state.paymentMethodEdited = true;
                getView().showPaymentMethodSelection();
            }
        });
    }

    public void onCustomReviewAndConfirmResponse(final Integer customResultCode) {
        getView().cancelCheckout(customResultCode, state.paymentMethodEdited);
    }

    private void savePaymentMethodQuantity(final PaymentMethodSearch paymentMethodSearch) {
        final int pluginCount = pluginRepository.getPaymentMethodPluginCount();
        int groupCount = 0;
        int customCount = 0;

        if (paymentMethodSearch != null && paymentMethodSearch.hasSearchItems()) {
            groupCount = paymentMethodSearch.getGroups().size();
            if (pluginCount == 0 && groupCount == 1 && paymentMethodSearch.getGroups().get(0).isGroup()) {
                state.isUniquePaymentMethod = false;
            }
        }

        if (paymentMethodSearch != null && paymentMethodSearch.hasCustomSearchItems()) {
            customCount = paymentMethodSearch.getCustomSearchItems().size();
        }

        state.isUniquePaymentMethod = groupCount + customCount + pluginCount == 1;
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        } else {
            IllegalStateException e = new IllegalStateException("Failure recovery not defined");
            getView().showError(new MercadoPagoError(getResourcesProvider().getCheckoutExceptionMessage(e), false));
        }
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    private boolean isRecoverableTokenProcess() {
        return paymentRepository.hasPayment() && paymentRepository.createPaymentRecovery().isTokenRecoverable();
    }

    public CheckoutPreference getCheckoutPreference() {
        return paymentSettingRepository.getCheckoutPreference();
    }

    private void finishCheckout() {
        //TODO improve this
        if (paymentRepository.hasPayment() && paymentRepository.getPayment() instanceof Payment) {
            getView().finishWithPaymentResult((Payment) paymentRepository.getPayment());
        } else {
            getView().finishWithPaymentResult();
        }
    }

    public void onCustomPaymentResultResponse(final Integer customResultCode) {
        //TODO improve this
        if (paymentRepository.hasPayment() && paymentRepository.getPayment() instanceof Payment) {
            getView().finishWithPaymentResult(customResultCode, (Payment) paymentRepository.getPayment());
        } else {
            getView().finishWithPaymentResult(customResultCode);
        }
    }

    private void recoverPayment() {
        try {
            getView().startPaymentRecoveryFlow(paymentRepository.createPaymentRecovery());
        } catch (final Exception e) {
            final String message = getResourcesProvider().getCheckoutExceptionMessage(e);
            getView().showError(new MercadoPagoError(message, e.getMessage(), false));
        }
    }

    /**
     * Send intention to close checkout if the checkout has oneTap data then it should not close.
     */
    public void cancelCheckout() {
        //TODO improve this
        if (state.isExpressCheckout) {
            getView().hideProgress();
        } else {
            getView().cancelCheckout();
        }
    }

    private void saveIsExpressCheckout(final PaymentMethodSearch paymentMethodSearch) {
        state.isExpressCheckout = paymentMethodSearch.hasExpressCheckoutMetadata();
    }

    /**
     * Close checkout with resCode
     */
    public void exitWithCode(final int resCode) {
        getView().exitCheckout(resCode);
    }

    public boolean isUniquePaymentMethod() {
        return state.isUniquePaymentMethod;
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        getView().showSavedCardFlow(card);
    }

    @Override
    public void onVisualPayment() {
        getView().showPaymentProcessor();
    }

    @Override
    public void onPaymentFinished(@NonNull final IPaymentDescriptor payment) {
        payment.process(new IPaymentDescriptorHandler() {
            @Override
            public void visit(@NonNull final IPaymentDescriptor payment) {
                getView().hideProgress();
                getView().showPaymentResult(paymentRepository.createPaymentResult(payment));
            }

            @Override
            public void visit(@NonNull final BusinessPayment businessPayment) {
                getView().hideProgress();
                getView().showBusinessResult(businessModelMapper.map(businessPayment));
            }
        });
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        getView().hideProgress();
        recoverCreatePayment(error);
    }

    private void recoverCreatePayment(final MercadoPagoError error) {
        setFailureRecovery(new FailureRecovery() {
            @Override
            public void recover() {
                getView().startPayment();
            }
        });
        resolvePaymentFailure(error);
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        getView().startPaymentRecoveryFlow(recovery);
    }

    @Override
    public void recoverFromReviewAndConfirm(@NonNull final PostPaymentAction postPaymentAction) {
        getView().showReviewAndConfirmAndRecoverPayment(isUniquePaymentMethod(), postPaymentAction);
    }

    @Override
    public void recoverFromOneTap() {
        recoverPayment();
    }

    @Override
    public void onChangePaymentMethod() {
        state.paymentMethodEdited = true;
        userSelectionRepository.reset();
        paymentSettingRepository.clearToken();
        getView().transitionOut();

        new OnChangePaymentMethodDriver(internalConfiguration, state, paymentRepository)
            .drive(new OnChangePaymentMethodDriver.ChangePaymentMethodDriverCallback() {
                @Override
                public void driveToFinishWithPaymentResult(final Integer resultCode, final Payment payment) {
                    getView().finishWithPaymentResult(resultCode, payment);
                }

                @Override
                public void driveToFinishWithoutPaymentResult(final Integer resultCode) {
                    getView().finishWithPaymentResult(resultCode);
                }

                @Override
                public void driveToShowOneTap() {
                    //donothing
                }

                @Override
                public void driveToShowPaymentMethodSelection() {
                    getView().showPaymentMethodSelection();
                }
            });
    }

    //TODO separate with better navigation when we have a proper driver.
    public void onChangePaymentMethodFromReviewAndConfirm() {
        onChangePaymentMethod();
    }
}