package com.mercadopago.android.px.internal.callbacks;

import com.mercadopago.android.px.internal.datasource.DisabledPaymentMethodService;
import com.mercadopago.android.px.internal.repository.EscPaymentManager;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.IPaymentDescriptorHandler;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceHandlerWrapperTest {

    @Mock private PaymentServiceHandler wrapped;
    @Mock private PaymentRepository paymentRepository;
    @Mock private DisabledPaymentMethodService disabledPaymentMethodService;
    @Mock private PaymentRecovery paymentRecovery;
    @Mock private InstructionsRepository instructionsRepository;
    @Mock private EscPaymentManager escPaymentManager;

    private PaymentServiceHandlerWrapper paymentServiceHandlerWrapper;

    @Before
    public void setUp() {
        paymentServiceHandlerWrapper =
            new PaymentServiceHandlerWrapper(paymentRepository, disabledPaymentMethodService, escPaymentManager,
                instructionsRepository);
        paymentServiceHandlerWrapper.setHandler(wrapped);
        when(paymentRepository.createRecoveryForInvalidESC()).thenReturn(paymentRecovery);
        when(paymentRepository.getPaymentDataList()).thenReturn(Collections.singletonList(mock(PaymentData.class)));
    }

    private void noMoreInteractions() {
        verifyNoMoreInteractions(wrapped);
        verifyNoMoreInteractions(escPaymentManager);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(instructionsRepository);
    }

    @Test
    public void whenVisualRequired() {
        paymentServiceHandlerWrapper.onVisualPayment();
        verify(wrapped).onVisualPayment();
        noMoreInteractions();
    }

    @Test
    public void whenOnCvvRequiredVerifyOnCvvRequired() {
        final Card mock = mock(Card.class);
        paymentServiceHandlerWrapper.onCvvRequired(mock);
        verify(wrapped).onCvvRequired(mock);
        noMoreInteractions();
    }

    @Test
    public void whenRecoverPaymentEscInvalidVerifyRecoverPaymentEscInvalid() {
        paymentServiceHandlerWrapper.onRecoverPaymentEscInvalid(paymentRecovery);
        verify(wrapped).onRecoverPaymentEscInvalid(paymentRecovery);
        noMoreInteractions();
    }

    @Test
    public void whenPaymentFinishedWithPaymentVerifyEscManaged() {
        final PaymentResult paymentResult = mock(PaymentResult.class);
        final Payment payment = mock(Payment.class);
        when(paymentResult.isOffPayment()).thenReturn(false);
        when(paymentRepository.createPaymentResult(payment)).thenReturn(paymentResult);

        final IPaymentDescriptorHandler handler = paymentServiceHandlerWrapper.getHandler();
        handler.visit(payment);

        verify(escPaymentManager)
            .manageEscForPayment(paymentRepository.getPaymentDataList(), payment.getPaymentStatus(),
                payment.getPaymentStatusDetail());

        verify(paymentRepository).createPaymentResult(payment);
        verify(paymentRepository).storePayment(payment);
        verify(paymentRepository, times(2)).getPaymentDataList();
        verify(wrapped).onPaymentFinished(payment);
        noMoreInteractions();
    }

    @Test
    public void whenPaymentFinishedWithBusinessVerifyEscManaged() {
        final BusinessPayment payment = mock(BusinessPayment.class);
        final PaymentResult paymentResult = mock(PaymentResult.class);
        when(paymentRepository.createPaymentResult(payment)).thenReturn(paymentResult);

        final IPaymentDescriptorHandler handler = paymentServiceHandlerWrapper.getHandler();
        handler.visit(payment);

        verify(escPaymentManager)
            .manageEscForPayment(paymentRepository.getPaymentDataList(), payment.getPaymentStatus(),
                payment.getPaymentStatusDetail());

        verify(paymentRepository).storePayment(payment);

        verify(paymentRepository).createPaymentResult(payment);

        verify(paymentRepository, times(2)).getPaymentDataList();
        verify(wrapped).onPaymentFinished(payment);
        noMoreInteractions();
    }

    @Test
    public void whenPaymentFinishedWithGenericPaymentVerifyEscManaged() {
        final PaymentResult paymentResult = mock(PaymentResult.class);
        final IPaymentDescriptor payment = mock(IPaymentDescriptor.class);
        when(paymentResult.isOffPayment()).thenReturn(false);
        when(paymentRepository.createPaymentResult(payment)).thenReturn(paymentResult);

        final IPaymentDescriptorHandler handler = paymentServiceHandlerWrapper.getHandler();
        handler.visit(payment);

        verify(escPaymentManager)
            .manageEscForPayment(paymentRepository.getPaymentDataList(), payment.getPaymentStatus(),
                payment.getPaymentStatusDetail());

        verify(paymentRepository).storePayment(payment);
        verify(paymentRepository).createPaymentResult(payment);
        verify(paymentRepository, times(2)).getPaymentDataList();
        verify(wrapped).onPaymentFinished(payment);
        noMoreInteractions();
    }

    @Test
    public void whenPaymentFinishedWithErrorVerifyEscManaged() {
        final MercadoPagoError error = mock(MercadoPagoError.class);
        paymentServiceHandlerWrapper.onPaymentError(error);

        verify(escPaymentManager).manageEscForError(error, paymentRepository.getPaymentDataList());

        verify(paymentRepository, times(2)).getPaymentDataList();
        verify(wrapped).onPaymentError(error);
        noMoreInteractions();
    }

    @Test
    public void whenPaymentFinishedWithGenericPaymentAndEscIsInvalidatedVerifyRecoveryCalled() {
        final IPaymentDescriptor payment = mock(IPaymentDescriptor.class);

        when(escPaymentManager.manageEscForPayment(paymentRepository.getPaymentDataList(), payment.getPaymentStatus(),
            payment.getPaymentStatusDetail())).thenReturn(true);

        final IPaymentDescriptorHandler handler = paymentServiceHandlerWrapper.getHandler();
        handler.visit(payment);

        verify(paymentRepository).createRecoveryForInvalidESC();

        verify(escPaymentManager)
            .manageEscForPayment(paymentRepository.getPaymentDataList(), payment.getPaymentStatus(),
                payment.getPaymentStatusDetail());

        verify(paymentRepository, times(3)).getPaymentDataList();
        verify(wrapped).onRecoverPaymentEscInvalid(paymentRecovery);

        noMoreInteractions();
    }

    @Test
    public void whenPaymentFinishedWithPaymentAndEscIsInvalidatedVerifyRecoveryCalled() {
        final Payment payment = mock(Payment.class);

        when(escPaymentManager.manageEscForPayment(paymentRepository.getPaymentDataList(), payment.getPaymentStatus(),
            payment.getPaymentStatusDetail())).thenReturn(true);

        final IPaymentDescriptorHandler handler = paymentServiceHandlerWrapper.getHandler();
        handler.visit(payment);

        verify(escPaymentManager)
            .manageEscForPayment(paymentRepository.getPaymentDataList(), payment.getPaymentStatus(),
                payment.getPaymentStatusDetail());

        verify(paymentRepository).createRecoveryForInvalidESC();
        verify(paymentRepository, times(3)).getPaymentDataList();
        verify(wrapped).onRecoverPaymentEscInvalid(paymentRecovery);

        noMoreInteractions();
    }

    @Test
    public void whenPaymentFinishedWithErrorAndEscIsInvalidatedVerifyRecoveryCalled() {
        final MercadoPagoError error = mock(MercadoPagoError.class);

        when(escPaymentManager.manageEscForError(error, paymentRepository.getPaymentDataList())).thenReturn(true);

        paymentServiceHandlerWrapper.onPaymentError(error);

        verify(escPaymentManager).manageEscForError(error, paymentRepository.getPaymentDataList());
        verify(paymentRepository).createRecoveryForInvalidESC();
        verify(paymentRepository, times(3)).getPaymentDataList();
        verify(wrapped).onRecoverPaymentEscInvalid(paymentRecovery);

        noMoreInteractions();
    }
}