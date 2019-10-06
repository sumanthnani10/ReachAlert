package com.ismartapps.reachalert;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivityFinal extends FragmentActivity implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private String TAG="Maps Activity Final";
    private TextView targetPlaceName,targetPlaceType,targetPlaceAddress;
    private CardView searchCard, radiusControlCard;
    private SeekBar radiusController;
    private Circle circle = null;
    private Marker marker = null;
    private ImageView mCurrLoc,zoomIn,zoomOut;
    private ImageView mCancel;
    private LatLng targetLatLng;
    private static FusedLocationProviderClient mFusedLocationClient;
    private static LocationRequest mLocationRequest;
    public static double[] tempd = new double[3];
    public static Activity activity=null;
    private static AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_primary);
        mapFragment.getMapAsync(this);
        activity=this;
        Log.d(TAG, "onCreate: Final");
    }

    private void initVars(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        targetPlaceName = (TextView) findViewById(R.id.place_name);
        targetPlaceType = (TextView) findViewById(R.id.place_type);
        targetPlaceAddress = (TextView) findViewById(R.id.place_address);
        mCurrLoc = (ImageView) findViewById(R.id.location_btn_img);
        searchCard = (CardView) findViewById(R.id.searchbar_layout_card);;
        mCancel = (ImageView) findViewById(R.id.place_tick_image);
        radiusControlCard = (CardView) findViewById(R.id.radius_controller_container_card);
        zoomIn = (ImageView) findViewById(R.id.zoom_in);
        zoomIn.setVisibility(View.INVISIBLE);
        zoomOut = (ImageView) findViewById(R.id.zoom_ot);
        zoomOut.setVisibility(View.INVISIBLE);
    }

    private void init() {
        Intent intent = getIntent();
        marker=null;
        circle=null;
        mCancel.setImageResource(R.mipmap.ic_launcher_cancel);
        mCancel.setVisibility(View.VISIBLE);
        targetPlaceName.setText(intent.getExtras().getString("targetPlaceName"));
        targetPlaceType.setText(getResources().getString(R.string.target_yet_to_reach));
        targetPlaceAddress.setText(intent.getExtras().getString("targetPlaceAddress"));
        radiusControlCard.setVisibility(View.INVISIBLE);
        mCurrLoc.setVisibility(View.INVISIBLE);
        searchCard.setVisibility(View.INVISIBLE);
        targetLatLng = intent.getExtras().getParcelable("targetLatLng");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng,15f));
        marker = mMap.addMarker(new MarkerOptions().position(targetLatLng).title(targetPlaceName.getText().toString()).draggable(false));
        circle = mMap.addCircle(new CircleOptions()
                .center(targetLatLng)
                .strokeWidth(3)
                .radius(intent.getExtras().getDouble("circleRadius"))
                .strokeColor(R.color.imageColor3));

        tempd[0]=targetLatLng.latitude;
        tempd[1]=targetLatLng.longitude;
        tempd[2]=circle.getRadius();

        startReceiver(this);

        mCancel.setOnLongClickListener(view -> {
            Toast.makeText(MapsActivityFinal.this, "Cancel Tracking", Toast.LENGTH_SHORT).show();
            return true;
        });

        mCancel.setOnClickListener(view -> {
            Toast.makeText(this, "Stopped Tracking", Toast.LENGTH_SHORT).show();
            //old-removeLocationUpdates(this);
            stopReceiver(this);
            Utils.clearNotifications();

            //Show Ad
            InterstitialAd mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
            mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("CA06D3EADBD276408AD961E110AFC903").build());

            mInterstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    if(mInterstitialAd.isLoaded()){
                        Log.d(TAG, "onAdLoaded: Showing Ad");
                        mInterstitialAd.show();
                    }
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Log.d(TAG, "onAdFailedToLoad");
                    finish();
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    Log.d(TAG, "onAdClosed");
                    finish();
                }
            });
        });

    }

    @Override
    public void onBackPressed(){
        stopReceiver(this);
        Utils.clearNotifications();

        InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6999236909954331/5210656040");
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("CA06D3EADBD276408AD961E110AFC903").build());

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if(mInterstitialAd.isLoaded()){
                    Log.d(TAG, "onAdLoaded: Showing Ad");
                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "onAdFailedToLoad: going to primary");
                finish();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.d(TAG, "onAdClosed: going to primary");
                finish();
            }
        });
    }

    public static double[] getInfo(){
        return tempd;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(8000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setMaxWaitTime(15000);
        Log.d(TAG, "createLocationRequest: Created");
    }

    private static PendingIntent getPendingIntentAlarm(Context context) {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        Log.d("TAG", "getPendingIntentAlarm: Created");
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getPendingIntentClient(Context context) {
        Intent intent = new Intent(context, LocationUpdatesClientReceiver.class);
        intent.setAction(LocationUpdatesClientReceiver.ACTION_PROCESS_UPDATES);
        Log.d("TAG", "getPendingIntentClient: Created");
        return PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startReceiver(Context context) {
        createLocationRequest();
        requestLocationUpdates(context);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Utils.sendNotification(context,"Loading");
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(),10000, getPendingIntentAlarm(context));
        Log.d(TAG, "startReceiver");
    }

    public static void stopReceiver(Context context){
        removeLocationUpdates(context);
        alarmManager.cancel(getPendingIntentAlarm(context));
        activity.finish();
        Log.d("MAF", "stopReciever");
    }

    public static void requestLocationUpdates(Context context) {
        try {
            Log.i("requestLocationUpdates", "Starting location updates");
            Utils.setRequestingLocationUpdates(context, true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntentClient(context));
        } catch (SecurityException e) {
            Utils.setRequestingLocationUpdates(context, false);
            e.printStackTrace();
        }
    }

    public static void removeLocationUpdates(Context context) {
        Log.i("TAG", "Removing location updates");
        Utils.setRequestingLocationUpdates(context, false);
        mFusedLocationClient.removeLocationUpdates(getPendingIntentClient(context));
        activity.finish();
    }

    public static void moveCamera(LatLng target, LatLng current){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(target);
        builder.include(current);
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: (Final) map is ready");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setPadding(0,0,0,400);
        initVars();
        init();
    }

}
