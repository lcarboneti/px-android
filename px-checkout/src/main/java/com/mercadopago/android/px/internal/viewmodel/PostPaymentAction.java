package com.mercadopago.android.px.internal.viewmodel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class PostPaymentAction implements Parcelable {

    private static final String EXTRA_POST_PAYMENT_ACTION = "extra_post_payment_action";
    @NonNull protected final RequiredAction requiredAction;

    public PostPaymentAction(@NonNull final RequiredAction requiredAction) {
        this.requiredAction = requiredAction;
    }

    protected PostPaymentAction(final Parcel in) {
        requiredAction = RequiredAction.values()[in.readInt()];
    }

    public abstract void execute(@NonNull final ActionController actionController);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(requiredAction.ordinal());
    }

    @NonNull
    public Intent addToIntent(@NonNull final Intent intent) {
        intent.putExtra(EXTRA_POST_PAYMENT_ACTION, this);
        return intent;
    }

    public static boolean hasPostPaymentAction(@Nullable final Intent intent) {
        return intent != null && intent.getExtras() != null && intent.hasExtra(EXTRA_POST_PAYMENT_ACTION);
    }

    @NonNull
    public static PostPaymentAction fromBundle(@NonNull final Bundle bundle) {
        final PostPaymentAction action = bundle.getParcelable(EXTRA_POST_PAYMENT_ACTION);
        switch (action.requiredAction) {
        case RECOVER_PAYMENT:
            return new RecoverPaymentPostPaymentAction();
        case SELECT_OTHER_PAYMENT_METHOD:
            return new ChangePaymentMethodPostPaymentAction();
        default:
            throw new IllegalStateException("Impossible to create PostPaymentAction");
        }
    }

    public enum RequiredAction {
        SELECT_OTHER_PAYMENT_METHOD, RECOVER_PAYMENT
    }

    public interface ActionController {
        void recoverPayment(@NonNull final PostPaymentAction postPaymentAction);

        void onChangePaymentMethod();
    }
}