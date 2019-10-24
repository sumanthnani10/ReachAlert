package com.ismartapps.reachalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;


public class StopRing extends BroadcastReceiver {

    String TAG = "StopRing";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Stopping Ring");
        Utils.stopRing(context);
        Utils.clearNotifications();
        SharedPreferences sharedPreferences = context.getSharedPreferences("userdetails", Context.MODE_PRIVATE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Last Used").child(sharedPreferences.getString("dbname", "User Name")).child("Status").child("Stopped Ring").setValue(true);
    }
}
