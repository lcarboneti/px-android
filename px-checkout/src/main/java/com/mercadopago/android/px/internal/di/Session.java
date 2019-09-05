package com.mercadopago.android.px.internal.di;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.addons.internal.ESCManagerBehaviourProvider;
import com.mercadopago.android.px.addons.internal.PXApplicationBehaviourProvider;
import com.mercadopago.android.px.addons.model.SecurityValidationData;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.core.SplitPaymentProcessor;
import com.mercadopago.android.px.internal.configuration.InternalConfiguration;
import com.mercadopago.android.px.internal.core.ApplicationModule;
import com.mercadopago.android.px.internal.core.SessionIdProvider;
import com.mercadopago.android.px.internal.datasource.AmountConfigurationRepositoryImpl;
import com.mercadopago.android.px.internal.datasource.AmountService;
import com.mercadopago.android.px.internal.datasource.BankDealsService;
import com.mercadopago.android.px.internal.datasource.CardTokenService;
import com.mercadopago.android.px.internal.datasource.CheckoutPreferenceService;
import com.mercadopago.android.px.internal.datasource.DiscountServiceImp;
import com.mercadopago.android.px.internal.datasource.EscPaymentManagerImp;
import com.mercadopago.android.px.internal.datasource.GroupsService;
import com.mercadopago.android.px.internal.datasource.IdentificationService;
import com.mercadopago.android.px.internal.datasource.InstructionsService;
import com.mercadopago.android.px.internal.datasource.IssuersServiceImp;
import com.mercadopago.android.px.internal.datasource.PaymentMethodsService;
import com.mercadopago.android.px.internal.datasource.PaymentService;
import com.mercadopago.android.px.internal.datasource.PluginService;
import com.mercadopago.android.px.internal.datasource.SummaryAmountService;
import com.mercadopago.android.px.internal.datasource.TokenizeService;
import com.mercadopago.android.px.internal.datasource.cache.GroupsCache;
import com.mercadopago.android.px.internal.datasource.cache.GroupsCacheCoordinator;
import com.mercadopago.android.px.internal.datasource.cache.GroupsDiskCache;
import com.mercadopago.android.px.internal.datasource.cache.GroupsMemCache;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.BankDealsRepository;
import com.mercadopago.android.px.internal.repository.CardTokenRepository;
import com.mercadopago.android.px.internal.repository.CheckoutPreferenceRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.IdentificationRepository;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.IssuersRepository;
import com.mercadopago.android.px.internal.repository.PaymentMethodsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.SummaryAmountRepository;
import com.mercadopago.android.px.internal.repository.TokenRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.services.BankDealService;
import com.mercadopago.android.px.internal.services.CheckoutService;
import com.mercadopago.android.px.internal.services.GatewayService;
import com.mercadopago.android.px.internal.services.InstallmentService;
import com.mercadopago.android.px.internal.services.InstructionsClient;
import com.mercadopago.android.px.internal.services.PreferenceService;
import com.mercadopago.android.px.internal.util.LocaleUtil;
import com.mercadopago.android.px.internal.util.RetrofitUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.viewmodel.mappers.BusinessModelMapper;
import com.mercadopago.android.px.model.Device;
import com.mercadopago.android.px.services.MercadoPagoServices;
import com.mercadopago.android.px.tracking.internal.MPTracker;

public final class Session extends ApplicationModule implements AmountComponent {

    /**
     * This singleton instance is safe because session will work with application applicationContext. Application
     * applicationContext it's never leaking.
     */
    @SuppressLint("StaticFieldLeak")
    private static Session instance;

    // mem cache - lazy init.
    private ConfigurationModule configurationModule;
    private DiscountRepository discountRepository;
    private AmountRepository amountRepository;
    private GroupsRepository groupsRepository;
    private PaymentRepository paymentRepository;
    private AmountConfigurationRepository amountConfigurationRepository;
    private GroupsCache groupsCache;
    private PluginService pluginRepository;
    private InternalConfiguration internalConfiguration;
    private InstructionsService instructionsRepository;
    private SummaryAmountRepository summaryAmountRepository;
    private IssuersRepository issuersRepository;
    private CardTokenRepository cardTokenRepository;
    private BankDealsRepository bankDealsRepository;
    private IdentificationRepository identificationRepository;
    private CheckoutPreferenceRepository checkoutPreferenceRepository;
    private PaymentMethodsRepository paymentMethodsRepository;

