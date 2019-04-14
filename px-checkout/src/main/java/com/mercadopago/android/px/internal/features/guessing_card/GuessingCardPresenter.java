package com.mercadopago.android.px.internal.features.guessing_card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.android.px.core.internal.MercadoPagoCardStorage;
import com.mercadopago.android.px.internal.base.BasePresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.di.CardAssociationSession;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.uicontrollers.card.CardView;
import com.mercadopago.android.px.internal.features.uicontrollers.card.FrontCardView;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.IdentificationUtils;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.util.MPCardMaskUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.BankDeal;
import com.mercadopago.android.px.model.Bin;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.CardToken;
import com.mercadopago.android.px.model.Cardholder;
import com.mercadopago.android.px.model.Identification;
import com.mercadopago.android.px.model.IdentificationType;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.PaymentType;
import com.mercadopago.android.px.model.SecurityCode;
import com.mercadopago.android.px.model.Setting;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.CardTokenException;
import com.mercadopago.android.px.model.exceptions.InvalidFieldException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker;
import com.mercadopago.android.px.tracking.internal.views.CardHolderNameViewTracker;
import com.mercadopago.android.px.tracking.internal.views.CardNumberViewTracker;
import com.mercadopago.android.px.tracking.internal.views.CvvGuessingViewTracker;
import com.mercadopago.android.px.tracking.internal.views.ExpirationDateViewTracker;
import com.mercadopago.android.px.tracking.internal.views.IdentificationViewTracker;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mercadopago.android.px.model.Card.CARD_DEFAULT_SECURITY_CODE_LENGTH;

