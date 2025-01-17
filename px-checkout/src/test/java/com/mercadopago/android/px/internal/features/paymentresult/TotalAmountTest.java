package com.mercadopago.android.px.internal.features.paymentresult;

import com.mercadopago.android.px.internal.util.CurrenciesUtil;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.TotalAmount;
import com.mercadopago.android.px.mocks.PayerCosts;
import com.mercadopago.android.px.model.PayerCost;
import java.math.BigDecimal;
import java.util.Locale;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TotalAmountTest {

    private static final String CURRENCY_ID = "ARS";
    private ActionDispatcher dispatcher;

    @Before
    public void setup() {
        dispatcher = mock(ActionDispatcher.class);
    }

    @Test
    public void getAmountTitleWhenComponentHasPayerCostWithInstallments() {
        final TotalAmount component = getTotalAmountComponent(PayerCosts.getPayerCost());

        String expected = String.format(Locale.getDefault(),
            "%dx %s",
            component.props.payerCost.getInstallments(),
            CurrenciesUtil.getLocalizedAmountWithoutZeroDecimals(CURRENCY_ID,
                component.props.payerCost.getInstallmentAmount()));

        Assert.assertEquals(expected, component.getAmountTitle());
    }

    @Test
    public void getAmountTitleWhenComponentHasPayerCostWithoutInstallments() {
        final PayerCost payerCost = PayerCosts.getPayerCostWithoutInstallments();
        final TotalAmount component = getTotalAmountComponent(payerCost);
        String expected = CurrenciesUtil.getLocalizedAmountWithoutZeroDecimals(CURRENCY_ID, component.props.amount);
        Assert.assertEquals(expected, component.getAmountTitle());
    }

    @Test
    public void getEmptyAmountTitleWhenComponentHasNotPayerCost() {
        final PayerCost payerCost = null;
        final TotalAmount component = getTotalAmountComponent(payerCost);
        Assert.assertEquals("", component.getAmountDetail());
    }

    @Test
    public void getAmountDetailWhenComponentHasPayerCost() {

        final PayerCost payerCost = PayerCosts.getPayerCost();

        final TotalAmount component = getTotalAmountComponent(payerCost);

        String expected = String.format(Locale.getDefault(),
            "(%s)",
            CurrenciesUtil
                .getLocalizedAmountWithoutZeroDecimals(CURRENCY_ID, component.props.payerCost.getTotalAmount()));

        Assert.assertEquals(expected, component.getAmountDetail());
    }

    @Test
    public void getEmptyAmountDetailWhenComponentHasNotPayerCost() {
        final PayerCost payerCost = null;

        final TotalAmount component = getTotalAmountComponent(payerCost);

        Assert.assertTrue(component.getAmountDetail().equals(""));
    }

    private TotalAmount getTotalAmountComponent(final PayerCost payerCost) {
        final TotalAmount.Props props =
            new TotalAmount.Props(CURRENCY_ID, new BigDecimal(1000), payerCost);
        return new TotalAmount(props);
    }
}
