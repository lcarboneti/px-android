package com.mercadopago.android.px.internal.features.express.slider;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.view.DynamicTextViewRowView;
import com.mercadopago.android.px.internal.viewmodel.drawables.SavedCardDrawableFragmentItem;
import com.mercadopago.android.px.model.PaymentTypes;

public class SavedCardFragment extends PaymentMethodFragment {

    @NonNull
    public static Fragment getInstance(final SavedCardDrawableFragmentItem savedCard) {
        final SavedCardFragment savedCardFragment = new SavedCardFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_MODEL, savedCard);
        bundle.putString(ARG_PM_TYPE, PaymentTypes.CREDIT_CARD);
        savedCardFragment.setArguments(bundle);
        return savedCardFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_saved_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_MODEL)) {
            final SavedCardDrawableFragmentItem drawableCard =
                (SavedCardDrawableFragmentItem) arguments.getSerializable(ARG_MODEL);
            tintBackground(view.findViewById(R.id.background), drawableCard.card.color);
            setCardInformation(view, drawableCard);
            setPaymentMethodIcon(view, drawableCard);
            setIssuerIcon(view, drawableCard);
        } else {
            throw new IllegalStateException("SavedCardFragment does not contain card information");
        }
    }

    protected void setIssuerIcon(@NonNull final View view, @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        final ImageView issuerIcon = view.findViewById(R.id.card_issuer_logo);
        final int issuerResource = ResourceUtil.getCardIssuerImage(view.getContext(), drawableCard.card.issuerImage);

        if (issuerResource > 0) {
            issuerIcon.setImageResource(issuerResource);
        }
    }

    private void setPaymentMethodIcon(@NonNull final View view,
        @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        final ImageView paymentMethodIcon = view.findViewById(R.id.card_payment_type_logo);
        // set card brand image
        final int paymentMethodResource = ResourceUtil.getCardImage(view.getContext(), drawableCard.paymentMethodId);

        if (paymentMethodResource > 0) {
            paymentMethodIcon.setImageResource(paymentMethodResource);
        }
    }

    private void setCardInformation(@NonNull final View view,
        @NonNull final SavedCardDrawableFragmentItem drawableCard) {
        final TextView cardHolderName = view.findViewById(R.id.card_holder_name);
        final TextView expDate = view.findViewById(R.id.exp_date);
        final int fontColor = Color.parseColor(drawableCard.card.fontColor);
        cardHolderName.setTextColor(fontColor);
        expDate.setTextColor(fontColor);
        cardHolderName.setText(drawableCard.card.cardholderName);
        expDate.setText(drawableCard.card.expiration);
        setCardNumber(view, drawableCard, fontColor);
    }

    protected void setCardNumber(@NonNull final View view,
        @NonNull final SavedCardDrawableFragmentItem drawableCard, final int fontColor) {
        final DynamicTextViewRowView cardNumber = view.findViewById(R.id.card_number_view);
        cardNumber.setColor(fontColor);
        cardNumber.setText(drawableCard.card.getCardPattern(), DynamicTextViewRowView.SPACE);
    }
}