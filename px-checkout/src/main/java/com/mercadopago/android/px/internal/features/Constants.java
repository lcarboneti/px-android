package com.mercadopago.android.px.internal.features;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.features.bank_deals.BankDealsActivity;
import com.mercadopago.android.px.internal.features.installments.InstallmentsActivity;
import com.mercadopago.android.px.internal.features.payment_vault.PaymentVaultActivity;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.preferences.PaymentPreference;
import java.util.List;

public final class Constants {

    public static final int RESULT_PAYMENT = 200;
    public static final int RESULT_ACTION = 201;
    public static final int RESULT_CUSTOM_EXIT = 202;
    public static final int RESULT_CANCELED_RYC = 203;

    public static final int RESULT_CHANGE_PAYMENT_METHOD = 300;
    public static final int RESULT_CANCEL_PAYMENT = 499;
    public static final int RESULT_FAIL_ESC = 500;
    public static final int RESULT_ERROR = 502;
    public static final int RESULT_SILENT_ERROR = 8;

    private Constants() {
    }

    public static final class Activities {

        public static final int PAYMENT_METHODS_REQUEST_CODE = 1;
        public static final int INSTALLMENTS_REQUEST_CODE = 2;
        public static final int ISSUERS_REQUEST_CODE = 3;
        public static final int CALL_FOR_AUTHORIZE_REQUEST_CODE = 7;
        public static final int PENDING_REQUEST_CODE = 8;
        public static final int REJECTION_REQUEST_CODE = 9;
        public static final int PAYMENT_VAULT_REQUEST_CODE = 10;
        public static final int BANK_DEALS_REQUEST_CODE = 11;
        public static final int GUESSING_CARD_FOR_PAYMENT_REQUEST_CODE = 13;
        public static final int INSTRUCTIONS_REQUEST_CODE = 14;

        public static final int CONGRATS_REQUEST_CODE = 16;
        public static final int PAYMENT_TYPES_REQUEST_CODE = 17;
        public static final int SECURITY_CODE_REQUEST_CODE = 18;

        private Activities() {
        }

        public static class PaymentVaultActivityBuilder {

            private Activity activity;

            public PaymentVaultActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }

                startPaymentVaultActivity();
            }

            private void startPaymentVaultActivity() {
                final Intent intent = new Intent(activity, PaymentVaultActivity.class);
                activity.startActivityForResult(intent, PAYMENT_VAULT_REQUEST_CODE);
            }
        }

        public static class PaymentMethodsActivityBuilder {

            private Activity activity;
            private PaymentPreference paymentPreference;

            public PaymentMethodsActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentMethodsActivityBuilder setPaymentPreference(final PaymentPreference paymentPreference) {
                this.paymentPreference = paymentPreference;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                startPaymentMethodsActivity();
            }

            private void startPaymentMethodsActivity() {
                final Intent paymentMethodsIntent = new Intent(activity, PaymentMethodsActivity.class);
                paymentMethodsIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

                activity.startActivityForResult(paymentMethodsIntent, PAYMENT_METHODS_REQUEST_CODE);
            }
        }

        public static class InstallmentsActivityBuilder {
            private Activity activity;
            private CardInfo cardInfo;

            public InstallmentsActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public InstallmentsActivityBuilder setCardInfo(final CardInfo cardInformation) {
                cardInfo = cardInformation;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                startInstallmentsActivity();
            }

            private void startInstallmentsActivity() {
                final Intent intent = new Intent(activity, InstallmentsActivity.class);
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInfo));
                activity.startActivityForResult(intent, INSTALLMENTS_REQUEST_CODE);
            }
        }

        public static class PaymentTypesActivityBuilder {
            private Activity activity;
            private CardInfo cardInformation;
            private List<PaymentMethod> paymentMethods;
            private List<PaymentType> paymentTypes;

            public PaymentTypesActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public PaymentTypesActivityBuilder setCardInfo(final CardInfo cardInformation) {
                this.cardInformation = cardInformation;
                return this;
            }

            public PaymentTypesActivityBuilder setPaymentMethods(@Nullable final List<PaymentMethod> paymentMethods) {
                this.paymentMethods = paymentMethods;
                return this;
            }

            public PaymentTypesActivityBuilder setPaymentTypes(final List<PaymentType> paymentTypes) {
                this.paymentTypes = paymentTypes;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                if (paymentMethods == null) {
                    throw new IllegalStateException("payment method list is null");
                }
                if (paymentTypes == null) {
                    throw new IllegalStateException("payment types list is null");
                }

                startSecurityCodeActivity();
            }

            private void startSecurityCodeActivity() {
                final Intent intent = new Intent(activity, PaymentTypesActivity.class);
                intent.putExtra("paymentMethods", JsonUtil.getInstance().toJson(paymentMethods));
                intent.putExtra("paymentTypes", JsonUtil.getInstance().toJson(paymentTypes));
                intent.putExtra("cardInfo", JsonUtil.getInstance().toJson(cardInformation));
                activity.startActivityForResult(intent, PAYMENT_TYPES_REQUEST_CODE);
            }
        }

        public static class BankDealsActivityBuilder {

            private Activity activity;
            private List<BankDeal> bankDeals;

            public BankDealsActivityBuilder setActivity(final Activity activity) {
                this.activity = activity;
                return this;
            }

            public BankDealsActivityBuilder setBankDeals(final List<BankDeal> bankDeals) {
                this.bankDeals = bankDeals;
                return this;
            }

            public void startActivity() {
                if (activity == null) {
                    throw new IllegalStateException("activity is null");
                }
                startBankDealsActivity();
            }

            private void startBankDealsActivity() {
                final Intent bankDealsIntent = new Intent(activity, BankDealsActivity.class);
                if (bankDeals != null) {
                    bankDealsIntent.putExtra("bankDeals", JsonUtil.getInstance().toJson(bankDeals));
                }
                activity.startActivityForResult(bankDealsIntent, BANK_DEALS_REQUEST_CODE);
            }
        }
    }
}
