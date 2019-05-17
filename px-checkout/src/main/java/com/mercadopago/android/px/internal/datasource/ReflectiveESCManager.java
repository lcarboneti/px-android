package com.mercadopago.android.px.internal.datasource;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public final class ReflectiveESCManager implements IESCManager {

    private static final class EscManagerNames {
        private static final String FACTORY_METHOD = "create";
        private static final String CLASS_NAME = "com.mercadopago.ml_esc_manager.ESCManager";
        private static final String METHOD_GET_SAVED_CARD_IDS = "getSavedCardIds";
        private static final String METHOD_SAVE_ESC_WITH = "saveESCWith";
        private static final String METHOD_DELETE_ESC_WITH = "deleteESCWith";
        private static final String METHOD_GET_ESC = "getESC";
    }

    @NonNull private final Context appContext;

    @Nullable private final Object escManagerInstance;

    private final boolean escEnabled;

    public ReflectiveESCManager(@NonNull final Context appContext, @NonNull final String sessionId,
        final boolean escEnabled) {
        this.appContext = appContext;
        escManagerInstance = createEscManagerInstance(sessionId);
        this.escEnabled = escEnabled && escManagerInstance != null;
    }

    @Override
    public boolean isESCEnabled() {
        return escEnabled;
    }

    @Nullable
    private Object createEscManagerInstance(@NonNull final String sessionId) {
        try {
            final Class escManagerClass = Class.forName(EscManagerNames.CLASS_NAME);
            final Method factoryMethod =
                escManagerClass.getMethod(EscManagerNames.FACTORY_METHOD, Context.class, String.class);
            return factoryMethod.invoke(null, appContext, sessionId);
        } catch (final Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    public String getESC(@Nullable final String cardId, @NonNull final String firstDigits,
        @NonNull final String lastDigits) {
        try {
            if (allowOperate()) {
                final Method getMethod = escManagerInstance.getClass()
                    .getMethod(EscManagerNames.METHOD_GET_ESC, String.class, String.class, String.class);
                return (String) getMethod.invoke(escManagerInstance, cardId, firstDigits, lastDigits);
            }
            return null;
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public boolean saveESCWith(@NonNull final String cardId, @NonNull final String value) {
        return save(cardId, value);
    }

    @Override
    public boolean saveESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits,
        @NonNull final String esc) {
        return save(firstDigits, lastDigits, esc);
    }

    private boolean save(final String... args) {
        try {
            if (allowOperate()) {
                final Method saveMethod = escManagerInstance.getClass()
                    .getMethod(EscManagerNames.METHOD_SAVE_ESC_WITH, createClassesParams(args));
                final Object wasSaved = saveMethod.invoke(escManagerInstance, args);
                return (Boolean) wasSaved;
            }

            return false;
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public void deleteESCWith(@NonNull final String cardId) {
        delete(cardId);
    }

    @Override
    public void deleteESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits) {
        delete(firstDigits, lastDigits);
    }

    private void delete(@NonNull final String... args) {
        try {
            if (allowOperate()) {
                final Method deleteMethod =
                    escManagerInstance.getClass()
                        .getMethod(EscManagerNames.METHOD_DELETE_ESC_WITH, createClassesParams(args));
                deleteMethod.invoke(escManagerInstance, args);
            }
        } catch (final Exception e) {
            //Do nothing
        }
    }

    private Class<String>[] createClassesParams(@NonNull final String[] args) {
        final Class<String>[] params = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            params[i] = String.class;
        }
        return params;
    }

    @Override
    public Set<String> getESCCardIds() {
        try {
            if (allowOperate()) {
                final Method getAllMethod =
                    escManagerInstance.getClass().getMethod(EscManagerNames.METHOD_GET_SAVED_CARD_IDS);
                final Object objects = getAllMethod.invoke(escManagerInstance);
                return (Set<String>) objects;
            } else {
                return Collections.emptySet();
            }
        } catch (final Exception e) {
            return Collections.emptySet();
        }
    }

    private boolean allowOperate() {
        return escEnabled && escManagerInstance != null;
    }
}
