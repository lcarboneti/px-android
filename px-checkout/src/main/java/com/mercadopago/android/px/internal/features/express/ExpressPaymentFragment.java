package com.mercadopago.android.px.internal.features.express;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadolibre.android.ui.widgets.MeliSnackbar;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.addons.internal.PXApplicationBehaviourProvider;
import com.mercadopago.android.px.addons.model.SecurityValidationData;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.SecurityCodeActivity;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultActivity;
import com.mercadopago.android.px.internal.features.checkout.CheckoutActivity;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.explode.ExplodeParams;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.features.express.animations.ExpandAndCollapseAnimation;
import com.mercadopago.android.px.internal.features.express.animations.FadeAnimationListener;
import com.mercadopago.android.px.internal.features.express.animations.FadeAnimator;
import com.mercadopago.android.px.internal.features.express.installments.InstallmentsAdapter;
import com.mercadopago.android.px.internal.features.express.slider.ConfirmButtonAdapter;
import com.mercadopago.android.px.internal.features.express.slider.HubAdapter;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodFragmentAdapter;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodHeaderAdapter;
import com.mercadopago.android.px.internal.features.express.slider.SplitPaymentHeaderAdapter;
import com.mercadopago.android.px.internal.features.express.slider.SummaryViewAdapter;
import com.mercadopago.android.px.internal.features.express.slider.TitlePagerAdapter;
import com.mercadopago.android.px.internal.features.plugins.PaymentProcessorActivity;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.FragmentUtil;
import com.mercadopago.android.px.internal.util.StatusBarDecorator;
import com.mercadopago.android.px.internal.util.VibrationUtils;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.DynamicHeightViewPager;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.LabeledSwitch;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.view.ScrollingPagerIndicator;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.TitlePager;
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.RenderMode;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ExpressPaymentFragment extends Fragment implements ExpressPayment.View, ViewPager.OnPageChangeListener,
    InstallmentsAdapter.ItemListener,
    SummaryView.OnFitListener,
    ExplodingFragment.ExplodingAnimationListener,
    SplitPaymentHeaderAdapter.SplitListener {

    private static final String TAG_EXPLODING_FRAGMENT = "TAG_EXPLODING_FRAGMENT";
    private static final String TAG_HEADER_DYNAMIC_DIALOG = "TAG_HEADER_DYNAMIC_DIALOG";
    private static final String EXTRA_RENDER_MODE = "render_mode";
    private static final int REQ_CODE_PAYMENT_PROCESSOR = 101;
    private static final int REQ_CODE_CARD_VAULT = 102;
    private static final int REQ_CODE_SECURITY_CODE = 103;
    private static final int REQ_CODE_BIOMETRICS = 104;
    private static final float PAGER_NEGATIVE_MARGIN_MULTIPLIER = -1.5f;

    @Nullable private CallBack callback;

    /* default */ ExpressPaymentPresenter presenter;

    private ActionBar actionBar;
    private ElementDescriptorView toolbarElementDescriptor;
    private SummaryView summaryView;
    private MeliButton confirmButton;
    private RecyclerView installmentsRecyclerView;
    /* default */ DynamicHeightViewPager paymentMethodPager;
    /* default */ View pagerAndConfirmButtonContainer;
    private ScrollingPagerIndicator indicator;
    private ExpandAndCollapseAnimation expandAndCollapseAnimation;
    private FadeAnimator fadeAnimation;
    @Nullable private Animation slideUpAndFadeAnimation;
    @Nullable private Animation slideDownAndFadeAnimation;
    private InstallmentsAdapter installmentsAdapter;
    private Animation toolbarAppearAnimation;
    private Animation toolbarDisappearAnimation;
    private TitlePager titlePager;
    private PaymentMethodHeaderView paymentMethodHeaderView;
    private LabeledSwitch splitPaymentView;
    private PaymentMethodFragmentAdapter paymentMethodFragmentAdapter;
    @RenderMode private String renderMode;

    private final HubAdapter hubAdapter = new HubAdapter();

    public static Fragment getInstance() {
        return new ExpressPaymentFragment();
    }

    @Override
    public void onSplitChanged(final boolean isChecked) {
        presenter.onSplitChanged(isChecked);
    }

    public interface CallBack {
        void onOneTapCanceled();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
        @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_express_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {

        configureViews(view);

        //TODO remove try catch after session is persisted
        try {
            presenter = createPresenter();
            presenter.attachView(this);
            if (savedInstanceState == null) {
                presenter.trackExpressView();
            } else {
                renderMode = savedInstanceState.getString(EXTRA_RENDER_MODE);
                presenter.recoverFromBundle(savedInstanceState);
            }
            presenter.loadViewModel();
        } catch (final Exception e) {
            if (savedInstanceState == null) {
                ErrorUtil.startErrorActivity(getActivity());
            } else {
                cancel();
            }
        }

        // Order is important - On click and events should be wired AFTER view is attached.
        summaryView.setOnFitListener(this);
        final View.OnClickListener listener = v -> presenter.onHeaderClicked();
        summaryView.setBigHeaderListener(listener);
        toolbarElementDescriptor.setOnClickListener(listener);

        toolbarAppearAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.px_toolbar_appear);
        toolbarDisappearAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.px_toolbar_disappear);

        confirmButton.setOnClickListener(v -> {
            if (ApiUtil.checkConnection(getContext())) {
                presenter.startSecuredPayment();
            } else {
                presenter.manageNoConnection();
            }
        });

        paymentMethodPager.addOnPageChangeListener(this);
    }

    private void configureViews(@NonNull final View view) {
        splitPaymentView = view.findViewById(R.id.labeledSwitch);
        titlePager = view.findViewById(R.id.title_pager);
        summaryView = view.findViewById(R.id.summary_view);

        toolbarElementDescriptor = view.findViewById(R.id.element_descriptor_toolbar);

        pagerAndConfirmButtonContainer = view.findViewById(R.id.container);
        paymentMethodPager = view.findViewById(R.id.payment_method_pager);
        indicator = view.findViewById(R.id.indicator);
        installmentsRecyclerView = view.findViewById(R.id.installments_recycler_view);
        confirmButton = view.findViewById(R.id.confirm_button);
        expandAndCollapseAnimation = new ExpandAndCollapseAnimation(installmentsRecyclerView);
        fadeAnimation = new FadeAnimator(view.getContext());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        installmentsRecyclerView.setLayoutManager(linearLayoutManager);
        installmentsRecyclerView
            .addItemDecoration(new DividerItemDecoration(view.getContext(), linearLayoutManager.getOrientation()));

        paymentMethodPager.setPageMargin(
            ((int) (getResources().getDimensionPixelSize(R.dimen.px_m_margin) * PAGER_NEGATIVE_MARGIN_MULTIPLIER)));
        paymentMethodPager.setOffscreenPageLimit(2);
        slideDownAndFadeAnimation.setAnimationListener(new FadeAnimationListener(paymentMethodPager, INVISIBLE));
        slideUpAndFadeAnimation.setAnimationListener(new FadeAnimationListener(paymentMethodPager, VISIBLE));

        paymentMethodHeaderView = view.findViewById(R.id.installments_header);
        paymentMethodHeaderView.setListener(new PaymentMethodHeaderView.Listener() {
            @Override
            public void onDescriptorViewClicked() {
                presenter.onInstallmentsRowPressed();
            }

            @Override
            public void onInstallmentsSelectorCancelClicked() {
                presenter.onInstallmentSelectionCanceled();
            }
        });

        configureToolbar(view);
    }

    private ExpressPaymentPresenter createPresenter() {
        final Session session = Session.getInstance();
        return new ExpressPaymentPresenter(session.getPaymentRepository(),
            session.getConfigurationModule().getPaymentSettings(),
            session.getConfigurationModule().getDisabledPaymentMethodRepository(),
            session.getDiscountRepository(),
            session.getAmountRepository(),
            session.getGroupsRepository(),
            session.getAmountConfigurationRepository(),
            session.getConfigurationModule().getChargeSolver(),
            session.getMercadoPagoESC(),
            session.getProductIdProvider(),
            PXApplicationBehaviourProvider.getSecurityBehaviour());
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putString(EXTRA_RENDER_MODE, renderMode);
        if (presenter != null) {
            super.onSaveInstanceState(presenter.storeInBundle(outState));
        } else {
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO remove null check after session is persisted
        if (presenter != null) {
            presenter.onViewResumed();
        }
    }

    @Override
    public void onPause() {
        if (presenter != null) {
            presenter.onViewPaused();
        }
        super.onPause();
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        slideDownAndFadeAnimation = AnimationUtils.loadAnimation(context, R.anim.px_slide_down_and_fade);
        slideUpAndFadeAnimation = AnimationUtils.loadAnimation(context, R.anim.px_slide_up_and_fade);
        if (context instanceof CallBack) {
            callback = (CallBack) context;
        }
    }

    @Override
    public void onDetach() {
        callback = null;
        slideDownAndFadeAnimation = null;
        slideUpAndFadeAnimation = null;
        //TODO remove null check after session is persisted
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDetach();
    }

    @Override
    public void configureAdapters(@NonNull final List<DrawableFragmentItem> items,
        @NonNull final Site site,
        @NonNull final HubAdapter.Model model) {

        if (paymentMethodPager.getAdapter() == null) {
            paymentMethodFragmentAdapter = new PaymentMethodFragmentAdapter(getChildFragmentManager(), items);
            if (renderMode == null) {
                summaryView.setMeasureListener((itemsClipped) -> {
                    summaryView.setMeasureListener(null);
                    renderMode = itemsClipped ? RenderMode.LOW_RES : RenderMode.HIGH_RES;
                    onRenderModeDecided();
                });
            } else {
                onRenderModeDecided();
            }
            paymentMethodPager.setAdapter(paymentMethodFragmentAdapter);
            indicator.attachToPager(paymentMethodPager);
        }

        installmentsAdapter = new InstallmentsAdapter(site, new ArrayList<>(), PayerCost.NO_SELECTED, this);
        installmentsRecyclerView.setAdapter(installmentsAdapter);
        installmentsRecyclerView.setVisibility(View.GONE);

        final TitlePagerAdapter titlePagerAdapter =
            new TitlePagerAdapter(model.paymentMethodDescriptorModels, titlePager);
        titlePager.setAdapter(titlePagerAdapter);

        hubAdapter.update(Arrays.asList(titlePagerAdapter,
            new SummaryViewAdapter(model.summaryViewModels, summaryView),
            new SplitPaymentHeaderAdapter(model.splitModels, splitPaymentView, this),
            new PaymentMethodHeaderAdapter(model.paymentMethodDescriptorModels, paymentMethodHeaderView),
            new ConfirmButtonAdapter(model.confirmButtonViewModels, confirmButton)
        ));
    }

    @Override
    public void setPayButtonText(@NonNull final PayButtonViewModel payButtonViewModel) {
        confirmButton.setText(payButtonViewModel.getButtonText(getContext()));
    }

    @Override
    public void cancel() {
        if (callback != null) {
            callback.onOneTapCanceled();
        }
    }

    @Override
    public void showInstallmentsList(final List<PayerCost> payerCostList, final int payerCostSelected) {
        animateViewPagerDown();
        installmentsRecyclerView.scrollToPosition(payerCostSelected);
        installmentsAdapter.setPayerCosts(payerCostList);
        installmentsAdapter.setPayerCostSelected(payerCostSelected);
        installmentsAdapter.notifyDataSetChanged();
        hubAdapter.showInstallmentsList();
        expandAndCollapseAnimation.expand();
    }

    private void animateViewPagerDown() {
        paymentMethodPager.startAnimation(slideDownAndFadeAnimation);
        fadeAnimation.fadeOutFast(confirmButton);
        fadeAnimation.fadeOutFast(indicator);
    }

    @Override
    public void showToolbarElementDescriptor(@NonNull final ElementDescriptorView.Model elementDescriptorModel) {
        toolbarElementDescriptor.update(elementDescriptorModel);
        toolbarElementDescriptor.setVisibility(VISIBLE);
    }

    @Override
    public void collapseInstallmentsSelection() {
        paymentMethodPager.startAnimation(slideUpAndFadeAnimation);
        fadeAnimation.fadeIn(confirmButton);
        fadeAnimation.fadeIn(indicator);
        expandAndCollapseAnimation.collapse();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQ_CODE_CARD_VAULT) {
            handleCardVaultResult(resultCode);
        } else if (requestCode == REQ_CODE_SECURITY_CODE && resultCode == RESULT_OK) {
            presenter.onTokenResolved();
        } else if (requestCode == REQ_CODE_BIOMETRICS) {
            handleBiometricsResult(resultCode);
        } else if (resultCode == Constants.RESULT_ACTION) {
            handleAction(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleBiometricsResult(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.confirmPayment();
        } else {
            presenter.trackSecurityFriction();
        }
    }

    private void handleCardVaultResult(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.onTokenResolved();
        } else if (resultCode == RESULT_CANCELED) {
            presenter.trackExpressView();
        }
    }

    private void handleAction(final Intent data) {
        if (data != null && data.getExtras() != null) {
            PostPaymentAction.fromBundle(data.getExtras()).execute(presenter);
        }
    }

    @Override
    public void showPaymentProcessor() {
        final Intent intent = PaymentProcessorActivity.getIntent(getContext());
        startActivityForResult(intent, REQ_CODE_PAYMENT_PROCESSOR);
    }

    @Override
    public void showErrorScreen(@NonNull final MercadoPagoError error) {
        if (getActivity() != null) {
            ((CheckoutActivity) getActivity()).presenter.onPaymentError(error);
        }
    }

    @SuppressLint("Range")
    @Override
    public void showErrorSnackBar(@NonNull final MercadoPagoError error) {
        if (getView() != null && getActivity() != null) {
            MeliSnackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_LONG, MeliSnackbar.SnackbarType.ERROR)
                .show();
        }
    }

    @Override
    public void updateViewForPosition(final int paymentMethodIndex,
        final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState) {
        hubAdapter.updateData(paymentMethodIndex, payerCostSelected, splitSelectionState);
    }

    //TODO refactor
    @Override
    public void showPaymentResult(@NonNull final IPaymentDescriptor paymentResult) {
        if (getActivity() != null) {
            ((CheckoutActivity) getActivity()).presenter.onPaymentFinished(paymentResult);
        }
    }

    @Override
    public void startSecurityValidation(@NonNull final SecurityValidationData data) {
        PXApplicationBehaviourProvider.getSecurityBehaviour().startValidation(this, data, REQ_CODE_BIOMETRICS);
    }

    //FIXME Used to start payment from activity
    @Override
    public void startPayment() {
        presenter.confirmPayment();
    }

    @Override
    public void finishLoading(@NonNull final ExplodeDecorator params) {
        final ExplodingFragment fragment =
            FragmentUtil.getFragmentByTag(getChildFragmentManager(), TAG_EXPLODING_FRAGMENT, ExplodingFragment.class);
        if (fragment != null) {
            fragment.finishLoading(params);
        } else {
            presenter.hasFinishPaymentAnimation();
        }
    }

    @Override
    public void cancelLoading() {
        showConfirmButton();
        final FragmentManager childFragmentManager = getChildFragmentManager();
        final ExplodingFragment fragment =
            (ExplodingFragment) childFragmentManager.findFragmentByTag(TAG_EXPLODING_FRAGMENT);
        if (fragment != null && fragment.isAdded() && fragment.hasFinished()) {
            childFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commitNowAllowingStateLoss();
            restoreStatusBar();
        }
    }

    private void restoreStatusBar() {
        if (getActivity() != null) {
            new StatusBarDecorator(getActivity().getWindow())
                .setupStatusBarColor(ContextCompat.getColor(getActivity(), R.color.px_colorPrimaryDark));
        }
    }

    private void configureToolbar(final View view) {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null) {
            final Toolbar toolbar = view.findViewById(R.id.toolbar);
            activity.setSupportActionBar(toolbar);

            actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                enableToolbarBack();
            }
            toolbar.setNavigationOnClickListener(v -> presenter.cancel());
        }
    }

    private void showConfirmButton() {
        confirmButton.clearAnimation();
        confirmButton.setVisibility(VISIBLE);
    }

    private void hideConfirmButton() {
        confirmButton.setClickable(false);
        confirmButton.clearAnimation();
        confirmButton.setVisibility(INVISIBLE);
    }

    @Override
    public void enableToolbarBack() {
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public void disableToolbarBack() {
        actionBar.setHomeButtonEnabled(false);
    }

    @Override
    public void startLoadingButton(final int paymentTimeout, @NonNull final PayButtonViewModel payButtonViewModel) {
        hideConfirmButton();
        ViewUtils.runWhenViewIsFullyMeasured(getView(), () -> {
            final ExplodeParams explodeParams = ExplodingFragment.getParams(confirmButton,
                payButtonViewModel.getButtonProgressText(confirmButton.getContext()), paymentTimeout);
            final FragmentManager childFragmentManager = getChildFragmentManager();
            final ExplodingFragment explodingFragment = ExplodingFragment.newInstance(explodeParams);
            childFragmentManager.beginTransaction()
                .replace(R.id.exploding_frame, explodingFragment, TAG_EXPLODING_FRAGMENT)
                .commitNowAllowingStateLoss();
        });
    }

    @Override
    public void showSecurityCodeScreen(@NonNull final Card card) {
        SecurityCodeActivity.startForSavedCard(card, this, REQ_CODE_SECURITY_CODE);
    }

    @Override
    public void showCardFlow(@NonNull final PaymentRecovery paymentRecovery) {
        CardVaultActivity.startActivityForRecovery(this, REQ_CODE_CARD_VAULT, paymentRecovery);
    }

    @Override
    public void onClick(final PayerCost payerCostSelected) {
        presenter.onPayerCostSelected(payerCostSelected);
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        hubAdapter.updatePosition(positionOffset, position);
    }

    @Override
    public void onPageSelected(final int position) {
        presenter.onSliderOptionSelected(position);
        VibrationUtils.smallVibration(getContext());
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        // do nothing.
    }

    @Override
    public void onBigHeaderOverlaps() {
        toolbarElementDescriptor.startAnimation(toolbarAppearAnimation);
    }

    @Override
    public void onBigHeaderDoesNotOverlaps() {
        toolbarElementDescriptor.startAnimation(toolbarDisappearAnimation);
    }

    @Override
    public void showDiscountDetailDialog(@NonNull final DiscountConfigurationModel discountModel) {
        DiscountDetailDialog.showDialog(getFragmentManager(), discountModel);
    }

    @Override
    public void onAnimationFinished() {
        presenter.hasFinishPaymentAnimation();
    }

    @Override
    public boolean isExploding() {
        return FragmentUtil.isFragmentVisible(getChildFragmentManager(), TAG_EXPLODING_FRAGMENT);
    }

    @Override
    public void resetPagerIndex() {
        paymentMethodPager.setCurrentItem(0);
    }

    @Override
    public void showDynamicDialog(@NonNull final DynamicDialogCreator creator,
        @NonNull final DynamicDialogCreator.CheckoutData checkoutData) {
        if (creator.shouldShowDialog(getContext(), checkoutData)) {
            creator.create(getContext(), checkoutData).show(getChildFragmentManager(),
                TAG_HEADER_DYNAMIC_DIALOG);
        }
    }

    private void onRenderModeDecided() {
        //Workaround to pager not updating the fragments
        paymentMethodPager.post(() -> paymentMethodFragmentAdapter.setRenderMode(renderMode));
    }
}