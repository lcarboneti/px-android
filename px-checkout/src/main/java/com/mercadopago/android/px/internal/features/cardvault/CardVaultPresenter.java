package com.mercadopago.android.px.internal.features.cardvault;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.features.guessing_card.GuessingCardActivity;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.CardTokenRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.EscUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.EscFrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import java.util.List;

/* default */ class CardVaultPresenter extends BasePresenter<CardVault.View> implements CardVault.Actions {

    @NonNull private final ESCManagerBehaviour escManagerBehaviour;
    @NonNull private final AmountConfigurationRepository amountConfigurationRepository;
    @NonNull private final UserSelectionRepository userSelectionRepository;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final CardTokenRepository cardTokenRepository;

    private FailureRecovery failureRecovery;
    private String bin;

    //Activity parameters
    private PaymentRecovery paymentRecovery;

    private boolean issuersListShown;

    //Activity result
    protected PaymentMethod paymentMethod;

    //Card Info
    @Nullable /* default */ CardInfo cardInfo;
    /* default */ Token token;
    @Nullable /* default */ Card card;

    //Security Code
    @Nullable /* default */ String esc;

    public CardVaultPresenter(@NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final ESCManagerBehaviour escManagerBehaviour,
        @NonNull final AmountConfigurationRepository amountConfigurationRepository,
        @NonNull final CardTokenRepository cardTokenRepository) {
        this.userSelectionRepository = userSelectionRepository;
        this.paymentSettingRepository = paymentSettingRepository;
        this.escManagerBehaviour = escManagerBehaviour;
        this.amountConfigurationRepository = amountConfigurationRepository;
        this.cardTokenRepository = cardTokenRepository;
    }

    @Override
    public void initialize() {
        issuersListShown = false;

        if (tokenRecoveryAvailable()) {
            startTokenRecoveryFlow();
        } else if (getCard() == null) {
            startNewCardFlow();
        } else {
            startSavedCardFlow();
        }
    }

    @Override
    public void setPaymentRecovery(final PaymentRecovery paymentRecovery) {
        this.paymentRecovery = paymentRecovery;
    }

    @Override
    public void setCard(@Nullable final Card card) {
        this.card = card;
    }

    @Override
    public void setFailureRecovery(@NonNull final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    @Override
    @Nullable
    public Token getToken() {
        return token;
    }

    @Override
    public void setToken(final Token mToken) {
        token = mToken;
    }

    @Override
    @Nullable
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public void setPaymentMethod(final PaymentMethod mPaymentMethod) {
        paymentMethod = mPaymentMethod;
    }

    @Override
    @Nullable
    public PaymentRecovery getPaymentRecovery() {
        return paymentRecovery;
    }

    // TODO: can we kill this and use the selected card on user selection repository?
    @Override
    @Nullable
    public Card getCard() {
        return card;
    }

    private void setESC(@NonNull final String esc) {
        this.esc = esc;
    }

    @Override
    public void setCardInfo(final CardInfo cardInfo) {
        this.cardInfo = cardInfo;
        bin = cardInfo == null ? TextUtil.EMPTY : cardInfo.getFirstSixDigits();
    }

    @Override
    @Nullable
    public CardInfo getCardInfo() {
        return cardInfo;
    }

    @Override
    public int getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(paymentMethod, bin);
    }

    public boolean isIssuersListShown() {
        return issuersListShown;
    }

    public void setIssuersListShown(final boolean issuersListShown) {
        this.issuersListShown = issuersListShown;
    }

    private void checkStartInstallmentsActivity() {
        if (userSelectionRepository.getPayerCost() == null) {
            getView().askForInstallments(getCardInfo());
        } else {
            getView().finishWithResult();
        }
    }

    private void checkStartIssuersActivity(@NonNull final Intent data) {
        if (userSelectionRepository.getIssuer() == null) {
            issuersListShown = true;
            getView().startIssuersActivity(GuessingCardActivity.extractIssuersFromIntent(data));
        } else {
            checkStartInstallmentsActivity();
        }
    }

    @Override
    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    /* default */ void resolveIssuersRequest() {
        issuersListShown = true;
        checkStartInstallmentsActivity();
    }

    @Override
    public void resolveInstallmentsRequest() {
        if (getCard() == null) {
            getView().finishWithResult();
        } else {
            startSecurityCodeFlowIfNeeded();
        }
    }

    @Override
    public void resolveSecurityCodeRequest() {
        setToken(paymentSettingRepository.getToken());
        getView().finishWithResult();
    }

    @Override
    public void resolveNewCardRequest(@NonNull final Intent data) {
        setCardInfo(new CardInfo(paymentSettingRepository.getToken()));
        checkStartIssuersActivity(data);
    }

    @Override
    public void onResultCancel() {
        getView().cancelCardVault();
    }

    @Override
    public void onResultFinishOnError() {
        getView().finishOnErrorResult();
    }

    private void startTokenRecoveryFlow() {
        setCardInfo(new CardInfo(paymentSettingRepository.getToken()));
        setPaymentMethod(userSelectionRepository.getPaymentMethod());
        setToken(paymentSettingRepository.getToken());
        getView().askForSecurityCodeFromTokenRecovery(Reason.from(paymentRecovery));
    }

    private void startSavedCardFlow() {
        // here is a saved card selected
        final Card card = getCard();
        setCardInfo(new CardInfo(card));
        setPaymentMethod(card.getPaymentMethod());

        if (userSelectionRepository.getPayerCost() == null) {
            onPayerCosts(amountConfigurationRepository.getCurrentConfiguration().getPayerCosts());
        } else {
            // This could happen on one tap flows
            startSecurityCodeFlowIfNeeded();
        }
    }

    private void startNewCardFlow() {
        getView().askForCardInformation();
    }

    private boolean tokenRecoveryAvailable() {
        return getPaymentRecovery() != null && getPaymentRecovery().isTokenRecoverable();
    }

    private void startSecurityCodeFlowIfNeeded() {
        if (isESCSaved()) {
            createESCToken();
        } else {
            getView().startSecurityCodeActivity(Reason.SAVED_CARD);
        }
    }

    private boolean isESCSaved() {
        if (!TextUtil.isEmpty(esc)) {
            return true;
        } else {
            setESC(escManagerBehaviour.getESC(card.getId(), card.getFirstSixDigits(), card.getLastFourDigits()));
            return !TextUtil.isEmpty(esc);
        }
    }

    private void createESCToken() {
        getView().showProgressLayout();

        final SavedESCCardToken escCardToken = SavedESCCardToken.createWithEsc(card.getId(), esc);
        cardTokenRepository
            .createToken(escCardToken).enqueue(new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
            @Override
            public void onSuccess(final Token token) {
                CardVaultPresenter.this.token = token;
                CardVaultPresenter.this.token.setLastFourDigits(card.getLastFourDigits());
                paymentSettingRepository.configure(CardVaultPresenter.this.token);
                escManagerBehaviour.saveESCWith(token.getCardId(), token.getEsc());
                if (isViewAttached()) {
                    getView().finishWithResult();
                }
            }

            @Override
            public void onFailure(final MercadoPagoError error) {
                if (error.isApiException() && EscUtil.isInvalidEscForApiException(error.getApiException())) {
                    EscFrictionEventTracker.create(escCardToken.getCardId(), esc, error.getApiException()).track();
                    escManagerBehaviour.deleteESCWith(escCardToken.getCardId());
                    esc = null;
                    //Start CVV screen if fail
                    if (isViewAttached()) {
                        getView().startSecurityCodeActivity(Reason.SAVED_CARD);
                    }
                } else {
                    //Retry with error screen
                    recoverCreateESCToken(error);
                }
            }
        });
    }

    private void recoverCreateESCToken(final MercadoPagoError error) {
        if (isViewAttached()) {
            getView().showError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
            setFailureRecovery(this::createESCToken);
        }
    }

    private void onPayerCosts(@NonNull final List<PayerCost> payerCosts) {
        if (payerCosts.isEmpty()) {
            getView().showEmptyPayerCostScreen();
        } else if (payerCosts.size() == 1) {
            userSelectionRepository.select(payerCosts.get(0));
            startSecurityCodeFlowIfNeeded();
        } else {
            getView().askForInstallments(getCardInfo());
        }
    }
}