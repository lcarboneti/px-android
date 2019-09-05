package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadolibre.android.ui.widgets.MeliSnackbar;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.addons.internal.PXApplicationBehaviourProvider;
import com.mercadopago.android.px.addons.model.SecurityValidationData;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.ReviewAndConfirmConfiguration;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.business_result.BusinessPaymentResultActivity;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultActivity;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.explode.ExplodeParams;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.features.payer_information.PayerInformationActivity;
import com.mercadopago.android.px.internal.features.paymentresult.PaymentResultActivity;
import com.mercadopago.android.px.internal.features.plugins.PaymentProcessorActivity;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.ReviewAndConfirmContainer;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.CancelPaymentAction;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ChangePayerInformationAction;
import com.mercadopago.android.px.internal.features.review_and_confirm.components.actions.ChangePaymentMethodAction;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.ItemsModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.PaymentModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.SummaryModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.TermsAndConditionsModel;
import com.mercadopago.android.px.internal.features.uicontrollers.FontCache;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.FragmentUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.ComponentManager;
import com.mercadopago.android.px.internal.view.LinkableTextComponent;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.model.Action;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.ExitAction;
import com.mercadopago.android.px.model.Payer;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.display_info.LinkableText;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.views.ReviewAndConfirmViewTracker;

import static android.content.Intent.FLAG_ACTIVITY_FORWARD_RESULT;
import static com.mercadopago.android.px.core.MercadoPagoCheckout.EXTRA_ERROR;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CANCELED_RYC;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CANCEL_PAYMENT;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CHANGE_PAYMENT_METHOD;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_ERROR;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_SILENT_ERROR;

