package com.mercadopago.android.px.internal.features;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESCImpl;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.business_result.BusinessPaymentResultActivity;
import com.mercadopago.android.px.internal.features.express.ExpressPayment;
import com.mercadopago.android.px.internal.features.express.ExpressPaymentFragment;
import com.mercadopago.android.px.internal.features.paymentresult.PaymentResultActivity;
import com.mercadopago.android.px.internal.features.plugins.PaymentProcessorActivity;
import com.mercadopago.android.px.internal.features.providers.CheckoutProvider;
import com.mercadopago.android.px.internal.features.providers.CheckoutProviderImpl;
import com.mercadopago.android.px.internal.features.review_and_confirm.ReviewAndConfirmBuilder;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.FragmentUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.CheckoutStateModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.FinishCheckoutEventTracker;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;

import static com.mercadopago.android.px.core.MercadoPagoCheckout.EXTRA_ERROR;
import static com.mercadopago.android.px.core.MercadoPagoCheckout.EXTRA_PAYMENT_RESULT;
import static com.mercadopago.android.px.core.MercadoPagoCheckout.PAYMENT_RESULT_CODE;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_ACTION;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CANCELED_RYC;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CANCEL_PAYMENT;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CHANGE_PAYMENT_METHOD;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CUSTOM_EXIT;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_ERROR;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_FAIL_ESC;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_PAYMENT;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_SILENT_ERROR;
import static com.mercadopago.android.px.internal.features.paymentresult.PaymentResultActivity.EXTRA_RESULT_CODE;
import static com.mercadopago.android.px.model.ExitAction.EXTRA_CLIENT_RES_CODE;

public class CheckoutActivity extends PXActivity implements CheckoutView, ExpressPaymentFragment.CallBack {

    private static final String EXTRA_PAYMENT_METHOD_CHANGED = "paymentMethodChanged";
    private static final String EXTRA_PRIVATE_KEY = "extra_private_key";
    private static final String EXTRA_PUBLIC_KEY = "extra_public_key";

    private static final int REQ_CONGRATS_BUSINESS = 0x01;
    private static final int REQ_CONGRATS = 0x02;
    private static final int REQ_PAYMENT_PROCESSOR = 0x03;
    private static final int REQ_CARD_VAULT = 0x04;
    private static final int REQ_REVIEW_AND_CONFIRM = 0x05;
    private static final String TAG_ONETAP_FRAGMENT = "TAG_ONETAP";

    //TODO do not make it public - Needed refactor one tap for this.
    public CheckoutPresenter presenter;

    private String merchantPublicKey;
    private String privateKey;
    private Intent customDataBundle;

