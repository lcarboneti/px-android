package com.mercadopago.android.px.internal.features.paymentresult;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.PaymentResultScreenConfiguration;
import com.mercadopago.android.px.internal.features.paymentresult.components.Header;
import com.mercadopago.android.px.internal.features.paymentresult.components.PaymentResultContainer;
import com.mercadopago.android.px.internal.features.paymentresult.props.HeaderProps;
import com.mercadopago.android.px.internal.features.paymentresult.props.PaymentResultProps;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.mocks.InitStubUtils;
import com.mercadopago.android.px.mocks.PaymentResults;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentResult;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentResultContainerTest {

    private static final String CURRENCY_ID = "ARS";

    private final static String APPROVED_TITLE = "approved title";
    private final static String PENDING_TITLE = "pending title";
    private final static String REJECTED_OTHER_REASON_TITLE = "rejected other reason title";
    private final static String REJECTED_INSUFFICIENT_AMOUNT_TITLE = "rejected insufficient amount title";
    private final static String REJECTED_BAD_FILLED_TITLE = "rejected bad filled title";
    private final static String REJECTED_CALL_FOR_AUTH_TITLE = "rejected call for auth title";
    private final static String EMPTY_TITLE = "empty title";
    private final static String PENDING_LABEL = "pending label";
    private final static String REJECTION_LABEL = "rejection label";
    private final static String REJECTED_BLACKLIST = "rejected_blacklist";
    private final static String REJECTED_FRAUD = "rejected_fraud";

    private ActionDispatcher dispatcher;

    @Mock
    private Context context;

    @Before
    public void setup() {
        dispatcher = mock(ActionDispatcher.class);

        when(context.getString(R.string.px_title_approved_payment)).thenReturn(APPROVED_TITLE);
        when(context.getString(R.string.px_title_pending_payment)).thenReturn(PENDING_TITLE);
        when(context.getString(R.string.px_title_other_reason_rejection)).thenReturn(REJECTED_OTHER_REASON_TITLE);
        when(context.getString(R.string.px_text_insufficient_amount)).thenReturn(REJECTED_INSUFFICIENT_AMOUNT_TITLE);
        when(context.getString(R.string.px_text_some_card_data_is_incorrect)).thenReturn(REJECTED_BAD_FILLED_TITLE);
        when(context.getString(R.string.px_title_activity_call_for_authorize)).thenReturn(REJECTED_CALL_FOR_AUTH_TITLE);
        when(context.getString(R.string.px_title_pending_payment)).thenReturn(PENDING_LABEL);
        when(context.getString(R.string.px_rejection_label)).thenReturn(REJECTION_LABEL);
        when(context.getString(R.string.px_title_rejection_blacklist)).thenReturn(REJECTED_BLACKLIST);
        when(context.getString(R.string.px_title_rejection_fraud)).thenReturn(REJECTED_FRAUD);
    }

    @Test
    public void onApprovedPaymentThenShowGreenBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_success_color);
    }

    @Test
    public void onInProcessPaymentThenShowGreenBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusInProcessContingencyPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_warning_color);
    }

    @Test
    public void onPaymentMethodOffPaymentThenShowGreenBackground() {

        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_success_color);
    }

    @Test
    public void onRejectedOtherReasonPaymentThenShowRedBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_error_color);
    }

    @Test
    public void onRejectedCallForAuthPaymentThenShowOrangeBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusCallForAuthPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_warning_color);
    }

    @Test
    public void onRejectedInsufficientAmountPaymentThenShowOrangeBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedInsufficientAmountPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_warning_color);
    }

    @Test
    public void onRejectedBadFilledSecuPaymentThenShowOrangeBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledSecuPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_warning_color);
    }

    @Test
    public void onRejectedBadFilledDatePaymentThenShowOrangeBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledDatePaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_warning_color);
    }

    @Test
    public void onRejectedBadFilledFormPaymentThenShowOrangeBackground() {

        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledFormPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.background, R.color.ui_components_warning_color);
    }

    @Test
    public void onInvalidPaymentResultStatusGetDefaultBackground() {
        final PaymentData paymentData = new PaymentData.Builder()
            .setPaymentMethod(InitStubUtils.getPaymentMethodOff())
            .createPaymentData();

        final PaymentResult paymentResult = new PaymentResult.Builder()
            .setPaymentStatus("")
            .setPaymentStatusDetail("")
            .setPaymentData(paymentData)
            .build();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);
        Assert.assertEquals(headerProps.background, R.color.ui_components_error_color);
    }

    @Test
    public void onAccreditedPaymentThenShowItemIcon() {
        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.ITEM_ICON_IMAGE);
    }

    @Test
    public void onPaymentMethodOffPaymentThenShowItemIcon() {
        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.ITEM_ICON_IMAGE);
    }

    @Test
    public void onPaymentMethodOnInProcessThenShowCardIcon() {
        final PaymentResult paymentResult = PaymentResults.getStatusInProcessContingencyPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.CARD_ICON_IMAGE);
    }

    @Test
    public void onPaymentMethodOnRejectedBadFilledThenShowCardIcon() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledFormPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.CARD_ICON_IMAGE);
    }

    @Test
    public void onPaymentMethodOnRejectedInsufficientAmountThenShowCardIcon() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedInsufficientAmountPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.CARD_ICON_IMAGE);
    }

    @Test
    public void onBoletoRejectedPaymentThenShowBoletoIcon() {
        final PaymentResult paymentResult = PaymentResults.getBoletoRejectedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.BOLETO_ICON_IMAGE);
    }

    @Test
    public void onBoletoApprovedPaymentThenShowItemIcon() {
        final PaymentResult paymentResult = PaymentResults.getBoletoApprovedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.ITEM_ICON_IMAGE);
    }

    @Test
    public void onPaymentMethodOnRejectedOtherThenShowCardIcon() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.CARD_ICON_IMAGE);
    }

    @Test
    public void onInvalidPaymentResultStatusGetDefaultIcon() {
        final PaymentData paymentData = new PaymentData.Builder()
            .setPaymentMethod(InitStubUtils.getPaymentMethodOff())
            .createPaymentData();

        final PaymentResult paymentResult = new PaymentResult.Builder()
            .setPaymentStatus("")
            .setPaymentStatusDetail("")
            .setPaymentData(paymentData)
            .build();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);
        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.DEFAULT_ICON_IMAGE);
    }

