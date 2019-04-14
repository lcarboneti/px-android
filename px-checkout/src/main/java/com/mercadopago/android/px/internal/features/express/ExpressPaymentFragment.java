package com.mercadopago.android.px.internal.features.express;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.CheckoutActivity;
import com.mercadopago.android.px.internal.features.Constants;
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
import com.mercadopago.android.px.internal.util.ScaleUtil;
import com.mercadopago.android.px.internal.util.VibrationUtils;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.FixedAspectRatioFrameLayout;
import com.mercadopago.android.px.internal.view.LabeledSwitch;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.view.ScrollingPagerIndicator;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.TitlePager;
import com.mercadopago.android.px.internal.viewmodel.StatusBarDecorator;
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

    private final HubAdapter hubAdapter = new HubAdapter();

    public static Fragment getInstance() {
        return new ExpressPaymentFragment();
    }

    @Override
    public void onSplitChanged(final boolean isChecked) {
        final int currentItem = paymentMethodPager.getCurrentItem();
        presenter.onSplitChanged(isChecked, currentItem);
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
            //Nothing to do here
        }

        // Order is important - On click and events should be wired AFTER view is attached.
        summaryView.setOnFitListener(this);
        toolbarAppearAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.px_toolbar_appear);
        toolbarDisappearAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.px_toolbar_disappear);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (ApiUtil.checkConnection(getContext())) {
                    presenter.confirmPayment(paymentMethodPager.getCurrentItem());
                } else {
                    presenter.manageNoConnection();
                }
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
        configureCardAspectRatio(aspectRatioContainer);

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

    private void configureCardAspectRatio(final FixedAspectRatioFrameLayout aspectRatioContainer) {
        if (ScaleUtil.isLowRes(aspectRatioContainer.getContext())) {
            aspectRatioContainer.setAspectRatio(ASPECT_RATIO_LOW_RES.first, ASPECT_RATIO_LOW_RES.second);
        } else {
            aspectRatioContainer.setAspectRatio(ASPECT_RATIO_HIGH_RES.first, ASPECT_RATIO_HIGH_RES.second);
        }
    }

    private ExpressPaymentPresenter createPresenter(@NonNull final Context context) {
        final Session session = Session.getSession(context);
        return new ExpressPaymentPresenter(session.getPaymentRepository(),
            session.getConfigurationModule().getPaymentSettings(),
            session.getConfigurationModule().getDisabledPaymentMethodRepository(),
            session.getDiscountRepository(),
            session.getAmountRepository(),
            session.getInitRepository(),
            session.getAmountConfigurationRepository());
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
        presenter.onViewPaused();
        super.onPause();
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof CallBack) {
            callback = (CallBack) context;
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onDetach() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
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

        if (paymentMethodPager.getAdapter() == null) {
            paymentMethodPager
                .setAdapter(PaymentMethodFragmentAdapter.with(getContext(), getChildFragmentManager(), items));
        }

        installmentsAdapter = new InstallmentsAdapter(site, new ArrayList<PayerCost>(), PayerCost.NO_SELECTED, this);
        installmentsRecyclerView.setAdapter(installmentsAdapter);
        installmentsRecyclerView.setVisibility(View.GONE);

        indicator.attachToPager(paymentMethodPager);

        // indicator must be after paymentMethodPager adapter is set.

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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
        final boolean isSplitUserPreference) {
        hubAdapter.updateData(paymentMethodIndex, payerCostSelected, isSplitUserPreference);
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

    //TODO refactor
    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        if (getActivity() != null) {
            ((CheckoutActivity) getActivity()).presenter.onRecoverPaymentEscInvalid(recovery);
        }
    }

    @Override
    public void startPayment() {
        presenter.confirmPayment(paymentMethodPager.getCurrentItem());
    }

    @Override
    public void finishLoading(@NonNull final ExplodeDecorator params) {
        final Fragment fragment = FragmentUtil.getFragmentByTag(getChildFragmentManager(), TAG_EXPLODING_FRAGMENT);
        if (fragment != null) {
            ((ExplodingFragment) fragment).finishLoading(params);
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
        new Constants.Activities.CardVaultActivityBuilder()
            .setCard(card)
            .startActivity(this, REQ_CODE_CARD_VAULT);
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
}
