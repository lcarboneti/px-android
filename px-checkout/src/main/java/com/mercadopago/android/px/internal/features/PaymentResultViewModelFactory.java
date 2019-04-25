package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.paymentresult.PaymentResultDecorator;
import com.mercadopago.android.px.internal.features.paymentresult.props.BodyErrorProps;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.NextAction;
import com.mercadopago.android.px.internal.view.RecoverPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.PaymentResultViewModel;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import java.util.HashMap;
import java.util.Map;
import static com.mercadopago.android.px.model.Payment.StatusCodes.STATUS_APPROVED;
import static com.mercadopago.android.px.model.Payment.StatusCodes.STATUS_IN_PROCESS;
import static com.mercadopago.android.px.model.Payment.StatusCodes.STATUS_PENDING;
import static com.mercadopago.android.px.model.Payment.StatusCodes.STATUS_REJECTED;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_BLACKLIST;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_CARD_DISABLED;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_FRAUD;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_HIGH_RISK;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_OTHER_REASON;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_CC_REJECTED_PLUGIN_PM;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_REJECTED_BY_REGULATIONS;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_REJECTED_HIGH_RISK;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_BY_BANK;
import static com.mercadopago.android.px.model.Payment.StatusDetail.STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA;

public final class PaymentResultViewModelFactory {

    private static final int EMPTY_LABEL = 0;

    private PaymentResultViewModelFactory() {
        // default empty constructor
    }

    public static PaymentResultDecorator createPaymentResultDecorator(@NonNull final IPayment payment) {
        final PaymentResultViewModel vm =
            createPaymentResultViewModel(payment.getPaymentStatus(), payment.getPaymentStatusDetail());
        return PaymentResultDecoratorFactory.createDecorator(vm);
    }

    public static PaymentResultViewModel createPaymentResultViewModel(@NonNull final String statusCode,
        @NonNull final String statusDetail) {
        return createPaymentResultViewModel(generatePaymentResult(statusCode, statusDetail));
    }

    private static PaymentResult generatePaymentResult(@NonNull final String statusCode,
        @NonNull final String statusDetail) {
        return new PaymentResult.Builder()
            .setPaymentStatus(statusCode)
            .setPaymentStatusDetail(statusDetail)
            .build();
    }

    public static PaymentResultViewModel createPaymentResultViewModel(@NonNull final PaymentResult paymentResult) {
        return createViewModelBuilder(paymentResult, null).build();
    }

    /**
     * We need payment information in order to return the correct description
     *
     * @param props body information
     */
    public static PaymentResultViewModel createPaymentStatusWithProps(@NonNull final String status,
        @NonNull final String detail, @Nullable final BodyErrorProps props) {
        return createViewModelBuilder(generatePaymentResult(status, detail), props).build();
    }

    @SuppressWarnings("fallthrough")
    private static PaymentResultViewModel.Builder createViewModelBuilder(@NonNull final PaymentResult paymentResult,
        @Nullable final BodyErrorProps props) {

        final String status = paymentResult.getPaymentStatus();
        final String detail = paymentResult.getPaymentStatusDetail();
        final String paymentMethodName = props == null ? TextUtil.EMPTY : props.paymentMethodName;
        final String paymentAmount = props == null ? null : props.paymentAmount;

        final PaymentResultViewModel.Builder builder = new PaymentResultViewModel.Builder();
        // defaults
        builder.setLinkAction(new NextAction());
        builder.setLinkActionTitle(R.string.px_continue_shopping);
        setApprovedResources(builder);

        switch (status) {
        case STATUS_APPROVED:
            return builder
                .setTitleResId(R.string.px_title_approved_payment)
                .setApprovedSuccess(true)
                .setLabelResId(EMPTY_LABEL);

        // Fallthrough pending & in process
        case STATUS_PENDING:
            builder.setLinkAction(new NextAction());
        case STATUS_IN_PROCESS:
            setPendingResources(builder, detail);
            return builder
                .setTitleResId(checkPaymentMethodsOff(status, detail))
                .setDescriptionResId(getPendingDescription(detail))
                .setLinkActionTitle(R.string.px_got_it)
                .setLabelResId(EMPTY_LABEL)
                .setApprovedSuccess(pendingStatusIsSuccess(detail))
                .setPendingSuccess(pendingStatusIsSuccess(detail))
                .setPendingWarning(!pendingStatusIsSuccess(detail))
                .setHasDetail(true);

        case STATUS_REJECTED:
            setRecoverableErrorResources(builder);
            // defaults
            builder.setLabelResId(R.string.px_rejection_label);
            builder.setMainAction(new ChangePaymentMethodAction());
            builder.setIsErrorRecoverable(true);
            builder.setMainActionTitle(R.string.px_text_pay_with_other_method);
            return rejectedStatusBuilder(detail, builder, paymentMethodName, paymentAmount);

        default:
            builder.setLabelResId(EMPTY_LABEL);
            return unknownStatusFallback(builder, status, detail);
        }
    }

    private static boolean pendingStatusIsSuccess(final String detail) {
        return STATUS_DETAIL_PENDING_WAITING_PAYMENT.equalsIgnoreCase(detail);
    }

