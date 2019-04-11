package com.mercadopago.android.px.internal.services;

import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Issuer;
import java.util.HashMap;
import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CardService {
    @POST("{environment}/{version}/px_mobile_api/card-association")
    MPCall<Card> assignCard(@Path(value = "environment", encoded = true) String environment,
        @Path(value = "version", encoded = true) String version,
        @Query("access_token") String accessToken, @Body HashMap<String, Object> body);

    @GET("{environment}/{version}/payment_methods/card_issuers")
    MPCall<List<Issuer>> getCardIssuers(@Path(value = "environment", encoded = true) String environment,
        @Path(value = "version", encoded = true) String version,
        @Query("access_token") String accessToken,
        @Query("payment_method_id") String paymentMethodId, @Query("bin") String bin,
        @Query("processing_mode") String processingMode);

}
