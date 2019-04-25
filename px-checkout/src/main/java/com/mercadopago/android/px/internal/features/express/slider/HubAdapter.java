package com.mercadopago.android.px.internal.features.express.slider;

import android.support.annotation.NonNull;
import android.view.View;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.viewmodel.ConfirmButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import java.util.ArrayList;
import java.util.List;

public class HubAdapter extends ViewAdapter<List<ViewAdapter<?, ? extends View>>, View> {

    public static class Model {

        @NonNull public final List<PaymentMethodDescriptorView.Model> paymentMethodDescriptorModels;
        @NonNull public final List<SummaryView.Model> summaryViewModels;
        @NonNull public final List<SplitPaymentHeaderAdapter.Model> splitModels;
        @NonNull public final List<ConfirmButtonViewModel> confirmButtonViewModels;

        public Model(@NonNull final List<PaymentMethodDescriptorView.Model> paymentMethodDescriptorModels,
            @NonNull final List<SummaryView.Model> summaryViewModels,
            @NonNull final List<SplitPaymentHeaderAdapter.Model> splitModels,
            @NonNull final List<ConfirmButtonViewModel> confirmButtonViewModels) {
            this.paymentMethodDescriptorModels = paymentMethodDescriptorModels;
            this.summaryViewModels = summaryViewModels;
            this.splitModels = splitModels;
            this.confirmButtonViewModels = confirmButtonViewModels;
        }
    }

    public HubAdapter() {
        super(new ArrayList<>());
    }

    @Override
    public void showInstallmentsList() {
        for (final ViewAdapter adapter : data) {
            adapter.showInstallmentsList();
        }
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState) {
        for (final ViewAdapter adapter : data) {
            adapter.updateData(currentIndex, payerCostSelected, splitSelectionState);
        }
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        for (final ViewAdapter adapter : data) {
            adapter.updatePosition(positionOffset, position);
        }
    }

    @Override
    public void updateViewsOrder(@NonNull final View previousView, @NonNull final View currentView,
        @NonNull final View nextView) {
        for (final ViewAdapter adapter : data) {
            adapter.updateViewsOrder(previousView, currentView, nextView);
        }
    }
}