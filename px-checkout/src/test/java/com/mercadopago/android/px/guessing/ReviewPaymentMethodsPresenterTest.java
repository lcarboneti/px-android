package com.mercadopago.android.px.guessing;

import com.mercadopago.android.px.internal.features.ReviewPaymentMethodsPresenter;
import com.mercadopago.android.px.internal.features.ReviewPaymentMethodsView;
import com.mercadopago.android.px.internal.features.providers.ReviewPaymentMethodsProvider;
import com.mercadopago.android.px.mocks.InitStubUtils;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by vaserber on 8/24/17.
 */

public class ReviewPaymentMethodsPresenterTest {

    @Test
    public void testShowSupportedPaymentMethodsList() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        List<PaymentMethod> paymentMethodList = InitStubUtils.getPaymentMethodListMLA();

        ReviewPaymentMethodsPresenter presenter = new ReviewPaymentMethodsPresenter(paymentMethodList);
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);
        presenter.initialize();

        assertTrue(mockedView.initialized);
        assertEquals(mockedView.supportedPaymentMethodsCount, paymentMethodList.size());
    }

    private class MockedView implements ReviewPaymentMethodsView {

        private boolean initialized;
        private int supportedPaymentMethodsCount;

        @Override
        public void showError(final MercadoPagoError error, final String requestOrigin) {

        }

        @Override
        public void initializeSupportedPaymentMethods(List<PaymentMethod> supportedPaymentMethods) {
            initialized = true;
            supportedPaymentMethodsCount = supportedPaymentMethods.size();
        }
    }

    private class MockedProvider implements ReviewPaymentMethodsProvider {

        private static final String EMPTY_PAYMENT_METHOD_LIST = "empty payment methods";
        private static final String STANDARD_ERROR_MESSAGE = "oops something went wrong";

        @Override
        public String getEmptyPaymentMethodsListError() {
            return EMPTY_PAYMENT_METHOD_LIST;
        }

        @Override
        public String getStandardErrorMessage() {
            return STANDARD_ERROR_MESSAGE;
        }
    }
}
