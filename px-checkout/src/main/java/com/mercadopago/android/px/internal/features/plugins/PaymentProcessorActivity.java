package com.mercadopago.android.px.internal.features.plugins;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.core.SplitPaymentProcessor;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandlerWrapper;
import com.mercadopago.android.px.internal.datasource.EscPaymentManagerImp;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.GenericPayment;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.IPaymentDescriptorHandler;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.IParcelablePaymentDescriptor;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.List;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_FAIL_ESC;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_PAYMENT;
import static com.mercadopago.android.px.internal.util.ErrorUtil.ERROR_REQUEST_CODE;

public final class PaymentProcessorActivity extends AppCompatActivity
    implements SplitPaymentProcessor.OnPaymentListener,
    PaymentProcessor.OnPaymentListener {

    private static final String TAG_PROCESSOR_FRAGMENT = "TAG_PROCESSOR_FRAGMENT";
    private static final String EXTRA_BUSINESS_PAYMENT = "extra_business_payment";
    private static final String EXTRA_PAYMENT = "extra_payment";
    private static final String EXTRA_RECOVERY = "extra_recovery";

    private PaymentServiceHandlerWrapper paymentServiceHandlerWrapper;
    private PaymentServiceHandler wrapper;

    public static Intent getIntent(@NonNull final Context context) {
        return new Intent(context, PaymentProcessorActivity.class);
    }

    public static boolean isBusiness(@Nullable final Intent intent) {
        return intent != null && intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_BUSINESS_PAYMENT);
    }

    @Nullable
    public static IParcelablePaymentDescriptor getPayment(final Intent intent) {
        return (IParcelablePaymentDescriptor) intent.getExtras().get(EXTRA_PAYMENT);
    }

    @Nullable
    public static BusinessPayment getBusinessPayment(final Intent intent) {
        return (BusinessPayment) intent.getExtras().get(EXTRA_BUSINESS_PAYMENT);
    }

    @Nullable
    public static PaymentRecovery getPaymentRecovery(final Intent intent) {
        return (PaymentRecovery) intent.getExtras().get(EXTRA_RECOVERY);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(R.id.px_main_container);
        setContentView(frameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));

        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final Fragment fragmentByTag = supportFragmentManager.findFragmentByTag(TAG_PROCESSOR_FRAGMENT);

        final Session session = Session.getSession(getApplicationContext());

        try {
            paymentServiceHandlerWrapper = new PaymentServiceHandlerWrapper(session.getPaymentRepository(),
                session.getConfigurationModule().getDisabledPaymentMethodRepository(),
                new EscPaymentManagerImp(session.getMercadoPagoESC()), session.getInstructionsRepository());

            if (fragmentByTag == null) { // if fragment is not added, then create it.
                addPaymentProcessorFragment(supportFragmentManager, session);
            }
        } catch (final Exception e) {
            onBackPressed();
        }
    }

    private void addPaymentProcessorFragment(@NonNull final FragmentManager supportFragmentManager,
        @NonNull final Session session) {

        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final PaymentSettingRepository paymentSettings = configurationModule.getPaymentSettings();

        final SplitPaymentProcessor paymentProcessor = paymentSettings
            .getPaymentConfiguration()
            .getPaymentProcessor();

        final List<PaymentData> paymentData = session
            .getPaymentRepository()
            .getPaymentDataList();

        final CheckoutPreference checkoutPreference = paymentSettings.getCheckoutPreference();

        final SplitPaymentProcessor.CheckoutData checkoutData =
            new SplitPaymentProcessor.CheckoutData(paymentData, checkoutPreference);

        final Fragment fragment = paymentProcessor.getFragment(checkoutData, this);

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.px_main_container, fragment, TAG_PROCESSOR_FRAGMENT)
                .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        wrapper = createWrapper();
        paymentServiceHandlerWrapper.setHandler(wrapper);
        paymentServiceHandlerWrapper.processMessages();
    }

    @Override
    protected void onPause() {
        paymentServiceHandlerWrapper.detach(wrapper);
        super.onPause();
    }

    @NonNull
    private PaymentServiceHandler createWrapper() {
        return new PaymentServiceHandler() {
            @Override
            public void onCvvRequired(@NonNull final Card card) {
                // do nothing
            }

            @Override
            public void onVisualPayment() {
                // do nothing
            }

            @Override
            public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
                final Intent intent = new Intent();
                intent.putExtra(EXTRA_RECOVERY, recovery);
                setResult(RESULT_FAIL_ESC, intent);
                finish();
            }

            @Override
            public void onPaymentFinished(@NonNull final IPaymentDescriptor payment) {

                payment.process(new IPaymentDescriptorHandler() {
                    @Override
                    public void visit(@NonNull final BusinessPayment businessPayment) {
                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_BUSINESS_PAYMENT, (Parcelable) businessPayment);
                        setResult(RESULT_PAYMENT, intent);
                        finish();
                    }

                    @Override
                    public void visit(@NonNull final IPaymentDescriptor payment) {
                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_PAYMENT, (Parcelable) IParcelablePaymentDescriptor.with(payment));
                        setResult(RESULT_PAYMENT, intent);
                        finish();
                    }
                });
            }

            @Override
            public void onPaymentError(@NonNull final MercadoPagoError error) {
                //TODO verify error handling
                ErrorUtil.startErrorActivity(PaymentProcessorActivity.this, error);
            }
        };
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ERROR_REQUEST_CODE) {
            //TODO verify error handling
            onBackPressed();
        }
    }

    @Override
    public void onPaymentFinished(@NonNull final IPaymentDescriptor payment) {
        paymentServiceHandlerWrapper.onPaymentFinished(payment);
    }

    @Override
    public void onPaymentFinished(@NonNull final Payment payment) {
        paymentServiceHandlerWrapper.onPaymentFinished(payment);
    }

    @Override
    public void onPaymentFinished(@NonNull final GenericPayment genericPayment) {
        paymentServiceHandlerWrapper.onPaymentFinished(IParcelablePaymentDescriptor.with(genericPayment));
    }

    @Override
    public void onPaymentFinished(@NonNull final BusinessPayment businessPayment) {
        paymentServiceHandlerWrapper.onPaymentFinished(businessPayment);
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        paymentServiceHandlerWrapper.onPaymentError(error);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }
}