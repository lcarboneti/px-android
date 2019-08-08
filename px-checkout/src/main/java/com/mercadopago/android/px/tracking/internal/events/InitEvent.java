package com.mercadopago.android.px.tracking.internal.events;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.tracking.internal.model.InitData;
import java.util.Map;

public final class InitEvent extends EventTracker {

    @NonNull private final InitData initData;

    public InitEvent(@NonNull final PaymentSettingRepository paymentSettingRepository) {
        initData = InitData.from(paymentSettingRepository);
    }

    @NonNull
    @Override
    public String getEventPath() {
        return BASE_PATH + "/init";
    }

    @NonNull
    @Override
    public Map<String, Object> getEventData() {
        return initData.toMap();
    }
}