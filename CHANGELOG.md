## VERSION 4.20.0
_29_08_2019_
* FEATURE - Added tracking of session time and checkout type.
* FEATURE - Added card drawer on express payment.
* FIX - CPF validation.

## VERSION 4.19.0
_22_08_2019_
* FEATURE - Added tracking of payment method selected index and available methods quantity.
* FEATURE - Unified product id setters.
* FIX - Consumer credits crash on low res.
* FIX - Payment method discount on one tap.
* ENHANCEMENT - Gradle task for local builds.
* ENHANCEMENT - Security Code screen refactor.

## VERSION 4.18.0
_01_08_2019_
* FIX - Prevent double click on Confirm Payment.
* FIX - Changed launch mode to singleTop to prevent double initialization.
* ENHANCEMENT - Migrated deploying to Bitrise.
* ENHANCEMENT - Updated Card Association ETEs.

## VERSION 4.17.1
_24_07_2019_
* FIX - Cancel exploding button loading on cvv comeback after payment recover
* FIX - Prevent double lazy initialization of PX.

## VERSION 4.16.4
_24_07_2019_
* FIX - Cancel exploding button loading on cvv comeback after payment recover
* FIX - Prevent double lazy initialization of PX.

## VERSION 4.17.0
_18_07_2019_
* FEATURE - Changed preference mail validator to required if no private key is setted
* ENHANCEMENT - Removed payer from init track for security reasons

## VERSION 4.16.3
_16_07_2019_
* FIX - Prevent library double initialization

## VERSION 4.15.2
_15_07_2019_
* FIX - Prevent library double initialization

## VERSION 4.16.2
_11_07_2019_
* FIX - Default marketplace to NONE

## VERSION 4.16.1
_10_07_2019_
* FIX - Handling context in http client interceptor
* FIX - Validating flow in onRestore validations in guessing card activity

## VERSION 4.16.0
_01_07_2019_
* Added support for max installments
* Custom product id header
* Default Payment Processor for black labeled payments
* Added support for consumer credits payment method

## VERSION 4.15.1
_19_06_2019_
* FIX - Incorrect amount on congrats when discount and one installment

## VERSION 4.14.3
_19_06_2019_
* FIX - Incorrect amount on congrats when discount and one installment

## VERSION 4.15.0
_14_06_2019_
* FEATURE - Added gateway mode support

## VERSION 4.14.2
_14_06_2019_
* ENHANCEMENT - Now express presenter saves the current payment method index

## VERSION 4.14.1
_13_06_2019_
* FIX - Added back deprecated method

## VERSION 4.14.0
_11_06_2019_
* FEATURE - Custom pay button text support
* FEATURE - BackHandler interface for visual Payment Processor
* ENHANCEMENT - Better error handling in Express Payment
* ENHANCEMENT - CheckoutActivity MVP refactor
* ENHANCEMENT - Constants refactor
* FIX - Congrats layout

## VERSION 4.12.6
_11_06_2019_
* Fix invalid tracks again
* Fix circle reveal animation crash on Android 6

## VERSION 4.13.0
_03_06_2019_
* FEATURE - Express Payment Subtitle
* FEATURE - Express Payment Charges
* FEATURE - Modified Processing Mode
* FEATURE - New agreement params for Payer Cost
* ENHANCEMENT - Dynamic Low res in Express Payment
* ENHANCEMENT - Context for Session using a Content Provider
* ENHANCEMENT - Added view tags for ETEs
* ENHANCEMENT - IPaymentDescriptor default methods
* ENHANCEMENT - Tracking cards ESC
* ENHANCEMENT - Payer Information ETEs refactor
* ENHANCEMENT - Migration to API 28
* FIX - Payer Information restore instance

## VERSION 4.12.5
_23_05_2019_
* Fix invalid tracks

## VERSION 4.12.4
_20_05_2019_
* Fix automatic selection when the only payment method isn't card
* Changed item validation
* Fix serialization of payer lastName and firstName
* Fix amount descriptor view crop

## VERSION 4.12.3
_13_05_2019_
* FIX - Business result crash when session isn't initialized correctly

## VERSION 4.12.2
_08_05_2019_
* FIX - Removed empty box and divider in rejected views
* FIX - Catch NPE in network status check

## VERSION 4.12.1
_07_05_2019_
* FIX - Populate card properties with custom options in one tap

## VERSION 4.12.0
_03_05_2019_
* FEATURE - Esc for guessing card
* FEATURE - Using discount name for his description
* ENHANCEMENT - Track friction event when invalid esc
* ENHANCEMENT - Add reason to cvv tracker
* FIX - Returning to one tap after esc recover
* FIX - Orientation issues

## VERSION 4.11.0
_26_04_2019_
* FEATURE - Congrats tracking new attributes
* FIX - Crash payment processor activity

## VERSION 4.10.3
_25_04_2019_
* FIX - Recover removed method avoiding breaking changes.

## VERSION 4.10.2
_25_04_2019_
* FIX - Creation of session id for tracking purpose

## VERSION 4.10.1
_22_04_2019_
* ENHANCEMENT - Added amount on call for auth rejection message
* FIX - User wants to split selection persist
* FIX - Correct amount on congrats
* FIX - Reset payment method slider position on payment method changed after rejection
* FIX - Show result using visual payment processor

## VERSION 4.10.0
_11_04_2019_
* FEATURE - Disable last selected payment method after reject and recover
* FEATURE - Express Payment's support for Single Player
* ENHANCEMENT - Updated Citibanamex logo
* FIX - Blank screen in installments

## VERSION 4.9.3
_29_03_2019_
* FIX - Friction rate in id card

## VERSION 4.9.2
_28_03_2019_
* FIX - Automatic selection flow
* FIX - Correct amount for split payment on Congrats
* FIX - Crash on Circle Transform for certain images

