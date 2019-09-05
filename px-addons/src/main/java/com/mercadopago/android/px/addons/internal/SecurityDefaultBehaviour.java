package com.mercadopago.android.px.addons.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.addons.SecurityBehaviour;
import com.mercadopago.android.px.addons.model.SecurityValidationData;

/* default */ final class SecurityDefaultBehaviour implements SecurityBehaviour {
    @Override
    public boolean isSecurityEnabled(@NonNull final SecurityValidationData data) {
        return false;
    }

    @Override
    public void startValidation(@NonNull final Activity activity,
        @NonNull final SecurityValidationData data, final int requestCode) {
        activity.startActivityForResult(getIntent(activity), requestCode);
    }

    @Override
    public void startValidation(@NonNull final Fragment fragment,
        @NonNull final SecurityValidationData data, final int requestCode) {
        //noinspection ConstantConditions
        fragment.startActivityForResult(getIntent(fragment.getContext()), requestCode);
    }

    @NonNull
    private Intent getIntent(@NonNull final Context context) {
        return new Intent(context, MockValidationActivity.class);
    }
}