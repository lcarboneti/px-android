package com.mercadopago.android.px.paymentvault;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.datasource.PaymentVaultTitleSolver;
import com.mercadopago.android.px.internal.features.payment_vault.PaymentVaultPresenter;
import com.mercadopago.android.px.internal.features.payment_vault.PaymentVaultView;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.PaymentMethodViewModel;
import com.mercadopago.android.px.internal.viewmodel.mappers.CustomSearchOptionViewModelMapper;
import com.mercadopago.android.px.internal.viewmodel.mappers.PaymentMethodSearchOptionViewModelMapper;
import com.mercadopago.android.px.mocks.PaymentMethodSearchs;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.PaymentMethods;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.utils.ReflectionArgumentMatchers;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.math.BigDecimal;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentVaultPresenterTest {

    private PaymentVaultPresenter presenter;
    private final StubView stubView = new StubView();

    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private CheckoutPreference checkoutPreference;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private DisabledPaymentMethodRepository disabledPaymentMethodRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private GroupsRepository groupsRepository;
    @Mock private AdvancedConfiguration advancedConfiguration;
    @Mock private PaymentVaultView view;
    @Mock private PaymentVaultTitleSolver paymentVaultTitleSolver;

    @Mock private Site mockSite;

    private static final String CUSTOM_PAYMENT_VAULT_TITLE = "CUSTOM_PAYMENT_VAULT_TITLE";
    private static final DiscountConfigurationModel WITHOUT_DISCOUNT =
        new DiscountConfigurationModel(null, null, false);

    @Before
    public void setUp() {
        when(checkoutPreference.getTotalAmount()).thenReturn(new BigDecimal(100));
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getPaymentPreference()).thenReturn(new PaymentPreference());
        when(checkoutPreference.getSite()).thenReturn(mockSite);
        when(paymentSettingRepository.getAdvancedConfiguration()).thenReturn(advancedConfiguration);

        presenter = getPresenter();
    }

    @NonNull
    private PaymentVaultPresenter getBasePresenter(
        final PaymentVaultView view) {

        final PaymentVaultPresenter presenter =
            new PaymentVaultPresenter(paymentSettingRepository, userSelectionRepository,
                disabledPaymentMethodRepository, discountRepository, groupsRepository, mock(ESCManagerBehaviour.class),
                paymentVaultTitleSolver);
        presenter.attachView(view);

        return presenter;
    }

    @NonNull
    private PaymentVaultPresenter getPresenter() {
        return getBasePresenter(view);
    }

    @Test
    public void whenNoPaymentMethodsAvailableThenShowError() {
        final PaymentMethodSearch paymentMethodSearch = new PaymentMethodSearch();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();

        verify(view).showEmptyPaymentMethodsError();
    }

    @Test
    public void whenPaymentMethodSearchHasItemsShowThem() {
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();

        final List<PaymentMethodViewModel> models =
            new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups());

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));
    }

    @Test
    public void whenPaymentMethodSearchHasPayerCustomOptionsShowThem() {
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();

        final List<PaymentMethodViewModel> models =
            new CustomSearchOptionViewModelMapper(presenter, disabledPaymentMethodRepository)
                .map(paymentMethodSearch.getCustomSearchItems());
        models.addAll(new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups()));

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));
    }

    //Automatic selections

    @Test
    public void whenOnlyUniqueSearchItemAvailableRestartWithItSelected() {
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithOnlyTicketMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(discountRepository.getCurrentConfiguration()).thenReturn(WITHOUT_DISCOUNT);

        presenter.initialize();

        verify(view).showSelectedItem(paymentMethodSearch.getGroups().get(0));
    }

    @Test
    public void whenOnlyCardPaymentTypeAvailableStartCardFlow() {
        final PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyCreditCardMLA();
        final PaymentVaultPresenter presenter = getPresenter();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(discountRepository.getCurrentConfiguration()).thenReturn(WITHOUT_DISCOUNT);
        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(true);
        when(paymentVaultTitleSolver.solveTitle()).thenReturn(CUSTOM_PAYMENT_VAULT_TITLE);

        presenter.initialize();

        verify(view).showAmount(discountRepository.getCurrentConfiguration(),
            checkoutPreference.getTotalAmount(), mockSite);
        verify(view).saveAutomaticSelection(true);
        verify(view).startCardFlow();
        verify(paymentSettingRepository, atLeastOnce()).getCheckoutPreference();
        verify(userSelectionRepository, times(1)).select(PaymentTypes.CREDIT_CARD);
        verify(view).setTitle(paymentVaultTitleSolver.solveTitle());
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenOnlyCardPaymentTypeAvailableAndCardAvailableThenDoNotSelectAutomatically() {
        final PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyCreditCardAndOneCardMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(true);

        presenter.initialize();

        verifyDoNotSelectCustomOptionAutomatically(paymentMethodSearch);
    }

    @Test
    public void whenOnlyCardPaymentTypeAvailableAndAccountMoneyAvailableThenDoNotSelectAutomatically() {
        final PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyCreditCardAndAccountMoneyMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(true);

        presenter.initialize();

        verifyDoNotSelectCustomOptionAutomatically(paymentMethodSearch);
    }

    @Test
    public void whenOnlyOffPaymentTypeAvailableAndAccountMoneyAvailableThenDoNotSelectAutomatically() {
        final PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyOneOffTypeAndAccountMoneyMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(true);

        presenter.initialize();

        verifyDoNotSelectCustomOptionAutomatically(paymentMethodSearch);
    }

    //User selections

    @Test
    public void whenItemWithChildrenSelectedThenShowChildren() {
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        final PaymentMethodSearchItem selectedSearchItem = paymentMethodSearch.getGroups().get(1);
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.setSelectedSearchItem(selectedSearchItem);

        presenter.initialize();

        verify(view).setTitle(selectedSearchItem.getChildrenHeader());
        final List<PaymentMethodViewModel> models =
            new PaymentMethodSearchOptionViewModelMapper(presenter).map(selectedSearchItem.getChildren());

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));
        verify(view).hideProgress();
    }

    @Test
    public void whenCardPaymentTypeSelectedThenStartCardFlow() {
        final PaymentVaultPresenter presenter = getBasePresenter(stubView);
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();
        presenter.selectItem(paymentMethodSearch.getGroups().get(0));

        Assert.assertTrue(stubView.cardFlowStarted);
    }

    @Test
    public void whenSavedCardSelectedThenStartSavedCardFlow() {
        final PaymentVaultPresenter presenter = getBasePresenter(stubView);
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();
        presenter.selectItem(paymentMethodSearch.getCustomSearchItems().get(1));

        Assert.assertTrue(stubView.cardFlowStarted);
    }

    @Test
    public void whenPaymentMethodSearchItemIsNotCardAndDoesNotHaveChildrenThenStartPaymentMethodsSelection() {
        final PaymentVaultPresenter presenter = getBasePresenter(stubView);
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        paymentMethodSearch.getGroups().get(1).getChildren()
            .removeAll(paymentMethodSearch.getGroups().get(1).getChildren());

        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();

        presenter.selectItem(paymentMethodSearch.getGroups().get(1));
        Assert.assertTrue(stubView.paymentMethodSelectionStarted);
    }

    @Test
    public void whenPaymentMethodTypeSelectedThenSelectPaymentMethod() {
        final PaymentVaultPresenter presenter = getBasePresenter(stubView);
        final PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithPaymentMethodOnTop();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();
        presenter.selectItem(paymentMethodSearch.getGroups().get(1));

        Assert.assertEquals(paymentMethodSearch.getGroups().get(1).getId(), stubView.selectedPaymentMethod.getId());
    }

    //Payment Preference tests
    @Test
    public void whenAllPaymentMethodsExcludedThenShowError() {
        final PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setExcludedPaymentTypeIds(PaymentTypes.getAllPaymentTypes());

        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);

        presenter.initialize();

        verify(view).showError(any(MercadoPagoError.class), anyString());
    }

    @Test
    public void whenInvalidDefaultInstallmentsThenShowError() {
        final PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setDefaultInstallments(BigDecimal.ONE.negate().intValue());
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);

        presenter.initialize();

        verify(view).showError(any(MercadoPagoError.class), anyString());
    }

    @Test
    public void whenInvalidMaxInstallmentsThenShowError() {
        final PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setMaxAcceptedInstallments(BigDecimal.ONE.negate().intValue());
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);

        presenter.initialize();

        verify(view).showError(any(MercadoPagoError.class), anyString());
    }

    @Test
    public void whenMaxSavedCardNotSetThenDoNotLimitCardsShown() {
        final PaymentVaultPresenter presenter = getBasePresenter(stubView);
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();

        assertEquals(paymentMethodSearch.getCustomSearchItems().size() + paymentMethodSearch.getGroups().size(),
            stubView.searchItemsShown.size());
    }

    //Discounts
    @Test
    public void whenDetailClickedThenShowDetailDialog() {
        presenter.onDetailClicked(mock(DiscountConfigurationModel.class));
        verify(view).showDetailDialog(any(DiscountConfigurationModel.class));
    }

    @Test
    public void whenGroupsRetrievalReturnsAliExceptionThenShowError() {
        final ApiException apiException = new ApiException();
        when(groupsRepository.getGroups()).thenReturn(new StubFailMpCall<PaymentMethodSearch>(apiException));

        presenter.initialize();

        verify(view).showError(any(MercadoPagoError.class), anyString());
    }

    @Test
    public void whenResourcesRetrievalFailedAndRecoverRequestedThenRepeatRetrieval() {
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getCompletePaymentMethodSearchMLA();
        final ApiException apiException = new ApiException();
        when(groupsRepository.getGroups()).thenReturn(new StubFailMpCall<PaymentMethodSearch>(apiException));

        presenter.initialize();

        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        //Presenter gets resources, fails
        presenter.recoverFromFailure();

        final List<PaymentMethodViewModel> models =
            new CustomSearchOptionViewModelMapper(presenter, disabledPaymentMethodRepository)
                .map(paymentMethodSearch.getCustomSearchItems());
        models.addAll(new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups()));

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));
    }

    @Test
    public void whenPaymentMethodSearchSetAndHasItemsThenShowThem() {
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();

        final List<PaymentMethodViewModel> models =
            new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups());

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));
    }

    @Test
    public void whenHasCustomItemsThenShowThemAll() {
        // 6 Saved Cards + Account Money
        final PaymentMethodSearch paymentMethodSearch = PaymentMethodSearchs.getPaymentMethodSearchWithSavedCardsMLA();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.initialize();

        final List<PaymentMethodViewModel> models =
            new CustomSearchOptionViewModelMapper(presenter, disabledPaymentMethodRepository)
                .map(paymentMethodSearch.getCustomSearchItems());
        models.addAll(new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups()));

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));
    }

    @Test
    public void whenBoletoSelectedThenCollectPayerInformation() {
        final PaymentVaultPresenter presenter = getPresenter();
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getPaymentMethodSearchWithOnlyBolbradescoMLB()));
        when(discountRepository.getCurrentConfiguration()).thenReturn(WITHOUT_DISCOUNT);

        presenter.initialize();

        verify(view).collectPayerInformation();
    }

    @Test
    public void whenPayerInformationReceivedThenFinishWithPaymentMethodSelection() {
        final PaymentVaultPresenter presenter = getPresenter();

        presenter.onPayerInformationReceived();

        verify(view).finishPaymentMethodSelection(userSelectionRepository.getPaymentMethod());
    }

    @Test
    public void whenHasConsumerCreditsVerifyIsShown() {
        final PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyMercadoCredito();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        presenter.initialize();

        final List<PaymentMethodViewModel> models =
            new CustomSearchOptionViewModelMapper(presenter, disabledPaymentMethodRepository)
                .map(paymentMethodSearch.getCustomSearchItems());
        models.addAll(new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups()));

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));

        verify(view).hideAmountRow();
        verify(view).hideProgress();
        verify(view).setTitle(any());
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenConsumerCreditsIsSelectedVerifyFlow() {
        final PaymentMethodSearch paymentMethodSearch =
            PaymentMethodSearchs.getPaymentMethodSearchWithOnlyMercadoCredito();
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        presenter.initialize();

        final List<PaymentMethodViewModel> models =
            new CustomSearchOptionViewModelMapper(presenter, disabledPaymentMethodRepository)
                .map(paymentMethodSearch.getCustomSearchItems());
        models.addAll(new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups()));

        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));

        final CustomSearchItem consumerCreditsItem = paymentMethodSearch.getCustomSearchItems()
            .stream()
            .filter(item -> item.getPaymentMethodId().equals(PaymentMethods.CONSUMER_CREDITS))
            .findAny().orElse(null);

        Assert.assertNotNull(consumerCreditsItem);
        presenter.selectItem(consumerCreditsItem);

        Assert.assertTrue(PaymentTypes.isDigitalCurrency(consumerCreditsItem.getType()));

        verify(view, times(1)).showInstallments();

        verify(view).hideAmountRow();
        verify(view).hideProgress();
        verify(view).setTitle(any());
        verifyNoMoreInteractions(view);
    }
    // --------- Helper methods ----------- //

    private void verifyInitializeWithGroups() {
        verify(view, atLeastOnce()).showAmount(discountRepository.getCurrentConfiguration(),
            paymentSettingRepository.getCheckoutPreference().getTotalAmount(),
            paymentSettingRepository.getCheckoutPreference().getSite());
        verify(view, atLeastOnce()).hideProgress();
    }

    private void verifyDoNotSelectCustomOptionAutomatically(final PaymentMethodSearch paymentMethodSearch) {
        verifyInitializeWithGroups();
        final List<PaymentMethodViewModel> models =
            new CustomSearchOptionViewModelMapper(presenter, disabledPaymentMethodRepository)
                .map(paymentMethodSearch.getCustomSearchItems());
        models.addAll(new PaymentMethodSearchOptionViewModelMapper(presenter).map(paymentMethodSearch.getGroups()));
        verify(view).showSearchItems(ReflectionArgumentMatchers.reflectionEquals(models));
        verify(view).setTitle(paymentVaultTitleSolver.solveTitle());
        verifyNoMoreInteractions(view);
    }

    private static class StubView implements PaymentVaultView {

        /* default */ List<PaymentMethodViewModel> searchItemsShown;
        /* default */ boolean cardFlowStarted = false;
        /* default */ PaymentMethod selectedPaymentMethod;
        /* default */ boolean paymentMethodSelectionStarted = false;

        @Override
        public void showInstallments() {

        }

        @Override
        public void showSelectedItem(final PaymentMethodSearchItem item) {
            searchItemsShown = new PaymentMethodSearchOptionViewModelMapper(any()).map(item.getChildren());
        }

        @Override
        public void showProgress() {
            //Not yet tested
        }

        @Override
        public void hideProgress() {
            //Not yet tested
        }

        @Override
        public void showSearchItems(final List<PaymentMethodViewModel> searchItems) {
            searchItemsShown = searchItems;
        }

        @Override
        public void showError(final MercadoPagoError mpException, final String requestOrigin) {
            //Not yet tested
        }

        @Override
        public void setTitle(final String title) {
            //Not yet tested
        }

        @Override
        public void startCardFlow() {
            cardFlowStarted = true;
        }

        @Override
        public void startPaymentMethodsSelection(final PaymentPreference paymentPreference) {
            paymentMethodSelectionStarted = true;
        }

        @Override
        public void finishPaymentMethodSelection(final PaymentMethod selectedPaymentMethod) {
            this.selectedPaymentMethod = selectedPaymentMethod;
        }

        @Override
        public void showAmount(@NonNull final DiscountConfigurationModel discountModel,
            @NonNull final BigDecimal totalAmount,
            @NonNull final Site site) {
            //Not yet tested
        }

        @Override
        public void hideAmountRow() {
            //Do nothing
        }

        @Override
        public void collectPayerInformation() {
            //Not yet tested
        }

        @Override
        public void showDetailDialog(@NonNull final DiscountConfigurationModel discountModel) {
            //Do nothing
        }

        @Override
        public void showEmptyPaymentMethodsError() {
            //Not yet tested
        }

        @Override
        public void showMismatchingPaymentMethodError() {
            //Not yet tested
        }

        @Override
        public void showDisabledPaymentMethodDetailDialog(@NonNull final String paymentMethodType) {
            //Do nothing
        }

        @Override
        public void cancel(@Nullable final Intent data) {
            //Do nothing
        }

        @Override
        public void overrideTransitionInOut() {
            //Do nothing
        }

        @Override
        public void saveAutomaticSelection(final boolean automaticSelection) {
            //Not yet tested
        }
    }
}