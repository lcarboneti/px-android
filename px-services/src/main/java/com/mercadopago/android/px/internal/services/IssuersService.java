package com.mercadopago.android.px.internal.services;

import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Issuer;
import java.util.List;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IssuersService {

    @GET("{environment}/{version}/checkout/payment_methods/card_issuers")
    MPCall<List<Issuer>> getIssuers(@Path(value = "environment", encoded = true) String environment,
        @Path(value = "version", encoded = true) String version,
        @Query("public_key") String publicKey, @Query("access_token") String privateKey,
        @Query("payment_method_id") String paymentMethodId, @Query("bin") String bin,
        @Query("processing_mode") String processingMode);
}
