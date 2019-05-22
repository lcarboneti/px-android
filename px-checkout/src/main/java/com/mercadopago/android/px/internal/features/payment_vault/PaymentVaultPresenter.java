package com.mercadopago.android.px.internal.features.payment_vault;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.datasource.IESCManager;
import com.mercadopago.android.px.internal.datasource.PaymentVaultTitleSolver;
import com.mercadopago.android.px.internal.features.uicontrollers.AmountRowController;
import com.mercadopago.android.px.internal.navigation.DefaultPayerInformationDriver;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.InitRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.AmountView;
import com.mercadopago.android.px.internal.viewmodel.PaymentMethodViewModel;
import com.mercadopago.android.px.internal.viewmodel.mappers.CustomSearchItemToCardMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.CustomSearchOptionViewModelMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodSearchOptionViewModelMapper;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.views.SelectMethodChildView;
import com.mercadopago.android.px.tracking.internal.views.SelectMethodView;
import java.util.List;

public class PaymentVaultPresenter extends BasePresenter<PaymentVaultView> implements AmountView.OnClick,
    PaymentVault.Actions, AmountRowController.AmountRowVisibilityBehaviour, SearchItemOnClickListenerHandler {

    @NonNull
    private final PaymentSettingRepository paymentSettingRepository;
    @NonNull
    private final UserSelectionRepository userSelectionRepository;

    private final DiscountRepository discountRepository;
    @NonNull
    private final InitRepository initRepository;

    @NonNull private DisabledPaymentMethodRepository disabledPaymentMethodRepository;

    @NonNull private final IESCManager IESCManager;

    @NonNull private final PaymentVaultTitleSolver titleSolver;

    private PaymentMethodSearchItem selectedSearchItem;

    /* default */ PaymentMethodSearch paymentMethodSearch;
    private FailureRecovery failureRecovery;
    private AmountRowController amountRowController;

    public PaymentVaultPresenter(@NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final DisabledPaymentMethodRepository disabledPaymentMethodRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final InitRepository initRepository,
        @NonNull final IESCManager IESCManager,
        @NonNull final PaymentVaultTitleSolver titleSolver) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.userSelectionRepository = userSelectionRepository;
        this.discountRepository = discountRepository;
        this.disabledPaymentMethodRepository = disabledPaymentMethodRepository;
        this.initRepository = initRepository;
        this.IESCManager = IESCManager;
        this.titleSolver = titleSolver;
    }

    public void initialize() {
        try {
            validateParameters();
            initPaymentVaultFlow();
            getView().setTitle(titleSolver.solveTitle());
        } catch (final IllegalStateException exception) {
            getView().showError(MercadoPagoError.createNotRecoverable(exception.getMessage()), TextUtil.EMPTY);
        }
    }

    public void initPaymentVaultFlow() {
        initializeAmountRow();

        initRepository.getInit().enqueue(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {
                if (isViewAttached()) {
                    PaymentVaultPresenter.this.paymentMethodSearch = paymentMethodSearch;
                    initPaymentMethodSearch();
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                getView()
                    .showError(MercadoPagoError
                            .createNotRecoverable(apiException, ApiException.ErrorCodes.PAYMENT_METHOD_NOT_FOUND),
                        ApiException.ErrorCodes.PAYMENT_METHOD_NOT_FOUND);
                setFailureRecovery(() -> initPaymentVaultFlow());
            }
        });
    }

    /* default */ void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    public void initializeAmountRow() {
        if (isViewAttached()) {
            amountRowController = new AmountRowController(this, paymentSettingRepository.getAdvancedConfiguration());
            amountRowController.configure();
        }
    }

    @Override
    public void showAmountRow() {
        getView().showAmount(discountRepository.getCurrentConfiguration(),
            paymentSettingRepository.getCheckoutPreference().getTotalAmount(),
            paymentSettingRepository.getSite());
    }

    @Override
    public void hideAmountRow() {
        getView().hideAmountRow();
    }

    public void onPayerInformationReceived() {
        getView().finishPaymentMethodSelection(userSelectionRepository.getPaymentMethod());
    }

    private void validateParameters() throws IllegalStateException {
        final PaymentPreference paymentPreference =
            paymentSettingRepository.getCheckoutPreference().getPaymentPreference();
        if (!paymentPreference.validMaxInstallments()) {
            throw new IllegalStateException("Invalid max installments number");
        }
        if (!paymentPreference.validDefaultInstallments()) {
            throw new IllegalStateException("Invalid installments number by default");
        }
        if (!paymentPreference.excludedPaymentTypesValid()) {
            throw new IllegalStateException("All payments types excluded");
        }
    }

    public boolean isItemSelected() {
        return selectedSearchItem != null;
    }

    /* default */ void initPaymentMethodSearch() {
        showPaymentMethodGroup();
    }

    private void showPaymentMethodGroup() {
        if (isItemSelected()) {
            showSelectedItemChildren();
        } else {
            resolveAvailablePaymentMethods();
        }
    }

    private void showSelectedItemChildren() {
        trackScreen();
        getView().setTitle(selectedSearchItem.getChildrenHeader());
        getView().showSearchItems(
            new PaymentMethodSearchOptionViewModelMapper(this).map(selectedSearchItem.getChildren()));
        getView().hideProgress();
    }

    private void resolveAvailablePaymentMethods() {
        if (noPaymentMethodsAvailable()) {
            getView().showEmptyPaymentMethodsError();
        } else if (isOnlyOneItemAvailable() && !isDiscountAvailable()) {
            if (!paymentMethodSearch.getGroups().isEmpty()) {
                selectItem(paymentMethodSearch.getGroups().get(0), true);
            } else if (!paymentMethodSearch.getCustomSearchItems().isEmpty()) {
                if (PaymentTypes.CREDIT_CARD.equals(paymentMethodSearch.getCustomSearchItems().get(0).getType())) {
                    selectItem(paymentMethodSearch.getCustomSearchItems().get(0));
                }
            }
        } else {
            showAvailableOptions();
            getView().hideProgress();
        }
    }

    @Override
    public void selectItem(@NonNull final PaymentMethodSearchItem item) {
        selectItem(item, false);
    }

    private void selectItem(@NonNull final PaymentMethodSearchItem item, final boolean automaticSelection) {
        userSelectionRepository.select((Card) null, null);
        getView().saveAutomaticSelection(automaticSelection);
        if (item.hasChildren()) {
            getView().showSelectedItem(item);
        } else if (item.isPaymentType()) {
            startNextStepForPaymentType(item);
        } else if (item.isPaymentMethod()) {
            resolvePaymentMethodSelection(item);
        }
    }

    private void showAvailableOptions() {
        final List<PaymentMethodViewModel> searchItemViewModels =
            new CustomSearchOptionViewModelMapper(this, disabledPaymentMethodRepository)
                .map(paymentMethodSearch.getCustomSearchItems());
        searchItemViewModels
            .addAll(new PaymentMethodSearchOptionViewModelMapper(this).map(paymentMethodSearch.getGroups()));
        getView().showSearchItems(searchItemViewModels);
        trackScreen();
    }

    @Override
    public void selectItem(@NonNull final CustomSearchItem item) {
        if (PaymentTypes.isCardPaymentType(item.getType())) {
            final Card card = getCardWithPaymentMethod(item);
            userSelectionRepository.select(card, null);
            getView().startSavedCardFlow(card);
        } else if (PaymentTypes.isAccountMoney(item.getType())) {
            final PaymentMethod paymentMethod = paymentMethodSearch.getPaymentMethodById(item.getPaymentMethodId());
            userSelectionRepository.select(paymentMethod, null);
            getView().finishPaymentMethodSelection(paymentMethod);
        }
    }

    @Override
    public void showDisabledPaymentMethodDetailDialog(@NonNull final String paymentMethodType) {
        getView().showDisabledPaymentMethodDetailDialog(paymentMethodType);
    }

    private Card getCardWithPaymentMethod(final CustomSearchItem searchItem) {
        final PaymentMethod paymentMethod = paymentMethodSearch.getPaymentMethodById(searchItem.getPaymentMethodId());
        final Card selectedCard = new CustomSearchItemToCardMapper().map(searchItem);
        if (paymentMethod != null) {
            selectedCard.setPaymentMethod(paymentMethod);
            if (selectedCard.getSecurityCode() == null && paymentMethod.getSettings() != null &&
                paymentMethod.getSettings().get(0) != null) {
                selectedCard.setSecurityCode(paymentMethod.getSettings().get(0).getSecurityCode());
            }
        }
        return selectedCard;
    }

    private Card getCardById(final Iterable<Card> savedCards, final String cardId) {
        Card foundCard = null;
        for (final Card card : savedCards) {
            if (card.getId().equals(cardId)) {
                foundCard = card;
                break;
            }
        }
        return foundCard;
    }

    private void startNextStepForPaymentType(final PaymentMethodSearchItem item) {

        final String itemId = item.getId();

        if (PaymentTypes.isCardPaymentType(itemId)) {
            userSelectionRepository.select(itemId);
            getView().startCardFlow();
        } else {
            getView().startPaymentMethodsSelection(
                paymentSettingRepository.getCheckoutPreference().getPaymentPreference());
        }
    }

    private void resolvePaymentMethodSelection(final PaymentMethodSearchItem item) {

        final PaymentMethod selectedPaymentMethod = paymentMethodSearch.getPaymentMethodBySearchItem(item);
        userSelectionRepository.select(selectedPaymentMethod, null);

        if (selectedPaymentMethod == null) {
            getView().showMismatchingPaymentMethodError();
        } else {
            handleCollectPayerInformation(selectedPaymentMethod);
        }
    }

    private void handleCollectPayerInformation(final PaymentMethod selectedPaymentMethod) {
        new DefaultPayerInformationDriver(paymentSettingRepository.getCheckoutPreference().getPayer(),
            selectedPaymentMethod).drive(
            new DefaultPayerInformationDriver.PayerInformationDriverCallback() {
                @Override
                public void driveToNewPayerData() {
                    getView().collectPayerInformation();
                }

                @Override
                public void driveToReviewConfirm() {
                    getView().finishPaymentMethodSelection(selectedPaymentMethod);
                }
            });
    }

    public boolean isOnlyOneItemAvailable() {
        return paymentMethodSearch.getGroups().size() + paymentMethodSearch.getCustomSearchItems().size() == 1;
    }

    private boolean noPaymentMethodsAvailable() {
        return paymentMethodSearch.getGroups().isEmpty() && paymentMethodSearch.getCustomSearchItems().isEmpty();
    }

    public PaymentMethodSearchItem getSelectedSearchItem() {
        return selectedSearchItem;
    }

    public void setSelectedSearchItem(final PaymentMethodSearchItem mSelectedSearchItem) {
        selectedSearchItem = mSelectedSearchItem;
    }

    public void trackScreen() {
        // Do not remove check paymentMethodSearch, sometimes in recovery status is null.
        if (paymentMethodSearch != null) {
            if (selectedSearchItem == null) {
                trackInitialScreen();
            } else {
                trackChildScreen();
            }
        }
    }

    private void trackInitialScreen() {
        final SelectMethodView selectMethodView =
            new SelectMethodView(paymentMethodSearch, IESCManager.getESCCardIds(),
                paymentSettingRepository.getCheckoutPreference());
        setCurrentViewTracker(selectMethodView);
    }

    private void trackChildScreen() {
        final SelectMethodChildView selectMethodChildView =
            new SelectMethodChildView(paymentMethodSearch, selectedSearchItem,
                paymentSettingRepository.getCheckoutPreference());
        setCurrentViewTracker(selectMethodChildView);
    }

    @Override
    public void trackOnBackPressed() {
        if (selectedSearchItem == null) {
            tracker.trackAbort();
        } else {
            tracker.trackBack();
        }
    }

    @Override
    public void onDetailClicked(@NonNull final DiscountConfigurationModel discountModel) {
        getView().showDetailDialog(discountModel);
    }

    public void onPaymentMethodReturned() {
        getView().finishPaymentMethodSelection(userSelectionRepository.getPaymentMethod());
    }

    private boolean isDiscountAvailable() {
        return discountRepository.getCurrentConfiguration().getDiscount() != null;
    }
}