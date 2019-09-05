package com.mercadopago.android.px.addons.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import java.util.Collections;
import java.util.Set;

/* default */ final class ESCManagerDefaultBehaviour implements ESCManagerBehaviour {

    @Override
    public void setSessionId(@NonNull final String sessionId) {
        // do nothing
    }

    @Nullable
    @Override
    public String getESC(@Nullable final String cardId, @NonNull final String firstDigits,
        @NonNull final String lastDigits) {
        return null;
    }

    @Override
    public boolean saveESCWith(@NonNull final String cardId, @NonNull final String esc) {
        return false;
    }

    @Override
    public boolean saveESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits,
        @NonNull final String esc) {
        return false;
    }

    @Override
    public void deleteESCWith(@NonNull final String cardId) {
        // do nothing
    }

    @Override
    public void deleteESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits) {
        // do nothing
    }

    @Override
    public Set<String> getESCCardIds() {
        return Collections.emptySet();
    }

    @Override
    public boolean isESCEnabled() {
        return false;
    }
}