package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import junit.framework.Test;

public final class Payment implements IPayment, IPaymentDescriptor {

    private Boolean binaryMode;
    private String callForAuthorizeId;
    private Boolean captured;
    private Card card;
    private long collectorId;
    private BigDecimal couponAmount;
    private String currencyId;
    private Date dateApproved;
    private Date dateCreated;
    private Date dateLastUpdated;
    private String description;
    private Long differentialPricingId;
    private String externalReference;
    private List<FeeDetail> feeDetails;
    private Long id;
    private Integer installments;
    private String issuerId;
    private Boolean liveMode;
    private Map metadata;
    private Date moneyReleaseDate;
    private String notificationUrl;
    private String operationType;
    private Order order;
    private Payer payer;
    private String paymentMethodId;
    private String paymentTypeId;
    private List<Refund> refunds;
    private String statementDescriptor;
    private String status;
    private String statusDetail;
    private BigDecimal transactionAmount;
    private BigDecimal transactionAmountRefunded;
    private TransactionDetails transactionDetails;

    public Payment() {
        //Gson
    }

    public Payment(@NonNull final String status, @NonNull final String statusDetail) {
        this.status = status;
        this.statusDetail = statusDetail;
    }

    public Boolean getBinaryMode() {
        return binaryMode;
    }

    public void setBinaryMode(Boolean binaryMode) {
        this.binaryMode = binaryMode;
    }

    public String getCallForAuthorizeId() {
        return callForAuthorizeId;
    }

    public void setCallForAuthorizeId(String callForAuthorizeId) {
        this.callForAuthorizeId = callForAuthorizeId;
    }

    public Boolean getCaptured() {
        return captured;
    }

