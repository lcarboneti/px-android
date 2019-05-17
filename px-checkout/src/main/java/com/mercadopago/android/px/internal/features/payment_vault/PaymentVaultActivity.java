package com.mercadopago.android.px.internal.features.payment_vault;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.adapters.PaymentMethodSearchItemAdapter;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.controllers.CheckoutTimer;
import com.mercadopago.android.px.internal.datasource.PaymentVaultTitleSolverImpl;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultActivity;
import com.mercadopago.android.px.internal.features.disable_payment_method.DisabledPaymentMethodDetailDialog;
import com.mercadopago.android.px.internal.features.payer_information.PayerInformationActivity;
import com.mercadopago.android.px.internal.features.uicontrollers.FontCache;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.ScaleUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.AmountView;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.GridSpacingItemDecoration;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.viewmodel.PaymentMethodViewModel;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.views.SelectMethodView;
import java.math.BigDecimal;
import java.util.List;

import static com.mercadopago.android.px.core.MercadoPagoCheckout.EXTRA_ERROR;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_SILENT_ERROR;

public class PaymentVaultActivity extends PXActivity<PaymentVaultPresenter> implements PaymentVaultView {

    public static final int COLUMN_SPACING_DP_VALUE = 20;
    public static final int COLUMNS = 2;
    private static final int PAYER_INFORMATION_REQUEST_CODE = 22;
    private static final int REQ_CARD_VAULT = 102;
    private static final String EXTRA_SELECTED_SEARCH_ITEM = "selectedSearchItem";
    private static final String EXTRA_AUTOMATIC_SELECTION = "automaticSelection";
    private static final String MISMATCHING_PAYMENT_METHOD_ERROR = "Payment method in search not found";

    // Local vars
    protected boolean mActivityActive;
    protected Token mToken;
    protected Issuer mSelectedIssuer;
    protected Card mSelectedCard;
    protected Context mContext;

    // Controls
    protected RecyclerView mSearchItemsRecyclerView;
    protected AppBarLayout mAppBar;

    protected CollapsingToolbarLayout mAppBarLayout;
    protected MPTextView mTimerTextView;

    protected View mProgressLayout;

    private AmountView amountView;
    private final PaymentMethodSearchItemAdapter groupsAdapter = new PaymentMethodSearchItemAdapter();
    private boolean automaticSelection;

    public static void start(@NonNull final AppCompatActivity from) {
        final Intent intent = new Intent(from, PaymentVaultActivity.class);
        from.startActivityForResult(intent, Constants.Activities.PAYMENT_VAULT_REQUEST_CODE);
        from.overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    public static void startWithPaymentMethodSelected(@NonNull final AppCompatActivity from,
        @NonNull final PaymentMethodSearchItem item) {
        final Intent intent = new Intent(from, PaymentVaultActivity.class);
        intent.putExtra(EXTRA_SELECTED_SEARCH_ITEM, JsonUtil.getInstance().toJson(item));
        from.startActivityForResult(intent, Constants.Activities.PAYMENT_VAULT_REQUEST_CODE);
        from.overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Session session = Session.getSession(this);
        final PaymentSettingRepository configuration = session.getConfigurationModule().getPaymentSettings();
        presenter = new PaymentVaultPresenter(configuration,
            session.getConfigurationModule().getUserSelectionRepository(),
            session.getConfigurationModule().getDisabledPaymentMethodRepository(),
            session.getDiscountRepository(),
            session.getInitRepository(),
            session.getMercadoPagoESC(),
            new PaymentVaultTitleSolverImpl(getApplicationContext(),
                configuration.getAdvancedConfiguration().getCustomStringConfiguration()));

        getActivityParameters();
        configurePresenter();
        setContentView();
        initializeControls();

        //Avoid automatic selection if activity restored on back pressed from next step
        if (savedInstanceState != null) {
            automaticSelection = savedInstanceState.getBoolean(EXTRA_AUTOMATIC_SELECTION);
        }
        if (!automaticSelection) {
            initialize();
        }
        validatePaymentConfiguration();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_AUTOMATIC_SELECTION, automaticSelection);
    }

    @Override
    public void saveAutomaticSelection(final boolean automaticSelection) {
        this.automaticSelection = automaticSelection;
    }

    //TODO remove method after session is persisted
    private void validatePaymentConfiguration() {
        final Session session = Session.getSession(this);
        try {
            session.getConfigurationModule().getPaymentSettings().getPaymentConfiguration().getCharges();
            session.getConfigurationModule().getPaymentSettings().getPaymentConfiguration().getPaymentProcessor();
        } catch (Exception e) {
            FrictionEventTracker.with(SelectMethodView.PATH_PAYMENT_VAULT,
                FrictionEventTracker.Id.SILENT, FrictionEventTracker.Style.SCREEN,
                ErrorUtil.getStacktraceMessage(e));

            exitCheckout(RESULT_SILENT_ERROR);
        }
    }

