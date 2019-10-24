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
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SplashActivity extends Activity {
    private static final String TAG = "SA";
    private Intent intent,mainIntent;

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
        handler.postDelayed(() -> {
            shadow.setVisibility(View.VISIBLE);
            shadow.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this,R.anim.shadow_anim));
        },1500);

        mainIntent = getIntent();

        if(mainIntent.getBooleanExtra("fromNotification",false))
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(mainIntent.getExtras().getInt("id"));
        }

        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(2500);
                }catch (InterruptedException e)
                {
                    Log.d(TAG, "run: "+e);
                }
                finally {
                    SharedPreferences targetDetails = getSharedPreferences("targetDetails",MODE_PRIVATE);
                    if(!targetDetails.getBoolean("running",false))
                    {
                        if(mainIntent.getBooleanExtra("fromNotification",false))
                        {
                            SharedPreferences sharedPreferences = getSharedPreferences("userdetails",MODE_PRIVATE);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            databaseReference.child("Last Used").child(sharedPreferences.getString("dbname", "User Name")).child("Notification Clicked").setValue(new SimpleDateFormat("dd-MMM-yyyy,E hh:mm:ss a zzzz",new Locale("EN")).format(new Date()));
                            String text = mainIntent.getStringExtra("name");
                            Log.d(TAG, "onCreate: "+text+" , "+mainIntent.getExtras());
                            double[] latLng = mainIntent.getExtras().getDoubleArray("latlng");
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                            intent.putExtra("from","SAN");
                            intent.putExtra("name",text);
                            intent.putExtra("placeId",mainIntent.getStringExtra("placeId"));
                            intent.putExtra("latlng",latLng);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                            intent.putExtra("from","SA");
                        }
                    }
                    else
                    {
                        intent = new Intent(SplashActivity.this,MapsActivityFinal.class);
                        intent.putExtra("from",1);
                    }
                    startActivity(intent);
                    finish();

                }
            }
        };
        thread.start();

    }

}
