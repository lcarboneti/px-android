package com.mercadopago.android.px.internal.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.util.CurrenciesUtil;
import com.mercadopago.android.px.internal.util.ParcelableUtil;
import com.mercadopago.android.px.model.PayerCost;
import java.math.BigDecimal;
import java.util.Locale;

public class TotalAmount extends Component<TotalAmount.Props, Void> {

    static {
        RendererFactory.register(TotalAmount.class, TotalAmountRenderer.class);
    }

    public static class Props implements Parcelable {

        public final PayerCost payerCost;
        public final String currencyId;
        public final BigDecimal amount;

        public Props(final String currencyId,
            final BigDecimal amount,
            final PayerCost payerCost) {
            this.payerCost = payerCost;
            this.currencyId = currencyId;
            this.amount = amount;
        }

        protected Props(final Parcel in) {
            payerCost = in.readParcelable(PayerCost.class.getClassLoader());
            currencyId = in.readString();
            amount = ParcelableUtil.getBigDecimal(in);
        }

        public static final Creator<Props> CREATOR = new Creator<Props>() {
            @Override
            public Props createFromParcel(final Parcel in) {
                return new Props(in);
            }

            @Override
            public Props[] newArray(final int size) {
                return new Props[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeParcelable(payerCost, flags);
            dest.writeString(currencyId);
            ParcelableUtil.write(dest, amount);
        }
    }

    public TotalAmount(@NonNull final Props props) {
        super(props);
    }

    public String getAmountTitle() {
        final String amountTitle;

        if (hasPayerCostWithMultipleInstallments()) {
            final String installmentsAmount = CurrenciesUtil
                .getLocalizedAmountWithoutZeroDecimals(props.currencyId, props.payerCost.getInstallmentAmount());
            amountTitle = String.format(Locale.getDefault(),
                "%dx %s",
                props.payerCost.getInstallments(),
                installmentsAmount);
        } else {
            amountTitle = CurrenciesUtil.getLocalizedAmountWithoutZeroDecimals(props.currencyId, props.amount);
        }

        return amountTitle;
    }

    public String getAmountDetail() {
        String amountDetail = "";

        if (hasPayerCostWithMultipleInstallments()) {
            amountDetail = String.format(Locale.getDefault(), "(%s)", CurrenciesUtil
                .getLocalizedAmountWithoutZeroDecimals(props.currencyId, props.payerCost.getTotalAmount()));
        }

        return amountDetail;
    }

    private boolean hasPayerCostWithMultipleInstallments() {
        return props.payerCost != null && props.payerCost.hasMultipleInstallments();
    }
}
