package com.mercadopago.android.px.internal.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.PaymentDataHelper;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentTypes;
import java.util.Locale;
import javax.annotation.Nonnull;

public class PaymentMethodComponent extends CompactComponent<PaymentMethodComponent.PaymentMethodProps, Void> {

    public static class PaymentMethodProps implements Parcelable {

        /* default */ final TotalAmount.Props totalAmountProps;
        @Nullable
        /* default */ final String lastFourDigits;
        @Nullable
        /* default */ final String disclaimer;

        /* default */ final PaymentMethod paymentMethod;

        private PaymentMethodProps(final PaymentMethod paymentMethod,
            @Nullable final String lastFourDigits,
            @Nullable final String disclaimer,
            final TotalAmount.Props totalAmountProps) {
            this.paymentMethod = paymentMethod;
            this.lastFourDigits = lastFourDigits;
            this.disclaimer = disclaimer;
            this.totalAmountProps = totalAmountProps;
        }

        public static PaymentMethodProps with(@NonNull final PaymentData paymentData,
            @NonNull final String currencyId,
            @NonNull final String statementDescription) {
            final TotalAmount.Props totalAmountProps =
                new TotalAmount.Props(currencyId, PaymentDataHelper.getPrettyAmountToPay(paymentData),
                    paymentData.getPayerCost());

            return new PaymentMethodComponent.PaymentMethodProps(paymentData.getPaymentMethod(),
                paymentData.getToken() != null ? paymentData.getToken().getLastFourDigits() : null,
                statementDescription,
                totalAmountProps);
        }

        protected PaymentMethodProps(final Parcel in) {
            paymentMethod = in.readParcelable(PaymentMethod.class.getClassLoader());
            totalAmountProps = in.readParcelable(TotalAmount.Props.class.getClassLoader());
            lastFourDigits = in.readString();
            disclaimer = in.readString();
        }

        public static final Creator<PaymentMethodProps> CREATOR = new Creator<PaymentMethodProps>() {
            @Override
            public PaymentMethodProps createFromParcel(final Parcel in) {
                return new PaymentMethodProps(in);
            }

            @Override
            public PaymentMethodProps[] newArray(final int size) {
                return new PaymentMethodProps[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeParcelable(paymentMethod, flags);
            dest.writeParcelable(totalAmountProps, flags);
            dest.writeString(lastFourDigits);
            dest.writeString(disclaimer);
        }
    }

    public PaymentMethodComponent(@NonNull final PaymentMethodProps props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final Context context = parent.getContext();
        final View paymentMethodView = ViewUtils.inflate(parent, R.layout.px_payment_method_component);
        final ImageView imageView = paymentMethodView.findViewById(R.id.mpsdkPaymentMethodIcon);
        final MPTextView descriptionTextView = paymentMethodView.findViewById(R.id.mpsdkPaymentMethodDescription);
        final MPTextView statementDescriptionTextView = paymentMethodView.findViewById(R.id.mpsdkStatementDescription);

        addTotalAmountContainer(this, context, paymentMethodView);

        imageView.setImageDrawable(
            ContextCompat.getDrawable(context, ResourceUtil.getIconResource(context, props.paymentMethod.getId())));
        descriptionTextView
            .setText(getDescription(props.paymentMethod.getName(), props.paymentMethod.getPaymentTypeId(),
                props.lastFourDigits, context));
        statementDescriptionTextView.setText(getDisclaimer(props.paymentMethod.getPaymentTypeId(),
            props.disclaimer, context));
        return paymentMethodView;
    }

    private void addTotalAmountContainer(@NonNull final PaymentMethodComponent component,
        @NonNull final Context context,
        final View paymentMethodView) {
        final FrameLayout totalAmountContainer = paymentMethodView.findViewById(R.id.mpsdkTotalAmountContainer);
        RendererFactory.create(context, getTotalAmountComponent(component.props.totalAmountProps))
            .render(totalAmountContainer);
    }

    private Component getTotalAmountComponent(final TotalAmount.Props props) {
        return new TotalAmount(props);
    }

    @VisibleForTesting
    @NonNull
    String getDisclaimer(final String paymentMethodTypeId, final String disclaimer, final Context context) {
        if (PaymentTypes.isCardPaymentType(paymentMethodTypeId) && TextUtil.isNotEmpty(disclaimer)) {
            return String.format(context.getString(R.string.px_text_state_account_activity_congrats), disclaimer);
        }
        return "";
    }

    @VisibleForTesting
    @NonNull
    String getDescription(final String paymentMethodName,
        final String paymentMethodType,
        final String lastFourDigits,
        final Context context) {
        if (PaymentTypes.isCardPaymentType(paymentMethodType)) {
            return String.format(Locale.getDefault(), "%s %s %s",
                paymentMethodName,
                context.getString(R.string.px_ending_in),
                lastFourDigits);
        } else {
            return paymentMethodName;
        }
    }
}
