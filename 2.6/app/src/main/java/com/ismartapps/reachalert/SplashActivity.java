package com.ismartapps.reachalert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);
        Intent intent= new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
