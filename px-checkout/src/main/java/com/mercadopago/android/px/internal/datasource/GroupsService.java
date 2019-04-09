package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.datasource.cache.GroupsCache;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.CheckoutService;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.CheckoutParams;
import com.mercadopago.android.px.model.internal.InitRequest;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import java.util.ArrayList;

import static com.mercadopago.android.px.services.BuildConfig.API_ENVIRONMENT;

public class GroupsService implements GroupsRepository {

    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final MercadoPagoESC mercadoPagoESC;
    @NonNull private final CheckoutService checkoutService;
    @NonNull private final String language;
    @NonNull /* default */ final GroupsCache groupsCache;

    public GroupsService(@NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final MercadoPagoESC mercadoPagoESC, @NonNull final CheckoutService checkoutService,
        @NonNull final String language, @NonNull final GroupsCache groupsCache) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.mercadoPagoESC = mercadoPagoESC;
        this.checkoutService = checkoutService;
        this.language = language;
        this.groupsCache = groupsCache;
    }

    @NonNull
    @Override
    public MPCall<PaymentMethodSearch> getGroups() {
        if (groupsCache.isCached()) {
            return groupsCache.get();
        } else {
            return newCall();
        }
    }

    @NonNull
    private MPCall<PaymentMethodSearch> newCall() {
        return new MPCall<PaymentMethodSearch>() {

            @Override
            public void enqueue(final Callback<PaymentMethodSearch> callback) {
                newRequest().enqueue(getInternalCallback(callback));
            }

            @Override
            public void execute(final Callback<PaymentMethodSearch> callback) {
                newRequest().execute(getInternalCallback(callback));
            }

            @NonNull /* default */ Callback<PaymentMethodSearch> getInternalCallback(
                final Callback<PaymentMethodSearch> callback) {
                return new Callback<PaymentMethodSearch>() {
                    @Override
                    public void success(final PaymentMethodSearch paymentMethodSearch) {
                        groupsCache.put(paymentMethodSearch);
                        callback.success(paymentMethodSearch);
                    }

                    @Override
                    public void failure(final ApiException apiException) {
                        callback.failure(apiException);
                    }
                };
            }
        };
    }

    /* default */
    @NonNull
    MPCall<PaymentMethodSearch> newRequest() {

        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
        final PaymentConfiguration paymentConfiguration = paymentSettingRepository.getPaymentConfiguration();

        final DiscountParamsConfiguration discountParamsConfiguration =
            paymentSettingRepository.getAdvancedConfiguration()
                .getDiscountParamsConfiguration();

        final CheckoutParams checkoutParams = new CheckoutParams.Builder()
            .setDiscountConfiguration(discountParamsConfiguration)
            .setCardWithEsc(new ArrayList<>(mercadoPagoESC.getESCCardIds()))
            .setCharges(paymentConfiguration.getCharges())
            .setHasSplit(paymentConfiguration.getPaymentProcessor()
                .supportsSplitPayment(checkoutPreference))
            .setHasExpressPayment(paymentSettingRepository.getAdvancedConfiguration().isExpressPaymentEnabled())
            .build();

        final InitRequest initRequest = new InitRequest.Builder()
            .setCheckoutPreferenceId(paymentSettingRepository.getCheckoutPreferenceId())
            .setCheckoutPreference(checkoutPreference)
            .setCheckoutParams(checkoutParams)
            .build();

        return checkoutService
            .getPaymentMethodSearch(API_ENVIRONMENT, language, paymentSettingRepository.getPublicKey(),
                paymentSettingRepository.getPrivateKey(), JsonUtil.getInstance().getMapFromObject(initRequest));
    }
}
