package com.mercadopago.android.px.internal.features.installments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.controllers.CheckoutTimer;
import com.mercadopago.android.px.internal.di.ConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.express.installments.InstallmentsAdapter;
import com.mercadopago.android.px.internal.features.uicontrollers.FontCache;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.internal.features.uicontrollers.card.FrontCardView;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.ScaleUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.AmountView;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.math.BigDecimal;
import java.util.List;

public class InstallmentsActivity extends PXActivity<InstallmentsPresenter> implements InstallmentsView {

    private RecyclerView installmentsRecyclerView;

    //Local vars
    protected boolean mActivityActive;

    //ViewMode
    protected boolean mLowResActive;

    //Low Res View
    protected Toolbar mLowResToolbar;
    protected MPTextView mLowResTitleToolbar;

    //Normal View
    protected CollapsingToolbarLayout mCollapsingToolbar;
    protected AppBarLayout mAppBar;
    protected FrameLayout mCardContainer;
    protected Toolbar mNormalToolbar;
    protected FrontCardView mFrontCardView;
    protected MPTextView mTimerTextView;

    private static final int TOOLBAR_TEXT_SIZE = 19;
    private static final int TIMER_TEXT_SIZE = 17;
    private static final String CARD_INFO_KEY = "cardInfo";

    private AmountView amountView;
    private PaymentSettingRepository configuration;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Session session = Session.getSession(this);
        final ConfigurationModule configurationModule = session.getConfigurationModule();
        configuration = configurationModule.getPaymentSettings();
        presenter = new InstallmentsPresenter(session.getAmountRepository(), configuration,
            configurationModule.getUserSelectionRepository(), session.getDiscountRepository(),
            session.getSummaryAmountRepository(), session.getAmountConfigurationRepository(),
            session.providePayerCostSolver());

        getActivityParameters();
        presenter.attachView(this);

        mActivityActive = true;
        mLowResActive = ScaleUtil.isLowRes(this);
        setContentView();
        initializeControls();
        initializeView();

