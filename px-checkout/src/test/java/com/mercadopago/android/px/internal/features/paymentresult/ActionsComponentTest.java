package com.mercadopago.android.px.internal.features.paymentresult;

import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsAction;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsActions;
import com.mercadopago.android.px.internal.features.paymentresult.props.InstructionsActionsProps;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.mocks.Instructions;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.InstructionAction;
import java.util.List;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by vaserber on 23/11/2017.
 */

public class ActionsComponentTest {

    private ActionDispatcher dispatcher;

    @Before
    public void setup() {
        dispatcher = mock(ActionDispatcher.class);
    }

    @Test
    public void testCreateActionComponentsList() {
        final Instruction instruction = Instructions.getBanamexBankTransferInstruction();
        final InstructionsActionsProps props = new InstructionsActionsProps.Builder()
            .setInstructionsActions(instruction.getActions())
            .build();
        final InstructionsActions component = new InstructionsActions(props, dispatcher);

        Assert.assertNotNull(instruction.getActions());
        Assert.assertFalse(instruction.getActions().isEmpty());

        final List<InstructionsAction> actionComponentsList = component.getActionComponents();

        Assert.assertNotNull(actionComponentsList);
        for (InstructionsAction action : actionComponentsList) {
            InstructionAction actionInfo = action.props.instructionAction;
            Assert.assertEquals(actionInfo.getTag(), InstructionAction.Tags.LINK);
        }
    }
}