    private Session(@NonNull final Context context) {
        super(context);
    }

    public static Session getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                "Session is not initialized. Make sure to call Session.initialize(Context) first.");
        }
        return instance;
    }

    public static Session initialize(@NonNull final Context context) {
        // In shared processes' content providers getApplicationContext() can return null.
        instance = new Session(context.getApplicationContext() == null ? context : context.getApplicationContext());
        return instance;
    }

    /**
     * Initialize Session with MercadoPagoCheckout information.
     *
     * @param mercadoPagoCheckout non mutable checkout intent.
     */
    public void init(@NonNull final MercadoPagoCheckout mercadoPagoCheckout) {
        //TODO add session mapping object.
        // delete old data.
        clear();

        //Favoring product id in discount params because that one is surely custom if exists
        final String deprecatedProductId =
            mercadoPagoCheckout.getAdvancedConfiguration().getDiscountParamsConfiguration().getProductId();
        final String productId = TextUtil.isNotEmpty(deprecatedProductId) ? deprecatedProductId
            : mercadoPagoCheckout.getAdvancedConfiguration().getProductId();
        final SessionIdProvider sessionIdProvider =
            newSessionProvider(mercadoPagoCheckout.getTrackingConfiguration().getSessionId());
        MPTracker.getInstance().setSessionId(sessionIdProvider.getSessionId());
        MPTracker.getInstance().setSecurityEnabled(PXApplicationBehaviourProvider.getSecurityBehaviour()
            .isSecurityEnabled(new SecurityValidationData.Builder().setFlowId(productId).build()));
        newProductIdProvider(productId);

        // Store persistent paymentSetting
        final ConfigurationModule configurationModule = getConfigurationModule();

        final PaymentConfiguration paymentConfiguration = mercadoPagoCheckout.getPaymentConfiguration();
        final PaymentSettingRepository paymentSetting = configurationModule.getPaymentSettings();
        paymentSetting.configure(mercadoPagoCheckout.getPublicKey());
        paymentSetting.configure(mercadoPagoCheckout.getAdvancedConfiguration());
        paymentSetting.configurePrivateKey(mercadoPagoCheckout.getPrivateKey());
        paymentSetting.configure(paymentConfiguration);
        resolvePreference(mercadoPagoCheckout, paymentSetting);
        // end Store persistent paymentSetting
    }

    private void resolvePreference(@NonNull final MercadoPagoCheckout mercadoPagoCheckout,
        final PaymentSettingRepository paymentSetting) {
        final String preferenceId = mercadoPagoCheckout.getPreferenceId();

        if (TextUtil.isEmpty(preferenceId)) {
            paymentSetting.configure(mercadoPagoCheckout.getCheckoutPreference());
        } else {
            //Pref cerrada.
            paymentSetting.configurePreferenceId(preferenceId);
        }
    }

    private void clear() {
        getConfigurationModule().reset();
        getGroupsCache().evict();
        configurationModule = null;
        discountRepository = null;
        amountRepository = null;
        groupsRepository = null;
        paymentRepository = null;
        groupsCache = null;
        pluginRepository = null;
        internalConfiguration = null;
        instructionsRepository = null;
        summaryAmountRepository = null;
        amountConfigurationRepository = null;
        issuersRepository = null;
        cardTokenRepository = null;
        checkoutPreferenceRepository = null;
        paymentMethodsRepository = null;
    }

    public GroupsRepository getGroupsRepository() {
        if (groupsRepository == null) {
            final PaymentSettingRepository paymentSettings = getConfigurationModule().getPaymentSettings();
            groupsRepository = new GroupsService(paymentSettings, getMercadoPagoESC(),
                RetrofitUtil.getRetrofitClient(getApplicationContext()).create(CheckoutService.class),
                LocaleUtil.getLanguage(getApplicationContext()), getProductIdProvider(),
                getGroupsCache());
        }
        return groupsRepository;
    }

    public SummaryAmountRepository getSummaryAmountRepository() {
        if (summaryAmountRepository == null) {
            final PaymentSettingRepository paymentSettings = getConfigurationModule().getPaymentSettings();
            final AdvancedConfiguration advancedConfiguration = paymentSettings.getAdvancedConfiguration();
            final UserSelectionRepository userSelectionRepository =
                getConfigurationModule().getUserSelectionRepository();
            final InstallmentService paymentService =
                RetrofitUtil.getRetrofitClient(getApplicationContext()).create(InstallmentService.class);

            summaryAmountRepository = new SummaryAmountService(paymentService, paymentSettings,
                advancedConfiguration, userSelectionRepository, getProductIdProvider());
        }
        return summaryAmountRepository;
    }

    @NonNull
    public ESCManagerBehaviour getMercadoPagoESC() {
        final PaymentSettingRepository paymentSettings = getConfigurationModule().getPaymentSettings();
        return ESCManagerBehaviourProvider
            .get(getSessionIdProvider().getSessionId(), paymentSettings.getAdvancedConfiguration().isEscEnabled());
    }

    @NonNull
    private Device getDevice() {
        return new Device(getApplicationContext());
    }

    @NonNull
    public MercadoPagoServices getMercadoPagoServices() {
        final PaymentSettingRepository paymentSettings = getConfigurationModule().getPaymentSettings();
        return new MercadoPagoServices(getApplicationContext(), paymentSettings.getPublicKey(),
            paymentSettings.getPrivateKey());
    }

    @Override
    public AmountRepository getAmountRepository() {
        if (amountRepository == null) {
            final ConfigurationModule configurationModule = getConfigurationModule();
            final PaymentSettingRepository configuration = configurationModule.getPaymentSettings();
            amountRepository = new AmountService(configuration,
                configurationModule.getChargeSolver(),
                getDiscountRepository());
        }
        return amountRepository;
    }

    @NonNull
    public DiscountRepository getDiscountRepository() {
        if (discountRepository == null) {
            discountRepository =
                new DiscountServiceImp(getGroupsRepository(), getConfigurationModule().getUserSelectionRepository());
        }
        return discountRepository;
    }

    @NonNull
    public AmountConfigurationRepository getAmountConfigurationRepository() {
        if (amountConfigurationRepository == null) {
            amountConfigurationRepository =
                new AmountConfigurationRepositoryImpl(getGroupsRepository(),
                    getConfigurationModule().getUserSelectionRepository());
        }
        return amountConfigurationRepository;
    }

    @StringRes
    public int getMainVerb() {
        return getConfigurationModule().getPaymentSettings().getAdvancedConfiguration()
            .getCustomStringConfiguration().getMainVerbStringResourceId();
    }

    @NonNull
    public ConfigurationModule getConfigurationModule() {
        if (configurationModule == null) {
            configurationModule = new ConfigurationModule(getApplicationContext());
        }
        return configurationModule;
    }

    @NonNull
    private GroupsCache getGroupsCache() {
        if (groupsCache == null) {
            groupsCache =
                new GroupsCacheCoordinator(new GroupsDiskCache(getFileManager(), getJsonUtil(), getCacheDir()),
                    new GroupsMemCache());
        }
        return groupsCache;
    }

    @NonNull
    public PluginRepository getPluginRepository() {
        if (pluginRepository == null) {
            pluginRepository =
                new PluginService(getApplicationContext(), getConfigurationModule().getPaymentSettings());
        }
        return pluginRepository;
    }

    @NonNull
    public PaymentRepository getPaymentRepository() {
        if (paymentRepository == null) {
            final ConfigurationModule configurationModule = getConfigurationModule();
            final SplitPaymentProcessor paymentProcessor =
                getConfigurationModule().getPaymentSettings().getPaymentConfiguration().getPaymentProcessor();
            paymentRepository = new PaymentService(configurationModule.getUserSelectionRepository(),
                configurationModule.getPaymentSettings(),
                configurationModule.getDisabledPaymentMethodRepository(),
                getPluginRepository(),
                getDiscountRepository(), getAmountRepository(),
                paymentProcessor,
                getApplicationContext(),
                new EscPaymentManagerImp(getMercadoPagoESC()),
                getTokenRepository(),
                getInstructionsRepository(),
                getGroupsRepository(),
                getAmountConfigurationRepository());
        }

        return paymentRepository;
    }

    @NonNull
    private TokenRepository getTokenRepository() {
        return new TokenizeService(getRetrofitClient().create(GatewayService.class),
            getConfigurationModule().getPaymentSettings(),
            getMercadoPagoESC(), getDevice());
    }

    @NonNull
    public InternalConfiguration getInternalConfiguration() {
        return internalConfiguration == null ? new InternalConfiguration(false) : internalConfiguration;
    }

    /**
     * Set internal configuration after building MercadoPagoCheckout.
     *
     * @param internalConfiguration internal configuration for checkout.
     */
    @SuppressWarnings("unused")
    public void setInternalConfiguration(@NonNull final InternalConfiguration internalConfiguration) {
        this.internalConfiguration = internalConfiguration;
    }

    //TODO move.
    @NonNull
    public BusinessModelMapper getBusinessModelMapper() {
        return new BusinessModelMapper(getConfigurationModule().getPaymentSettings(), getPaymentRepository());
    }

    @NonNull
    public InstructionsRepository getInstructionsRepository() {
        if (instructionsRepository == null) {
            instructionsRepository =
                new InstructionsService(getConfigurationModule().getPaymentSettings(),
                    getRetrofitClient().create(InstructionsClient.class),
                    LocaleUtil.getLanguage(getApplicationContext()));
        }
        return instructionsRepository;
    }

    public IssuersRepository getIssuersRepository() {
        if (issuersRepository == null) {
            final com.mercadopago.android.px.internal.services.IssuersService issuersService =
                RetrofitUtil.getRetrofitClient(getApplicationContext()).create(
                    com.mercadopago.android.px.internal.services.IssuersService.class);

            issuersRepository = new IssuersServiceImp(issuersService, getConfigurationModule().getPaymentSettings(),
                getConfigurationModule().getUserSelectionRepository());
        }
        return issuersRepository;
    }

    public CardTokenRepository getCardTokenRepository() {
        if (cardTokenRepository == null) {
            final GatewayService gatewayService =
                RetrofitUtil.getRetrofitClient(getApplicationContext()).create(GatewayService.class);
            cardTokenRepository =
                new CardTokenService(gatewayService, getConfigurationModule().getPaymentSettings(),
                    new Device(getApplicationContext()),
                    getMercadoPagoESC());
        }
        return cardTokenRepository;
    }

    public BankDealsRepository getBankDealsRepository() {
        if (bankDealsRepository == null) {
            final BankDealService bankDealsService =
                RetrofitUtil.getRetrofitClient(getApplicationContext())
                    .create(BankDealService.class);
            bankDealsRepository =
                new BankDealsService(bankDealsService, getApplicationContext(),
                    getConfigurationModule().getPaymentSettings());
        }
        return bankDealsRepository;
    }

    public IdentificationRepository getIdentificationRepository() {
        if (identificationRepository == null) {
            final com.mercadopago.android.px.internal.services.IdentificationService identificationService =
                RetrofitUtil.getRetrofitClient(getApplicationContext())
                    .create(com.mercadopago.android.px.internal.services.IdentificationService.class);
            identificationRepository =
                new IdentificationService(identificationService, getConfigurationModule().getPaymentSettings());
        }
        return identificationRepository;
    }

    public CheckoutPreferenceRepository getCheckoutPreferenceRepository() {
        if (checkoutPreferenceRepository == null) {
            final PreferenceService preferenceService =
                RetrofitUtil.getRetrofitClient(getApplicationContext()).create(PreferenceService.class);
            checkoutPreferenceRepository =
                new CheckoutPreferenceService(preferenceService, getConfigurationModule().getPaymentSettings());
        }
        return checkoutPreferenceRepository;
    }

    public PaymentMethodsRepository getPaymentMethodsRepository() {
        if (paymentMethodsRepository == null) {
            final CheckoutService checkoutService =
                RetrofitUtil.getRetrofitClient(getApplicationContext()).create(CheckoutService.class);
            paymentMethodsRepository =
                new PaymentMethodsService(getConfigurationModule().getPaymentSettings(), checkoutService);
        }
        return paymentMethodsRepository;
    }
}