        presenter.initialize();
    }

    private void getActivityParameters() {
        final Intent intent = getIntent();
        presenter.setCardInfo(JsonUtil.getInstance().fromJson(intent.getStringExtra(CARD_INFO_KEY), CardInfo.class));
    }

    public void setContentView() {
        if (mLowResActive) {
            setContentViewLowRes();
        } else {
            setContentViewNormal();
        }
    }

    public void setContentViewLowRes() {
        setContentView(R.layout.px_activity_installments_lowres);
    }

    public void setContentViewNormal() {
        setContentView(R.layout.px_activity_installments_normal);
    }

    private void initializeControls() {
        installmentsRecyclerView = findViewById(R.id.mpsdkActivityInstallmentsView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        installmentsRecyclerView.setLayoutManager(linearLayoutManager);
        installmentsRecyclerView
            .addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);
        amountView = findViewById(R.id.amount_view);

        if (mLowResActive) {
            initializeLowResControls();
        } else {
            initializeNormalControls();
        }
    }

    private void initializeLowResControls() {
        mLowResToolbar = findViewById(R.id.mpsdkRegularToolbar);
        mLowResTitleToolbar = findViewById(R.id.mpsdkTitle);

        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            final Toolbar.LayoutParams marginParams =
                new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            marginParams.setMargins(0, 0, 0, 6);
            mLowResTitleToolbar.setLayoutParams(marginParams);
            mLowResTitleToolbar.setTextSize(TOOLBAR_TEXT_SIZE);
            mTimerTextView.setTextSize(TIMER_TEXT_SIZE);
        }

        mLowResToolbar.setVisibility(View.VISIBLE);
    }

    private void initializeNormalControls() {
        mCollapsingToolbar = findViewById(R.id.mpsdkCollapsingToolbar);
        mAppBar = findViewById(R.id.mpsdkInstallmentesAppBar);
        mCardContainer = findViewById(R.id.mpsdkActivityCardContainer);
        mNormalToolbar = findViewById(R.id.mpsdkRegularToolbar);
        mNormalToolbar.setVisibility(View.VISIBLE);
    }

    private void initializeView() {
        loadViews();
        hideHeader();
        showTimer();
    }

    public void loadViews() {
        if (mLowResActive) {
            loadLowResViews();
        } else {
            loadNormalViews();
        }
    }

    @Override
    public void warnAboutBankInterests() {
        final FrameLayout mNoInstallmentsRate = findViewById(R.id.mpsdkNoInstallmentsRate);
        mNoInstallmentsRate.setVisibility(View.VISIBLE);
        final MPTextView mNoInstallmentsRateTextView = findViewById(R.id.mpsdkNoInstallmentsRateTextView);
        mNoInstallmentsRateTextView.setVisibility(View.VISIBLE);
        mNoInstallmentsRateTextView.setText(R.string.px_interest_label);
    }

    public void loadLowResViews() {
        loadToolbarArrow(mLowResToolbar);
        mLowResTitleToolbar.setText(getString(R.string.px_card_installments_title));

        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mLowResTitleToolbar.setTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    public void loadNormalViews() {
        loadToolbarArrow(mNormalToolbar);
        mNormalToolbar.setTitle(getString(R.string.px_card_installments_title));
        setCustomFontNormal();

        mFrontCardView = new FrontCardView(this, CardRepresentationModes.SHOW_FULL_FRONT_ONLY);
        mFrontCardView.setSize(CardRepresentationModes.MEDIUM_SIZE);
        mFrontCardView.setPaymentMethod(presenter.getPaymentMethod());
        if (presenter.getCardInfo() != null) {
            mFrontCardView.setCardNumberLength(presenter.getCardNumberLength());
            mFrontCardView.setLastFourDigits(presenter.getCardInfo().getLastFourDigits());
        }
        mFrontCardView.inflateInParent(mCardContainer, true);
        mFrontCardView.initializeControls();
        mFrontCardView.draw();
        mFrontCardView.enableEditingCardNumber();
    }

    private void loadToolbarArrow(final Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void setCustomFontNormal() {
        if (FontCache.hasTypeface(FontCache.CUSTOM_REGULAR_FONT)) {
            mCollapsingToolbar.setCollapsedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
            mCollapsingToolbar.setExpandedTitleTypeface(FontCache.getTypeface(FontCache.CUSTOM_REGULAR_FONT));
        }
    }

    private void hideHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.GONE);
        } else {
            mNormalToolbar.setTitle("");
        }
    }

    private void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    private void showHeader() {
        if (mLowResActive) {
            mLowResToolbar.setVisibility(View.VISIBLE);
        } else {
            mNormalToolbar.setTitle(getString(R.string.px_card_installments_title));
            setCustomFontNormal();
        }
    }

    @Override
    public void showApiErrorScreen(final ApiException apiException, final String requestOrigin) {
        ErrorUtil.showApiExceptionError(this, apiException, requestOrigin);
    }

    @Override
    public void showInstallments(final List<PayerCost> payerCostList) {
        showHeader();
        final InstallmentsAdapter installmentsAdapter =
            new InstallmentsAdapter(configuration.getSite(),
                payerCostList, presenter);
        installmentsRecyclerView.setAdapter(installmentsAdapter);
    }

    @Override
    public void showLoadingView() {
        installmentsRecyclerView.setVisibility(View.GONE);
        ViewUtils.showProgressLayout(this);
    }

    @Override
    public void hideLoadingView() {
        installmentsRecyclerView.setVisibility(View.VISIBLE);
        ViewUtils.showRegularLayout(this);
    }

    @Override
    public void showErrorNoPayerCost() {
        ErrorUtil.startErrorActivity(this, new MercadoPagoError(getString(R.string.px_standard_error_message),
            getString(R.string.px_error_message_detail_no_payer_cost_found), false));
    }

    @Override
    public void finishWithResult() {
        final Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
        animateTransitionSlideInSlideOut();
    }

    public void animateTransitionSlideInSlideOut() {
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    public void onBackPressed() {
        presenter.removeUserSelection();
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                presenter.recoverFromFailure();
            } else {
                setResult(resultCode, data);
                finish();
            }
        } else {
            setResult(resultCode, data);
            finish();
        }
    }

    @Override
    public void showAmount(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final BigDecimal itemsPlusCharges, @NonNull final Site site) {
        amountView.setVisibility(View.VISIBLE);
        amountView.setOnClickListener(presenter);
        amountView.show(discountModel, itemsPlusCharges, site);
    }

    @Override
    public void hideAmountRow() {
        amountView.setVisibility(View.GONE);
    }

    @Override
    public void showDetailDialog(@NonNull final DiscountConfigurationModel discountModel) {
        DiscountDetailDialog.showDialog(getSupportFragmentManager(), discountModel);
    }
}
