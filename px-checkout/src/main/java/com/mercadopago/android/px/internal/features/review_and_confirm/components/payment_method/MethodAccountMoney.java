package com.mercadopago.android.px.internal.features.review_and_confirm.components.payment_method;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.PaymentModel;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.internal.InitResponse;
import com.mercadopago.android.px.services.Callback;

/* default */ class MethodAccountMoney extends CompactComponent<MethodAccountMoney.Props, Void> {

    /* default */ static final class Props {

        /* default */ final String paymentMethodId;
        /* default */ final String title;

        private Props(@NonNull final String paymentMethodId, @NonNull final String title) {
            this.paymentMethodId = paymentMethodId;
            this.title = title;
        }

        /* default */
        static Props createFrom(final PaymentModel props) {
            return new Props(props.paymentMethodId,
                props.paymentMethodName);
        }
    }

    /* default */ MethodAccountMoney(final Props props) {
        super(props);
    }

    @Override
    public View render(@NonNull final ViewGroup parent) {
        final View paymentView = ViewUtils.inflate(parent, R.layout.px_payment_method_account_money);

        // TODO: process this on PaymentMethodComponent to only render here.
        final Session session = Session.getSession(parent.getContext());
        final MPCall<InitResponse> groups = session.getInitRepository().getInit();
        groups.execute(new Callback<InitResponse>() {
            @Override
            public void success(final InitResponse paymentMethodSearch) {

                final TextView comment = paymentView.findViewById(R.id.comment);

                final CustomSearchItem customOptionsAccountMoney =
                    paymentMethodSearch.getCustomSearchItemByPaymentMethodId(props.paymentMethodId);

                if (customOptionsAccountMoney != null && !TextUtil.isEmpty(customOptionsAccountMoney.getComment())) {
                    comment.setText(customOptionsAccountMoney.getComment());
                    comment.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(final ApiException apiException) {
                //Do nothing here.
            }
        });

        final TextView title = paymentView.findViewById(R.id.title);
        title.setText(props.title);

        final ImageView imageView = paymentView.findViewById(R.id.icon);
        imageView.setImageResource(ResourceUtil.getIconResource(imageView.getContext(), props.paymentMethodId));

        return paymentView;
    }
}
