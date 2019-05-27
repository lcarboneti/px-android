package com.mercadopago.android.px.internal.features.express;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
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
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadolibre.android.ui.widgets.MeliSnackbar;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.CheckoutActivity;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.cardvault.CardVaultActivity;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.explode.ExplodeParams;
import com.mercadopago.android.px.internal.features.explode.ExplodingFragment;
import com.mercadopago.android.px.internal.features.express.animations.ExpandAndCollapseAnimation;
import com.mercadopago.android.px.internal.features.express.animations.FadeAnimator;
import com.mercadopago.android.px.internal.features.express.animations.SlideAnim;
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
import com.mercadopago.android.px.internal.util.FragmentUtil;
import com.mercadopago.android.px.internal.util.StatusBarDecorator;
import com.mercadopago.android.px.internal.util.VibrationUtils;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.FixedAspectRatioFrameLayout;
import com.mercadopago.android.px.internal.view.LabeledSwitch;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.view.ScrollingPagerIndicator;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.TitlePager;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.BusinessPayment;
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
    private static final int REQ_CODE_CARD_VAULT = 0x999;
    private static final int REQ_CODE_PAYMENT_PROCESSOR = 0x123;
    private static final float PAGER_NEGATIVE_MARGIN_MULTIPLIER = -1.5f;

    // Width / Height
    @NonNull private static final Pair<Integer, Integer> ASPECT_RATIO_HIGH_RES = new Pair<>(850, 460);
    // Width / Height
    @NonNull private static final Pair<Integer, Integer> ASPECT_RATIO_LOW_RES = new Pair<>(288, 98);

    private CallBack callback;

    /* default */ ExpressPaymentPresenter presenter;

    private ActionBar actionBar;
    private ElementDescriptorView toolbarElementDescriptor;
    private SummaryView summaryView;
    private MeliButton confirmButton;
    private RecyclerView installmentsRecyclerView;
    /* default */ ViewPager paymentMethodPager;
    /* default */ View pagerAndConfirmButtonContainer;
    private ScrollingPagerIndicator indicator;
    private ExpandAndCollapseAnimation expandAndCollapseAnimation;
    private FadeAnimator fadeAnimation;
    private SlideAnim paymentMethodSlideAnim;
    private InstallmentsAdapter installmentsAdapter;
    private FixedAspectRatioFrameLayout aspectRatioContainer;
    private Animation toolbarAppearAnimation;
    private Animation toolbarDisappearAnimation;
    private TitlePager titlePager;
    private PaymentMethodHeaderView paymentMethodHeaderView;
    private LabeledSwitch splitPaymentView;
    private PaymentMethodFragmentAdapter paymentMethodFragmentAdapter;

    private final HubAdapter hubAdapter = new HubAdapter();

    public static Fragment getInstance() {
        return new ExpressPaymentFragment();
    }

    @Override
    public void onSplitChanged(final boolean isChecked) {
        presenter.onSplitChanged(isChecked, paymentMethodPager.getCurrentItem());
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
            presenter = createPresenter(view.getContext());
            presenter.attachView(this);
            if (savedInstanceState == null) {
                presenter.trackExpressView();
            } else {
                presenter.recoverFromBundle(savedInstanceState);
            }
            presenter.loadViewModel();
        } catch (final Exception e) {
            cancel();
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
                presenter.confirmPayment(paymentMethodPager.getCurrentItem());
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
        aspectRatioContainer = view.findViewById(R.id.aspect_ratio_container);
        paymentMethodPager = view.findViewById(R.id.payment_method_pager);
        indicator = view.findViewById(R.id.indicator);
        installmentsRecyclerView = view.findViewById(R.id.installments_recycler_view);
        confirmButton = view.findViewById(R.id.confirm_button);
        expandAndCollapseAnimation = new ExpandAndCollapseAnimation(installmentsRecyclerView);
        fadeAnimation = new FadeAnimator(view.getContext());

        paymentMethodSlideAnim = new SlideAnim(aspectRatioContainer);
        configureCardAspectRatio(ASPECT_RATIO_HIGH_RES);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        installmentsRecyclerView.setLayoutManager(linearLayoutManager);
        installmentsRecyclerView
            .addItemDecoration(new DividerItemDecoration(view.getContext(), linearLayoutManager.getOrientation()));

        paymentMethodPager.setPageMargin(
            ((int) (getResources().getDimensionPixelSize(R.dimen.px_m_margin) * PAGER_NEGATIVE_MARGIN_MULTIPLIER)));
        paymentMethodPager.setOffscreenPageLimit(2);

        pagerAndConfirmButtonContainer.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (pagerAndConfirmButtonContainer.getHeight() > 0) {
                        final ViewGroup.LayoutParams params = installmentsRecyclerView.getLayoutParams();
                        params.height = pagerAndConfirmButtonContainer.getHeight() - (int)
                            getContext().getResources().getDimension(R.dimen.px_badge_offset);
                        installmentsRecyclerView.setLayoutParams(params);
                        pagerAndConfirmButtonContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });

        paymentMethodHeaderView = view.findViewById(R.id.installments_header);
        paymentMethodHeaderView.setListener(new PaymentMethodHeaderView.Listener() {
            @Override
            public void onDescriptorViewClicked() {
                presenter.onInstallmentsRowPressed(paymentMethodPager.getCurrentItem());
            }

            @Override
            public void onInstallmentsSelectorCancelClicked() {
                presenter.onInstallmentSelectionCanceled(paymentMethodPager.getCurrentItem());
            }
        });

        configureToolbar(view);
    }

    private void configureCardAspectRatio(@NonNull final Pair<Integer, Integer> aspect) {
        aspectRatioContainer.setAspectRatio(aspect.first, aspect.second);
    }

    private ExpressPaymentPresenter createPresenter(@NonNull final Context context) {
        final Session session = Session.getInstance();
        return new ExpressPaymentPresenter(session.getPaymentRepository(),
            session.getConfigurationModule().getPaymentSettings(),
            session.getConfigurationModule().getDisabledPaymentMethodRepository(),
            session.getDiscountRepository(),
            session.getAmountRepository(),
            session.getGroupsRepository(),
            session.getAmountConfigurationRepository(),
            session.getConfigurationModule().getChargeSolver());
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
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
            presenter.updateElementPosition(paymentMethodPager.getCurrentItem());
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
        if (context instanceof CallBack) {
            callback = (CallBack) context;
        }
    }

    @Override
    public void onDetach() {
        callback = null;
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

        if (paymentMethodFragmentAdapter == null) {
            paymentMethodFragmentAdapter = new PaymentMethodFragmentAdapter(getChildFragmentManager(), items);
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
        paymentMethodSlideAnim.slideDown(0, pagerAndConfirmButtonContainer.getHeight());
        fadeAnimation.fadeOut(aspectRatioContainer);
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
        paymentMethodSlideAnim.slideUp(pagerAndConfirmButtonContainer.getHeight(), 0);
        fadeAnimation.fadeInFastest(aspectRatioContainer);
        fadeAnimation.fadeIn(confirmButton);
        fadeAnimation.fadeIn(indicator);
        expandAndCollapseAnimation.collapse();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQ_CODE_CARD_VAULT && resultCode == RESULT_OK) {
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    presenter.onTokenResolved(paymentMethodPager.getCurrentItem());
                }
            });
        } else if (requestCode == REQ_CODE_CARD_VAULT && resultCode == RESULT_CANCELED) {
            getActivity().getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    presenter.trackExpressView();
                }
            });
            super.onActivityResult(requestCode, resultCode, data);
        } else if (resultCode == Constants.RESULT_ACTION) {
            handleAction(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
            if (paymentResult instanceof BusinessPayment) {
                ((CheckoutActivity) getActivity()).presenter.onPaymentFinished((BusinessPayment) paymentResult);
            } else {
                ((CheckoutActivity) getActivity()).presenter.onPaymentFinished(paymentResult);
            }
        }
    }

    @Override
    public void startPayment() {
        presenter.confirmPayment(paymentMethodPager.getCurrentItem());
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
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    presenter.cancel();
                }
            });
        }
    }

    private void showConfirmButton() {
        confirmButton.clearAnimation();
        confirmButton.setVisibility(VISIBLE);
    }

    private void hideConfirmButton() {
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
    public void startLoadingButton(final int paymentTimeout) {

        hideConfirmButton();
        final int[] location = new int[2];
        confirmButton.getLocationOnScreen(location);

        // TODO refactor - unify review and confirm.
        final ExplodeParams explodeParams =
            new ExplodeParams(location[1] - confirmButton.getMeasuredHeight() / 2, confirmButton.getMeasuredHeight(),
                (int) getContext().getResources().getDimension(R.dimen.px_m_margin),
                getContext().getResources().getString(R.string.px_processing_payment_button),
                paymentTimeout);

        final FragmentManager childFragmentManager = getChildFragmentManager();
        final ExplodingFragment explodingFragment = ExplodingFragment.newInstance(explodeParams);
        childFragmentManager.beginTransaction()
            .replace(R.id.exploding_frame, explodingFragment, TAG_EXPLODING_FRAGMENT)
            .commitNowAllowingStateLoss();
        childFragmentManager.executePendingTransactions();
    }

    @Override
    public void showCardFlow(@NonNull final Card card) {
        CardVaultActivity.startActivity(this, REQ_CODE_CARD_VAULT);
    }

    @Override
    public void showCardFlow(@NonNull final PaymentRecovery paymentRecovery) {
        CardVaultActivity.startActivity(this, REQ_CODE_CARD_VAULT, paymentRecovery);
    }

    @Override
    public void onClick(final PayerCost payerCostSelected) {
        presenter.onPayerCostSelected(paymentMethodPager.getCurrentItem(), payerCostSelected);
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
    public void onSummaryMeasured(final boolean itemsClipped) {
        if (itemsClipped) {
            configureCardAspectRatio(ASPECT_RATIO_LOW_RES);
            paymentMethodFragmentAdapter.setLowResMode();
        }
        final Runnable runnable = () -> {
            paymentMethodPager.setAdapter(paymentMethodFragmentAdapter);
            indicator.attachToPager(paymentMethodPager);
        };
        //Workaround to weird bug in older versions with the view pager
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            runnable.run();
        } else {
            new Handler().post(runnable);
        }
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
}