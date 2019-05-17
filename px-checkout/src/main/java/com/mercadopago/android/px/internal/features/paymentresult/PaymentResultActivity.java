package com.mercadopago.android.px.internal.features.paymentresult;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import com.mercadolibre.android.ui.widgets.MeliSnackbar;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.PaymentResultScreenConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.internal.base.PXActivity;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationComment;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationCommentRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationTime;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationTimeRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.Body;
import com.mercadopago.android.px.internal.features.paymentresult.components.BodyError;
import com.mercadopago.android.px.internal.features.paymentresult.components.BodyErrorRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.BodyRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractionComponent;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractionComponentRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractions;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractionsRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionReferenceComponent;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionReferenceRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.Instructions;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsAction;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsActionRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsActions;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsActionsRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsContent;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsContentRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsInfo;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsInfoRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsReferences;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsReferencesRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSecondaryInfo;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSecondaryInfoRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSubtitle;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSubtitleRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsTertiaryInfo;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsTertiaryInfoRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.PaymentResultContainer;
import com.mercadopago.android.px.internal.features.paymentresult.props.HeaderProps;
import com.mercadopago.android.px.internal.features.paymentresult.props.PaymentResultProps;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.internal.view.ComponentManager;
import com.mercadopago.android.px.internal.view.LoadingComponent;
import com.mercadopago.android.px.internal.view.LoadingRenderer;
import com.mercadopago.android.px.internal.view.RendererFactory;
import com.mercadopago.android.px.internal.viewmodel.ChangePaymentMethodPostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.RecoverPaymentPostPaymentAction;
import com.mercadopago.android.px.model.Instruction;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_ACTION;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CUSTOM_EXIT;