    public void setCaptured(Boolean captured) {
        this.captured = captured;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Long getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(Long collectorId) {
        this.collectorId = collectorId;
    }

    public BigDecimal getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(BigDecimal couponAmount) {
        this.couponAmount = couponAmount;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public Date getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(Date dateApproved) {
        this.dateApproved = dateApproved;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(Date dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDifferentialPricingId() {
        return differentialPricingId;
    }

    public void setDifferentialPricingId(Long differentialPricingId) {
        this.differentialPricingId = differentialPricingId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public List<FeeDetail> getFeeDetails() {
        return feeDetails;
    }

    public void setFeeDetails(List<FeeDetail> feeDetails) {
        this.feeDetails = feeDetails;
    }

    @Override
    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public Boolean getLiveMode() {
        return liveMode;
    }

    public void setLiveMode(Boolean liveMode) {
        this.liveMode = liveMode;
    }

    public Map getMetadata() {
        return metadata;
    }

    public void setMetadata(Map metadata) {
        this.metadata = metadata;
    }

    public Date getMoneyReleaseDate() {
        return moneyReleaseDate;
    }

    public void setMoneyReleaseDate(Date moneyReleaseDate) {
        this.moneyReleaseDate = moneyReleaseDate;
    }

    public String getNotificationUrl() {
        return notificationUrl;
    }

    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(Payer payer) {
        this.payer = payer;
    }

    @NonNull
    @Override
    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    @NonNull
    @Override
    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    @Override
    public void process(@NonNull final IPaymentDescriptorHandler handler) {
        handler.visit(this);
    }

    @Deprecated
    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    @Deprecated
    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public List<Refund> getRefunds() {
        return refunds;
    }

    @Deprecated
    public void setRefunds(List<Refund> refunds) {
        this.refunds = refunds;
    }

    @Deprecated
    public String getStatementDescriptor() {
        return statementDescriptor;
    }

    @Deprecated
    public void setStatementDescriptor(String statementDescriptor) {
        this.statementDescriptor = statementDescriptor;
    }

    @Deprecated
    public void setStatus(String status) {
        this.status = status;
    }

    @Deprecated
    public void setStatusDetail(String statusDetail) {
        this.statusDetail = statusDetail;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    @Deprecated
    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public BigDecimal getTransactionAmountRefunded() {
        return transactionAmountRefunded;
    }

    @Deprecated
    public void setTransactionAmountRefunded(BigDecimal transactionAmountRefunded) {
        this.transactionAmountRefunded = transactionAmountRefunded;
    }

    public TransactionDetails getTransactionDetails() {
        return transactionDetails;
    }

    @Deprecated
    public void setTransactionDetails(TransactionDetails transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public Boolean isCardPaymentType(String paymentTypeId) {
        return paymentTypeId.equals(PaymentTypes.CREDIT_CARD)
            || paymentTypeId.equals(PaymentTypes.DEBIT_CARD)
            || paymentTypeId.equals(PaymentTypes.PREPAID_CARD);
    }

    public static boolean isPendingStatus(final String status, final String statusDetail) {
        return StatusCodes.STATUS_PENDING.equals(status) &&
            StatusDetail.STATUS_DETAIL_PENDING_WAITING_PAYMENT.equals(statusDetail);
    }

    @Override
    public String toString() {
        return "Payment{" +
            "binaryMode=" + binaryMode +
            ", callForAuthorizeId='" + callForAuthorizeId + '\'' +
            ", captured=" + captured +
            ", card=" + card +
            ", collectorId=" + collectorId +
            ", couponAmount=" + couponAmount +
            ", currencyId='" + currencyId + '\'' +
            ", dateApproved=" + dateApproved +
            ", dateCreated=" + dateCreated +
            ", dateLastUpdated=" + dateLastUpdated +
            ", description='" + description + '\'' +
            ", differentialPricingId=" + differentialPricingId +
            ", externalReference='" + externalReference + '\'' +
            ", feeDetails=" + feeDetails +
            ", id=" + id +
            ", installments=" + installments +
            ", issuerId='" + issuerId + '\'' +
            ", liveMode=" + liveMode +
            ", metadata=" + metadata +
            ", moneyReleaseDate=" + moneyReleaseDate +
            ", notificationUrl='" + notificationUrl + '\'' +
            ", operationType='" + operationType + '\'' +
            ", order=" + order +
            ", payer=" + payer +
            ", paymentMethodId='" + paymentMethodId + '\'' +
            ", paymentTypeId='" + paymentTypeId + '\'' +
            ", refunds=" + refunds +
            ", statementDescriptor='" + statementDescriptor + '\'' +
            ", status='" + status + '\'' +
            ", statusDetail='" + statusDetail + '\'' +
            ", transactionAmount=" + transactionAmount +
            ", transactionAmountRefunded=" + transactionAmountRefunded +
            ", transactionDetails=" + transactionDetails +
            '}';
    }

    @Nullable
    @Override
    public String getStatementDescription() {
        return statementDescriptor;
    }

    @NonNull
    @Override
    public String getPaymentStatus() {
        return status;
    }

    @NonNull
    @Override
    public String getPaymentStatusDetail() {
        return statusDetail;
    }

    public static class StatusCodes {
        public static final String STATUS_APPROVED = "approved";
        public static final String STATUS_IN_PROCESS = "in_process";
        public static final String STATUS_REJECTED = "rejected";
        public static final String STATUS_PENDING = "pending";
    }

    public static class StatusDetail {
        /**
         * @deprecated TODO
         */
        @Deprecated
        public static final String STATUS_DETAIL_APPROVED_PLUGIN_PM = "approved_plugin_pm";
        /**
         * @deprecated TODO
         */
        @Deprecated
        public static final String STATUS_DETAIL_CC_REJECTED_PLUGIN_PM = "cc_rejected_plugin_pm";
        public static final String STATUS_DETAIL_ACCREDITED = "accredited";
        public static final String STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE = "cc_rejected_call_for_authorize";

        public static final String STATUS_DETAIL_PENDING_CONTINGENCY = "pending_contingency";
        public static final String STATUS_DETAIL_PENDING_REVIEW_MANUAL = "pending_review_manual";
        public static final String STATUS_DETAIL_PENDING_WAITING_PAYMENT = "pending_waiting_payment";
        public static final String STATUS_DETAIL_CC_REJECTED_OTHER_REASON = "cc_rejected_other_reason";

        public static final String STATUS_DETAIL_INVALID_ESC = "invalid_esc";
        public static final String STATUS_DETAIL_CC_REJECTED_CARD_DISABLED = "cc_rejected_card_disabled";
        public static final String STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT = "cc_rejected_insufficient_amount";
        public static final String STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER = "cc_rejected_bad_filled_other";
        public static final String STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER =
            "cc_rejected_bad_filled_card_number";
        public static final String STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE =
            "cc_rejected_bad_filled_security_code";
        public static final String STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE = "cc_rejected_bad_filled_date";
        public static final String STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT = "cc_rejected_duplicated_payment";
        public static final String STATUS_DETAIL_CC_REJECTED_HIGH_RISK = "cc_rejected_high_risk";
        public static final String STATUS_DETAIL_REJECTED_HIGH_RISK = "rejected_high_risk";
        public static final String STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS = "cc_rejected_max_attempts";
        public static final String STATUS_DETAIL_REJECTED_REJECTED_BY_BANK = "rejected_by_bank";
        public static final String STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA = "rejected_insufficient_data";
        public static final String STATUS_DETAIL_REJECTED_BY_REGULATIONS = "rejected_by_regulations";
        public static final String STATUS_DETAIL_CC_REJECTED_FRAUD = "cc_rejected_fraud";
        public static final String STATUS_DETAIL_CC_REJECTED_BLACKLIST = "cc_rejected_blacklist";

        // Return all the static declared fields as a collection
        private static Collection<Field> getAll() {
            final Field[] declaredFields = StatusDetail.class.getDeclaredFields();
            final Collection<Field> staticFields = new ArrayList<>();
            for (final Field field : declaredFields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    staticFields.add(field);
                }
            }

            return staticFields;
        }

        // Return all the static declared fields as a String collection
        public static Collection<String> getAllStaticFields() {
            final Collection<String> statusList = new ArrayList<>();
            for (final Field status : getAll()) {
                statusList.add(status.getName());
            }

            return statusList;
        }

        public static boolean isKnownErrorDetail(final String statusDetail) {
            return STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_CARD_DISABLED.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS.equals(statusDetail)
                || STATUS_DETAIL_INVALID_ESC.equals(statusDetail)
                || STATUS_DETAIL_REJECTED_HIGH_RISK.equals(statusDetail)
                || STATUS_DETAIL_REJECTED_REJECTED_BY_BANK.equals(statusDetail)
                || STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA.equals(statusDetail)
                || STATUS_DETAIL_REJECTED_BY_REGULATIONS.equals(statusDetail);
        }

        public static boolean isKnownStatusDetail(final String statusDetail) {
            boolean knownError = false;

            for (final String status : Payment.StatusDetail.getAllStaticFields()) {
                if (status.toLowerCase().contains(statusDetail.toLowerCase())) {
                    knownError = true;
                    break;
                }
            }

            return knownError;
        }
    
        public static boolean isPaymentStatusRecoverable(final String statusDetail) {
            return STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER.equals(statusDetail) ||
                STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER.equals(statusDetail) ||
                STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE.equals(statusDetail) ||
                STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE.equals(statusDetail) ||
                STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE.equals(statusDetail) ||
                STATUS_DETAIL_INVALID_ESC.equals(statusDetail) ||
                STATUS_DETAIL_CC_REJECTED_CARD_DISABLED.equals(statusDetail);
        }

        public static boolean isStatusDetailRecoverable(final String statusDetail) {
            return (STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE.equals(statusDetail) ||
                STATUS_DETAIL_INVALID_ESC.equals(statusDetail) ||
                STATUS_DETAIL_CC_REJECTED_CARD_DISABLED.equals(statusDetail));
        }

        public static boolean isRecoverablePaymentStatus(final String paymentStatus, final String paymentStatusDetail) {
            return Payment.StatusCodes.STATUS_REJECTED.equals(paymentStatus)
                && isPaymentStatusRecoverable(paymentStatusDetail);
        }

        public static boolean isBadFilled(final String statusDetail) {
            return STATUS_DETAIL_CC_REJECTED_BAD_FILLED_DATE.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_BAD_FILLED_SECURITY_CODE.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_BAD_FILLED_OTHER.equals(statusDetail)
                || STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER.equals(statusDetail);
        }

        public static boolean isPendingWithDetail(@Nullable final String statusDetail) {
            return STATUS_DETAIL_PENDING_CONTINGENCY.equals(statusDetail)
                || STATUS_DETAIL_PENDING_REVIEW_MANUAL.equals(statusDetail);
        }

        public static boolean isRejectedWithDetail(@NonNull final String statusDetail) {
            switch (statusDetail) {
            case STATUS_DETAIL_CC_REJECTED_INSUFFICIENT_AMOUNT:
            case STATUS_DETAIL_REJECTED_BY_REGULATIONS:
            case STATUS_DETAIL_CC_REJECTED_CARD_DISABLED:
            case STATUS_DETAIL_CC_REJECTED_CALL_FOR_AUTHORIZE:
            case STATUS_DETAIL_REJECTED_HIGH_RISK:
            case STATUS_DETAIL_CC_REJECTED_MAX_ATTEMPTS:
            case STATUS_DETAIL_CC_REJECTED_DUPLICATED_PAYMENT:
            case STATUS_DETAIL_REJECTED_REJECTED_INSUFFICIENT_DATA:
            case STATUS_DETAIL_REJECTED_REJECTED_BY_BANK:
            case STATUS_DETAIL_CC_REJECTED_OTHER_REASON:
                return true;
            default:
                return false;
            }
        }
    }
}