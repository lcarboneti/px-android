package com.mercadopago.android.px.internal.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class DynamicHeightViewPager extends ViewPager {

    public DynamicHeightViewPager(@NonNull final Context context) {
        super(context);
    }

    public DynamicHeightViewPager(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final View firstChild = getChildAt(0);
        int measuredHeight = 0;
        if (firstChild != null) {
            firstChild.measure(widthMeasureSpec, heightMeasureSpec);
            measuredHeight = firstChild.getMeasuredHeight();
        }
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY));
    }
}