package com.mercadopago.android.px.internal.features.cardvault;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import java.util.List;

public interface CardVaultView extends MvpView {

    void finishWithResult();

    void showApiExceptionError(final ApiException exception, final String requestOrigin);

    void showError(final MercadoPagoError mercadoPagoError, final String requestOrigin);

    void showEmptyPayerCostScreen();

    void askForInstallments();

    void startIssuersActivity(@NonNull final List<Issuer> issuers);

    void startSecurityCodeActivity(final Reason reason);

    void showProgressLayout();

    void askForCardInformation();

    void askForSecurityCodeFromTokenRecovery(final Reason recoveryReason);

    void cancelCardVault();

    void animateTransitionSlideInSlideOut();

    void finishOnErrorResult();
}
