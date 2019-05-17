package com.mercadopago.android.px.internal.features.cardvault;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.IssuersActivity;
import com.mercadopago.android.px.internal.features.SecurityCodeActivity;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;
import com.mercadopago.android.px.internal.features.providers.CardVaultProviderImpl;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import java.util.List;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_SILENT_ERROR;

public class CardVaultActivity extends AppCompatActivity implements CardVaultView {

    private static final String EXTRA_CARD = "card";
    private static final String EXTRA_PAYMENT_RECOVERY = "paymentRecovery";
    private static final String EXTRA_PAYMENT_METHOD = "paymentMethod";
    private static final String EXTRA_TOKEN = "token";
    private static final String EXTRA_CARD_INFO = "cardInfo";
    private static final String EXTRA_INSTALLMENTS_LIST_SHOWN = "installmentsListShown";
    private static final String EXTRA_ISSUERS_LIST_SHOWN = "issuersListShown";

    private CardVaultPresenter presenter;

    @SuppressWarnings("TypeMayBeWeakened")
    public static void startActivity(@NonNull final Activity context, final int reqCode,
        @NonNull final PaymentRecovery paymentRecovery) {
        final Intent intent = new Intent(context, CardVaultActivity.class);
        intent.putExtra(EXTRA_PAYMENT_RECOVERY, paymentRecovery);
        context.startActivityForResult(intent, reqCode);
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public static void startActivity(final Fragment oneTapFragment, final int reqCode,
        @NonNull final PaymentRecovery paymentRecovery) {
        final Intent intent = new Intent(oneTapFragment.getActivity(), CardVaultActivity.class);
        intent.putExtra(EXTRA_PAYMENT_RECOVERY, paymentRecovery);
        oneTapFragment.startActivityForResult(intent, reqCode);
    }

    public static void startActivity(@NonNull final Activity context, final int reqCode) {
        final Intent intent = new Intent(context, CardVaultActivity.class);
        context.startActivityForResult(intent, reqCode);
    }

    public static void startActivity(final Fragment oneTapFragment, final int reqCode) {
        final Intent intent = new Intent(oneTapFragment.getActivity(), CardVaultActivity.class);
        oneTapFragment.startActivityForResult(intent, reqCode);
    }

    private void configure() {
        final Session session = Session.getSession(this);
        final PaymentSettingRepository paymentSettingRepository = session.getConfigurationModule().getPaymentSettings();
        presenter = new CardVaultPresenter(session.getConfigurationModule().getUserSelectionRepository(),
            paymentSettingRepository,
            session.getMercadoPagoESC(), session.getAmountConfigurationRepository(), session.providePayerCostSolver());
        presenter.attachResourcesProvider(new CardVaultProviderImpl(getApplicationContext()));
        presenter.attachView(this);
        final Card card = session.getConfigurationModule().getUserSelectionRepository().getCard();
        presenter.setCard(card);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.px_activity_card_vault);
        configure();

        if (savedInstanceState == null) {
            getActivityParameters();
            try {
                presenter.initialize();
            } catch (final Exception e) {
                FrictionEventTracker.with("/px_checkout/card_vault",
                    FrictionEventTracker.Id.SILENT, FrictionEventTracker.Style.SCREEN,
                    ErrorUtil.getStacktraceMessage(e))
                    .track();
                cancelCardVault();
            }
        } else {
            restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        presenter.detachResourceProvider();
        super.onDestroy();
    }

    public void restoreInstanceState(final Bundle savedInstanceState) {
        presenter.setPaymentRecovery((PaymentRecovery) savedInstanceState.getSerializable(EXTRA_PAYMENT_RECOVERY));
        presenter.setCard((Card) savedInstanceState.getSerializable(EXTRA_CARD));
        presenter.setPaymentMethod((PaymentMethod) savedInstanceState.getSerializable(EXTRA_PAYMENT_METHOD));
        presenter.setToken((Token) savedInstanceState.getSerializable(EXTRA_TOKEN));
        presenter.setCardInfo((CardInfo) savedInstanceState.getSerializable(EXTRA_CARD_INFO));
        presenter.setInstallmentsListShown(savedInstanceState.getBoolean(EXTRA_INSTALLMENTS_LIST_SHOWN, false));
        presenter.setIssuersListShown(savedInstanceState.getBoolean(EXTRA_ISSUERS_LIST_SHOWN, false));
    }

    private void getActivityParameters() {
        final Intent intent = getIntent();
        final PaymentRecovery paymentRecovery = (PaymentRecovery) intent.getSerializableExtra(EXTRA_PAYMENT_RECOVERY);
        presenter.setPaymentRecovery(paymentRecovery);
    }

    @Override
    public void showProgressLayout() {
        ViewUtils.showProgressLayout(this);
    }

    @Override
    public void askForSecurityCodeFromTokenRecovery(final Reason recoveryReason) {
        startSecurityCodeActivity(recoveryReason);
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void startSecurityCodeActivity(final Reason reason) {
        new SecurityCodeActivity.Builder()
            .setPaymentMethod(presenter.getPaymentMethod())
            .setCardInfo(presenter.getCardInfo())
            .setToken(presenter.getToken())
            .setCard(presenter.getCard())
            .setPaymentRecovery(presenter.getPaymentRecovery())
            .setReason(reason)
            .startActivity(this, Constants.Activities.SECURITY_CODE_REQUEST_CODE);
    }

    @Override
    public void askForCardInformation() {
        GuessingCardActivity.startGuessingCardActivityForPayment(this,
            presenter.getPaymentRecovery());
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Constants.Activities.GUESSING_CARD_FOR_PAYMENT_REQUEST_CODE) {
            resolveGuessingCardRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.ISSUERS_REQUEST_CODE) {
            resolveIssuersRequest(resultCode);
        } else if (requestCode == Constants.Activities.INSTALLMENTS_REQUEST_CODE) {
            resolveInstallmentsRequest(resultCode);
        } else if (requestCode == Constants.Activities.SECURITY_CODE_REQUEST_CODE) {
            resolveSecurityCodeRequest(resultCode);
        } else if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            resolveErrorRequest(resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (presenter != null) {
            outState.putSerializable(EXTRA_CARD, presenter.getCard());
            outState.putSerializable(EXTRA_PAYMENT_RECOVERY, presenter.getPaymentRecovery());
            outState.putBoolean(EXTRA_INSTALLMENTS_LIST_SHOWN, presenter.isInstallmentsListShown());
            outState.putBoolean(EXTRA_ISSUERS_LIST_SHOWN, presenter.isIssuersListShown());

            if (presenter.getPaymentMethod() != null) {
                outState.putSerializable(EXTRA_PAYMENT_METHOD, presenter.getPaymentMethod());
            }

            if (presenter.getToken() != null) {
                outState.putSerializable(EXTRA_TOKEN, presenter.getToken());
            }

            if (presenter.getCardInfo() != null) {
                outState.putSerializable(EXTRA_CARD_INFO, presenter.getCardInfo());
            }
        }
    }

    private void resolveErrorRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.recoverFromFailure();
        } else {
            setResult(resultCode, data);
            finish();
        }
    }

