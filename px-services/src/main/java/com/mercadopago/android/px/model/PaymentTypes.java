package com.mercadopago.android.px.model;

import java.util.ArrayList;
import java.util.List;

public final class PaymentTypes {

    public static String CREDIT_CARD = "credit_card";

    public static String DEBIT_CARD = "debit_card";

    public static String PREPAID_CARD = "prepaid_card";

    public static String TICKET = "ticket";

    public static String ATM = "atm";

    public static String DIGITAL_CURRENCY = "digital_currency";

    public static String BANK_TRANSFER = "bank_transfer";

    public static String ACCOUNT_MONEY = "account_money";

    public static String PLUGIN = "payment_method_plugin";

    private PaymentTypes() {
    }

    public static boolean isCardPaymentType(final String paymentType) {
        return PaymentTypes.CREDIT_CARD.equals(paymentType) ||
            PaymentTypes.DEBIT_CARD.equals(paymentType) ||
            PaymentTypes.PREPAID_CARD.equals(paymentType);
    }

    public static boolean isAccountMoney(final String type) {
        return PaymentTypes.ACCOUNT_MONEY.equals(type);
    }

    /**
     *  We don't support plugins since 4.5.0
     */
    @Deprecated
    public static boolean isPlugin(final String type) {
        return PaymentTypes.PLUGIN.equals(type);
    }

    public static List<String> getAllPaymentTypes() {
        return new ArrayList<String>() {{
            add(CREDIT_CARD);
            add(DEBIT_CARD);
            add(PREPAID_CARD);
            add(TICKET);
            add(ATM);
            add(DIGITAL_CURRENCY);
            add(BANK_TRANSFER);
            add(ACCOUNT_MONEY);
        }};
    }

    public static boolean isCreditCardPaymentType(final String paymentTypeId) {
        return PaymentTypes.CREDIT_CARD.equals(paymentTypeId);
    }

}
