package com.mercadopago.android.px.internal.features.express.slider;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.mercadopago.android.px.internal.viewmodel.RenderMode;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.internal.viewmodel.drawables.PaymentMethodFragmentDrawer;
import java.util.List;

public class PaymentMethodFragmentAdapter extends FragmentStatePagerAdapter {

    @NonNull private final List<DrawableFragmentItem> items;
    @NonNull private PaymentMethodFragmentDrawer drawer;

    public PaymentMethodFragmentAdapter(@NonNull final FragmentManager fm,
        @NonNull final List<DrawableFragmentItem> drawableFragmentItems) {
        super(fm);
        items = drawableFragmentItems;
        drawer = new PaymentMethodHighResDrawer();
    }

    @Override
    public Fragment getItem(final int position) {
        return items.get(position).draw(drawer);
    }

    @Override
    public int getItemPosition(@NonNull final Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void setRenderMode(@NonNull @RenderMode final String renderMode) {
        if (renderMode.equals(RenderMode.LOW_RES)) {
            drawer = new PaymentMethodLowResDrawer();
            notifyDataSetChanged();
        }
    }
}