package com.mercadopago.android.px.guessing;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.core.internal.MercadoPagoCardStorage;
import com.mercadopago.android.px.internal.datasource.CardAssociationGatewayService;
import com.mercadopago.android.px.internal.datasource.CardAssociationService;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCard;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardStoragePresenter;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.repository.CardPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.services.CardService;
import com.mercadopago.android.px.mocks.Cards;
import com.mercadopago.android.px.mocks.IdentificationTypes;
import com.mercadopago.android.px.mocks.PaymentMethods;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "PMD.ExcessiveClassLength", "unchecked" })
@RunWith(MockitoJUnitRunner.class)
public class GuessingCardStoragePresenterTest {

    private static final String DUMMY_ACCESS_TOKEN =
        "TEST-8331125484710872-100611-d91fdace2a0a6e42293927d3289b53c3__LC_LB__-99037920";
    private static final String DUMMY_TOKEN_ID = "123";
    private static final String DUMMY_CARD_ID = "1234567890";
    private static final String DUMMY_TOKEN_ESC = "ABCDEFGH";
    final List<PaymentMethod> cardPaymentMethodListMLA = PaymentMethods.getCardPaymentMethodListMLA();

    private GuessingCardStoragePresenter presenter;

    @Mock private CardPaymentMethodRepository cardPaymentMethodRepository;
    @Mock private IdentificationRepository identificationRepository;
    @Mock private ESCManagerBehaviour escManagerBehaviour;
    @Mock private CardAssociationGatewayService cardAssociationGatewayService;
    @Mock private GuessingCard.View view;
    @Mock private CardService cardService;
    @Mock private final CardAssociationService cardAssociationService = new CardAssociationService(cardService);
    @Mock private MercadoPagoCardStorage mercadoPagoCardStorage;

    @Before
    public void setUp() {
        when(mercadoPagoCardStorage.getAccessToken()).thenReturn(DUMMY_ACCESS_TOKEN);
        presenter = getPresenter();
        whenGetIdentificationTypesAsync();
    }

    @NonNull
    private GuessingCardStoragePresenter getBasePresenter(
        final GuessingCard.View view) {
        final GuessingCardStoragePresenter presenter =
            new GuessingCardStoragePresenter(mercadoPagoCardStorage, cardPaymentMethodRepository,
                identificationRepository,
                cardAssociationService,
                escManagerBehaviour, cardAssociationGatewayService);

        presenter.attachView(view);
        return presenter;
    }

    @NonNull
    private GuessingCardStoragePresenter getPresenter() {
        return getBasePresenter(view);
    }

    @Test
    public void whenPresenterInitializedCorrectlyThenInitializeView() {
        initializePresenterWithValidCardPaymentMethods();

        verify(view).onValidStart();
        //We shouldn't be showing bank deals
        verify(view).hideBankDeals();
    }

    @Test
    public void cardPaymentMethodListSetIsEmptyThenShowError() {
        final List<PaymentMethod> emptyList = Collections.emptyList();
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubSuccessMpCall<>(emptyList));

        presenter.initialize();
        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenCardPaymentMethodListSetIsNullThenShowError() {
        final List<PaymentMethod> nullList = null;
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubSuccessMpCall<>(nullList));

        presenter.initialize();
        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenCardPaymentMethodCallFailsThenShowError() {
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubFailMpCall<List<PaymentMethod>>(PaymentMethods.getDoNotFindPaymentMethodsException()));

        presenter.initialize();
        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenCardPaymentListIsNotEmptyThenStartGuessingForm() {
        initializePresenterWithValidCardPaymentMethods();

        verify(view).initializeTitle();
        verify(view).setSecurityCodeListeners();
        verify(view).setIdentificationTypeListeners();
        verify(view).setIdentificationNumberListeners();
    }

    @Test
    public void whenPaymentMethodIsSetAndDeletedThenClearConfiguration() {

        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> selected = Collections.singletonList(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), selected.get(0).getId());

        presenter.resolvePaymentMethodListSet(selected, Cards.MOCKED_BIN_VISA);

        verify(view).resolvePaymentMethodSet(selected.get(0));

        presenter.resolvePaymentMethodCleared();

        assertEquals(Card.CARD_DEFAULT_SECURITY_CODE_LENGTH, presenter.getSecurityCodeLength());
        assertEquals(CardView.CARD_SIDE_BACK, presenter.getSecurityCodeLocation());
        assertTrue(presenter.isSecurityCodeRequired());
        assertEquals(0, presenter.getSavedBin().length());

