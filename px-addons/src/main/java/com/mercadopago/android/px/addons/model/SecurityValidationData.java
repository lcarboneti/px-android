package com.mercadopago.android.px.addons.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class SecurityValidationData {

    @Nullable private final String flowId;

    /* default */ SecurityValidationData(@NonNull final Builder builder) {
        flowId = builder.flowId;
    }

    @Nullable
    public String getFlowId() {
        return flowId;
    }

    public static final class Builder {

        @Nullable /* default */ String flowId;

        public Builder setFlowId(@Nullable final String flowId) {
            this.flowId = flowId;
            return this;
        }

        public SecurityValidationData build() {
            return new SecurityValidationData(this);
        }
    }
}