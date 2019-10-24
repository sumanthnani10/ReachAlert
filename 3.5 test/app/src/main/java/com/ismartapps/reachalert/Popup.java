package com.ismartapps.reachalert;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import androidx.annotation.NonNull;

public class Popup extends Dialog {

    public Popup(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.setContentView(R.layout.popup);
    }



    @Override
    public void show() {
        super.show();
        Handler handler = new Handler();
        handler.postDelayed(() -> dismiss(),2000);
    }
}
