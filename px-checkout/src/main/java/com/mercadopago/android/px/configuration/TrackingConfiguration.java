package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import java.util.UUID;

/**
 * Allows you to customize certain information in your events.
 */
@SuppressWarnings("unused")
public final class TrackingConfiguration {

    @NonNull private final String sessionId;

    /* default */ TrackingConfiguration(final Builder builder) {
        sessionId = builder.sessionId;
    }

    @NonNull
    public String getSessionId() {
        return sessionId;
    }

    public static final class Builder {
        // By default create a new session id.
        private String sessionId = UUID.randomUUID().toString();

        /**
         * Unique identifier for your checkout experience session.
         *
         * @param sessionId unique identifier for the session to be started.
         * @return builder to keep operating.
         */
        public Builder sessionId(@NonNull final String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * return a new configuration instance.
         *
         * @return tracking configuration instance.
         */
        public TrackingConfiguration build() {
            return new TrackingConfiguration(this);
        }
    }
}
