package com.mercadopago.android.px.cardvault;

import android.content.Intent;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.datasource.IESCManager;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultPresenter;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultView;
import com.mercadopago.android.px.internal.features.installments.PayerCostSolver;
import com.mercadopago.android.px.internal.features.providers.CardVaultProvider;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CardVaultPresenterTest {

    private CardVaultPresenter presenter;

    @Mock private CardVaultProvider cardVaultProvider;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private AmountConfigurationRepository amountConfigurationRepository;
    @Mock private IESCManager IESCManager;
    @Mock private PayerCostSolver payerCostSolver;

    @Mock private CardVaultView view;

    @Before
    public void setUp() {
        configurePaymentPreferenceMock(null);

        presenter = new CardVaultPresenter(userSelectionRepository, paymentSettingRepository, IESCManager,
            amountConfigurationRepository, payerCostSolver);

        presenter.setPaymentRecovery(null);
        presenter.attachView(view);
        presenter.attachResourcesProvider(cardVaultProvider);
    }

    private void configurePaymentPreferenceMock(@Nullable final Integer defaultInstallments) {
        final PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setDefaultInstallments(defaultInstallments);
    }

    private PaymentRecovery providePaymentRecoveryMock() {
        final PaymentRecovery paymentRecovery = mock(PaymentRecovery.class);
        when(paymentRecovery.isTokenRecoverable()).thenReturn(true);
        return paymentRecovery;
    }

    private void configureMockedCardWith() {
        final Card card = mock(Card.class);
        when(card.getId()).thenReturn("1");
        when(userSelectionRepository.getCard()).thenReturn(card);
        presenter.setCard(card);
    }

    @Test
    public void whenTokenIsRecoverableThenStartTokenRecoveryFlow() {

        when(paymentSettingRepository.getToken()).thenReturn(mock(Token.class));

        PaymentRecovery paymentRecovery = providePaymentRecoveryMock();
        presenter.setPaymentRecovery(paymentRecovery);

        presenter.initialize();

        verify(view).askForSecurityCodeFromTokenRecovery(Reason.from(paymentRecovery));
    }

    @Test
    public void whenSavedCardNotPresentThenStartGuessingFlow() {
        presenter.initialize();

        verify(view).askForCardInformation();
    }

    @Test
    public void whenFlowCanceledThenFinishFlow() {
        presenter.onResultCancel();

        verify(view).cancelCardVault();
    }

    @Test
    public void whenCardHasInstallmentSelectedAndTokenThenFinishFlow() {
        presenter.resolveSecurityCodeRequest();

        verify(view).finishWithResult();
    }

    /**
     * Guessing
     */

    @Test
    public void whenGuessingCardHasInstallmentSelectedAndWithoutTokenThenStartSecurityCodeFlow() {
        configureMockedCardWith();
        final Card card = userSelectionRepository.getCard();
        when(IESCManager.getESC(card.getId(), card.getFirstSixDigits(), card.getLastFourDigits()))
            .thenReturn(TextUtil.EMPTY);

        presenter.resolveInstallmentsRequest();

        verify(view).startSecurityCodeActivity(Reason.SAVED_CARD);
    }

    @Test
    public void whenGuessingHasIssuerAndPayerCostThenFinishWithResult() {
        when(userSelectionRepository.getIssuer()).thenReturn(mock(Issuer.class));
        when(userSelectionRepository.getPayerCost()).thenReturn(mock(PayerCost.class));
        when(paymentSettingRepository.getToken()).thenReturn(mock(Token.class));

        presenter.resolveNewCardRequest(mock(Intent.class));

        verify(view).finishWithResult();
    }

    @Test
    public void whenGuessingWithIssuerSelectedAndPayerCostNotSelectedThenStartInstallmentFlow() {
        when(userSelectionRepository.getIssuer()).thenReturn(mock(Issuer.class));
        when(paymentSettingRepository.getToken()).thenReturn(mock(Token.class));

        presenter.resolveNewCardRequest(mock(Intent.class));

        verify(view).askForInstallments();
    }

    @Test
    public void whenGuessingCardHasNoIssuerThenStartIssuerFlow() {
        when(paymentSettingRepository.getToken()).thenReturn(mock(Token.class));

        presenter.resolveNewCardRequest(mock(Intent.class));

        verify(view).startIssuersActivity(argThat(List::isEmpty));
    }

    /**
     * Saved card
     */

    @Test
    public void verifyResolvesEmptyPayerCostList() {
        presenter.onEmptyOptions();

        verify(view).showEmptyPayerCostScreen();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void verifyResolvesOnSelectedPayerCostPayerCostListWithoutESC() {
        configureMockedCardWith();
        final Card card = userSelectionRepository.getCard();
        when(IESCManager.getESC(card.getId(), card.getFirstSixDigits(), card.getLastFourDigits()))
            .thenReturn(TextUtil.EMPTY);

        presenter.onSelectedPayerCost();

        verify(view).startSecurityCodeActivity(Reason.SAVED_CARD);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void verifyResolvesOnSelectedPayerCostPayerCostListWithESC() {
        configureMockedCardWith();
        final Card card = userSelectionRepository.getCard();
        when(IESCManager.getESC(card.getId(), card.getFirstSixDigits(), card.getLastFourDigits()))
            .thenReturn("1");

        presenter.onSelectedPayerCost();

        verify(view).showProgressLayout();
    }

    @Test
    public void verifyResolvesDisplayInstallments() {
        final List<PayerCost> payerCosts = Collections.singletonList(mock(PayerCost.class));

        presenter.displayInstallments(payerCosts);

        verify(view).askForInstallments();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void verifyIsSavedCardAndSolverIsCalled() {
        configureMockedCardWith();
        when(amountConfigurationRepository.getCurrentConfiguration()).thenReturn(mock(AmountConfiguration.class));

        presenter.initialize();

        verify(payerCostSolver)
            .solve(presenter, amountConfigurationRepository.getCurrentConfiguration().getPayerCosts());
    }

    @Test
    public void whenIsOneTapFlowThenDontAskForInstallmentsAndContinueFlow() {
        configureMockedCardWith();
        when(userSelectionRepository.getPayerCost()).thenReturn(mock(PayerCost.class));

        presenter.initialize();

        verifyNoMoreInteractions(payerCostSolver);
    }

    /*

    @Test
    public void ifPaymentRecoveryIsSetThenStartTokenRecoverableFlow() {

        final Token mockedToken = Tokens.getToken();
        final PaymentMethod mockedPaymentMethod = InitStubUtils.getPaymentMethodOnVisa();
        final Issuer mockedIssuer = Issuers.getIssuerMLA();
        final String mockedPaymentStatus = Payment.StatusCodes.STATUS_REJECTED;
        final String mockedPaymentStatusDeatil = Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE;

        final PaymentRecovery mockedPaymentRecovery =
            new PaymentRecovery(mockedToken, mockedPaymentMethod, mockedIssuer, mockedPaymentStatus,
                mockedPaymentStatusDeatil);

        presenter.setPaymentRecovery(mockedPaymentRecovery);

        presenter.initialize();

        assertNotNull(presenter.getCardInfo());
        assertNotNull(presenter.getPaymentMethod());
        assertNotNull(presenter.getToken());
        assertTrue(mockedView.recoverableTokenFlowStarted);
        assertTrue(mockedView.securityCodeFlowStarted);
    }


    @Test
    public void whenSecurityCodeResolvedWithPaymentRecoverySetThenFinishWithResult() {

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Token mockedToken = Tokens.getToken();
        final PaymentMethod mockedPaymentMethod = InitStubUtils.getPaymentMethodOnVisa();
        final Issuer mockedIssuer = Issuers.getIssuerMLA();
        final String mockedPaymentStatus = Payment.StatusCodes.STATUS_REJECTED;
        final String mockedPaymentStatusDetail = Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE;

        final PaymentRecovery mockedPaymentRecovery =
            new PaymentRecovery(mockedToken, mockedPaymentMethod, mockedIssuer, mockedPaymentStatus,
                mockedPaymentStatusDetail);

        presenter.setPaymentRecovery(mockedPaymentRecovery);

        presenter.initialize();

        paymentSettingRepository.configure(mockedToken);
        when(paymentSettingRepository.getToken()).thenReturn(mockedToken);

        //Response from SecurityCodeActivity, with recoverable token
        presenter.resolveSecurityCodeRequest();

        assertNotNull(presenter.getToken());
        assertTrue(mockedView.finishedWithResult);
    }


    @Test
    public void onCreateTokenWithESCHasErrorThenAskForSecurityCode() {

        provider.setESCEnabled(true);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId(MOCK_CARD_ID);
        presenter.setCard(mockedCard);

        presenter.initialize();

        //Set error with create token ESC
        final ApiException apiException = Tokens.getInvalidTokenWithESC();
        provider.setResponse(new MercadoPagoError(apiException, ""));
        //Installments onActivityResult
        presenter.resolveInstallmentsRequest();

        assertTrue(mockedView.installmentsFlowStarted);
        assertTrue(mockedView.securityCodeFlowStarted);
        assertTrue(provider.deleteRequested);
        assertEquals(MOCK_CARD_ID, provider.cardIdDeleted);
    }

    @Test
    public void onCreateTokenWithESCHasErrorFingerprintThenAskForSecurityCode() {

        provider.setESCEnabled(true);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId(MOCK_CARD_ID);
        presenter.setCard(mockedCard);

        presenter.initialize();

        //Set error with create token ESC
        final ApiException apiException = Tokens.getInvalidTokenWithESCFingerprint();
        provider.setResponse(new MercadoPagoError(apiException, ""));

        //Installments onActivityResult
        presenter.resolveInstallmentsRequest();

        assertTrue(mockedView.installmentsFlowStarted);
        assertTrue(mockedView.securityCodeFlowStarted);
        assertTrue(provider.deleteRequested);
        assertEquals(MOCK_CARD_ID, provider.cardIdDeleted);
    }

    @Test
    public void onESCDisabledThenAskForSecurityCodeWhenCardIdIsSaved() {

        //ESC disabled
        provider.setESCEnabled(false);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId(MOCK_CARD_ID);
        presenter.setCard(mockedCard);

        presenter.initialize();

        //Installments response
        presenter.resolveInstallmentsRequest();
        assertTrue(mockedView.securityCodeFlowStarted);

        final Token mockedToken = Tokens.getToken();
        provider.setResponse(mockedToken);

        presenter.startSecurityCodeFlowIfNeeded();
        assertTrue(mockedView.securityCodeFlowStarted);

        assertEquals(provider.successfulTokenResponse.getId(), mockedToken.getId());
    }

    @Test
    public void onSavedCardWithESCSavedThenCreateTokenWithESC() {

        provider.setESCEnabled(true);

        final List<Installment> installmentsList = Installments.getInstallmentsList();
        provider.setResponse(installmentsList);

        final Card mockedCard = Cards.getCard();
        mockedCard.setId(MOCK_CARD_ID);
        presenter.setCard(mockedCard);

        //Set ESC to simulate it is saved
        presenter.setESC("12345678");

        presenter.initialize();

        final Token mockedToken = Tokens.getTokenWithESC();

        //Installments response
        provider.setResponse(mockedToken);
        presenter.resolveInstallmentsRequest();
        //Set error with create token ESC
        assertEquals(provider.successfulTokenResponse.getId(), mockedToken.getId());
    }
*/
}
