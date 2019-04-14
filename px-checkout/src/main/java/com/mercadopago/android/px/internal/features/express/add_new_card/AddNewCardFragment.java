package com.mercadopago.android.px.internal.features.express.add_new_card;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.payment_vault.PaymentVaultActivity;
import com.mercadopago.android.px.internal.viewmodel.drawables.AddNewCardFragmentDrawableFragmentItem;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;

public class AddNewCardFragment extends Fragment implements AddNewCard.View, View.OnClickListener {

    private static final String ARG_MODEL = "ARG_MODEL";

    private AddNewCardPresenter presenter;

    @SuppressWarnings("TypeMayBeWeakened")
    @NonNull
    public static Fragment getInstance(@NonNull final AddNewCardFragmentDrawableFragmentItem drawableItem) {
        final AddNewCardFragment addNewCardFragment = new AddNewCardFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_MODEL, drawableItem);
        addNewCardFragment.setArguments(bundle);
        return addNewCardFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_change_payment_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_MODEL)) {
            // unused for now
            //final AddNewCardFragmentDrawableFragmentItem model =
            //    (AddNewCardFragmentDrawableFragmentItem) arguments.getSerializable(ARG_MODEL);

            configureClick(view);
        } else {
            throw new IllegalStateException("AddNewCardFragment does not contains model info");
        }
        presenter = createPresenter(view.getContext());
        presenter.attachView(this);
    }

    @Override
    public void onDetach() {
        presenter.detachView();
        super.onDetach();
    }

    @Override
    public void showPaymentMethods() {
        PaymentVaultActivity.start((AppCompatActivity) getActivity());
    }

    @Override
    public void showPaymentMethodsWithSelection(@NonNull final PaymentMethodSearchItem paymentMethodSearchItem) {
        PaymentVaultActivity.startWithPaymentMethodSelected((AppCompatActivity) getActivity(), paymentMethodSearchItem);
    }

    private AddNewCardPresenter createPresenter(@NonNull final Context context) {
        return new AddNewCardPresenter(Session.getSession(context).getInitRepository());
    }

    protected void configureClick(@NonNull final View view) {
        final View floating = view.findViewById(R.id.floating_change);
        final MeliButton message = view.findViewById(R.id.message);
        message.setText(getString(R.string.px_add_new_card));
        floating.setOnClickListener(this);
        message.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        presenter.onAddNewCardSelected();
    }
}
