package com.mercadopago.android.px.internal.features.express.slider;

import android.support.annotation.NonNull;
import android.view.View;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.TitlePager;
import com.mercadopago.android.px.internal.viewmodel.GoingToModel;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import java.util.List;

public class TitlePagerAdapter extends ViewAdapter<List<PaymentMethodDescriptorView.Model>, TitlePager> {

    private static final int NO_SELECTED = -1;

    private PaymentMethodDescriptorView previousView;
    private PaymentMethodDescriptorView currentView;
    private PaymentMethodDescriptorView nextView;
    private int currentIndex = NO_SELECTED;

    public TitlePagerAdapter(@NonNull final List<PaymentMethodDescriptorView.Model> models,
        @NonNull final TitlePager titlePager) {
        super(models, titlePager);
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState) {
        if (this.currentIndex != currentIndex) {
            final GoingToModel goingTo =
                this.currentIndex < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
            view.orderViews(goingTo);
            this.currentIndex = currentIndex;
        }
        refreshData(currentIndex, payerCostSelected, splitSelectionState);
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        final GoingToModel goingTo = position < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
        view.updatePosition(positionOffset, goingTo);
    }

    @Override
    public void updateViewsOrder(@NonNull final View previousView,
        @NonNull final View currentView,
        @NonNull final View nextView) {
        this.previousView = (PaymentMethodDescriptorView) previousView;
        this.currentView = (PaymentMethodDescriptorView) currentView;
        this.nextView = (PaymentMethodDescriptorView) nextView;
    }

    private void refreshData(final int currentIndex, final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState) {
        if (currentIndex > 0) {
            final PaymentMethodDescriptorView.Model previousModel = data.get(currentIndex - 1);
            previousView.update(previousModel);
        }

        final PaymentMethodDescriptorView.Model currentModel = data.get(currentIndex);
        currentModel.setCurrentPayerCost(payerCostSelected);
        currentModel.setSplit(splitSelectionState.userWantsToSplit());
        currentView.update(currentModel);

        if (currentIndex + 1 < data.size()) {
            final PaymentMethodDescriptorView.Model nextModel = data.get(currentIndex + 1);
            nextView.update(nextModel);
        }
    }
}