public abstract class GuessingCardPresenter extends BasePresenter<GuessingCard.View>
    implements GuessingCard.Actions {

    protected static final String CARD_SIDE_STATE_BUNDLE = "cardSideState";
    protected static final String PAYMENT_METHOD_BUNDLE = "paymentMethod";
    protected static final String ID_REQUIRED_BUNDLE = "identificationNumberRequired";
    protected static final String SEC_CODE_REQUIRED_BUNDLE = "isSecurityCodeRequired";
    protected static final String SEC_CODE_LENGTH_BUNDLE = "cardSecurityCodeLength";
    protected static final String CARD_NUMBER_LENGTH_BUNDLE = "cardNumberLength";
    protected static final String SEC_CODE_LOCATION_BUNDLE = "securityCodeLocation";
    protected static final String CARD_TOKEN_BUNDLE = "cardToken";
    protected static final String CARD_INFO_BIN_BUNDLE = "bin";
    protected static final String EXPIRY_MONTH_BUNDLE = "expiryMonth";
    protected static final String EXPIRY_YEAR_BUNDLE = "expiryYear";
    protected static final String CARD_NUMBER_BUNDLE = "cardNumber";
    protected static final String CARD_NAME_BUNDLE = "cardName";
    protected static final String IDENTIFICATION_BUNDLE = "identification";
    protected static final String IDENTIFICATION_NUMBER_BUNDLE = "identificationNumber";
    protected static final String IDENTIFICATION_TYPE_BUNDLE = "identificationType";
    protected static final String PAYMENT_TYPES_LIST_BUNDLE = "paymentTypeList";
    protected static final String BANK_DEALS_LIST_BUNDLE = "bankDealsList";
    protected static final String IDENTIFICATION_TYPES_LIST_BUNDLE = "idTypesList";
    protected static final String PAYMENT_RECOVERY_BUNDLE = "paymentRecovery";
    protected static final String LOW_RES_BUNDLE = "lowRes";
    protected static final String TOKEN_BUNDLE = "tokenBundle";
    //Card Info
    protected String bin;
    protected boolean showPaymentTypes;
    protected boolean eraseSpace;
    //Activity parameters
    protected PaymentMethodGuessingController paymentMethodGuessingController;
    protected Identification identification;
    protected Token token;
    protected CardToken cardToken;
    // Extra info
    private List<PaymentType> paymentTypeList;
    private List<IdentificationType> identificationTypes;
    private String cardNumber;
    private int currentNumberLength;
    private int securityCodeLength;
    private String securityCodeLocation;
    private String cardholderName;
    private String expiryMonth;
    private String expiryYear;
    private IdentificationType identificationType;
    private String identificationNumber;
    private boolean isSecurityCodeRequired;
    private boolean identificationNumberRequired;
    private FailureRecovery failureRecovery;
    private String securityCode;

    public GuessingCardPresenter() {
        cardToken = CardToken.createEmpty();
        token = new Token();
        identification = new Identification();
        eraseSpace = true;
    }

    public static GuessingCardPaymentPresenter buildGuessingCardPaymentPresenter(final Session session,
        final PaymentRecovery paymentRecovery) {
        return new GuessingCardPaymentPresenter(
            session.getConfigurationModule().getUserSelectionRepository(),
            session.getConfigurationModule().getPaymentSettings(), session.getInitRepository(),
            session.getIssuersRepository(),
            session.getCardTokenRepository(), session.getBankDealsRepository(),
            session.getIdentificationRepository(),
            session.getConfigurationModule().getPaymentSettings().getAdvancedConfiguration(),
            paymentRecovery,
            session.getSummaryAmountRepository(),
            session.provideIssuersSolver(),
            session.providePayerCostSolver());
    }

    public static GuessingCardPresenter buildGuessingCardStoragePresenter(final Session session,
        final CardAssociationSession cardAssociationSession,
        @NonNull final MercadoPagoCardStorage mercadoPagoCardStorage) {
        return new GuessingCardStoragePresenter(mercadoPagoCardStorage,
            cardAssociationSession.getCardPaymentMethodRepository(),
            session.getIdentificationRepository(), cardAssociationSession.getCardAssociationService(),
            cardAssociationSession.getMercadoPagoESC(),
            cardAssociationSession.getGatewayService());
    }

    /* default */ void trackCardNumber() {
        final CardNumberViewTracker cardNumberViewTracker = new CardNumberViewTracker();
        setCurrentViewTracker(cardNumberViewTracker);
    }

    /* default */ void trackCardIdentification() {
        if (TextUtil.isNotEmpty(getPaymentTypeId()) && getPaymentMethod() != null) {
            final IdentificationViewTracker identificationViewTracker =
                new IdentificationViewTracker(getPaymentTypeId(), getPaymentMethod().getId());
            setCurrentViewTracker(identificationViewTracker);
        }
    }

    /* default */ void trackCardHolderName() {
        if (TextUtil.isNotEmpty(getPaymentTypeId()) && getPaymentMethod() != null) {
            final CardHolderNameViewTracker cardHolderNameViewTracker =
                new CardHolderNameViewTracker(getPaymentTypeId(), getPaymentMethod().getId());
            setCurrentViewTracker(cardHolderNameViewTracker);
        }
    }

    /* default */ void trackCardExpiryDate() {
        if (TextUtil.isNotEmpty(getPaymentTypeId()) && getPaymentMethod() != null) {
            final ExpirationDateViewTracker expirationDateViewTracker =
                new ExpirationDateViewTracker(getPaymentTypeId(), getPaymentMethod().getId());
            setCurrentViewTracker(expirationDateViewTracker);
        }
    }

    /* default */ void trackCardSecurityCode() {
        if (TextUtil.isNotEmpty(getPaymentTypeId()) && getPaymentMethod() != null) {
            final CvvGuessingViewTracker cvvGuessingViewTracker =
                new CvvGuessingViewTracker(getPaymentTypeId(), getPaymentMethod().getId());
            setCurrentViewTracker(cvvGuessingViewTracker);
        }
    }

    public void resolvePaymentMethodListSet(final List<PaymentMethod> paymentMethodList, final String bin) {
        saveBin(bin);
        if (paymentMethodList.isEmpty()) {
            FrictionEventTracker.with(FrictionEventTracker.Id.INVALID_BIN,
                new CardNumberViewTracker(),
                FrictionEventTracker.Style.SNACKBAR);

            getView().setCardNumberInputMaxLength(Bin.BIN_LENGTH);
            setInvalidCardMessage();
        } else if (paymentMethodList.size() == 1) {
            onPaymentMethodSet(paymentMethodList.get(0));
        } else if (shouldAskPaymentType(paymentMethodList)) {
            enablePaymentTypeSelection(paymentMethodList);
            onPaymentMethodSet(paymentMethodList.get(0));
        } else {
            onPaymentMethodSet(paymentMethodList.get(0));
        }
    }

    private void enablePaymentTypeSelection(final Iterable<PaymentMethod> paymentMethodList) {
        final List<PaymentType> paymentTypesList = new ArrayList<>();
        for (final PaymentMethod pm : paymentMethodList) {
            final PaymentType type = new PaymentType(pm.getPaymentTypeId());
            paymentTypesList.add(type);
        }
        paymentTypeList = paymentTypesList;
        showPaymentTypes = true;
    }

    public boolean shouldAskPaymentType(@NonNull final List<PaymentMethod> paymentMethodList) {
        final String paymentType = paymentMethodList.get(0).getPaymentTypeId();
        for (final PaymentMethod currentPaymentMethod : paymentMethodList) {
            if (!paymentType.equals(currentPaymentMethod.getPaymentTypeId())) {
                return true;
            }
        }
        return false;
    }

    private void setInvalidCardMessage() {
        if (onlyOnePaymentMethodSupported()) {
            getView().setInvalidCardOnePaymentMethodErrorView();
        } else {
            getView().setInvalidCardMultipleErrorView();
        }
    }

    private boolean onlyOnePaymentMethodSupported() {
        final List<PaymentMethod> supportedPaymentMethods = getAllSupportedPaymentMethods();
        return supportedPaymentMethods != null && supportedPaymentMethods.size() == 1;
    }

    public List<PaymentMethod> getAllSupportedPaymentMethods() {
        if (paymentMethodGuessingController != null) {
            return paymentMethodGuessingController.getAllSupportedPaymentMethods();
        }
        return Collections.emptyList();
    }

    protected void saveBin(@Nullable final String bin) {
        this.bin = bin;
        if (paymentMethodGuessingController != null) {
            paymentMethodGuessingController.saveBin(bin);
        }
    }

    public void saveCardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCurrentNumberLength(final int currentNumberLength) {
        this.currentNumberLength = currentNumberLength;
    }

    public int getSecurityCodeLength() {
        return securityCodeLength;
    }

    public String getSecurityCodeLocation() {
        return securityCodeLocation;
    }

    public void setSecurityCodeLocation(final String securityCodeLocation) {
        this.securityCodeLocation = securityCodeLocation;
    }

    public void saveCardholderName(final String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public void saveExpiryMonth(final String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public void saveExpiryYear(final String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public void saveSecurityCode(final String securityCode) {
        this.securityCode = securityCode;
    }

    public void saveIdentificationType(final IdentificationType identificationType) {
        this.identificationType = identificationType;
        if (identificationType != null) {
            identification.setType(identificationType.getId());
            getView().setIdentificationNumberRestrictions(identificationType.getType());
        }
    }

    public void saveIdentificationNumber(final String identificationNumber) {
        this.identificationNumber = identificationNumber;
        if (TextUtil.isEmpty(identificationNumber)) {
            getView().clearErrorView();
            getView().clearErrorIdentificationNumber();
        } else if (getIdentificationNumberMaxLength() == identificationNumber.length()) {
            validateIdentificationNumber();
        }
    }

    public int getIdentificationNumberMaxLength() {
        if (identificationType != null) {
            return identificationType.getMaxLength();
        } else {
            return Card.CARD_DEFAULT_IDENTIFICATION_NUMBER_LENGTH;
        }
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(final String number) {
        identificationNumber = number;
        identification.setNumber(number);
    }

    @Override
    public void validateIdentificationNumberAndContinue() {
        try {
            validateCardIdentification();
            checkFinishWithCardToken();
        } catch (InvalidFieldException e) {
            trackInvalidDocument();
            showErrorView(e);
        }
    }

    private void showErrorView(final InvalidFieldException e) {
        switch (e.getErrorCode()) {
        case InvalidFieldException.INVALID_IDENTIFICATION_LENGHT:
            getView().showInvalidIdentificationNumberLengthErrorView();
            break;
        default:
            getView().showInvalidIdentificationNumberErrorView();
            break;
        }
    }

    private void trackInvalidDocument() {
        FrictionEventTracker.with(FrictionEventTracker.Id.INVALID_DOCUMENT,
            new IdentificationViewTracker(getPaymentMethod().getPaymentTypeId(),
                getPaymentMethod().getId()), FrictionEventTracker.Style.CUSTOM_COMPONENT,
            getPaymentMethod()).track();
    }

    @Override
    public void validateIdentificationNumber() {
        try {
            validateCardIdentification();
        } catch (InvalidFieldException e) {
            trackInvalidDocument();
            showErrorView(e);
        }
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(@Nullable final String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(@Nullable final String name) {
        cardholderName = name;
    }

    public boolean validateCardName() {
        final Cardholder cardHolder = new Cardholder();
        cardHolder.setName(getCardholderName());
        cardHolder.setIdentification(identification);
        cardToken.setCardholder(cardHolder);
        if (cardToken.validateCardholderName()) {
            getView().clearErrorView();
            return true;
        } else {

            FrictionEventTracker.with(FrictionEventTracker.Id.INVALID_NAME,
                new CardHolderNameViewTracker(getPaymentMethod().getPaymentTypeId(),
                    getPaymentMethod().getId()), FrictionEventTracker.Style.CUSTOM_COMPONENT,
                getPaymentMethod()).track();

            getView().setInvalidEmptyNameErrorView();
            getView().setErrorCardholderName();
            return false;
        }
    }

    public boolean validateExpiryDate() {
        final String monthString = getExpiryMonth();
        final String yearString = getExpiryYear();
        final Integer month = TextUtil.isDigitsOnly(monthString) ? Integer.valueOf(monthString) : null;
        final Integer year = TextUtil.isDigitsOnly(yearString) ? Integer.valueOf(yearString) : null;
        cardToken.setExpirationMonth(month);
        cardToken.setExpirationYear(year);
        if (cardToken.validateExpiryDate()) {
            getView().clearErrorView();
            return true;
        } else {
            FrictionEventTracker
                .with(FrictionEventTracker.Id.INVALID_EXP_DATE,
                    new ExpirationDateViewTracker(getPaymentMethod().getPaymentTypeId(), getPaymentMethod().getId()),
                    FrictionEventTracker.Style.CUSTOM_COMPONENT,
                    getPaymentMethod())
                .track();
            getView().setInvalidExpiryDateErrorView();
            getView().setErrorExpiryDate();
            return false;
        }
    }

    public boolean checkIsEmptyOrValidExpiryDate() {
        return TextUtils.isEmpty(expiryMonth) || validateExpiryDate();
    }

    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(@Nullable final String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(@Nullable final String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public boolean isSecurityCodeRequired() {
        return isSecurityCodeRequired;
    }

    /* default */ void setSecurityCodeRequired(final boolean required) {
        isSecurityCodeRequired = required;
    }

    public boolean isIdentificationNumberRequired() {
        return identificationNumberRequired;
    }

    protected void setIdentificationNumberRequired(final boolean identificationNumberRequired) {
        this.identificationNumberRequired = identificationNumberRequired;
        getView().showIdentificationInput();
    }

    public boolean checkIsEmptyOrValidCardholderName() {
        return TextUtils.isEmpty(cardholderName) || validateCardName();
    }

    public boolean checkIsEmptyOrValidSecurityCode() {
        return TextUtils.isEmpty(securityCode) || validateSecurityCode();
    }

    private void validateCardIdentification() throws InvalidFieldException {
        identification.setNumber(getIdentificationNumber());
        IdentificationUtils.validateCardIdentification(cardToken, identification, identificationType);
        getView().clearErrorView();
        getView().clearErrorIdentificationNumber();
    }

    public String getSecurityCode() {
        return securityCode;
    }

    @Nullable
    public String getSecurityCodeFront() {
        String securityCode = null;
        if (securityCodeLocation.equals(CardView.CARD_SIDE_FRONT)) {
            securityCode = getSecurityCode();
        }
        return securityCode;
    }

    /* default */ List<PaymentType> getPaymentTypes() {
        return paymentTypeList;
    }

    /* default */
    void resolveTokenCreationError(final MercadoPagoError error, final String requestOrigin) {
        if (isIdentificationNumberWrong(error)) {
            showIdentificationNumberError();
        } else {
            setFailureRecovery(new FailureRecovery() {
                @Override
                public void recover() {
                    createToken();
                }
            });
            if (isViewAttached()) {
                getView().showError(error, requestOrigin);
            }
        }
    }

    protected boolean isIdentificationNumberWrong(final MercadoPagoError error) {
        return error.isApiException() &&
            error.getApiException().containsCause(ApiException.ErrorCodes.INVALID_CARD_HOLDER_IDENTIFICATION_NUMBER);
    }

    protected void showIdentificationNumberError() {
        getView().hideProgress();
        getView().setInvalidFieldErrorView();
        getView().showErrorIdentificationNumber();
    }

    public void setSelectedPaymentType(final PaymentType paymentType) {
        if (paymentMethodGuessingController == null) {
            return;
        }
        for (final PaymentMethod paymentMethod : paymentMethodGuessingController.getGuessedPaymentMethods()) {
            if (paymentMethod.getPaymentTypeId().equals(paymentType.getId())) {
                setPaymentMethod(paymentMethod);
            }
        }
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    public List<IdentificationType> getIdentificationTypes() {
        return identificationTypes;
    }

    /* default */ void loadIdentificationTypes(final PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return;
        }
        identificationNumberRequired = paymentMethod.isIdentificationNumberRequired();
        if (!identificationNumberRequired) {
            getView().hideIdentificationInput();
        } else if (identificationTypes != null) {
            getView().initializeIdentificationTypes(identificationTypes, identificationType);
        } else {
            getIdentificationTypesAsync();
        }
    }

    protected void resolveIdentificationTypes(final List<IdentificationType> identificationTypeList) {
        saveIdentificationTypes(identificationTypeList);
        //TODO identificationTypes are filtered until new card flow + cnpj is analized.
        if (identificationTypes == null || identificationTypes.isEmpty()) {
            getView().showMissingIdentificationTypesError(false, ApiUtil.RequestOrigin.GET_IDENTIFICATION_TYPES);
        } else {
            getView().initializeIdentificationTypes(identificationTypes, identificationType);
        }
    }

    private void saveIdentificationTypes(final List<IdentificationType> identificationTypeList) {
        identificationTypes = new ArrayList<>();

        if (identificationTypeList == null) {
            return;
        }

        for (final IdentificationType identificationType : identificationTypeList) {
            //TODO Do not filter CNPJ when new card flow + cnpj is analized.
            if (!IdentificationUtils.isCnpj(identificationType)) {
                identificationTypes.add(identificationType);
            }
        }
        if (!identificationTypes.isEmpty()) {
            identificationType = identificationTypeList.get(0);
        }
    }

    protected void configureWithSettings(final PaymentMethod paymentMethod) {
        if (paymentMethod != null) {
            isSecurityCodeRequired = paymentMethod.isSecurityCodeRequired(bin);
            if (!isSecurityCodeRequired) {
                getView().hideSecurityCodeInput();
            }
            final Setting setting =
                Setting.getSettingByPaymentMethodAndBin(paymentMethod, bin);
            if (setting == null) {
                getView().showSettingNotFoundForBinError();
            } else {
                final int cardNumberLength = getCardNumberLength();
                int spaces = FrontCardView.CARD_DEFAULT_AMOUNT_SPACES;

                if (cardNumberLength == FrontCardView.CARD_NUMBER_DINERS_LENGTH ||
                    cardNumberLength == FrontCardView.CARD_NUMBER_AMEX_LENGTH ||
                    cardNumberLength == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_1_LENGTH) {
                    spaces = FrontCardView.CARD_AMEX_DINERS_AMOUNT_SPACES;
                } else if (cardNumberLength == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_2_LENGTH) {
                    spaces = FrontCardView.CARD_NUMBER_MAESTRO_SETTING_2_AMOUNT_SPACES;
                }
                getView().setCardNumberInputMaxLength(cardNumberLength + spaces);
                final SecurityCode securityCode = setting.getSecurityCode();
                if (securityCode == null) {
                    securityCodeLength = CARD_DEFAULT_SECURITY_CODE_LENGTH;
                    securityCodeLocation = CardView.CARD_SIDE_BACK;
                } else {
                    securityCodeLength = securityCode.getLength();
                    securityCodeLocation = securityCode.getCardLocation();
                }
                getView().setSecurityCodeInputMaxLength(securityCodeLength);
                getView().setSecurityCodeViewLocation(securityCodeLocation);
            }
        }
    }

    protected void startGuessingForm() {
        getView().initializeTitle();
        getView().setCardNumberListeners(paymentMethodGuessingController);
        getView().setCardholderNameListeners();
        getView().setExpiryDateListeners();
        getView().setSecurityCodeListeners();
        getView().setIdentificationTypeListeners();
        getView().setIdentificationNumberListeners();
        getView().setNextButtonListeners();
        getView().setBackButtonListeners();
        getView().setErrorContainerListener();
        getView().setContainerAnimationListeners();
        checkPaymentMethodsSupported(false);
    }

    private void checkPaymentMethodsSupported(final boolean withAnimation) {
        final List<PaymentMethod> supportedPaymentMethods = getAllSupportedPaymentMethods();
        if (supportedPaymentMethods != null && supportedPaymentMethods.size() == 1) {
            getView().setExclusionWithOneElementInfoView(getAllSupportedPaymentMethods().get(0), withAnimation);
        }
    }

    protected int getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(getPaymentMethod(), bin);
    }

    protected Identification getIdentification() {
        return identification;
    }

    public void setIdentification(final Identification identification) {
        this.identification = identification;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(final Token token) {
        this.token = token;
    }

    public CardToken getCardToken() {
        return cardToken;
    }

    protected void setCardToken(final CardToken cardToken) {
        this.cardToken = cardToken;
    }

    protected void setPaymentTypesList(@Nullable final List<PaymentType> paymentTypesList) {
        paymentTypeList = paymentTypesList;
    }

    public boolean isDefaultSpaceErasable() {
        if (MPCardMaskUtil.isDefaultSpaceErasable(currentNumberLength)) {
            eraseSpace = true;
        }

        if (getPaymentMethod() != null && bin != null && eraseSpace &&
            (getCardNumberLength() == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_1_LENGTH ||
                getCardNumberLength() == FrontCardView.CARD_NUMBER_MAESTRO_SETTING_2_LENGTH)) {
            eraseSpace = false;
            return true;
        }
        return false;
    }

    public FailureRecovery getFailureRecovery() {
        return failureRecovery;
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    public boolean validateSecurityCode() {
        cardToken.setSecurityCode(getSecurityCode());
        try {
            cardToken.validateSecurityCode(getPaymentMethod());
            getView().clearErrorView();
            return true;
        } catch (final CardTokenException e) {

            FrictionEventTracker.with(FrictionEventTracker.Id.INVALID_CVV,
                new CvvGuessingViewTracker(getPaymentMethod().getPaymentTypeId(),
                    getPaymentMethod().getId()), FrictionEventTracker.Style.CUSTOM_COMPONENT, getPaymentMethod())
                .track();

            setCardSecurityCodeErrorView(e);

            return false;
        }
    }

    private void setCardSecurityCodeErrorView(final CardTokenException exception) {
        if (!isSecurityCodeRequired()) {
            return;
        }
        getView().setErrorView(exception);
        getView().setErrorSecurityCode();
    }

    public boolean validateCardNumber() {
        cardToken.setCardNumber(getCardNumber());
        try {
            final PaymentMethod paymentMethod = getPaymentMethod();
            if (paymentMethod == null) {
                if (getCardNumber() == null || getCardNumber().length() < Bin.BIN_LENGTH) {
                    throw new CardTokenException(CardTokenException.INVALID_CARD_NUMBER_INCOMPLETE);
                } else if (getCardNumber().length() == Bin.BIN_LENGTH) {
                    throw new CardTokenException(CardTokenException.INVALID_PAYMENT_METHOD);
                } else {
                    throw new CardTokenException(CardTokenException.INVALID_PAYMENT_METHOD);
                }
            }
            cardToken.validateCardNumber(paymentMethod);
            getView().clearErrorView();
            return true;
        } catch (final CardTokenException e) {

            FrictionEventTracker
                .with(FrictionEventTracker.Id.INVALID_CC_NUMBER,
                    new CardNumberViewTracker(),
                    FrictionEventTracker.Style.CUSTOM_COMPONENT,
                    getPaymentMethod());

            getView().setErrorView(e);
            getView().setErrorCardNumber();
            return false;
        }
    }

    public PaymentMethodGuessingController getGuessingController() {
        return paymentMethodGuessingController;
    }

    @Nullable
    protected List<PaymentMethod> getGuessedPaymentMethods() {
        if (paymentMethodGuessingController == null) {
            return null;
        }
        return paymentMethodGuessingController.getGuessedPaymentMethods();
    }

    protected IdentificationType getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(final IdentificationType identificationType) {
        this.identificationType = identificationType;
    }

    protected void clearCardSettings() {
        securityCodeLength = CARD_DEFAULT_SECURITY_CODE_LENGTH;
        securityCodeLocation = CardView.CARD_SIDE_BACK;
        isSecurityCodeRequired = true;
        bin = "";
    }

    public void resolvePaymentMethodCleared() {
        setPaymentMethod(null);
        getView().clearErrorView();
        getView().hideRedErrorContainerView(true);
        getView().restoreBlackInfoContainerView();
        getView().clearCardNumberInputLength();
        eraseSpace = true;
        getView().clearSecurityCodeEditText();
        cardToken = CardToken.createEmpty();
        setIdentificationNumberRequired(true);
        setSecurityCodeRequired(true);
        showPaymentTypes = false;
        paymentTypeList = null;
        getView().checkClearCardView();
        checkPaymentMethodsSupported(true);
    }

    public String getSavedBin() {
        return bin;
    }

    public void checkFinishWithCardToken() {
        if (showPaymentTypes && getGuessedPaymentMethods() != null) {
            getView().askForPaymentType(getGuessedPaymentMethods(), getPaymentTypes(), new CardInfo(getCardToken()));
        } else {
            getView().showFinishCardFlow();
        }
    }

    public void onPaymentMethodSet(final PaymentMethod paymentMethod) {
        setPaymentMethod(paymentMethod);
        configureWithSettings(paymentMethod);
        loadIdentificationTypes(paymentMethod);
        getView().setPaymentMethod(paymentMethod);
        getView().resolvePaymentMethodSet(paymentMethod);
        //We need to erase default space in position 4 in some special cases.
        if (isDefaultSpaceErasable()) {
            getView().eraseDefaultSpace();
        }
    }

    public abstract void initialize();

    @Nullable
    public abstract String getPaymentTypeId();

    public abstract PaymentMethod getPaymentMethod();

    public abstract void setPaymentMethod(@Nullable final PaymentMethod paymentMethod);

    public abstract void getIdentificationTypesAsync();

    public abstract void createToken();

    public abstract void getPaymentMethods();

    public abstract void resolveTokenRequest(final Token token);

    public abstract List<BankDeal> getBankDealsList();

    public abstract void onIssuerSelected(Long issuerId);

    public void onSaveInstanceState(final Bundle outState, final String cardSideState,
        final boolean lowResActive) {
        outState.putString(CARD_SIDE_STATE_BUNDLE, cardSideState);
        outState.putString(PAYMENT_METHOD_BUNDLE, JsonUtil.getInstance().toJson(getPaymentMethod()));
        outState.putBoolean(ID_REQUIRED_BUNDLE, isIdentificationNumberRequired());
        outState.putBoolean(SEC_CODE_REQUIRED_BUNDLE, isSecurityCodeRequired());
        outState.putInt(SEC_CODE_LENGTH_BUNDLE, getSecurityCodeLength());
        outState.putInt(CARD_NUMBER_LENGTH_BUNDLE, getCardNumberLength());
        outState.putString(SEC_CODE_LOCATION_BUNDLE, getSecurityCodeLocation());
        outState.putString(CARD_TOKEN_BUNDLE, JsonUtil.getInstance().toJson(getCardToken()));
        outState.putString(CARD_INFO_BIN_BUNDLE, getSavedBin());
        outState.putString(CARD_NUMBER_BUNDLE, getCardNumber());
        outState.putString(CARD_NAME_BUNDLE, getCardholderName());
        outState.putString(EXPIRY_MONTH_BUNDLE, getExpiryMonth());
        outState.putString(EXPIRY_YEAR_BUNDLE, getExpiryYear());
        outState.putString(IDENTIFICATION_BUNDLE, JsonUtil.getInstance().toJson(getIdentification()));
        outState.putString(IDENTIFICATION_NUMBER_BUNDLE, getIdentificationNumber());
        outState.putString(IDENTIFICATION_TYPE_BUNDLE,
            JsonUtil.getInstance().toJson(getIdentificationType()));
        outState.putString(IDENTIFICATION_TYPES_LIST_BUNDLE,
            JsonUtil.getInstance().toJson(getIdentificationTypes()));
        outState.putBoolean(LOW_RES_BUNDLE, lowResActive);
        outState.putString(TOKEN_BUNDLE, JsonUtil.getInstance().toJson(getToken()));
        getView().clearSecurityCodeEditText();
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        final String paymentMethodBundleJson = savedInstanceState.getString(PAYMENT_METHOD_BUNDLE);
        if (!TextUtil.isEmpty(paymentMethodBundleJson)) {
            final PaymentMethod pm = JsonUtil.getInstance()
                .fromJson(paymentMethodBundleJson, PaymentMethod.class);
            if (pm != null) {
                try {
                    final Type listType = new TypeToken<List<IdentificationType>>() {
                    }.getType();
                    identificationTypes = JsonUtil.getInstance().getGson().fromJson(
                        savedInstanceState.getString(IDENTIFICATION_TYPES_LIST_BUNDLE), listType);
                } catch (final Exception ex) {
                    identificationTypes = null;
                }
                saveBin(savedInstanceState.getString(CARD_INFO_BIN_BUNDLE));
                setIdentificationNumberRequired(savedInstanceState.getBoolean(ID_REQUIRED_BUNDLE));
                setSecurityCodeRequired(savedInstanceState.getBoolean(SEC_CODE_REQUIRED_BUNDLE));
                setCardNumber(savedInstanceState.getString(CARD_NUMBER_BUNDLE));
                setCardholderName(savedInstanceState.getString(CARD_NAME_BUNDLE));
                setExpiryMonth(savedInstanceState.getString(EXPIRY_MONTH_BUNDLE));
                setExpiryYear(savedInstanceState.getString(EXPIRY_YEAR_BUNDLE));
                final Token token = JsonUtil.getInstance()
                    .fromJson(savedInstanceState.getString(TOKEN_BUNDLE), Token.class);
                setToken(token);
                final String idNumber = savedInstanceState.getString(IDENTIFICATION_NUMBER_BUNDLE);
                setIdentificationNumber(idNumber);
                final Identification identification = JsonUtil.getInstance()
                    .fromJson(savedInstanceState.getString(IDENTIFICATION_BUNDLE), Identification.class);
                identification.setNumber(idNumber);
                setIdentification(identification);
                setSecurityCodeLocation(savedInstanceState.getString(SEC_CODE_LOCATION_BUNDLE));
                final CardToken cardToken = JsonUtil.getInstance()
                    .fromJson(savedInstanceState.getString(CARD_TOKEN_BUNDLE), CardToken.class);
                cardToken.getCardholder().setIdentification(identification);
                final IdentificationType identificationType = JsonUtil.getInstance()
                    .fromJson(savedInstanceState.getString(IDENTIFICATION_TYPE_BUNDLE),
                        IdentificationType.class);
                setCardToken(cardToken);
                setIdentificationType(identificationType);
                final boolean lowResActive = savedInstanceState.getBoolean(LOW_RES_BUNDLE);
                getView().recoverCardViews(lowResActive, getCardNumber(), getCardholderName(), getExpiryMonth(),
                    getExpiryYear(), idNumber, identificationType);
                onPaymentMethodSet(pm);
            }
        }
    }

    @Override
    public void trackAbort() {
        tracker.trackAbort();
    }

    @Override
    public void trackBack() {
        tracker.trackBack();
    }
}
