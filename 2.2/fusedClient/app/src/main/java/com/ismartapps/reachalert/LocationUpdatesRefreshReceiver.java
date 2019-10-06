package com.ismartapps.reachalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocationUpdatesRefreshReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("LocationUpdatesRefresh", "onReceive: Refreshing location updates");
        MapsActivityFinal.removeLocationUpdates(context);
        MapsActivityFinal.requestLocationUpdates(context);
    }
}