        verify(view).clearErrorView();
        verify(view).clearCardNumberInputLength();
        verify(view).clearSecurityCodeEditText();
        verify(view).checkClearCardView();
    }

    @Test
    public void whenPaymentMethodSetHasIdentificationTypeRequiredThenShowIdentificationView() {

        final List<IdentificationType> identificationTypes = IdentificationTypes.getIdentificationTypes();

        when(identificationRepository.getIdentificationTypes(anyString())).thenReturn(new StubSuccessMpCall<>
            (identificationTypes));

        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");

        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        assertTrue(presenter.isIdentificationNumberRequired());
        verify(view).resolvePaymentMethodSet(mockedGuessedPaymentMethods.get(0));
        verify(view).initializeIdentificationTypes(identificationTypes, identificationTypes.get(0));
    }

    @Test
    public void whenPaymentMethodSetDoNotHaveIdentificationTypeRequiredThenHideIdentificationView() {
        final List<PaymentMethod> idNotRequiredPaymentMethods = new ArrayList<>();
        idNotRequiredPaymentMethods.add(PaymentMethods.getPaymentMethodWithIdNotRequired());

        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString()))
            .thenReturn(new StubSuccessMpCall<>(idNotRequiredPaymentMethods));
        presenter.initialize();

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), idNotRequiredPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(idNotRequiredPaymentMethods, Cards.MOCKED_BIN_CORDIAL);

        verify(view).resolvePaymentMethodSet(idNotRequiredPaymentMethods.get(0));

        assertFalse(presenter.isIdentificationNumberRequired());
        verify(view, times(0)).initializeIdentificationTypes(any(List.class), any(IdentificationType.class));
        verify(view).hideIdentificationInput();
    }

    @Test
    public void whenIdentificationTypesListSetIsEmptyThenShowError() {
        final List<IdentificationType> identificationTypes = new ArrayList<>();

        when(identificationRepository.getIdentificationTypes(anyString())).thenReturn(new StubSuccessMpCall<>
            (identificationTypes));

        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenIdentificationTypesCallFailsThenThenShowError() {
        final ApiException apiException = mock(ApiException.class);

        when(identificationRepository.getIdentificationTypes(DUMMY_ACCESS_TOKEN))
            .thenReturn(new StubFailMpCall<List<IdentificationType>>(apiException));

        initializePresenterWithValidCardPaymentMethods();

        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenTokenIsNullThenShowError() {

        initializePresenterWithValidCardPaymentMethods();

        final Token token = null;

        when(cardAssociationGatewayService.createToken(anyString(), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.createToken();

        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenTokenCallFailsThenShowError() {
        final ApiException apiException = mock(ApiException.class);

        initializePresenterWithValidCardPaymentMethods();

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubFailMpCall<Token>(apiException));

        presenter.createToken();

        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenPaymentMethodSetThenFetchIssuers() {
        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        verify(cardAssociationService)
            .getCardIssuers(DUMMY_ACCESS_TOKEN, mockedGuessedPaymentMethods.get(0).getId(), Cards.MOCKED_BIN_VISA);
    }

    @Test
    public void whenTokenNotNullAndCardIsNullThenFinishCardStorageFlowWithError() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        final Card card = null;
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());
        final PaymentMethod paymentMethod = mockedGuessedPaymentMethods.get(0);

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), paymentMethod.getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId(),
                dummyIssuer.getId()))
            .thenReturn(new StubSuccessMpCall<>(card));

        presenter.createToken();

        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenTokenNotNullThenAssociateCardToUser() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        final Card card = mock(Card.class);
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());
        final PaymentMethod paymentMethod = mockedGuessedPaymentMethods.get(0);

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), paymentMethod.getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId(),
                dummyIssuer.getId()))
            .thenReturn(new StubSuccessMpCall<>(card));

        when(cardAssociationGatewayService.createEscToken(eq(DUMMY_ACCESS_TOKEN), any(SavedESCCardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.createToken();

        verify(cardAssociationService)
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId(),
                dummyIssuer.getId());
    }

    @Test
    public void whenAssociateCardFailsThenShowError() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId(),
                dummyIssuer.getId()))
            .thenReturn(new StubFailMpCall<Card>(new ApiException()));

        presenter.createToken();

        verify(view).showErrorScreen(DUMMY_ACCESS_TOKEN);
    }

    @Test
    public void whenAssociateCardSucceedesThenSaveEscAndShowSuccessScreen() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);
        token.setCardId(DUMMY_CARD_ID);
        token.setEsc(DUMMY_TOKEN_ESC);

        final Card stubCard = new Card();

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId(),
                dummyIssuer.getId()))
            .thenReturn(new StubSuccessMpCall<>(stubCard));

        when(cardAssociationGatewayService.createEscToken(eq(DUMMY_ACCESS_TOKEN), any(SavedESCCardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.createToken();

        verify(escManagerBehaviour).saveESCWith(DUMMY_CARD_ID, DUMMY_TOKEN_ESC);
        verify(view).showSuccessScreen();
    }

    @Test
    public void whenSaveEscFailsThenShowSuccessScreenAnyway() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        final Card stubCard = new Card();
        stubCard.setId(DUMMY_CARD_ID);
        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId(),
                dummyIssuer.getId()))
            .thenReturn(new StubSuccessMpCall<>(stubCard));

        when(cardAssociationGatewayService.createEscToken(eq(DUMMY_ACCESS_TOKEN), any(SavedESCCardToken.class)))
            .thenReturn(new StubFailMpCall<Token>(new ApiException()));

        presenter.createToken();

        // Do not save esc, since the tokenization failed
        verify(escManagerBehaviour, never()).saveESCWith(DUMMY_CARD_ID, DUMMY_TOKEN_ESC);
        verify(view).showSuccessScreen();
    }

    @Test
    public void whenMoreThanOneIssuerIsReturnedThenCallIssuersActivity() {
        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer1 = new Issuer(1L, "Dummy Issuer #1");
        final Issuer dummyIssuer2 = new Issuer(2L, "Dummy Issuer #2");
        final List<Issuer> issuers = Arrays.asList(dummyIssuer1, dummyIssuer2);
        mockIssuers(issuers, mockedGuessedPaymentMethods.get(0).getId());

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);
        presenter.createToken();

        verify(view)
            .askForIssuer(any(CardInfo.class), eq(issuers), eq(mockedGuessedPaymentMethods.get(0)));
    }

    // --------- Helper methods ----------- //

    private void initializePresenterWithValidCardPaymentMethods() {
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString()))
            .thenReturn(new StubSuccessMpCall<>(cardPaymentMethodListMLA));
        presenter.initialize();
    }

    private void mockIssuers(final List<Issuer> issuers, final String paymentMethodId) {

        when(cardAssociationService
            .getCardIssuers(eq(DUMMY_ACCESS_TOKEN), eq(paymentMethodId), anyString()))
            .thenReturn(new StubSuccessMpCall<>(issuers));
    }

    private void whenGetIdentificationTypesAsync() {
        final List<IdentificationType> identificationTypes = IdentificationTypes.getIdentificationTypes();

        when(identificationRepository.getIdentificationTypes(anyString())).thenReturn(new StubSuccessMpCall<>
            (identificationTypes));
    }

    @Test
    public void whenErrorProducedThenFinishFlowSkippingErrorScreen() {
        final List<PaymentMethod> emptyList = Collections.emptyList();
        when(cardPaymentMethodRepository.getCardPaymentMethods(anyString())).thenReturn(
            new StubSuccessMpCall<>(emptyList));

        when(mercadoPagoCardStorage.shouldSkipResultScreen()).thenReturn(true);

        presenter.initialize();
        verify(view).finishCardStorageFlowWithError();
    }

    @Test
    public void whenNormalFlowFinishedThenFinishFlowSkippingSuccessScreen() {

        initializePresenterWithValidCardPaymentMethods();
        final List<PaymentMethod> mockedGuessedPaymentMethods = new ArrayList<>();
        mockedGuessedPaymentMethods.add(PaymentMethods.getPaymentMethodOnVisa());

        final Issuer dummyIssuer = new Issuer(1L, "Dummy Issuer");
        mockIssuers(Collections.singletonList(dummyIssuer), mockedGuessedPaymentMethods.get(0).getId());

        when(mercadoPagoCardStorage.shouldSkipResultScreen()).thenReturn(true);

        presenter.resolvePaymentMethodListSet(mockedGuessedPaymentMethods, Cards.MOCKED_BIN_VISA);

        final Token token = new Token();
        token.setId(DUMMY_TOKEN_ID);
        token.setCardId(DUMMY_CARD_ID);
        token.setEsc(DUMMY_TOKEN_ESC);

        final Card stubCard = new Card();

        when(cardAssociationGatewayService.createToken(eq(DUMMY_ACCESS_TOKEN), any(CardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        when(cardAssociationService
            .associateCardToUser(DUMMY_ACCESS_TOKEN, DUMMY_TOKEN_ID, mockedGuessedPaymentMethods.get(0).getId(),
                dummyIssuer.getId()))
            .thenReturn(new StubSuccessMpCall<>(stubCard));

        when(cardAssociationGatewayService.createEscToken(eq(DUMMY_ACCESS_TOKEN), any(SavedESCCardToken.class)))
            .thenReturn(new StubSuccessMpCall<>(token));

        presenter.createToken();

        verify(escManagerBehaviour).saveESCWith(DUMMY_CARD_ID, DUMMY_TOKEN_ESC);
        verify(view).finishCardStorageFlowWithSuccess();
    }
}
