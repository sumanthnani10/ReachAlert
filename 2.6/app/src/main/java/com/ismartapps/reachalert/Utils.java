package com.ismartapps.reachalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
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

    static String getLocationText(Location location,String dist) {
        return DateFormat.getDateTimeInstance().format(new Date()) + " : " + (location == null ? "Network Issue.Check Connections" : dist + "m away");
    }

    static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static void sendNotificationOnComplete(Context context) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MapsActivityPrimary.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MapsActivityPrimary.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //on Cancel Click
        Intent stopRingIntent = new Intent(context,StopRing.class);
        PendingIntent stopRingPendingIntent = PendingIntent.getBroadcast(context,0,stopRingIntent,0);


        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"Reached");

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.notification_small_icon)
                .setColor(Color.GREEN)
                .setContentTitle("Location Reach update")
                .setContentText("Reached Location")
                //.setContentIntent(notificationPendingIntent)
                .addAction(R.drawable.notification_small_icon,"Stop",stopRingPendingIntent)
                .setFullScreenIntent(stopRingPendingIntent,true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setOngoing(true);

        // Get an instance of the Notification manager
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
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