    public static Intent getIntent(@NonNull final Context context) {
        return new Intent(context, CheckoutActivity.class);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.px_activity_checkout);
        if (savedInstanceState == null) {
            presenter = getActivityParameters();
            configurePresenter();
            presenter.initialize();
        }
    }

    private void configurePresenter() {
        final CheckoutProvider provider = new CheckoutProviderImpl(this,
            merchantPublicKey,
            privateKey,
            new MercadoPagoESCImpl(this, presenter.isESCEnabled()));

        presenter.attachResourcesProvider(provider);
        presenter.attachView(this);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putString(EXTRA_PRIVATE_KEY, privateKey);
        outState.putString(EXTRA_PUBLIC_KEY, merchantPublicKey);
        if (presenter != null) {
            final CheckoutStateModel state = presenter.getState();
            state.toBundle(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final Session session = Session.getSession(this);
            final ConfigurationModule configurationModule = session.getConfigurationModule();

            //TODO remove try catch after session is persisted
            try {
                presenter =
                    new CheckoutPresenter(CheckoutStateModel.fromBundle(savedInstanceState),
                        configurationModule.getPaymentSettings(),
                        configurationModule.getUserSelectionRepository(),
                        session.getInitRepository(),
                        session.getPluginRepository(),
                        session.getPaymentRepository(),
                        session.getInternalConfiguration(),
                        session.getBusinessModelMapper());

                privateKey = savedInstanceState.getString(EXTRA_PRIVATE_KEY);
                merchantPublicKey = savedInstanceState.getString(EXTRA_PUBLIC_KEY);
                configurePresenter();

                if (presenter.getState().isExpressCheckout) {
                    presenter.retrievePaymentMethodSearch();
                }
            } catch (final Exception e) {
                FrictionEventTracker.with(FinishCheckoutEventTracker.PATH,
                    FrictionEventTracker.Id.SILENT, FrictionEventTracker.Style.NON_SCREEN,
                    ErrorUtil.getStacktraceMessage(e))
                    .track();
                exitCheckout(RESULT_CANCELED);
            }
        }
    }

    @Override
    public void onBackPressed() {
        final ExpressPayment.View fragment =
            (ExpressPayment.View) FragmentUtil.getFragmentByTag(getSupportFragmentManager(), TAG_ONETAP_FRAGMENT);
        if (fragment == null || !fragment.isExploding()) {
            super.onBackPressed();
        }
    }

    protected CheckoutPresenter getActivityParameters() {

        final Session session = Session.getSession(this);
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        final PaymentSettingRepository configuration = configurationModule.getPaymentSettings();

        privateKey = configuration.getPrivateKey();

        final CheckoutStateModel
            persistentData = new CheckoutStateModel();

        merchantPublicKey = configuration.getPublicKey();

        return new CheckoutPresenter(persistentData,
            configuration,
            configurationModule.getUserSelectionRepository(),
            session.getInitRepository(),
            session.getPluginRepository(),
            session.getPaymentRepository(),
            session.getInternalConfiguration(),
            session.getBusinessModelMapper());
    }

    @Override
    public void showBusinessResult(final BusinessPaymentModel model) {
        overrideTransitionIn();
        final Intent intent = BusinessPaymentResultActivity.getIntent(this, model);
        startActivityForResult(intent, REQ_CONGRATS_BUSINESS);
    }

    @Override
    public void showOneTap() {

        final FragmentManager supportFragmentManager = getSupportFragmentManager();

        Fragment fragment = supportFragmentManager.findFragmentByTag(TAG_ONETAP_FRAGMENT);

        if (fragment == null) {
            fragment = ExpressPaymentFragment.getInstance();
        }

        getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out)
            .replace(R.id.one_tap_fragment, fragment, TAG_ONETAP_FRAGMENT)
            .commitNowAllowingStateLoss();
    }

    @Override
    public void hideProgress() {
        ViewUtils.showRegularLayout(this);
    }

    @Override
    public void trackScreen() {
        //TODO Implement when Events definition is done.
    }

    @Override
    public void showReviewAndConfirmAndRecoverPayment(final boolean isUniquePaymentMethod,
        @NonNull final PostPaymentAction postPaymentAction) {
        overrideTransitionOut();
        overrideTransitionIn();
        final Intent intent = new ReviewAndConfirmBuilder()
            .setHasExtraPaymentMethods(!isUniquePaymentMethod)
            .setPostPaymentAction(postPaymentAction)
            .getIntent(this);
        startActivityForResult(intent, REQ_REVIEW_AND_CONFIRM);
    }

    @Override
    public void startPayment() {
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final Fragment fragment = supportFragmentManager.findFragmentByTag(TAG_ONETAP_FRAGMENT);
        if (fragment != null && fragment instanceof ExpressPaymentFragment) {
            ((ExpressPaymentFragment) fragment).startPayment();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case ErrorUtil.ERROR_REQUEST_CODE:
            resolveErrorRequest(resultCode, data);
            break;
        case Constants.Activities.PAYMENT_VAULT_REQUEST_CODE: //TODO refactor REQ_CODE.
            resolvePaymentVaultRequest(resultCode, data);
            break;
        case REQ_CARD_VAULT:
            resolveCardVaultRequest(resultCode, data);
            break;
        default:
            resolveCodes(resultCode, data);
            break;
        }
    }

    public void resolveCodes(final int resultCode, final Intent data) {
        // TODO check cancel on payment processor.
        switch (resultCode) {
        case RESULT_CHANGE_PAYMENT_METHOD:
            //TODO support one tap too.
            presenter.onChangePaymentMethodFromReviewAndConfirm();
            break;
        case RESULT_CANCEL_PAYMENT:
            resolveCancelReviewAndConfirm(data);
            break;
        case RESULT_ERROR:
            //TODO crash when intent is null
            // TODO verify scenario.
            if (data != null && data.hasExtra(EXTRA_ERROR)) {
                cancelCheckout((MercadoPagoError) data.getExtras().get(EXTRA_ERROR));
            } else {
                cancelCheckout();
            }
            break;
        case RESULT_CANCELED:
            handleCancel();
            break;
        case RESULT_CANCELED_RYC:
            presenter.onReviewAndConfirmCancel();
            break;
        case RESULT_ACTION:
            handleAction(data);
            break;
        case RESULT_CUSTOM_EXIT:
            handleCustomExit(data);
            break;
        case RESULT_PAYMENT:
            handlePayment(data);
            break;
        case RESULT_FAIL_ESC:
            handleRecovery(data);
            break;
        default:
            break;
        }
    }

    private void handleRecovery(final Intent data) {
        showProgress();
        presenter.onRecoverPaymentEscInvalid(PaymentProcessorActivity.getPaymentRecovery(data));
    }

    private void handlePayment(final Intent data) {
        showProgress();
        paymentResultOk(data);
    }

    private void handleCancel() {
        presenter.cancelCheckout();
    }

    private void handleAction(final Intent data) {
        if (data != null && data.getExtras() != null) {
            PostPaymentAction.fromBundle(data.getExtras()).execute(presenter);
        }
    }

    private void handleCustomExit(final Intent data) {
        if (data != null && data.hasExtra(EXTRA_CLIENT_RES_CODE)) {
            //Business custom exit
            final int resCode = data.getIntExtra(EXTRA_CLIENT_RES_CODE, RESULT_OK);
            presenter.exitWithCode(resCode);
        } else if (data != null && data.hasExtra(EXTRA_RESULT_CODE)) {
            //Custom exit  - Result screen.
            final Integer finalResultCode = data.getIntExtra(EXTRA_RESULT_CODE, PAYMENT_RESULT_CODE);
            customDataBundle = data;
            presenter.onCustomPaymentResultResponse(finalResultCode);
        } else {
            //Normal exit - Result screen.
            presenter.onPaymentResultResponse();
        }
    }

    /**
     * Depending on intent data it triggers {@link com.mercadopago.android.px.internal.features.paymentresult.PaymentResultActivity}
     * flow or {@link BusinessPaymentResultActivity}.
     *
     * @param data intent data that can contains a {@link BusinessPayment}
     */
    private void paymentResultOk(final Intent data) {
        if (PaymentProcessorActivity.isBusiness(data)) {
            final BusinessPayment businessPayment = PaymentProcessorActivity.getBusinessPayment(data);
            presenter.onPaymentFinished(businessPayment);
        } else {
            final IPaymentDescriptor payment = PaymentProcessorActivity.getPayment(data);
            presenter.onPaymentFinished(payment);
        }
    }

    @Override
    public void transitionOut() {
        overrideTransitionOut();
    }

    private void resolveCancelReviewAndConfirm(final Intent data) {
        if (data != null && data.hasExtra(EXTRA_CLIENT_RES_CODE)) {
            final Integer customResultCode = data.getIntExtra(EXTRA_CLIENT_RES_CODE, 0);
            presenter.onCustomReviewAndConfirmResponse(customResultCode);
        } else {
            MercadoPagoError mercadoPagoError = null;
            if (data != null && data.getStringExtra(EXTRA_ERROR) != null) {
                mercadoPagoError = JsonUtil.getInstance()
                    .fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            }
            if (mercadoPagoError == null) {
                presenter.cancelCheckout();
            } else {
                presenter.onReviewAndConfirmError(mercadoPagoError);
            }
        }
    }

    protected void resolveCardVaultRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.onCardFlowResponse();
        } else {
            //TODO CHECK WHEN IT HAPPENS.
            final MercadoPagoError mercadoPagoError =
                (data == null || data.getStringExtra(EXTRA_ERROR) == null) ? null :
                    JsonUtil.getInstance()
                        .fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            if (mercadoPagoError == null) {
                presenter.onCardFlowCancel();
            } else {
                presenter.onTerminalError(mercadoPagoError);
            }
        }
    }

    private void resolvePaymentVaultRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.onPaymentMethodSelected();
        } else if (resultCode == RESULT_SILENT_ERROR) {
            cancelCheckout();
        } else if (isErrorResult(data)) {
            //TODO check when it happens.
            final MercadoPagoError mercadoPagoError =
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            presenter.onPaymentMethodSelectionError(mercadoPagoError);
        } else {
            presenter.onPaymentMethodSelectionCancel();
        }
    }

    private boolean isErrorResult(final Intent data) {
        return data != null && !TextUtil.isEmpty(data.getStringExtra(EXTRA_ERROR));
    }

    @Override
    public void showReviewAndConfirm(final boolean isUniquePaymentMethod) {
        overrideTransitionOut();
        overrideTransitionIn();
        final Intent intent = new ReviewAndConfirmBuilder()
            .setHasExtraPaymentMethods(!isUniquePaymentMethod)
            .getIntent(this);
        startActivityForResult(intent, REQ_REVIEW_AND_CONFIRM);
    }

    @Override
    public void showPaymentMethodSelection() {
        if (isActive()) {
            new Constants.Activities.PaymentVaultActivityBuilder()
                .setActivity(this)
                .startActivity();
            overrideTransitionIn();
        }
    }

    @Override
    public void showPaymentResult(final PaymentResult paymentResult) {
        overrideTransitionIn();
        final Intent intent = PaymentResultActivity.getIntent(this, paymentResult,
            PostPaymentAction.OriginAction.ONE_TAP);
        startActivityForResult(intent, REQ_CONGRATS);
    }

    @Override
    public void showSavedCardFlow(final Card card) {
        new Constants.Activities.CardVaultActivityBuilder()
            .setCard(card)
            .startActivity(this, REQ_CARD_VAULT);
    }

    @Override
    public void showNewCardFlow() {
        new Constants.Activities.CardVaultActivityBuilder()
            .startActivity(this, REQ_CARD_VAULT);
    }

    @Override
    public void startPaymentRecoveryFlow(final PaymentRecovery paymentRecovery) {
        new Constants.Activities.CardVaultActivityBuilder()
            .setPaymentRecovery(paymentRecovery)
            .startActivity(this, REQ_CARD_VAULT);
        overrideTransitionIn();
    }

    private void resolveErrorRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.recoverFromFailure();
        } else {
            final MercadoPagoError mercadoPagoError = data.getStringExtra(EXTRA_ERROR) == null ? null :
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            presenter.onErrorCancel(mercadoPagoError);
        }
    }

    @Override
    public void showError(final MercadoPagoError error) {
        ErrorUtil.startErrorActivity(this, error);
    }

    @Override
    public void showProgress() {
        ViewUtils.showProgressLayout(this);
    }

    @Override
    public void showPaymentProcessor() {
        overrideTransitionWithNoAnimation();
        startActivityForResult(PaymentProcessorActivity.getIntent(this), REQ_PAYMENT_PROCESSOR);
    }

    @Override
    public void showPaymentProcessorWithAnimation() {
        overrideTransitionOut();
        overrideTransitionIn();
        startActivityForResult(PaymentProcessorActivity.getIntent(this), REQ_PAYMENT_PROCESSOR);
    }

    @Override
    protected void onDestroy() {
        //TODO remove null check after session is persisted
        if (presenter != null) {
            presenter.detachResourceProvider();
            presenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public boolean isActive() {
        return !isFinishing();
    }

    @Override
    public void finishWithPaymentResult() {
        presenter.exitWithCode(RESULT_OK);
    }

    @Override
    public void onOneTapCanceled() {
        cancelCheckout();
    }

    //TODO UNIFY
    @Override
    public void cancelCheckout(final Integer resultCode, final Boolean paymentMethodEdited) {
        overrideTransitionOut();
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_PAYMENT_METHOD_CHANGED, paymentMethodEdited);
        setResult(resultCode, intent);
        finish();
    }

    //TODO UNIFY
    @Override
    public void finishWithPaymentResult(final Payment payment) {
        final Intent data = new Intent();
        data.putExtra(EXTRA_PAYMENT_RESULT, payment);
        setResult(PAYMENT_RESULT_CODE, data);
        finish();
    }

    //TODO UNIFY
    @Override
    public void finishWithPaymentResult(final Integer paymentResultCode) {
        final Intent intent = new Intent();
        if (customDataBundle != null) {
            intent.putExtras(customDataBundle);
        }
        setResult(paymentResultCode, intent);
        finish();
    }

    //TODO UNIFY
    @Override
    public void finishWithPaymentResult(final Integer resultCode, final Payment payment) {
        final Intent intent = new Intent();
        if (customDataBundle != null) {
            intent.putExtras(customDataBundle);
        }
        intent.putExtra(EXTRA_PAYMENT_RESULT, payment);
        setResult(resultCode, intent);
        finish();
    }

    //TODO UNIFY
    @Override
    public void cancelCheckout(final MercadoPagoError mercadoPagoError) {
        overrideTransitionOut();
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_ERROR, mercadoPagoError);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    //TODO UNIFY
    @Override
    public void exitCheckout(final int resCode) {
        overrideTransitionOut();
        setResult(resCode);
        finish();
    }

    //TODO UNIFY
    @Override
    public void cancelCheckout() {
        overrideTransitionOut();
        exitCheckout(RESULT_CANCELED);
    }
}