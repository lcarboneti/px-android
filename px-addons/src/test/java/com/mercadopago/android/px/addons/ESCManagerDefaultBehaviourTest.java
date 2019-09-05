package com.mercadopago.android.px.addons;

import com.mercadopago.android.px.addons.internal.ESCManagerBehaviourProvider;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ESCManagerDefaultBehaviourTest {

    private static final String DUMMY_SESSION_ID = "DUMMY_SESSION_ID";

    @Test
    public void testDefaultBehaviourEscEnabled_returnsFalse() {
        assertFalse(ESCManagerBehaviourProvider.get(DUMMY_SESSION_ID, true).isESCEnabled());
    }

    @Test
    public void testDefaultBehaviourEscDisabled_returnsFalse() {
        assertFalse(ESCManagerBehaviourProvider.get(DUMMY_SESSION_ID, false).isESCEnabled());
    }
}