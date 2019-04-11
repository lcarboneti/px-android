package com.mercadopago.android.px.internal.features.guessing_card.card_association_result;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.viewmodel.StatusBarDecorator;
import com.mercadopago.android.px.tracking.internal.views.CardAssociationResultViewTrack;

public class CardAssociationResultSuccessActivity extends AppCompatActivity {

    public static void startCardAssociationResultSuccessActivity(final Activity callerActivity) {
        final Intent intent = new Intent(callerActivity, CardAssociationResultSuccessActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        callerActivity.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.px_card_association_result_success);

        new StatusBarDecorator(getWindow())
            .setupStatusBarColor(ContextCompat.getColor(this, R.color.px_green_status_bar));

        final MeliButton exitButton = findViewById(R.id.mpsdkCardAssociationResultExitButton);
        exitButton.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });

        new CardAssociationResultViewTrack(CardAssociationResultViewTrack.Type.SUCCESS).track();
    }
}