public class PaymentResultActivity extends PXActivity<PaymentResultPresenter> implements
    PaymentResultContract.PaymentResultView {

    private static final String EXTRA_PAYMENT_RESULT = "extra_payment_result";
    public static final String EXTRA_RESULT_CODE = "extra_result_code";

    private PaymentResultProps props;
    private ComponentManager componentManager;

    public static Intent getIntent(@NonNull final Context context, @NonNull final PaymentResult result) {
        final Intent resultIntent = new Intent(context, PaymentResultActivity.class);
        //TODO remove
        resultIntent.putExtra(EXTRA_PAYMENT_RESULT, result);
        return resultIntent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RendererFactory.register(Body.class, BodyRenderer.class);
        RendererFactory.register(LoadingComponent.class, LoadingRenderer.class);
        RendererFactory.register(Instructions.class, InstructionsRenderer.class);
        RendererFactory.register(InstructionsSubtitle.class, InstructionsSubtitleRenderer.class);
        RendererFactory.register(InstructionsContent.class, InstructionsContentRenderer.class);
        RendererFactory.register(InstructionsInfo.class, InstructionsInfoRenderer.class);
        RendererFactory.register(InstructionsReferences.class, InstructionsReferencesRenderer.class);
        RendererFactory.register(InstructionReferenceComponent.class, InstructionReferenceRenderer.class);
        RendererFactory.register(InstructionInteractionComponent.class, InstructionInteractionComponentRenderer.class);
        RendererFactory.register(InstructionInteractions.class, InstructionInteractionsRenderer.class);
        RendererFactory.register(AccreditationTime.class, AccreditationTimeRenderer.class);
        RendererFactory.register(AccreditationComment.class, AccreditationCommentRenderer.class);
        RendererFactory.register(InstructionsSecondaryInfo.class, InstructionsSecondaryInfoRenderer.class);
        RendererFactory.register(InstructionsTertiaryInfo.class, InstructionsTertiaryInfoRenderer.class);
        RendererFactory.register(InstructionsActions.class, InstructionsActionsRenderer.class);
        RendererFactory.register(InstructionsAction.class, InstructionsActionRenderer.class);
        RendererFactory.register(BodyError.class, BodyErrorRenderer.class);

        final PaymentSettingRepository paymentSettings =
            Session.getSession(this).getConfigurationModule().getPaymentSettings();

        final PaymentResultScreenConfiguration paymentResultScreenConfiguration =
            paymentSettings.getAdvancedConfiguration().getPaymentResultScreenConfiguration();

        props = new PaymentResultProps.Builder(paymentResultScreenConfiguration).build();

        presenter = createPresenter(paymentSettings);

        componentManager = new ComponentManager(this);
        final Component root = new PaymentResultContainer(componentManager, props);
        componentManager.setComponent(root);
        componentManager.setActionsListener(presenter);

        presenter.attachView(this);

        if (savedInstanceState == null) {
            presenter.freshStart();
        }
    }

    private PaymentResultPresenter createPresenter(final PaymentSettingRepository paymentSettings) {
        final Intent intent = getIntent();
        final PaymentResult paymentResult = (PaymentResult) intent.getSerializableExtra(EXTRA_PAYMENT_RESULT);
        return new PaymentResultPresenter(paymentSettings, Session.getSession(this).getInstructionsRepository(),
            paymentResult);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public void showApiExceptionError(final ApiException exception, final String requestOrigin) {
        ErrorUtil.showApiExceptionError(this, exception, requestOrigin);
    }

    @Override
    public void showInstructionsError() {
        ErrorUtil.startErrorActivity(this, new MercadoPagoError(getString(R.string.px_standard_error_message), false));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Constants.Activities.CONGRATS_REQUEST_CODE) {
            finishWithOkResult(resultCode, data);
        } else if (requestCode == Constants.Activities.PENDING_REQUEST_CODE) {
            resolveRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.REJECTION_REQUEST_CODE) {
            resolveRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.CALL_FOR_AUTHORIZE_REQUEST_CODE) {
            resolveRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.INSTRUCTIONS_REQUEST_CODE) {
            finishWithOkResult(resultCode, data);
        } else {
            finishWithCancelResult(data);
        }
    }

    @Override
    public void onBackPressed() {
        presenter.onAbort();
        finishWithResult(MercadoPagoCheckout.PAYMENT_RESULT_CODE);
    }

    private void resolveRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_CANCELED && data != null) {
            finishWithCancelResult(data);
        } else {
            finishWithOkResult(resultCode, data);
        }
    }

    private void finishWithCancelResult(final Intent data) {
        setResult(RESULT_CANCELED, data);
        finish();
    }

    private void finishWithOkResult(final int resultCode, final Intent data) {
        setResult(resultCode, data);
        finish();
    }

    @Override
    public void openLink(final String url) {
        //TODO agregar try catch
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void finishWithResult(final int resultCode) {
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        setResult(RESULT_CUSTOM_EXIT, intent);
        finish();
    }

    @Override
    public void changePaymentMethod() {
        final Intent returnIntent = new Intent();
        new ChangePaymentMethodPostPaymentAction().addToIntent(returnIntent);
        setResult(RESULT_ACTION, returnIntent);
        finish();
    }

    @Override
    public void recoverPayment() {
        final Intent returnIntent = new Intent();
        new RecoverPaymentPostPaymentAction().addToIntent(returnIntent);
        setResult(RESULT_ACTION, returnIntent);
        finish();
    }

    @SuppressLint("Range")
    @Override
    public void copyToClipboard(@NonNull final String content) {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText("", content);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            MeliSnackbar.make(findViewById(R.id.mpsdkPaymentResultContainer),
                getString(R.string.px_copied_to_clipboard_ack),
                Snackbar.LENGTH_SHORT, MeliSnackbar.SnackbarType.SUCCESS).show();
        }
    }

    @Override
    public void setPropPaymentResult(@NonNull final String currencyId, @NonNull final PaymentResult paymentResult,
        final boolean showLoading) {

        props = props.toBuilder()
            .setPaymentResult(paymentResult)
            .setCurrencyId(currencyId)
            .setHeaderMode(HeaderProps.HEADER_MODE_WRAP)
            .setLoading(showLoading)
            .build();
    }

    @Override
    public void setPropInstruction(@NonNull final Instruction instruction,
        @NonNull final String processingModeString,
        final boolean showLoading) {

        props = props.toBuilder()
            .setInstruction(instruction)
            .setLoading(showLoading)
            .setProcessingMode(processingModeString)
            .build();
    }

    /**
     * @deprecated Este método tiene que ser privado, se hizo publico para poder hacer notificaciones condicionales.
     * Esta no es la forma adecuada de hacerlo hay que definir algún mecanismo de transacciones para eso. Mejor no tener
     * el feature a tener algo mal implementado.
     */
    @Override
    @Deprecated
    public void notifyPropsChanged() {
        if (componentManager != null && props != null) {
            componentManager.onProps(props);
        }
    }
}