    /**
     * Generate friction event when we receive an unknown status detail Payment information might be useful ToDo Add
     * payment information that might be useful
     */
    private static void generateFrictionEvent(final String statusCode, final String statusDetail) {

        final Map<String, String> metadata = new HashMap<>();
        // Add metadata values
        metadata.put("status_received", statusCode);
        metadata.put("status_detail", statusDetail);

        FrictionEventTracker.with(
            "/px_checkout/result",
            FrictionEventTracker.Id.INVALID_STATUS_DETAIL,
            FrictionEventTracker.Style.NON_SCREEN,
            metadata).track();
    }

    private static int checkPaymentMethodsOff(final String status, final String detail) {
        if (status.equalsIgnoreCase(STATUS_PENDING) && detail.equalsIgnoreCase(STATUS_DETAIL_PENDING_WAITING_PAYMENT)) {
            return EMPTY_LABEL;
        } else {
            return R.string.px_title_pending_payment;
        }
    }

    private static int getPendingDescription(final String detail) {
        switch (detail) {
        case Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY:
            return R.string.px_error_description_contingency;
        case Payment.StatusDetail.STATUS_DETAIL_PENDING_REVIEW_MANUAL:
            return R.string.px_error_description_review_manual;
        default:
            return EMPTY_LABEL;
        }
    }

    private static PaymentResultViewModel.Builder rejectedStatusBuilder(final String detail,
        final PaymentResultViewModel.Builder builder, final String paymentMethodName,
        final String paymentAmount) {

        if (!Payment.StatusDetail.isKnownStatusDetail(detail)) {
            return unknownStatusFallback(builder, STATUS_REJECTED, detail);
        }

        switch (detail) {

        case STATUS_DETAIL_CC_REJECTED_PLUGIN_PM:
        case STATUS_DETAIL_CC_REJECTED_OTHER_REASON:
            setNonRecoverableErrorResources(builder);
            return builder
                .setTitleResId(R.string.px_title_other_reason_rejection)
                .setLinkAction(null)
                .setMainAction(new ChangePaymentMethodAction())
                .setMainActionTitle(R.string.px_change_payment)
                .setHasDetail(false);
        case STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT:
            setNonRecoverableErrorResources(builder);
            return builder
                .setTitleResId(R.string.px_title_duplicated_reason_rejection)
                .setHasDetail(true)
                .setMainAction(null)
                .setLinkAction(new NextAction())
                .setDescriptionResId(R.string.px_error_description_duplicated_payment)
                .setLinkActionTitle(R.string.px_got_it);
        case STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT:
            setRecoverableErrorResources(builder);
            return builder
                .setTitleResId(R.string.px_text_insufficient_amount)
                .setLinkAction(null)
                .setMainActionTitle(R.string.px_change_payment)
                .setBodyTitleResId(R.string.px_what_can_do)
                .setBodyDetailDescriptionResId(R.string.px_text_insufficient_amount_title_description)
                .setDescriptionResId(R.string.px_error_description_rejected_by_insufficient_amount)
                .setHasDetail(true);
        case STATUS_DETAIL_CC_REJECTED_CARD_DISABLED:
            setRecoverableErrorResources(builder);
            return builder
                .setTitleResId(R.string.px_text_active_card)
                .setMainAction(new RecoverPaymentAction())
                .setMainActionTitle(R.string.px_text_card_enabled)
                .setLinkAction(new ChangePaymentMethodAction())
                .setLinkActionTitle(R.string.px_text_pay_with_other_method)
                .setDescriptionResId(R.string.px_error_description_card_disabled, paymentMethodName)
                .setBodyTitleResId(R.string.px_what_can_do)
                .setHasDetail(true);
        case STATUS_DETAIL_CC_REJECTED_HIGH_RISK:
            return getHighRiskBuilder(builder, R.string.px_title_rejection_high_risk);
        case STATUS_DETAIL_REJECTED_HIGH_RISK:
            return getHighRiskBuilder(builder, R.string.px_title_rejection_account_high_risk);
        case STATUS_DETAIL_REJECTED_BY_REGULATIONS:
            setNonRecoverableErrorResources(builder);
            return builder
                .setTitleResId(R.string.px_title_other_reason_rejection)
                .setMainAction(new ChangePaymentMethodAction())
                .setMainActionTitle(R.string.px_change_payment)
                .setLinkAction(null);
        case STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS:
            setNonRecoverableErrorResources(builder);
            return builder
                .setMainAction(new ChangePaymentMethodAction())
                .setMainActionTitle(R.string.px_change_payment)
                .setTitleResId(R.string.px_title_rejection_max_attempts)
                .setBodyTitleResId(R.string.px_what_can_do)
                .setDescriptionResId(R.string.px_error_description_max_attempts)
                .setLinkAction(null)
                .setHasDetail(true);
        case STATUS_DETAIL_CC_REJECTED_BLACKLIST:
            setNonRecoverableErrorResources(builder);
            return builder
                .setLinkAction(null)
                .setMainActionTitle(R.string.px_change_payment)
                .setHasDetail(false)
                .setTitleResId(R.string.px_title_rejection_blacklist);
        case STATUS_DETAIL_CC_REJECTED_FRAUD:
            setNonRecoverableErrorResources(builder);
            return builder
                .setLinkAction(null)
                .setMainActionTitle(R.string.px_continue_shopping)
                .setMainAction(new NextAction())
                .setTitleResId(R.string.px_title_rejection_fraud);
        case STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE:
            return builder
                .setTitleResId(R.string.px_title_activity_call_for_authorize)
                .setMainAction(new RecoverPaymentAction())
                .setMainActionTitle(R.string.px_text_authorized_call_for_authorize)
                .setLinkAction(new ChangePaymentMethodAction())
                .setLinkActionTitle(R.string.px_text_pay_with_other_method)
                .setBodyTitleResId(R.string.px_text_how_can_authorize)
                .setDescriptionResId(R.string.px_error_description_call, paymentAmount)
                .setHasDetail(true);

        case STATUS_DETAIL_REJECTED_REJECTED_BY_BANK:
        case STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA:
            setNonRecoverableErrorResources(builder);
            return builder
                .setTitleResId(R.string.px_bolbradesco_rejection)
                .setBodyTitleResId(R.string.px_what_can_do)
                .setDescriptionResId(getRejectedDescription(detail))
                .setHasDetail(true);

        case STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER:
        case STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER:
        case STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE:
        case STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE:
            return builder
                .setTitleResId(R.string.px_text_some_card_data_is_incorrect)
                .setMainAction(new RecoverPaymentAction())
                .setMainActionTitle(R.string.px_error_bad_filled_action)
                .setLinkAction(new ChangePaymentMethodAction())
                .setLinkActionTitle(R.string.px_text_pay_with_other_method);

        default:
            setNonRecoverableErrorResources(builder);
            return builder
                .setTitleResId(R.string.px_title_other_reason_rejection)
                .setPendingSuccess(false)
                .setPendingWarning(false)
                .setHasDetail(false);
        }
    }

