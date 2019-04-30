package com.mercadopago.android.px.preferences;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.PaymentTypes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PaymentPreference implements Serializable, Parcelable {

    public static final Creator<PaymentPreference> CREATOR = new Creator<PaymentPreference>() {
        @Override
        public PaymentPreference createFromParcel(final Parcel in) {
            return new PaymentPreference(in);
        }

        @Override
        public PaymentPreference[] newArray(final int size) {
            return new PaymentPreference[size];
        }
    };

    private List<PaymentMethod> excludedPaymentMethods;
    private List<PaymentType> excludedPaymentTypes;
    private String defaultPaymentTypeId;
    private String defaultPaymentMethodId;
    private String defaultCardId;
    @SerializedName("installments")
    private Integer maxInstallments;
    private Integer defaultInstallments;


    public Integer getMaxInstallments() {
        return maxInstallments;
    }

    @NonNull
    public List<String> getExcludedPaymentMethodIds() {
        if (excludedPaymentMethods != null) {
            final List<String> excludedPaymentMethodIds = new ArrayList<>();
            for (final PaymentMethod paymentMethod : excludedPaymentMethods) {
                excludedPaymentMethodIds.add(paymentMethod.getId());
            }
            return excludedPaymentMethodIds;
        } else {
            return new ArrayList<>();
        }
    }

    public Integer getDefaultInstallments() {
        return defaultInstallments;
    }

    @Deprecated
    public void setExcludedPaymentMethodIds(final List<String> excludedPaymentMethodIds) {
        if (excludedPaymentMethodIds != null) {
            excludedPaymentMethods = new ArrayList<>();
            for (final String paymentMethodId : excludedPaymentMethodIds) {
                final PaymentMethod excludedPaymentMethod = new PaymentMethod(paymentMethodId);
                excludedPaymentMethods.add(excludedPaymentMethod);
            }
        }
    }

    @NonNull
    public List<String> getExcludedPaymentTypes() {
        if (excludedPaymentTypes != null) {
            final List<String> excludedPaymentTypeIds = new ArrayList<>();
            for (final PaymentType paymentType : excludedPaymentTypes) {
                excludedPaymentTypeIds.add(paymentType.getId());
            }
            return excludedPaymentTypeIds;
        } else {
            return new ArrayList<>();
        }
    }

    @Nullable
    public String getDefaultPaymentMethodId() {
        return defaultPaymentMethodId;
    }

    public String getDefaultPaymentTypeId() {
        return defaultPaymentTypeId;
    }

    public List<PayerCost> getInstallmentsBelowMax(final List<PayerCost> payerCosts) {
        final List<PayerCost> validPayerCosts = new ArrayList<>();

        if (maxInstallments != null) {
            for (final PayerCost currentPayerCost : payerCosts) {
                if (currentPayerCost.getInstallments() <= maxInstallments) {
                    validPayerCosts.add(currentPayerCost);
                }
            }
            return validPayerCosts;
        } else {
            return payerCosts;
        }
    }

    public boolean installmentPreferencesValid() {
        return validDefaultInstallments() && validMaxInstallments();
    }

    public boolean excludedPaymentTypesValid() {
        return excludedPaymentTypes == null
            || excludedPaymentTypes.size() < PaymentTypes.getAllPaymentTypes().size();
    }

    public boolean validDefaultInstallments() {
        return defaultInstallments == null || defaultInstallments > 0;
    }

    public boolean validMaxInstallments() {
        return maxInstallments == null || maxInstallments > 0;
    }

    @Deprecated
    public void setMaxAcceptedInstallments(final Integer installments) {
        maxInstallments = installments;
    }

    @Deprecated
    public void setDefaultInstallments(final Integer defaultInstallments) {
        this.defaultInstallments = defaultInstallments;
    }

    @Deprecated
    public void setExcludedPaymentTypeIds(final List<String> excludedPaymentTypeIds) {
        if (excludedPaymentTypeIds != null) {
            excludedPaymentTypes = new ArrayList<>();
            for (final String paymentTypeId : excludedPaymentTypeIds) {
                final PaymentType excludedPaymentType = new PaymentType(paymentTypeId);
                excludedPaymentTypes.add(excludedPaymentType);
            }
        }
    }

    @Deprecated
    public void setDefaultPaymentMethodId(final String defaultPaymentMethodId) {
        this.defaultPaymentMethodId = defaultPaymentMethodId;
    }

    @Deprecated
    public void setDefaultPaymentTypeId(final String defaultPaymentTypeId) {
        this.defaultPaymentTypeId = defaultPaymentTypeId;
    }

    //TODO move this logic.
    @Deprecated
    @Nullable
    public PayerCost getDefaultInstallments(final List<PayerCost> payerCosts) {
        for (final PayerCost currentPayerCost : payerCosts) {
            if (currentPayerCost.getInstallments().equals(defaultInstallments)) {
                return currentPayerCost;
            }
        }
        return null;
    }

    //TODO move this logic.
    @Deprecated
    public List<PaymentMethod> getSupportedPaymentMethods(final List<PaymentMethod> paymentMethods) {
        final List<PaymentMethod> supportedPaymentMethods = new ArrayList<>();
        if (paymentMethods != null) {
            for (final PaymentMethod paymentMethod : paymentMethods) {
                if (isPaymentMethodSupported(paymentMethod)) {
                    supportedPaymentMethods.add(paymentMethod);
                }
            }
        }
        return supportedPaymentMethods;
    }

    //TODO move this logic
    @Deprecated
    public boolean isPaymentMethodSupported(final PaymentMethod paymentMethod) {
        boolean isSupported = true;
        if (paymentMethod == null) {
            isSupported = false;
        } else {
            final List<String> excludedPaymentMethodIds = getExcludedPaymentMethodIds();
            final List<String> excludedPaymentTypes = getExcludedPaymentTypes();

            if (excludedPaymentMethodIds.contains(paymentMethod.getId()) ||
                excludedPaymentTypes.contains(paymentMethod.getPaymentTypeId())) {
                isSupported = false;
            }
        }
        return isSupported;
    }

    //TODO move this logic
    @Nullable
    @Deprecated
    public PaymentMethod getDefaultPaymentMethod(final List<PaymentMethod> paymentMethods) {
        if (defaultPaymentMethodId != null && paymentMethods != null) {
            for (final PaymentMethod pm : paymentMethods) {
                if (pm.getId().equals(defaultPaymentMethodId)) {
                    return pm;
                }
            }
        }
        return null;
    }

    //TODO move this logic
    @Deprecated
    public List<Card> getValidCards(final List<Card> cards) {
        final List<Card> supportedCards = new ArrayList<>();
        if (cards != null) {
            for (final Card card : cards) {
                if (isPaymentMethodSupported(card.getPaymentMethod())) {
                    supportedCards.add(card);
                }
            }
        }
        return supportedCards;
    }

    @Deprecated
    public void setDefaultCardId(final String defaultCardId) {
        this.defaultCardId = defaultCardId;
    }

    @Nullable
    public String getDefaultCardId() {
        return defaultCardId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        if (maxInstallments == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(maxInstallments);
        }
        if (defaultInstallments == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(defaultInstallments);
        }
        dest.writeTypedList(excludedPaymentMethods);
        dest.writeTypedList(excludedPaymentTypes);
        dest.writeString(defaultPaymentMethodId);
        dest.writeString(defaultCardId);
        dest.writeString(defaultPaymentTypeId);
    }

    @Deprecated
    public PaymentPreference() {
    }

    protected PaymentPreference(final Parcel in) {
        if (in.readByte() == 0) {
            maxInstallments = null;
        } else {
            maxInstallments = in.readInt();
        }
        if (in.readByte() == 0) {
            defaultInstallments = null;
        } else {
            defaultInstallments = in.readInt();
        }
        excludedPaymentMethods = in.createTypedArrayList(PaymentMethod.CREATOR);
        excludedPaymentTypes = in.createTypedArrayList(PaymentType.CREATOR);
        defaultPaymentMethodId = in.readString();
        defaultCardId = in.readString();
        defaultPaymentTypeId = in.readString();
    }
}
