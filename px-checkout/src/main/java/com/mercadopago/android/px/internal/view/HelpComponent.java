package com.mercadopago.android.px.internal.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ViewUtils;
import javax.annotation.Nonnull;

public class HelpComponent extends CompactComponent<String, Void> {

    public HelpComponent(final String s) {
        super(s);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final ViewGroup bodyErrorView = (ViewGroup) ViewUtils.inflate(parent, R.layout.px_payment_result_body_help);
        final TextView errorTitle = bodyErrorView.findViewById(R.id.paymentResultBodyErrorTitle);
        final TextView errorDescription = bodyErrorView.findViewById(R.id.paymentResultBodyErrorDescription);
        errorTitle.setText(parent.getContext().getString(R.string.px_what_can_do));
        errorDescription.setText(props);
        return bodyErrorView;
    }
}