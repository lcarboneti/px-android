package com.mercadopago.android.px.internal.features.express.slider;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.CompoundButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.util.textformatter.AmountLabeledFormatter;
import com.mercadopago.android.px.internal.util.textformatter.TextFormatter;
import com.mercadopago.android.px.internal.view.LabeledSwitch;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.model.Split;
import java.util.List;

public class SplitPaymentHeaderAdapter extends ViewAdapter<List<SplitPaymentHeaderAdapter.Model>, LabeledSwitch>
    implements CompoundButton.OnCheckedChangeListener {

    @NonNull private final SplitListener splitListener;

    public interface SplitListener {
        void onSplitChanged(final boolean isChecked);
    }

    public abstract static class Model {
        public abstract void visit(final LabeledSwitch labeledSwitch);

        public abstract void visit(final boolean isChecked);
    }

    public static final class Empty extends Model {
        @Override
        public void visit(final LabeledSwitch labeledSwitch) {
            labeledSwitch.setVisibility(View.GONE);
        }

        @Override
        public void visit(final boolean isChecked) {
            // do nothing
        }
    }

    public static final class SplitModel extends Model {

        private final String currencyId;
        @NonNull private final Split split;
        private boolean isChecked;

        public SplitModel(@NonNull final String currencyId, @NonNull final Split split) {
            this.currencyId = currencyId;
            this.split = split;
            isChecked = split.defaultEnabled;
        }

        @Override
        public void visit(final LabeledSwitch labeledSwitch) {

            // ${amount} semibold, color black
            final Spannable amount =
                new AmountLabeledFormatter(new SpannableStringBuilder(), labeledSwitch.getContext())
                    .withSemiBoldStyle()
                    .withTextColor(ContextCompat.getColor(labeledSwitch.getContext(), R.color.ui_meli_black))
                    .apply(TextFormatter
                        .withCurrencyId(currencyId)
                        .amount(split.secondaryPaymentMethod.getVisibleAmountToPay())
                        .normalDecimals()
                        .toSpannable());

            // create text message
            final SpannableStringBuilder message = new SpannableStringBuilder(TextUtil.SPACE)
                .append(split.secondaryPaymentMethod.message);

            // added color
            ViewUtils.setColorInSpannable(ContextCompat.getColor(labeledSwitch.getContext(),
                R.color.ui_meli_grey), 0, message.length(),
                message);
            // build definitive message
            labeledSwitch.setText(new SpannableStringBuilder(amount).append(message));
            labeledSwitch.setChecked(isChecked);
            labeledSwitch.setVisibility(View.VISIBLE);
        }

        @Override
        public void visit(final boolean isChecked) {
            this.isChecked = isChecked;
        }
    }

    public SplitPaymentHeaderAdapter(@NonNull final List<Model> data, @Nullable final LabeledSwitch view,
        @NonNull final SplitListener splitListener) {
        super(data, view);
        this.splitListener = splitListener;
        view.setOnCheckedChanged(this);
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected,
        @NonNull final SplitSelectionState splitSelectionState) {
        // Empty data case
        if (currentIndex >= data.size()) {
            new Empty().visit(view);
            return;
        }

        final Model model = data.get(currentIndex);
        if (!splitSelectionState.preferDefault()) {
            model.visit(splitSelectionState.userWantsToSplit());
        }
        model.visit(view);
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        splitListener.onSplitChanged(isChecked);
    }
}
