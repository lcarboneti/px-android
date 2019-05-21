package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaymentMethodSearch implements Serializable {

    /**
     * express list contains the list of user related payment methods to offer inside ExpressCheckout
     */
    @Nullable private List<ExpressMetadata> express;
    private List<PaymentMethod> paymentMethods;
    private List<PaymentMethodSearchItem> groups;
    private List<Card> cards;
    @SerializedName("custom_options")
    private List<CustomSearchItem> customSearchItems;

    /**
     * amount management
     **/
    private String defaultAmountConfiguration;

    private Map<String, DiscountConfigurationModel> discountsConfigurations;

    /**
     * @deprecated use new {{@link #express}} we will delete this method on px v5
     */
    @Deprecated
    @SerializedName("one_tap")
    private OneTapMetadata oneTapMetadata;

    /**
     * @deprecated please use new {{@link #hasExpressCheckoutMetadata()} we will delete this method on px v5
     */
    @Deprecated
    public boolean hasOneTapMetadata() {
        return false;
    }

    /**
     * @deprecated please use new {{@link #getExpress()}} we will delete this method on px v5
     */
    @Deprecated
    public OneTapMetadata getOneTapMetadata() {
        return oneTapMetadata;
    }

    /**
     * @deprecated please do not modify this information on runtime. we will delete this method on px v5
     */
    @Deprecated
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    /**
     * @deprecated please do not modify this information on runtime. we will delete this method on px v5
     */
    @Deprecated
    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    /**
     * old account money support that no longer exists.
     *
     * @deprecated please do not modify this information on runtime. we will delete this method on px v5
     */
    @Deprecated
    public void setAccountMoney(AccountMoney accountMoney) {
        //NOOP
    }

    /**
     * @deprecated we will delete this method on px v5
     */
    @Deprecated
    public boolean hasSavedCards() {
        return cards != null && !cards.isEmpty();
    }

    /**
     * @deprecated we will delete this method on px v5
     */
    @Deprecated
    public void setCards(final List<Card> cards, final String lastFourDigitsText) {
        if (cards != null) {
            customSearchItems = new ArrayList<>();
            this.cards = new ArrayList<>();

            for (final Card card : cards) {
                final CustomSearchItem searchItem = new CustomSearchItem();
                searchItem.setDescription(lastFourDigitsText + " " + card.getLastFourDigits());
                searchItem.setType(card.getPaymentMethod().getPaymentTypeId());
                searchItem.setId(card.getId());
                searchItem.setPaymentMethodId(card.getPaymentMethod().getId());
                customSearchItems.add(searchItem);
                this.cards.add(card);
            }
        }
    }

    /**
     * old account money support that no longer exists.
     *
     * @deprecated we will delete this method on px v5
     */
    @Deprecated
    @Nullable
    public AccountMoney getAccountMoney() {
        return null;
    }

    /**
     * @deprecated please do not modify this information on runtime. we will delete this method on px v5
     */
    @Deprecated
    public void setGroups(List<PaymentMethodSearchItem> groups) {
        this.groups = groups;
    }

    //endregion deprecated

    @NonNull
    public List<PaymentMethodSearchItem> getGroups() {
        return groups == null ? new ArrayList<PaymentMethodSearchItem>() : groups;
    }

    @NonNull
    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods == null ? new ArrayList<PaymentMethod>() : paymentMethods;
    }

    public PaymentMethod getPaymentMethodBySearchItem(final PaymentMethodSearchItem item) {
        PaymentMethod requiredPaymentMethod = null;
        if (paymentMethods != null && item != null && item.getId() != null) {
            for (final PaymentMethod currentPaymentMethod : paymentMethods) {
                if (itemMatchesPaymentMethod(item, currentPaymentMethod)) {
                    requiredPaymentMethod = currentPaymentMethod;
                    requiredPaymentMethod.setPaymentTypeId(getPaymentTypeIdFromItem(item, currentPaymentMethod));
                }
            }
        }
        return requiredPaymentMethod;
    }

    private String getPaymentTypeIdFromItem(final PaymentMethodSearchItem item, final PaymentMethod paymentMethod) {
        //Remove payment method id from item id and the splitter
        final String paymentType;
        final String itemIdWithoutPaymentMethod = item.getId().replaceFirst(paymentMethod.getId(), "");
        if (itemIdWithoutPaymentMethod.isEmpty()) {
            paymentType = paymentMethod.getPaymentTypeId();
        } else {
            paymentType = itemIdWithoutPaymentMethod.substring(1);
        }
        return paymentType;
    }

    private boolean itemMatchesPaymentMethod(final PaymentMethodSearchItem item, final PaymentMethod paymentMethod) {
        return item.getId().startsWith(paymentMethod.getId());
    }

    public PaymentMethodSearchItem getSearchItemByPaymentMethod(final PaymentMethod selectedPaymentMethod) {
        PaymentMethodSearchItem requiredItem = null;
        if (selectedPaymentMethod != null) {

            requiredItem = searchItemMatchingPaymentMethod(selectedPaymentMethod);
        }
        return requiredItem;
    }

    private PaymentMethodSearchItem searchItemMatchingPaymentMethod(final PaymentMethod paymentMethod) {
        return searchItemInList(groups, paymentMethod);
    }

    private PaymentMethodSearchItem searchItemInList(final List<PaymentMethodSearchItem> list,
        final PaymentMethod paymentMethod) {
        PaymentMethodSearchItem requiredItem = null;
        for (final PaymentMethodSearchItem currentItem : list) {

            //Case like "pagofacil", without the payment type in the item id.
            if (itemMatchesPaymentMethod(currentItem, paymentMethod) &&
                    currentItem.getId().equals(paymentMethod.getId())) {
                requiredItem = currentItem;
                break;
            }
            //Case like "bancomer_ticket", with the payment type in the item id
            else if (itemMatchesPaymentMethod(currentItem, paymentMethod)) {
                //Remove payment method id from item id
                String potentialPaymentType = currentItem.getId().replaceFirst(paymentMethod.getId(), "");
                if (potentialPaymentType.endsWith(paymentMethod.getPaymentTypeId())) {
                    requiredItem = currentItem;
                    break;
                }
            } else if (currentItem.hasChildren()) {
                requiredItem = searchItemInList(currentItem.getChildren(), paymentMethod);
                if (requiredItem != null) {
                    break;
                }
            }
        }
        return requiredItem;
    }

    @Nullable
    public PaymentMethod getPaymentMethodById(@Nullable final String paymentMethodId) {
        for (final PaymentMethod paymentMethod : getPaymentMethods()) {
            if (paymentMethod.getId().equals(paymentMethodId)) {
                return paymentMethod;
            }
        }
        return null;
    }

    @Nullable
    public Card getCardById(@NonNull final String cardId) {
        for (final CustomSearchItem customSearchItem : getCustomSearchItems()) {
            if (customSearchItem.getId().equals(cardId)) {
                final PaymentMethod paymentMethod = getPaymentMethodById(customSearchItem.getPaymentMethodId());
                final Card card = new Card();
                card.setId(cardId);
                card.setSecurityCode(paymentMethod != null ? paymentMethod.getSecurityCode() : null);
                card.setPaymentMethod(paymentMethod);
                card.setFirstSixDigits(customSearchItem.getFirstSixDigits());
                card.setLastFourDigits(customSearchItem.getLastFourDigits());
                card.setIssuer(customSearchItem.getIssuer());
                return card;
            }
        }
        return null;
    }

    @NonNull
    public List<CustomSearchItem> getCustomSearchItems() {
        return customSearchItems == null ? new ArrayList<>() : customSearchItems;
    }

    @Nullable
    public CustomSearchItem getCustomSearchItemByPaymentMethodId(@NonNull final String paymentMethodId) {
        for (final CustomSearchItem customSearchItem : customSearchItems) {
            if (paymentMethodId.equals(customSearchItem.getPaymentMethodId())) {
                return customSearchItem;
            }
        }
        return null;
    }

    /**
     *
     * @return cards or empty arraylist.
     * @deprecated the card info has been moved to custom option items.
     */
    @Deprecated
    @NonNull
    public List<Card> getCards() {
        return cards == null ? new ArrayList<>() : cards;
    }

    /**
     * @return the list of express options for the user to select.
     */
    @Nullable
    public List<ExpressMetadata> getExpress() {
        return express;
    }

    /**
     * @return boolean that represents if there is express information.
     */
    public boolean hasExpressCheckoutMetadata() {
        return getExpress() != null && !getExpress().isEmpty();
    }

    public boolean hasCustomSearchItems() {
        return !getCustomSearchItems().isEmpty();
    }

    public boolean hasSearchItems() {
        return !getGroups().isEmpty();
    }

    @Nullable
    public Issuer getIssuer(@NonNull final String cardId) {
        final Card foundCard = getCardById(cardId);
        return foundCard == null ? null : foundCard.getIssuer();
    }

    @Nullable
    public String getLastFourDigits(@NonNull final String cardId) {
        final Card foundCard = getCardById(cardId);
        return foundCard == null ? null : foundCard.getLastFourDigits();
    }

    @NonNull
    public String getDefaultAmountConfiguration() {
        return defaultAmountConfiguration;
    }

    @NonNull
    public Map<String, DiscountConfigurationModel> getDiscountsConfigurations() {
        return discountsConfigurations == null ? new HashMap<>() : discountsConfigurations;
    }

    /**
     * Return all the ids of custom options that by default supports split payment.
     *
     * @return set of ids
     */
    @NonNull
    public Set<String> getIdsWithSplitAllowed() {
        final Set<String> cardsWithSplit = new HashSet<>();
        for (final CustomSearchItem customSearchItem : getCustomSearchItems()) {
            if (customSearchItem
                .getAmountConfiguration(customSearchItem.getDefaultAmountConfiguration()).allowSplit()) {
                cardsWithSplit.add(customSearchItem.getId());
            }
        }
        return cardsWithSplit;
    }
}
