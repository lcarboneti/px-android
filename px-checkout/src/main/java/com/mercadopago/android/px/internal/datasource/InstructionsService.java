package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.adapters.MPCallWrapper;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.InstructionsClient;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.Instructions;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mercadopago.android.px.services.BuildConfig.API_ENVIRONMENT;
import static com.mercadopago.android.px.services.BuildConfig.API_VERSION;

public class InstructionsService implements InstructionsRepository {

    /* default */ @NonNull final PaymentSettingRepository paymentSettingRepository;
    /* default */ @NonNull final InstructionsClient instructionsClient;
    /* default */ @NonNull final String locale;
    /* default */ @NonNull final Map<Long, List<Instruction>> instructionsMap = new HashMap();

    public InstructionsService(@NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final InstructionsClient instructionsClient,
        @NonNull final String locale) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.instructionsClient = instructionsClient;
        this.locale = locale;
    }

    @Override
    public MPCall<List<Instruction>> getInstructions(@NonNull final PaymentResult paymentResult) {
        // Off payment method returns always 1 payment data.
        final String paymentTypeId = paymentResult.getPaymentData()
            .getPaymentMethod()
            .getPaymentTypeId();

        final Long paymentId = paymentResult.getPaymentId();
        if (instructionsMap.containsKey(paymentId)) {
            return new MPCall<List<Instruction>>() {
                @Override
                public void enqueue(final Callback<List<Instruction>> callback) {
                    callback.success(instructionsMap.get(paymentId));
                }

                @Override
                public void execute(final Callback<List<Instruction>> callback) {
                    callback.success(instructionsMap.get(paymentId));
                }
            };
        }
        return new MPCall<List<Instruction>>() {
            @Override
            public void enqueue(final Callback<List<Instruction>> callback) {
                getInstructionsCall(paymentTypeId, paymentId)
                    .enqueue(getCallback(callback, paymentId));
            }

            @Override
            public void execute(final Callback<List<Instruction>> callback) {
                getInstructionsCall(paymentTypeId, paymentId)
                    .execute(getCallback(callback, paymentId));
            }
        };
    }

    @NonNull
    private Callback<Instructions> getCallback(final Callback<List<Instruction>> callback, final Long id) {
        return new Callback<Instructions>() {
            @Override
            public void success(final Instructions instructions) {
                final List<Instruction> instructionList = resolveInstruction(instructions);
                instructionsMap.put(id, instructionList);
                callback.success(instructionList);
            }

            @Override
            public void failure(final ApiException apiException) {
                callback.failure(apiException);
            }
        };
    }

    /* default */
    @NonNull
    List<Instruction> resolveInstruction(final Instructions instructions) {
        return instructions == null ? new ArrayList<>()
            : (instructions.getInstructions() == null ? new ArrayList<>()
                : instructions.getInstructions());
    }

    /* default */ MPCall<Instructions> getInstructionsCall(final String paymentTypeId, final Long id) {
        return new MPCallWrapper<Instructions>() {
            @Override
            protected MPCall<Instructions> method() {
                return instructionsClient.getInstructions(API_ENVIRONMENT,
                    API_VERSION,
                    locale, id,
                    paymentSettingRepository.getPublicKey(),
                    paymentSettingRepository.getPrivateKey(),
                    paymentTypeId);
            }
        }.wrap();
    }
}
