package com.mercadopago.android.px.internal.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.viewmodel.PaymentMethodViewModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaymentMethodSearchItemAdapter extends RecyclerView.Adapter<PaymentMethodSearchItemAdapter.ViewHolder> {

    private final List<PaymentMethodViewModel> items;

    public PaymentMethodSearchItemAdapter() {
        items = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
        return new ViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.px_row_pm_search_item, parent, false));
    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.populate(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull final Collection<PaymentMethodViewModel> searchItems) {
        items.clear();
        items.addAll(searchItems);
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView description;
        private final TextView comment;
        private final TextView discountInfo;
        private final ImageView icon;
        private final ImageView badge;

        public ViewHolder(final View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.mpsdkDescription);
            comment = itemView.findViewById(R.id.mpsdkComment);
            discountInfo = itemView.findViewById(R.id.mpsdkDiscountInfo);
            icon = itemView.findViewById(R.id.mpsdkImage);
            badge = itemView.findViewById(R.id.mpsdkIconBadge);
        }

        void populate(@NonNull final PaymentMethodViewModel model) {
            final Context context = itemView.getContext();
            ViewUtils.loadOrGone(model.getDescription(), description);
            ViewUtils.loadOrGone(model.getComment(), comment);
            ViewUtils.loadOrGone(model.getIconResourceId(context), icon);
            ViewUtils.loadOrGone(model.getDiscountInfo(), discountInfo);
            ViewUtils.loadOrGone(model.getBadgeResourceId(context), badge);
            model.tint(icon);
            itemView.setOnClickListener(v -> model.handleOnClick());
        }
    }
}