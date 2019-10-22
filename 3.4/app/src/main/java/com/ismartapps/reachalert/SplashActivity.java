package com.ismartapps.reachalert;

import android.app.Activity;
import android.content.Intent;
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

public class SplashActivity extends Activity {
    private static final String TAG = "SA";

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
                shadow.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this,R.anim.shadow_anim));
            }
        },1500);

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
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                intent.putExtra("from","SA");
                startActivity(intent);
                finish();

                }
            }
        };
        thread.start();

    }

}
