package com.mercadopago.android.px.internal.viewmodel;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import com.meli.android.carddrawer.configuration.FontType;
import com.meli.android.carddrawer.configuration.SecurityCodeLocation;
import com.meli.android.carddrawer.model.CardAnimationType;
import com.meli.android.carddrawer.model.CardUI;
import com.mercadopago.android.px.model.CardDisplayInfo;
import java.io.Serializable;

public final class CardDrawerConfiguration implements CardUI, Serializable {

    private static final int NUMBER_SEC_CODE = 3;

    @DrawableRes private int logoRes;
    @DrawableRes private int issuerRes;
    private final CardDisplayInfo info;
    @ColorInt private final int color;
    @ColorInt private final int fontColor;

    public CardDrawerConfiguration(final CardDisplayInfo info) {
        this.info = info;
        color = Color.parseColor(info.color);
        fontColor = Color.parseColor(info.fontColor);
    }

    @Override
    public int[] getCardNumberPattern() {
        return info.cardPattern;
    }

    @Override
    public String getNamePlaceHolder() {
        return "";
    }

    @Override
    public String getExpirationPlaceHolder() {
        return "";
    }

    @Override @FontType
    public String getFontType() {
        return info.fontType == null ? FontType.LIGHT_TYPE : info.fontType;
    }

    @Override
    public String getAnimationType() {
        return CardAnimationType.NONE;
    }

    @Override
    public int getBankImageRes() {
        return issuerRes;
    }

    @Override
    public int getCardLogoImageRes() {
        return logoRes;
    }

    @Override
    public String getSecurityCodeLocation() {
        return SecurityCodeLocation.BACK;
    }

    @Override
    public int getCardFontColor() {
        return fontColor;
    }

    @Override
    public int getCardBackgroundColor() {
        return color;
    }

    @Override
    public int getSecurityCodePattern() {
        return NUMBER_SEC_CODE;
    }

    @Override
    public void setCardLogoImage(@NonNull final ImageView cardLogo) {
        cardLogo.setImageResource(getCardLogoImageRes());
    }

    @Override
    public void setBankImage(@NonNull final ImageView bankImage) {
        bankImage.setImageResource(getBankImageRes());
    }

    public String getName() {
        return info.cardholderName;
    }

    public String getDate() {
        return info.expiration;
    }

    public String getNumber() {
        return info.getCardPattern();
    }

    public String getIssuerImageName() {
        return info.issuerImage;
    }

    public void setLogoRes(@DrawableRes final int logoRes) {
        this.logoRes = logoRes;
    }

    public void setIssuerRes(@DrawableRes final int issuerRes) {
        this.issuerRes = issuerRes;
    }
}