    public void exitCheckout(final int resCode) {
        overrideTransitionOut();
        setResult(resCode);
        finish();
    }

    private void configurePresenter() {
        presenter.attachView(this);
    }

    protected void setContentView() {
        setContentView(R.layout.px_activity_payment_vault);
    }

    protected void getActivityParameters() {
        final Intent intent = getIntent();

        final JsonUtil instance = JsonUtil.getInstance();

        if (intent.getStringExtra(EXTRA_SELECTED_SEARCH_ITEM) != null) {
            presenter.setSelectedSearchItem(instance
                .fromJson(intent.getStringExtra(EXTRA_SELECTED_SEARCH_ITEM), PaymentMethodSearchItem.class));
        }
    }

    protected void initializeControls() {
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);

        amountView = findViewById(R.id.amount_view);
        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);

        initializePaymentOptionsRecyclerView();
        mAppBar = findViewById(R.id.mpsdkAppBar);
        mAppBarLayout = findViewById(R.id.mpsdkCollapsingToolbar);
        initializeToolbar();
    }

    protected void initialize() {
        showTimer();
        presenter.initialize();
    }

    private void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    private void initializeToolbar() {
        final Toolbar toolbar = findViewById(R.id.mpsdkToolbar);
        setSupportActionBar(toolbar);
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(false);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mAppBarLayout.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            mAppBarLayout.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    protected void initializePaymentOptionsRecyclerView() {
        final int columns = COLUMNS;
        mSearchItemsRecyclerView = findViewById(R.id.mpsdkGroupsList);
        mSearchItemsRecyclerView.setLayoutManager(new GridLayoutManager(this, columns));
        mSearchItemsRecyclerView.addItemDecoration(
            new GridSpacingItemDecoration(columns, ScaleUtil.getPxFromDp(COLUMN_SPACING_DP_VALUE, this), true));
        mSearchItemsRecyclerView.setAdapter(groupsAdapter);
    }

    @Override
    public void showSelectedItem(final PaymentMethodSearchItem item) {
        startWithPaymentMethodSelected(this, item);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        presenter.initializeAmountRow();
        if (requestCode == REQ_CARD_VAULT) {
            resolveCardRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.PAYMENT_METHODS_REQUEST_CODE) {
            presenter.onPaymentMethodReturned();
        } else if (requestCode == Constants.Activities.PAYMENT_VAULT_REQUEST_CODE) {
            resolvePaymentVaultRequest(resultCode, data);
        } else if (requestCode == PAYER_INFORMATION_REQUEST_CODE) {
            resolvePayerInformationRequest(resultCode);
        } else if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            resolveErrorRequest(resultCode, data);
            overrideTransitionOut();
        }
    }

    private void resolveErrorRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            recoverFromFailure();
        } else if (presenter.isItemSelected()) {
            hideProgress();
        } else {
            setResult(resultCode, data);
            finish();
        }
    }

    private void recoverFromFailure() {
        presenter.recoverFromFailure();
    }

    private void resolvePaymentVaultRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        } else if (resultCode == RESULT_SILENT_ERROR) {
            setResult(RESULT_SILENT_ERROR);
            finish();
        } else if (resultCode == RESULT_CANCELED && data != null && data.hasExtra(EXTRA_ERROR)) {
            setResult(Activity.RESULT_CANCELED, data);
            finish();
        } else {
            //When it comes back from payment vault "children" view
            presenter.trackScreen();

            if (shouldFinishOnBack(data)) {
                setResult(Activity.RESULT_CANCELED, data);
                finish();
            } else {
                overridePendingTransition(R.anim.px_slide_left_to_right_in, R.anim.px_slide_left_to_right_out);
            }
        }
    }

    protected void resolveCardRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            showProgress();
            mToken = JsonUtil.getInstance().fromJson(data.getStringExtra("token"), Token.class);
            mSelectedIssuer = JsonUtil.getInstance().fromJson(data.getStringExtra("issuer"), Issuer.class);
            mSelectedCard = JsonUtil.getInstance().fromJson(data.getStringExtra("card"), Card.class);
            finishWithCardResult();
        } else if (resultCode == RESULT_SILENT_ERROR) {
            setResult(RESULT_SILENT_ERROR);
            finish();
        } else {
            presenter.trackScreen();

            if (shouldFinishOnBack(data)) {
                setResult(Activity.RESULT_CANCELED, data);
                finish();
            } else {
                overridePendingTransition(R.anim.px_slide_left_to_right_in, R.anim.px_slide_left_to_right_out);
            }
        }
    }

    private void resolvePayerInformationRequest(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.onPayerInformationReceived();
        } else {
            overridePendingTransition(R.anim.px_slide_left_to_right_in, R.anim.px_slide_left_to_right_out);
        }
    }

    private boolean shouldFinishOnBack(final Intent data) {
        return presenter.getSelectedSearchItem() == null ||
            (presenter.getSelectedSearchItem() != null &&
                (!presenter.getSelectedSearchItem().hasChildren()
                    || (presenter.getSelectedSearchItem().getChildren().size() == 1))
                || (presenter.getSelectedSearchItem() == null &&
                presenter.isOnlyOneItemAvailable()) ||
                (data != null) && (data.getStringExtra(EXTRA_ERROR) != null));
    }

    @Override
    public void finishPaymentMethodSelection(final PaymentMethod paymentMethod) {
        finishWith(paymentMethod);
    }

    private void finishWith(final PaymentMethod paymentMethod) {
        final Intent returnIntent = new Intent();
        returnIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(paymentMethod));
        finishWithResult(returnIntent);
    }

    protected void finishWithCardResult() {
        final Intent returnIntent = new Intent();
        returnIntent.putExtra("token", JsonUtil.getInstance().toJson(mToken));
        if (mSelectedIssuer != null) {
            returnIntent.putExtra("issuer", JsonUtil.getInstance().toJson(mSelectedIssuer));
        }
        returnIntent.putExtra("card", JsonUtil.getInstance().toJson(mSelectedCard));
        finishWithResult(returnIntent);
    }

    private void finishWithResult(final Intent returnIntent) {
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        overrideTransitionIn();
    }

    @Override
    public void showProgress() {
        mProgressLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mProgressLayout.setVisibility(View.GONE);
    }

    @Override
    public void showSearchItems(final List<PaymentMethodViewModel> searchItems) {
        groupsAdapter.setItems(searchItems);
        groupsAdapter.notifyDataSetChanged();
    }

    @Override
    public void setTitle(final String title) {
        if (mAppBarLayout != null) {
            mAppBarLayout.setTitle(title);
        }
    }

    @Override
    public void startSavedCardFlow(final Card card) {
        CardVaultActivity.startActivity(this, REQ_CARD_VAULT);
        overrideTransitionIn();
    }

    @Override
    public void startCardFlow() {
        CardVaultActivity.startActivity(this, REQ_CARD_VAULT);
        overrideTransitionIn();
    }

    @Override
    public void startPaymentMethodsSelection(final PaymentPreference paymentPreference) {
        new Constants.Activities.PaymentMethodsActivityBuilder()
            .setActivity(this)
            .setPaymentPreference(paymentPreference)
            .startActivity();
    }

    public void showApiException(final ApiException apiException, final String requestOrigin) {
        if (mActivityActive) {
            ErrorUtil.showApiExceptionError(this, apiException, requestOrigin);
        }
    }

    @Override
    public void showError(final MercadoPagoError error, final String requestOrigin) {
        if (error.isApiException()) {
            showApiException(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        presenter.trackOnBackPressed();
        finish();
        overrideTransitionOut();
    }

    @Override
    protected void onResume() {
        mActivityActive = true;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mActivityActive = false;
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mActivityActive = false;
        super.onPause();
    }

    @Override
    protected void onStop() {
        mActivityActive = false;
        super.onStop();
    }

    @Override
    public void collectPayerInformation() {
        overrideTransitionIn();
        PayerInformationActivity.start(this, PAYER_INFORMATION_REQUEST_CODE);
    }

    @Override
    public void showDetailDialog(@NonNull final DiscountConfigurationModel discountModel) {
        DiscountDetailDialog.showDialog(getSupportFragmentManager(), discountModel);
    }

    @Override
    public void showEmptyPaymentMethodsError() {
        final String errorMessage = getString(R.string.px_no_payment_methods_found);
        showError(MercadoPagoError.createNotRecoverable(errorMessage), TextUtil.EMPTY);
    }

    @Override
    public void showMismatchingPaymentMethodError() {
        final String errorMessage = getString(R.string.px_standard_error_message);
        showError(MercadoPagoError.createNotRecoverable(errorMessage, MISMATCHING_PAYMENT_METHOD_ERROR),
            TextUtil.EMPTY);
    }

    @Override
    public void showAmount(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final BigDecimal totalAmount,
        @NonNull final Site site) {
        amountView.setOnClickListener(presenter);
        amountView.show(discountModel, totalAmount, site);
    }

    @Override
    public void hideAmountRow() {
        amountView.setVisibility(View.GONE);
    }

    @Override
    public void showDisabledPaymentMethodDetailDialog(@NonNull final String paymentMethodType) {
        DisabledPaymentMethodDetailDialog.showDialog(getSupportFragmentManager(), paymentMethodType);
    }
}