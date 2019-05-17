package com.mercadopago.android.px.internal.features;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.callbacks.card.CardSecurityCodeEditTextCallback;
import com.mercadopago.android.px.internal.controllers.CheckoutTimer;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.card.CardSecurityCodeTextWatcher;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardRepresentationModes;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.ResourceUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.MPEditText;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CardTokenException;
import com.mercadopago.android.px.model.exceptions.ExceptionHandler;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.model.Reason;

public class SecurityCodeActivity extends PXActivity<SecurityCodePresenter> implements SecurityCodeActivityView {

    private static final String EXTRA_PAYMENT_METHOD = "PAYMENT_METHOD";
    private static final String EXTRA_TOKEN = "TOKEN";
    private static final String EXTRA_CARD = "CARD";
    private static final String EXTRA_CARD_INFO = "CARD_INFO";
    private static final String EXTRA_PAYMENT_RECOVERY = "PAYMENT_RECOVERY";
    private static final String EXTRA_REASON = "REASON";

    private static final String CARD_INFO_BUNDLE = "cardInfoBundle";
    private static final String PAYMENT_RECOVERY_BUNDLE = "paymentRecoveryBundle";

    public static final String ERROR_STATE = "textview_error";
    public static final String NORMAL_STATE = "textview_normal";

    //View controls
    protected ViewGroup mProgressLayout;
    protected MPEditText mSecurityCodeEditText;
    protected FrameLayout mNextButton;
    protected FrameLayout mBackButton;
    protected MPTextView mNextButtonText;
    protected MPTextView mBackButtonText;
    protected LinearLayout mButtonContainer;
    protected FrameLayout mErrorContainer;
    protected MPTextView mErrorTextView;
    protected String mErrorState;
    protected FrameLayout mBackground;
    protected ImageView mSecurityCodeCardIcon;
    protected Toolbar mToolbar;

