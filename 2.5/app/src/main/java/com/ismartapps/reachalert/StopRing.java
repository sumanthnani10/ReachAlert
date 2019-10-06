package com.ismartapps.reachalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StopRing extends BroadcastReceiver {

    String TAG = "StopRing";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Stopping Ring");
        Utils.stopRing(context);
        Utils.clearNotifications();
    }
}
