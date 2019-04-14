package com.mercadopago.android.px.internal.features.express;

import android.os.Bundle;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecoratorMapper;
import com.mercadopago.android.px.internal.features.express.slider.HubAdapter;
import com.mercadopago.android.px.internal.features.express.slider.SplitPaymentHeaderAdapter;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.NoConnectivityException;
import com.mercadopago.android.px.internal.view.AmountDescriptorView;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.viewmodel.ConfirmButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.PayerCostSelection;
import com.mercadopago.android.px.internal.viewmodel.mappers.ConfirmButtonViewModelMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.ElementDescriptorMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodDescriptorMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodDrawableItemMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.SplitHeaderMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.SummaryInfoMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.SummaryViewModelMapper;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.SummaryInfo;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.events.ConfirmEvent;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.events.InstallmentsEventTrack;
import com.mercadopago.android.px.tracking.internal.events.SwipeOneTapEventTracker;
import com.mercadopago.android.px.tracking.internal.views.OneTapViewTracker;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/* default */ class ExpressPaymentPresenter extends BasePresenter<ExpressPayment.View>
    implements ExpressPayment.Actions,
    AmountDescriptorView.OnClickListenerWithDiscount {

    private static final String BUNDLE_STATE_PAYER_COST =
        "com.mercadopago.android.px.internal.features.express.PAYER_COST";
    /* default */ PayerCostSelection payerCostSelection;

    private static final String BUNDLE_STATE_SPLIT_PREF =
        "com.mercadopago.android.px.internal.features.express.SPLIT_PREF";
    /* default */ boolean isSplitUserPreference = false;

    private static final String BUNDLE_STATE_AVAILABLE_PM_COUNT =
        "com.mercadopago.android.px.internal.features.express.AVAILABLE_PM_COUNT";
    private int availablePaymentMethodsCount = -1;

    @NonNull private final PaymentRepository paymentRepository;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final PaymentSettingRepository paymentConfiguration;
    @NonNull private final AmountConfigurationRepository amountConfigurationRepository;
    @NonNull private final DisabledPaymentMethodRepository disabledPaymentMethodRepository;
    @NonNull private final ExplodeDecoratorMapper explodeDecoratorMapper;

    //TODO remove.
    /* default */ List<ExpressMetadata> expressMetadataList;

    private final PaymentMethodDrawableItemMapper paymentMethodDrawableItemMapper;
    private Set<String> cardsWithSplit;

    /* default */ ExpressPaymentPresenter(@NonNull final PaymentRepository paymentRepository,
        @NonNull final PaymentSettingRepository paymentConfiguration,
        @NonNull final DisabledPaymentMethodRepository disabledPaymentMethodRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final AmountRepository amountRepository,
        @NonNull final InitRepository initRepository,
        @NonNull final AmountConfigurationRepository amountConfigurationRepository) {

        this.paymentRepository = paymentRepository;
        this.paymentConfiguration = paymentConfiguration;
        this.amountRepository = amountRepository;
        this.discountRepository = discountRepository;
        this.amountConfigurationRepository = amountConfigurationRepository;
        this.disabledPaymentMethodRepository = disabledPaymentMethodRepository;
        explodeDecoratorMapper = new ExplodeDecoratorMapper();
        paymentMethodDrawableItemMapper = new PaymentMethodDrawableItemMapper();

        initRepository.getInit().execute(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                expressMetadataList = paymentMethodSearch.getExpress();
                //Plus one to compensate for add new payment method
                payerCostSelection = createNewPayerCostSelected();
                cardsWithSplit = paymentMethodSearch.getIdsWithSplitAllowed();
            }

            @Override
            public void failure(final ApiException apiException) {
                throw new IllegalStateException("groups missing rendering one tap");
            }
        });
    }

    @Override
    public void loadViewModel() {
        availablePaymentMethodsCount = countAvailablePaymentMethods();

        final SummaryInfo summaryInfo = new SummaryInfoMapper().map(paymentConfiguration.getCheckoutPreference());

        final ElementDescriptorView.Model elementDescriptorModel =
            new ElementDescriptorMapper().map(summaryInfo);

        final List<SummaryView.Model> summaryModels =
            new SummaryViewModelMapper(paymentConfiguration.getCheckoutPreference(), discountRepository,
                amountRepository, elementDescriptorModel, this, summaryInfo).map(expressMetadataList);

        final List<PaymentMethodDescriptorView.Model> paymentModels =
            new PaymentMethodDescriptorMapper(paymentConfiguration, amountConfigurationRepository,
                disabledPaymentMethodRepository).map(expressMetadataList);

        final List<SplitPaymentHeaderAdapter.Model> splitHeaderModels =
            new SplitHeaderMapper(paymentConfiguration.getCheckoutPreference().getSite().getCurrencyId(),
                amountConfigurationRepository)
                .map(expressMetadataList);

        final List<ConfirmButtonViewModel> confirmButtonViewModels =
            new ConfirmButtonViewModelMapper(disabledPaymentMethodRepository).map(expressMetadataList);

        final HubAdapter.Model model =
            new HubAdapter.Model(paymentModels, summaryModels, splitHeaderModels, confirmButtonViewModels);

        getView().showToolbarElementDescriptor(elementDescriptorModel);

        getView().configureAdapters(paymentMethodDrawableItemMapper.map(expressMetadataList),
            paymentConfiguration.getCheckoutPreference().getSite(), model);
    }

    @Override
    public void onViewResumed() {
        // If a payment was attempted, the exploding fragment is still visible when we go back to one tap fragment.
        // Example: call for authorize, after asking for cvv and pressing back, we go back to one tap and need to
        // remove the exploding fragment we had before.
        if (paymentRepository.hasPayment()) {
            cancelLoading();
        }
        paymentRepository.attach(this);
        if (shouldReloadModel()) {
            loadViewModel();
        }
    }

    private boolean shouldReloadModel() {
        final int currentAvailablePaymentMethodsCount = countAvailablePaymentMethods();
        return availablePaymentMethodsCount != currentAvailablePaymentMethodsCount;
    }

    private int countAvailablePaymentMethods() {
        int currentAvailablePaymentMethodsCount = expressMetadataList.size();
        for (final ExpressMetadata expressMetadata : expressMetadataList) {
            if ((expressMetadata.isCard() &&
                disabledPaymentMethodRepository.hasPaymentMethodId(expressMetadata.getCard().getId())) ||
                disabledPaymentMethodRepository.hasPaymentMethodId(expressMetadata.getPaymentMethodId())) {
                currentAvailablePaymentMethodsCount--;
            }
        }
        return currentAvailablePaymentMethodsCount;
    }

    @Override
    public void detachView() {
        onViewPaused();
        super.detachView();
    }

    @Override
    public void recoverFromBundle(@NonNull final Bundle bundle) {
        payerCostSelection = bundle.getParcelable(BUNDLE_STATE_PAYER_COST);
        isSplitUserPreference = bundle.getBoolean(BUNDLE_STATE_SPLIT_PREF, false);
        availablePaymentMethodsCount = bundle.getInt(BUNDLE_STATE_AVAILABLE_PM_COUNT);
    }

    @NonNull
    @Override
    public Bundle storeInBundle(@NonNull final Bundle bundle) {
        bundle.putParcelable(BUNDLE_STATE_PAYER_COST, payerCostSelection);
        bundle.putBoolean(BUNDLE_STATE_SPLIT_PREF, isSplitUserPreference);
        bundle.putInt(BUNDLE_STATE_AVAILABLE_PM_COUNT, availablePaymentMethodsCount);
        return bundle;
    }

    @Override
    public void trackExpressView() {
        final OneTapViewTracker oneTapViewTracker = new OneTapViewTracker(expressMetadataList,
            paymentConfiguration.getCheckoutPreference(),
            discountRepository.getCurrentConfiguration(),
            Collections.emptySet(),
            cardsWithSplit);
        setCurrentViewTracker(oneTapViewTracker);
    }

    @Override
    public void confirmPayment(final int paymentMethodSelectedIndex) {
        refreshExplodingState();

        // TODO improve: This was added because onetap can detach this listener on its onDestroy
        paymentRepository.attach(this);

        final ExpressMetadata expressMetadata = expressMetadataList.get(paymentMethodSelectedIndex);

        PayerCost payerCost = null;
        boolean splitPayment = false;

        if (expressMetadata.isCard()) {
            final AmountConfiguration amountConfiguration =
                amountConfigurationRepository.getConfigurationFor(expressMetadata.getCard().getId());
            splitPayment = isSplitUserPreference && amountConfiguration.allowSplit();
            payerCost = amountConfiguration
                .getCurrentPayerCost(isSplitUserPreference, payerCostSelection.get(paymentMethodSelectedIndex));
        } else if (expressMetadata.isAccountMoney()) {
            final AmountConfiguration amountConfiguration =
                amountConfigurationRepository.getConfigurationFor(expressMetadata.getPaymentMethodId());
            splitPayment = isSplitUserPreference && amountConfiguration.allowSplit();
        }

        //TODO fill cards with esc
        ConfirmEvent.from(Collections.emptySet(), expressMetadata, payerCost, splitPayment).track();

        paymentRepository.startExpressPayment(expressMetadata, payerCost, splitPayment);
    }

    private void refreshExplodingState() {
        if (paymentRepository.isExplodingAnimationCompatible()) {
            getView().startLoadingButton(paymentRepository.getPaymentTimeout());
            getView().disableToolbarBack();
        }
    }

    @Override
    public void cancel() {
        tracker.trackAbort();
        getView().cancel();
    }

    //TODO verify if current item still persist when activity is destroyed.
    @Override
    public void onTokenResolved(final int paymentMethodSelectedIndex) {
        cancelLoading();
        confirmPayment(paymentMethodSelectedIndex);
    }

    /**
     * When there is no visual interaction needed this callback is called.
     *
     * @param payment plugin payment.
     */
    @Override
    public void onPaymentFinished(@NonNull final IPaymentDescriptor payment) {
        getView().finishLoading(explodeDecoratorMapper.map(payment));
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        cancelLoading();
        if (error.isInternalServerError() || error.isNoConnectivityError()) {
            FrictionEventTracker.with(OneTapViewTracker.PATH_REVIEW_ONE_TAP_VIEW,
                FrictionEventTracker.Id.GENERIC, FrictionEventTracker.Style.CUSTOM_COMPONENT,
                error)
                .track();
            getView().showErrorSnackBar(error);
        } else {
            getView().showErrorScreen(error);
        }
    }

    @Override
    public void onVisualPayment() {
        getView().showPaymentProcessor();
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        cancelLoading();
        getView().showCardFlow(card);
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        getView().onRecoverPaymentEscInvalid(recovery);
    }

    private void updateElementPosition(final int paymentMethodIndex, final int selectedPayerCost) {
        payerCostSelection.save(paymentMethodIndex, selectedPayerCost);
        updateElementPosition(paymentMethodIndex);
    }

    @Override
    public void onViewPaused() {
        paymentRepository.detach(this);
    }

    @Override
    public void onInstallmentsRowPressed(final int currentItem) {

        final ExpressMetadata expressMetadata = expressMetadataList.get(currentItem);
        final CardMetadata cardMetadata = expressMetadata.getCard();

        if (currentItem <= expressMetadataList.size() && cardMetadata != null) {
            final AmountConfiguration amountConfiguration =
                amountConfigurationRepository.getConfigurationFor(cardMetadata.getId());
            final List<PayerCost> payerCostList = amountConfiguration.getAppliedPayerCost(isSplitUserPreference);
            final int index = payerCostList.indexOf(
                amountConfiguration.getCurrentPayerCost(isSplitUserPreference, payerCostSelection.get(currentItem)));
            getView().showInstallmentsList(payerCostList, index);
            new InstallmentsEventTrack(expressMetadata, amountConfiguration).track();
        }
    }

    /**
     * When user cancel the payer cost selection this method will be called with the current payment method position
     *
     * @param position current payment method position.
     */
    @Override
    public void onInstallmentSelectionCanceled(final int position) {
        updateElementPosition(position);
        getView().collapseInstallmentsSelection();
    }

    /**
     * When user selects a new payment method this method will be called with the new current paymentMethodIndex.
     *
     * @param paymentMethodIndex current payment method paymentMethodIndex.
     */
    @Override
    public void onSliderOptionSelected(final int paymentMethodIndex) {
        new SwipeOneTapEventTracker().track();
        updateElementPosition(paymentMethodIndex, payerCostSelection.get(paymentMethodIndex));
    }

    @Override
    public void updateElementPosition(final int paymentMethodIndex) {
        getView().updateViewForPosition(paymentMethodIndex, payerCostSelection.get(paymentMethodIndex),
            isSplitUserPreference);
    }

    /**
     * When user selects a new payer cost for certain payment method this method will be called.
     *
     * @param paymentMethodIndex current payment method position.
     * @param payerCostSelected user selected payerCost.
     */
    @Override
    public void onPayerCostSelected(final int paymentMethodIndex, final PayerCost payerCostSelected) {
        final CardMetadata cardMetadata = expressMetadataList.get(paymentMethodIndex).getCard();
        final int selected = amountConfigurationRepository.getConfigurationFor(cardMetadata.getId())
            .getAppliedPayerCost(isSplitUserPreference)
            .indexOf(payerCostSelected);

        updateElementPosition(paymentMethodIndex, selected);
        getView().collapseInstallmentsSelection();
    }

    @Override
    public void hasFinishPaymentAnimation() {
        final IPaymentDescriptor payment = paymentRepository.getPayment();
        if (payment != null) {
            getView().showPaymentResult(payment);
        }
    }

    private void cancelLoading() {
        getView().enableToolbarBack();
        getView().cancelLoading();
    }

    @Override
    public void manageNoConnection() {
        final NoConnectivityException exception = new NoConnectivityException();
        final ApiException apiException = ApiUtil.getApiException(exception);
        final MercadoPagoError mercadoPagoError = new MercadoPagoError(apiException, null);
        FrictionEventTracker.with(OneTapViewTracker.PATH_REVIEW_ONE_TAP_VIEW,
            FrictionEventTracker.Id.GENERIC, FrictionEventTracker.Style.CUSTOM_COMPONENT,
            mercadoPagoError)
            .track();
        getView().showErrorSnackBar(mercadoPagoError);
    }

    @Override
    public void onAmountDescriptorClicked(@NonNull final DiscountConfigurationModel discountModel) {
        getView().showDiscountDetailDialog(discountModel);
    }

    @Override
    public void onSplitChanged(final boolean isChecked, final int currentItem) {
        payerCostSelection = createNewPayerCostSelected();
        isSplitUserPreference = isChecked;
        // cancel also update the position.
        // it is used because the installment selection can be expanded by the user.
        onInstallmentSelectionCanceled(currentItem);
    }

    @NonNull
    private PayerCostSelection createNewPayerCostSelected() {
        return new PayerCostSelection(expressMetadataList.size() + 1);
    }
}