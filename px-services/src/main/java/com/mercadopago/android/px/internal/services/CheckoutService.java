package com.mercadopago.android.px.internal.services;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.internal.InitResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CheckoutService {

    //TODO modify RESPONSE to support backend driven on/off features + merchant order + pref retrieved.
    @POST("{environment}/{version}/px_mobile_api/init")
    MPCall<InitResponse> init(
        @Path(value = "environment", encoded = true) String environment,
        @Path(value = "version", encoded = true) String version,
        @Header("Accept-Language") String locale,
        @Query("public_key") String publicKey,
        @Query("access_token") String privateKey,
        @Body Map<String, Object> body);

    /**
     * Old api call version ; used by MercadoPagoServices.
     *
     * @param version
     * @param locale
     * @param publicKey
     * @param amount
     * @param excludedPaymentTypes
     * @param excludedPaymentMethods
     * @param siteId
     * @param processingMode
     * @param cardsWithEsc
     * @param differentialPricingId
     * @param defaultInstallments
     * @param expressEnabled
     * @param accessToken
     * @return payment method search
     */
    @GET("{environment}/{version}/px_mobile_api/payment_methods?api_version=1.8")
    MPCall<PaymentMethodSearch> getPaymentMethodSearch(
        @Path(value = "environment", encoded = true) String environment,
        @Path(value = "version", encoded = true) String version,
        @Header("Accept-Language") String locale,
        @Query("public_key") String publicKey,
        @Query("amount") BigDecimal amount,
        @Query("excluded_payment_types") String excludedPaymentTypes,
        @Query("excluded_payment_methods") String excludedPaymentMethods,
        @Query("site_id") String siteId,
        @Query("processing_mode") String processingMode,
        @Query("cards_esc") String cardsWithEsc,
        @Nullable @Query("differential_pricing_id") Integer differentialPricingId,
        @Nullable @Query("default_installments") final Integer defaultInstallments,
        @Query("express_enabled") final boolean expressEnabled,
        @Nullable @Query("access_token") String accessToken);

    @GET("{environment}/{version}/payment_methods")
    MPCall<List<PaymentMethod>> getPaymentMethods(
        @Path(value = "environment", encoded = true) String environment,
        @Path(value = "version", encoded = true) String version,
        @Query("public_key") String publicKey,
        @Query("access_token") String privateKey);
}

