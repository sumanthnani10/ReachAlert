package com.ismartapps.reachalert;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class FullScreenIntent extends Activity {

    String TAG = "StopRing";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);


        setContentView(R.layout.fullscreenintent);
        TextView stop = findViewById(R.id.stop);
        Context context = this;
        Log.d(TAG, "onReceive: Stopping Ring");
        stop.setOnClickListener(view -> {
            Utils.stopRing(context);
            Utils.clearNotifications();
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