//    @Test
//    public void onCustomizedIconOnApprovedStatusThenShowIt() {
//        final int customizedIcon = 1;
//
//        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .setApprovedHeaderIcon(customizedIcon)
//            .build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//        Assert.assertEquals(headerProps.iconImage, customizedIcon);
//    }
//
//    @Test
//    public void onCustomizedIconOnPaymentMethodOffThenShowIt() {
//        final int customizedIcon = 2;
//
//        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .setPendingHeaderIcon(customizedIcon).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//        Assert.assertEquals(headerProps.iconImage, customizedIcon);
//    }
//
//    @Test
//    public void onCustomizedIconOnRejectedStatusThenShowIt() {
//        final int customizedIcon = 3;
//
//        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
//        final PaymentResultScreenConfiguration preferences =
//            new PaymentResultScreenConfiguration.Builder()
//                .setRejectedHeaderIcon(customizedIcon).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preferences);
//
//        Assert.assertEquals(headerProps.iconImage, customizedIcon);
//    }
//
//    @Test
//    public void onCustomizedIconWithOtherStatusThenDontShowIt() {
//        final int customizedIcon = 4;
//
//        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .setRejectedHeaderIcon(customizedIcon).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//
//        Assert.assertNotSame(headerProps.iconImage, customizedIcon);
//        Assert.assertEquals(headerProps.iconImage, PaymentResultContainer.ITEM_ICON_IMAGE);
//    }

    @Test
    public void onApprovedPaymentThenShowCheckBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.CHECK_BADGE_IMAGE);
    }

    @Test
    public void onInProcessPaymentThenShowPendingBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusInProcessContingencyPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.PENDING_BADGE_ORANGE_IMAGE);
    }

    @Test
    public void onPaymentMethodOffThenShowPendingBadge() {
        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.PENDING_BADGE_GREEN_IMAGE);
    }

    @Test
    public void onStatusCallForAuthPaymentThenShowWarningBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusCallForAuthPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.WARNING_BADGE_IMAGE);
    }

    @Test
    public void onStatusBadFilledSecuPaymentThenShowWarningBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledSecuPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.WARNING_BADGE_IMAGE);
    }

    @Test
    public void onStatusBadFilledDatePaymentThenShowWarningBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledDatePaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.WARNING_BADGE_IMAGE);
    }

    @Test
    public void onStatusBadFilledFormPaymentThenShowWarningBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledFormPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.WARNING_BADGE_IMAGE);
    }

    @Test
    public void onStatusInsufficientAmountPaymentThenShowWarningBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedInsufficientAmountPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.WARNING_BADGE_IMAGE);
    }

    @Test
    public void onStatusRejectedOtherReasonPaymentThenShowErrorBadge() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.ERROR_BADGE_IMAGE);
    }

    @Test
    public void onInvalidPaymentResultStatusGetDefaultBadge() {
        final PaymentData paymentData = new PaymentData.Builder()
            .setPaymentMethod(InitStubUtils.getPaymentMethodOff())
            .createPaymentData();

        final PaymentResult paymentResult = new PaymentResult.Builder()
            .setPaymentStatus("")
            .setPaymentStatusDetail("")
            .setPaymentData(paymentData)
            .build();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);
        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.ERROR_BADGE_IMAGE);
    }

