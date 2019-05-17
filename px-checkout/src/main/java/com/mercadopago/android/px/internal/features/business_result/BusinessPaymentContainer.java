package com.mercadopago.android.px.internal.features.business_result;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.features.paymentresult.components.Header;
import com.mercadopago.android.px.internal.features.paymentresult.props.HeaderProps;
import com.mercadopago.android.px.internal.util.FragmentUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.ActionDispatcher;
import com.mercadopago.android.px.internal.view.Button;
import com.mercadopago.android.px.internal.view.CompactComponent;
import com.mercadopago.android.px.internal.view.Footer;
import com.mercadopago.android.px.internal.view.HelpComponent;
import com.mercadopago.android.px.internal.view.PaymentMethodBodyComponent;
import com.mercadopago.android.px.internal.view.Receipt;
import com.mercadopago.android.px.internal.view.RendererFactory;
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel;
import com.mercadopago.android.px.model.ExitAction;
import javax.annotation.Nonnull;

public class BusinessPaymentContainer extends CompactComponent<BusinessPaymentModel, ActionDispatcher> {

    public BusinessPaymentContainer(final BusinessPaymentModel businessPaymentModel, final ActionDispatcher callBack) {
        super(businessPaymentModel, callBack);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final Context context = parent.getContext();
        final LinearLayout mainContentContainer = ViewUtils.createLinearContainer(context);
        final LinearLayout headerContainer = ViewUtils.createLinearContainer(context);
        final ScrollView scrollView = ViewUtils.createScrollContainer(context);
        scrollView.addView(mainContentContainer);
        mainContentContainer.addView(headerContainer);

        final View header = renderHeader(headerContainer);

        final ViewTreeObserver vto = scrollView.getViewTreeObserver();

        if (props.payment.hasReceipt()) {
            RendererFactory
                .create(context, new Receipt(new Receipt.ReceiptProps(props.payment.getReceipt())))
                .render(headerContainer);
        }

        if (props.payment.hasHelp()) {
            final View helpView = new HelpComponent(props.payment.getHelp()).render(mainContentContainer);
            mainContentContainer.addView(helpView);
        }

        if (props.payment.hasTopFragment()) {
            FragmentUtil.addFragmentInside(mainContentContainer,
                R.id.px_fragment_container_top,
                props.payment.getTopFragment());
        }

        if (props.payment.shouldShowPaymentMethod()) {
            final PaymentMethodBodyComponent paymentMethodBodyComponent =
                new PaymentMethodBodyComponent(PaymentMethodBodyComponent.PaymentMethodBodyProp
                    .with(props.getPaymentMethodProps()));
            final View pmBody = paymentMethodBodyComponent.render(mainContentContainer);
            mainContentContainer.addView(pmBody);
            ViewUtils.stretchHeight(pmBody);
        }

        if (props.payment.hasBottomFragment()) {
            FragmentUtil.addFragmentInside(mainContentContainer,
                R.id.px_fragment_container_bottom,
                props.payment.getBottomFragment());
        }

        if (mainContentContainer.getChildCount() == 1) { //has only header
            vto.addOnGlobalLayoutListener(noBodyCorrection(mainContentContainer, scrollView, header));
        } else if (mainContentContainer.getChildCount() > 1) { // has more elements
            vto.addOnGlobalLayoutListener(
                bodyCorrection(mainContentContainer, scrollView, mainContentContainer.getChildAt(1)));
        }

        renderFooter(mainContentContainer);

        return scrollView;
    }

    private ViewTreeObserver.OnGlobalLayoutListener bodyCorrection(final LinearLayout mainContentContainer,
        final ScrollView scrollView,
        final View toCorrect) {
        return () -> {
            final int diffHeight = calculateDiff(mainContentContainer, scrollView);
            if (diffHeight > 0) {
                toCorrect.setPadding(toCorrect.getPaddingLeft(),
                    toCorrect.getPaddingTop() + (int) Math.ceil(diffHeight / 2f), toCorrect.getPaddingRight(),
                    toCorrect.getPaddingBottom() + (int) Math.ceil(diffHeight / 2f));
            }
        };
    }

    private void renderFooter(@NonNull final LinearLayout linearLayout) {
        final Button.Props primaryButtonProps = getButtonProps(props.payment.getPrimaryAction());
        final Button.Props secondaryButtonProps = getButtonProps(props.payment.getSecondaryAction());
        final Footer footer =
            new Footer(new Footer.Props(primaryButtonProps, secondaryButtonProps), getActions());
        final View footerView = footer.render(linearLayout);
        linearLayout.addView(footerView);
    }

    @Nullable
    private Button.Props getButtonProps(final ExitAction action) {
        if (action != null) {
            final String label = action.getName();
            return new Button.Props(label, action);
        }
        return null;
    }

    @NonNull
    private View renderHeader(@NonNull final LinearLayout linearLayout) {
        final Context context = linearLayout.getContext();
        final Header header = new Header(HeaderProps.from(props.payment, context), getActions());
        ViewUtils.addCancelToolbar(linearLayout, header.props.background);
        final View render = RendererFactory.create(context, header).render(linearLayout);
        return render.findViewById(R.id.headerContainer);
    }

    @NonNull
    private ViewTreeObserver.OnGlobalLayoutListener noBodyCorrection(@NonNull final LinearLayout mainContentContainer,
        @NonNull final ScrollView scrollView,
        @NonNull final View header) {
        return () -> {
            final int diffHeight = calculateDiff(mainContentContainer, scrollView);
            if (diffHeight > 0) {
                header
                    .setPadding(header.getPaddingLeft(), header.getPaddingTop() + (int) Math.ceil(diffHeight / 2f),
                        header.getPaddingRight(), (header.getPaddingBottom() + (int) Math.ceil(diffHeight / 2f)));
            }
        };
    }

    private int calculateDiff(final LinearLayout mainContentContainer, final ScrollView scrollView) {
        final int linearHeight = mainContentContainer.getMeasuredHeight();
        final int scrollHeight = scrollView.getMeasuredHeight();
        return scrollHeight - linearHeight;
    }
}