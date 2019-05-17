package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.Set;

public interface IESCManager {

    String getESC(@Nullable final String cardId, @NonNull final String firstDigits, @NonNull final String lastDigits);

    boolean saveESCWith(@NonNull final String cardId, @NonNull final String esc);

    boolean saveESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits, @NonNull final String esc);

    void deleteESCWith(@NonNull final String cardId);

    void deleteESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits);

    Set<String> getESCCardIds();

    boolean isESCEnabled();
}
