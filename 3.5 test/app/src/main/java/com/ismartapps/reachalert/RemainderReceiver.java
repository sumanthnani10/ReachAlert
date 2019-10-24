package com.ismartapps.reachalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class RemainderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RR", "onReceive"+intent.getStringExtra("text"));

        NotificationManager mNotificationManager;

        String text = intent.getStringExtra("text");
        int id = intent.getExtras().getInt("id");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Remainder");
        String content;

        double[] latLng = intent.getExtras().getDoubleArray("latlng");
        Intent notificationIntent;
        if(intent.getBooleanExtra("New?",true))
        {
            notificationIntent = new Intent(context,SplashActivity.class);
            content = "Just Set And Sleep";
        }
        else{

        notificationIntent = new Intent(context,SplashActivityNotification.class);
        notificationIntent.putExtra("name",text);
        notificationIntent.putExtra("placeId",intent.getStringExtra("placeId"));
        notificationIntent.putExtra("latlng",latLng);
        notificationIntent.putExtra("id",id);
        Log.d("RR", "onReceive: "+text+" ,"+id+" ,"+intent.getStringExtra("placeId")+" ,"+latLng.toString());
        if(text.equals("Dropped Pin"))
            content="You visited some random place yesterday\nPlanning to go again?";
        else
            content = "You visited this place.\nPlanning to go again?";
        }
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context,123,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);


        builder.setSmallIcon(R.drawable.notification_small_icon)
                .setColor(Color.YELLOW)
                .setContentTitle(text)
                .setSmallIcon(R.mipmap.ic_launcher_icon_round)
                .setContentText(content)
                .setContentIntent(notificationPendingIntent)
                .addAction(R.drawable.notification_small_icon,"Yes",notificationPendingIntent)
                .setTicker(text+content)
                .setAutoCancel(true);

        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel("Remainder", name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setShowBadge(true);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.setLightColor(Color.YELLOW);

            mNotificationManager.createNotificationChannel(mChannel);

            builder.setChannelId("Remainder");
        }

        mNotificationManager.notify(id, builder.build());

        SharedPreferences sharedPreferences = context.getSharedPreferences("userdetails",MODE_PRIVATE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Last Used").child(sharedPreferences.getString("dbname", "User Name")).child("Notified").setValue(DateFormat.getDateTimeInstance().format(new Date()));
    }
}
