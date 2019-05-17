package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CustomSearchItem implements Serializable, Parcelable {

    //TODO make final when deprecate custom search item constructor.
    private String description;
    private String id;
    private String comment;

    //TODO change name / serialize name - signature V5
    @SerializedName("payment_type_id")
    private String type;
    private String paymentMethodId;

    @Nullable private String discountInfo;


    private String defaultAmountConfiguration;

    private Map<String, AmountConfiguration> amountConfigurations;

    @Nullable private Issuer issuer;
    @Nullable private String lastFourDigits;
    @Nullable private String firstSixDigits;

    @Deprecated
    public CustomSearchItem() {
        amountConfigurations = new HashMap<>();
    }

    protected CustomSearchItem(final Parcel in) {
        description = in.readString();
        id = in.readString();
        type = in.readString();
        comment = in.readString();
        paymentMethodId = in.readString();
        discountInfo = in.readString();
        defaultAmountConfiguration = in.readString();
        amountConfigurations = new HashMap<>();
        in.readMap(amountConfigurations, CustomSearchItem.class.getClassLoader());
        lastFourDigits = in.readString();
        firstSixDigits = in.readString();
        issuer = in.readParcelable(Issuer.class.getClassLoader());
    }

    public static final Creator<CustomSearchItem> CREATOR = new Creator<CustomSearchItem>() {
        @Override
        public CustomSearchItem createFromParcel(final Parcel in) {
            return new CustomSearchItem(in);
        }

        @Override
        public CustomSearchItem[] newArray(final int size) {
            return new CustomSearchItem[size];
        }
    };

    public String getDescription() {
        return description;
    }

    @Nullable
    public String getDiscountInfo() {
        return discountInfo;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getComment() {
        return comment;
    }

    public String getDefaultAmountConfiguration() {
        return defaultAmountConfiguration;
    }

    public AmountConfiguration getAmountConfiguration(final String key) {
        return amountConfigurations.get(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(paymentMethodId);
        dest.writeString(comment);
        dest.writeString(discountInfo);
        dest.writeString(defaultAmountConfiguration);
        dest.writeMap(amountConfigurations);
        dest.writeString(lastFourDigits);
        dest.writeString(firstSixDigits);
        dest.writeParcelable(issuer, 0);
    }

    @Deprecated
    public void setDescription(final String description) {
        this.description = description;
    }

    @Deprecated
    public void setId(final String id) {
        this.id = id;
    }

    @Deprecated
    public void setType(final String type) {
        this.type = type;
    }

    @Deprecated
    public void setPaymentMethodId(final String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public boolean hasDiscountInfo() {
        return !TextUtils.isEmpty(discountInfo);
    }

    @Nullable
    public Issuer getIssuer() {
        return issuer;
    }

    @Nullable
    public String getLastFourDigits() {
        return lastFourDigits;
    }

    @Nullable
    public String getFirstSixDigits() {
        return firstSixDigits;
    }
}
