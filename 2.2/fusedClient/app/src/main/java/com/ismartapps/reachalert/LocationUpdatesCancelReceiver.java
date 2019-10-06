package com.ismartapps.reachalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationUpdatesCancelReceiver extends BroadcastReceiver {

    String TAG = "LocationUpdatesCancelReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        MapsActivityFinal.removeLocationUpdates(context);
        Log.d(TAG, "onReceive: Removed Location Updates");
        Utils.clearNotifications();
        Log.d(TAG, "onReceive: Cleared Notifications");
    }
}
