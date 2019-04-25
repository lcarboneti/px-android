package com.mercadopago.android.px.internal.features.express.slider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;

public abstract class ViewAdapter<T, V extends View> {

    @NonNull protected T data;
    @Nullable protected final V view;

    /* default */ ViewAdapter(@NonNull final T data) {
        this(data, null);
    }

    /* default */ ViewAdapter(@NonNull final T data, @Nullable final V view) {
        this.data = data;
        this.view = view;
    }

    public void update(@NonNull final T newData) {
        data = newData;
    }

    public abstract void updateData(final int currentIndex, final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState);

    public void updateViewsOrder(@NonNull final View previousView,
        @NonNull final View currentView,
        @NonNull final View nextView) {
        // Do nothing
    }

    public void updatePosition(final float positionOffset, final int position) {
        // Do nothing
    }

    public void showInstallmentsList() {
        // Do nothing
    }
}