//    @Test
//    public void onCustomizedBadgeOnApprovedStatusThenShowIt() {
//        final String customizedBadge = Badge.PENDING_BADGE_IMAGE;
//        final int badgeImage = PaymentResultContainer.PENDING_BADGE_GREEN_IMAGE;
//
//
//        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .setBadgeApproved(customizedBadge).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//        Assert.assertEquals(headerProps.badgeImage, badgeImage);
//    }

//    @Test
//    public void onInvalidCustomizedBadgeOnApprovedStatusThenDontShowIt() {
//        final String customizedBadge = "";
//
//        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
//        new PaymentResultScreenConfiguration.Builder()
//            .setBadgeApproved(customizedBadge).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);
//        Assert.assertEquals(headerProps.badgeImage, PaymentResultContainer.CHECK_BADGE_IMAGE);
//    }

    @Test
    public void onApprovedPaymentThenShowApprovedTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_title_approved_payment));
    }

    @Test
    public void onInProcessPaymentThenShowPendingTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusInProcessContingencyPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_title_pending_payment));
    }

    @Test
    public void onRejectedOtherReasonPaymentThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(paymentResult.getPaymentData().getPaymentMethod().getName(), "Mastercard");
        Assert.assertEquals(headerProps.title, context.getString(R.string.px_title_other_reason_rejection));
    }

    @Test
    public void onRejectedInsufficientAmountPaymentThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedInsufficientAmountPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(paymentResult.getPaymentData().getPaymentMethod().getName(), "Mastercard");
        Assert.assertEquals(headerProps.title, context.getString(R.string.px_text_insufficient_amount));
    }

    @Test
    public void onRejectedBadFilledSecuPaymentThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledSecuPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_text_some_card_data_is_incorrect));
    }

    @Test
    public void onRejectedBadFilledDatePaymentThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledDatePaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_text_some_card_data_is_incorrect));
    }

    @Test
    public void onRejectedBadFilledFormPaymentThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledFormPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_text_some_card_data_is_incorrect));
    }

    @Test
    public void onRejectedCallForAuthPaymentThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusCallForAuthPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_title_activity_call_for_authorize));
    }

    @Test
    public void onPaymentMethodOffPaymentThenShowInstructionsTitle() {
        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();
        final Instruction instruction = com.mercadopago.android.px.mocks.Instructions.getRapipagoInstruction();

        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, instruction);

        Assert.assertEquals(headerProps.title, instruction.getTitle());
    }

    @Test
    public void onPaymentMethodOffWithoutInstructionThenShowEmptyTitle() {
        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();

        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, TextUtil.EMPTY);
    }

    @Test
    public void onRejectedBlacklistedThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBlacklist();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_title_rejection_blacklist));
    }

    @Test
    public void onRejectedFraudThenShowTitle() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedFraud();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.title, context.getString(R.string.px_title_rejection_fraud));
    }

