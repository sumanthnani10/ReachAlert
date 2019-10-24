package com.ismartapps.reachalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


class Utils {
    private static String TAG="Utils";

    private final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    private static NotificationManager mNotificationManager;

    static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static void sendNotificationOnComplete(String placeName, Context context) {
        //on Stop Click
        Intent stopRingIntent = new Intent(context,StopRing.class);
        PendingIntent stopRingPendingIntent = PendingIntent.getBroadcast(context,0,stopRingIntent,0);

        PendingIntent fullscreenPendingIntent = PendingIntent.getActivity(context,22,new Intent(context,FullScreenIntent.class),PendingIntent.FLAG_UPDATE_CURRENT);

        if (placeName.equals("Dropped Pin"))
        {
            placeName = "Location";
        }

        placeName ="Reached "+placeName;


        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Reached")
                .setSmallIcon(R.drawable.notification_small_icon)
                .setColor(Color.GREEN)
                .setContentTitle("Reached")
                .setContentText(placeName)
                .addAction(R.drawable.notification_small_icon,"Stop",stopRingPendingIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setOngoing(true);

        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        boolean dark = settings.getBoolean("dark",false);
        if(dark)
        {
            builder.setColor(Color.BLACK);
            builder.setColorized(true);
        }

        // Get an instance of the Notification manager
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel("Reached", name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setShowBadge(true);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.setLightColor(Color.GREEN);
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            // Channel ID
            builder.setChannelId("Reached");
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    private static Ringtone r;
    private static Timer timer;
    private static Vibrator vibrator;


    static void playRing(Context context){
        Log.d(TAG, "playRing");
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(context,notification);
        r.setStreamType(AudioManager.STREAM_RING);

        if(!r.isPlaying()) r.play();

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0,1000,1000};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern,0));
        }
        else
            vibrator.vibrate(pattern,0);


        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!r.isPlaying()) r.play();
                Log.d(TAG, "run ");
            }
        },1000,1000);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRing(context);
                clearNotifications();
                timer.cancel();
            }

        }, 5*60*1000);
    }

    static void stopRing(Context context){
        Log.d(TAG, "Stopping Ring");
        if (r.isPlaying())
        {r.stop();}

        if(vibrator!=null)
        {
            vibrator.cancel();
        }

        timer.cancel();
    }

    static void clearNotifications(){
        Log.d(TAG, "clearNotifications");
        mNotificationManager.cancelAll();
    }
}