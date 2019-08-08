package com.mercadopago.android.px.internal.features.express;

import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.CustomStringConfiguration;
import com.mercadopago.android.px.configuration.DynamicDialogConfiguration;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.datasource.IESCManager;
import com.mercadopago.android.px.internal.features.express.slider.HubAdapter;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpressPaymentPresenterTest {

    @Mock
    private ExpressPayment.View view;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentSettingRepository configuration;

    @Mock
    private DisabledPaymentMethodRepository disabledPaymentMethodRepository;

    @Mock
    private GroupsRepository groupsRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private AmountConfigurationRepository amountConfigurationRepository;

    @Mock
    private AmountRepository amountRepository;

    @Mock
    private PaymentMethodSearch paymentMethodSearch;

    @Mock
    private ExpressMetadata expressMetadata;

    @Mock
    private CardMetadata cardMetadata;

    @Mock
    private AmountConfiguration amountConfiguration;

    @Mock
    private DiscountConfigurationModel discountConfigurationModel;

    @Mock
    private AdvancedConfiguration advancedConfiguration;

    @Mock
    private DynamicDialogConfiguration dynamicDialogConfiguration;

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private IESCManager escManager;

    private ExpressPaymentPresenter expressPaymentPresenter;

    @Before
    public void setUp() {
        //This is needed for the presenter constructor
        final CheckoutPreference preference = mock(CheckoutPreference.class);
        when(preference.getSite()).thenReturn(Sites.ARGENTINA);
        when(preference.getItems()).thenReturn(Collections.singletonList(mock(Item.class)));
        when(configuration.getCheckoutPreference()).thenReturn(preference);
        when(configuration.getAdvancedConfiguration()).thenReturn(advancedConfiguration);
        when(advancedConfiguration.getDynamicDialogConfiguration()).thenReturn(dynamicDialogConfiguration);
        when(advancedConfiguration.getCustomStringConfiguration()).thenReturn(mock(CustomStringConfiguration.class));
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(paymentMethodSearch.getExpress()).thenReturn(Collections.singletonList(expressMetadata));
        when(expressMetadata.getCard()).thenReturn(cardMetadata);
        when(expressMetadata.isCard()).thenReturn(true);
        when(cardMetadata.getId()).thenReturn("123");
        when(discountRepository.getConfigurationFor("123")).thenReturn(discountConfigurationModel);
        when(discountRepository.getConfigurationFor(TextUtil.EMPTY)).thenReturn(discountConfigurationModel);
        when(amountConfigurationRepository.getConfigurationFor("123")).thenReturn(amountConfiguration);

        expressPaymentPresenter =
            new ExpressPaymentPresenter(paymentRepository, configuration, disabledPaymentMethodRepository,
                discountRepository,
                amountRepository, groupsRepository, amountConfigurationRepository, chargeRepository,
                escManager);

        verifyAttachView();
    }

    @Test
    public void whenCanceledThenCancelAndTrack() {
        expressPaymentPresenter.cancel();

        verify(view).cancel();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenViewIsResumedThenPaymentRepositoryIsAttached() {
        verifyOnViewResumed();
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whenViewIsPausedThenPaymentRepositoryIsDetached() {
        verifyOnViewResumed();
        expressPaymentPresenter.onViewPaused();
        verify(paymentRepository).detach(expressPaymentPresenter);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whenElementDescriptorViewClickedAndHasCreatorThenShowDynamicDialog() {
        final DynamicDialogCreator dynamicDialogCreatorMock = mock(DynamicDialogCreator.class);
        when(dynamicDialogConfiguration.hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER))
            .thenReturn(true);
        when(dynamicDialogConfiguration.getCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER))
            .thenReturn(dynamicDialogCreatorMock);

        expressPaymentPresenter.onHeaderClicked();
        verify(dynamicDialogConfiguration)
            .hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER);
        verify(dynamicDialogConfiguration)
            .getCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER);
        verify(view).showDynamicDialog(eq(dynamicDialogCreatorMock),
            any(DynamicDialogCreator.CheckoutData.class));

        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whenElementDescriptorViewClickedAndHasNotCreatorThenDoNotShowDynamicDialog() {
        expressPaymentPresenter.onHeaderClicked();
        verify(dynamicDialogConfiguration)
            .hasCreatorFor(DynamicDialogConfiguration.DialogLocation.TAP_ONE_TAP_HEADER);

        verifyNoMoreInteractions(view);
        verifyNoMoreInteractions(dynamicDialogConfiguration);
    }

    @Test
    public void whenSliderOptionSelectedThenShowInstallmentsRow() {
        final int currentElementPosition = 1;

        expressPaymentPresenter.onSliderOptionSelected(currentElementPosition);

        verify(view).updateViewForPosition(eq(currentElementPosition), eq(PayerCost.NO_SELECTED), any());
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPayerCostSelectedThenItsReflectedOnView() {
        final int paymentMethodIndex = 0;
        final int selectedPayerCostIndex = 1;
        final PayerCost firstPayerCost = mock(PayerCost.class);
        final List<PayerCost> payerCostList =
            Arrays.asList(mock(PayerCost.class), firstPayerCost, mock(PayerCost.class));
        when(amountConfiguration.getAppliedPayerCost(false)).thenReturn(payerCostList);
        expressPaymentPresenter.onPayerCostSelected(payerCostList.get(selectedPayerCostIndex));

        verify(view).updateViewForPosition(eq(paymentMethodIndex), eq(selectedPayerCostIndex), any());
        verify(view).collapseInstallmentsSelection();
        verifyNoMoreInteractions(view);
    }

    private void verifyAttachView() {
        expressPaymentPresenter.attachView(view);
        expressPaymentPresenter.loadViewModel();

        verify(view).showToolbarElementDescriptor(any(ElementDescriptorView.Model.class));
        verify(view).configureAdapters(anyListOf(DrawableFragmentItem.class), any(Site.class),
            any(HubAdapter.Model.class));
        verify(view).setPayButtonText(any(PayButtonViewModel.class));
    }

    private void verifyOnViewResumed() {
        expressPaymentPresenter.onViewResumed();

        verify(paymentRepository).attach(expressPaymentPresenter);
        verify(view).updateViewForPosition(eq(0), eq(PayerCost.NO_SELECTED), any(SplitSelectionState.class));
    }
}