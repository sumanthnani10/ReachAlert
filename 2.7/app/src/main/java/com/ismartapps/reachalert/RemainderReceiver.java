package com.ismartapps.reachalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.maps.model.LatLng;

public class RemainderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RR", "onReceive"+intent.getStringExtra("text"));

        NotificationManager mNotificationManager;

        String text = intent.getStringExtra("text");
        int id = intent.getExtras().getInt("id");

        double[] latLng = intent.getExtras().getDoubleArray("latlng");
        Intent notificationIntent = new Intent(context,SplashActivityNotification.class);
        notificationIntent.putExtra("name",text);
        notificationIntent.putExtra("placeId",intent.getStringExtra("placeId"));
        notificationIntent.putExtra("latlng",latLng);
        Log.d("RR", "onReceive: "+text+" ,"+id+" ,"+intent.getStringExtra("placeId")+" ,"+latLng.toString());
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context,123,notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Remainder");

        builder.setSmallIcon(R.drawable.notification_small_icon)
                .setColor(Color.YELLOW)
                .setContentTitle("Going there again?")
                .setContentText(text+"\nYou visited this place.\nPlanning to go again?")
                .setContentIntent(notificationPendingIntent)
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
    }
}
