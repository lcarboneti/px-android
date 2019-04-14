package com.mercadopago.android.px.mocks;

import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.utils.ResourcesUtil;

public class PaymentMethodSearchs {

    private PaymentMethodSearchs() {
    }

    public static InitResponse getCompletePaymentMethodSearchMLA() {
        String json = ResourcesUtil.getStringResource("complete_payment_method_search_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithSavedCardsMLA() {
        String json = ResourcesUtil.getStringResource("saved_cards_payment_method_search_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithPaymentMethodOnTop() {
        String json = ResourcesUtil.getStringResource("payment_method_on_top.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithCardsMLA() {
        String json = ResourcesUtil.getStringResource("cards_but_no_account_money_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodWithoutCustomOptionsMLA() {
        String json = ResourcesUtil.getStringResource("not_cards_nor_account_money_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithOnlyCreditCardMLA() {
        String json = ResourcesUtil.getStringResource("only_credit_card_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithOnlyTicketMLA() {
        String json = ResourcesUtil.getStringResource("only_ticket_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithOnlyAccountMoneyMLA() {
        String json = ResourcesUtil.getStringResource("only_account_money_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithOnlyCreditCardAndOneCardMLA() {
        String json = ResourcesUtil.getStringResource("only_credit_card_and_one_card_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithOnlyCreditCardAndAccountMoneyMLA() {
        String json = ResourcesUtil.getStringResource("only_credit_card_and_account_money_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithOnlyOneOffTypeAndAccountMoneyMLA() {
        String json = ResourcesUtil.getStringResource("only_one_off_type_and_account_money_MLA.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }

    public static InitResponse getPaymentMethodSearchWithOnlyBolbradescoMLB() {
        String json = ResourcesUtil.getStringResource("only_bolbradesco_payment_method_search_MLB.json");
        return JsonUtil.getInstance().fromJson(json, InitResponse.class);
    }
}