    protected void resolveIssuersRequest(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.resolveIssuersRequest();
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        }
    }

    protected void resolveInstallmentsRequest(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.resolveInstallmentsRequest();
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        }
    }

    protected void resolveGuessingCardRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            presenter.resolveNewCardRequest(data);
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        } else if (resultCode == RESULT_SILENT_ERROR) {
            presenter.onResultFinishOnError();
        }
    }

    protected void resolveSecurityCodeRequest(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.resolveSecurityCodeRequest();
        } else if (resultCode == RESULT_CANCELED) {
            presenter.onResultCancel();
        }
    }

    @Override
    public void cancelCardVault() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void finishOnErrorResult() {
        setResult(RESULT_SILENT_ERROR);
        finish();
    }

    @Override
    public void startIssuersActivity(@NonNull final List<Issuer> issuers) {
        IssuersActivity.start(this, Constants.Activities.ISSUERS_REQUEST_CODE, issuers, presenter.getCardInfo());
    }

    @Override
    public void askForInstallments() {
        startInstallmentsActivity();
        animateTransitionSlideInSlideOut();
    }

    private void startInstallmentsActivity() {
        new Constants.Activities.InstallmentsActivityBuilder()
            .setActivity(this)
            .setCardInfo(presenter.getCardInfo())
            .startActivity();
    }

    @Override
    public void animateTransitionSlideInSlideOut() {
        overridePendingTransition(R.anim.px_slide_right_to_left_in, R.anim.px_slide_right_to_left_out);
    }

    @Override
    public void finishWithResult() {
        overridePendingTransition(R.anim.px_slide_left_to_right_in, R.anim.px_slide_left_to_right_out);
        final Intent returnIntent = new Intent();
        // TODO: can we kill this and use user selection repository?
        returnIntent.putExtra(EXTRA_TOKEN, JsonUtil.getInstance().toJson(presenter.getToken()));
        returnIntent.putExtra(EXTRA_CARD, JsonUtil.getInstance().toJson(presenter.getCard()));
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void showApiExceptionError(final ApiException exception, final String requestOrigin) {
        ErrorUtil.showApiExceptionError(this, exception, requestOrigin);
    }

    @Override
    public void showError(final MercadoPagoError error, final String requestOrigin) {
        if (error != null && error.isApiException()) {
            showApiExceptionError(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    public void showEmptyPayerCostScreen() {
        showError(new MercadoPagoError(getString(R.string.px_error_message_missing_payer_cost), false), TextUtil.EMPTY);
    }
}
