package com.mercadopago.android.px.issuers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.features.IssuersActivityView;
import com.mercadopago.android.px.internal.features.IssuersPresenter;
import com.mercadopago.android.px.internal.repository.IssuersRepository;
import com.mercadopago.android.px.mocks.InitStubUtils;
import com.mercadopago.android.px.mocks.Issuers;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IssuersPresenterTest {

    @Mock
    private IssuersRepository issuersRepository;
    @Mock
    private IssuersActivityView mockedView;

    private IssuersPresenter presenter;

    @Before
    public void setUp() {
        presenter = getPresenter();
    }

    @NonNull
    private IssuersPresenter getBasePresenter(
        final IssuersActivityView view, final boolean comesFromStorageFlow) {
        final PaymentMethod paymentMethod = InitStubUtils.getPaymentMethodOnVisa();
        IssuersPresenter presenter = new IssuersPresenter(issuersRepository, paymentMethod, comesFromStorageFlow);
        presenter.attachView(view);
        return presenter;
    }

    @NonNull
    private IssuersPresenter getPresenter() {
        return getBasePresenter(mockedView, false);
    }

    @NonNull
    private IssuersPresenter getPresenterFromStorageFlow() {
        return getBasePresenter(mockedView, true);
    }

    @Test
    public void whenIssuersNotSetAndGetThemWithSuccessThenResolveThem() {
        final List<Issuer> stubIssuers = Issuers.getIssuersListMLA();
        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubSuccessMpCall<>(stubIssuers));

        presenter.initialize();

        verifyGettingListOfIssuersWithSuccess();
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenIssuersWereSetThenResolveThem() {
        final List<Issuer> stubIssuers = Issuers.getIssuersListMLA();
        presenter.setIssuers(stubIssuers);

        presenter.initialize();

        verify(mockedView).showHeader();
        verify(mockedView).showIssuers(any(List.class), any(OnSelectedCallback.class));
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenIssuersNotSetAndGetThemWithFailureThenShowError() {
        final ApiException apiException = mock(ApiException.class);

        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubFailMpCall<List<Issuer>>(apiException));

        presenter.initialize();

        verifyGettingIssuersWithFailure();
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenIssuersNotSetAndGetAnEmptyListWithSuccessThenResolveThem() {
        final List<Issuer> stubIssuers = new ArrayList<>();
        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubSuccessMpCall<>(stubIssuers));

        presenter.initialize();

        verifyLoading();
        verify(mockedView).showEmptyIssuersError(anyString());
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenIssuersNotSetAndGetOneIssuerWithSuccessThenResolveThem() {
        final List<Issuer> stubIssuers = Issuers.getOneIssuerListMLA();
        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubSuccessMpCall<>(stubIssuers));

        presenter.initialize();

        verifyLoading();
        verify(mockedView).finishWithResult(stubIssuers.get(0));
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenIssuersNotSetAndGetNullThenResolveThemAsAnEmptyList() {
        final List<Issuer> stubIssuers = null;
        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubSuccessMpCall<>(stubIssuers));

        presenter.initialize();

        verifyLoading();
        verify(mockedView).showEmptyIssuersError(anyString());
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenItemSelectedAndDoesNotComeFromStorageFlowThenFinishWithResult() {
        final List<Issuer> stubIssuers = Issuers.getIssuersListMLA();
        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubSuccessMpCall<>(stubIssuers));

        presenter.initialize();

        verifyGettingListOfIssuersWithSuccess();

        presenter.onItemSelected(0);

        verify(mockedView).finishWithResult(stubIssuers.get(0));
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenItemSelectedAndComesFromStorageFlowThen() {
        final IssuersPresenter presenter = getPresenterFromStorageFlow();
        final List<Issuer> stubIssuers = Issuers.getIssuersListMLA();
        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubSuccessMpCall<>(stubIssuers));

        presenter.initialize();

        verifyGettingListOfIssuersWithSuccess();

        presenter.onItemSelected(0);

        verify(mockedView).finishWithResultForCardStorage(stubIssuers.get(0).getId());
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenGetIssuersWithFailureAndRecoverThenResolveThem() {
        final ApiException apiException = mock(ApiException.class);
        final List<Issuer> stubIssuers = Issuers.getIssuersListMLA();

        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubFailMpCall<List<Issuer>>(apiException));

        presenter.initialize();

        verifyGettingIssuersWithFailure();

        when(issuersRepository.getIssuers(presenter.getPaymentMethod().getId(), presenter.getBin()))
            .thenReturn(new StubSuccessMpCall<>(stubIssuers));

        presenter.recoverFromFailure();

        verifyGettingListOfIssuersWithSuccess();
        verifyNoMoreInteractions(mockedView);
    }

    @Test
    public void whenSetCardInfoThenSetBin() {
        final CardInfo cardInfo = mock(CardInfo.class);
        final String stubFirstSixDigits = "123456";

        when(cardInfo.getFirstSixDigits()).thenReturn(stubFirstSixDigits);

        presenter.setCardInfo(cardInfo);

        assertEquals(presenter.getBin(), stubFirstSixDigits);
    }

    private void verifyGettingListOfIssuersWithSuccess() {
        verifyLoading();
        verify(mockedView).showHeader();
        verify(mockedView).showIssuers(any(List.class), any(OnSelectedCallback.class));
    }

    private void verifyGettingIssuersWithFailure() {
        verifyLoading();
        verify(mockedView).showError(any(MercadoPagoError.class), anyString());
    }

    private void verifyLoading() {
        verify(mockedView, atLeastOnce()).showLoadingView();
        verify(mockedView, atLeastOnce()).stopLoadingView();
    }
}