    private static int getRejectedDescription(final String detail) {
        switch (detail) {
        case STATUS_DETAIL_REJECTED_REJECTED_BY_BANK:
            return R.string.px_error_description_by_bank;
        default:
            return R.string.px_error_description_insufficient_data;
        }
    }

    private static PaymentResultViewModel.Builder getHighRiskBuilder(final PaymentResultViewModel.Builder builder,
        final int resId) {
        setNonRecoverableErrorResources(builder);
        return builder
            .setTitleResId(resId)
            .setLinkAction(null)
            .setBodyTitleResId(R.string.px_what_can_do)
            .setDescriptionResId(R.string.px_error_description_high_risk)
            .setMainAction(new ChangePaymentMethodAction())
            .setMainActionTitle(R.string.px_change_payment)
            .setHasDetail(true);
    }

    private static void setApprovedResources(final PaymentResultViewModel.Builder builder) {
        builder
            .setBackgroundColor(R.color.ui_components_success_color)
            .setStatusBarResId(R.color.px_green_status_bar)
            .setBadgeResId(R.drawable.px_badge_check);
    }

    private static void setNonRecoverableErrorResources(@NonNull final PaymentResultViewModel.Builder builder) {
        builder
            .setIsErrorRecoverable(false)
            .setBadgeResId(R.drawable.px_badge_error)
            .setBackgroundColor(R.color.ui_components_error_color)
            .setStatusBarResId(R.color.px_red_status_bar);
    }

    private static void setRecoverableErrorResources(@NonNull final PaymentResultViewModel.Builder builder) {
        builder
            .setIsErrorRecoverable(true)
            .setBadgeResId(R.drawable.px_badge_warning)
            .setBackgroundColor(R.color.ui_components_warning_color)
            .setStatusBarResId(R.color.px_orange_status_bar);
    }

    private static void setPendingResources(@NonNull final PaymentResultViewModel.Builder builder,
        @NonNull final String statusDetail) {
        if (pendingStatusIsSuccess(statusDetail)) {
            builder
                .setBackgroundColor(R.color.ui_components_success_color)
                .setStatusBarResId(R.color.px_green_status_bar)
                .setBadgeResId(R.drawable.px_badge_pending);
        } else {
            builder
                .setBadgeResId(R.drawable.px_badge_pending_orange)
                .setBackgroundColor(R.color.ui_components_warning_color)
                .setStatusBarResId(R.color.px_orange_status_bar);
        }
    }

    private static PaymentResultViewModel.Builder unknownStatusFallback(final PaymentResultViewModel.Builder builder,
        final String status, final String detail) {

        // Generate a friction event with status info
        generateFrictionEvent(status, detail);

        setNonRecoverableErrorResources(builder);
        return builder
            .setTitleResId(R.string.px_title_other_reason_rejection)
            .setLabelResId(EMPTY_LABEL)
            .setMainAction(new ChangePaymentMethodAction())
            .setMainActionTitle(R.string.px_change_payment)
            .setLinkAction(null)
            .setHasDetail(false);
    }
}