package com.mercadopago.android.px.internal.features.cardvault;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;

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


    private void configure() {
        final Session session = Session.getSession(this);
        presenter = new CardVaultPresenter(session.getConfigurationModule().getUserSelectionRepository(),
            session.getConfigurationModule().getPaymentSettings(),
            session.getMercadoPagoESC(),
            session.getAmountConfigurationRepository(),
            session.providePayerCostSolver(),
            session.getTokenRepository());
        presenter.attachView(this);
        final Card card = session.getConfigurationModule().getUserSelectionRepository().getCard();
        presenter.setCard(card);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreenOrientation();
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
        super.onDestroy();
    }

    private void setScreenOrientation() {
        final int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public void restoreInstanceState(final Bundle savedInstanceState) {
        presenter.setPaymentRecovery(
            JsonUtil.getInstance()
                .fromJson(savedInstanceState.getString(EXTRA_PAYMENT_RECOVERY), PaymentRecovery.class));
        presenter.setCard(JsonUtil.getInstance().fromJson(savedInstanceState.getString(EXTRA_CARD), Card.class));

        presenter.setPaymentMethod(
            JsonUtil.getInstance().fromJson(savedInstanceState.getString(EXTRA_PAYMENT_METHOD), PaymentMethod.class));
        presenter
            .setToken(JsonUtil.getInstance().fromJson(savedInstanceState.getString(EXTRA_TOKEN), Token.class));
        presenter
            .setCardInfo(
                JsonUtil.getInstance().fromJson(savedInstanceState.getString(EXTRA_CARD_INFO), CardInfo.class));
        presenter.setInstallmentsListShown(savedInstanceState.getBoolean(EXTRA_INSTALLMENTS_LIST_SHOWN, false));
        presenter.setIssuersListShown(savedInstanceState.getBoolean(EXTRA_ISSUERS_LIST_SHOWN, false));
    }

    private void getActivityParameters() {
        final Intent intent = getIntent();
        final PaymentRecovery paymentRecovery =
            JsonUtil.getInstance().fromJson(intent.getStringExtra(EXTRA_PAYMENT_RECOVERY), PaymentRecovery.class);
        presenter.setPaymentRecovery(paymentRecovery);
    }

    @Override
    public void showProgressLayout() {
        ViewUtils.showProgressLayout(this);
    }

    @Override
    public void askForSecurityCodeFromTokenRecovery() {
        startSecurityCodeActivity();
        animateTransitionSlideInSlideOut();
    }

    @Override
    public void startSecurityCodeActivity() {
        new Constants.Activities.SecurityCodeActivityBuilder()
            .setActivity(this)
            .setPaymentMethod(presenter.getPaymentMethod())
            .setCardInfo(presenter.getCardInfo())
            .setToken(presenter.getToken())
            .setCard(presenter.getCard())
            .setPaymentRecovery(presenter.getPaymentRecovery())
            .startActivity();
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
            resolveGuessingCardRequest(resultCode);
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
            outState.putString(EXTRA_CARD, JsonUtil.getInstance().toJson(presenter.getCard()));
            outState
                .putString(EXTRA_PAYMENT_RECOVERY, JsonUtil.getInstance().toJson(presenter.getPaymentRecovery()));
            outState.putBoolean(EXTRA_INSTALLMENTS_LIST_SHOWN, presenter.isInstallmentsListShown());
            outState.putBoolean(EXTRA_ISSUERS_LIST_SHOWN, presenter.isIssuersListShown());

            if (presenter.getPaymentMethod() != null) {
                outState
                    .putString(EXTRA_PAYMENT_METHOD, JsonUtil.getInstance().toJson(presenter.getPaymentMethod()));
            }

            if (presenter.getToken() != null) {
                outState.putString(EXTRA_TOKEN, JsonUtil.getInstance().toJson(presenter.getToken()));
            }

            if (presenter.getCardInfo() != null) {
                outState.putString(EXTRA_CARD_INFO, JsonUtil.getInstance().toJson(presenter.getCardInfo()));
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

    protected void resolveGuessingCardRequest(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.resolveNewCardRequest();
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
    public void startIssuersActivity() {
        new Constants.Activities.IssuersActivityBuilder()
            .setActivity(this)
            .setCardInfo(presenter.getCardInfo())
            .startActivity();
        animateTransitionSlideInSlideOut();
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
