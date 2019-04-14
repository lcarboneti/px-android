package com.mercadopago.android.px.model.internal;

public enum Feature {
    EXPRESS_PAYMENT("EXPRESS_PAYMENT"),
    SPLIT_PAYMENT("SPLIT_PAYMENT"),
    ESC("ESC");

    public final String name;

    /* default */Feature(final String name) {
        this.name = name;
    }
}
