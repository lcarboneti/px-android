package com.mercadopago.android.px.internal.features.express;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.addons.model.SecurityValidationData;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.express.slider.HubAdapter;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public interface ExpressPayment {

    interface View extends MvpView {

        void configureAdapters(@NonNull List<DrawableFragmentItem> items, @NonNull final Site site, @NonNull HubAdapter
            .Model paymentMethodViewModel);

        void cancel();

        void showSecurityCodeScreen(@NonNull final Card card);

        void showCardFlow(@NonNull PaymentRecovery paymentRecovery);

        void showPaymentProcessor();

        void finishLoading(@NonNull final ExplodeDecorator params);

        void cancelLoading();

        void startLoadingButton(final int paymentTimeout, @NonNull final PayButtonViewModel payButtonViewModel);

        //TODO shared with Checkout activity

        void showErrorScreen(@NonNull final MercadoPagoError error);

        void showPaymentResult(@NonNull final IPaymentDescriptor paymentResult);

        void startSecurityValidation(@NonNull SecurityValidationData data);

        void startPayment();

        void enableToolbarBack();

        void disableToolbarBack();

        void showErrorSnackBar(@NonNull final MercadoPagoError error);

        void updateViewForPosition(final int paymentMethodIndex,
            final int payerCostSelected,
            @NonNull final SplitSelectionState splitSelectionState);

        void showInstallmentsList(List<PayerCost> payerCostList, final int payerCostSelected);

        void showToolbarElementDescriptor(@NonNull final ElementDescriptorView.Model elementDescriptorModel);

        void collapseInstallmentsSelection();

        void showDiscountDetailDialog(@NonNull final DiscountConfigurationModel discountModel);

        boolean isExploding();

        void resetPagerIndex();

        void showDynamicDialog(@NonNull final DynamicDialogCreator creatorFor,
            @NonNull final DynamicDialogCreator.CheckoutData checkoutData);

        void setPayButtonText(@NonNull final PayButtonViewModel payButtonViewModel);
    }

    interface Actions extends PaymentServiceHandler {

        void trackExpressView();

        void startSecuredPayment();

        void confirmPayment();

        void trackSecurityFriction();

        void cancel();

        void onTokenResolved();

        void loadViewModel();

        void onViewResumed();

        void onViewPaused();

        void onInstallmentsRowPressed();

        void onInstallmentSelectionCanceled();

        void onSliderOptionSelected(final int paymentMethodIndex);

        void onPayerCostSelected(final PayerCost payerCostSelected);

        void hasFinishPaymentAnimation();

        void manageNoConnection();

        void onSplitChanged(boolean isChecked);

        void onHeaderClicked();
    }
}