package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.core.DynamicFragmentCreator;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

// Used by single player to inform charges and other payment special information.
// Single player usecase depends on amount and payment method.
@SuppressWarnings("unused")
public final class DynamicFragmentConfiguration implements Serializable {

    private final HashMap<FragmentLocation, DynamicFragmentCreator> creators;

    public enum FragmentLocation {
        TOP_PAYMENT_METHOD_REVIEW_AND_CONFIRM,
        BOTTOM_PAYMENT_METHOD_REVIEW_AND_CONFIRM
    }

    /* default */ DynamicFragmentConfiguration(@NonNull final Builder builder) {
        creators = builder.creators;
    }

    @Nullable
    public DynamicFragmentCreator getCreatorFor(@NonNull final FragmentLocation fragmentLocation) {
        return creators.get(fragmentLocation);
    }

    public boolean hasCreatorFor(@NonNull final FragmentLocation fragmentLocation) {
        return creators.containsKey(fragmentLocation);
    }

    public Set<FragmentLocation> getSupportedLocations() {
        return creators.keySet();
    }


    public static final class Builder {

        /* default */ HashMap<FragmentLocation, DynamicFragmentCreator> creators = new HashMap<>();

        /**
         * @param location where dynamic fragment will be placed.
         * @param dynamicFragmentCreator your creator.
         */
        public Builder addDynamicCreator(@NonNull final FragmentLocation location,
            @NonNull final DynamicFragmentCreator dynamicFragmentCreator) {
            creators.put(location, dynamicFragmentCreator);
            return this;
        }

        public DynamicFragmentConfiguration build() {
            return new DynamicFragmentConfiguration(this);
        }
    }
}
