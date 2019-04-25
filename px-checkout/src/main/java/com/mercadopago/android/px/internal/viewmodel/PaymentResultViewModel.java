package com.mercadopago.android.px.internal.viewmodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Action;

public final class PaymentResultViewModel {

    private final int titleResId;
    private final int labelResId;
    private final int bodyTitleResId;
    private final int titleDescriptionResId;
    private final boolean hasDetail;
    private final boolean isRecoverable;
    private final boolean isSuccess;
    private final boolean isPendingWarning;
    private final boolean isPendingSuccess;
    private final int descriptionResId;
    private final int backgroundResId;
    private final int statusBarResId;
    private final int badgeResId;
    private final String descriptionParam;

    // Action metadata
    @Nullable private final Action mainAction;
    private final int mainActionTitle;
    @Nullable private final Action linkAction;
    private final int linkActionTitle;

    private PaymentResultViewModel(final Builder builder) {
        titleResId = builder.titleResId;
        labelResId = builder.labelResId;
        hasDetail = builder.hasDetail;
        mainAction = builder.mainAction;
        mainActionTitle = builder.mainActionTitle;
        linkAction = builder.linkAction;
        linkActionTitle = builder.linkActionTitle;
        isRecoverable = builder.isRecoverable;
        isSuccess = builder.isApprovedSuccess;
        isPendingWarning = builder.isPendingWarning;
        isPendingSuccess = builder.isPendingSuccess;
        descriptionResId = builder.descriptionResId;
        bodyTitleResId = builder.bodyTitleResId;
        titleDescriptionResId = builder.titleDescriptionResId;
        backgroundResId = builder.backgroundResId;
        statusBarResId = builder.statusBarResId;
        badgeResId = builder.badgeResId;
        descriptionParam = builder.descriptionParam;
    }

    public boolean hasBodyTitle() {
        return bodyTitleResId != 0;
    }

    public String getTitle(@NonNull final Context context) {
        return titleResId == 0 ? TextUtil.EMPTY : context.getString(titleResId);
    }

    public String getLabel(@NonNull final Context context) {
        return labelResId == 0 ? TextUtil.EMPTY : context.getString(labelResId);
    }

    public boolean hasBodyError() {
        return hasDetail;
    }

    public boolean isErrorRecoverable() {
        return isRecoverable;
    }

    public String getMainActionTitle(@NonNull final Context context) {
        return mainActionTitle == 0 ? TextUtil.EMPTY : context.getString(mainActionTitle);
    }

    @Nullable
    public Action getMainAction() {
        return mainAction;
    }

    @Nullable
    public Action getLinkAction() {
        return linkAction;
    }

    public String getLinkActionTitle(@NonNull final Context context) {
        return linkActionTitle == 0 ? TextUtil.EMPTY : context.getString(linkActionTitle);
    }

    public boolean isPendingWarning() {
        return isPendingWarning;
    }

    public boolean isPendingSuccess() {
        return isPendingSuccess;
    }

    public boolean isApprovedSuccess() {
        return isSuccess;
    }

    @NonNull
    public String getDescription(@NonNull final Context context) {
        return descriptionResId == 0 ? TextUtil.EMPTY : getDescriptionText(context);
    }

    @NonNull
    private String getDescriptionText(@NonNull final Context context) {
        return descriptionParam != null ? context.getString(descriptionResId, descriptionParam)
            : context.getString(descriptionResId);
    }

    public String getBodyTitle(@NonNull final Context context) {
        return bodyTitleResId == 0 ? TextUtil.EMPTY : context.getString(bodyTitleResId);
    }

    public int getBackgroundResId() {
        return backgroundResId;
    }

    public int getStatusBarResId() {
        return statusBarResId;
    }

    public int getBadgeResId() {
        return badgeResId;
    }

    public String getTitleDescription(@NonNull final Context context) {
        return titleDescriptionResId == 0 ? TextUtil.EMPTY : context.getString(titleDescriptionResId);
    }

    public static class Builder {
        int titleResId;
        int labelResId;
        int descriptionResId;
        int bodyTitleResId;
        int titleDescriptionResId;
        boolean isRecoverable;
        boolean hasDetail;
        boolean isApprovedSuccess;
        boolean isPendingWarning;
        boolean isPendingSuccess;
        int backgroundResId;
        int statusBarResId;
        int badgeResId;
        String descriptionParam;

        // Action metadata
        @Nullable Action mainAction;
        int mainActionTitle;

        @Nullable Action linkAction;
        int linkActionTitle;

        public Builder setLabelResId(final int labelResId) {
            this.labelResId = labelResId;
            return this;
        }

        public Builder setTitleResId(final int titleResId) {
            this.titleResId = titleResId;
            return this;
        }

        public Builder setHasDetail(final boolean hasDetail) {
            this.hasDetail = hasDetail;
            return this;
        }

        public Builder setMainAction(@Nullable final Action action) {
            mainAction = action;
            return this;
        }

        public Builder setMainActionTitle(final int titleResId) {
            mainActionTitle = titleResId;
            return this;
        }

        public Builder setLinkAction(@Nullable final Action action) {
            linkAction = action;
            return this;
        }

        public Builder setLinkActionTitle(final int actionTitleResId) {
            linkActionTitle = actionTitleResId;
            return this;
        }

        public Builder setIsErrorRecoverable(final boolean isRecoverable) {
            this.isRecoverable = isRecoverable;
            return this;
        }

        public Builder setApprovedSuccess(final boolean success) {
            isApprovedSuccess = success;
            return this;
        }

        public Builder setPendingWarning(final boolean pendingWarning) {
            isPendingWarning = pendingWarning;
            return this;
        }

        public Builder setPendingSuccess(final boolean pendingSuccess) {
            isPendingSuccess = pendingSuccess;
            return this;
        }

        public Builder setDescriptionResId(final int descriptionResId) {
            this.descriptionResId = descriptionResId;
            return this;
        }

        public Builder setBodyTitleResId(final int bodyTitleResId) {
            this.bodyTitleResId = bodyTitleResId;
            return this;
        }

        public Builder setBodyDetailDescriptionResId(final int titleDescriptionResId) {
            this.titleDescriptionResId = titleDescriptionResId;
            return this;
        }

        public Builder setBackgroundColor(final int backgroundColor) {
            backgroundResId = backgroundColor;
            return this;
        }

        public Builder setStatusBarResId(final int statusBarResId) {
            this.statusBarResId = statusBarResId;
            return this;
        }

        public Builder setBadgeResId(final int badgeResId) {
            this.badgeResId = badgeResId;
            return this;
        }

        public Builder setDescriptionResId(final int descriptionResId, final String param) {
            this.descriptionResId = descriptionResId;
            descriptionParam = param;
            return this;
        }

        public PaymentResultViewModel build() {
            return new PaymentResultViewModel(this);
        }
    }
}