public final class ReviewAndConfirmActivity extends PXActivity<ReviewAndConfirmPresenter> implements
    ReviewAndConfirm.View, ActionDispatcher, ExplodingFragment.ExplodingAnimationListener {

    private static final int REQ_CODE_CARD_VAULT = 1;
    private static final int REQ_CODE_BIOMETRICS = 2;

    private static final String EXTRA_TERMS_AND_CONDITIONS = "extra_terms_and_conditions";
    private static final String EXTRA_DISPLAY_INFO_LINKABLE_TEXT =
        "extra_digital_currency_terms_and_conditions";
    private static final String EXTRA_PAYMENT_MODEL = "extra_payment_model";
    private static final String EXTRA_SUMMARY_MODEL = "extra_summary_model";
    private static final String EXTRA_PUBLIC_KEY = "extra_public_key";
    private static final String EXTRA_ITEMS = "extra_items";
    private static final String EXTRA_DISCOUNT_TERMS_AND_CONDITIONS = "extra_discount_terms_and_conditions";
    private static final String TAG_DYNAMIC_DIALOG = "tag_dynamic_dialog";
    private static final int PAYER_INFORMATION_REQUEST_CODE = 22;
    private static final String TAG_EXPLODING_FRAGMENT = "TAG_EXPLODING_FRAGMENT";

    private MeliButton confirmButton;
    private LinearLayout floatingLayout;

    public static Intent getIntentForAction(@NonNull final Context context,
        @NonNull final String merchantPublicKey,
        @Nullable final TermsAndConditionsModel mercadoPagoTermsAndConditions,
        @Nullable final LinkableText linkableText,
        @NonNull final PaymentModel paymentModel,
        @NonNull final SummaryModel summaryModel,
        @NonNull final ItemsModel itemsModel,
        @Nullable final TermsAndConditionsModel discountTermsAndConditions,
        @Nullable final PostPaymentAction postPaymentAction) {

        final Intent intent = new Intent(context, ReviewAndConfirmActivity.class);
        intent.putExtra(EXTRA_PUBLIC_KEY, merchantPublicKey);
        intent.putExtra(EXTRA_TERMS_AND_CONDITIONS, mercadoPagoTermsAndConditions);
        intent.putExtra(EXTRA_DISPLAY_INFO_LINKABLE_TEXT, (Parcelable) linkableText);
        intent.putExtra(EXTRA_PAYMENT_MODEL, paymentModel);
        intent.putExtra(EXTRA_SUMMARY_MODEL, summaryModel);
        intent.putExtra(EXTRA_ITEMS, itemsModel);
        intent.putExtra(EXTRA_DISCOUNT_TERMS_AND_CONDITIONS, discountTermsAndConditions);

        if (postPaymentAction != null) {
            postPaymentAction.addToIntent(intent);
        }

        return intent;
    }

    /**
     * It is necessary to check if we have a PostPaymentAction when we start Review and confirm so that we don't show
     * the UI for review and confirm right away, and we can start the recover payment process
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.px_view_container_review_and_confirm);
        initializeViews();
        final Session session = Session.getInstance();

        //TODO remove try/catch after session is persisted
        try {
            presenter = new ReviewAndConfirmPresenter(session.getPaymentRepository(),
                session.getBusinessModelMapper(),
                session.getDiscountRepository(),
                session.getConfigurationModule().getPaymentSettings(),
                session.getConfigurationModule().getUserSelectionRepository(),
                session.getMercadoPagoESC(),
                session.getProductIdProvider(),
                PXApplicationBehaviourProvider.getSecurityBehaviour());
            presenter.attachView(this);
        } catch (final Exception e) {
            FrictionEventTracker.with(ReviewAndConfirmViewTracker.PATH,
                FrictionEventTracker.Id.SILENT, FrictionEventTracker.Style.SCREEN, ErrorUtil.getStacktraceMessage(e))
                .track();

            exitCheckout(RESULT_SILENT_ERROR);
        }

        if (savedInstanceState == null) {
            checkIntentActions();
        }
    }

    /**
     * This is called whenever savedInstanceState is null, that means the first time the activity is launched. When we
     * don't have a payment yet, we don't have PostPaymentAction so we don't do anything If we have a PostPaymentAction
     * (CheckoutActivity launches ReviewAndConfirm again to recover the payment) we need to get it and execute the
     * recovery
     */
    private void checkIntentActions() {
        if (PostPaymentAction.hasPostPaymentAction(getIntent())) {
            presenter.executePostPaymentAction(PostPaymentAction.fromBundle(getIntent().getExtras()));
        }
    }

    /**
     * We need to save something on save instance state to cover the cases when the activity is destroyed. If there is
     * something saved (savedInstanceState is not null), that means we already tried to recover the payment if that was
     * necessary.
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putInt("TOBESAVED", 1);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onViewResumed(this);
    }

    @Override
    protected void onDestroy() {
        //TODO remove null check after session is persisted
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDestroy();
    }

    /**
     * On a payment recovery, when it comes back from card vault, we need the view to be initialized before we can try
     * to make the payment again (so we can explode the button). We added the post() method to make sure the view was
     * initialized.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQ_CODE_CARD_VAULT:
            getWindow().getDecorView().post(() -> resolveCardVaultRequest(resultCode, data));
            break;
        case REQ_CODE_BIOMETRICS:
            handleBiometricsResult(resultCode);
            break;
        case ErrorUtil.ERROR_REQUEST_CODE:
            getWindow().getDecorView().post(() -> resolveErrorRequest(resultCode, data));
            break;
        case PAYER_INFORMATION_REQUEST_CODE:
            getWindow().getDecorView().post(() -> resolvePayerInformationRequest(resultCode));
            break;
        default:
            //Do nothing
            break;
        }
    }

    private void handleBiometricsResult(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.onPaymentConfirm();
        } else {
            presenter.trackSecurityFriction();
        }
    }

    /* default */ void resolvePayerInformationRequest(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.onPayerInformationResponse();
        }
    }

    @Override
    public void reloadBody() {
        final ViewGroup mainContent = findViewById(R.id.scroll_view);
        mainContent.removeAllViews();
        initBody();
    }

    private void initializeViews() {
        confirmButton = findViewById(R.id.floating_confirm);
        initToolbar();
        initBody();
    }

    private void initBody() {
        final ViewGroup mainContent = findViewById(R.id.scroll_view);
        final ContainerProps props = getActivityParameters();
        initContent(mainContent, props.reviewAndConfirmContainerProps);
        initFloatingButton(mainContent, props.linkableText);
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayShowTitleEnabled(false);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.px_activity_checkout_title));
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            collapsingToolbarLayout.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            collapsingToolbarLayout.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void initFloatingButton(final ViewGroup scrollView, @Nullable final LinkableText linkableText) {
        floatingLayout = findViewById(R.id.floating_layout);
        confirmButton.setOnClickListener(v -> presenter.startSecuredPayment());

        if (linkableText != null) {
            final LinkableTextComponent linkableTextComponent = new LinkableTextComponent(linkableText);
            floatingLayout.addView(linkableTextComponent.render(floatingLayout), 0);
        }

        configureFloatingBehaviour(scrollView, floatingLayout);
    }

    @Override
    public void startSecurityValidation(@NonNull final SecurityValidationData data) {
        PXApplicationBehaviourProvider.getSecurityBehaviour().startValidation(this, data, REQ_CODE_BIOMETRICS);
    }

    private void configureFloatingBehaviour(final ViewGroup scrollView, final View floatingConfirmLayout) {
        addScrollBottomPadding(floatingConfirmLayout, scrollView);
        configureScrollLayoutListener(floatingConfirmLayout, scrollView);
        addScrollListener(floatingConfirmLayout, scrollView);
    }

    private void addScrollBottomPadding(final View floatingConfirmLayout, final View scrollView) {
        final ViewTreeObserver floatingObserver = floatingConfirmLayout.getViewTreeObserver();
        floatingObserver.addOnGlobalLayoutListener(() -> {
            final int bottomPadding = floatingConfirmLayout.getHeight();
            if (scrollView.getPaddingBottom() != bottomPadding) {
                scrollView.setPadding(scrollView.getPaddingLeft(), scrollView.getPaddingTop(),
                    scrollView.getPaddingRight(), bottomPadding);
            }
        });
    }

    private void configureScrollLayoutListener(final View floatingConfirmLayout, final ViewGroup scrollView) {
        final ViewTreeObserver viewTreeObserver = scrollView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(
            () -> resolveFloatingButtonElevationVisibility(floatingConfirmLayout, scrollView));
    }

    private void addScrollListener(final View floatingConfirmLayout, final ViewGroup scrollView) {
        final ViewTreeObserver viewTreeObserver = scrollView.getViewTreeObserver();
        viewTreeObserver.addOnScrollChangedListener(
            () -> resolveFloatingButtonElevationVisibility(floatingConfirmLayout, scrollView));
    }

    /* default */ void resolveFloatingButtonElevationVisibility(final View floatingConfirmLayout,
        final ViewGroup scrollView) {
        final ViewGroup content = (ViewGroup) scrollView.getChildAt(0);
        final int containerHeight = content.getHeight();
        final float finalSize = containerHeight - scrollView.getHeight();
        setFloatingElevationVisibility(floatingConfirmLayout, scrollView.getScrollY() < finalSize);
    }

    private void initContent(final ViewGroup mainContent, final ReviewAndConfirmContainer.Props props) {
        final ComponentManager manager = new ComponentManager(this);
        final ReviewAndConfirmContainer container = new ReviewAndConfirmContainer(props, this);

        container.setDispatcher(this);

        //TODO remove try/catch after session is persisted
        try {
            manager.render(container, mainContent);
        } catch (final Exception e) {
            FrictionEventTracker.with(ReviewAndConfirmViewTracker.PATH,
                FrictionEventTracker.Id.SILENT, FrictionEventTracker.Style.SCREEN, ErrorUtil.getStacktraceMessage(e))
                .track();

            exitCheckout(RESULT_SILENT_ERROR);
        }
    }

    public void exitCheckout(final int resCode) {
        overrideTransitionOut();
        setResult(resCode);
        finish();
    }

    private ContainerProps getActivityParameters() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
            final TermsAndConditionsModel termsAndConditionsModel = extras.getParcelable(EXTRA_TERMS_AND_CONDITIONS);
            final PaymentModel paymentModel = extras.getParcelable(EXTRA_PAYMENT_MODEL);

            final SummaryModel summaryModel = extras.getParcelable(EXTRA_SUMMARY_MODEL);
            final ItemsModel itemsModel = extras.getParcelable(EXTRA_ITEMS);
            final TermsAndConditionsModel discountTermsAndConditions =
                extras.getParcelable(EXTRA_DISCOUNT_TERMS_AND_CONDITIONS);

            final Session session = Session.getInstance();
            final ConfigurationModule configurationModule = session.getConfigurationModule();
            final AdvancedConfiguration advancedConfiguration = configurationModule.getPaymentSettings()
                .getAdvancedConfiguration();
            final Payer payer = configurationModule.getPaymentSettings().getCheckoutPreference().getPayer();

            final ReviewAndConfirmConfiguration reviewAndConfirmConfiguration =
                advancedConfiguration.getReviewAndConfirmConfiguration();

            final LinkableText linkableText = extras.getParcelable(EXTRA_DISPLAY_INFO_LINKABLE_TEXT);

            ReviewAndConfirmContainer.Props reviewAndConfirmContainerProps =
                new ReviewAndConfirmContainer.Props(termsAndConditionsModel,
                    paymentModel,
                    summaryModel,
                    reviewAndConfirmConfiguration,
                    advancedConfiguration.getDynamicFragmentConfiguration(),
                    itemsModel,
                    discountTermsAndConditions,
                    payer);

            return new ContainerProps(reviewAndConfirmContainerProps, linkableText);
        }

        throw new IllegalStateException("Unsupported parameters for Review and confirm activity");
    }

    private void setFloatingElevationVisibility(final View floatingConfirmLayout, final boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final float elevationInPixels =
                visible ? getBaseContext().getResources().getDimension(R.dimen.px_xxs_margin) : 0;
            floatingConfirmLayout.setElevation(elevationInPixels);
        }
    }

    @Override
    public void dispatch(final Action action) {
        if (action instanceof ChangePaymentMethodAction) {
            presenter.changePaymentMethod();
        } else if (action instanceof CancelPaymentAction) {
            onBackPressed();
        } else if (action instanceof ChangePayerInformationAction) {
            changePayerInformation();
        } else if (action instanceof ExitAction) {
            processCustomExit((ExitAction) action);
        } else {
            throw new UnsupportedOperationException("action not allowed");
        }
    }

    /**
     * Start the Payer Information flow to modify existing payer's data
     */
    private void changePayerInformation() {
        overrideTransitionIn();
        PayerInformationActivity.start(this, PAYER_INFORMATION_REQUEST_CODE);
    }

    /**
     * Called when user press back or Cancel action is dispatched.
     */
    @Override
    public void onBackPressed() {
        if (isExploding()) {
            return;
        }

        presenter.removeUserSelection();
        setResult(RESULT_CANCELED_RYC);
        super.onBackPressed();
    }

    private boolean isExploding() {
        return FragmentUtil.isFragmentVisible(getSupportFragmentManager(), TAG_EXPLODING_FRAGMENT);
    }

    /**
     * Custom exit added by Configuration.
     *
     * @param action custom exit action.
     */
    private void processCustomExit(final ExitAction action) {
        setResult(RESULT_CANCEL_PAYMENT, action.toIntent());
        finish();
    }

    // Opens CVV screen
    @Override
    public void showCardCVVRequired(@NonNull final Card card) {
        CardVaultActivity.startActivity(this, REQ_CODE_CARD_VAULT);
    }

    // Opens Card vault with recovery info.
    @Override
    public void startPaymentRecoveryFlow(final PaymentRecovery recovery) {
        CardVaultActivity.startActivityForRecovery(this, REQ_CODE_CARD_VAULT, recovery);
    }

    @Override
    public void showErrorScreen(@NonNull final MercadoPagoError error) {
        ErrorUtil.startErrorActivity(this, error);
    }

    @Override
    public void setPayButtonText(@NonNull final PayButtonViewModel payButtonViewModel) {
        confirmButton.setText(payButtonViewModel.getButtonText(this));
    }

    /**
     * When payment needs to start the visual payment processor it won't come back to review and confirm. The result for
     * the start activity will be delegated to Checkout activity.
     */
    @Override
    public void showPaymentProcessor() {
        overrideTransitionWithNoAnimation();
        final Intent intent = PaymentProcessorActivity.getIntent(this);
        intent.addFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

    /**
     * When payment is shown inside congrats the user can't return to review and confirm. Result for this activity will
     * be transferred.
     */
    @Override
    public void showResult(@NonNull final BusinessPaymentModel businessPaymentModel) {
        overrideTransitionFadeInFadeOut();
        final Intent intent = BusinessPaymentResultActivity.getIntent(this, businessPaymentModel);
        intent.addFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

    /**
     * When payment is shown inside congrats the user can't return to review and confirm. Result for this activity will
     * be transferred.
     */
    @Override
    public void showResult(@NonNull final PaymentResult paymentResult) {
        overrideTransitionFadeInFadeOut();
        final Intent intent = PaymentResultActivity.getIntent(this, paymentResult);
        intent.addFlags(FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
        finish();
    }

    @Override
    public void cancelCheckoutAndInformError(@NonNull final MercadoPagoError mercadoPagoError) {
        //TODO handle Error better - It goes back to checkout activity.
        // Goes to Checkout activity and provides error object.
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_ERROR, mercadoPagoError);
        setResult(RESULT_ERROR, intent);
        finish();
    }

    //TODO remove duplication
    void resolveErrorRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.recoverFromFailure();
        } else {
            final MercadoPagoError mercadoPagoError = data.getStringExtra(EXTRA_ERROR) == null ? null :
                JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            presenter.onError(mercadoPagoError);
        }
    }

    //TODO remove duplication
    void resolveCardVaultRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.onCardFlowResponse();
        } else {
            final MercadoPagoError mercadoPagoError =
                (data == null || data.getStringExtra(EXTRA_ERROR) == null) ? null :
                    JsonUtil.getInstance().fromJson(data.getStringExtra(EXTRA_ERROR), MercadoPagoError.class);
            if (mercadoPagoError == null) {
                presenter.onCardFlowCancel();
            } else {
                presenter.onError(mercadoPagoError);
            }
        }
    }

    @Override
    public void startLoadingButton(final int paymentTimeout, @NonNull final PayButtonViewModel payButtonViewModel) {
        final ExplodeParams explodeParams = ExplodingFragment.getParams(confirmButton,
            payButtonViewModel.getButtonProgressText(this), paymentTimeout);
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final ExplodingFragment explodingFragment = ExplodingFragment.newInstance(explodeParams);
        supportFragmentManager.beginTransaction()
            .replace(R.id.exploding_frame, explodingFragment, TAG_EXPLODING_FRAGMENT)
            .commitAllowingStateLoss();
        supportFragmentManager.executePendingTransactions();
    }

    @Override
    public void cancelLoadingButton() {
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final Fragment fragment = supportFragmentManager.findFragmentByTag(TAG_EXPLODING_FRAGMENT);
        if (fragment != null && fragment.isAdded()) {
            supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commitNowAllowingStateLoss();
        }
    }

    @Override
    public void finishLoading(@NonNull final ExplodeDecorator decorator) {
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final Fragment fragment = supportFragmentManager.findFragmentByTag(TAG_EXPLODING_FRAGMENT);
        if (fragment instanceof ExplodingFragment && fragment.isAdded() && fragment.isVisible()) {
            final ExplodingFragment explodingFragment = (ExplodingFragment) fragment;
            explodingFragment.finishLoading(decorator);
        } else {
            presenter.hasFinishPaymentAnimation();
        }
    }

    @Override
    public void hideConfirmButton() {
        confirmButton.setVisibility(View.GONE);
    }

    @Override
    public void showConfirmButton() {
        confirmButton.setVisibility(View.VISIBLE);
    }

    @SuppressLint("Range")
    @Override
    public void showErrorSnackBar(@NonNull final MercadoPagoError error) {
        MeliSnackbar.make(floatingLayout, error.getMessage(), Snackbar.LENGTH_LONG,
            MeliSnackbar.SnackbarType.ERROR).show();
    }

    @Override
    public void showDynamicDialog(@NonNull final DynamicDialogCreator creator,
        @NonNull final DynamicDialogCreator.CheckoutData checkoutData) {
        if (creator.shouldShowDialog(this, checkoutData)) {
            creator.create(this, checkoutData).show(getSupportFragmentManager(), TAG_DYNAMIC_DIALOG);
        }
    }

    @Override
    public void onAnimationFinished() {
        //we attach the view here for fix bug 5bda1d3f5de03a001b24f69a reported in bugsnag
        presenter.attachView(this);
        presenter.hasFinishPaymentAnimation();
    }

    /**
     * Exit review and confirm and notify the origin activity that payment method change button has been pressed.
     */
    @Override
    public void finishAndChangePaymentMethod() {
        setResult(RESULT_CHANGE_PAYMENT_METHOD);
        finish();
    }

    public static final class ContainerProps {
        /* default */ ReviewAndConfirmContainer.Props reviewAndConfirmContainerProps;
        /* default */ LinkableText linkableText;

        /* default */ ContainerProps(
            final ReviewAndConfirmContainer.Props reviewAndConfirmContainerProps,
            final LinkableText linkableText) {
            this.reviewAndConfirmContainerProps = reviewAndConfirmContainerProps;
            this.linkableText = linkableText;
        }
    }
}