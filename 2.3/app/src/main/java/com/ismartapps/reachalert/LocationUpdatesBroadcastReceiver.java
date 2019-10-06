package com.ismartapps.reachalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES =
            "com.example.android.map.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {


        double[] temp = MapsActivityFinal.getInfo();
        if (temp[2]>0){
        FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        final Task taskLocation = mFusedLocationClient.getLastLocation();
        taskLocation.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "onComplete: (Primary) found location");
                Location location = (Location) task.getResult();

                if (location != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(temp[0], temp[1], location.getLatitude(), location.getLongitude(), results);
                    Log.d(TAG, "onReceive: " + results[0] + " , " + temp[0] + "," + temp[1] + "," + temp[2]);
                    String dist = "0";
                    if (results[0] - temp[2] > 1000) {
                        dist = String.format("%.2f", (results[0] - temp[2]) / 1000) + " k";
                    } else
                        dist = String.valueOf((int) results[0] - temp[2]);
                    Utils.setLocationUpdatesResult(context, dist);
                    Utils.sendNotification(context, Utils.getLocationResultTitle(context, dist));
                    Log.i(TAG, Utils.getLocationUpdatesResult(context));
                    MapsActivityFinal.moveCamera(new LatLng(temp[0], temp[1]), new LatLng(location.getLatitude(), location.getLongitude()));
                    if (results[0] < temp[2]) {
                        Log.d(TAG, "onReceive: Target is Reached");
                        MapsActivityFinal.stopReceiver(context);
                        Utils.sendNotificationOnComplete(context, "Reached Location");
                        Utils.playRing(context);
                    }
                }
            } else {
                Log.d(TAG, "onComplete: (Primary) location not found");
                Toast.makeText(context, "Reach Alert : Unable to find the location.Please Check the Internet", Toast.LENGTH_SHORT).show();
            }
        });
    }

     else {
         Utils.clearNotifications();
         MapsActivityFinal.stopReceiver(context);
        }
    }
}
/*old-if (intent != null) {
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
                        Utils.sendNotificationOnComplete(context, "Reached Location");
                        Utils.playRing(context);
                        MapsActivityFinal.removeLocationUpdates(context);
                    }
                }
            }
        }*/