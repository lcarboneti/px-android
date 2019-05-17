package com.mercadopago.android.px.model;

import android.support.annotation.Nullable;
import java.util.Date;

@SuppressWarnings("UseOfObsoleteDateTimeApi")
public final class Card implements CardInformation {

    public static final int CARD_DEFAULT_IDENTIFICATION_NUMBER_LENGTH = 12;
    public static final int CARD_DEFAULT_SECURITY_CODE_LENGTH = 4;
    private static final String CARD_DEFAULT_SECURITY_CODE_LOCATION = "back";
    public static final Integer CARD_NUMBER_MAX_LENGTH = 16;

    private Cardholder cardHolder;
    private String customerId;
    private Date dateCreated;
    private Date dateLastUpdated;
    private Integer expirationMonth;
    private Integer expirationYear;
    private String firstSixDigits;
    private String id;
    private Issuer issuer;
    private String lastFourDigits;
    private PaymentMethod paymentMethod;
    private SecurityCode securityCode;

    @Deprecated
    public Card() {
    }

    @Override
    public Cardholder getCardHolder() {
        return cardHolder;
    }

    @Deprecated
    public void setCardHolder(final Cardholder cardHolder) {
        this.cardHolder = cardHolder;
    }

    @Deprecated
    public String getCustomerId() {
        return customerId;
    }

    @Deprecated
    public void setCustomerId(final String customerId) {
        this.customerId = customerId;
    }

    @Deprecated
    public Date getDateCreated() {
        return dateCreated;
    }

    @Deprecated
    public void setDateCreated(final Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Deprecated
    public Date getDateLastUpdated() {
        return dateLastUpdated;
    }

    @Deprecated
    public void setDateLastUpdated(final Date dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    @Override
    public Integer getExpirationMonth() {
        return expirationMonth;
    }

    @Deprecated
    public void setExpirationMonth(final Integer expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    @Override
    public Integer getExpirationYear() {
        return expirationYear;
    }

    @Deprecated
    public void setExpirationYear(final Integer expirationYear) {
        this.expirationYear = expirationYear;
    }

    @Override
    public String getFirstSixDigits() {
        return firstSixDigits;
    }

    @Deprecated
    public void setFirstSixDigits(final String firstSixDigits) {
        this.firstSixDigits = firstSixDigits;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Nullable
    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(@Nullable final Issuer issuer) {
        this.issuer = issuer;
    }

    @Nullable
    @Override
    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(@Nullable final String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(@Nullable final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public SecurityCode getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(@Nullable final SecurityCode securityCode) {
        this.securityCode = securityCode;
    }

    public boolean isSecurityCodeRequired() {
        return securityCode != null && securityCode.getLength() != 0;
    }

    @Override
    public Integer getSecurityCodeLength() {
        return securityCode != null ? securityCode.getLength() : CARD_DEFAULT_SECURITY_CODE_LENGTH;
    }

    public String getSecurityCodeLocation() {
        return securityCode != null ? securityCode.getCardLocation() : CARD_DEFAULT_SECURITY_CODE_LOCATION;
    }

    @SuppressWarnings("ObjectToString")
    @Override
    public String toString() {
        return "Card{" +
            "cardHolder=" + cardHolder +
            ", customerId='" + customerId + '\'' +
            ", dateCreated=" + dateCreated +
            ", dateLastUpdated=" + dateLastUpdated +
            ", expirationMonth=" + expirationMonth +
            ", expirationYear=" + expirationYear +
            ", firstSixDigits='" + firstSixDigits + '\'' +
            ", id='" + id + '\'' +
            ", issuer=" + issuer +
            ", lastFourDigits='" + lastFourDigits + '\'' +
            ", paymentMethod=" + paymentMethod +
            ", securityCode=" + securityCode +
            '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }

        final Card card = (Card) o;

        return id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}