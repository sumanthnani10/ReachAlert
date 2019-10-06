package com.ismartapps.reachalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

public class LocationUpdatesClientReceiver extends BroadcastReceiver {
    private static final String TAG = "LUClientReceiver";

    static final String ACTION_PROCESS_UPDATES =
            "com.example.android.map.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null ) {
                    double[] temp = MapsActivityFinal.getInfo();
                    Location location = result.getLastLocation();
                    float[] results = new float[1];
                    Location.distanceBetween(temp[0],temp[1],location.getLatitude(),location.getLongitude(),results);
                    Log.d(TAG, "onReceive: "+results[0]+" , "+temp[0]+","+temp[1]+","+temp[2]);
                    String dist = "0";
                    if (results[0]-temp[2]>1000){
                        dist= String.format("%.2f",(results[0]-temp[2])/1000)+" k";
                    }
                    else
                        dist= String.valueOf((int) results[0]-temp[2]);
                    Utils.setLocationUpdatesResult(context, dist);
                    Utils.sendNotification(context, Utils.getLocationResultTitle(context, dist));
                    Log.i(TAG, Utils.getLocationUpdatesResult(context));
                    MapsActivityFinal.moveCamera(new LatLng(temp[0],temp[1]),new LatLng(location.getLatitude(),location.getLongitude()));
                    if (results[0]<temp[2]) {
                        Log.d(TAG, "onReceive: Target is Reached");
                        MapsActivityFinal.stopReceiver(context);
                        Utils.sendNotificationOnComplete(context, "Reached Location");
                        Utils.playRing(context);
                        MapsActivityFinal.updateReached();
                    }
                }
            }
        }
    }
}