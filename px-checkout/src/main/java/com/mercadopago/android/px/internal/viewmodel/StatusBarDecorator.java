package com.mercadopago.android.px.internal.viewmodel;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;

public final class StatusBarDecorator {

    private static final float DARKEN_FACTOR = 0.04f;
    private static final int HSV_LENGTH = 3;
    private final float[] hsv = new float[HSV_LENGTH];
    private final Window window;

    /**
     * Create a status bar decorator
     *
     * @param window the activity window
     */
    public StatusBarDecorator(@NonNull final Window window) {
        this.window = window;
    }

    /**
     * Paint the status bar
     *
     * @param color the color to use. The color will be darkened by {@link #DARKEN_FACTOR} percent
     */
    @SuppressLint({ "InlinedApi" })
    public void setupStatusBarColor(@ColorInt final int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
            hsv[2] = Math.max(hsv[2] * (1 - DARKEN_FACTOR), 0);
            window.setStatusBarColor(Color.HSVToColor(hsv));
        }
    }
}
