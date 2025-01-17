package com.mercadopago.android.px.internal.features.installments;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.SummaryAmountRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.mocks.StubSummaryAmount;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstallmentsPresenterTest {

    private InstallmentsPresenter presenter;

    @Mock private AmountRepository amountRepository;
    @Mock private PaymentSettingRepository configuration;
    @Mock private CheckoutPreference checkoutPreference;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private SummaryAmountRepository summaryAmountRepository;
    @Mock private AmountConfigurationRepository amountConfigurationRepository;
    @Mock private AdvancedConfiguration advancedConfiguration;
    @Mock private PaymentMethod paymentMethod;
    @Mock private InstallmentsView view;
    @Mock private DiscountConfigurationModel discountConfigurationModel;
    @Mock private AmountConfiguration amountConfiguration;

    @Before
    public void setUp() {
        when(checkoutPreference.getSite()).thenReturn(Sites.ARGENTINA);
        when(configuration.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(configuration.getAdvancedConfiguration()).thenReturn(advancedConfiguration);
        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(true);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(discountRepository.getCurrentConfiguration()).thenReturn(discountConfigurationModel);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenReturn(amountConfiguration);
        when(amountRepository.getItemsPlusCharges(anyString())).thenReturn(BigDecimal.TEN);
        presenter = new InstallmentsPresenter(amountRepository, configuration, userSelectionRepository,
            discountRepository, summaryAmountRepository, amountConfigurationRepository);
        presenter.attachView(view);
    }

    @Test
    public void whenIsGuessingFlowThenCallSummaryAmount() {
        when(userSelectionRepository.hasCardSelected()).thenReturn(false);
        presetApiError();

        presenter.initialize();

        verify(view).showLoadingView();
        verify(summaryAmountRepository).getSummaryAmount(anyString());
    }

    @Test
    public void whenIsGuessingFlowAndApiCallFailedThenShowErrorScreen() {
        when(userSelectionRepository.hasCardSelected()).thenReturn(false);
        final ApiException apiException = presetApiError();

        presenter.initialize();

        verify(view).showLoadingView();
        verify(view).hideLoadingView();
        verify(view).showApiErrorScreen(apiException, ApiUtil.RequestOrigin.POST_SUMMARY_AMOUNT);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenIsGuessingAndPayerCostIsSelectedFinishWithResult() {
        when(userSelectionRepository.hasCardSelected()).thenReturn(false);
        final SummaryAmount response = StubSummaryAmount.getSummaryAmountTwoPayerCosts();
        when(summaryAmountRepository.getSummaryAmount(anyString())).thenReturn(new StubSuccessMpCall<>(response));
        final List<PayerCost> payerCosts = response.getAmountConfiguration(response.getDefaultAmountConfiguration())
            .getPayerCosts();

        presenter.initialize();
        presenter.onClick(payerCosts.get(0));

        verify(view).showLoadingView();
        verify(view).hideLoadingView();
        verify(view).showInstallments(payerCosts);
        verify(view).showAmount(discountConfigurationModel, BigDecimal.TEN, Sites.ARGENTINA);
        verify(view).finishWithResult();
        verify(summaryAmountRepository).getSummaryAmount(anyString());
        verify(userSelectionRepository).select(payerCosts.get(0));
        verifyNoMoreInteractions(view, summaryAmountRepository);
    }

    @Test
    public void whenMCOThenShowBankInterestsNotCoveredWarning() {
        when(checkoutPreference.getSite()).thenReturn(Sites.COLOMBIA);
        when(userSelectionRepository.hasCardSelected()).thenReturn(false);
        final SummaryAmount response = StubSummaryAmount.getSummaryAmountTwoPayerCosts();
        when(summaryAmountRepository.getSummaryAmount(anyString())).thenReturn(new StubSuccessMpCall<>(response));

        presenter.initialize();
        verify(view).warnAboutBankInterests();
    }

    @Test
    public void whenCardSelectedThenResolvePayerCosts() {
        final List<PayerCost> payerCosts = Collections.emptyList();
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(amountConfiguration.getPayerCosts()).thenReturn(payerCosts);

        presenter.initialize();

        verify(view).hideLoadingView();
    }

    @Test
    public void whenCardSelectedAndNoPayerCostsThenShowError() {
        final List<PayerCost> payerCosts = Collections.emptyList();
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(amountConfiguration.getPayerCosts()).thenReturn(payerCosts);

        presenter.initialize();

        verify(view).hideLoadingView();
        verify(view).showAmount(discountConfigurationModel, BigDecimal.TEN, Sites.ARGENTINA);
        verify(view).showErrorNoPayerCost();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenCardSelectedAndOnlyOnePayerCostThenSelectAndFinish() {
        final PayerCost selectedPayerCost = mock(PayerCost.class);
        final List<PayerCost> payerCosts = Collections.singletonList(selectedPayerCost);
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(amountConfiguration.getPayerCosts()).thenReturn(payerCosts);

        presenter.initialize();

        verify(userSelectionRepository).select(selectedPayerCost);
        verify(view).hideLoadingView();
        verify(view).showAmount(discountConfigurationModel, amountRepository.getItemsPlusCharges(anyString()),
            checkoutPreference.getSite());
        verify(view).finishWithResult();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenCardSelectedAndMultiplePayerCostThenDisplayThem() {
        final List<PayerCost> payerCosts = Arrays.asList(mock(PayerCost.class), mock(PayerCost.class));
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(amountConfiguration.getPayerCosts()).thenReturn(payerCosts);

        presenter.initialize();

        verify(view).hideLoadingView();
        verify(view).showAmount(discountConfigurationModel, amountRepository.getItemsPlusCharges(anyString()),
            checkoutPreference.getSite());
        verify(view).showInstallments(payerCosts);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenAmountRowIsNotEnabledItShouldBeHidden(){
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(false);

        presenter.initialize();

        verify(view).hideAmountRow();
    }

    @Test
    public void whenAmountRowIsEnabledItShouldBeSetted(){
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(true);

        presenter.initialize();

        verify(view).showAmount(discountConfigurationModel, amountRepository.getItemsPlusCharges(anyString()),
            checkoutPreference.getSite());
    }

    @Test
    public void whenAmountRowIsNotEnabledItShouldBeHiddenWithGuessedCards(){
        when(userSelectionRepository.hasCardSelected()).thenReturn(false);

        final SummaryAmount response = StubSummaryAmount.getSummaryAmountTwoPayerCosts();
        when(summaryAmountRepository.getSummaryAmount(anyString())).thenReturn(new StubSuccessMpCall<>(response));

        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(false);

        presenter.initialize();

        verify(view).hideAmountRow();
    }

    @Test
    public void whenAmountRowIsEnabledItShouldBeSettedWithGuessedCards(){
        when(userSelectionRepository.hasCardSelected()).thenReturn(false);

        final SummaryAmount response = StubSummaryAmount.getSummaryAmountTwoPayerCosts();
        when(summaryAmountRepository.getSummaryAmount(anyString())).thenReturn(new StubSuccessMpCall<>(response));

        when(advancedConfiguration.isAmountRowEnabled()).thenReturn(true);

        presenter.initialize();

        verify(view).showAmount(discountConfigurationModel, amountRepository.getItemsPlusCharges(anyString()),
            checkoutPreference.getSite());
        verify(view).hideLoadingView();
    }

    @NonNull
    private ApiException presetApiError() {
        final ApiException apiException = new ApiException();
        when(summaryAmountRepository.getSummaryAmount(anyString()))
            .thenReturn(new StubFailMpCall<>(apiException));
        return apiException;
    }
}