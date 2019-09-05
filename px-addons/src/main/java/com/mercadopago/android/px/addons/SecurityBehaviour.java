package com.mercadopago.android.px.addons;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.mercadopago.android.px.addons.model.SecurityValidationData;

public interface SecurityBehaviour {

    /**
     * @param data the preference that represents the payment information.
     * @return true if user will be challenged with biometrics/pin/pattern, false otherwise.
     */
    boolean isSecurityEnabled(@NonNull SecurityValidationData data);

    /**
     * Starts biometrics/Pin/Pattern/Password validation flow. Handle result in onActivityResult with RESULT_OK when
     * success.
     *
     * @param activity activity needed to start biometrics.
     * @param data data to be validated.
     * @param requestCode request code to be used to start biometrics validation activity.
     */
    void startValidation(@NonNull final Activity activity,
        @NonNull final SecurityValidationData data, final int requestCode);

    /**
     * Starts biometrics/Pin/Pattern/Password validation flow. Handle result in onActivityResult with RESULT_OK when
     * success.
     *
     * @param fragment fragment needed to start biometrics.
     * @param data data to be validated.
     * @param requestCode request code to be used to start biometrics validation activity.
     */
    void startValidation(@NonNull final Fragment fragment,
        @NonNull final SecurityValidationData data, final int requestCode);
}