//    @Test
//    public void onCustomizedTitleOnApprovedStatusThenShowIt() {
//        final String customizedTitle = "customized approved";
//
//        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .setApprovedTitle(customizedTitle).build();
//
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//        Assert.assertEquals(headerProps.title, customizedTitle);
//    }
//
//    @Test
//    public void onCustomizedTitleOnInProcessStatusThenShowIt() {
//        final String customizedTitle = "customized pending";
//
//        final PaymentResult paymentResult = PaymentResults.getStatusInProcessContingencyPaymentResult();
//
//        final PaymentResultScreenConfiguration preferences = new PaymentResultScreenConfiguration.Builder()
//            .setPendingTitle(customizedTitle).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preferences);
//        Assert.assertEquals(headerProps.title, customizedTitle);
//    }
//
//    @Test
//    public void onCustomizedTitleOnPaymentMethodOffThenDontShowIt() {
//        final String customizedTitle = "customized instructions";
//
//        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();
//        new PaymentResultScreenConfiguration.Builder()
//            .setPendingTitle(customizedTitle).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);
//        Assert.assertNotSame(headerProps.title, customizedTitle);
//    }

//    @Test
//    public void onCustomizedTitleOnRejectedStatusThenShowIt() {
//        final String customizedTitle = "customized rejected";
//
//        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .setRejectedTitle(customizedTitle).build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//        Assert.assertEquals(headerProps.title, customizedTitle);
//    }

    @Test
    public void onApprovedPaymentThenShowEmptyLabel() {
        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, TextUtil.EMPTY);
    }

    @NonNull
    private HeaderProps getHeaderPropsFromContainerWith(@NonNull final PaymentResult paymentResult) {
        return getHeaderPropsFromContainerWith(paymentResult, new PaymentResultScreenConfiguration.Builder().build());
    }

    @Test
    public void onInProcessPaymentThenShowEmptyLabel() {
        final PaymentResult paymentResult = PaymentResults.getStatusInProcessContingencyPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, TextUtil.EMPTY);
    }

    @Test
    public void onPaymentMethodOffPaymentThenShowPendingLabel() {
        final PaymentResult paymentResult = PaymentResults.getPaymentMethodOffPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, TextUtil.EMPTY);
    }

    @Test
    public void onRejectedBadFilledStatusPaymentThenShowRejectionLabel() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledFormPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, context.getString(R.string.px_rejection_label));
    }

    @Test
    public void onRejectedInsufficientAmountStatusPaymentThenShowRejectionLabel() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedInsufficientAmountPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, context.getString(R.string.px_rejection_label));
    }

    @Test
    public void onRejectedOtherReasonStatusPaymentThenShowRejectionLabel() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, context.getString(R.string.px_rejection_label));
    }

    @Test
    public void onRejectedPaymentOffStatusPaymentThenShowRejectionLabel() {
        final PaymentResult paymentResult = PaymentResults.getBoletoRejectedPaymentResult();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, context.getString(R.string.px_rejection_label));
    }

    @Test
    public void onInvalidPaymentResultStatusGetEmptyLabel() {
        final PaymentData paymentData = new PaymentData.Builder()
            .setPaymentMethod(InitStubUtils.getPaymentMethodOff())
            .createPaymentData();

        final PaymentResult paymentResult = new PaymentResult.Builder()
            .setPaymentStatus("")
            .setPaymentStatusDetail("")
            .setPaymentData(paymentData)
            .build();
        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);

        Assert.assertEquals(headerProps.label, TextUtil.EMPTY);
    }

//    @Test
//    public void onCustomizedLabelOnApprovedStatusThenShowIt() {
//        final String customizedLabel = "customized approved label";
//
//        final PaymentResult paymentResult = PaymentResults.getStatusApprovedPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .setApprovedLabelText(customizedLabel).build();
//
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//        Assert.assertEquals(headerProps.label, customizedLabel);
//    }

