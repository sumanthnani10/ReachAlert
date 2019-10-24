package com.ismartapps.reachalert;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class SplashActivityNotification extends Activity {
    private static final String TAG = "SAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.setContentView(R.layout.splash_activity);
        ImageView imageView = findViewById(R.id.splash_img);
        ImageView shadow = findViewById(R.id.splash_shadow);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.splash_animation);
        imageView.startAnimation(animation);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                shadow.setVisibility(View.VISIBLE);
                shadow.startAnimation(AnimationUtils.loadAnimation(SplashActivityNotification.this,R.anim.shadow_anim));
            }
        },1500);

        SharedPreferences sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Last Used").child(sharedPreferences.getString("dbname", "User Name")).child("Notification Clicked").setValue(DateFormat.getDateTimeInstance().format(new Date()));


        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(2000);
                }catch (InterruptedException e)
                {
                    Log.d(TAG, "run: "+e);
                }
                finally {
                    Intent intent = getIntent();
                    //NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    //notificationManager.cancel(intent.getExtras().getInt("id"));
                    String text = intent.getStringExtra("name");
                    Log.d(TAG, "onCreate: "+text+" , "+intent.getExtras());
                    double[] latLng = intent.getExtras().getDoubleArray("latlng");
                    Intent mainIntent = new Intent(SplashActivityNotification.this, LoginActivity.class);
                    mainIntent.putExtra("from","SAN");
                    mainIntent.putExtra("name",text);
                    mainIntent.putExtra("placeId",intent.getStringExtra("placeId"));
                    mainIntent.putExtra("latlng",latLng);
                    startActivity(mainIntent);
                    finish();

                }
            }
        };
        thread.start();

    }

    @Override
    public void onBackPressed(){
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}
