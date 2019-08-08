package com.mercadopago.android.px.internal.features.checkout;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.core.SplitPaymentProcessor;
import com.mercadopago.android.px.internal.configuration.InternalConfiguration;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.repository.CheckoutPreferenceRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.CheckoutStateModel;
import com.mercadopago.android.px.internal.viewmodel.RecoverPaymentPostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.mappers.BusinessModelMapper;
import com.mercadopago.android.px.mocks.PaymentMethodSearchs;
import com.mercadopago.android.px.mocks.Payments;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.Setting;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CheckoutPreferenceException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubExpiredPreference;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItem;
import static com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils.stubPreferenceOneItemAndPayer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutPresenterTest {

    private static final String DEFAULT_CARD_ID = "260077840";
    private static final String DEBIT_CARD_DEBCABAL = "debcabal";
    public static final String PREF_ID = "pref_id";
    public static final int CUSTOM_RESULT_CODE = 1;

    @Mock private Checkout.View checkoutView;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private GroupsRepository groupsRepository;
    @Mock private PluginRepository pluginRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private CheckoutPreferenceRepository checkoutPreferenceRepository;
    @Mock private InternalConfiguration internalConfiguration;
    @Mock private BusinessModelMapper businessModelMapper;

    private CheckoutPresenter presenter;

    @Before
    public void setUp() {
        presenter = getPresenter();
    }

    @Test
    public void whenChoHasPrefIdSetRetrievePreferenceFromMercadoPagoAndItFailsThenShowError() {
        final ApiException apiException = mock(ApiException.class);

        when(paymentSettingRepository.getCheckoutPreferenceId()).thenReturn(PREF_ID);
        when(checkoutPreferenceRepository.getCheckoutPreference(PREF_ID))
            .thenReturn(new StubFailMpCall<>(apiException));

        presenter.initialize();

        verify(checkoutView).showError(any(MercadoPagoError.class));
    }

    @Test
    public void whenPreferenceIsRetrievedFromMercadoPagoThenStartCheckoutForPreference() {
        final CheckoutPreference checkoutPreference = stubPreferenceOneItem();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreferenceRepository.getCheckoutPreference(anyString()))
            .thenReturn(new StubSuccessMpCall<>(checkoutPreference));
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));

        presenter.retrieveCheckoutPreference(anyString());

        verify(checkoutView).fetchFonts();
        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPreferenceIsNotRetrievedAndRecoverFromFailureThenStartCheckoutForPreference() {
        final CheckoutPreference checkoutPreference = stubPreferenceOneItem();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreferenceRepository.getCheckoutPreference(anyString()))
            .thenReturn(new StubFailMpCall<>(mock(ApiException.class)));
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));

        presenter.retrieveCheckoutPreference(anyString());

        when(checkoutPreferenceRepository.getCheckoutPreference(anyString()))
            .thenReturn(new StubSuccessMpCall<>(checkoutPreference));

        presenter.recoverFromFailure();

        verify(checkoutView).showError(any(MercadoPagoError.class));
        verify(checkoutView).fetchFonts();
        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCheckoutInitializedAndPaymentMethodSearchFailsThenShowError() {
        final ApiException apiException = mock(ApiException.class);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        when(groupsRepository.getGroups())
            .thenReturn(new StubFailMpCall<>(apiException));

        presenter.initialize();

        verify(checkoutView).showProgress();
        verify(checkoutView).fetchFonts();
        verify(checkoutView).showError(any(MercadoPagoError.class));
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenChoHasCompletePrefSetDoNotCallRepositoryToGetPreference() {
        final CheckoutPreference preference = stubPreferenceOneItem();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));

        presenter.initialize();

        verifyInitializeWithPreference();
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(checkoutPreferenceRepository);
    }

    @Test
    public void whenPreferenceIsExpiredThenShowErrorInView() {
        final CheckoutPreference preference = stubExpiredPreference();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        presenter.initialize();
        verify(checkoutView).showCheckoutExceptionError(any(CheckoutPreferenceException.class));
    }

    @Test
    public void whenChoHasPreferenceAndPaymentMethodRetrievedShowPaymentMethodSelection() {
        final CheckoutPreference preference = stubPreferenceOneItemAndPayer();
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        presenter.initialize();

        verify(groupsRepository).getGroups();
        verifyInitializeWithPreference();
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(groupsRepository);
    }

    @Test
    public void whenAPaymentMethodIsSelectedThenShowReviewAndConfirmIfPaymentProcessorShouldNotSkipUserConfirmation() {
        final CheckoutPresenter presenter = getPresenter();
        final PaymentConfiguration paymentConfiguration = mock(PaymentConfiguration.class);
        final SplitPaymentProcessor paymentProcessor = mock(SplitPaymentProcessor.class);

        when(paymentSettingRepository.getPaymentConfiguration()).thenReturn(paymentConfiguration);
        when(paymentConfiguration.getPaymentProcessor()).thenReturn(paymentProcessor);
        when(paymentProcessor.shouldSkipUserConfirmation()).thenReturn(false);

        presenter.onPaymentMethodSelected();
        verify(checkoutView).showReviewAndConfirm(false);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenAPaymentMethodIsSelectedThenShowVisualPaymentProcessorIfItShouldSkipShowUserConfirmation() {
        final PaymentConfiguration paymentConfiguration = mock(PaymentConfiguration.class);
        final SplitPaymentProcessor paymentProcessor = mock(SplitPaymentProcessor.class);

        when(paymentSettingRepository.getPaymentConfiguration()).thenReturn(paymentConfiguration);
        when(paymentConfiguration.getPaymentProcessor()).thenReturn(paymentProcessor);
        when(paymentProcessor.shouldSkipUserConfirmation()).thenReturn(true);

        presenter.onPaymentMethodSelected();
        verify(checkoutView).showPaymentProcessorWithAnimation();
        verify(checkoutView, never()).showReviewAndConfirm(anyBoolean());
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenDefaultCardIdValidSelectedThenShowSecurityCode() {
        final PaymentMethodSearch search = mockPaymentMethodSearchForDriver(true);
        presenter.startFlow(search);
        verify(checkoutView).showSavedCardFlow(any(Card.class));
    }

    @Test
    public void whenDefaultCardIdInvalidSelectedThenShowPaymentVault() {
        final PaymentMethodSearch search = mockPaymentMethodSearchForDriver(false);
        presenter.startFlow(search);
        verify(checkoutView).showPaymentMethodSelection();
    }

    @Test
    public void whenDefaultCardIdIsNullAndDefaultPaymentTypeIsValidThenShowNewCardFlow() {
        final PaymentMethodSearch search = mockPaymentMethodSearchForNewCardDriver();
        presenter.startFlow(search);
        verify(checkoutView).showNewCardFlow();
    }

    @Test
    public void whenCardFlowCanceledAndThereIsValidCardThenCancelCheckout() {
        final PaymentMethodSearch paymentMethodSearch = mockPaymentMethodSearchForDriver(true);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.onCardFlowCancel();

        verify(checkoutView).cancelCheckout();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCardFlowCancelAndDefaultCardIdIsNullAndDefaultPaymentTypeIsValidThenCancelCheckout() {
        final PaymentMethodSearch paymentMethodSearch = mockPaymentMethodSearchForNewCardDriver();
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.onCardFlowCancel();

        verify(checkoutView).cancelCheckout();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCardFlowCanceledAndThereIsInValidCardThenShowPaymentMethodSelection() {
        final PaymentMethodSearch paymentMethodSearch = mockPaymentMethodSearchForDriver(false);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));

        presenter.onCardFlowCancel();

        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCardFlowCanceledAndPaymentMethodSearchFailsThenShowPaymentMethodSelection() {
        final ApiException apiException = mock(ApiException.class);
        when(groupsRepository.getGroups())
            .thenReturn(new StubFailMpCall<>(apiException));

        presenter.onCardFlowCancel();

        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentMethodCanceledThenCancelCheckout() {
        presenter.onPaymentMethodSelectionCancel();
        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenPaymentResultWithCreatedPaymentThenFinishCheckoutWithPaymentResult() {
        final Payment payment = mock(Payment.class);

        when(paymentRepository.hasPayment()).thenReturn(true);
        when(paymentRepository.getPayment()).thenReturn(payment);

        presenter.onPaymentResultResponse();

        verify(checkoutView).finishWithPaymentResult(payment);
    }

    @Test
    public void whenPaymentResultWithoutCreatedPaymentThenFinishCheckoutWithoutPaymentResult() {
        presenter.onPaymentResultResponse();
        verify(checkoutView).finishWithPaymentResult();
    }

    @Test
    public void whenPaymentIsCanceledBecauseUserWantsToSelectOtherPaymentMethodThenShowPaymentMethodSelection() {
        presenter.onChangePaymentMethod();

        verify(checkoutView).transitionOut();
        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenUserSelectChangePaymentMethodFromPaymentResultAndExitOnIsTrueThenNotShowPaymentMethodSelection() {
        when(paymentRepository.getPayment()).thenReturn(mock(Payment.class));
        when(internalConfiguration.shouldExitOnPaymentMethodChange()).thenReturn(true);

        presenter.onChangePaymentMethod();

        verify(checkoutView).transitionOut();
        verify(checkoutView)
            .finishWithPaymentResult(Constants.RESULT_CHANGE_PAYMENT_METHOD, (Payment) paymentRepository.getPayment());
    }

    @Test
    public void whenUserSelectChangePaymentMethodWithoutPaymentAndExitOnIsTrueThenFinishWithPaymentResultChangePaymentMethodCode() {
        when(internalConfiguration.shouldExitOnPaymentMethodChange()).thenReturn(true);

        presenter.onChangePaymentMethod();

        verify(checkoutView).transitionOut();
        verify(checkoutView)
            .finishWithPaymentResult(Constants.RESULT_CHANGE_PAYMENT_METHOD);
    }

    @Test
    public void whenPaymentNeedsRecoveryFromReviewAndConfirmThenShowReviewAndConfirmAndRecoverPayment() {
        final RecoverPaymentPostPaymentAction action =
            new RecoverPaymentPostPaymentAction();
        action.execute(presenter);

        verify(checkoutView).showReviewAndConfirmAndRecoverPayment(false, action);
    }

    @Test
    public void whenPaymentNeedsRecoveryFromOneTapThenDoNothing() {
        final CheckoutPresenter presenter = getOneTapPresenter();

        final RecoverPaymentPostPaymentAction action =
            new RecoverPaymentPostPaymentAction();
        action.execute(presenter);

        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentHasInvalidEscThenStartPaymentRecoveryFlow() {
        final PaymentRecovery paymentRecovery = mock(PaymentRecovery.class);

        presenter.onRecoverPaymentEscInvalid(paymentRecovery);

        verify(checkoutView).startPaymentRecoveryFlow(paymentRecovery);
    }

    @Test
    public void whenOneTapPaymentHasInvalidEscThenStartPaymentRecoveryFlow() {
        final CheckoutPresenter presenter = getOneTapPresenter();
        final PaymentRecovery paymentRecovery = mock(PaymentRecovery.class);

        presenter.onRecoverPaymentEscInvalid(paymentRecovery);

        verify(checkoutView).startExpressPaymentRecoveryFlow(paymentRecovery);
    }

    @Test
    public void whenCardFlowResponseHasRecoverableTokenProcessThenCreatePaymentInOneTap() {
        final PaymentRecovery paymentRecovery = mock(PaymentRecovery.class);
        when(paymentRepository.hasPayment()).thenReturn(true);
        when(paymentRepository.createPaymentRecovery()).thenReturn(paymentRecovery);
        when(paymentRecovery.isTokenRecoverable()).thenReturn(true);

        presenter.onCardFlowResponse();

        verify(checkoutView).startPayment();
    }

    @Test
    public void whenCardFlowResponseHasNotRecoverableTokenProcessAndThereIsNoAvailableHooksThenShowReviewAndConfirmIfPaymentProcessorShouldNotSkipUserConfirmation() {
        final PaymentConfiguration paymentConfiguration = mock(PaymentConfiguration.class);
        final SplitPaymentProcessor paymentProcessor = mock(SplitPaymentProcessor.class);

        when(paymentRepository.hasPayment()).thenReturn(false);

        when(paymentSettingRepository.getPaymentConfiguration()).thenReturn(paymentConfiguration);
        when(paymentConfiguration.getPaymentProcessor()).thenReturn(paymentProcessor);
        when(paymentProcessor.shouldSkipUserConfirmation()).thenReturn(false);

        presenter.onCardFlowResponse();

        verify(paymentRepository).hasPayment();
        verify(checkoutView).showReviewAndConfirm(false);
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void whenCardFlowResponseHasNotRecoverableTokenProcessAndThereIsNoAvailableHooksThenShowVisualPaymentProcessorIfItShouldSkipUserConfirmation() {
        final PaymentConfiguration paymentConfiguration = mock(PaymentConfiguration.class);
        final SplitPaymentProcessor paymentProcessor = mock(SplitPaymentProcessor.class);

        when(paymentRepository.hasPayment()).thenReturn(false);

        when(paymentSettingRepository.getPaymentConfiguration()).thenReturn(paymentConfiguration);
        when(paymentConfiguration.getPaymentProcessor()).thenReturn(paymentProcessor);
        when(paymentProcessor.shouldSkipUserConfirmation()).thenReturn(true);

        presenter.onCardFlowResponse();

        verify(paymentRepository).hasPayment();
        verify(checkoutView).showPaymentProcessorWithAnimation();
        verify(checkoutView, never()).showReviewAndConfirm(anyBoolean());
        verifyNoMoreInteractions(checkoutView);
        verifyNoMoreInteractions(paymentRepository);
    }

    //Backs
    @Test
    public void whenCheckoutisInitializedAndUserPressesBackThenCancelCheckout() {
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));

        presenter.initialize();

        verify(checkoutView).showPaymentMethodSelection();

        presenter.onPaymentMethodSelectionCancel();

        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenPaymentMethodEditionIsRequestedAndUserPressesBackThenCancelCheckout() {
        final PaymentConfiguration paymentConfiguration = mock(PaymentConfiguration.class);
        final SplitPaymentProcessor paymentProcessor = mock(SplitPaymentProcessor.class);

        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));
        when(paymentSettingRepository.getPaymentConfiguration()).thenReturn(paymentConfiguration);
        when(paymentConfiguration.getPaymentProcessor()).thenReturn(paymentProcessor);
        when(paymentProcessor.shouldSkipUserConfirmation()).thenReturn(false);

        presenter.initialize();

        verify(checkoutView).showPaymentMethodSelection();

        presenter.onPaymentMethodSelected();
        verify(checkoutView).showReviewAndConfirm(anyBoolean());

        presenter.onChangePaymentMethodFromReviewAndConfirm();
        verify(checkoutView, atLeastOnce()).showPaymentMethodSelection();

        presenter.onPaymentMethodSelectionCancel();
        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenReviewAndConfirmCanceledAndOnlyOnePaymentMethodThenCancelCheckout() {
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getPaymentMethodSearchWithOnlyAccountMoneyMLA()));

        presenter.initialize();
        presenter.onReviewAndConfirmCancel();

        verify(checkoutView).cancelCheckout();
    }

    @Test
    public void whenReviewAndConfirmCanceledAndThereIsMoreThanOnePaymentMethodThenShowPaymentMethodSelection() {
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(stubPreferenceOneItem());
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(PaymentMethodSearchs.getCompletePaymentMethodSearchMLA()));

        presenter.initialize();
        presenter.onReviewAndConfirmCancel();

        verifyInitializeWithPreference();
        verify(checkoutView, atLeastOnce()).showPaymentMethodSelection();
        verify(checkoutView).transitionOut();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentSelectionErrorAndPaymentMethodChangeRequestedFromReviewAndConfirmOnBackExitCheckout() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        presenter.onChangePaymentMethodFromReviewAndConfirm();
        presenter.onPaymentMethodSelectionError(mercadoPagoError);

        verify(checkoutView).transitionOut();
        verify(checkoutView).showPaymentMethodSelection();
        verify(checkoutView).cancelCheckout(mercadoPagoError);

        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentSelectionErrorAndPaymentMethodChangeNotRequestedFromReviewAndConfirmThenCancelCheckout() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        presenter.onPaymentMethodSelectionError(mercadoPagoError);

        verify(checkoutView).cancelCheckout(mercadoPagoError);

        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenErrorShownAndInvalidIdentificationThenGoBackToPaymentMethodSelection() {
        final ApiException apiException = Payments.getInvalidIdentificationPayment();
        final MercadoPagoError mpException = new MercadoPagoError(apiException, "");

        presenter.onErrorCancel(mpException);

        verify(checkoutView).showPaymentMethodSelection();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenErrorShownAndValidIdentificationThenCancelCheckout() {
        final ApiException apiException = mock(ApiException.class);
        final MercadoPagoError mpException = new MercadoPagoError(apiException, "");

        presenter.onErrorCancel(mpException);

        verify(checkoutView).cancelCheckout();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenNoDefaultPaymentMethodsAndIsExpressCheckoutThenShowExpressCheckout() {
        final PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        when(search.hasExpressCheckoutMetadata()).thenReturn(true);

        presenter.noDefaultPaymentMethods(search);

        verifyShowOneTap();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCancelExpressCheckoutThenHideProgress() {
        final PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        final CheckoutPreference checkoutPreference = mock(CheckoutPreference.class);

        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getPaymentPreference()).thenReturn(null);
        when(search.hasExpressCheckoutMetadata()).thenReturn(true);

        presenter.startFlow(search);
        presenter.cancelCheckout();

        verifyShowOneTap();
        verify(checkoutView, atLeastOnce()).hideProgress();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCancelRegularCheckoutThenCancelCheckout() {
        presenter.cancelCheckout();

        verify(checkoutView).cancelCheckout();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentErrorIsPaymentProcessingThenShowPaymentResult() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);
        when(mercadoPagoError.isPaymentProcessing()).thenReturn(true);
        when(paymentRepository.getPaymentDataList()).thenReturn(mock(List.class));

        presenter.onPaymentError(mercadoPagoError);

        verify(checkoutView).hideProgress();
        verify(checkoutView).showPaymentResult(any(PaymentResult.class));
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenPaymentErrorIsInternalServerErrorThenShowError() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);
        when(mercadoPagoError.isInternalServerError()).thenReturn(true);

        presenter.onPaymentError(mercadoPagoError);

        verify(checkoutView).hideProgress();
        verify(checkoutView).showError(any(MercadoPagoError.class));
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenMercadoPagoErrorIsNotInternalServerErrorOrPaymentProcessingThenShowError() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        presenter.onPaymentError(mercadoPagoError);

        verify(checkoutView).hideProgress();
        verify(checkoutView).showError(any(MercadoPagoError.class));
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenTerminalErrorThenCancelCheckout() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);
        presenter.onTerminalError(mercadoPagoError);
        verify(checkoutView).cancelCheckout(mercadoPagoError);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenReviewAndConfirmErrorThenCancelCheckout() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);
        presenter.onReviewAndConfirmError(mercadoPagoError);
        verify(checkoutView).cancelCheckout(mercadoPagoError);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCustomReviewAndConfirmResponseThenCancelCheckout() {
        presenter.onCustomReviewAndConfirmResponse(CUSTOM_RESULT_CODE);
        verify(checkoutView).cancelCheckout(eq(CUSTOM_RESULT_CODE), anyBoolean());
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCustomPaymentResultResponseHasPaymentThenFinishWithPaymentResult() {
        final Payment payment = mock(Payment.class);
        when(paymentRepository.hasPayment()).thenReturn(true);
        when(paymentRepository.getPayment()).thenReturn(payment);

        presenter.onCustomPaymentResultResponse(CUSTOM_RESULT_CODE);

        verify(checkoutView).finishWithPaymentResult(CUSTOM_RESULT_CODE, payment);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCustomPaymentResultResponseHasNotPaymentThenFinishWithPaymentResult() {
        when(paymentRepository.hasPayment()).thenReturn(false);

        presenter.onCustomPaymentResultResponse(CUSTOM_RESULT_CODE);

        verify(checkoutView).finishWithPaymentResult(CUSTOM_RESULT_CODE);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCustomPaymentResultResponseHasBusinessPaymentThenFinishWithPaymentResult() {
        final BusinessPayment payment = mock(BusinessPayment.class);
        when(paymentRepository.hasPayment()).thenReturn(true);
        when(paymentRepository.getPayment()).thenReturn(payment);

        presenter.onCustomPaymentResultResponse(CUSTOM_RESULT_CODE);

        verify(checkoutView).finishWithPaymentResult(CUSTOM_RESULT_CODE);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenFailureRecoveryNotSetThenShowFailureRecoveryError() {
        presenter.recoverFromFailure();
        verify(checkoutView).showFailureRecoveryError();
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenExitWithCodeThenExitCheckout() {
        presenter.exitWithCode(CUSTOM_RESULT_CODE);
        verify(checkoutView).exitCheckout(CUSTOM_RESULT_CODE);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenCvvRequiredThenShowSavedCardFlow() {
        final Card card = mock(Card.class);
        presenter.onCvvRequired(card);
        verify(checkoutView).showSavedCardFlow(card);
        verifyNoMoreInteractions(checkoutView);
    }

    @Test
    public void whenVisualPaymentConfiguredThenShowPaymentProcessor() {
        presenter.onVisualPayment();
        verify(checkoutView).showPaymentProcessor();
        verifyNoMoreInteractions(checkoutView);
    }

// --------- Helper methods ----------- //

    @NonNull
    private CheckoutPresenter getBasePresenter(
        final Checkout.View view, final CheckoutStateModel checkoutStateModel) {

        presenter = new CheckoutPresenter(checkoutStateModel, paymentSettingRepository,
            userSelectionRepository,
            groupsRepository,
            pluginRepository,
            paymentRepository,
            checkoutPreferenceRepository,
            internalConfiguration,
            businessModelMapper);

        presenter.attachView(view);
        return presenter;
    }

    @NonNull
    private CheckoutPresenter getPresenter() {
        return getBasePresenter(checkoutView, new CheckoutStateModel());
    }

    @NonNull
    private CheckoutPresenter getOneTapPresenter() {
        final CheckoutStateModel stateModel = new CheckoutStateModel();
        stateModel.isExpressCheckout = true;
        return getBasePresenter(checkoutView, stateModel);
    }

    private void verifyInitializeWithPreference() {
        verify(checkoutView).showProgress();
        verify(checkoutView).fetchFonts();
        verify(checkoutView, atLeastOnce()).showPaymentMethodSelection();
    }

    @NonNull
    private PaymentMethodSearch mockPaymentMethodSearchForDriver(boolean isValidCard) {
        PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        PaymentMethod paymentMethod = mock(PaymentMethod.class);
        when(paymentMethod.getPaymentTypeId()).thenReturn("debit_card");
        final ArrayList settingsList = mock(ArrayList.class);
        final Setting setting = mock(Setting.class);
        when(setting.getSecurityCode()).thenReturn(null);
        when(settingsList.get(any(int.class))).thenReturn(setting);
        when(paymentMethod.getSettings()).thenReturn(settingsList);
        when(search.getPaymentMethodById(any(String.class))).thenReturn(paymentMethod);
        if (isValidCard) {
            when(search.getCardById(any(String.class))).thenReturn(new Card());
        } else {
            when(search.getCardById(any(String.class))).thenReturn(null);
        }
        final CheckoutPreference checkoutPreference = mock(CheckoutPreference.class);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        final PaymentPreference paymentPreference = mock(PaymentPreference.class);
        when(checkoutPreference.getPaymentPreference()).thenReturn(paymentPreference);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultCardId()).thenReturn(
            DEFAULT_CARD_ID);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultPaymentMethodId())
            .thenReturn(DEBIT_CARD_DEBCABAL);
        return search;
    }

    @NonNull
    private PaymentMethodSearch mockPaymentMethodSearchForNewCardDriver() {
        final PaymentMethodSearch search = mock(PaymentMethodSearch.class);
        final CheckoutPreference checkoutPreference = mock(CheckoutPreference.class);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(checkoutPreference.getPaymentPreference()).thenReturn(mock(PaymentPreference.class));
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultCardId())
            .thenReturn(null);
        when(paymentSettingRepository.getCheckoutPreference().getPaymentPreference().getDefaultPaymentTypeId())
            .thenReturn("debit_card");
        return search;
    }

    private void verifyShowOneTap() {
        verify(checkoutView, atLeastOnce()).hideProgress();
        verify(checkoutView).showOneTap();
    }
}