## VERSION 4.9.1
_25_03_2019_
* FIX - Retry card storage

## VERSION 4.9.0
_21_03_2019_
* FEATURE - Issuer images in Cards
* FEATURE - CNPJ payment with Boleto
* FEATURE - Skip congrats in storage card flow

## VERSION 4.8.1
_18_03_2019_
* FIX - Crash on guessing when retrying payment

## VERSION 4.8.0
_12_03_2019_
* FEATURE - Account money discounts
* FEATURE - CPF validation in card guessing
* ENHANCEMENT - Soldout discount communication
* ENHANCEMENT - Changes in Loyalty flow
* ENHANCEMENT - Much lower assets weight
* FIX - Money In UI fixes

## VERSION 4.7.3
_11_03_2019_
* FIX - disable back button on exploding animation

## VERSION 4.7.2
_27_02_2019_
* FIX - payment processor bundle mapping fix for vending
* FIX - animations split payment
* FIX - invalid state cardvault

## VERSION 4.7.1
_27_02_2019_
* FIX - signature card storage
* FIX - identification only cpf for brazil
* FIX - event data review and confirm

## VERSION 4.7.0
_22_02_2019_
* FEATURE - Split Payment.
* FEATURE - CPF Validation.
* ENHANCEMENT - Added abort and action events for congrats / business.
* FIX - Terms and conditions event data.
* FIX - Animations in PaymentVaultActivity.
* FIX - Loading identification types NPE.
* FIX - Add new method drives to groups if cards isn't present.

## VERSION 4.6.2
_12_02_2019_
* FIX - Added credit card date validation.  
* ENHANCEMENT - Added tracks. 
* ENHANCEMENT - Added discount terms and conditions.
* FIX - Rollback public method.
* FIX - Connectivity manager.
* FIX - Activity new flag support - Android 9.

## VERSION 4.6.1
_04_02_2019_
* FIX - Added correct discount id in PaymentData.
* FIX - Attached view when exploding animation finished.

## VERSION 4.5.2
_31_01_2019_
* FIX - NPE tracking events.

## VERSION 4.6.0
_24_01_2019_
* FEATURE - Support to payment method discount.

## VERSION 4.5.1
_10_01_2019_
* FIX - Crash in groups disk cache.

## VERSION 4.5.0
_02_01_2019_
* FEATURE - Account money as a first class member.
* FEATURE - Added event and view data for PXTrackingListener class.
* FIX - Crash on back on Sec code saved card.

## VERSION 4.4.1
_18_12_2018_

* FIX - Navigation on payment method changed
* FIX - Crash on back from payment vault

## VERSION 4.4.0
_13_12_2018_

* FIX - Installments list clip in groups flow
* FIX - Invalid tracks
* ENHANCEMENT - Check for additional info for payer
* ENHANCEMENT - Better deploy scripts

## VERSION 4.3.3
_4_12_2018_

* FIX - Crash in Checkout, PaymentResult and BusinessResult on application kill

## VERSION 4.3.2
_30_11_2018_

* FIX - Crash on Card Association congrats

## VERSION 4.3.1
_21_11_2018_

* FIX - Installments selection in express flow
* FIX - Account money invested in express flow
* FIX - PEC and payer information assets
* FIX - Instructions padding

## VERSION 4.3.0
_31_10_2018_

 * FEATURE - Express checkout.
 * FEATURE - Skip Payer Information.
 * FEATURE - Pec Payment Method.
 * FEATURE - Enabled MLU (Site Uruguay)
 * FEATURE - Interactive instructions in congrats.
 * FEATURE - Configurable titles for Payment Vault Screen.
 * FEATURE - added new tracking listener PXTrackingListener for MeliData compatibility
 ```java
  void setListener(@NonNull final PXTrackingListener listener,
              @NonNull final Map<String, ? extends Object> flowDetail, 
              @Nullable final String flowName)
```

## VERSION 4.2.1
_30_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.2.0
_25_10_2018_

* FEATURE - dynamic custom dialogs for certain locations.
* FEATURE - dynamic custom views for review and confirm.
* ENHANCEMENT - tracking screen's names and paths unified.
* ENHANCEMENT/FIX - card addition flow now supports installments and ESC.

## VERSION 4.1.3
_01_11_2018_

* FIX - Crash NPE on exploding button

## VERSION 4.1.2
_31_10_2018_

* FIX - Crash NPE when processing payment in background
* FIX - Crash on recovery payment

## VERSION 4.1.0
_04_10_2018_

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* ENHANCEMENT - Standalone Card Association

## VERSION 4.0.6
_08_10_2018_

* FIX - Double congrats one tap.

## VERSION 4.0.5

* FIX - Show app bar when tap back from CVV screen.
* FIX - Tracking of PaymentMethodSearchItem.
* FIX - Payment processor - visual attach bug

## VERSION 4.0.4
_25_09_2018_

* FIX - DefaultPaymentTypeId debit card error, can't look for settings.
* FIX - Colombia currency utils.

## VERSION 4.0.3

_20_09_2018_

* FIX - Payment recovery call for auth.
* FIX - destroy activity behaviour.
* FIX - NPE no decimals for Site Colombia.
* ENHANCEMENT - Color customization detailed documentation.
* ENHANCEMENT - Loading improvements for visual payments (payment processor).

## VERSION 4.0.2

_05_09_2018_

* Fix: one tap with payment recovery
* Fix: payment processor background support
* Fix: added internal payment method change behaviour


## VERSION 4.0.1

_03_09_2018_

* Fix: dynamic id declaration
* Fix: code discount
* Fix: esc with one tap

## VERSION 4.0.0

_30_08_2018_
