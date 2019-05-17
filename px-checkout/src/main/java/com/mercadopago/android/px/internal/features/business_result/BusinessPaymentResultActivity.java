package com.mercadopago.android.px.internal.features.business_result;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.internal.PrimaryExitAction;
import com.mercadopago.android.px.model.internal.SecondaryExitAction;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.internal.events.AbortEvent;
import com.mercadopago.android.px.tracking.internal.events.PrimaryActionEvent;
import com.mercadopago.android.px.tracking.internal.events.SecondaryActionEvent;
import com.mercadopago.android.px.tracking.internal.views.ResultViewTrack;
import com.mercadopago.android.px.tracking.internal.views.ViewTracker;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_CUSTOM_EXIT;

public class BusinessPaymentResultActivity extends PXActivity implements ActionDispatcher {

    private static final String EXTRA_BUSINESS_PAYMENT_MODEL = "extra_business_payment_model";

    private ViewTracker viewTracker;

    public static Intent getIntent(@NonNull final Context context, @NonNull final BusinessPaymentModel model) {
        final Intent intent = new Intent(context, BusinessPaymentResultActivity.class);
        intent.putExtra(EXTRA_BUSINESS_PAYMENT_MODEL, model);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final BusinessPaymentModel model = parseBusinessPaymentModel();
        if (model != null) {
            viewTracker = createTracker(model);

            final LinearLayout linearContainer = ViewUtils.createLinearContainer(this);
            linearContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
            setContentView(linearContainer);

            final BusinessPaymentContainer businessPaymentContainer = new BusinessPaymentContainer(model, this);
            final View render = businessPaymentContainer.render(linearContainer);
            linearContainer.addView(render);
        } else {
            throw new IllegalStateException("BusinessPayment can't be loaded");
        }

        if (savedInstanceState == null) {
            viewTracker.track();
        }
    }

    @NonNull
    private ViewTracker createTracker(final BusinessPaymentModel model) {
        final CheckoutPreference checkoutPreference =
            Session.getSession(this).getConfigurationModule().getPaymentSettings().getCheckoutPreference();

        return new ResultViewTrack(ResultViewTrack.Style.CUSTOM, new PaymentResult.Builder()
            .setPaymentData(model.getPaymentDataList())
            .setPaymentStatus(model.payment.getPaymentStatus())
            .setPaymentStatusDetail(model.payment.getPaymentStatusDetail())
            .setPaymentId(model.payment.getId())
            .build(), checkoutPreference);
    }

    @Nullable
    private BusinessPaymentModel parseBusinessPaymentModel() {
        return getIntent().getExtras() != null ? (BusinessPaymentModel) getIntent()
            .getExtras()
            .getParcelable(EXTRA_BUSINESS_PAYMENT_MODEL) : null;
    }

    @Override
    public void dispatch(final Action action) {
        if (action instanceof ExitAction) {

            // Hack for tracking
            if (action instanceof PrimaryExitAction) {
                new PrimaryActionEvent(viewTracker).track();
            } else if (action instanceof SecondaryExitAction) {
                new SecondaryActionEvent(viewTracker).track();
            }

            processCustomExit((ExitAction) action);
        } else {
            throw new UnsupportedOperationException("this Action class can't be executed in this screen");
        }
    }

    @Override
    public void onBackPressed() {
        new AbortEvent(viewTracker).track();
        processCustomExit(new ExitAction("exit", RESULT_OK));
    }

    private void processCustomExit(final ExitAction action) {
        final Intent intent = action.toIntent();
        setResult(RESULT_CUSTOM_EXIT, intent);
        finish();
    }
}