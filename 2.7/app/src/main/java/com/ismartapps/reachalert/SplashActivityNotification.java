package com.ismartapps.reachalert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashActivityNotification extends Activity {
    private static final String TAG = "SAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");
        Intent intent = getIntent();
        String text = intent.getStringExtra("name");
        Log.d(TAG, "onCreate: "+text+" , "+intent.getExtras());
        double[] latLng = intent.getExtras().getDoubleArray("latlng");
        Intent mainIntent = new Intent(this, LoginActivity.class);
        mainIntent.putExtra("from","SAN");
        mainIntent.putExtra("name",text);
        mainIntent.putExtra("placeId",intent.getStringExtra("placeId"));
        mainIntent.putExtra("latlng",latLng);
        startActivity(mainIntent);
        finish();
    }
}