    //Normal View
    protected FrameLayout mCardContainer;
    protected CardView mCardView;
    protected MPTextView mTimerTextView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            createPresenter();
            getActivityParameters();
            configurePresenter();
            setContentView();
            presenter.initialize();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable(CARD_INFO_BUNDLE, presenter.getCardInfo());
        outState.putSerializable(PAYMENT_RECOVERY_BUNDLE, presenter.getPaymentRecovery());
        presenter.storeInBundle(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            final Session session = Session.getSession(this);
            final PaymentSettingRepository paymentSettings = session.getConfigurationModule().getPaymentSettings();
            presenter = new SecurityCodePresenter(paymentSettings, session.getCardTokenRepository(),
                session.getMercadoPagoESC());

            presenter.setToken(paymentSettings.getToken());
            presenter.setCard(session.getConfigurationModule().getUserSelectionRepository().getCard());
            presenter
                .setPaymentMethod(session.getConfigurationModule().getUserSelectionRepository().getPaymentMethod());
            presenter.setCardInfo((CardInfo) savedInstanceState.getSerializable(CARD_INFO_BUNDLE));
            presenter
                .setPaymentRecovery((PaymentRecovery) savedInstanceState.getSerializable(PAYMENT_RECOVERY_BUNDLE));
            presenter.recoverFromBundle(savedInstanceState);

            configurePresenter();
            setContentView();
            presenter.initialize();
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void showApiExceptionError(final ApiException exception, final String requestOrigin) {
        ErrorUtil.showApiExceptionError(this, exception, requestOrigin);
    }

    private void createPresenter() {
        if (presenter == null) {
            final Session session = Session.getSession(this);
            presenter = new SecurityCodePresenter(session.getConfigurationModule().getPaymentSettings(),
                session.getCardTokenRepository(), session.getMercadoPagoESC());
        }
    }

    private void configurePresenter() {
        if (presenter != null) {
            presenter.attachView(this);
        }
    }

    private void getActivityParameters() {

        final CardInfo cardInfo =
            JsonUtil.getInstance().fromJson(getIntent().getStringExtra(EXTRA_CARD_INFO), CardInfo.class);
        final Card card = JsonUtil.getInstance().fromJson(getIntent().getStringExtra(EXTRA_CARD), Card.class);
        final Token token = JsonUtil.getInstance().fromJson(getIntent().getStringExtra(EXTRA_TOKEN), Token.class);
        final PaymentMethod paymentMethod =
            JsonUtil.getInstance().fromJson(getIntent().getStringExtra(EXTRA_PAYMENT_METHOD), PaymentMethod.class);
        final PaymentRecovery paymentRecovery =
            JsonUtil.getInstance().fromJson(getIntent().getStringExtra(EXTRA_PAYMENT_RECOVERY), PaymentRecovery.class);
        final Reason reason =
            Reason.valueOf(getIntent().getStringExtra(EXTRA_REASON));

        presenter.setToken(token);
        presenter.setCard(card);
        presenter.setPaymentMethod(paymentMethod);
        presenter.setCardInfo(cardInfo);
        presenter.setPaymentRecovery(paymentRecovery);
        presenter.setReason(reason);
    }

    public void setContentView() {
        setContentView(R.layout.px_activity_security_code);
    }

    private void initializeControls() {
        initializeToolbar();

        mProgressLayout = findViewById(R.id.mpsdkProgressLayout);
        mSecurityCodeEditText = findViewById(R.id.mpsdkCardSecurityCode);
        mNextButton = findViewById(R.id.mpsdkNextButton);
        mBackButton = findViewById(R.id.mpsdkBackButton);
        mNextButtonText = findViewById(R.id.mpsdkNextButtonText);
        mBackButtonText = findViewById(R.id.mpsdkBackButtonText);
        mButtonContainer = findViewById(R.id.mpsdkButtonContainer);
        mErrorContainer = findViewById(R.id.mpsdkErrorContainer);
        mErrorTextView = findViewById(R.id.mpsdkErrorTextView);
        mBackground = findViewById(R.id.mpsdkSecurityCodeActivityBackground);
        mCardContainer = findViewById(R.id.mpsdkCardViewContainer);
        mTimerTextView = findViewById(R.id.mpsdkTimerTextView);
        mProgressLayout.setVisibility(View.GONE);
        mSecurityCodeCardIcon = findViewById(R.id.mpsdkSecurityCodeCardIcon);

        setListeners();
    }

    private void initializeToolbar() {
        mToolbar = findViewById(R.id.mpsdkToolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        presenter.trackAbort();
        configureFinish();
        finish();
    }

    @Override
    public void onBackButtonPressed() {
        configureFinish();
        super.onBackPressed();
    }

    private void configureFinish() {
        final Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        overrideTransitionOut();
    }

    private void setListeners() {
        setSecurityCodeListeners();
        setButtonsListeners();
    }

    @Override
    public void showLoadingView() {
        hideKeyboard();
        mProgressLayout.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
        mBackButton.setVisibility(View.INVISIBLE);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void stopLoadingView() {
        mProgressLayout.setVisibility(View.GONE);
    }

    private void loadViews() {
        loadNormalViews();
    }

    private void loadNormalViews() {
        mCardView = new CardView(this);
        mCardView.setSize(CardRepresentationModes.BIG_SIZE);
        mCardView.inflateInParent(mCardContainer, true);
        mCardView.initializeControls();
        mCardView.setPaymentMethod(presenter.getPaymentMethod());
        mCardView.setSecurityCodeLength(presenter.getSecurityCodeLength());
        mCardView.setSecurityCodeLocation(presenter.getSecurityCodeLocation());
        mCardView.setCardNumberLength(presenter.getCardNumberLength());
        mCardView.setLastFourDigits(presenter.getCardInfo().getLastFourDigits());
        mCardView.draw(CardView.CARD_SIDE_FRONT);
        mCardView.drawFullCard();
        mCardView.drawEditingSecurityCode("");
        presenter.setSecurityCodeCardType();
    }

    private void setSecurityCodeCardColorFilter() {
        final int color = ResourceUtil.getCardColor(presenter.getPaymentMethod().getId(), this);
        mSecurityCodeCardIcon.setColorFilter(ContextCompat.getColor(this, color), PorterDuff.Mode.DST_OVER);
    }

    @Override
    public void initialize() {
        initializeControls();
        presenter.initializeSettings();
        loadViews();
    }

    @Override
    public void showTimer() {
        if (CheckoutTimer.getInstance().isTimerEnabled()) {
            mTimerTextView.setVisibility(View.VISIBLE);
            mTimerTextView.setText(CheckoutTimer.getInstance().getCurrentTime());
        }
    }

    @Override
    public void showBackSecurityCodeCardView() {
        mSecurityCodeCardIcon.setImageResource(R.drawable.px_tiny_card_cvv_screen);
        setSecurityCodeCardColorFilter();
    }

    @Override
    public void showFrontSecurityCodeCardView() {
        mSecurityCodeCardIcon.setImageResource(R.drawable.px_amex_tiny_card_cvv_screen);
        setSecurityCodeCardColorFilter();
    }

    @Override
    public void showStandardErrorMessage() {
        final String standardErrorMessage = getString(R.string.px_standard_error_message);
        showError(MercadoPagoError.createNotRecoverable(standardErrorMessage), TextUtil.EMPTY);
    }

    @Override
    public void setSecurityCodeInputMaxLength(int length) {
        setInputMaxLength(mSecurityCodeEditText, length);
    }

    private void setInputMaxLength(MPEditText text, int maxLength) {
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        text.setFilters(fArray);
    }

    private void setSecurityCodeListeners() {
        mSecurityCodeEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEditText(mSecurityCodeEditText, event);
                return true;
            }
        });
        mSecurityCodeEditText
            .addTextChangedListener(new CardSecurityCodeTextWatcher(new CardSecurityCodeEditTextCallback() {
                @Override
                public void checkOpenKeyboard() {
                    openKeyboard(mSecurityCodeEditText);
                }

                @Override
                public void saveSecurityCode(CharSequence s) {
                    presenter.saveSecurityCode(s.toString());
                    mCardView.setSecurityCodeLocation(presenter.getSecurityCodeLocation());
                    mCardView.drawEditingSecurityCode(s.toString());
                }

                @Override
                public void changeErrorView() {
                    clearErrorView();
                }

                @Override
                public void toggleLineColorOnError(boolean toggle) {
                    mSecurityCodeEditText.toggleLineColorOnError(toggle);
                }
            }));
        mSecurityCodeEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return onNextKey(actionId, event);
            }
        });
    }

    private boolean onNextKey(int actionId, KeyEvent event) {
        if (isNextKey(actionId, event)) {
            presenter.validateSecurityCodeInput();
            return true;
        }
        return false;
    }

    private boolean isNextKey(int actionId, KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_NEXT ||
            (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    private void setButtonsListeners() {
        setNextButtonListeners();
        setBackButtonListeners();
    }

    private void setNextButtonListeners() {
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.validateSecurityCodeInput();
            }
        });
    }

    public void setBackButtonListeners() {
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackButtonPressed();
            }
        });
    }

    private void setErrorState(String mErrorState) {
        this.mErrorState = mErrorState;
    }

    @Override
    public void setErrorView(CardTokenException exception) {
        mSecurityCodeEditText.toggleLineColorOnError(true);
        mButtonContainer.setVisibility(View.GONE);
        mErrorContainer.setVisibility(View.VISIBLE);
        String errorText = ExceptionHandler.getErrorMessage(this, exception);
        mErrorTextView.setText(errorText);
        setErrorState(ERROR_STATE);
    }

    @Override
    public void showError(MercadoPagoError error, String requestOrigin) {
        if (error.isApiException()) {
            showApiExceptionError(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    public void clearErrorView() {
        mSecurityCodeEditText.toggleLineColorOnError(false);
        mButtonContainer.setVisibility(View.VISIBLE);
        mErrorContainer.setVisibility(View.GONE);
        mErrorTextView.setText("");
        setErrorState(NORMAL_STATE);
    }

    private void onTouchEditText(MPEditText editText, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            openKeyboard(editText);
        }
    }

    private void openKeyboard(MPEditText ediText) {
        ediText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ediText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                presenter.recoverFromFailure();
            } else {
                setResult(RESULT_CANCELED, data);
                finish();
            }
        }
    }

    @Override
    public void finishWithResult() {
        setResult(RESULT_OK);
        finish();
    }

    public static final class Builder {
        private CardInfo cardInformation;
        private PaymentMethod paymentMethod;
        private Card card;
        private Token token;
        private PaymentRecovery paymentRecovery;
        private Reason reason;

        public Builder() {
        }

        public Builder setCardInfo(final CardInfo cardInformation) {
            this.cardInformation = cardInformation;
            return this;
        }

        public Builder setPaymentRecovery(final PaymentRecovery paymentRecovery) {
            this.paymentRecovery = paymentRecovery;
            return this;
        }

        public Builder setPaymentMethod(final PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder setCard(final Card card) {
            this.card = card;
            return this;
        }

        public Builder setToken(final Token token) {
            this.token = token;
            return this;
        }

        public Builder setReason(final Reason reason) {
            this.reason = reason;
            return this;
        }

        public void startActivity(@NonNull final Activity activity, final int requestCode) {
            if (reason == null) {
                throw new IllegalStateException("reason is null");
            }
            if (cardInformation == null) {
                throw new IllegalStateException("card info is null");
            }
            if (paymentMethod == null) {
                throw new IllegalStateException("payment method is null");
            }
            if (card != null && token != null && paymentRecovery == null) {
                throw new IllegalStateException(
                    "can't start with card and token at the same time if it's not recoverable");
            }
            if (card == null && token == null) {
                throw new IllegalStateException("card and token can't both be null");
            }

            startSecurityCodeActivity(activity, requestCode);
        }

        private void startSecurityCodeActivity(@NonNull final Activity activity, final int requestCode) {
            final Intent intent = new Intent(activity, SecurityCodeActivity.class);
            intent.putExtra(EXTRA_PAYMENT_METHOD, JsonUtil.getInstance().toJson(paymentMethod));
            intent.putExtra(EXTRA_TOKEN, JsonUtil.getInstance().toJson(token));
            intent.putExtra(EXTRA_CARD, JsonUtil.getInstance().toJson(card));
            intent.putExtra(EXTRA_CARD_INFO, JsonUtil.getInstance().toJson(cardInformation));
            intent.putExtra(EXTRA_PAYMENT_RECOVERY, JsonUtil.getInstance().toJson(paymentRecovery));
            intent.putExtra(EXTRA_REASON, reason.name());
            activity.startActivityForResult(intent, requestCode);
        }
    }
}