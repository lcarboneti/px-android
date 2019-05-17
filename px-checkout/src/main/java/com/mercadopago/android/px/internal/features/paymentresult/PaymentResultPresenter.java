package com.mercadopago.android.px.internal.features.paymentresult;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.constants.ProcessingModes;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.view.ActionsListener;
import com.mercadopago.android.px.internal.view.CopyAction;
import com.mercadopago.android.px.internal.view.LinkAction;
import com.mercadopago.android.px.internal.view.NextAction;
import com.mercadopago.android.px.internal.view.RecoverPaymentAction;
import com.mercadopago.android.px.internal.view.ResultCodeAction;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;
import com.mercadopago.android.px.tracking.internal.events.AbortEvent;
import com.mercadopago.android.px.tracking.internal.events.ChangePaymentMethodEvent;
import com.mercadopago.android.px.tracking.internal.events.ContinueEvent;
import com.mercadopago.android.px.tracking.internal.views.ResultViewTrack;
import java.util.List;

/* default */ class PaymentResultPresenter extends BasePresenter<PaymentResultContract.PaymentResultView>
    implements ActionsListener, PaymentResultContract.Actions {

    private final PaymentResult paymentResult;
    private final PaymentSettingRepository paymentSettings;
    private final InstructionsRepository instructionsRepository;
    @NonNull private final ResultViewTrack resultViewTrack;

    private FailureRecovery failureRecovery;

    /* default */ PaymentResultPresenter(@NonNull final PaymentSettingRepository paymentSettings,
        @NonNull final InstructionsRepository instructionsRepository,
        @NonNull final PaymentResult paymentResult) {
        this.paymentSettings = paymentSettings;
        this.instructionsRepository = instructionsRepository;
        this.paymentResult = paymentResult;

        resultViewTrack =
            new ResultViewTrack(ResultViewTrack.Style.GENERIC, paymentResult, paymentSettings.getCheckoutPreference());
    }

    @Override
    public void attachView(final PaymentResultContract.PaymentResultView view) {
        super.attachView(view);

        getView().setPropPaymentResult(paymentSettings.getCheckoutPreference().getSite().getCurrencyId(), paymentResult,
            paymentResult.isOffPayment());

        getView().notifyPropsChanged();

        if (paymentResult.isOffPayment()) {
            getInstructionsAsync();
        }
    }

    @Override
    public void freshStart() {
        setCurrentViewTracker(resultViewTrack);
    }

    @Override
    public void onAbort() {
        new AbortEvent(resultViewTrack).track();
    }

    private void getInstructionsAsync() {
        instructionsRepository.getInstructions(paymentResult).enqueue(new Callback<List<Instruction>>() {
            @Override
            public void success(final List<Instruction> instructions) {
                if (isViewAttached()) {
                    if (instructions.isEmpty()) {
                        getView().showInstructionsError();
                    } else {
                        resolveInstructions(instructions);
                    }
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                if (isViewAttached()) {
                    getView().showApiExceptionError(apiException, ApiUtil.RequestOrigin.GET_INSTRUCTIONS);
                    setFailureRecovery(() -> getInstructionsAsync());
                }
            }
        });
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    /* default */ void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    /* default */ void resolveInstructions(final List<Instruction> instructionsList) {
        final Instruction instruction = getInstruction(instructionsList);
        if (instruction == null) {
            getView().showInstructionsError();
        } else {
            getView().setPropInstruction(instruction, ProcessingModes.AGGREGATOR, false);
            getView().notifyPropsChanged();
        }
    }

    private Instruction getInstruction(final List<Instruction> instructions) {
        final Instruction instruction;
        if (instructions.size() == 1) {
            instruction = instructions.get(0);
        } else {
            instruction = getInstructionForType(instructions,
                paymentResult.getPaymentData().getPaymentMethod().getPaymentTypeId());
        }
        return instruction;
    }

    private Instruction getInstructionForType(final Iterable<Instruction> instructions, final String paymentTypeId) {
        Instruction instructionForType = null;
        for (final Instruction instruction : instructions) {
            if (instruction.getType().equals(paymentTypeId)) {
                instructionForType = instruction;
                break;
            }
        }
        return instructionForType;
    }

    @Override
    public void onAction(@NonNull final Action action) {
        if (!isViewAttached()) {
            return;
        }

        if (action instanceof NextAction) {
            new ContinueEvent(resultViewTrack).track();
            getView().finishWithResult(MercadoPagoCheckout.PAYMENT_RESULT_CODE);
        } else if (action instanceof ResultCodeAction) {
            getView().finishWithResult(((ResultCodeAction) action).resultCode);
        } else if (action instanceof ChangePaymentMethodAction) {
            ChangePaymentMethodEvent.with(resultViewTrack).track();
            getView().changePaymentMethod();
        } else if (action instanceof RecoverPaymentAction) {
            getView().recoverPayment();
        } else if (action instanceof LinkAction) {
            getView().openLink(((LinkAction) action).url);
        } else if (action instanceof CopyAction) {
            getView().copyToClipboard(((CopyAction) action).content);
        }
    }
}