//    @Test
//    public void onCustomizedDisabledLabelOnRejectedStatusThenHideIt() {
//
//        final PaymentResult paymentResult = PaymentResults.getStatusRejectedOtherPaymentResult();
//        final PaymentResultScreenConfiguration preference = new PaymentResultScreenConfiguration.Builder()
//            .disableRejectedLabelText().build();
//
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult, preference);
//        Assert.assertEquals(headerProps.label, "");
//    }
//
//    @Test
//    public void onCustomizedDisabledLabelOnInvalidStatusThenShowDefaultLabel() {
//
//        final PaymentResult paymentResult = PaymentResults.getStatusInProcessContingencyPaymentResult();
//        new PaymentResultScreenConfiguration.Builder()
//            .disableRejectedLabelText().build();
//
//        final HeaderProps headerProps = getHeaderPropsFromContainerWith(paymentResult);
//        Assert.assertEquals(headerProps.label, paymentResultProvider.getEmptyText());
//    }

    @Test
    public void testHasBodyComponentOnCallForAuth() {
        final PaymentResult paymentResult = PaymentResults.getStatusCallForAuthPaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .build();
        container.setProps(paymentResultProps);

        Assert.assertTrue(container.hasBodyComponent());
    }

    @Test
    public void testDoesntHaveBodyComponentOnBadFilledDate() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledDatePaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .build();
        container.setProps(paymentResultProps);

        Assert.assertFalse(container.hasBodyComponent());
    }

    @Test
    public void testDoesntHaveBodyComponentOnBadFilledSecu() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledSecuPaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .build();
        container.setProps(paymentResultProps);

        Assert.assertFalse(container.hasBodyComponent());
    }

    @Test
    public void testDoesntHaveBodyComponentOnBadFilledForm() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledFormPaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .build();
        container.setProps(paymentResultProps);

        Assert.assertFalse(container.hasBodyComponent());
    }

    @Test
    public void testHeaderWrapModeOnCallForAuth() {
        final PaymentResult paymentResult = PaymentResults.getStatusCallForAuthPaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .setCurrencyId(CURRENCY_ID)
                .build();
        container.setProps(paymentResultProps);

        final Header header = container.getHeaderComponent(context);
        Assert.assertEquals(header.props.height, HeaderProps.HEADER_MODE_WRAP);
    }

    @Test
    public void testHeaderStretchOnBadFilledDate() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledDatePaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .build();
        container.setProps(paymentResultProps);

        Header header = container.getHeaderComponent(context);
        Assert.assertEquals(header.props.height, HeaderProps.HEADER_MODE_STRETCH);
    }

    @Test
    public void testHeaderStretchOnBadFilledSecu() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledSecuPaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .build();
        container.setProps(paymentResultProps);

        Header header = container.getHeaderComponent(context);
        Assert.assertEquals(header.props.height, HeaderProps.HEADER_MODE_STRETCH);
    }

    @Test
    public void testHeaderStretchOnBadFilledForm() {
        final PaymentResult paymentResult = PaymentResults.getStatusRejectedBadFilledFormPaymentResult();
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .build();

        container.setProps(paymentResultProps);

        Header header = container.getHeaderComponent(context);
        Assert.assertEquals(header.props.height, HeaderProps.HEADER_MODE_STRETCH);
    }

    private HeaderProps getHeaderPropsFromContainerWith(@NonNull final PaymentResult paymentResult, @NonNull final
    PaymentResultScreenConfiguration pref) {
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps = new PaymentResultProps.Builder(pref)
            .setPaymentResult(paymentResult)
            .setCurrencyId(CURRENCY_ID)
            .build();

        container.setProps(paymentResultProps);
        return container.getHeaderComponent(context).props;
    }

    private HeaderProps getHeaderPropsFromContainerWith(PaymentResult paymentResult, Instruction instruction) {
        final PaymentResultContainer container = getContainer();

        final PaymentResultProps paymentResultProps =
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build())
                .setPaymentResult(paymentResult)
                .setInstruction(instruction)
                .build();

        container.setProps(paymentResultProps);
        return container.getHeaderComponent(context).props;
    }

    private PaymentResultContainer getContainer() {
        return new PaymentResultContainer(dispatcher,
            new PaymentResultProps.Builder(new PaymentResultScreenConfiguration.Builder().build()).build());
    }
}
