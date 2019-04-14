package com.mercadopago.android.px.model.internal;

import com.mercadopago.android.px.model.PaymentMethodSearch;
import java.util.EnumMap;

public final class InitResponse extends PaymentMethodSearch {

    private EnumMap<Feature, FeatureConfig> features;

    public EnumMap<Feature, FeatureConfig> getFeatures() {
        return features == null ? new EnumMap<>(Feature.class) : features;
    }
}
