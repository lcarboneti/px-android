package com.mercadopago.android.px.internal.services;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Instructions;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface InstructionsClient {

    String INSTRUCTIONS_VERSION = "1.7";

    @GET("{environment}/{version}/checkout/payments/{payment_id}/results?api_version=" + INSTRUCTIONS_VERSION)
    MPCall<Instructions> getInstructions(
        @Path(value = "environment", encoded = true) String environment,
        @Path(value = "version", encoded = true) String version,
        @Header("Accept-Language") String locale,
        @Path(value = "payment_id", encoded = true) Long paymentId,
        @Query("public_key") String mKey,
        @Nullable @Query("access_token") String privateKey,
        @Query("payment_type") String paymentTypeId);
}
