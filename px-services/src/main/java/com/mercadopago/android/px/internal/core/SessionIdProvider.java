package com.mercadopago.android.px.internal.core;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public final class SessionIdProvider {

    private static final String PREF_SESSION_ID = "PREF_SESSION_ID";

    @NonNull private final SharedPreferences sharedPreferences;

    /* default */ SessionIdProvider(@NonNull final SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    /* default */ SessionIdProvider(@NonNull final SharedPreferences sharedPreferences,
        @NonNull final String newSessionId) {
        this.sharedPreferences = sharedPreferences;
        this.sharedPreferences.edit().putString(PREF_SESSION_ID, newSessionId).apply();
    }

    @NonNull
    public String getSessionId() {
        return sharedPreferences.getString(PREF_SESSION_ID, "no-value");
    }
}
