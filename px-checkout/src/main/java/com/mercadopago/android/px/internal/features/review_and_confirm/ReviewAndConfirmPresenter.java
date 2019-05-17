package com.mercadopago.android.px.internal.features.review_and_confirm;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.datasource.IESCManager;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecoratorMapper;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.mappers.BusinessModelMapper;
import com.mercadopago.android.px.model.BusinessPayment;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.internal.events.ChangePaymentMethodEvent;
import com.mercadopago.android.px.tracking.internal.events.ConfirmEvent;
import com.mercadopago.android.px.tracking.internal.views.ReviewAndConfirmViewTracker;
import java.util.Set;

/* default */ final class ReviewAndConfirmPresenter extends BasePresenter<ReviewAndConfirm.View>
    implements ReviewAndConfirm.Action {

    @NonNull /* default */ final PaymentRepository paymentRepository;
    @NonNull private final BusinessModelMapper businessModelMapper;
    @NonNull private final PaymentSettingRepository paymentSettings;
    private final ExplodeDecoratorMapper explodeDecoratorMapper;
    private final ReviewAndConfirmViewTracker reviewAndConfirmViewTracker;
    private final ConfirmEvent confirmEvent;
    private final UserSelectionRepository userSelectionRepository;
    private FailureRecovery recovery;

    /* default */ ReviewAndConfirmPresenter(@NonNull final PaymentRepository paymentRepository,
        @NonNull final BusinessModelMapper businessModelMapper,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final PaymentSettingRepository paymentSettings,
        @NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final IESCManager IESCManager) {
        final Set<String> escCardIds = IESCManager.getESCCardIds();
        this.paymentRepository = paymentRepository;
        this.businessModelMapper = businessModelMapper;
        this.paymentSettings = paymentSettings;
        this.userSelectionRepository = userSelectionRepository;

        explodeDecoratorMapper = new ExplodeDecoratorMapper();
        reviewAndConfirmViewTracker =
            new ReviewAndConfirmViewTracker(escCardIds, userSelectionRepository, paymentSettings,
                discountRepository.getCurrentConfiguration());
        confirmEvent = ConfirmEvent.from(escCardIds, userSelectionRepository);
    }

    @Override
    public void attachView(final ReviewAndConfirm.View view) {
        super.attachView(view);
        paymentRepository.attach(this);
    }

    @Override
    public void onViewResumed(final ReviewAndConfirm.View view) {
        attachView(view);
        setCurrentViewTracker(reviewAndConfirmViewTracker);
        resolveDynamicDialog(DynamicDialogConfiguration.DialogLocation.ENTER_REVIEW_AND_CONFIRM);
    }

    @Override
    public void hasFinishPaymentAnimation() {
        final IPaymentDescriptor payment = paymentRepository.getPayment();
        if (payment instanceof BusinessPayment) {
            getView().showResult(businessModelMapper.map((BusinessPayment) payment));
        } else {
            getView().showResult(paymentRepository.createPaymentResult(payment));
        }
    }

    @Override
    public void changePaymentMethod() {
        ChangePaymentMethodEvent.create().track();
        removeUserSelection();
        getView().finishAndChangePaymentMethod();
    }

    private void resolveDynamicDialog(@NonNull final DynamicDialogConfiguration.DialogLocation location) {
        final CheckoutPreference checkoutPreference = paymentSettings.getCheckoutPreference();
        final DynamicDialogConfiguration dynamicDialogConfiguration =
            paymentSettings.getAdvancedConfiguration().getDynamicDialogConfiguration();
        final DynamicDialogCreator.CheckoutData checkoutData =
            new DynamicDialogCreator.CheckoutData(checkoutPreference, paymentRepository.getPaymentDataList());
        if (dynamicDialogConfiguration.hasCreatorFor(location)) {
            getView().showDynamicDialog(dynamicDialogConfiguration.getCreatorFor(location), checkoutData);
        }
    }

    @Override
    public void detachView() {
        paymentRepository.detach(this);
        super.detachView();
    }

    @Override
    public void onPaymentConfirm() {
        confirmEvent.track();
        pay();
    }

    /* default */ void pay() {
        if (paymentRepository.isExplodingAnimationCompatible()) {
            getView().startLoadingButton(paymentRepository.getPaymentTimeout());
            getView().hideConfirmButton();
        }
        // TODO improve: This was added because onetap can detach this listener on its OnDestroy
        paymentRepository.attach(this);
        paymentRepository.startPayment();
    }

    @Override
    public void onCardFlowResponse() {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        pay();
    }

    @Override
    public void onCardFlowCancel() {
        // TODO do nothing
        // TODO check if it's needed
        // TODO Reset UI - Exploading button
    }

    @Override
    public void onError(@NonNull final MercadoPagoError mercadoPagoError) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().cancelCheckoutAndInformError(mercadoPagoError);
    }

    @Override
    public void onCvvRequired(@NonNull final Card card) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().showCardCVVRequired(card);
    }

    @Override
    public void onVisualPayment() {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().showPaymentProcessor();
    }

    @Override
    public void onRecoverPaymentEscInvalid(final PaymentRecovery recovery) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();
        getView().startPaymentRecoveryFlow(recovery);
    }

    @Override
    public void onPaymentFinished(@NonNull final IPaymentDescriptor payment) {
        getView().hideConfirmButton();
        getView().finishLoading(explodeDecoratorMapper.map(payment));
    }

    @Override
    public void onPaymentError(@NonNull final MercadoPagoError error) {
        getView().cancelLoadingButton();
        getView().showConfirmButton();

        recovery = new FailureRecovery() {
            @Override
            public void recover() {
                pay();
            }
        };

        if (error.isPaymentProcessing()) {
            final PaymentResult paymentResult =
                new PaymentResult.Builder()
                    .setPaymentData(paymentRepository.getPaymentDataList())
                    .setPaymentStatus(Payment.StatusCodes.STATUS_IN_PROCESS)
                    .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY)
                    .build();
            getView().showResult(paymentResult);
        } else if (error.isInternalServerError() || error.isNoConnectivityError()) {
            getView().showErrorSnackBar(error);
        } else {
            getView().showErrorScreen(error);
        }
    }

    @Override
    public void recoverFromFailure() {
        if (recovery != null) {
            recovery.recover();
        }
    }

    @Override
    public void executePostPaymentAction(@NonNull final PostPaymentAction postPaymentAction) {
        postPaymentAction.execute(new PostPaymentAction.ActionController() {
            @Override
            public void recoverPayment(@NonNull final PostPaymentAction postPaymentAction) {
                getView().startPaymentRecoveryFlow(paymentRepository.createPaymentRecovery());
            }

            @Override
            public void onChangePaymentMethod() {
                // Do nothing, It is handled before.
            }
        });
    }

    public void onPayerInformationResponse() {
        getView().reloadBody();
    }

    public void removeUserSelection() {
        userSelectionRepository.reset();
    }
}