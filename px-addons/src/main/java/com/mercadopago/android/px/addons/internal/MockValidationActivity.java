package com.mercadopago.android.px.addons.internal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MockValidationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_OK);
        finish();
    }
}