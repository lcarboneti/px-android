package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration;
import com.mercadopago.android.px.internal.datasource.IESCManager;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.mappers.BusinessModelMapper;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReviewAndConfirmPresenterTest {

    @Mock
    private ReviewAndConfirm.View view;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BusinessModelMapper businessModelMapper;

    private ReviewAndConfirmPresenter reviewAndConfirmPresenter;

    @Mock private IESCManager IESCManager;

    @Mock private PaymentSettingRepository paymentSettingRepository;

    @Mock private UserSelectionRepository userSelectionRepository;

    @Mock private PaymentMethod paymentMethod;

    @Mock private AdvancedConfiguration advancedConfiguration;
    @Mock private CheckoutPreference checkoutPreference;
    @Mock private DynamicDialogConfiguration dynamicDialogConfiguration;
    @Mock private DiscountRepository discountRepository;

    @Before
    public void setUp() {

        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(checkoutPreference);
        when(paymentSettingRepository.getAdvancedConfiguration()).thenReturn(advancedConfiguration);
        when(advancedConfiguration.getDynamicDialogConfiguration()).thenReturn(dynamicDialogConfiguration);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        reviewAndConfirmPresenter =
            new ReviewAndConfirmPresenter(paymentRepository, businessModelMapper,
                discountRepository, paymentSettingRepository,
                userSelectionRepository,
                IESCManager);

        verifyAttachView();
    }

    private void verifyAttachView() {
        reviewAndConfirmPresenter.attachView(view);
        verify(paymentRepository, atLeastOnce()).attach(reviewAndConfirmPresenter);
    }

    @Test
    public void whenIsPaymentAndAnimationIsFinishedThenShowResult() {
        final IPaymentDescriptor payment = mock(Payment.class);
        whenIPaymentAndAnimationIsFinishedThenShowResult(payment);
    }

    @Test
    public void whenIsGenericPaymentAndAnimationIsFinishedThenShowResult() {
        final IPaymentDescriptor payment = mock(IPaymentDescriptor.class);
        whenIPaymentAndAnimationIsFinishedThenShowResult(payment);
    }

    @Test
    public void whenIsBusinessPaymentAndAnimationIsFinishedThenMapItAndShowResult() {
        final BusinessPayment payment = mock(BusinessPayment.class);
        final BusinessPaymentModel businessPaymentModel = mock(BusinessPaymentModel.class);

        when(paymentRepository.getPayment()).thenReturn(payment);
        when(businessModelMapper.map(payment)).thenReturn(businessPaymentModel);

        reviewAndConfirmPresenter.hasFinishPaymentAnimation();

        verify(paymentRepository).getPayment();
        verify(view).showResult(businessPaymentModel);
        verify(businessModelMapper).map(payment);
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void whenPaymentErrorIsPaymentProcessingThenShowResult() {
        when(paymentRepository.getPaymentDataList()).thenReturn(Collections.singletonList(mock(PaymentData.class)));
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        when(mercadoPagoError.isPaymentProcessing()).thenReturn(true);

        verifyOnPaymentError(mercadoPagoError);

        verify(view).showResult(any(PaymentResult.class));
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPaymentErrorIsInternalServerErrorThenShowErrorSnackBar() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        when(mercadoPagoError.isInternalServerError()).thenReturn(true);

        verifyOnPaymentError(mercadoPagoError);

        verify(view).showErrorSnackBar(mercadoPagoError);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPaymentErrorIsNoConnectivityErrorThenShowErrorSnackBar() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        when(mercadoPagoError.isNoConnectivityError()).thenReturn(true);

        verifyOnPaymentError(mercadoPagoError);

        verify(view).showErrorSnackBar(mercadoPagoError);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPaymentErrorThenShowErrorScreen() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        verifyOnPaymentError(mercadoPagoError);

        verify(view).showErrorScreen(mercadoPagoError);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenRecoverFromFailureThenPay() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        when(paymentRepository.isExplodingAnimationCompatible()).thenReturn(true);

        verifyShowErrorScreen(mercadoPagoError);

        reviewAndConfirmPresenter.recoverFromFailure();

        verifyPaymentExplodingCompatible();

        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void whenViewIsResumedThenAttachItAndResolveDynamicDialogWithoutCreator() {
        when(paymentRepository.getPaymentDataList()).thenReturn(Collections.singletonList(mock(PaymentData.class)));
        reviewAndConfirmPresenter.onViewResumed(view);

        verifyAttachView();
        verify(paymentRepository).getPaymentDataList();
        verify(dynamicDialogConfiguration, atLeastOnce())
            .hasCreatorFor(DynamicDialogConfiguration.DialogLocation.ENTER_REVIEW_AND_CONFIRM);

        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whendViewDetachedThenPaymentRepositoryViewDetachmentIsPerformed() {
        reviewAndConfirmPresenter.detachView();

        verify(paymentRepository).detach(reviewAndConfirmPresenter);
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void whenPaymentConfirmationThenTrackItAndPay() {
        when(paymentRepository.isExplodingAnimationCompatible()).thenReturn(true);
        reviewAndConfirmPresenter.onPaymentConfirm();
        verifyPaymentExplodingCompatible();
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void whenCardFlowResponseThenPay() {
        when(paymentRepository.isExplodingAnimationCompatible()).thenReturn(true);

        reviewAndConfirmPresenter.onCardFlowResponse();

        verify(view).cancelLoadingButton();
        verify(view).showConfirmButton();
        verifyPaymentExplodingCompatible();
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void whenErrorThenCancelCheckoutAndInformError() {
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);

        reviewAndConfirmPresenter.onError(mercadoPagoError);

        verify(view).cancelLoadingButton();
        verify(view).showConfirmButton();
        verify(view).cancelCheckoutAndInformError(mercadoPagoError);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenCvvRequiredThenShowCardCvvRequired() {
        final Card card = mock(Card.class);

        reviewAndConfirmPresenter.onCvvRequired(card);

        verify(view).cancelLoadingButton();
        verify(view).showConfirmButton();
        verify(view).showCardCVVRequired(card);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenVisualPaymentThenShowPaymentProcessor() {
        reviewAndConfirmPresenter.onVisualPayment();

        verify(view).cancelLoadingButton();
        verify(view).showConfirmButton();
        verify(view).showPaymentProcessor();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenRecoverPaymentWithEscInvalidThenStartPaymentRecoveryFlow() {
        final PaymentRecovery paymentRecovery = mock(PaymentRecovery.class);

        reviewAndConfirmPresenter.onRecoverPaymentEscInvalid(paymentRecovery);

        verify(view).cancelLoadingButton();
        verify(view).showConfirmButton();
        verify(view).startPaymentRecoveryFlow(paymentRecovery);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPaymentFinishedThenFinishLoadingWithExplodeDecorator() {
        final Payment payment = mock(Payment.class);
        when(payment.getPaymentStatus()).thenReturn(Payment.StatusCodes.STATUS_APPROVED);
        when(payment.getPaymentStatusDetail()).thenReturn(Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT);

        reviewAndConfirmPresenter.onPaymentFinished(payment);

        verifyPaymentFinished();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenGenericPaymentFinishedThenFinishLoadingWithExplodeDecorator() {
        final IPaymentDescriptor genericPayment = mock(IPaymentDescriptor.class);
        when(genericPayment.getPaymentStatus()).thenReturn(Payment.StatusCodes.STATUS_APPROVED);
        when(genericPayment.getPaymentStatusDetail())
            .thenReturn(Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT);

        reviewAndConfirmPresenter.onPaymentFinished(genericPayment);

        verifyPaymentFinished();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenBusinessPaymentFinishedThenFinishLoadingWithExplodeDecorator() {
        final BusinessPayment businessPayment = mock(BusinessPayment.class);
        when(businessPayment.getPaymentStatus()).thenReturn(Payment.StatusCodes.STATUS_APPROVED);
        when(businessPayment.getPaymentStatusDetail())
            .thenReturn(Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT);

        reviewAndConfirmPresenter.onPaymentFinished(businessPayment);

        verifyPaymentFinished();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPayerInformationResponseThenReloadBody() {
        reviewAndConfirmPresenter.onPayerInformationResponse();

        verify(view).reloadBody();
        verifyNoMoreInteractions(view);
    }

    private void verifyPaymentFinished() {
        verify(view).hideConfirmButton();
        verify(view).finishLoading(any(ExplodeDecorator.class));
    }

    private void verifyPaymentExplodingCompatible() {
        verify(paymentRepository).isExplodingAnimationCompatible();
        verify(paymentRepository).getPaymentTimeout();
        verify(view).startLoadingButton(any(Integer.class));
        verify(view).hideConfirmButton();
        verify(paymentRepository, atLeastOnce()).attach(reviewAndConfirmPresenter);
        verify(paymentRepository).startPayment();
    }

    private void verifyShowErrorScreen(@NonNull final MercadoPagoError mercadoPagoError) {
        verifyOnPaymentError(mercadoPagoError);
        verify(view).showErrorScreen(mercadoPagoError);
    }

    private void verifyOnPaymentError(@NonNull final MercadoPagoError mercadoPagoError) {
        reviewAndConfirmPresenter.onPaymentError(mercadoPagoError);
        verify(view).cancelLoadingButton();
        verify(view).showConfirmButton();
    }

    private void whenIPaymentAndAnimationIsFinishedThenShowResult(final IPaymentDescriptor payment) {
        final PaymentResult paymentResult = mock(PaymentResult.class);

        when(paymentRepository.getPayment()).thenReturn(payment);
        when(paymentRepository.createPaymentResult(payment)).thenReturn(paymentResult);

        reviewAndConfirmPresenter.hasFinishPaymentAnimation();

        verify(paymentRepository).getPayment();
        verify(view).showResult(paymentResult);
        verify(paymentRepository).createPaymentResult(payment);
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(paymentRepository);
    }
}
