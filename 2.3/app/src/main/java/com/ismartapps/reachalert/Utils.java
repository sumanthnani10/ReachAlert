package com.ismartapps.reachalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;


class Utils {
    private static String TAG="Utils";

    private final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    private final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    private final static String CHANNEL_ID = "channel_01";
    private static NotificationManager mNotificationManager;

    static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MapsActivityFinal.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MapsActivityFinal.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //on Cancel Click
        Intent cancelIntent = new Intent(context,LocationUpdatesCancelReceiver.class);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context,0,cancelIntent,0);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.notification_small_icon)
                .setColor(Color.RED)
                .setContentTitle("Location Reach update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(false)
                .addAction(R.drawable.notification_small_icon,"Cancel",cancelPendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        // Get an instance of the Notification manager
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            mChannel.setShowBadge(true);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);


            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            // Channel ID
            builder.setChannelId(CHANNEL_ID);
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    static void sendNotificationOnComplete(Context context, String notificationDetails) {
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.notification_small_icon)
                .setColor(Color.GREEN)
                .setContentTitle("Location Reach update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent)
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

    static void playRing(Context context){
        Log.d(TAG, "playRing");
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(context,notification);
        if(!r.isPlaying()) r.play();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!r.isPlaying()) r.play();
                Log.d(TAG, "run ");
            }
        },1000,1000);
    }

    static void stopRing(Context context){
        Log.d(TAG, "Stopping Ring");
        if (r.isPlaying())
        {r.stop();}
        timer.cancel();
    }

    static void clearNotifications(){
        Log.d(TAG, "clearNotifications");
        mNotificationManager.cancelAll();
    }

    static String getLocationResultTitle(Context context,String distance) {
        return DateFormat.getDateTimeInstance().format(new Date())+" : "+distance+"m away";
    }

    private static String getLocationResultText(Context context, String distance) {
        String sb = distance + "m away";
        return sb;
    }

    static void setLocationUpdatesResult(Context context, String distance) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, distance) + "\n" + getLocationResultText(context, distance))
                .apply();
    }

    static String getLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }
}